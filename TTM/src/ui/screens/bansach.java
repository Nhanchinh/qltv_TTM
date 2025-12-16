/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.BookService;
import services.PurchaseService;
import services.CardService;
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
import smartcard.CardKeyManager;
import smartcard.CardInfoManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author admin
 */
public class bansach extends javax.swing.JPanel {

    private BookService bookService;
    private PurchaseService purchaseService;
    private CardService cardService;
    private String currentCardId = "CARD001";
    private int maxPointsAvailable = 0;

    /**
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            // Cập nhật UI field
            if (cardIdField != null) {
                cardIdField.setText(cardId);
            }
            updateCardInfo(); // Reload thông tin thẻ với CardID mới
        }
    }

    private List<CartItem> cartItems;

    private static class CartItem {
        String bookId;
        String title;
        int quantity;
        double unitPrice;
        double discountPercent;

        CartItem(String bookId, String title, int quantity, double unitPrice, double discountPercent) {
            this.bookId = bookId;
            this.title = title;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discountPercent = discountPercent;
        }

        double getFinalPrice() {
            return unitPrice * quantity * (1 - discountPercent / 100.0);
        }
    }

    /**
     * Creates new form BuyBookPanel
     */
    public bansach() {
        bookService = new BookService();
        purchaseService = new PurchaseService();
        cardService = new CardService();
        cartItems = new ArrayList<>();
        initComponents();
        loadBooks();
        updateCardInfo();
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    /**
     * Khởi tạo các component của giao diện
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

        // Left Side: Search + Book List (60%)
        gbc.gridx = 0;
        gbc.weightx = 0.6;
        gbc.insets = new java.awt.Insets(0, 0, 0, 10);

        javax.swing.JPanel leftWrapper = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        leftWrapper.setOpaque(false);
        leftWrapper.add(createSearchPanel(), java.awt.BorderLayout.NORTH);
        leftWrapper.add(createBookListPanel(), java.awt.BorderLayout.CENTER);

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

    private javax.swing.JPanel createHeaderPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(new java.awt.Color(255, 255, 255));
        p.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30)));

        titleLabel = new javax.swing.JLabel("Mua Sách");
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 24));
        titleLabel.setForeground(new java.awt.Color(30, 41, 59));

        p.add(titleLabel, java.awt.BorderLayout.WEST);
        return p;
    }

    private javax.swing.JPanel createSearchPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(15, 0));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));

        searchLabel = new javax.swing.JLabel("Tìm kiếm:");
        searchLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        searchLabel.setForeground(new java.awt.Color(100, 116, 139));

        searchField = createStyledTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Nhập tên sách, tác giả...");

        searchButton = createModernButton("Tìm", java.awt.Color.WHITE, new java.awt.Color(59, 130, 246));
        searchButton.setPreferredSize(new java.awt.Dimension(80, 40));
        searchButton.addActionListener(e -> searchBooks());

        p.add(searchLabel, java.awt.BorderLayout.WEST);
        p.add(searchField, java.awt.BorderLayout.CENTER);
        p.add(searchButton, java.awt.BorderLayout.EAST);

        return p;
    }

    private javax.swing.JPanel createBookListPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.BorderLayout(0, 10));
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        javax.swing.JLabel header = new javax.swing.JLabel("Danh sách sách bán");
        header.setFont(new java.awt.Font("Segoe UI", 1, 16));
        header.setForeground(new java.awt.Color(30, 41, 59));
        p.add(header, java.awt.BorderLayout.NORTH);

        String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Giá", "Số lượng" };
        booksTableScroll = createStyledTable(new Object[][] {}, columns);
        booksTableScroll.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = booksTableScroll.getSelectedRow();
            if (selectedRow >= 0) {
                bookIdField.setText(booksTableScroll.getValueAt(selectedRow, 0).toString());
                bookNameField.setText(booksTableScroll.getValueAt(selectedRow, 1).toString());
                authorField.setText(booksTableScroll.getValueAt(selectedRow, 2).toString());
                publisherField.setText(booksTableScroll.getValueAt(selectedRow, 3).toString());
                priceField.setText(booksTableScroll.getValueAt(selectedRow, 4).toString());
            }
        });

        booksTable = new javax.swing.JScrollPane(booksTableScroll);
        booksTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 245, 249)));
        booksTable.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        p.add(booksTable, java.awt.BorderLayout.CENTER);

        return p;
    }

    private javax.swing.JPanel createDetailsPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.GridBagLayout());
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Compact padding

        javax.swing.JLabel header = new javax.swing.JLabel("Thông tin sách");
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
        addDetailRow(p, gbc, 1, "Mã sách:", bookIdLabel = createLabel(""), bookIdField = createStyledTextField());
        addDetailRow(p, gbc, 2, "Tên sách:", bookNameLabel = createLabel(""), bookNameField = createStyledTextField());
        addDetailRow(p, gbc, 3, "Tác giả:", authorLabel = createLabel(""), authorField = createStyledTextField());
        addDetailRow(p, gbc, 4, "NXB:", publisherLabel = createLabel(""), publisherField = createStyledTextField());
        addDetailRow(p, gbc, 5, "Giá bán:", priceLabel = createLabel(""), priceField = createStyledTextField());

        // Read-only
        bookIdField.setEditable(false);
        bookNameField.setEditable(false);
        authorField.setEditable(false);
        publisherField.setEditable(false);
        priceField.setEditable(false);

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
        quantitySpinner.setPreferredSize(new java.awt.Dimension(80, 36));

        addToCartButton = createModernButton("Thêm vào giỏ", java.awt.Color.WHITE, new java.awt.Color(22, 163, 74));
        addToCartButton.setPreferredSize(new java.awt.Dimension(120, 36));
        addToCartButton.addActionListener(e -> addToCart());

        actionPanel.add(quantitySpinner, java.awt.BorderLayout.WEST);
        actionPanel.add(addToCartButton, java.awt.BorderLayout.CENTER);

        p.add(actionPanel, gbc);

        return p;
    }

    // Helper to add row more compactly
    private void addDetailRow(javax.swing.JPanel p, java.awt.GridBagConstraints gbc, int row, String label,
            javax.swing.JLabel lblComp, javax.swing.JTextField txtComp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        lblComp.setText(label);
        lblComp.setFont(new java.awt.Font("Segoe UI", 1, 13)); // Smaller font for label
        p.add(lblComp, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtComp.setPreferredSize(new java.awt.Dimension(0, 34)); // Fixed compact height
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
        // We will layout total label properly at bottom, just title here
        header.add(title, java.awt.BorderLayout.WEST);
        p.add(header, java.awt.BorderLayout.NORTH);

        // Table
        String[] cartColumns = { "Mã sách", "Tên sách", "Số lượng", "Đơn giá", "Giảm giá", "Thành tiền" };
        cartTableScroll = createStyledTable(new Object[][] {}, cartColumns);
        cartTable = new javax.swing.JScrollPane(cartTableScroll);
        cartTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 245, 249)));
        cartTable.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        // Limit height REMOVED to allow expansion
        p.add(cartTable, java.awt.BorderLayout.CENTER);

        // Bottom Info
        javax.swing.JPanel botPanel = new javax.swing.JPanel();
        botPanel.setLayout(new javax.swing.BoxLayout(botPanel, javax.swing.BoxLayout.Y_AXIS));
        botPanel.setOpaque(false);

        // Info Grid
        javax.swing.JPanel infoGrid = new javax.swing.JPanel(new java.awt.GridLayout(2, 2, 10, 10));
        infoGrid.setOpaque(false);

        infoGrid.add(createCompactInfoPanel("Mã thẻ:", cardIdField = createStyledTextField()));
        cardIdField.setText(currentCardId);
        cardIdField.setEditable(false);

        infoGrid.add(createCompactInfoPanel("Giảm giá:", discountField = createStyledTextField()));
        discountField.setEditable(false);
        discountField.setText("0");

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

        pointsEarnedField = createStyledTextField();
        pointsEarnedField.setText("0");
        pointsEarnedField.setEditable(false);
        infoGrid.add(createCompactInfoPanel("Điểm tích:", pointsEarnedField));

        botPanel.add(infoGrid);
        botPanel.add(javax.swing.Box.createVerticalStrut(15));

        // Total and Actions
        javax.swing.JPanel totalActionPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        totalActionPanel.setOpaque(false);

        totalField = new javax.swing.JTextField("0 đ");
        totalField.setFont(new java.awt.Font("Segoe UI", 1, 20));
        totalField.setForeground(new java.awt.Color(22, 163, 74));
        totalField.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        totalField.setBorder(null);
        totalField.setOpaque(false);
        totalField.setEditable(false);

        totalActionPanel.add(totalField, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel btnPanel = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        clearCartButton = createModernButton("Xóa", java.awt.Color.WHITE, new java.awt.Color(239, 68, 68));
        clearCartButton.setPreferredSize(new java.awt.Dimension(80, 40));
        clearCartButton.addActionListener(e -> clearCart());

        checkoutButton = createModernButton("Thanh toán", java.awt.Color.WHITE, new java.awt.Color(59, 130, 246));
        checkoutButton.setPreferredSize(new java.awt.Dimension(120, 40));
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
        lbl.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lbl.setForeground(new java.awt.Color(100, 116, 139));
        p.add(lbl, java.awt.BorderLayout.NORTH);
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

    private void loadBooks() {
        List<BookService.Book> books = bookService.getAllBooks();
        String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Giá", "Số lượng" };
        Object[][] data = new Object[books.size()][6];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        for (int i = 0; i < books.size(); i++) {
            BookService.Book book = books.get(i);
            data[i][0] = book.bookId;
            data[i][1] = book.title;
            data[i][2] = book.author;
            data[i][3] = book.publisher;
            data[i][4] = nf.format(book.price) + " đ";
            data[i][5] = book.stock;
        }
        booksTableScroll.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    private void updateCardInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);
        if (card != null) {
            // Calculate discount based on member type
            // New mapping: Normal=0, Silver=3, Gold=5, Diamond=10
            // Hỗ trợ cả tên tiếng Việt và tiếng Anh
            double discount = 0;
            String memberType = card.memberType;
            if (memberType != null) {
                if (memberType.equalsIgnoreCase("Normal") || memberType.equalsIgnoreCase("ThanhVien")) {
                    discount = 0;
                } else if (memberType.equalsIgnoreCase("Silver") || memberType.equalsIgnoreCase("Bac")) {
                    discount = 3;
                } else if (memberType.equalsIgnoreCase("Gold") || memberType.equalsIgnoreCase("Vang")) {
                    discount = 5;
                } else if (memberType.equalsIgnoreCase("Diamond") || memberType.equalsIgnoreCase("KimCuong")) {
                    discount = 10;
                }
            }
            discountField.setText(String.format("%.0f%%", discount));
            pointsEarnedField.setText("0");
        }

        // Lấy điểm từ thẻ (Smart Card)
        int currentPoints = 0;
        try {
            CardConnectionManager connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                if (info.success) {
                    currentPoints = info.points;
                    System.out.println("[UPDATE_INFO] Points from card: " + currentPoints);
                }
                connManager.disconnectCard();
            } else {
                // Fallback nếu không kết nối được thẻ
                System.out.println("[UPDATE_INFO] Cannot connect card, fallback to DB if available.");
                if (card != null)
                    currentPoints = card.totalPoints;
            }
        } catch (Exception e) {
            System.err.println("[UPDATE_INFO] Error reading card: " + e.getMessage());
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
        String[] columns = { "Mã sách", "Tên sách", "Số lượng", "Đơn giá", "Giảm giá (%)", "Thành tiền" };
        Object[][] data = new Object[cartItems.size()][6];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        double total = 0;
        int totalPoints = 0;

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            data[i][0] = item.bookId;
            data[i][1] = item.title;
            data[i][2] = item.quantity;
            data[i][3] = nf.format(item.unitPrice) + " đ";
            data[i][4] = String.format("%.0f%%", item.discountPercent);
            double finalPrice = item.getFinalPrice();
            data[i][5] = nf.format(finalPrice) + " đ";
            total += finalPrice;
            // Tính điểm tích lũy: 3% số tiền giao dịch
            totalPoints += (int) (finalPrice * 0.03);
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
                        pointsUsed = maxPointsAvailable; // Giới hạn bằng điểm tích lũy
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
        pointsEarnedField.setText(String.valueOf(totalPoints));
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadBooks();
            return;
        }
        List<BookService.Book> allBooks = bookService.getAllBooks();
        List<BookService.Book> filtered = new ArrayList<>();
        for (BookService.Book book : allBooks) {
            if (book.bookId.toLowerCase().contains(keyword) ||
                    book.title.toLowerCase().contains(keyword) ||
                    book.author.toLowerCase().contains(keyword)) {
                filtered.add(book);
            }
        }
        String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Giá", "Số lượng" };
        Object[][] data = new Object[filtered.size()][6];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        for (int i = 0; i < filtered.size(); i++) {
            BookService.Book book = filtered.get(i);
            data[i][0] = book.bookId;
            data[i][1] = book.title;
            data[i][2] = book.author;
            data[i][3] = book.publisher;
            data[i][4] = nf.format(book.price) + " đ";
            data[i][5] = book.stock;
        }
        booksTableScroll.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    private void addToCart() {
        String bookId = bookIdField.getText().trim();
        if (bookId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookService.Book book = bookService.getBookById(bookId);
        if (book == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy sách!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        if (book.stock < quantity) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không đủ số lượng sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get discount from member type (use new mapping)
        // Hỗ trợ cả tên tiếng Việt và tiếng Anh
        CardService.Card card = cardService.getCardById(currentCardId);
        double discount = 0;
        if (card != null && card.memberType != null) {
            String memberType = card.memberType;
            if (memberType.equalsIgnoreCase("Normal") || memberType.equalsIgnoreCase("ThanhVien")) {
                discount = 0;
            } else if (memberType.equalsIgnoreCase("Silver") || memberType.equalsIgnoreCase("Bac")) {
                discount = 3;
            } else if (memberType.equalsIgnoreCase("Gold") || memberType.equalsIgnoreCase("Vang")) {
                discount = 5;
            } else if (memberType.equalsIgnoreCase("Diamond") || memberType.equalsIgnoreCase("KimCuong")) {
                discount = 10;
            }
        }

        cartItems.add(new CartItem(bookId, book.title, quantity, book.price, discount));

        // Nếu đây là sách đầu tiên trong giỏ hàng, tự động set điểm sử dụng = điểm tích
        // lũy
        if (cartItems.size() == 1 && card != null && pointsUsedField != null) {
            pointsUsedField.setText(String.valueOf(card.totalPoints));
        }

        updateCartTable();
        javax.swing.JOptionPane.showMessageDialog(this, "Đã thêm " + quantity + " sách vào giỏ hàng!", "Thông báo",
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

        // Recompute final total and points to award
        double grandTotal = 0;
        for (CartItem item : cartItems) {
            grandTotal += item.getFinalPrice();
        }

        // Determine rankPercent from DB memberType (dùng cho giảm giá, không phải điểm
        // thưởng)
        CardService.Card dbCard = cardService.getCardById(currentCardId);

        // Điểm thưởng = 3% của số tiền thanh toán thực tế (sau khi trừ điểm đã dùng)
        // Áp dụng cho TẤT CẢ hạng thành viên
        double amountAfterPoints = grandTotal - pointsUsed;
        if (amountAfterPoints < 0)
            amountAfterPoints = 0;
        int pointsToAward = (int) Math.round(amountAfterPoints * 0.03);

        // Số tiền thực cần thanh toán qua thẻ
        double amountToPay = grandTotal - pointsUsed;
        if (amountToPay < 0)
            amountToPay = 0;

        // Check card balance and points via smart card
        boolean cardHasEnough = false;
        CardConnectionManager connManager = null;
        try {
            connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                if (info.success) {
                    System.out.println("[CHECKOUT] Card balance: " + info.balance + " VND, Points: " + info.points);

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
            System.err.println("[CHECKOUT] Error checking card balance: " + ex.getMessage());
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

        // 1) Decrement DB and record purchases
        boolean dbSuccess = true;
        for (CartItem item : cartItems) {
            if (!purchaseService.purchaseBook(currentCardId, item.bookId, item.quantity, item.discountPercent)) {
                dbSuccess = false;
                break;
            }
        }

        if (!dbSuccess) {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật cơ sở dữ liệu (stock/history).", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Charge the card & Use Points
        boolean transactionSuccess = false;
        try {
            connManager = new CardConnectionManager(); // New connection
            if (!connManager.connectCard()) {
                throw new Exception("Không thể kết nối lại thẻ để thanh toán.");
            }
            CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

            // Xử lý trừ điểm trước (nếu có)
            boolean pointsDeducted = true;
            if (pointsUsed > 0) {
                pointsDeducted = balanceManager.usePoints(pointsUsed);
                if (!pointsDeducted) {
                    System.err.println("[CHECKOUT] Failed to deduct points from card.");
                } else {
                    System.out.println("[CHECKOUT] Deducted " + pointsUsed + " points from card.");
                }
            }

            // Nếu trừ điểm thành công (hoặc không dùng điểm), mới trừ tiền
            if (pointsDeducted) {
                boolean paymentOk = balanceManager.payment((int) amountToPay);
                if (paymentOk) {
                    System.out.println("[CHECKOUT] Card charged: " + (int) amountToPay + " VND");
                    transactionSuccess = true;
                    // 3) Add points to card
                    if (pointsToAward > 0) {
                        boolean ptsOk = balanceManager.addPoints(pointsToAward);
                        System.out.println("[CHECKOUT] Added points to card: " + pointsToAward + " => " + ptsOk);
                    }
                } else {
                    System.err.println("[CHECKOUT] Card payment failed.");
                    // Nếu đã trừ điểm mà thanh toán fail, có thể cần hoàn lại điểm.
                    // Trong phạm vi bài này, ta ghi log lỗi.
                }
            }
        } catch (Exception ex) {
            System.err.println("[CHECKOUT] Exception during payment: " + ex.getMessage());
        } finally {
            try {
                if (connManager != null)
                    connManager.disconnectCard();
            } catch (Exception ignored) {
            }
        }

        if (!transactionSuccess) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Giao dịch thẻ thất bại (trừ tiền/điểm) nhưng dữ liệu DB đã được cập nhật. Vui lòng liên hệ admin.",
                    "Lỗi nghiêm trọng", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Deduct used points in DB for sync
        if (pointsUsed > 0) {
            cardService.usePoints(currentCardId, pointsUsed);
        }

        javax.swing.JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thông báo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        cartItems.clear();
        pointsUsedField.setText("0");
        updateCartTable();
        loadBooks();
        updateCardInfo();
    }

    private void clearCart() {
        int option = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa toàn bộ giỏ hàng?", "Xác nhận",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (option == javax.swing.JOptionPane.YES_OPTION) {
            cartItems.clear();
            updateCartTable();
        }
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton searchButton;
    private javax.swing.JScrollPane booksTable;
    private javax.swing.JTable booksTableScroll;
    private javax.swing.JLabel bookDetailsTitle; // Keeping just in case, though might be unused
    private javax.swing.JLabel bookIdLabel;
    private javax.swing.JTextField bookIdField;
    private javax.swing.JLabel bookNameLabel;
    private javax.swing.JTextField bookNameField;
    private javax.swing.JLabel authorLabel;
    private javax.swing.JTextField authorField;
    private javax.swing.JLabel publisherLabel;
    private javax.swing.JTextField publisherField;
    private javax.swing.JLabel priceLabel;
    private javax.swing.JTextField priceField;
    private javax.swing.JLabel quantityLabel;
    private javax.swing.JSpinner quantitySpinner;
    private javax.swing.JButton addToCartButton;
    private javax.swing.JLabel cartTitle;
    private javax.swing.JScrollPane cartTable;
    private javax.swing.JTable cartTableScroll;
    private javax.swing.JLabel cardIdLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JLabel discountLabel;
    private javax.swing.JTextField discountField;
    private javax.swing.JLabel pointsUsedLabel;
    private javax.swing.JTextField pointsUsedField;
    private javax.swing.JLabel pointsEarnedLabel;
    private javax.swing.JTextField pointsEarnedField;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JTextField totalField;
    private javax.swing.JButton checkoutButton;
    private javax.swing.JButton clearCartButton;
}
