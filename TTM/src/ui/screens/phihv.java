/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.CardService;
import services.TransactionService;
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Modernized Membership Fee Panel
 */
public class phihv extends javax.swing.JPanel {

    private CardService cardService;
    private TransactionService transactionService;
    private String currentCardId = "CARD001";
    private String selectedPackageName = "";
    private double selectedPackagePrice = 0;
    private int selectedDiscount = 0;
    private int selectedMonths = 0;

    // Rank hiện tại và giá tương ứng
    private String currentRank = "Normal"; // Normal/Silver/Gold/Diamond
    private int currentRankPrice = 0; // Giá của rank hiện tại

    // Bảng giá các rank (giá gốc)
    private static final int PRICE_NORMAL = 0;
    private static final int PRICE_SILVER = 100000;
    private static final int PRICE_GOLD = 200000;
    private static final int PRICE_DIAMOND = 300000;

    // UI Components
    private javax.swing.JTextField cardIdField;
    private javax.swing.JTextField memberStatusField;
    private javax.swing.JTextField expiryDateField;
    private javax.swing.JTextField selectedPackageField;
    private javax.swing.JTextField discountField;
    private javax.swing.JTextField totalField;
    private javax.swing.JButton paymentButton;
    private javax.swing.JPanel packagesWrapper; // Để refresh grid khi load card info
    private javax.swing.JScrollPane packagesScrollPane; // Reference để có thể thay thế

    public phihv() {
        cardService = new CardService();
        transactionService = new TransactionService();
        initComponents();
        loadCardInfo();
    }

    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            loadCardInfo();
        }
    }

    private void loadCardInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);

        // Mặc định rank = Normal
        currentRank = "Normal";
        currentRankPrice = PRICE_NORMAL;
        String displayRank = "Thành viên (Normal)";

        // Đọc rank từ thẻ chip
        try {
            CardConnectionManager connManager = CardConnectionManager.getInstance();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                // CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                // Note: Logic logic reading member type from card would go here
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[PHIHV] Error reading card: " + e.getMessage());
        }

        if (card != null) {
            if (cardIdField != null)
                cardIdField.setText(card.cardId);

            String memberType = card.memberType;
            if (memberType != null && !memberType.isEmpty()) {
                if (memberType.equalsIgnoreCase("Normal") || memberType.equals("ThanhVien")) {
                    currentRank = "Normal";
                    currentRankPrice = PRICE_NORMAL;
                    displayRank = "Thành viên (Normal)";
                } else if (memberType.equalsIgnoreCase("Silver") || memberType.equals("Bac")) {
                    currentRank = "Silver";
                    currentRankPrice = PRICE_SILVER;
                    displayRank = "Bạc (Silver)";
                } else if (memberType.equalsIgnoreCase("Gold") || memberType.equals("Vang")) {
                    currentRank = "Gold";
                    currentRankPrice = PRICE_GOLD;
                    displayRank = "Vàng (Gold)";
                } else if (memberType.equalsIgnoreCase("Diamond") || memberType.equals("KimCuong")) {
                    currentRank = "Diamond";
                    currentRankPrice = PRICE_DIAMOND;
                    displayRank = "Kim cương (Diamond)";
                }
            }

            if (memberStatusField != null)
                memberStatusField.setText(displayRank);

            if (card.registerDate != null && !card.registerDate.isEmpty() && !currentRank.equals("Normal")) {
                try {
                    LocalDate registerDate = LocalDate.parse(card.registerDate);
                    int months = 3;
                    LocalDate expiryDate = registerDate.plusMonths(months);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    if (expiryDateField != null)
                        expiryDateField.setText(expiryDate.format(formatter));
                } catch (Exception e) {
                    if (expiryDateField != null)
                        expiryDateField.setText("--");
                }
            } else {
                if (expiryDateField != null)
                    expiryDateField.setText(currentRank.equals("Normal") ? "Không giới hạn" : "--");
            }
        } else {
            if (cardIdField != null)
                cardIdField.setText(currentCardId);
            if (memberStatusField != null)
                memberStatusField.setText("Thành viên (Normal)");
            if (expiryDateField != null)
                expiryDateField.setText("Không giới hạn");
        }

        // Refresh packages grid để cập nhật nút "Đang sử dụng"
        refreshPackagesGrid();
    }

    private void refreshPackagesGrid() {
        if (packagesWrapper != null && packagesScrollPane != null) {
            packagesWrapper.remove(packagesScrollPane);
            packagesScrollPane = createPackagesGrid();
            packagesWrapper.add(packagesScrollPane, java.awt.BorderLayout.CENTER);
            packagesWrapper.revalidate();
            packagesWrapper.repaint();
        }
    }

    public void reloadCardInfo() {
        loadCardInfo();
    }

    private void initComponents() {
        setBackground(new java.awt.Color(248, 250, 252)); // Slate 50
        setLayout(new java.awt.BorderLayout(0, 0));

        // 1. Header
        add(createHeaderPanel(), java.awt.BorderLayout.NORTH);

        // 2. Content
        javax.swing.JPanel contentPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // LEFT: Package Selection (65%)
        gbc.gridx = 0;
        gbc.weightx = 0.6; // Reduced from 0.65
        gbc.insets = new java.awt.Insets(0, 0, 0, 15);

        packagesWrapper = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        packagesWrapper.setOpaque(false);

        javax.swing.JLabel pkgTitle = new javax.swing.JLabel("Bảng giá gói hội viên");
        pkgTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        pkgTitle.setForeground(new java.awt.Color(30, 41, 59));
        packagesWrapper.add(pkgTitle, java.awt.BorderLayout.NORTH);

        packagesScrollPane = createPackagesGrid();
        packagesWrapper.add(packagesScrollPane, java.awt.BorderLayout.CENTER);

        contentPanel.add(packagesWrapper, gbc);

        // RIGHT: Payment Info (35%) -> Increased to 40%
        gbc.gridx = 1;
        gbc.weightx = 0.4; // Increased from 0.35
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);

        contentPanel.add(createPaymentPanel(), gbc);

        add(contentPanel, java.awt.BorderLayout.CENTER);
    }

    // --- UI Creators ---

    private javax.swing.JPanel createHeaderPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));
        p.setPreferredSize(new java.awt.Dimension(0, 70));

        javax.swing.JLabel title = new javax.swing.JLabel("Phí Hội Viên");
        title.setFont(new java.awt.Font("Segoe UI", 1, 24));
        title.setForeground(new java.awt.Color(15, 23, 42));
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 25, 0, 0));

        p.add(title, java.awt.BorderLayout.WEST);
        return p;
    }

    private javax.swing.JScrollPane createPackagesGrid() {
        javax.swing.JPanel grid = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 20, 20)); // Grid with more
                                                                                                 // spacing
        grid.setOpaque(false);
        grid.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 20, 5)); // space for shadow

        // 1. Normal
        grid.add(createPremiumCard("Thành Viên", "Miễn phí", "Trọn đời",
                new String[] { "Thuê tối đa 3 quyển", "1 lượt thuê miễn phí/tháng", "Giữ sách 14 ngày" },
                new java.awt.Color(100, 116, 139), new java.awt.Color(241, 245, 249),
                "ThanhVien", 0, 0, 0));

        // 2. Silver
        grid.add(createPremiumCard("Hạng Bạc", "100.000đ", "/3 tháng",
                new String[] { "Thuê tối đa 5 quyển", "3 lượt thuê miễn phí/tháng", "Giảm 3% hóa đơn",
                        "Tích 3% điểm thưởng" },
                new java.awt.Color(51, 65, 85), new java.awt.Color(226, 232, 240),
                "Bac", 100000, 0, 3));

        // 3. Gold
        grid.add(createPremiumCard("Hạng Vàng", "200.000đ", "/3 tháng",
                new String[] { "Thuê tối đa 10 quyển", "5 lượt thuê miễn phí/tháng", "Giảm 5% hóa đơn",
                        "Tích 5% điểm thưởng" },
                new java.awt.Color(180, 83, 9), new java.awt.Color(254, 243, 199),
                "Vang", 200000, 0, 3));

        // 4. Diamond
        grid.add(createPremiumCard("Kim Cương", "300.000đ", "/3 tháng",
                new String[] { "Thuê tối đa 15 quyển", "10 lượt thuê miễn phí/tháng", "Giảm 10% hóa đơn",
                        "Tích 10% điểm thưởng" },
                new java.awt.Color(126, 34, 206), new java.awt.Color(243, 232, 255),
                "KimCuong", 300000, 0, 3));

        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        // Fix scrolling speed
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /**
     * Creates a premium-looking pricing card.
     */
    private javax.swing.JPanel createPremiumCard(String tierName, String price, String duration, String[] benefits,
            java.awt.Color themeColor, java.awt.Color bgColor,
            String pkgCode, int pkgPrice, int disc, int months) {
        javax.swing.JPanel p = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                // Card Background
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                // Header Background (Colored top part)
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, 60, 20, 20);
                g2.fillRect(0, 40, getWidth() - 1, 20); // Square off bottom corners of header

                // Border
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                // Selection Border (if needed - logic to update this can be added)
                // if selected... g2.setColor(themeColor); g2.setStroke(...);

                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setLayout(new java.awt.BorderLayout());

        // 1. Header (Title + Price)
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 10, 20));

        javax.swing.JLabel lblTitle = new javax.swing.JLabel(tierName.toUpperCase());
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblTitle.setForeground(themeColor);

        // Rich text price
        javax.swing.JLabel lblPrice = new javax.swing.JLabel("<html><span style='font-size:18px'>" + price
                + "</span> <span style='font-size:10px;color:gray'>" + duration + "</span></html>");
        lblPrice.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblPrice.setForeground(new java.awt.Color(15, 23, 42)); // Slate 900

        header.add(lblTitle);
        header.add(lblPrice);

        // 2. Body (Features list)
        javax.swing.JPanel body = new javax.swing.JPanel();
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (String benefit : benefits) {
            javax.swing.JLabel lbl = new javax.swing.JLabel("• " + benefit);
            lbl.setFont(new java.awt.Font("Segoe UI", 0, 13));
            lbl.setForeground(new java.awt.Color(51, 65, 85));
            lbl.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 4, 0));
            body.add(lbl);
        }

        // 3. Footer (Button)
        javax.swing.JPanel footer = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Kiểm tra xem đây có phải là gói hiện tại không
        String cardRankCode = "";
        if (currentRank.equals("Normal"))
            cardRankCode = "ThanhVien";
        else if (currentRank.equals("Silver"))
            cardRankCode = "Bac";
        else if (currentRank.equals("Gold"))
            cardRankCode = "Vang";
        else if (currentRank.equals("Diamond"))
            cardRankCode = "KimCuong";

        boolean isCurrentPkg = pkgCode.equals(cardRankCode);

        javax.swing.JButton btn;
        if (isCurrentPkg) {
            btn = new javax.swing.JButton("Đang sử dụng");
            btn.setFont(new java.awt.Font("Segoe UI", 1, 14));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(new java.awt.Color(34, 197, 94)); // Green
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setPreferredSize(new java.awt.Dimension(130, 36));
            btn.setEnabled(false); // Disable nút
        } else {
            btn = new javax.swing.JButton("Chọn gói");
            btn.setFont(new java.awt.Font("Segoe UI", 1, 14));
            btn.setForeground(java.awt.Color.WHITE);
            btn.setBackground(themeColor);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setPreferredSize(new java.awt.Dimension(120, 36));
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Hover effect helper
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(themeColor.darker());
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(themeColor);
                }
            });

            btn.addActionListener(e -> selectPackage(pkgCode, pkgPrice, disc, months));
        }
        footer.add(btn);

        p.add(header, java.awt.BorderLayout.NORTH);
        p.add(body, java.awt.BorderLayout.CENTER);
        p.add(footer, java.awt.BorderLayout.SOUTH);

        return p;
    }

    private javax.swing.JPanel createPaymentPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.GridBagLayout());
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 0, 5, 0);

        // Header
        javax.swing.JLabel header = new javax.swing.JLabel("Tóm tắt thanh toán");
        header.setFont(new java.awt.Font("Segoe UI", 1, 18));
        header.setForeground(new java.awt.Color(30, 41, 59));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(0, 0, 20, 0);
        p.add(header, gbc);

        // Reset
        gbc.gridwidth = 1;
        gbc.insets = new java.awt.Insets(5, 0, 5, 0);

        addFormRow(p, gbc, 1, "Mã thẻ:", cardIdField = createStyledTextField());
        addFormRow(p, gbc, 2, "Hạng hiện tại:", memberStatusField = createStyledTextField());
        addFormRow(p, gbc, 3, "Hết hạn:", expiryDateField = createStyledTextField());

        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(new java.awt.Color(226, 232, 240));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(15, 0, 15, 0);
        p.add(sep, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new java.awt.Insets(5, 0, 5, 0);

        addFormRow(p, gbc, 5, "Gói đã chọn:", selectedPackageField = createStyledTextField());
        addFormRow(p, gbc, 6, "Chiết khấu:", discountField = createStyledTextField());

        // Total
        gbc.gridx = 0;
        gbc.gridy = 7;
        javax.swing.JLabel lblTotal = new javax.swing.JLabel("Tổng tiền:");
        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 15));
        lblTotal.setForeground(new java.awt.Color(30, 41, 59));
        p.add(lblTotal, gbc);

        gbc.gridx = 1;
        totalField = new javax.swing.JTextField("0 đ");
        totalField.setFont(new java.awt.Font("Segoe UI", 1, 20)); // Bigger font
        totalField.setForeground(new java.awt.Color(22, 163, 74)); // Green
        totalField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        totalField.setBorder(null);
        totalField.setOpaque(false);
        totalField.setEditable(false);
        p.add(totalField, gbc);

        // Readonly fields setup
        cardIdField.setEditable(false);
        memberStatusField.setEditable(false);
        expiryDateField.setEditable(false);
        selectedPackageField.setEditable(false);
        discountField.setEditable(false);

        // Pay Button
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(25, 0, 0, 0);

        paymentButton = createModernButton("THANH TOÁN NGAY", java.awt.Color.WHITE, new java.awt.Color(37, 99, 235)); // Blue
                                                                                                                      // 600
        paymentButton.setPreferredSize(new java.awt.Dimension(0, 50)); // Taller button
        paymentButton.addActionListener(e -> processPayment());

        p.add(paymentButton, gbc);

        // Spacer to push content up
        gbc.gridy = 9;
        gbc.weighty = 1.0;
        p.add(new javax.swing.JLabel(), gbc);

        return p;
    }

    private void addFormRow(javax.swing.JPanel p, java.awt.GridBagConstraints gbc, int row, String label,
            javax.swing.JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3; // sticky label
        javax.swing.JLabel lbl = new javax.swing.JLabel(label);
        lbl.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lbl.setForeground(new java.awt.Color(100, 116, 139));
        p.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        comp.setPreferredSize(new java.awt.Dimension(0, 38)); // Slightly taller fields
        comp.setMinimumSize(new java.awt.Dimension(180, 38)); // Ensure minimum width so text isn't hidden
        p.add(comp, gbc);
    }

    // --- Helpers (Copied from other screens for consistency) ---

    private javax.swing.JPanel createPanelWithShadow() {
        javax.swing.JPanel p = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private javax.swing.JTextField createStyledTextField() {
        javax.swing.JTextField f = new javax.swing.JTextField();
        f.setFont(new java.awt.Font("Segoe UI", 0, 14));
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        f.setBackground(new java.awt.Color(248, 250, 252));
        return f;
    }

    private javax.swing.JButton createModernButton(String text, java.awt.Color fg, java.awt.Color bg) {
        javax.swing.JButton b = new javax.swing.JButton(text);
        b.setFont(new java.awt.Font("Segoe UI", 1, 14));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    // Modern ScrollBar
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new java.awt.Color(203, 213, 225);
            this.trackColor = new java.awt.Color(248, 250, 252);
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
            javax.swing.JButton b = new javax.swing.JButton();
            b.setPreferredSize(new java.awt.Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled())
                return;
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(java.awt.Graphics g, javax.swing.JComponent c, java.awt.Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    // --- Business Logic (Preserved) ---

    private void selectPackage(String packageName, int fullPrice, int discount, int months) {
        // Tính giá gói mới
        int newRankPrice = 0;
        String newRank = "Normal";
        if (packageName.equals("ThanhVien")) {
            newRankPrice = PRICE_NORMAL;
            newRank = "Normal";
        } else if (packageName.equals("Bac")) {
            newRankPrice = PRICE_SILVER;
            newRank = "Silver";
        } else if (packageName.equals("Vang")) {
            newRankPrice = PRICE_GOLD;
            newRank = "Gold";
        } else if (packageName.equals("KimCuong")) {
            newRankPrice = PRICE_DIAMOND;
            newRank = "Diamond";
        }

        // Kiểm tra xem rank mới có cao hơn rank hiện tại không
        int currentRankLevel = getRankLevel(currentRank);
        int newRankLevel = getRankLevel(newRank);

        if (newRankLevel < currentRankLevel) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Bạn đã là hạng " + currentRank + "!\nKhông thể hạ cấp xuống hạng thấp hơn.",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newRankLevel == currentRankLevel && newRankLevel > 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Bạn đang ở hạng " + currentRank + ".\nVui lòng chọn gói cao hơn để nâng cấp.",
                    "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Tính phí chênh lệch
        int upgradeCost = newRankPrice - currentRankPrice;
        if (upgradeCost < 0)
            upgradeCost = 0;

        selectedPackageName = packageName;
        selectedPackagePrice = upgradeCost;
        selectedDiscount = discount;
        selectedMonths = months;

        // Display package name in Vietnamese
        String displayName = packageName;
        if (packageName.equals("ThanhVien"))
            displayName = "Thành viên";
        else if (packageName.equals("Bac"))
            displayName = "Bạc";
        else if (packageName.equals("Vang"))
            displayName = "Vàng";
        else if (packageName.equals("KimCuong"))
            displayName = "Kim cương";

        selectedPackageField.setText(displayName);
        discountField.setText(discount + "%");

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        if (upgradeCost == 0) {
            totalField.setText("Miễn phí");
        } else {
            totalField.setText(nf.format(upgradeCost) + " đ");
        }
    }

    private int getRankLevel(String rank) {
        if (rank.equals("Normal"))
            return 0;
        if (rank.equals("Silver"))
            return 1;
        if (rank.equals("Gold"))
            return 2;
        if (rank.equals("Diamond"))
            return 3;
        return 0;
    }

    private void processPayment() {
        if (selectedPackageName.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn gói hội viên!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Added for Smart Card check ---
        if (selectedPackagePrice > 0) {
            int cardBalance = 0;
            try {
                CardConnectionManager connManager = CardConnectionManager.getInstance();
                if (connManager.connectCard()) {
                    CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                    CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                    if (info.success) {
                        cardBalance = info.balance;
                        System.out.println("[PHIHV] Card balance: " + cardBalance + " VND");
                    }
                    connManager.disconnectCard();
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối thẻ!", "Lỗi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi đọc thẻ: " + e.getMessage(), "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cardBalance < selectedPackagePrice) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Số dư thẻ không đủ!\nSố dư hiện tại: "
                                + NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(cardBalance)
                                + " đ\nCần: " +
                                NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(selectedPackagePrice)
                                + " đ",
                        "Thông báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        // ------------------------------------

        String confirmMessage;
        if (selectedPackagePrice == 0) {
            confirmMessage = "Xác nhận đăng ký gói hội viên?\nGói: " + selectedPackageField.getText()
                    + "\nGiá: Miễn phí";
        } else {
            confirmMessage = "Xác nhận thanh toán gói hội viên?\nGói: " + selectedPackageField.getText() + "\nGiá: " +
                    NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(selectedPackagePrice) + " đ";
        }

        int option = javax.swing.JOptionPane.showConfirmDialog(this,
                confirmMessage,
                "Xác nhận",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Yêu cầu xác nhận mã PIN
        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof java.awt.Frame) {
            PinConfirmDialog pinDialog = new PinConfirmDialog((java.awt.Frame) parentWindow);
            pinDialog.setVisible(true);

            if (!pinDialog.isConfirmed()) {
                return; // Người dùng hủy hoặc nhập sai PIN
            }
        }

        performUpgrade();
    }

    private void performUpgrade() {
        // Logic trừ tiền (nếu có) và cập nhật DB, thẻ
        boolean paymentSuccess = false;
        boolean cardUpgradeSuccess = false;

        // Xác định rank để upgrade trên thẻ
        String cardRank = "Normal";
        if (selectedPackageName.equals("Bac")) {
            cardRank = "Silver";
        } else if (selectedPackageName.equals("Vang")) {
            cardRank = "Gold";
        } else if (selectedPackageName.equals("KimCuong")) {
            cardRank = "Diamond";
        }

        try {
            CardConnectionManager connManager = CardConnectionManager.getInstance();
            if (!connManager.connectCard()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối thẻ!", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

            // 1. Nếu có phí, trừ thẻ trước
            if (selectedPackagePrice > 0) {
                paymentSuccess = balanceManager.payment((int) selectedPackagePrice);
                if (!paymentSuccess) {
                    connManager.disconnectCard();
                    javax.swing.JOptionPane.showMessageDialog(this, "Thanh toán bị từ chối bởi thẻ!", "Thất bại",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                System.out.println("[PHIHV] Payment SUCCESS: " + selectedPackagePrice + " đ");
            } else {
                paymentSuccess = true; // Free upgrade
            }

            // 2. Gọi upgradeRank() trên thẻ để cập nhật giới hạn mượn sách
            if (paymentSuccess && !cardRank.equals("Normal")) {
                System.out.println("[PHIHV] Upgrading card to: " + cardRank);
                cardUpgradeSuccess = balanceManager.upgradeRank(cardRank);

                if (!cardUpgradeSuccess) {
                    System.err.println("[PHIHV] Card upgrade FAILED! Rank may not be updated on card.");
                    // Vẫn tiếp tục cập nhật DB (có thể retry sau)
                } else {
                    System.out.println("[PHIHV] Card upgrade SUCCESS to: " + cardRank);
                }
            } else {
                cardUpgradeSuccess = true; // Normal không cần upgrade trên thẻ
            }

            connManager.disconnectCard();

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi kết nối thẻ khi nâng cấp: " + e.getMessage(), "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Cập nhật DB
        if (paymentSuccess) {
            boolean dbUpdated = cardService.updateMemberType(currentCardId, selectedPackageName);
            if (dbUpdated) {
                // 4. Log transaction if paid
                if (selectedPackagePrice > 0) {
                    String transId = java.util.UUID.randomUUID().toString();
                    transactionService.createTransaction(transId, currentCardId, "MembershipFee", selectedPackagePrice,
                            0);
                }

                String message = "Nâng cấp thành viên thành công!";
                if (!cardUpgradeSuccess) {
                    message += "\n\nLưu ý: Thẻ chưa được cập nhật giới hạn mượn sách.\nVui lòng liên hệ Admin để cập nhật lại.";
                }

                javax.swing.JOptionPane.showMessageDialog(this, message, "Thành công",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                loadCardInfo(); // Reload UI
                // Reset fields
                selectedPackageName = "";
                selectedPackagePrice = 0;
                selectedPackageField.setText("");
                discountField.setText("0%");
                totalField.setText("0 đ");
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Lỗi cập nhật CSDL! Liên hệ Admin.", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
