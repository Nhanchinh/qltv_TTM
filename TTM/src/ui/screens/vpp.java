/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.StationeryService;
import services.CardService;
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author admin
 */
public class vpp extends javax.swing.JPanel {

    private StationeryService stationeryService;
    private CardService cardService;
    private String currentCardId = "CARD001";
    private int maxPointsAvailable = 0;

    // Độ giảm giá và tích điểm theo hạng thành viên
    private double currentDiscount = 0; // Normal=0%, Silver=3%, Gold=5%, Diamond=10%
    private double currentPointsPercent = 0; // Normal=0%, Silver=3%, Gold=5%, Diamond=10%

    /**
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            updateCardInfo();
        }
    }

    private List<CartItem> cartItems;

    private static class CartItem {
        String itemId;
        String name;
        int quantity;
        double unitPrice;
        double discountPercent; // Giảm giá theo hạng thành viên

        CartItem(String itemId, String name, int quantity, double unitPrice, double discountPercent) {
            this.itemId = itemId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discountPercent = discountPercent;
        }

        double getTotalPrice() {
            return unitPrice * quantity * (1 - discountPercent / 100.0);
        }

        // Lấy giá gốc (không giảm giá) để tính điểm
        double getOriginalPrice() {
            return unitPrice * quantity;
        }
    }

    /**
     * Creates new form OfficeSuppliesPanel
     */
    public vpp() {
        stationeryService = new StationeryService();
        cardService = new CardService();
        cartItems = new ArrayList<>();
        initComponents();
        loadStationeryItems();
        updateCardInfo();
    }

    private void loadStationeryItems() {
        List<StationeryService.StationeryItem> items = stationeryService.getAllItems();
        String[] columns = { "Mã SP", "Tên sản phẩm", "Giá", "Tồn kho" };
        Object[][] data = new Object[items.size()][4];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        for (int i = 0; i < items.size(); i++) {
            StationeryService.StationeryItem item = items.get(i);
            data[i][0] = item.itemId;
            data[i][1] = item.name;
            data[i][2] = nf.format(item.price) + " đ";
            data[i][3] = item.stock;
        }
        productsTableScroll.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    private void updateCardInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);
        if (card != null) {
            cardIdField.setText(card.cardId);

            // Tính giảm giá và điểm thưởng theo hạng thành viên
            String memberType = card.memberType;
            if (memberType != null) {
                if (memberType.equalsIgnoreCase("Normal") || memberType.equalsIgnoreCase("ThanhVien")) {
                    currentDiscount = 0;
                    currentPointsPercent = 0;
                } else if (memberType.equalsIgnoreCase("Silver") || memberType.equalsIgnoreCase("Bac")) {
                    currentDiscount = 3;
                    currentPointsPercent = 3;
                } else if (memberType.equalsIgnoreCase("Gold") || memberType.equalsIgnoreCase("Vang")) {
                    currentDiscount = 5;
                    currentPointsPercent = 5;
                } else if (memberType.equalsIgnoreCase("Diamond") || memberType.equalsIgnoreCase("KimCuong")) {
                    currentDiscount = 10;
                    currentPointsPercent = 10;
                }
            }
            System.out.println("[VPP] Member: " + memberType + ", Discount: " + currentDiscount + "%, Points: "
                    + currentPointsPercent + "%");
        }
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (saleDateField != null)
            saleDateField.setText(dateFormat.format(new java.util.Date()));

        // Lấy điểm từ thẻ (Smart Card)
        int currentPoints = 0;
        try {
            CardConnectionManager connManager = CardConnectionManager.getInstance();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                if (info.success) {
                    currentPoints = info.points;
                    System.out.println("[VPP_UPDATE_INFO] Points from card: " + currentPoints);
                }
                connManager.disconnectCard();
            } else {
                // Fallback nếu không kết nối được thẻ
                System.out.println("[VPP_UPDATE_INFO] Cannot connect card, fallback to DB.");
                if (card != null)
                    currentPoints = card.totalPoints;
            }
        } catch (Exception e) {
            System.err.println("[VPP_UPDATE_INFO] Error reading card: " + e.getMessage());
            if (card != null)
                currentPoints = card.totalPoints;
        }

        this.maxPointsAvailable = currentPoints;

        // Tự động set điểm sử dụng = điểm tích lũy hiện có
        if (pointsUsedField != null) {
            pointsUsedField.setText(String.valueOf(currentPoints));
        }

        updateCartTable();
    }

    private void updateCartTable() {
        String[] columns = { "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Giảm giá (%)", "Thành tiền" };
        Object[][] data = new Object[cartItems.size()][6];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        double total = 0;
        int totalPoints = 0;

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            data[i][0] = item.itemId;
            data[i][1] = item.name;
            data[i][2] = item.quantity;
            data[i][3] = nf.format(item.unitPrice) + " đ";
            data[i][4] = (int) item.discountPercent + "%";
            double itemTotal = item.getTotalPrice();
            data[i][5] = nf.format(itemTotal) + " đ";
            total += itemTotal;

            // Tính điểm thưởng: currentPointsPercent% của giá gốc
            int pointsFromItem = (int) Math.round(item.getOriginalPrice() * currentPointsPercent / 100.0);
            totalPoints += pointsFromItem;
        }

        // Lấy số điểm sử dụng từ field (nếu có)
        int pointsUsed = 0;
        if (pointsUsedField != null) {
            try {
                String pointsText = pointsUsedField.getText().trim();
                if (!pointsText.isEmpty()) {
                    pointsUsed = Integer.parseInt(pointsText);
                    if (pointsUsed < 0)
                        pointsUsed = 0;

                    // Kiểm tra không được vượt quá điểm tích lũy hiện có (cache từ thẻ)
                    if (pointsUsed > maxPointsAvailable) {
                        pointsUsed = maxPointsAvailable;
                        pointsUsedField.setText(String.valueOf(pointsUsed));
                    }
                }
            } catch (NumberFormatException e) {
                pointsUsed = 0;
            }
        }

        // Trừ điểm vào tổng tiền (1 điểm = 1 VND)
        double finalTotal = total - pointsUsed;
        if (finalTotal < 0)
            finalTotal = 0;

        if (cartTableScroll != null)
            cartTableScroll.setModel(new javax.swing.table.DefaultTableModel(data, columns));
        NumberFormat nf2 = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        if (totalField != null)
            totalField.setText(nf2.format(finalTotal) + " đ");
        if (pointsEarnedField != null)
            pointsEarnedField.setText(String.valueOf(totalPoints));
    }

    /**
     * Modern initComponents with Split View
     */
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

        // Left Side: Search + Product List (60%)
        gbc.gridx = 0;
        gbc.weightx = 0.6;
        gbc.insets = new java.awt.Insets(0, 0, 0, 10);

        javax.swing.JPanel leftWrapper = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        leftWrapper.setOpaque(false);
        leftWrapper.add(createSearchPanel(), java.awt.BorderLayout.NORTH);
        leftWrapper.add(createProductListPanel(), java.awt.BorderLayout.CENTER);

        contentPanel.add(leftWrapper, gbc);

        // Right Side: Details + Cart (40%)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new java.awt.Insets(0, 10, 0, 0);

        javax.swing.JPanel rightWrapper = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        rightWrapper.setOpaque(false);
        rightWrapper.add(createDetailsPanel(), java.awt.BorderLayout.NORTH);
        rightWrapper.add(createCartPanel(), java.awt.BorderLayout.CENTER);

        contentPanel.add(rightWrapper, gbc);

        add(contentPanel, java.awt.BorderLayout.CENTER);
    }

    // --- Component Creators ---

    private javax.swing.JPanel createHeaderPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));
        p.setPreferredSize(new java.awt.Dimension(0, 70));

        javax.swing.JLabel title = new javax.swing.JLabel("Văn Phòng Phẩm");
        title.setFont(new java.awt.Font("Segoe UI", 1, 24));
        title.setForeground(new java.awt.Color(15, 23, 42)); // Slate 900
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 25, 0, 0));

        p.add(title, java.awt.BorderLayout.WEST);
        return p;
    }

    private javax.swing.JPanel createSearchPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(15, 0));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        searchLabel = new javax.swing.JLabel("Tìm kiếm:");
        searchLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        searchLabel.setForeground(new java.awt.Color(100, 116, 139));

        searchField = createStyledTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Nhập tên sản phẩm...");

        searchButton = createModernButton("Tìm", java.awt.Color.WHITE, new java.awt.Color(59, 130, 246));
        searchButton.setPreferredSize(new java.awt.Dimension(80, 40));
        searchButton.addActionListener(e -> searchProducts());

        p.add(searchLabel, java.awt.BorderLayout.WEST);
        p.add(searchField, java.awt.BorderLayout.CENTER);
        p.add(searchButton, java.awt.BorderLayout.EAST);

        return p;
    }

    private javax.swing.JPanel createProductListPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(0, 10));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        javax.swing.JLabel header = new javax.swing.JLabel("Danh sách sản phẩm");
        header.setFont(new java.awt.Font("Segoe UI", 1, 16));
        header.setForeground(new java.awt.Color(30, 41, 59));
        p.add(header, java.awt.BorderLayout.NORTH);

        String[] columns = { "Mã SP", "Tên sản phẩm", "Giá", "Tồn kho" };
        productsTableScroll = createStyledTable(new Object[][] {}, columns);
        productsTableScroll.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = productsTableScroll.getSelectedRow();
            if (selectedRow >= 0) {
                String itemId = productsTableScroll.getValueAt(selectedRow, 0).toString();
                StationeryService.StationeryItem item = stationeryService.getItemById(itemId);
                if (item != null) {
                    productIdField.setText(item.itemId);
                    productNameField.setText(item.name);
                    NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    priceField.setText(nf.format(item.price) + " đ");
                    stockField.setText(String.valueOf(item.stock));
                }
            }
        });

        productsTable = new javax.swing.JScrollPane(productsTableScroll);
        productsTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 245, 249)));
        productsTable.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        p.add(productsTable, java.awt.BorderLayout.CENTER);

        return p;
    }

    private javax.swing.JPanel createDetailsPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.GridBagLayout());
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Compact padding

        javax.swing.JLabel header = new javax.swing.JLabel("Thông tin sản phẩm");
        header.setFont(new java.awt.Font("Segoe UI", 1, 16));
        header.setForeground(new java.awt.Color(30, 41, 59));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(2, 5, 2, 5); // Tighter insets

        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(0, 0, 10, 0);
        p.add(header, gbc);

        // Reset
        gbc.gridwidth = 1;
        gbc.insets = new java.awt.Insets(2, 0, 2, 10);

        // Fields
        addDetailRow(p, gbc, 1, "Mã SP:", productIdLabel = createLabel(""), productIdField = createStyledTextField());
        addDetailRow(p, gbc, 2, "Tên SP:", productNameLabel = createLabel(""),
                productNameField = createStyledTextField());
        addDetailRow(p, gbc, 3, "Giá:", priceLabel = createLabel(""), priceField = createStyledTextField());
        addDetailRow(p, gbc, 4, "Tồn kho:", stockLabel = createLabel(""), stockField = createStyledTextField());

        // Read-only
        productIdField.setEditable(false);
        productNameField.setEditable(false);
        priceField.setEditable(false);
        stockField.setEditable(false);

        // Quantity & Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        quantityLabel = createLabel("Số lượng:");
        p.add(quantityLabel, gbc);

        gbc.gridx = 1;
        javax.swing.JPanel actionPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        actionPanel.setOpaque(false);

        quantitySpinner = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setFont(new java.awt.Font("Segoe UI", 0, 14));
        quantitySpinner.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        quantitySpinner.setPreferredSize(new java.awt.Dimension(80, 36)); // Compact height

        addToCartButton = createModernButton("Thêm vào giỏ", java.awt.Color.WHITE, new java.awt.Color(22, 163, 74));
        addToCartButton.setPreferredSize(new java.awt.Dimension(120, 36));
        addToCartButton.addActionListener(e -> addToCart());

        actionPanel.add(quantitySpinner, java.awt.BorderLayout.WEST);
        actionPanel.add(addToCartButton, java.awt.BorderLayout.CENTER);

        p.add(actionPanel, gbc);

        return p;
    }

    private void addDetailRow(javax.swing.JPanel p, java.awt.GridBagConstraints gbc, int row, String label,
            javax.swing.JLabel lblComp, javax.swing.JTextField txtComp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        lblComp.setText(label);
        lblComp.setFont(new java.awt.Font("Segoe UI", 1, 13));
        p.add(lblComp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtComp.setPreferredSize(new java.awt.Dimension(0, 34));
        p.add(txtComp, gbc);
    }

    private javax.swing.JPanel createCartPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(0, 5)); // Reduced vertical gap
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Compact padding

        // Header
        javax.swing.JPanel header = new javax.swing.JPanel(new java.awt.BorderLayout());
        header.setOpaque(false);
        javax.swing.JLabel title = new javax.swing.JLabel("Giỏ hàng");
        title.setFont(new java.awt.Font("Segoe UI", 1, 16));
        title.setForeground(new java.awt.Color(30, 41, 59));

        totalLabel = new javax.swing.JLabel("Tổng tiền:");
        header.add(title, java.awt.BorderLayout.WEST);
        p.add(header, java.awt.BorderLayout.NORTH);

        // Table
        String[] cartColumns = { "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" };
        cartTableScroll = createStyledTable(new Object[][] {}, cartColumns);
        cartTableScroll.setRowHeight(30); // Compact row
        cartTable = new javax.swing.JScrollPane(cartTableScroll);
        cartTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 245, 249)));
        cartTable.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        p.add(cartTable, java.awt.BorderLayout.CENTER);

        // Bottom Info
        javax.swing.JPanel botPanel = new javax.swing.JPanel();
        botPanel.setLayout(new javax.swing.BoxLayout(botPanel, javax.swing.BoxLayout.Y_AXIS));
        botPanel.setOpaque(false);
        botPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(241, 245, 249)));

        // Info Grid
        javax.swing.JPanel infoGrid = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        infoGrid.setOpaque(false);
        infoGrid.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 5, 0));

        infoGrid.add(createCompactInfoPanel("Mã thẻ:", cardIdField = createStyledTextField()));
        cardIdField.setText(currentCardId);
        cardIdField.setEditable(false);

        infoGrid.add(createCompactInfoPanel("Ngày bán:", saleDateField = createStyledTextField()));
        saleDateField.setEditable(false);

        pointsUsedField = createStyledTextField();
        pointsUsedField.setText("0");
        pointsUsedField.addActionListener(e -> updateCartTable());
        pointsUsedField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                updateCartTable();
            }
        });
        infoGrid.add(createCompactInfoPanel("Điểm dùng:", pointsUsedField));

        // Thêm field hiển thị điểm thưởng
        pointsEarnedField = createStyledTextField();
        pointsEarnedField.setText("0");
        pointsEarnedField.setEditable(false);
        pointsEarnedField.setForeground(new java.awt.Color(22, 163, 74)); // Green
        infoGrid.add(createCompactInfoPanel("Điểm thưởng:", pointsEarnedField));

        botPanel.add(infoGrid);

        // Total and Actions
        javax.swing.JPanel totalActionPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        totalActionPanel.setOpaque(false);
        totalActionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));

        totalField = new javax.swing.JTextField("0 đ");
        totalField.setFont(new java.awt.Font("Segoe UI", 1, 18));
        totalField.setForeground(new java.awt.Color(22, 163, 74));
        totalField.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalField.setBorder(null);
        totalField.setOpaque(false);
        totalField.setEditable(false);

        totalActionPanel.add(totalField, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel btnPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);

        clearCartButton = createModernButton("Xóa", java.awt.Color.WHITE, new java.awt.Color(239, 68, 68));
        clearCartButton.setPreferredSize(new java.awt.Dimension(70, 36));
        clearCartButton.addActionListener(e -> clearCart());

        checkoutButton = createModernButton("Thanh toán", java.awt.Color.WHITE, new java.awt.Color(59, 130, 246));
        checkoutButton.setPreferredSize(new java.awt.Dimension(110, 36));
        checkoutButton.addActionListener(e -> checkout());

        btnPanel.add(clearCartButton);
        btnPanel.add(checkoutButton);

        totalActionPanel.add(btnPanel, java.awt.BorderLayout.SOUTH);

        botPanel.add(totalActionPanel);

        p.add(botPanel, java.awt.BorderLayout.SOUTH);
        return p;
    }

    private javax.swing.JPanel createCompactInfoPanel(String label, javax.swing.JComponent field) {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(5, 0));
        p.setOpaque(false);
        javax.swing.JLabel lbl = new javax.swing.JLabel(label);
        lbl.setFont(new java.awt.Font("Segoe UI", 0, 11));
        lbl.setForeground(new java.awt.Color(100, 116, 139));
        p.add(lbl, java.awt.BorderLayout.NORTH);
        field.setFont(new java.awt.Font("Segoe UI", 0, 13));
        field.setPreferredSize(new java.awt.Dimension(0, 28));
        p.add(field, java.awt.BorderLayout.CENTER);
        return p;
    }

    // --- Helpers ---

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

    private javax.swing.JLabel createLabel(String text) {
        javax.swing.JLabel l = new javax.swing.JLabel(text);
        l.setFont(new java.awt.Font("Segoe UI", 1, 14));
        l.setForeground(new java.awt.Color(100, 116, 139));
        return l;
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

    private javax.swing.JTable createStyledTable(Object[][] data, String[] columns) {
        javax.swing.JTable t = new javax.swing.JTable(new javax.swing.table.DefaultTableModel(data, columns));
        t.setFont(new java.awt.Font("Segoe UI", 0, 14));
        t.setRowHeight(35);
        t.setSelectionBackground(new java.awt.Color(239, 246, 255));
        t.setSelectionForeground(new java.awt.Color(15, 23, 42));
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new java.awt.Color(241, 245, 249));
        t.getTableHeader().setFont(new java.awt.Font("Segoe UI", 1, 14));
        t.getTableHeader().setBackground(new java.awt.Color(248, 250, 252));
        t.getTableHeader().setForeground(new java.awt.Color(100, 116, 139));
        t.getTableHeader()
                .setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));
        return t;
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

    private void filterByCategory() {
        // Filter by category (co the implement sau)
        loadStationeryItems();
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadStationeryItems();
            return;
        }
        List<StationeryService.StationeryItem> allItems = stationeryService.getAllItems();
        List<StationeryService.StationeryItem> filtered = new ArrayList<>();
        for (StationeryService.StationeryItem item : allItems) {
            if (item.itemId.toLowerCase().contains(keyword) ||
                    item.name.toLowerCase().contains(keyword)) {
                filtered.add(item);
            }
        }
        String[] columns = { "Mã SP", "Tên sản phẩm", "Giá", "Tồn kho" };
        Object[][] data = new Object[filtered.size()][4];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        for (int i = 0; i < filtered.size(); i++) {
            StationeryService.StationeryItem item = filtered.get(i);
            data[i][0] = item.itemId;
            data[i][1] = item.name;
            data[i][2] = nf.format(item.price) + " đ";
            data[i][3] = item.stock;
        }
        productsTableScroll.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    private void addToCart() {
        String productId = productIdField.getText().trim();
        if (productId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui long chon san pham!", "Thong bao",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        StationeryService.StationeryItem item = stationeryService.getItemById(productId);
        if (item == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Khong tim thay san pham!", "Loi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        if (item.stock < quantity) {
            javax.swing.JOptionPane.showMessageDialog(this, "Khong du so luong!", "Thong bao",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if item already in cart
        boolean found = false;
        for (CartItem cartItem : cartItems) {
            if (cartItem.itemId.equals(productId)) {
                cartItem.quantity += quantity;
                found = true;
                break;
            }
        }

        if (!found) {
            // Thêm với giảm giá theo hạng thành viên
            cartItems.add(new CartItem(productId, item.name, quantity, item.price, currentDiscount));
        }

        // Tự động set điểm sử dụng = điểm tích lũy từ thẻ (luôn luôn, không chỉ khi giỏ
        // trống)
        if (pointsUsedField != null && maxPointsAvailable > 0) {
            pointsUsedField.setText(String.valueOf(maxPointsAvailable));
        }

        updateCartTable();
        javax.swing.JOptionPane.showMessageDialog(this, "Da them " + quantity + " san pham vao gio hang!", "Thong bao",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giỏ hàng trống!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy số điểm sử dụng
        int pointsUsed = 0;
        try {
            String pointsText = pointsUsedField.getText().trim();
            if (!pointsText.isEmpty()) {
                pointsUsed = Integer.parseInt(pointsText);
                if (pointsUsed < 0)
                    pointsUsed = 0;
            }
        } catch (NumberFormatException e) {
            pointsUsed = 0;
        }

        // Kiểm tra đủ điểm không (dựa trên maxPointsAvailable đã lấy từ thẻ)
        if (pointsUsed > maxPointsAvailable) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Không đủ điểm! Bạn có " + maxPointsAvailable + " điểm (trong thẻ).",
                    "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = javax.swing.JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán?", "Xác nhận",
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

        // Tính tổng tiền
        double grandTotal = 0;
        double originalTotal = 0;
        for (CartItem item : cartItems) {
            grandTotal += item.getTotalPrice();
            originalTotal += item.getOriginalPrice();
        }

        // Số tiền thực cần thanh toán qua thẻ
        double amountToPay = grandTotal - pointsUsed;
        if (amountToPay < 0)
            amountToPay = 0;

        // Điểm thưởng = currentPointsPercent% của giá gốc (trước giảm giá)
        int pointsToAward = (int) Math.round(originalTotal * currentPointsPercent / 100.0);

        // Check card balance and points via smart card
        boolean cardHasEnough = false;
        CardConnectionManager connManager = null;
        try {
            connManager = CardConnectionManager.getInstance();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                if (info.success) {
                    System.out.println("[VPP_CHECKOUT] Card balance: " + info.balance + " VND, Points: " + info.points);

                    // Validate lại điểm trên thẻ lần cuối
                    if (pointsUsed > 0 && info.points < pointsUsed) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Thẻ không đủ điểm để thanh toán! (Có: " + info.points + ", Cần dùng: " + pointsUsed
                                        + ")",
                                "Lỗi thẻ", javax.swing.JOptionPane.WARNING_MESSAGE);
                        connManager.disconnectCard();
                        return;
                    }

                    if (info.balance >= (int) amountToPay) {
                        cardHasEnough = true;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[VPP_CHECKOUT] Error checking card balance: " + ex.getMessage());
        } finally {
            try {
                if (connManager != null)
                    connManager.disconnectCard();
            } catch (Exception ignored) {
            }
        }

        if (!cardHasEnough) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Số dư thẻ không đủ để thanh toán " + (int) amountToPay + " VND.", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1) Cập nhật DB
        boolean dbSuccess = true;
        int distributedPoints = 0;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            int itemPoints = 0;

            if (pointsUsed > 0 && grandTotal > 0) {
                if (i == cartItems.size() - 1) {
                    itemPoints = pointsUsed - distributedPoints;
                } else {
                    double itemRatio = item.getTotalPrice() / grandTotal;
                    itemPoints = (int) Math.round(pointsUsed * itemRatio);
                    distributedPoints += itemPoints;
                }
            }

            if (!stationeryService.sellItem(currentCardId, item.itemId, item.quantity, itemPoints)) {
                dbSuccess = false;
                break;
            }
        }

        if (!dbSuccess) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật cơ sở dữ liệu.", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Charge the card & Use Points
        boolean transactionSuccess = false;
        try {
            connManager = CardConnectionManager.getInstance();
            if (!connManager.connectCard()) {
                throw new Exception("Không thể kết nối lại thẻ để thanh toán.");
            }
            CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

            // Xử lý trừ điểm trước (nếu có)
            boolean pointsDeducted = true;
            if (pointsUsed > 0) {
                pointsDeducted = balanceManager.usePoints(pointsUsed);
                if (!pointsDeducted) {
                    System.err.println("[VPP_CHECKOUT] Failed to deduct points from card.");
                } else {
                    System.out.println("[VPP_CHECKOUT] Deducted " + pointsUsed + " points from card.");
                }
            }

            // Nếu trừ điểm thành công (hoặc không dùng điểm), mới trừ tiền
            if (pointsDeducted) {
                boolean paymentOk = balanceManager.payment((int) amountToPay);
                if (paymentOk) {
                    System.out.println("[VPP_CHECKOUT] Card charged: " + (int) amountToPay + " VND");
                    transactionSuccess = true;
                    // Cộng điểm thưởng vào thẻ
                    if (pointsToAward > 0) {
                        boolean ptsOk = balanceManager.addPoints(pointsToAward);
                        System.out.println("[VPP_CHECKOUT] Added points to card: " + pointsToAward + " => " + ptsOk);
                    }
                } else {
                    System.err.println("[VPP_CHECKOUT] Card payment failed.");
                }
            }
        } catch (Exception ex) {
            System.err.println("[VPP_CHECKOUT] Exception during payment: " + ex.getMessage());
        } finally {
            try {
                if (connManager != null)
                    connManager.disconnectCard();
            } catch (Exception ignored) {
            }
        }

        if (!transactionSuccess) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Giao dịch thẻ thất bại nhưng dữ liệu DB đã được cập nhật. Vui lòng liên hệ admin.",
                    "Lỗi nghiêm trọng", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Đồng bộ điểm đã dùng vào DB
        if (pointsUsed > 0) {
            cardService.usePoints(currentCardId, pointsUsed);
        }

        javax.swing.JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thông báo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        cartItems.clear();
        pointsUsedField.setText("0");
        updateCartTable();
        loadStationeryItems();
        updateCardInfo();
    }

    private void clearCart() {
        int option = javax.swing.JOptionPane.showConfirmDialog(this,
                "Ban co chac chan muon xoa toan bo gio hang?", "Xac nhan",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (option == javax.swing.JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartTable();
        }
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel productsPanel;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JComboBox<String> categoryCombo;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton searchButton;
    private javax.swing.JScrollPane productsTable;
    private javax.swing.JTable productsTableScroll;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JPanel productDetailsPanel;
    private javax.swing.JLabel productDetailsTitle;
    private javax.swing.JLabel productIdLabel;
    private javax.swing.JTextField productIdField;
    private javax.swing.JLabel productNameLabel;
    private javax.swing.JTextField productNameField;
    private javax.swing.JLabel priceLabel;
    private javax.swing.JTextField priceField;
    private javax.swing.JLabel stockLabel;
    private javax.swing.JTextField stockField;
    private javax.swing.JLabel quantityLabel;
    private javax.swing.JSpinner quantitySpinner;
    private javax.swing.JButton addToCartButton;
    private javax.swing.JPanel cartPanel;
    private javax.swing.JLabel cartTitle;
    private javax.swing.JScrollPane cartTable;
    private javax.swing.JTable cartTableScroll;
    private javax.swing.JLabel cardIdLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JLabel pointsUsedLabel;
    private javax.swing.JTextField pointsUsedField;
    private javax.swing.JLabel saleDateLabel;
    private javax.swing.JTextField saleDateField;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JTextField totalField;
    private javax.swing.JButton checkoutButton;
    private javax.swing.JButton clearCartButton;
    private javax.swing.JLabel pointsEarnedLabel;
    private javax.swing.JTextField pointsEarnedField;
}
