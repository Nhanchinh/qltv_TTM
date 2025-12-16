/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.CardService;
import ui.DBConnect;
import java.text.NumberFormat;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import smartcard.CardConnectionManager;
import smartcard.CardKeyManager;
import smartcard.CardInfoManager;
import smartcard.CardImageManager;
import smartcard.CardUpdateManager;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.awt.Image;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.SwingWorker;

/**
 *
 * @author admin
 */
public class thongtincanhan extends javax.swing.JPanel {

    private CardService cardService;
    private String currentCardId = "CARD001";
    private boolean isEditing = false;
    private javax.swing.JLabel cardImageLabel;
    private javax.swing.JScrollPane scrollPane;
    private LoadingPanel loadingPanel;

    /**
     * Creates new form PersonalInfoPanel
     */
    public thongtincanhan() {
        cardService = new CardService();
        initComponents();
        loadCardInfo();
    }

    /**
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            loadCardInfo(); // Reload info với CardID mới
        }
    }

    /**
     * Load card information from database
     */
    /**
     * Helper class to hold data fetched from background
     */
    private class LoadResult {
        String cardIdFromCard;
        CardInfoManager.UserInfo userInfoFromCard;
        byte[] cardImageData;
        CardService.Card cardFromDB;
        double actualTotalSpent;
        boolean cardReadSuccess;
        String errorMessage;
    }

    /**
     * Load card information from database asynchronously
     */
    private void loadCardInfo() {
        // Show loading state
        showLoading(true);

        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() throws Exception {
                LoadResult result = new LoadResult();
                result.actualTotalSpent = 0;

                // 1. Thử lấy thông tin trực tiếp từ thẻ
                try {
                    CardConnectionManager connManager = CardConnectionManager.getInstance();
                    if (connManager.connectCard()) {
                        try {
                            CardKeyManager keyManager = new CardKeyManager(connManager.getChannel());
                            // keyManager.getPublicKey(); // Not strictly needed unless checking something

                            // Load app keypair
                            if (keyManager.loadAppKeyPair()) {
                                CardInfoManager infoManager = new CardInfoManager(connManager.getChannel(), keyManager);
                                result.userInfoFromCard = infoManager.getInfo();

                                if (result.userInfoFromCard != null && result.userInfoFromCard.cardId != null
                                        && !result.userInfoFromCard.cardId.isEmpty()) {
                                    result.cardIdFromCard = result.userInfoFromCard.cardId;
                                    result.cardReadSuccess = true;
                                }

                                // Lấy ảnh từ thẻ
                                System.out.println("[CARD_IMAGE] Đang lấy ảnh từ thẻ...");
                                CardImageManager imageManager = new CardImageManager(connManager.getChannel());
                                result.cardImageData = imageManager.downloadImage();
                            }
                        } finally {
                            connManager.disconnectCard();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[INFO] Không đọc được thẻ (sẽ lấy dữ liệu DB): " + e.getMessage());
                    result.errorMessage = e.getMessage();
                }

                // Determine effective CardID for DB lookup
                String dbCardId = (result.cardIdFromCard != null) ? result.cardIdFromCard : currentCardId;

                // 2. Lấy thông tin từ DB
                if (dbCardId != null && !dbCardId.isEmpty()) {
                    // Recalculate TotalSpent
                    cardService.recalculateTotalSpent(dbCardId);
                    result.cardFromDB = cardService.getCardById(dbCardId);
                    if (result.cardFromDB != null) {
                        result.actualTotalSpent = cardService.calculateTotalSpentFromHistory(result.cardFromDB.cardId);
                    }
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    LoadResult result = get();
                    updateUIWithData(result);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    showLoading(false);
                }
            }
        }.execute();
    }

    /**
     * Update UI components with loaded data
     */
    private void updateUIWithData(LoadResult result) {
        // Update Current ID if we got it from card
        if (result.cardIdFromCard != null) {
            currentCardId = result.cardIdFromCard;
        }

        displayCardImage(result.cardImageData);

        CardInfoManager.UserInfo userInfoFromCard = result.userInfoFromCard;
        CardService.Card card = result.cardFromDB;

        if (userInfoFromCard != null) {
            // ... Code to populate from Card Info ...
            System.out.println("[CARD_INFO] Thông tin lấy từ thẻ:");
            // (Logging skipped for brevity)

            cardIdField.setText(userInfoFromCard.cardId);
            nameField.setText(userInfoFromCard.name);
            phoneField.setText(userInfoFromCard.phone);
            addressField.setText(userInfoFromCard.address != null ? userInfoFromCard.address : "");

            // DOB Processing
            if (userInfoFromCard.dob != null && userInfoFromCard.dob.length() == 8) {
                String dob = userInfoFromCard.dob;
                dobField.setText(dob.substring(0, 2) + "/" + dob.substring(2, 4) + "/" + dob.substring(4));
            } else {
                dobField.setText(userInfoFromCard.dob != null ? userInfoFromCard.dob : "");
            }

            // RegDate Processing
            if (userInfoFromCard.regDate != null && userInfoFromCard.regDate.length() == 8) {
                String reg = userInfoFromCard.regDate;
                registerDateField.setText(reg.substring(0, 2) + "/" + reg.substring(2, 4) + "/" + reg.substring(4));
            } else {
                registerDateField.setText(userInfoFromCard.regDate != null ? userInfoFromCard.regDate : "");
            }

            if (userInfoFromCard != null) {
                memberTypeField.setText(userInfoFromCard.rank != null ? userInfoFromCard.rank : "");
            }
        } else if (card != null) {
            // Fallback: DB Data
            cardIdField.setText(card.cardId);
            nameField.setText(card.fullName);
            phoneField.setText(card.phone);
            addressField.setText(card.address != null ? card.address : "");

            // DOB Logic
            if (card.dob != null && !card.dob.isEmpty()) {
                try {
                    if (card.dob.contains("-")) {
                        String[] parts = card.dob.split("-");
                        if (parts.length == 3) {
                            dobField.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                        } else {
                            dobField.setText(card.dob);
                        }
                    } else {
                        dobField.setText(card.dob);
                    }
                } catch (Exception e) {
                    dobField.setText(card.dob);
                }
            } else {
                dobField.setText("");
            }

            // RegDate Logic
            if (card.registerDate != null && !card.registerDate.isEmpty()) {
                try {
                    if (card.registerDate.contains("-")) {
                        String[] parts = card.registerDate.split("-");
                        if (parts.length == 3) {
                            registerDateField.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                        } else {
                            registerDateField.setText(card.registerDate);
                        }
                    } else {
                        registerDateField.setText(card.registerDate);
                    }
                } catch (Exception e) {
                    registerDateField.setText(card.registerDate);
                }
            }
        } else {
            // No Data
            cardIdField.setText(currentCardId != null ? currentCardId : "");
            nameField.setText("");
            phoneField.setText("");
            addressField.setText("");
            dobField.setText("");
            registerDateField.setText("");
        }

        setFieldsEditable(false);
        isEditing = false;
        saveButton.setEnabled(false);

        // Member Info from DB
        if (card != null) {
            memberTypeField.setText(card.memberType != null ? card.memberType : "Basic");
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            totalSpentField.setText(nf.format(result.actualTotalSpent) + " đ");
            totalPointsField.setText(nf.format(card.totalPoints) + " điểm");
            fineDebtField.setText(nf.format(card.fineDebt) + " đ");
            isBlockedField.setText(card.isBlocked ? "Bị khóa" : "Hoạt động");
        } else {
            memberTypeField.setText("Basic");
            totalSpentField.setText("0 đ");
            totalPointsField.setText("0 điểm");
            fineDebtField.setText("0 đ");
            isBlockedField.setText("Hoạt động");
        }

        // Populate Rank Field based on Member Type
        String mType = memberTypeField.getText();
        String displayRank = "Thành viên (Normal)";
        if (mType != null) {
            if (mType.equalsIgnoreCase("Silver") || mType.equalsIgnoreCase("Bac")) {
                displayRank = "Bạc (Silver)";
            } else if (mType.equalsIgnoreCase("Gold") || mType.equalsIgnoreCase("Vang")) {
                displayRank = "Vàng (Gold)";
            } else if (mType.equalsIgnoreCase("Diamond") || mType.equalsIgnoreCase("KimCuong")
                    || mType.equalsIgnoreCase("Kim Cương")) {
                displayRank = "Kim cương (Diamond)";
            }
        }
        rankField.setText(displayRank);
    }

    private void showLoading(boolean loading) {
        if (loading) {
            if (scrollPane != null)
                remove(scrollPane);
            if (loadingPanel == null) {
                loadingPanel = new LoadingPanel();
            }
            add(loadingPanel, java.awt.BorderLayout.CENTER);
            loadingPanel.start();
        } else {
            if (loadingPanel != null) {
                loadingPanel.stop();
                remove(loadingPanel);
            }
            if (scrollPane != null)
                add(scrollPane, java.awt.BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    /**
     * Hiển thị ảnh thẻ
     */
    private void displayCardImage(byte[] imageData) {
        if (cardImageLabel == null)
            return;

        if (imageData != null && imageData.length > 2) {
            // Kiểm tra JPEG header (FF D8)
            boolean isValidJpeg = (imageData[0] & 0xFF) == 0xFF && (imageData[1] & 0xFF) == 0xD8;

            if (isValidJpeg) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
                    BufferedImage img = ImageIO.read(bais);
                    if (img != null) {
                        // Scale ảnh để fit vào label
                        int labelWidth = 200;
                        int labelHeight = 250;
                        Image scaledImg = img.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
                        cardImageLabel.setIcon(new javax.swing.ImageIcon(scaledImg));
                        cardImageLabel.setText("");
                        System.out.println("[CARD_IMAGE] Hiển thị ảnh thành công!");
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("[CARD_IMAGE] Lỗi đọc ảnh: " + e.getMessage());
                }
            } else {
                System.out.println("[CARD_IMAGE] Dữ liệu không phải JPEG (First bytes: " +
                        String.format("%02X %02X", imageData[0] & 0xFF, imageData[1] & 0xFF) + ")");
            }
        }

        // Không có ảnh hoặc ảnh không hợp lệ
        cardImageLabel.setIcon(null);
        cardImageLabel.setText("<html><center>Thẻ chưa<br>có ảnh</center></html>");
    }

    /**
     * Reload card info (public method for external refresh)
     */
    public void reloadCardInfo() {
        loadCardInfo();
    }

    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        phoneField.setEditable(editable);
        addressField.setEditable(editable);
        dobField.setEditable(editable);

        // Khi không ở chế độ chỉnh sửa thì cũng không cho focus để tránh hiện con trỏ
        // nháy
        nameField.setFocusable(editable);
        phoneField.setFocusable(editable);
        addressField.setFocusable(editable);
        dobField.setFocusable(editable);
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    private void initComponents() {

        // Setup Main Container
        setBackground(new java.awt.Color(248, 250, 252)); // Slate 50
        setLayout(new java.awt.BorderLayout(0, 0));

        // Header Section - Compact
        javax.swing.JPanel headerPanel = new javax.swing.JPanel();
        headerPanel.setBackground(new java.awt.Color(248, 250, 252));
        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 10, 30)); // Reduced padding

        titleLabel = new javax.swing.JLabel("Hồ sơ cá nhân");
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // Slightly smaller font
        titleLabel.setForeground(new java.awt.Color(15, 23, 42)); // Slate 900
        titleLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        javax.swing.JLabel subtitleLabel = new javax.swing.JLabel("Quản lý thông tin và tài khoản của bạn");
        subtitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        subtitleLabel.setForeground(new java.awt.Color(100, 116, 139)); // Slate 500
        subtitleLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(javax.swing.Box.createVerticalStrut(3));
        headerPanel.add(subtitleLabel);
        add(headerPanel, java.awt.BorderLayout.NORTH);

        // Content Scroll Pane
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setBackground(new java.awt.Color(248, 250, 252));
        contentPanel.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(0, 15, 15, 15); // Reduced insets
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;

        // --- Left Column: Profile Card ---
        javax.swing.JPanel leftPanel = createPanelWithShadow();
        leftPanel.setPreferredSize(new java.awt.Dimension(260, 420)); // More compact
        leftPanel.setLayout(new java.awt.BorderLayout(0, 15));
        leftPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Profile Image Area
        cardImageLabel = new javax.swing.JLabel();
        cardImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cardImageLabel.setPreferredSize(new java.awt.Dimension(220, 260));
        cardImageLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        cardImageLabel.setOpaque(true);
        cardImageLabel.setBackground(new java.awt.Color(241, 245, 249));
        cardImageLabel.setText(
                "<html><div style='text-align: center; color: #94a3b8;'>Chưa có ảnh<br>(No Image)</div></html>");

        // Upload Button
        uploadImageButton = createModernButton("Đổi ảnh đại diện", new java.awt.Color(255, 255, 255),
                new java.awt.Color(71, 85, 105));
        uploadImageButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(203, 213, 225)));
        uploadImageButton.setPreferredSize(new java.awt.Dimension(180, 35));
        uploadImageButton.addActionListener(this::uploadImageButtonActionPerformed);

        javax.swing.JPanel imgContainer = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        imgContainer.setOpaque(false);
        imgContainer.add(cardImageLabel, java.awt.BorderLayout.CENTER);
        imgContainer.add(uploadImageButton, java.awt.BorderLayout.SOUTH);

        leftPanel.add(imgContainer, java.awt.BorderLayout.NORTH);

        // --- Right Column: Info Forms ---
        javax.swing.JPanel rightPanel = new javax.swing.JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new javax.swing.BoxLayout(rightPanel, javax.swing.BoxLayout.Y_AXIS));

        // 1. Basic Info Section
        javax.swing.JPanel basicSection = createPanelWithShadow();
        basicSection.setLayout(new java.awt.BorderLayout());
        basicSection.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)); // Compact padding

        javax.swing.JLabel basicTitle = new javax.swing.JLabel("Thông tin cơ bản");
        basicTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        basicTitle.setForeground(new java.awt.Color(30, 41, 59));

        // Compact Grid Layout (3 rows, 2 cols, reduced gap)
        javax.swing.JPanel basicForm = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 15, 10));
        basicForm.setOpaque(false);

        cardIdField = createStyledTextField();
        nameField = createStyledTextField();
        phoneField = createStyledTextField();
        addressField = createStyledTextField();
        dobField = createStyledTextField();
        registerDateField = createStyledTextField();

        basicForm.add(createFormItem("Mã thẻ", cardIdField));
        basicForm.add(createFormItem("Họ và tên", nameField));
        basicForm.add(createFormItem("Số điện thoại", phoneField));
        basicForm.add(createFormItem("Địa chỉ", addressField));
        basicForm.add(createFormItem("Ngày sinh", dobField));
        basicForm.add(createFormItem("Ngày đăng ký", registerDateField));

        javax.swing.JPanel basicWrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
        basicWrapper.setOpaque(false);
        basicWrapper.add(basicTitle, java.awt.BorderLayout.NORTH);
        basicWrapper.add(javax.swing.Box.createVerticalStrut(15), java.awt.BorderLayout.CENTER);
        basicWrapper.add(basicForm, java.awt.BorderLayout.SOUTH);
        basicSection.add(basicWrapper);

        // 2. Member Info Section
        javax.swing.JPanel memberSection = createPanelWithShadow();
        memberSection.setLayout(new java.awt.BorderLayout());
        memberSection.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25));

        javax.swing.JLabel memberTitle = new javax.swing.JLabel("Thông tin hội viên");
        memberTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        memberTitle.setForeground(new java.awt.Color(30, 41, 59));

        javax.swing.JPanel memberForm = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 15, 10));
        memberForm.setOpaque(false);

        memberTypeField = createStyledTextField();
        totalSpentField = createStyledTextField();
        totalPointsField = createStyledTextField();
        fineDebtField = createStyledTextField();
        isBlockedField = createStyledTextField();
        rankField = createStyledTextField();

        memberForm.add(createFormItem("Loại hội viên", memberTypeField));
        memberForm.add(createFormItem("Hạng thẻ", rankField));
        memberForm.add(createFormItem("Tổng chi tiêu", totalSpentField));
        memberForm.add(createFormItem("Điểm tích lũy", totalPointsField));
        memberForm.add(createFormItem("Nợ phạt", fineDebtField));
        memberForm.add(createFormItem("Trạng thái", isBlockedField));

        javax.swing.JPanel memberWrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
        memberWrapper.setOpaque(false);
        memberWrapper.add(memberTitle, java.awt.BorderLayout.NORTH);
        memberWrapper.add(javax.swing.Box.createVerticalStrut(15), java.awt.BorderLayout.CENTER);
        memberWrapper.add(memberForm, java.awt.BorderLayout.SOUTH);
        memberSection.add(memberWrapper);

        rightPanel.add(basicSection);
        rightPanel.add(javax.swing.Box.createVerticalStrut(15));
        rightPanel.add(memberSection);

        // Add to main GridBag
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0; // Keep 0.0 to prevent vertical stretching
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; // Fill horizontally but not vertically
        gbc.anchor = java.awt.GridBagConstraints.NORTH; // Anchor to top
        contentPanel.add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // Right panel can expand
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
        contentPanel.add(rightPanel, gbc);

        // ScrollPane Setup
        // ScrollPane Setup
        scrollPane = new javax.swing.JScrollPane(contentPanel); // Assign to field

        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Custom Modern ScrollBar UI
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scrollPane, java.awt.BorderLayout.CENTER);

        // Bottom Action Bar
        javax.swing.JPanel actionPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 15));
        actionPanel.setBackground(new java.awt.Color(248, 250, 252));
        actionPanel
                .setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(226, 232, 240))); // Top
                                                                                                                        // border
                                                                                                                        // separator

        editButton = createModernButton("Chỉnh sửa thông tin", new java.awt.Color(255, 255, 255),
                new java.awt.Color(37, 99, 235));
        editButton.setBackground(new java.awt.Color(37, 99, 235));
        editButton.setForeground(java.awt.Color.WHITE);
        editButton.addActionListener(this::editButtonActionPerformed);

        saveButton = createModernButton("Lưu thay đổi", new java.awt.Color(255, 255, 255),
                new java.awt.Color(22, 163, 74));
        saveButton.setBackground(new java.awt.Color(22, 163, 74));
        saveButton.setForeground(java.awt.Color.WHITE);
        saveButton.setEnabled(false);
        saveButton.addActionListener(this::saveButtonActionPerformed);

        actionPanel.add(editButton);
        actionPanel.add(saveButton);

        add(actionPanel, java.awt.BorderLayout.SOUTH);
    }

    // Helpers for styling
    private javax.swing.JPanel createPanelWithShadow() {
        javax.swing.JPanel p = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); // Smaller radius
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private javax.swing.JPanel createFormItem(String label, javax.swing.JComponent field) {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4)); // Reduced gap
        p.setOpaque(false);
        javax.swing.JLabel l = new javax.swing.JLabel(label);
        l.setFont(new java.awt.Font("Segoe UI", 1, 12)); // Smaller font for label
        l.setForeground(new java.awt.Color(100, 116, 139));
        p.add(l, java.awt.BorderLayout.NORTH);
        p.add(field, java.awt.BorderLayout.CENTER);
        return p;
    }

    private javax.swing.JTextField createStyledTextField() {
        javax.swing.JTextField f = new javax.swing.JTextField();
        f.setFont(new java.awt.Font("Segoe UI", 0, 13)); // Smaller font
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10) // Reduced padding
        ));
        f.setBackground(new java.awt.Color(248, 250, 252));
        return f;
    }

    private javax.swing.JButton createModernButton(String text, java.awt.Color fg, java.awt.Color bg) {
        javax.swing.JButton b = new javax.swing.JButton(text);
        b.setFont(new java.awt.Font("Segoe UI", 1, 13)); // Smaller font
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        b.setPreferredSize(new java.awt.Dimension(160, 35)); // Compact size
        return b;
    }

    /**
     * Modern Custom ScrollBar UI
     */
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new java.awt.Color(203, 213, 225); // Slate 300
            this.trackColor = new java.awt.Color(248, 250, 252); // Match bg
        }

        @Override
        protected javax.swing.JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected javax.swing.JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private javax.swing.JButton createZeroButton() {
            javax.swing.JButton jbutton = new javax.swing.JButton();
            jbutton.setPreferredSize(new java.awt.Dimension(0, 0));
            jbutton.setMinimumSize(new java.awt.Dimension(0, 0));
            jbutton.setMaximumSize(new java.awt.Dimension(0, 0));
            return jbutton;
        }

        @Override
        protected void paintThumb(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
                return;
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            // Draw rounded pill
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {
        isEditing = !isEditing;
        setFieldsEditable(isEditing);
        saveButton.setEnabled(isEditing);
        if (!isEditing) {
            // Hủy chỉnh sửa -> reload lại dữ liệu từ DB/thẻ
            loadCardInfo();
        }
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveButtonActionPerformed
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String dob = dobField.getText().trim();
        String address = addressField.getText().trim();

        // Validate
        if (name.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Vui long nhap ho va ten!",
                    "Loi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (phone.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Vui long nhap so dien thoai!",
                    "Loi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (address.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Vui long nhap dia chi!",
                    "Loi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update to database
        boolean dbUpdateSuccess = false;
        try {
            Connection dbConn = DBConnect.getConnection();
            if (dbConn != null) {
                // Convert date format from DD/MM/YYYY to YYYY-MM-DD
                String dobFormatted = dob;
                if (!dob.isEmpty() && dob.contains("/")) {
                    String[] parts = dob.split("/");
                    if (parts.length == 3) {
                        dobFormatted = parts[2] + "-" + parts[1] + "-" + parts[0];
                    }
                }

                String sql = "UPDATE Cards SET FullName = ?, Phone = ?, Address = ?, DOB = ? WHERE CardID = ?";
                try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, phone);
                    pstmt.setString(3, address);
                    pstmt.setString(4, dobFormatted.isEmpty() ? null : dobFormatted);
                    pstmt.setString(5, currentCardId);

                    if (pstmt.executeUpdate() > 0) {
                        dbUpdateSuccess = true;
                        System.out.println("[DB] Đã lưu thông tin vào database");
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Loi khi luu thong tin vao database!",
                                "Loi",
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Khong the ket noi database!",
                        "Loi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Loi khi luu thong tin vao database: " + e.getMessage(),
                    "Loi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        // Update to card if database update was successful
        if (dbUpdateSuccess) {
            try {
                System.out.println("[CARD] Đang cập nhật thông tin lên thẻ...");
                CardConnectionManager connManager = CardConnectionManager.getInstance();
                connManager.connectCard();
                try {
                    CardUpdateManager updateManager = new CardUpdateManager(connManager.getChannel());

                    // Update card with new information
                    boolean cardUpdateSuccess = updateManager.updateInfo(name, dob, phone, address);

                    if (cardUpdateSuccess) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Da luu thong tin thanh cong!\n(Database + The)",
                                "Thong bao",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        loadCardInfo(); // Reload to show updated data
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Da luu vao database nhung cap nhat the that bai!\nVui long thu lai sau.",
                                "Canh bao",
                                javax.swing.JOptionPane.WARNING_MESSAGE);
                    }
                } finally {
                    connManager.disconnectCard();
                }
            } catch (Exception e) {
                System.err.println("[CARD] Lỗi khi cập nhật thẻ: " + e.getMessage());
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Da luu vao database nhung cap nhat the that bai:\n" + e.getMessage() +
                                "\n\nVui long kiem tra ket noi the va thu lai.",
                        "Canh bao",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
            }
        }
    }// GEN-LAST:event_saveButtonActionPerformed

    /**
     * Handle upload image button click
     */
    private void uploadImageButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // Create file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh để upload lên thẻ");

        // Set file filter for images only
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Ảnh (JPG, JPEG, PNG)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Show open dialog
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("[UPLOAD_IMAGE] Selected file: " + selectedFile.getAbsolutePath());

            // Show confirmation dialog
            int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn thay đổi ảnh thẻ?\nẢnh: " + selectedFile.getName() +
                            "\nKích thước: " + (selectedFile.length() / 1024) + " KB",
                    "Xác nhận",
                    javax.swing.JOptionPane.YES_NO_OPTION);

            if (confirm != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }

            // Upload image to card
            try {
                System.out.println("[UPLOAD_IMAGE] Connecting to card...");
                CardConnectionManager connManager = CardConnectionManager.getInstance();
                connManager.connectCard();

                try {
                    CardImageManager imageManager = new CardImageManager(connManager.getChannel());

                    System.out.println("[UPLOAD_IMAGE] Uploading image...");
                    boolean success = imageManager.uploadImage(selectedFile);

                    if (success) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Upload ảnh thành công!",
                                "Thông báo",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);

                        // Reload card info to display new image
                        loadCardInfo();
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Upload ảnh thất bại!",
                                "Lỗi",
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    connManager.disconnectCard();
                }
            } catch (Exception e) {
                System.err.println("[UPLOAD_IMAGE] Error: " + e.getMessage());
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Lỗi khi upload ảnh:\n" + e.getMessage(),
                        "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField phoneField;
    private javax.swing.JTextField addressField;
    private javax.swing.JTextField dobField;
    private javax.swing.JTextField registerDateField;
    private javax.swing.JTextField memberTypeField;
    private javax.swing.JTextField totalSpentField;
    private javax.swing.JTextField totalPointsField;
    private javax.swing.JTextField fineDebtField;
    private javax.swing.JTextField isBlockedField;
    private javax.swing.JTextField rankField;
    private javax.swing.JButton editButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton uploadImageButton;

    /**
     * Custom Loading Panel with Spinner
     */
    private class LoadingPanel extends javax.swing.JPanel {
        private javax.swing.Timer timer;
        private int angle = 0;

        public LoadingPanel() {
            setOpaque(false);
            setLayout(new java.awt.BorderLayout());
            timer = new javax.swing.Timer(40, e -> {
                angle = (angle + 12) % 360;
                repaint();
            });
        }

        public void start() {
            timer.start();
        }

        public void stop() {
            timer.stop();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int r = 25;

            // Draw text
            g2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            g2.setColor(new java.awt.Color(100, 116, 139));
            String text = "Đang tải dữ liệu...";
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, cx - fm.stringWidth(text) / 2, cy + r + 35);

            // Draw Spinner
            // Track
            g2.setStroke(new java.awt.BasicStroke(4, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.setColor(new java.awt.Color(226, 232, 240));
            g2.drawOval(cx - r, cy - r, 2 * r, 2 * r);

            // Indicator
            g2.setColor(new java.awt.Color(37, 99, 235));
            g2.drawArc(cx - r, cy - r, 2 * r, 2 * r, angle, 100);
        }
    }
}
