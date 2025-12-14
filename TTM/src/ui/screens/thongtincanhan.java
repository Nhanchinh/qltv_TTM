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
import java.sql.SQLException;
import smartcard.CardConnectionManager;
import smartcard.CardKeyManager;
import smartcard.CardInfoManager;
import smartcard.CardImageManager;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.awt.Image;

/**
 *
 * @author admin
 */
public class thongtincanhan extends javax.swing.JPanel {
    
    private CardService cardService;
    private String currentCardId = "CARD001";
    private boolean isEditing = false;
    private javax.swing.JLabel cardImageLabel;
    private javax.swing.JPanel imagePanel;

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
    private void loadCardInfo() {
        // 1. Thử lấy thông tin trực tiếp từ thẻ (giống AdminPanel -> CardInfoManager)
        String cardIdFromCard = null;
        CardInfoManager.UserInfo userInfoFromCard = null;
        byte[] cardImageData = null;
        
        try {
            CardConnectionManager connManager = new CardConnectionManager();
            connManager.connectCard();
            try {
                CardKeyManager keyManager = new CardKeyManager(connManager.getChannel());
                keyManager.getPublicKey();
                
                // Load app keypair từ file (đã tạo khi admin thêm thẻ)
                if (!keyManager.loadAppKeyPair()) {
                    throw new Exception("Không tìm thấy App KeyPair. Vui lòng thêm thẻ mới trước.");
                }
                
                CardInfoManager infoManager = new CardInfoManager(connManager.getChannel(), keyManager);
                userInfoFromCard = infoManager.getInfo();
                if (userInfoFromCard != null && userInfoFromCard.cardId != null && !userInfoFromCard.cardId.isEmpty()) {
                    cardIdFromCard = userInfoFromCard.cardId;
                    currentCardId = cardIdFromCard; // Đồng bộ CardID hiện tại với thẻ
                }

                // Lấy ảnh từ thẻ
                System.out.println("[CARD_IMAGE] Đang lấy ảnh từ thẻ...");
                CardImageManager imageManager = new CardImageManager(connManager.getChannel());
                cardImageData = imageManager.downloadImage();
                if (cardImageData != null && cardImageData.length > 0) {
                    System.out.println("[CARD_IMAGE] Đã nhận được " + cardImageData.length + " bytes");
                } else {
                    System.out.println("[CARD_IMAGE] Thẻ chưa có ảnh hoặc lỗi khi lấy ảnh");
                }
            } finally {
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("Không thể lấy thông tin từ thẻ, sẽ dùng dữ liệu DB. Lỗi: " + e.getMessage());
        }

        displayCardImage(cardImageData);
        
        // 2. Lấy thông tin từ DB theo CardID (ưu tiên CardID đọc từ thẻ nếu có)
        if (currentCardId != null && !currentCardId.isEmpty()) {
            // Recalculate TotalSpent from history to ensure accuracy
            cardService.recalculateTotalSpent(currentCardId);
        }
        
        CardService.Card card = (currentCardId != null) ? cardService.getCardById(currentCardId) : null;
        
        if (userInfoFromCard != null) {
                        // Log toàn bộ thông tin lấy từ thẻ
                        System.out.println("[CARD_INFO] Thông tin lấy từ thẻ:");
                        System.out.println("  CardID : " + userInfoFromCard.cardId);
                        System.out.println("  Name   : " + userInfoFromCard.name);
                        System.out.println("  Phone  : " + userInfoFromCard.phone);
                        System.out.println("  Address: " + userInfoFromCard.address);
                        System.out.println("  DOB    : " + userInfoFromCard.dob);
                        System.out.println("  RegDate: " + userInfoFromCard.regDate);
                        System.out.println("  Rank   : " + userInfoFromCard.rank);
            // Hiển thị THÔNG TIN CƠ BẢN theo đúng dữ liệu trên thẻ
            cardIdField.setText(userInfoFromCard.cardId);
            nameField.setText(userInfoFromCard.name);
            phoneField.setText(userInfoFromCard.phone);
            addressField.setText(userInfoFromCard.address != null ? userInfoFromCard.address : "");
            
            // DOB trên thẻ dạng DDMMYYYY -> hiển thị DD/MM/YYYY
            if (userInfoFromCard.dob != null && userInfoFromCard.dob.length() == 8) {
                String dob = userInfoFromCard.dob;
                dobField.setText(dob.substring(0, 2) + "/" + dob.substring(2, 4) + "/" + dob.substring(4));
            } else {
                dobField.setText(userInfoFromCard.dob != null ? userInfoFromCard.dob : "");
            }
            
            // Ngày đăng ký trên thẻ dạng DDMMYYYY
            if (userInfoFromCard.regDate != null && userInfoFromCard.regDate.length() == 8) {
                String reg = userInfoFromCard.regDate;
                registerDateField.setText(reg.substring(0, 2) + "/" + reg.substring(2, 4) + "/" + reg.substring(4));
            } else {
                registerDateField.setText(userInfoFromCard.regDate != null ? userInfoFromCard.regDate : "");
            }
        } else if (card != null) {
            // Fallback: chỉ có dữ liệu DB
            cardIdField.setText(card.cardId);
            nameField.setText(card.fullName);
            phoneField.setText(card.phone);
            addressField.setText(card.address != null ? card.address : "");
            
            // DOB từ DB (YYYY-MM-DD -> DD/MM/YYYY)
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
                // Hiển thị loại hội viên (hạng thẻ) lấy từ thẻ
                memberTypeField.setText(userInfoFromCard.rank != null ? userInfoFromCard.rank : "");
            }
        } else {
            // Không có dữ liệu nào
            cardIdField.setText(currentCardId != null ? currentCardId : "");
            nameField.setText("");
            phoneField.setText("");
            addressField.setText("");
            dobField.setText("");
            registerDateField.setText("");
        }
        
        // Sau khi load xong, luôn về trạng thái chỉ xem
        setFieldsEditable(false);
        isEditing = false;
        saveButton.setEnabled(false);
        
        // 3. Thông tin hội viên (luôn lấy từ DB, vì chỉ DB có tổng chi, điểm, nợ phạt,...)
        if (card != null) {
            double actualTotalSpent = cardService.calculateTotalSpentFromHistory(card.cardId);
            memberTypeField.setText(card.memberType != null ? card.memberType : "Basic");
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            totalSpentField.setText(nf.format(actualTotalSpent) + " đ");
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
    }
    
    /**
     * Hiển thị ảnh thẻ
     */
    private void displayCardImage(byte[] imageData) {
        if (cardImageLabel == null) return;
        
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
        
        // Khi không ở chế độ chỉnh sửa thì cũng không cho focus để tránh hiện con trỏ nháy
        nameField.setFocusable(editable);
        phoneField.setFocusable(editable);
        addressField.setFocusable(editable);
        dobField.setFocusable(editable);
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        // Tạo các component (các thành phần giao diện)
        titleLabel = new javax.swing.JLabel();
        
        // Panel ảnh thẻ
        imagePanel = new javax.swing.JPanel();
        cardImageLabel = new javax.swing.JLabel();
        
        // Thông tin cơ bản
        cardIdLabel = new javax.swing.JLabel();
        cardIdField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        phoneLabel = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        addressLabel = new javax.swing.JLabel();
        addressField = new javax.swing.JTextField();
        dobLabel = new javax.swing.JLabel();
        dobField = new javax.swing.JTextField();
        registerDateLabel = new javax.swing.JLabel();
        registerDateField = new javax.swing.JTextField();
        
        // Thông tin hội viên
        memberTypeLabel = new javax.swing.JLabel();
        memberTypeField = new javax.swing.JTextField();
        totalSpentLabel = new javax.swing.JLabel();
        totalSpentField = new javax.swing.JTextField();
        totalPointsLabel = new javax.swing.JLabel();
        totalPointsField = new javax.swing.JTextField();
        fineDebtLabel = new javax.swing.JLabel();
        fineDebtField = new javax.swing.JTextField();
        isBlockedLabel = new javax.swing.JLabel();
        isBlockedField = new javax.swing.JTextField();
        rankLabel = new javax.swing.JLabel();
        rankField = new javax.swing.JTextField();
        
        saveButton = new javax.swing.JButton();
        basicInfoPanel = new javax.swing.JPanel();
        memberInfoPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 0));

        // Thiết lập title
        javax.swing.JPanel titlePanel = new javax.swing.JPanel();
        titlePanel.setBackground(new java.awt.Color(245, 245, 250));
        titlePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 20, 40));
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Thông tin thẻ");
        titlePanel.add(titleLabel);
        add(titlePanel, java.awt.BorderLayout.NORTH);

        // Thiết lập các label (nhãn) - Thông tin cơ bản
        cardIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cardIdLabel.setForeground(new java.awt.Color(60, 60, 60));
        cardIdLabel.setText("Mã thẻ:");

        nameLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        nameLabel.setForeground(new java.awt.Color(60, 60, 60));
        nameLabel.setText("Họ và tên:");

        phoneLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        phoneLabel.setForeground(new java.awt.Color(60, 60, 60));
        phoneLabel.setText("Số điện thoại:");

        addressLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        addressLabel.setForeground(new java.awt.Color(60, 60, 60));
        addressLabel.setText("Địa chỉ:");

        dobLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        dobLabel.setForeground(new java.awt.Color(60, 60, 60));
        dobLabel.setText("Ngày sinh:");

        registerDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        registerDateLabel.setForeground(new java.awt.Color(60, 60, 60));
        registerDateLabel.setText("Ngày đăng ký:");

        // Thông tin hội viên
        memberTypeLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        memberTypeLabel.setForeground(new java.awt.Color(60, 60, 60));
        memberTypeLabel.setText("Loại hội viên:");

        totalSpentLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        totalSpentLabel.setForeground(new java.awt.Color(60, 60, 60));
        totalSpentLabel.setText("Tổng tiền đã chi:");

        totalPointsLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        totalPointsLabel.setForeground(new java.awt.Color(60, 60, 60));
        totalPointsLabel.setText("Tổng điểm tích lũy:");

        fineDebtLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        fineDebtLabel.setForeground(new java.awt.Color(60, 60, 60));
        fineDebtLabel.setText("Tiền nợ phạt:");

        isBlockedLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        isBlockedLabel.setForeground(new java.awt.Color(60, 60, 60));
        isBlockedLabel.setText("Trạng thái thẻ:");

        rankLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        rankLabel.setForeground(new java.awt.Color(60, 60, 60));
        rankLabel.setText("Hạng thẻ:");

        // Thiết lập các text field (ô nhập liệu) - Thông tin cơ bản
        cardIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cardIdField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        cardIdField.setColumns(30);
        cardIdField.setEditable(false);
        cardIdField.setFocusable(false);

        nameField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        nameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        nameField.setColumns(30);
        nameField.setEditable(false);
        nameField.setFocusable(false);

        phoneField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        phoneField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
        javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        phoneField.setColumns(30);
        phoneField.setEditable(false);
        phoneField.setFocusable(false);

        addressField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        addressField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
        javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        addressField.setColumns(30);
        addressField.setEditable(false);
        addressField.setFocusable(false);

        dobField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        dobField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        dobField.setColumns(30);
        dobField.setEditable(false);
        dobField.setFocusable(false);

        registerDateField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        registerDateField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        registerDateField.setColumns(30);
        registerDateField.setEditable(false);

        // Thông tin hội viên
        memberTypeField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        memberTypeField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        memberTypeField.setColumns(30);
        memberTypeField.setEditable(false);

        totalSpentField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        totalSpentField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        totalSpentField.setColumns(30);
        totalSpentField.setEditable(false);

        totalPointsField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        totalPointsField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        totalPointsField.setColumns(30);
        totalPointsField.setEditable(false);

        fineDebtField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        fineDebtField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        fineDebtField.setColumns(30);
        fineDebtField.setEditable(false);

        isBlockedField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        isBlockedField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        isBlockedField.setColumns(30);
        isBlockedField.setEditable(false);

        rankField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        rankField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        rankField.setColumns(30);
        rankField.setEditable(false);

        // Thiết lập panel ảnh thẻ
        imagePanel.setBackground(new java.awt.Color(255, 255, 255));
        imagePanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null, "Ảnh thẻ",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16),
                new java.awt.Color(60, 60, 60)
            ),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        imagePanel.setLayout(new java.awt.BorderLayout());
        imagePanel.setPreferredSize(new java.awt.Dimension(250, 320));
        imagePanel.setMaximumSize(new java.awt.Dimension(250, 320));
        
        cardImageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cardImageLabel.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        cardImageLabel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        cardImageLabel.setForeground(new java.awt.Color(128, 128, 128));
        cardImageLabel.setText("<html><center>Đang tải<br>ảnh...</center></html>");
        cardImageLabel.setPreferredSize(new java.awt.Dimension(200, 250));
        cardImageLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)));
        cardImageLabel.setOpaque(true);
        cardImageLabel.setBackground(new java.awt.Color(245, 245, 250));
        imagePanel.add(cardImageLabel, java.awt.BorderLayout.CENTER);

        // Thiết lập button
        editButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        // Nút chỉnh sửa
        editButton.setBackground(new java.awt.Color(108, 117, 125));
        editButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        editButton.setForeground(new java.awt.Color(255, 255, 255));
        editButton.setText("✏️ Chỉnh sửa");
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.setPreferredSize(new java.awt.Dimension(140, 40));
        editButton.addActionListener(this::editButtonActionPerformed);

        // Nút lưu
        saveButton.setBackground(new java.awt.Color(0, 120, 215));
        saveButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        saveButton.setForeground(new java.awt.Color(255, 255, 255));
        saveButton.setText("💾 Lưu thông tin");
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new java.awt.Dimension(160, 40));
        saveButton.setEnabled(false);
        saveButton.addActionListener(this::saveButtonActionPerformed);
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (saveButton.isEnabled()) {
                    saveButton.setBackground(new java.awt.Color(0, 100, 180));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveButton.setBackground(new java.awt.Color(0, 120, 215));
            }
        });

        // Tạo panel chứa thông tin cơ bản
        basicInfoPanel.setBackground(new java.awt.Color(255, 255, 255));
        basicInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null, "Thông tin cơ bản",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16),
                new java.awt.Color(60, 60, 60)
            ),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Sử dụng GroupLayout để sắp xếp các component - Thông tin cơ bản
        javax.swing.GroupLayout basicInfoLayout = new javax.swing.GroupLayout(basicInfoPanel);
        basicInfoPanel.setLayout(basicInfoLayout);
        
        basicInfoLayout.setHorizontalGroup(
            basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(phoneLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dobLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(registerDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, 10)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdField)
                    .addComponent(nameField)
                    .addComponent(phoneField)
                    .addComponent(addressField)
                    .addComponent(dobField)
                    .addComponent(registerDateField))
                .addContainerGap())
        );
        
        basicInfoLayout.setVerticalGroup(
            basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cardIdLabel)
                    .addComponent(cardIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phoneLabel)
                    .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressLabel)
                    .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dobLabel)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(registerDateLabel)
                    .addComponent(registerDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        // Tạo panel chứa thông tin hội viên
        memberInfoPanel.setBackground(new java.awt.Color(255, 255, 255));
        memberInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null, "Thông tin hội viên",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16),
                new java.awt.Color(60, 60, 60)
            ),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        javax.swing.GroupLayout memberInfoLayout = new javax.swing.GroupLayout(memberInfoPanel);
        memberInfoPanel.setLayout(memberInfoLayout);
        
        memberInfoLayout.setHorizontalGroup(
            memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memberInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(memberTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalSpentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalPointsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fineDebtLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(isBlockedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rankLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(memberTypeField)
                    .addComponent(totalSpentField)
                    .addComponent(totalPointsField)
                    .addComponent(fineDebtField)
                    .addComponent(isBlockedField)
                    .addComponent(rankField))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        
        memberInfoLayout.setVerticalGroup(
            memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memberInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memberTypeLabel)
                    .addComponent(memberTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalSpentLabel)
                    .addComponent(totalSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPointsLabel)
                    .addComponent(totalPointsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fineDebtLabel)
                    .addComponent(fineDebtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isBlockedLabel)
                    .addComponent(isBlockedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rankLabel)
                    .addComponent(rankField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        // Layout chính của panel này - dùng BorderLayout với content panel
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setBackground(new java.awt.Color(245, 245, 250));
        contentPanel.setLayout(new java.awt.BorderLayout(0, 0));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Panel chứa 2 info panel nằm ngang
        javax.swing.JPanel infoPanelsContainer = new javax.swing.JPanel();
        infoPanelsContainer.setBackground(new java.awt.Color(245, 245, 250));
        infoPanelsContainer.setLayout(new javax.swing.BoxLayout(infoPanelsContainer, javax.swing.BoxLayout.X_AXIS));
        
        // Thêm panel ảnh vào đầu tiên
        imagePanel.setAlignmentY(javax.swing.JComponent.TOP_ALIGNMENT);
        infoPanelsContainer.add(imagePanel);
        infoPanelsContainer.add(javax.swing.Box.createHorizontalStrut(20));
        
        // Đặt kích thước cho các panel để hẹp lại nhưng tự động căn chỉnh
        basicInfoPanel.setAlignmentY(javax.swing.JComponent.TOP_ALIGNMENT);
        basicInfoPanel.setPreferredSize(new java.awt.Dimension(400, basicInfoPanel.getPreferredSize().height));
        basicInfoPanel.setMaximumSize(new java.awt.Dimension(450, Integer.MAX_VALUE));
        memberInfoPanel.setAlignmentY(javax.swing.JComponent.TOP_ALIGNMENT);
        memberInfoPanel.setPreferredSize(new java.awt.Dimension(400, memberInfoPanel.getPreferredSize().height));
        memberInfoPanel.setMaximumSize(new java.awt.Dimension(450, Integer.MAX_VALUE));
        
        infoPanelsContainer.add(basicInfoPanel);
        infoPanelsContainer.add(javax.swing.Box.createHorizontalStrut(20));
        infoPanelsContainer.add(memberInfoPanel);
        
        contentPanel.add(infoPanelsContainer, java.awt.BorderLayout.CENTER);
        
        // Button panel ở dưới
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setBackground(new java.awt.Color(245, 245, 250));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 0, 10, 0));
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 0));
        editButton.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        saveButton.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        buttonPanel.add(editButton);
        buttonPanel.add(saveButton);
        contentPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        
        add(contentPanel, java.awt.BorderLayout.CENTER);
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

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
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
                        javax.swing.JOptionPane.showMessageDialog(this, 
                            "Da luu thong tin thanh cong!",
                            "Thong bao",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        loadCardInfo(); // Reload to show updated data
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, 
                            "Loi khi luu thong tin!",
                            "Loi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "Khong the ket noi database!",
                    "Loi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Loi khi luu thong tin: " + e.getMessage(),
                "Loi",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveButtonActionPerformed


    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel basicInfoPanel;
    private javax.swing.JLabel cardIdLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JTextField phoneField;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressField;
    private javax.swing.JLabel dobLabel;
    private javax.swing.JTextField dobField;
    private javax.swing.JLabel registerDateLabel;
    private javax.swing.JTextField registerDateField;
    private javax.swing.JPanel memberInfoPanel;
    private javax.swing.JLabel memberTypeLabel;
    private javax.swing.JTextField memberTypeField;
    private javax.swing.JLabel totalSpentLabel;
    private javax.swing.JTextField totalSpentField;
    private javax.swing.JLabel totalPointsLabel;
    private javax.swing.JTextField totalPointsField;
    private javax.swing.JLabel fineDebtLabel;
    private javax.swing.JTextField fineDebtField;
    private javax.swing.JLabel isBlockedLabel;
    private javax.swing.JTextField isBlockedField;
    private javax.swing.JLabel rankLabel;
    private javax.swing.JTextField rankField;
    private javax.swing.JButton editButton;
    private javax.swing.JButton saveButton;
}

