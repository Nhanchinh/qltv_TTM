/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.CardService;
import services.TransactionService;
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
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

    // Rank hiện tại và giá tương ứng
    private String currentRank = "Normal"; // Normal/Silver/Gold/Diamond
    private int currentRankPrice = 0; // Giá của rank hiện tại

    // Bảng giá các rank (giá gốc)
    private static final int PRICE_NORMAL = 0;
    private static final int PRICE_SILVER = 100000;
    private static final int PRICE_GOLD = 200000;
    private static final int PRICE_DIAMOND = 300000;

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
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            loadCardInfo();
        }
    }

    /**
     * Load card information from database and smart card
     */
    private void loadCardInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);

        // Mặc định rank = Normal
        currentRank = "Normal";
        currentRankPrice = PRICE_NORMAL;
        String displayRank = "Thành viên (Normal)";

        // Đọc rank từ thẻ chip
        try {
            CardConnectionManager connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                // Lấy memberType từ thẻ nếu có (cần thêm method getMemberType nếu có)
                // Tạm thời fallback về DB
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[PHIHV] Error reading card: " + e.getMessage());
        }

        if (card != null) {
            cardIdField.setText(card.cardId);

            // Set member status và tính giá rank hiện tại
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

            memberStatusField.setText(displayRank);

            // Calculate expiry date
            if (card.registerDate != null && !card.registerDate.isEmpty() && !currentRank.equals("Normal")) {
                try {
                    LocalDate registerDate = LocalDate.parse(card.registerDate);
                    int months = 3;
                    LocalDate expiryDate = registerDate.plusMonths(months);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    expiryDateField.setText(expiryDate.format(formatter));
                } catch (Exception e) {
                    expiryDateField.setText("--");
                }
            } else {
                expiryDateField.setText(currentRank.equals("Normal") ? "Không giới hạn" : "--");
            }
        } else {
            cardIdField.setText(currentCardId);
            memberStatusField.setText("Thành viên (Normal)");
            expiryDateField.setText("Không giới hạn");
        }

        System.out.println("[PHIHV] Current Rank: " + currentRank + ", Price: " + currentRankPrice);
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

        // Package cards - 4 gói mới
        freePackage = new javax.swing.JPanel();
        freeTitle = new javax.swing.JLabel();
        freePrice = new javax.swing.JLabel();
        freeDuration = new javax.swing.JLabel();
        freeFeatures = new javax.swing.JTextArea();
        freeButton = new javax.swing.JButton();

        silverPackage = new javax.swing.JPanel();
        silverTitle = new javax.swing.JLabel();
        silverPrice = new javax.swing.JLabel();
        silverDuration = new javax.swing.JLabel();
        silverFeatures = new javax.swing.JTextArea();
        silverButton = new javax.swing.JButton();

        goldPackage = new javax.swing.JPanel();
        goldTitle = new javax.swing.JLabel();
        goldPrice = new javax.swing.JLabel();
        goldDuration = new javax.swing.JLabel();
        goldFeatures = new javax.swing.JTextArea();
        goldButton = new javax.swing.JButton();
        goldBadge = new javax.swing.JLabel();

        diamondPackage = new javax.swing.JPanel();
        diamondTitle = new javax.swing.JLabel();
        diamondPrice = new javax.swing.JLabel();
        diamondDuration = new javax.swing.JLabel();
        diamondFeatures = new javax.swing.JTextArea();
        diamondButton = new javax.swing.JButton();
        diamondBadge = new javax.swing.JLabel();

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
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 24));
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

        packagesTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        packagesTitle.setForeground(new java.awt.Color(0, 120, 215));
        packagesTitle.setText("Chọn gói hội viên");
        packagesTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));
        packagesPanel.add(packagesTitle, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel cardsPanel = new javax.swing.JPanel();
        cardsPanel.setLayout(new java.awt.GridLayout(2, 2, 20, 20));
        cardsPanel.setBackground(new java.awt.Color(245, 245, 250));

        // Gói Thành viên (Miễn phí)
        freePackage.setBackground(new java.awt.Color(255, 255, 255));
        freePackage.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 2),
                javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        freePackage.setLayout(new java.awt.BorderLayout(15, 15));

        freeTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        freeTitle.setForeground(new java.awt.Color(60, 60, 60));
        freeTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        freeTitle.setText("Hạng Thành viên");

        freePrice.setFont(new java.awt.Font("Segoe UI", 1, 20));
        freePrice.setForeground(new java.awt.Color(0, 120, 215));
        freePrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        freePrice.setText("<html>Miễn phí <span style='font-size:11px; color:#777'>(Không giới hạn)</span></html>");

        freeDuration.setFont(new java.awt.Font("Segoe UI", 0, 11));
        freeDuration.setForeground(new java.awt.Color(100, 100, 100));
        freeDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        freeDuration.setText(""); // gộp vào dòng giá

        freeFeatures.setFont(new java.awt.Font("Segoe UI", 0, 11));
        freeFeatures.setEditable(false);
        freeFeatures.setLineWrap(true);
        freeFeatures.setWrapStyleWord(true);
        freeFeatures.setText("• Thuê tối đa 3 quyển\n• 1 lượt thuê miễn phí\n  14 ngày mỗi tháng");
        freeFeatures.setBackground(new java.awt.Color(250, 250, 250));
        freeFeatures.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        freeButton.setBackground(new java.awt.Color(0, 120, 215));
        freeButton.setForeground(new java.awt.Color(255, 255, 255));
        freeButton.setText("Chọn gói");
        freeButton.setFont(new java.awt.Font("Segoe UI", 1, 12));
        freeButton.setFocusPainted(false);
        freeButton.setPreferredSize(new java.awt.Dimension(0, 38));
        freeButton.addActionListener(e -> selectPackage("ThanhVien", 0, 0, 0));

        // Center panel for price, duration and features
        javax.swing.JPanel freeCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        freeCenterPanel.setBackground(new java.awt.Color(255, 255, 255));

        // Price panel
        javax.swing.JPanel freePricePanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        freePricePanel.setBackground(new java.awt.Color(255, 255, 255));
        freePricePanel.add(freePrice, java.awt.BorderLayout.CENTER);
        freePricePanel.add(freeDuration, java.awt.BorderLayout.SOUTH);

        freeCenterPanel.add(freePricePanel, java.awt.BorderLayout.NORTH);
        javax.swing.JScrollPane freeScroll = new javax.swing.JScrollPane(freeFeatures);
        freeScroll.setPreferredSize(new java.awt.Dimension(0, 150));
        freeCenterPanel.add(freeScroll, java.awt.BorderLayout.CENTER);

        // Layout for free package card
        freePackage.add(freeTitle, java.awt.BorderLayout.NORTH);
        freePackage.add(freeCenterPanel, java.awt.BorderLayout.CENTER);
        freePackage.add(freeButton, java.awt.BorderLayout.SOUTH);

        // Gói Bạc
        silverPackage.setBackground(new java.awt.Color(255, 255, 255));
        silverPackage.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(192, 192, 192), 2),
                javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        silverPackage.setLayout(new java.awt.BorderLayout(15, 15));

        silverTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        silverTitle.setForeground(new java.awt.Color(60, 60, 60));
        silverTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        silverTitle.setText("Hạng Bạc");

        silverPrice.setFont(new java.awt.Font("Segoe UI", 1, 20));
        silverPrice.setForeground(new java.awt.Color(192, 192, 192));
        silverPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        silverPrice.setText("<html>100,000 đ <span style='font-size:11px; color:#777'>/ 3 tháng</span></html>");

        silverDuration.setFont(new java.awt.Font("Segoe UI", 0, 11));
        silverDuration.setForeground(new java.awt.Color(100, 100, 100));
        silverDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        silverDuration.setText(""); // gộp vào giá

        silverFeatures.setFont(new java.awt.Font("Segoe UI", 0, 11));
        silverFeatures.setEditable(false);
        silverFeatures.setLineWrap(true);
        silverFeatures.setWrapStyleWord(true);
        silverFeatures.setText(
                "• Thuê tối đa 5 quyển\n• 3 lượt thuê miễn phí\n  14 ngày mỗi tháng\n• Giảm giá 3% mỗi đơn\n• Cộng 3% điểm mỗi đơn");
        silverFeatures.setBackground(new java.awt.Color(250, 250, 250));
        silverFeatures.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        silverButton.setBackground(new java.awt.Color(192, 192, 192));
        silverButton.setForeground(new java.awt.Color(255, 255, 255));
        silverButton.setText("Chọn gói");
        silverButton.setFont(new java.awt.Font("Segoe UI", 1, 12));
        silverButton.setFocusPainted(false);
        silverButton.setPreferredSize(new java.awt.Dimension(0, 38));
        silverButton.addActionListener(e -> selectPackage("Bac", 100000, 0, 3));

        // Center panel for price, duration and features
        javax.swing.JPanel silverCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        silverCenterPanel.setBackground(new java.awt.Color(255, 255, 255));

        // Price panel
        javax.swing.JPanel silverPricePanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        silverPricePanel.setBackground(new java.awt.Color(255, 255, 255));
        silverPricePanel.add(silverPrice, java.awt.BorderLayout.CENTER);
        silverPricePanel.add(silverDuration, java.awt.BorderLayout.SOUTH);

        silverCenterPanel.add(silverPricePanel, java.awt.BorderLayout.NORTH);
        javax.swing.JScrollPane silverScroll = new javax.swing.JScrollPane(silverFeatures);
        silverScroll.setPreferredSize(new java.awt.Dimension(0, 150));
        silverCenterPanel.add(silverScroll, java.awt.BorderLayout.CENTER);

        // Layout for silver package card
        silverPackage.add(silverTitle, java.awt.BorderLayout.NORTH);
        silverPackage.add(silverCenterPanel, java.awt.BorderLayout.CENTER);
        silverPackage.add(silverButton, java.awt.BorderLayout.SOUTH);

        // Gói Vàng (Nổi bật)
        goldPackage.setBackground(new java.awt.Color(255, 248, 220));
        goldPackage.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 200, 0), 3),
                javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        goldPackage.setLayout(new java.awt.BorderLayout(10, 10));

        goldBadge.setFont(new java.awt.Font("Segoe UI", 1, 12));
        goldBadge.setForeground(new java.awt.Color(255, 255, 255));
        goldBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        goldBadge.setBackground(new java.awt.Color(255, 140, 0));
        goldBadge.setOpaque(true);
        goldBadge.setText("PHỔ BIẾN");
        goldBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        goldTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        goldTitle.setForeground(new java.awt.Color(60, 60, 60));
        goldTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        goldTitle.setText("Hạng Vàng");

        goldPrice.setFont(new java.awt.Font("Segoe UI", 1, 20));
        goldPrice.setForeground(new java.awt.Color(255, 140, 0));
        goldPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        goldPrice.setText("<html>200,000 đ <span style='font-size:11px; color:#777'>/ 3 tháng</span></html>");

        goldDuration.setFont(new java.awt.Font("Segoe UI", 0, 11));
        goldDuration.setForeground(new java.awt.Color(100, 100, 100));
        goldDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        goldDuration.setText(""); // gộp vào giá

        goldFeatures.setFont(new java.awt.Font("Segoe UI", 0, 11));
        goldFeatures.setEditable(false);
        goldFeatures.setLineWrap(true);
        goldFeatures.setWrapStyleWord(true);
        goldFeatures.setText(
                "• Thuê tối đa 10 quyển\n• 5 lượt thuê miễn phí\n  14 ngày mỗi tháng\n• Giảm giá 5% mỗi đơn\n• Cộng 5% điểm mỗi đơn");
        goldFeatures.setBackground(new java.awt.Color(255, 248, 220));
        goldFeatures.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        goldButton.setBackground(new java.awt.Color(255, 140, 0));
        goldButton.setForeground(new java.awt.Color(255, 255, 255));
        goldButton.setText("Chọn gói");
        goldButton.setFont(new java.awt.Font("Segoe UI", 1, 12));
        goldButton.setFocusPainted(false);
        goldButton.setPreferredSize(new java.awt.Dimension(0, 38));
        goldButton.addActionListener(e -> selectPackage("Vang", 200000, 0, 3));

        // Center panel for price, duration and features
        javax.swing.JPanel goldCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        goldCenterPanel.setBackground(new java.awt.Color(255, 248, 220));

        // Price panel
        javax.swing.JPanel goldPricePanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        goldPricePanel.setBackground(new java.awt.Color(255, 248, 220));
        goldPricePanel.add(goldPrice, java.awt.BorderLayout.CENTER);
        goldPricePanel.add(goldDuration, java.awt.BorderLayout.SOUTH);

        goldCenterPanel.add(goldPricePanel, java.awt.BorderLayout.NORTH);
        javax.swing.JScrollPane goldScroll = new javax.swing.JScrollPane(goldFeatures);
        goldScroll.setPreferredSize(new java.awt.Dimension(0, 150));
        goldCenterPanel.add(goldScroll, java.awt.BorderLayout.CENTER);

        goldPackage.add(goldTitle, java.awt.BorderLayout.NORTH);
        goldPackage.add(goldCenterPanel, java.awt.BorderLayout.CENTER);
        goldPackage.add(goldButton, java.awt.BorderLayout.SOUTH);

        // Gói Kim cương
        diamondPackage.setBackground(new java.awt.Color(255, 255, 255));
        diamondPackage.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 0, 180), 2),
                javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        diamondPackage.setLayout(new java.awt.BorderLayout(10, 10));

        diamondBadge.setFont(new java.awt.Font("Segoe UI", 1, 12));
        diamondBadge.setForeground(new java.awt.Color(255, 255, 255));
        diamondBadge.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        diamondBadge.setBackground(new java.awt.Color(180, 0, 180));
        diamondBadge.setOpaque(true);
        diamondBadge.setText("CAO CẤP");
        diamondBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        diamondTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        diamondTitle.setForeground(new java.awt.Color(60, 60, 60));
        diamondTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        diamondTitle.setText("Hạng Kim cương");

        diamondPrice.setFont(new java.awt.Font("Segoe UI", 1, 20));
        diamondPrice.setForeground(new java.awt.Color(180, 0, 180));
        diamondPrice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        diamondPrice.setText("<html>300,000 đ <span style='font-size:11px; color:#777'>/ 3 tháng</span></html>");

        diamondDuration.setFont(new java.awt.Font("Segoe UI", 0, 11));
        diamondDuration.setForeground(new java.awt.Color(100, 100, 100));
        diamondDuration.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        diamondDuration.setText(""); // gộp vào giá

        diamondFeatures.setFont(new java.awt.Font("Segoe UI", 0, 11));
        diamondFeatures.setEditable(false);
        diamondFeatures.setLineWrap(true);
        diamondFeatures.setWrapStyleWord(true);
        diamondFeatures.setText(
                "• Thuê tối đa 15 quyển\n• 10 lượt thuê miễn phí\n  14 ngày mỗi tháng\n• Giảm giá 10% mỗi đơn\n• Cộng 10% điểm mỗi đơn");
        diamondFeatures.setBackground(new java.awt.Color(250, 250, 250));
        diamondFeatures.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        diamondButton.setBackground(new java.awt.Color(180, 0, 180));
        diamondButton.setForeground(new java.awt.Color(255, 255, 255));
        diamondButton.setText("Chọn gói");
        diamondButton.setFont(new java.awt.Font("Segoe UI", 1, 12));
        diamondButton.setFocusPainted(false);
        diamondButton.setPreferredSize(new java.awt.Dimension(0, 38));
        diamondButton.addActionListener(e -> selectPackage("KimCuong", 300000, 0, 3));

        // Center panel for price, duration and features
        javax.swing.JPanel diamondCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        diamondCenterPanel.setBackground(new java.awt.Color(255, 255, 255));

        // Price panel
        javax.swing.JPanel diamondPricePanel = new javax.swing.JPanel(new java.awt.BorderLayout(5, 5));
        diamondPricePanel.setBackground(new java.awt.Color(255, 255, 255));
        diamondPricePanel.add(diamondPrice, java.awt.BorderLayout.CENTER);
        diamondPricePanel.add(diamondDuration, java.awt.BorderLayout.SOUTH);

        diamondCenterPanel.add(diamondPricePanel, java.awt.BorderLayout.NORTH);
        javax.swing.JScrollPane diamondScroll = new javax.swing.JScrollPane(diamondFeatures);
        diamondScroll.setPreferredSize(new java.awt.Dimension(0, 150));
        diamondCenterPanel.add(diamondScroll, java.awt.BorderLayout.CENTER);

        diamondPackage.add(diamondTitle, java.awt.BorderLayout.NORTH);
        diamondPackage.add(diamondCenterPanel, java.awt.BorderLayout.CENTER);
        diamondPackage.add(diamondButton, java.awt.BorderLayout.SOUTH);

        cardsPanel.add(freePackage);
        cardsPanel.add(silverPackage);
        cardsPanel.add(goldPackage);
        cardsPanel.add(diamondPackage);

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
                                        .addComponent(cardIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(memberStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(expiryDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(selectedPackageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(discountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(totalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cardIdField)
                                        .addComponent(memberStatusField)
                                        .addComponent(expiryDateField)
                                        .addComponent(selectedPackageField)
                                        .addComponent(discountField)
                                        .addComponent(totalField))
                                .addContainerGap()));

        formLayout.setVerticalGroup(
                formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(formLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cardIdLabel)
                                        .addComponent(cardIdField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(memberStatusLabel)
                                        .addComponent(memberStatusField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(expiryDateLabel)
                                        .addComponent(expiryDateField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(selectedPackageLabel)
                                        .addComponent(selectedPackageField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(discountLabel)
                                        .addComponent(discountField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(totalLabel)
                                        .addComponent(totalField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

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
            // Hiển thị phí chênh lệch
            String priceDisplay = nf.format(upgradeCost) + " đ";
            if (currentRankPrice > 0) {
                priceDisplay += " (chênh lệch)";
            }
            totalField.setText(priceDisplay);
        }

        System.out.println(
                "[PHIHV] Selected: " + packageName + ", Full price: " + fullPrice + ", Upgrade cost: " + upgradeCost);
    }

    /**
     * Trả về level của rank (dùng để so sánh)
     */
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

        // Xác định rank mới từ package name
        String newRank = "Normal";
        if (selectedPackageName.equals("ThanhVien"))
            newRank = "Normal";
        else if (selectedPackageName.equals("Bac"))
            newRank = "Silver";
        else if (selectedPackageName.equals("Vang"))
            newRank = "Gold";
        else if (selectedPackageName.equals("KimCuong"))
            newRank = "Diamond";

        // Kiểm tra số dư từ thẻ chip (nếu gói có phí)
        if (selectedPackagePrice > 0) {
            int cardBalance = 0;
            try {
                CardConnectionManager connManager = new CardConnectionManager();
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
                System.err.println("[PHIHV] Error reading card: " + e.getMessage());
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

        // Thực hiện thanh toán và upgrade trên thẻ chip
        boolean cardTransactionSuccess = false;
        CardConnectionManager connManager = null;
        try {
            connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

                // Trừ tiền nếu có phí
                if (selectedPackagePrice > 0) {
                    boolean paymentOk = balanceManager.payment((int) selectedPackagePrice);
                    if (!paymentOk) {
                        javax.swing.JOptionPane.showMessageDialog(this, "Thanh toán trên thẻ thất bại!", "Lỗi",
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    System.out.println("[PHIHV] Payment success: " + (int) selectedPackagePrice + " VND");
                }

                // Upgrade rank trên thẻ (nếu không phải Normal)
                if (!newRank.equals("Normal")) {
                    boolean upgradeOk = balanceManager.upgradeRank(newRank);
                    if (!upgradeOk) {
                        System.err.println("[PHIHV] Upgrade rank on card failed, but payment was done.");
                        // Thanh toán đã thành công, chỉ warning về upgrade
                    }
                }

                cardTransactionSuccess = true;
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối thẻ để thanh toán!", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception e) {
            System.err.println("[PHIHV] Card transaction error: " + e.getMessage());
        } finally {
            try {
                if (connManager != null)
                    connManager.disconnectCard();
            } catch (Exception ignored) {
            }
        }

        if (!cardTransactionSuccess) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giao dịch thẻ thất bại!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cập nhật database để đồng bộ
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

                    // Create transaction record chỉ khi gói có phí
                    if (selectedPackagePrice > 0) {
                        String transSql = "INSERT INTO Transactions (TransID, CardID, Type, Amount, PointsChanged, DateTime, SignatureCard, SignatureStore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement transStmt = conn.prepareStatement(transSql)) {
                            transStmt.setString(1, UUID.randomUUID().toString());
                            transStmt.setString(2, currentCardId);
                            transStmt.setString(3, "Payment");
                            transStmt.setDouble(4, -selectedPackagePrice);
                            transStmt.setInt(5, 0);
                            transStmt.setString(6, java.time.LocalDateTime.now().toString());
                            transStmt.setBytes(7, new byte[] {});
                            transStmt.setBytes(8, new byte[] {});
                            transStmt.executeUpdate();
                        }
                    }

                    conn.commit();

                    String successMessage;
                    if (selectedPackagePrice == 0) {
                        successMessage = "Đăng ký thành công!\nGói hội viên: " + selectedPackageField.getText();
                    } else {
                        successMessage = "Thanh toán thành công!\nGói hội viên: " + selectedPackageField.getText() +
                                "\nSố tiền: "
                                + NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(selectedPackagePrice)
                                + " đ";
                    }

                    javax.swing.JOptionPane.showMessageDialog(this,
                            successMessage,
                            "Thông báo",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);

                    // Reset selection and reload info
                    selectedPackageName = "";
                    selectedPackagePrice = 0;
                    selectedDiscount = 0;
                    selectedMonths = 0;
                    selectedPackageField.setText("");
                    discountField.setText("0%");
                    totalField.setText("0 đ");
                    loadCardInfo();

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối database!", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật DB: " + e.getMessage(), "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel packagesPanel;
    private javax.swing.JLabel packagesTitle;
    private javax.swing.JPanel freePackage;
    private javax.swing.JLabel freeTitle;
    private javax.swing.JLabel freePrice;
    private javax.swing.JLabel freeDuration;
    private javax.swing.JTextArea freeFeatures;
    private javax.swing.JButton freeButton;
    private javax.swing.JPanel silverPackage;
    private javax.swing.JLabel silverTitle;
    private javax.swing.JLabel silverPrice;
    private javax.swing.JLabel silverDuration;
    private javax.swing.JTextArea silverFeatures;
    private javax.swing.JButton silverButton;
    private javax.swing.JPanel goldPackage;
    private javax.swing.JLabel goldTitle;
    private javax.swing.JLabel goldPrice;
    private javax.swing.JLabel goldDuration;
    private javax.swing.JTextArea goldFeatures;
    private javax.swing.JButton goldButton;
    private javax.swing.JLabel goldBadge;
    private javax.swing.JPanel diamondPackage;
    private javax.swing.JLabel diamondTitle;
    private javax.swing.JLabel diamondPrice;
    private javax.swing.JLabel diamondDuration;
    private javax.swing.JTextArea diamondFeatures;
    private javax.swing.JButton diamondButton;
    private javax.swing.JLabel diamondBadge;
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
