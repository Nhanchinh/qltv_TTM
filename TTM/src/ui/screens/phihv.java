/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.CardService;
import services.TransactionService;
import ui.DBConnect;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author admin
 */
public class phihv extends javax.swing.JPanel {
    
    private CardService cardService;
    private TransactionService transactionService;
    private String currentCardId = "CARD001";
    private String selectedPackageName = "";
    private double selectedPackagePrice = 0;
    private int selectedDiscount = 0;
    private int selectedMonths = 0;

    /**
     * Creates new form MembershipFeePanel
     */
    public phihv() {
        cardService = new CardService();
        transactionService = new TransactionService();
        initComponents();
        loadCardInfo();
    }
    
    /**
     * Load card information from database
     */
    private void loadCardInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);
        if (card != null) {
            cardIdField.setText(card.cardId);
            
            // Set member status
            if (card.memberType != null && !card.memberType.isEmpty() && !card.memberType.equals("Basic")) {
                memberStatusField.setText(card.memberType);
                
                // Calculate expiry date (if we have register date, add membership duration)
                if (card.registerDate != null && !card.registerDate.isEmpty()) {
                    try {
                        LocalDate registerDate = LocalDate.parse(card.registerDate);
                        // Assume 3 months for Basic, 6 months for Premium, 12 months for VIP
                        int months = 3;
                        if (card.memberType.equals("Premium") || card.memberType.equals("Cao cap")) {
                            months = 6;
                        } else if (card.memberType.equals("VIP")) {
                            months = 12;
                        }
                        LocalDate expiryDate = registerDate.plusMonths(months);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        expiryDateField.setText(expiryDate.format(formatter));
                    } catch (Exception e) {
                        expiryDateField.setText("--");
                    }
                } else {
                    expiryDateField.setText("--");
                }
            } else {
                memberStatusField.setText("Chua co hoi vien");
                expiryDateField.setText("--");
            }
        } else {
            cardIdField.setText(currentCardId);
            memberStatusField.setText("Chua co hoi vien");
            expiryDateField.setText("--");
        }
    }
    
    /**
     * Reload card info (public method for external refresh)
     */
    public void reloadCardInfo() {
        loadCardInfo();
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        
        // Main container
        mainContainer = new javax.swing.JPanel();
        
        // Left panel - Gói hội viên
        packagesPanel = new javax.swing.JPanel();
        packagesTitle = new javax.swing.JLabel();
        
        // Package cards
        basicPackage = new javax.swing.JPanel();
        basicTitle = new javax.swing.JLabel();
        basicPrice = new javax.swing.JLabel();
        basicDuration = new javax.swing.JLabel();
        basicFeatures = new javax.swing.JTextArea();
        basicButton = new javax.swing.JButton();
        
        premiumPackage = new javax.swing.JPanel();
        premiumTitle = new javax.swing.JLabel();
        premiumPrice = new javax.swing.JLabel();
        premiumDuration = new javax.swing.JLabel();
        premiumFeatures = new javax.swing.JTextArea();
        premiumButton = new javax.swing.JButton();
        premiumBadge = new javax.swing.JLabel();
        
        vipPackage = new javax.swing.JPanel();
        vipTitle = new javax.swing.JLabel();
        vipPrice = new javax.swing.JLabel();
        vipDuration = new javax.swing.JLabel();
        vipFeatures = new javax.swing.JTextArea();
        vipButton = new javax.swing.JButton();
        vipBadge = new javax.swing.JLabel();
        
        // Right panel - Thông tin hội viên và thanh toán
        infoPanel = new javax.swing.JPanel();
        infoTitle = new javax.swing.JLabel();
        cardIdLabel = new javax.swing.JLabel();
        cardIdField = new javax.swing.JTextField();
        memberStatusLabel = new javax.swing.JLabel();
        memberStatusField = new javax.swing.JTextField();
        expiryDateLabel = new javax.swing.JLabel();
        expiryDateField = new javax.swing.JTextField();
        selectedPackageLabel = new javax.swing.JLabel();
        selectedPackageField = new javax.swing.JTextField();
        discountLabel = new javax.swing.JLabel();
        discountField = new javax.swing.JTextField();
        totalLabel = new javax.swing.JLabel();
        totalField = new javax.swing.JTextField();
        paymentButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 20));

        // Title
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Phí hội viên");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        mainContainer.setLayout(new java.awt.BorderLayout(25, 0));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 30, 40));
        mainContainer.setBackground(new java.awt.Color(245, 245, 250));

        // ============ LEFT PANEL - GÓI HỘI VIÊN ============
        packagesPanel.setLayout(new java.awt.BorderLayout(0, 25));
        packagesPanel.setBackground(new java.awt.Color(245, 245, 250));

        packagesTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        packagesTitle.setForeground(new java.awt.Color(0, 120, 215));
        packagesTitle.setText("Chọn gói hội viên");
        packagesTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
        packagesPanel.add(packagesTitle, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel cardsPanel = new javax.swing.JPanel();
        cardsPanel.setLayout(new java.awt.GridLayout(1, 3, 25, 0));
        cardsPanel.setBackground(new java.awt.Color(245, 245, 250));

        // Basic Package
        basicPackage.setBackground(new java.awt.Color(255, 255, 255));
        basicPackage.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 2),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        basicPackage.setLayout(new java.awt.BorderLayout(15, 15));

        basicTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        basicTitle.setForeground(new java.awt.Color(60, 60, 60));
        basicTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        basicTitle.setText("Gói Cơ bản");

        basicPrice.setFont(new java.awt.Font("Segoe UI", 1, 28));
        basicPrice.setForeground(new java.awt.Color(0, 120, 215));
        basicPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        basicPrice.setText("100,000 đ");

        basicDuration.setFont(new java.awt.Font("Segoe UI", 0, 14));
        basicDuration.setForeground(new java.awt.Color(100, 100, 100));
        basicDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        basicDuration.setText("/ 3 tháng");

        basicFeatures.setFont(new java.awt.Font("Segoe UI", 0, 13));
        basicFeatures.setEditable(false);
        basicFeatures.setLineWrap(true);
        basicFeatures.setWrapStyleWord(true);
        basicFeatures.setText("• Mượn tối đa 3 sách\n• Thời gian mượn 7 ngày\n• Hỗ trợ email\n• Giảm giá 5% khi mua sách");
        basicFeatures.setBackground(new java.awt.Color(250, 250, 250));
        basicFeatures.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        basicButton.setBackground(new java.awt.Color(0, 120, 215));
        basicButton.setForeground(new java.awt.Color(255, 255, 255));
        basicButton.setText("Chọn gói");
        basicButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        basicButton.setFocusPainted(false);
        basicButton.setPreferredSize(new java.awt.Dimension(0, 45));
        basicButton.addActionListener(e -> selectPackage("Basic", 100000, 5, 3));

        // Center panel for price, duration and features
        javax.swing.JPanel basicCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        basicCenterPanel.setBackground(new java.awt.Color(255, 255, 255));
        
        // Price panel
        javax.swing.JPanel basicPricePanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        basicPricePanel.setBackground(new java.awt.Color(255, 255, 255));
        basicPricePanel.add(basicPrice, java.awt.BorderLayout.CENTER);
        basicPricePanel.add(basicDuration, java.awt.BorderLayout.SOUTH);
        
        basicCenterPanel.add(basicPricePanel, java.awt.BorderLayout.NORTH);
        basicCenterPanel.add(new javax.swing.JScrollPane(basicFeatures), java.awt.BorderLayout.CENTER);

        // Layout for basic package card
        basicPackage.add(basicTitle, java.awt.BorderLayout.NORTH);
        basicPackage.add(basicCenterPanel, java.awt.BorderLayout.CENTER);
        basicPackage.add(basicButton, java.awt.BorderLayout.SOUTH);

        // Premium Package (Nổi bật)
        premiumPackage.setBackground(new java.awt.Color(255, 248, 220));
        premiumPackage.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 200, 0), 3),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        premiumPackage.setLayout(new java.awt.BorderLayout(15, 15));

        premiumBadge.setFont(new java.awt.Font("Segoe UI", 1, 12));
        premiumBadge.setForeground(new java.awt.Color(255, 255, 255));
        premiumBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        premiumBadge.setBackground(new java.awt.Color(255, 140, 0));
        premiumBadge.setOpaque(true);
        premiumBadge.setText("KHUYẾN MÃI");
        premiumBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        premiumTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        premiumTitle.setForeground(new java.awt.Color(60, 60, 60));
        premiumTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        premiumTitle.setText("Gói Cao cấp");

        premiumPrice.setFont(new java.awt.Font("Segoe UI", 1, 28));
        premiumPrice.setForeground(new java.awt.Color(255, 140, 0));
        premiumPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        premiumPrice.setText("280,000 đ");

        javax.swing.JLabel premiumOldPrice = new javax.swing.JLabel("350,000 đ");
        premiumOldPrice.setFont(new java.awt.Font("Segoe UI", 0, 16));
        premiumOldPrice.setForeground(new java.awt.Color(150, 150, 150));
        premiumOldPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        premiumOldPrice.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        javax.swing.JPanel premiumPricePanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 5));
        premiumPricePanel.setBackground(new java.awt.Color(255, 248, 220));
        premiumPricePanel.add(premiumPrice, java.awt.BorderLayout.CENTER);
        premiumPricePanel.add(premiumOldPrice, java.awt.BorderLayout.SOUTH);

        premiumDuration.setFont(new java.awt.Font("Segoe UI", 0, 14));
        premiumDuration.setForeground(new java.awt.Color(100, 100, 100));
        premiumDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        premiumDuration.setText("/ 6 tháng (Ưu đãi 20%)");

        premiumFeatures.setFont(new java.awt.Font("Segoe UI", 0, 13));
        premiumFeatures.setEditable(false);
        premiumFeatures.setLineWrap(true);
        premiumFeatures.setWrapStyleWord(true);
        premiumFeatures.setText("• Mượn tối đa 5 sách\n• Thời gian mượn 14 ngày\n• Hỗ trợ 24/7\n• Giảm giá 10% khi mua sách\n• Đặt chỗ sách online");
        premiumFeatures.setBackground(new java.awt.Color(255, 248, 220));
        premiumFeatures.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        premiumButton.setBackground(new java.awt.Color(255, 140, 0));
        premiumButton.setForeground(new java.awt.Color(255, 255, 255));
        premiumButton.setText("Chọn gói");
        premiumButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        premiumButton.setFocusPainted(false);
        premiumButton.setPreferredSize(new java.awt.Dimension(0, 45));
        premiumButton.addActionListener(e -> selectPackage("Premium", 280000, 20, 6));

        // Top panel for badge and title
        javax.swing.JPanel premiumTopPanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        premiumTopPanel.setBackground(new java.awt.Color(255, 248, 220));
        premiumTopPanel.add(premiumBadge, java.awt.BorderLayout.NORTH);
        premiumTopPanel.add(premiumTitle, java.awt.BorderLayout.CENTER);
        
        // Center panel for price, duration and features
        javax.swing.JPanel premiumCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        premiumCenterPanel.setBackground(new java.awt.Color(255, 248, 220));
        
        // Price panel
        javax.swing.JPanel premiumPriceFullPanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        premiumPriceFullPanel.setBackground(new java.awt.Color(255, 248, 220));
        premiumPriceFullPanel.add(premiumPricePanel, java.awt.BorderLayout.CENTER);
        premiumPriceFullPanel.add(premiumDuration, java.awt.BorderLayout.SOUTH);
        
        premiumCenterPanel.add(premiumPriceFullPanel, java.awt.BorderLayout.NORTH);
        premiumCenterPanel.add(new javax.swing.JScrollPane(premiumFeatures), java.awt.BorderLayout.CENTER);

        premiumPackage.add(premiumTopPanel, java.awt.BorderLayout.NORTH);
        premiumPackage.add(premiumCenterPanel, java.awt.BorderLayout.CENTER);
        premiumPackage.add(premiumButton, java.awt.BorderLayout.SOUTH);

        // VIP Package
        vipPackage.setBackground(new java.awt.Color(255, 255, 255));
        vipPackage.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 0, 180), 2),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        vipPackage.setLayout(new java.awt.BorderLayout(15, 15));

        vipBadge.setFont(new java.awt.Font("Segoe UI", 1, 12));
        vipBadge.setForeground(new java.awt.Color(255, 255, 255));
        vipBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vipBadge.setBackground(new java.awt.Color(180, 0, 180));
        vipBadge.setOpaque(true);
        vipBadge.setText("VIP");
        vipBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        vipTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        vipTitle.setForeground(new java.awt.Color(60, 60, 60));
        vipTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vipTitle.setText("Gói VIP");

        vipPrice.setFont(new java.awt.Font("Segoe UI", 1, 28));
        vipPrice.setForeground(new java.awt.Color(180, 0, 180));
        vipPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vipPrice.setText("500,000 đ");

        vipDuration.setFont(new java.awt.Font("Segoe UI", 0, 14));
        vipDuration.setForeground(new java.awt.Color(100, 100, 100));
        vipDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vipDuration.setText("/ 12 tháng (Ưu đãi 30%)");

        vipFeatures.setFont(new java.awt.Font("Segoe UI", 0, 13));
        vipFeatures.setEditable(false);
        vipFeatures.setLineWrap(true);
        vipFeatures.setWrapStyleWord(true);
        vipFeatures.setText("• Mượn không giới hạn sách\n• Thời gian mượn 30 ngày\n• Hỗ trợ 24/7 ưu tiên\n• Giảm giá 15% khi mua sách\n• Đặt chỗ sách online\n• Tham gia sự kiện đặc biệt");
        vipFeatures.setBackground(new java.awt.Color(250, 250, 250));
        vipFeatures.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        vipButton.setBackground(new java.awt.Color(180, 0, 180));
        vipButton.setForeground(new java.awt.Color(255, 255, 255));
        vipButton.setText("Chọn gói");
        vipButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        vipButton.setFocusPainted(false);
        vipButton.setPreferredSize(new java.awt.Dimension(0, 45));
        vipButton.addActionListener(e -> selectPackage("VIP", 500000, 30, 12));

        // Top panel for badge and title
        javax.swing.JPanel vipTopPanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        vipTopPanel.setBackground(new java.awt.Color(255, 255, 255));
        vipTopPanel.add(vipBadge, java.awt.BorderLayout.NORTH);
        vipTopPanel.add(vipTitle, java.awt.BorderLayout.CENTER);
        
        // Center panel for price, duration and features
        javax.swing.JPanel vipCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        vipCenterPanel.setBackground(new java.awt.Color(255, 255, 255));
        
        // Price panel
        javax.swing.JPanel vipPricePanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        vipPricePanel.setBackground(new java.awt.Color(255, 255, 255));
        vipPricePanel.add(vipPrice, java.awt.BorderLayout.CENTER);
        vipPricePanel.add(vipDuration, java.awt.BorderLayout.SOUTH);
        
        vipCenterPanel.add(vipPricePanel, java.awt.BorderLayout.NORTH);
        vipCenterPanel.add(new javax.swing.JScrollPane(vipFeatures), java.awt.BorderLayout.CENTER);

        vipPackage.add(vipTopPanel, java.awt.BorderLayout.NORTH);
        vipPackage.add(vipCenterPanel, java.awt.BorderLayout.CENTER);
        vipPackage.add(vipButton, java.awt.BorderLayout.SOUTH);

        cardsPanel.add(basicPackage);
        cardsPanel.add(premiumPackage);
        cardsPanel.add(vipPackage);

        packagesPanel.add(cardsPanel, java.awt.BorderLayout.CENTER);

        // ============ RIGHT PANEL - THÔNG TIN ============
        infoPanel.setBackground(new java.awt.Color(255, 255, 255));
        infoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Thông tin hội viên",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        infoPanel.setLayout(new java.awt.BorderLayout(0, 20));
        infoPanel.setPreferredSize(new java.awt.Dimension(380, 0));
        infoPanel.setMinimumSize(new java.awt.Dimension(350, 0));
        infoPanel.setMaximumSize(new java.awt.Dimension(450, Integer.MAX_VALUE));

        infoTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        infoTitle.setForeground(new java.awt.Color(0, 120, 215));
        infoTitle.setText("Thông tin thanh toán");

        javax.swing.JPanel infoFormPanel = new javax.swing.JPanel();
        javax.swing.GroupLayout formLayout = new javax.swing.GroupLayout(infoFormPanel);
        infoFormPanel.setLayout(formLayout);
        infoFormPanel.setBackground(new java.awt.Color(255, 255, 255));

        formLayout.setHorizontalGroup(
            formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(memberStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(expiryDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectedPackageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(discountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdField)
                    .addComponent(memberStatusField)
                    .addComponent(expiryDateField)
                    .addComponent(selectedPackageField)
                    .addComponent(discountField)
                    .addComponent(totalField))
                .addContainerGap())
        );

        formLayout.setVerticalGroup(
            formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cardIdLabel)
                    .addComponent(cardIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memberStatusLabel)
                    .addComponent(memberStatusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(expiryDateLabel)
                    .addComponent(expiryDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectedPackageLabel)
                    .addComponent(selectedPackageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(discountLabel)
                    .addComponent(discountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalLabel)
                    .addComponent(totalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        // Labels
        cardIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cardIdLabel.setText("Mã thẻ:");
        memberStatusLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        memberStatusLabel.setText("Trạng thái:");
        expiryDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        expiryDateLabel.setText("Hết hạn:");
        selectedPackageLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        selectedPackageLabel.setText("Gói đã chọn:");
        discountLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        discountLabel.setText("Giảm giá:");
        totalLabel.setFont(new java.awt.Font("Segoe UI", 1, 16));
        totalLabel.setText("Tổng tiền:");

        // Fields
        cardIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cardIdField.setEditable(false);
        memberStatusField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        memberStatusField.setEditable(false);
        expiryDateField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        expiryDateField.setEditable(false);
        selectedPackageField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        selectedPackageField.setEditable(false);
        discountField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        discountField.setText("0%");
        discountField.setEditable(false);
        totalField.setFont(new java.awt.Font("Segoe UI", 1, 16));
        totalField.setText("0 đ");
        totalField.setEditable(false);
        totalField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        paymentButton.setBackground(new java.awt.Color(0, 120, 215));
        paymentButton.setForeground(new java.awt.Color(255, 255, 255));
        paymentButton.setText("💳 Thanh toán");
        paymentButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        paymentButton.setFocusPainted(false);
        paymentButton.setPreferredSize(new java.awt.Dimension(0, 45));
        paymentButton.addActionListener(e -> processPayment());

        infoPanel.add(infoTitle, java.awt.BorderLayout.NORTH);
        infoPanel.add(infoFormPanel, java.awt.BorderLayout.CENTER);
        infoPanel.add(paymentButton, java.awt.BorderLayout.SOUTH);

        mainContainer.add(packagesPanel, java.awt.BorderLayout.CENTER);
        mainContainer.add(infoPanel, java.awt.BorderLayout.EAST);

        add(mainContainer, java.awt.BorderLayout.CENTER);
    }

    private void selectPackage(String packageName, int price, int discount, int months) {
        selectedPackageName = packageName;
        selectedPackagePrice = price;
        selectedDiscount = discount;
        selectedMonths = months;
        
        // Display package name in Vietnamese
        String displayName = packageName;
        if (packageName.equals("Basic")) displayName = "Co ban";
        else if (packageName.equals("Premium")) displayName = "Cao cap";
        
        selectedPackageField.setText(displayName);
        discountField.setText(discount + "%");
        
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        totalField.setText(nf.format(price) + " d");
    }

    private void processPayment() {
        if (selectedPackageName.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui long chon goi hoi vien!", "Thong bao", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Check card balance
        List<TransactionService.Transaction> transactions = transactionService.getTransactionsByCard(currentCardId);
        double balance = 0;
        if (transactions != null) {
            for (TransactionService.Transaction t : transactions) {
                if (t.type.equals("Deposit")) {
                    balance += t.amount;
                } else if (t.type.equals("Payment")) {
                    balance += t.amount; // amount is negative for payment
                }
            }
        }
        
        if (balance < selectedPackagePrice) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "So du khong du!\nSo du hien tai: " + NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(balance) + " d\nCan: " + 
                NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(selectedPackagePrice) + " d",
                "Thong bao", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int option = javax.swing.JOptionPane.showConfirmDialog(this, 
            "Xac nhan thanh toan goi hoi vien?\nGoi: " + selectedPackageField.getText() + "\nGia: " + 
            NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(selectedPackagePrice) + " d",
            "Xac nhan",
            javax.swing.JOptionPane.YES_NO_OPTION);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        
        // Process payment
        try {
            Connection conn = DBConnect.getConnection();
            if (conn != null) {
                conn.setAutoCommit(false);
                
                try {
                    // Update MemberType in Cards table
                    String updateCardSql = "UPDATE Cards SET MemberType = ?, RegisterDate = ? WHERE CardID = ?";
                    try (PreparedStatement updateCardStmt = conn.prepareStatement(updateCardSql)) {
                        updateCardStmt.setString(1, selectedPackageName);
                        updateCardStmt.setString(2, LocalDate.now().toString());
                        updateCardStmt.setString(3, currentCardId);
                        updateCardStmt.executeUpdate();
                    }
                    
                    // Create transaction record
                    String transSql = "INSERT INTO Transactions (TransID, CardID, Type, Amount, PointsChanged, DateTime, SignatureCard, SignatureStore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement transStmt = conn.prepareStatement(transSql)) {
                        transStmt.setString(1, UUID.randomUUID().toString());
                        transStmt.setString(2, currentCardId);
                        transStmt.setString(3, "Payment");
                        transStmt.setDouble(4, -selectedPackagePrice);
                        transStmt.setInt(5, 0);
                        transStmt.setString(6, java.time.LocalDateTime.now().toString());
                        transStmt.setBytes(7, new byte[]{});
                        transStmt.setBytes(8, new byte[]{});
                        transStmt.executeUpdate();
                    }
                    
                    conn.commit();
                    
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Thanh toan thanh cong!\nGoi hoi vien: " + selectedPackageField.getText(),
                        "Thong bao", 
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    
                    // Reset selection and reload info
                    selectedPackageName = "";
                    selectedPackagePrice = 0;
                    selectedDiscount = 0;
                    selectedMonths = 0;
                    selectedPackageField.setText("");
                    discountField.setText("0%");
                    totalField.setText("0 d");
                    loadCardInfo();
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Khong the ket noi database!", "Loi", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Loi khi thanh toan: " + e.getMessage(), "Loi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel packagesPanel;
    private javax.swing.JLabel packagesTitle;
    private javax.swing.JPanel basicPackage;
    private javax.swing.JLabel basicTitle;
    private javax.swing.JLabel basicPrice;
    private javax.swing.JLabel basicDuration;
    private javax.swing.JTextArea basicFeatures;
    private javax.swing.JButton basicButton;
    private javax.swing.JPanel premiumPackage;
    private javax.swing.JLabel premiumTitle;
    private javax.swing.JLabel premiumPrice;
    private javax.swing.JLabel premiumDuration;
    private javax.swing.JTextArea premiumFeatures;
    private javax.swing.JButton premiumButton;
    private javax.swing.JLabel premiumBadge;
    private javax.swing.JPanel vipPackage;
    private javax.swing.JLabel vipTitle;
    private javax.swing.JLabel vipPrice;
    private javax.swing.JLabel vipDuration;
    private javax.swing.JTextArea vipFeatures;
    private javax.swing.JButton vipButton;
    private javax.swing.JLabel vipBadge;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel infoTitle;
    private javax.swing.JLabel cardIdLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JLabel memberStatusLabel;
    private javax.swing.JTextField memberStatusField;
    private javax.swing.JLabel expiryDateLabel;
    private javax.swing.JTextField expiryDateField;
    private javax.swing.JLabel selectedPackageLabel;
    private javax.swing.JTextField selectedPackageField;
    private javax.swing.JLabel discountLabel;
    private javax.swing.JTextField discountField;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JTextField totalField;
    private javax.swing.JButton paymentButton;
}

