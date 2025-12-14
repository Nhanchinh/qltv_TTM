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

        CartItem(String itemId, String name, int quantity, double unitPrice) {
            this.itemId = itemId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        double getTotalPrice() {
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
        }
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        saleDateField.setText(dateFormat.format(new java.util.Date()));

        // Lấy điểm từ thẻ (Smart Card)
        int currentPoints = 0;
        try {
            CardConnectionManager connManager = new CardConnectionManager();
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
        String[] columns = { "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" };
        Object[][] data = new Object[cartItems.size()][5];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        double total = 0;

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            data[i][0] = item.itemId;
            data[i][1] = item.name;
            data[i][2] = item.quantity;
            data[i][3] = nf.format(item.unitPrice) + " đ";
            double itemTotal = item.getTotalPrice();
            data[i][4] = nf.format(itemTotal) + " đ";
            total += itemTotal;
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

        cartTableScroll.setModel(new javax.swing.table.DefaultTableModel(data, columns));
        NumberFormat nf2 = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        totalField.setText(nf2.format(finalTotal) + " đ");
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

        // Left panel - Danh sách văn phòng phẩm
        productsPanel = new javax.swing.JPanel();
        categoryLabel = new javax.swing.JLabel();
        categoryCombo = new javax.swing.JComboBox<>();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        productsTable = new javax.swing.JScrollPane();
        productsTableScroll = new javax.swing.JTable();

        // Right panel - Chi tiết và giỏ hàng
        detailsPanel = new javax.swing.JPanel();

        // Product details
        productDetailsPanel = new javax.swing.JPanel();
        productDetailsTitle = new javax.swing.JLabel();
        productIdLabel = new javax.swing.JLabel();
        productIdField = new javax.swing.JTextField();
        productNameLabel = new javax.swing.JLabel();
        productNameField = new javax.swing.JTextField();
        priceLabel = new javax.swing.JLabel();
        priceField = new javax.swing.JTextField();
        stockLabel = new javax.swing.JLabel();
        stockField = new javax.swing.JTextField();
        quantityLabel = new javax.swing.JLabel();
        quantitySpinner = new javax.swing.JSpinner();
        addToCartButton = new javax.swing.JButton();

        // Cart panel
        cartPanel = new javax.swing.JPanel();
        cartTitle = new javax.swing.JLabel();
        cartTable = new javax.swing.JScrollPane();
        cartTableScroll = new javax.swing.JTable();
        cardIdLabel = new javax.swing.JLabel();
        cardIdField = new javax.swing.JTextField();
        pointsUsedLabel = new javax.swing.JLabel();
        pointsUsedField = new javax.swing.JTextField();
        saleDateLabel = new javax.swing.JLabel();
        saleDateField = new javax.swing.JTextField();
        totalLabel = new javax.swing.JLabel();
        totalField = new javax.swing.JTextField();
        checkoutButton = new javax.swing.JButton();
        clearCartButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 20));

        // Title
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Mua đồ dùng văn phòng phẩm");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        mainContainer.setLayout(new java.awt.BorderLayout(20, 0));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 30, 40));
        mainContainer.setBackground(new java.awt.Color(245, 245, 250));

        // ============ LEFT PANEL - DANH SÁCH VPP ============
        productsPanel.setBackground(new java.awt.Color(255, 255, 255));
        productsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Danh sách văn phòng phẩm",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)));
        productsPanel.setLayout(new java.awt.BorderLayout(0, 10));
        productsPanel.setPreferredSize(new java.awt.Dimension(600, 0));
        productsPanel.setMinimumSize(new java.awt.Dimension(500, 0));
        productsPanel.setMaximumSize(new java.awt.Dimension(700, Integer.MAX_VALUE));

        // Filter panel
        javax.swing.JPanel filterPanel = new javax.swing.JPanel();
        filterPanel.setLayout(new java.awt.BorderLayout(10, 10));
        filterPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        filterPanel.setBackground(new java.awt.Color(255, 255, 255));

        categoryLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        categoryLabel.setText("Danh mục:");
        categoryCombo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        categoryCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tất cả" }));
        categoryCombo.addActionListener(e -> filterByCategory());

        searchLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        searchLabel.setText("Tìm kiếm:");
        searchField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        searchField.setColumns(15);
        searchButton.setBackground(new java.awt.Color(0, 120, 215));
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("🔍 Tìm");
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchProducts());

        javax.swing.JPanel categoryPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        categoryPanel.setBackground(new java.awt.Color(255, 255, 255));
        categoryPanel.add(categoryLabel, java.awt.BorderLayout.WEST);
        categoryPanel.add(categoryCombo, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel searchPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        searchPanel.setBackground(new java.awt.Color(255, 255, 255));
        searchPanel.add(searchLabel, java.awt.BorderLayout.WEST);
        searchPanel.add(searchField, java.awt.BorderLayout.CENTER);
        searchPanel.add(searchButton, java.awt.BorderLayout.EAST);

        javax.swing.JPanel filterTopPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        filterTopPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterTopPanel.add(categoryPanel, java.awt.BorderLayout.CENTER);
        filterTopPanel.add(searchPanel, java.awt.BorderLayout.EAST);

        filterPanel.add(filterTopPanel, java.awt.BorderLayout.CENTER);

        // Products table - se duoc load tu database
        String[] columns = { "Mã SP", "Tên sản phẩm", "Giá", "Tồn kho" };
        Object[][] data = {};
        productsTableScroll = new javax.swing.JTable(data, columns);
        productsTableScroll.setFont(new java.awt.Font("Segoe UI", 0, 12));
        productsTableScroll.setRowHeight(25);
        productsTableScroll.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
        productsTable.setViewportView(productsTableScroll);

        productsPanel.add(filterPanel, java.awt.BorderLayout.NORTH);
        productsPanel.add(productsTable, java.awt.BorderLayout.CENTER);

        // ============ RIGHT PANEL ============
        detailsPanel.setLayout(new java.awt.BorderLayout(0, 15));
        detailsPanel.setBackground(new java.awt.Color(245, 245, 250));

        // Product details panel
        productDetailsPanel.setBackground(new java.awt.Color(255, 255, 255));
        productDetailsPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createTitledBorder(null, "Thông tin sản phẩm",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
                javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        javax.swing.GroupLayout detailsLayout = new javax.swing.GroupLayout(productDetailsPanel);
        productDetailsPanel.setLayout(detailsLayout);

        detailsLayout.setHorizontalGroup(
                detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(detailsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(productIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(productNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(priceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(stockLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(quantityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(productIdField)
                                        .addComponent(productNameField)
                                        .addComponent(priceField)
                                        .addComponent(stockField)
                                        .addGroup(detailsLayout.createSequentialGroup()
                                                .addComponent(quantitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(addToCartButton, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addContainerGap()));

        detailsLayout.setVerticalGroup(
                detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(detailsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(productIdLabel)
                                        .addComponent(productIdField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(productNameLabel)
                                        .addComponent(productNameField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(priceLabel)
                                        .addComponent(priceField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(stockLabel)
                                        .addComponent(stockField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(quantityLabel)
                                        .addComponent(quantitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addToCartButton))
                                .addContainerGap()));

        // Labels
        productIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        productIdLabel.setText("Mã SP:");
        productNameLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        productNameLabel.setText("Tên SP:");
        priceLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        priceLabel.setText("Giá:");
        stockLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        stockLabel.setText("Tồn kho:");
        quantityLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        quantityLabel.setText("Số lượng:");

        // Fields
        productIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        productIdField.setEditable(false);
        productNameField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        productNameField.setEditable(false);
        priceField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        priceField.setEditable(false);
        stockField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        stockField.setEditable(false);
        quantitySpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));

        addToCartButton.setBackground(new java.awt.Color(50, 150, 50));
        addToCartButton.setForeground(new java.awt.Color(255, 255, 255));
        addToCartButton.setText("🛒 Thêm vào giỏ");
        addToCartButton.setFocusPainted(false);
        addToCartButton.addActionListener(e -> addToCart());

        // Cart panel
        cartPanel.setBackground(new java.awt.Color(255, 255, 255));
        cartPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createTitledBorder(null, "Giỏ hàng",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
                javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        cartPanel.setLayout(new java.awt.BorderLayout(0, 10));

        String[] cartColumns = { "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền" };
        Object[][] cartData = {};
        cartTableScroll = new javax.swing.JTable(cartData, cartColumns);
        cartTableScroll.setFont(new java.awt.Font("Segoe UI", 0, 12));
        cartTableScroll.setRowHeight(25);
        cartTable.setViewportView(cartTableScroll);

        javax.swing.JPanel cartInfoPanel = new javax.swing.JPanel();
        cartInfoPanel.setLayout(new javax.swing.BoxLayout(cartInfoPanel, javax.swing.BoxLayout.Y_AXIS));
        cartInfoPanel.setBackground(new java.awt.Color(255, 255, 255));
        cartInfoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 10, 0));

        javax.swing.JPanel cardIdPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        cardIdPanel.setBackground(new java.awt.Color(255, 255, 255));
        cardIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cardIdLabel.setText("Mã thẻ:");
        cardIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cardIdField.setText(currentCardId);
        cardIdField.setEditable(false);
        cardIdPanel.add(cardIdLabel, java.awt.BorderLayout.WEST);
        cardIdPanel.add(cardIdField, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel pointsPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        pointsPanel.setBackground(new java.awt.Color(255, 255, 255));
        pointsUsedLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        pointsUsedLabel.setText("Điểm sử dụng:");
        pointsUsedField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        pointsUsedField.setText("0");
        pointsUsedField.setEditable(true);
        // Cập nhật tổng tiền khi nhấn Enter
        pointsUsedField.addActionListener(e -> updateCartTable());
        // Cập nhật tổng tiền khi rời khỏi field (focus lost)
        pointsUsedField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                updateCartTable();
            }
        });
        // Cập nhật tổng tiền khi thay đổi nội dung (với delay để tránh cập nhật quá
        // nhiều)
        javax.swing.Timer updateTimer = new javax.swing.Timer(500, e -> updateCartTable());
        updateTimer.setRepeats(false); // Chỉ chạy một lần sau khi dừng gõ
        pointsUsedField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateTimer.restart();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateTimer.restart();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateTimer.restart();
            }
        });
        pointsPanel.add(pointsUsedLabel, java.awt.BorderLayout.WEST);
        pointsPanel.add(pointsUsedField, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel saleDatePanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        saleDatePanel.setBackground(new java.awt.Color(255, 255, 255));
        saleDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        saleDateLabel.setText("Ngày bán:");
        saleDateField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        saleDateField.setText(dateFormat.format(new java.util.Date()));
        saleDateField.setEditable(false);
        saleDatePanel.add(saleDateLabel, java.awt.BorderLayout.WEST);
        saleDatePanel.add(saleDateField, java.awt.BorderLayout.CENTER);

        cartInfoPanel.add(cardIdPanel);
        cartInfoPanel.add(javax.swing.Box.createVerticalStrut(5));
        cartInfoPanel.add(pointsPanel);
        cartInfoPanel.add(javax.swing.Box.createVerticalStrut(5));
        cartInfoPanel.add(saleDatePanel);

        javax.swing.JPanel cartBottomPanel = new javax.swing.JPanel();
        cartBottomPanel.setLayout(new java.awt.BorderLayout(10, 10));
        cartBottomPanel.setBackground(new java.awt.Color(255, 255, 255));

        totalLabel.setFont(new java.awt.Font("Segoe UI", 1, 16));
        totalLabel.setText("Tổng tiền:");
        totalField.setFont(new java.awt.Font("Segoe UI", 1, 16));
        totalField.setText("0 đ");
        totalField.setEditable(false);
        totalField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        checkoutButton.setBackground(new java.awt.Color(0, 120, 215));
        checkoutButton.setForeground(new java.awt.Color(255, 255, 255));
        checkoutButton.setText("💳 Thanh toán");
        checkoutButton.setFocusPainted(false);
        checkoutButton.setPreferredSize(new java.awt.Dimension(150, 40));
        checkoutButton.addActionListener(e -> checkout());

        clearCartButton.setBackground(new java.awt.Color(200, 50, 50));
        clearCartButton.setForeground(new java.awt.Color(255, 255, 255));
        clearCartButton.setText("🗑️ Xóa giỏ");
        clearCartButton.setFocusPainted(false);
        clearCartButton.setPreferredSize(new java.awt.Dimension(120, 40));
        clearCartButton.addActionListener(e -> clearCart());

        javax.swing.JPanel totalPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        totalPanel.setBackground(new java.awt.Color(255, 255, 255));
        totalPanel.add(totalLabel, java.awt.BorderLayout.WEST);
        totalPanel.add(totalField, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.add(clearCartButton);
        buttonPanel.add(checkoutButton);

        cartBottomPanel.add(totalPanel, java.awt.BorderLayout.CENTER);
        cartBottomPanel.add(buttonPanel, java.awt.BorderLayout.EAST);

        // Tạo container cho info và bottom panel
        javax.swing.JPanel cartBottomContainer = new javax.swing.JPanel();
        cartBottomContainer.setLayout(new java.awt.BorderLayout(0, 10));
        cartBottomContainer.setBackground(new java.awt.Color(255, 255, 255));
        cartBottomContainer.add(cartInfoPanel, java.awt.BorderLayout.CENTER);
        cartBottomContainer.add(cartBottomPanel, java.awt.BorderLayout.SOUTH);

        cartPanel.add(cartTable, java.awt.BorderLayout.CENTER);
        cartPanel.add(cartBottomContainer, java.awt.BorderLayout.SOUTH);

        detailsPanel.add(productDetailsPanel, java.awt.BorderLayout.NORTH);
        detailsPanel.add(cartPanel, java.awt.BorderLayout.CENTER);

        mainContainer.add(productsPanel, java.awt.BorderLayout.WEST);
        mainContainer.add(detailsPanel, java.awt.BorderLayout.CENTER);

        add(mainContainer, java.awt.BorderLayout.CENTER);
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
            cartItems.add(new CartItem(productId, item.name, quantity, item.price));
        }

        // Nếu đây là sản phẩm đầu tiên trong giỏ hàng, tự động set điểm sử dụng = điểm
        // tích lũy
        CardService.Card card = cardService.getCardById(currentCardId);
        if (cartItems.size() == 1 && card != null && pointsUsedField != null) {
            pointsUsedField.setText(String.valueOf(card.totalPoints));
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

        // Tính tổng tiền
        double grandTotal = 0;
        for (CartItem item : cartItems) {
            grandTotal += item.getTotalPrice();
        }

        // Số tiền thực cần thanh toán qua thẻ
        double amountToPay = grandTotal - pointsUsed;
        if (amountToPay < 0)
            amountToPay = 0;

        // Điểm thưởng = 3% của số tiền thanh toán thực tế
        int pointsToAward = (int) Math.round(amountToPay * 0.03);

        // Check card balance and points via smart card
        boolean cardHasEnough = false;
        CardConnectionManager connManager = null;
        try {
            connManager = new CardConnectionManager();
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
            connManager = new CardConnectionManager();
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
}
