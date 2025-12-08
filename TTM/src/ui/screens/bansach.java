/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.BookService;
import services.PurchaseService;
import services.CardService;
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
    @SuppressWarnings("unchecked")
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        
        // Main container
        mainContainer = new javax.swing.JPanel();
        
        // Left panel - Danh sách sách
        leftPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        booksTable = new javax.swing.JScrollPane();
        booksTableScroll = new javax.swing.JTable();
        
        // Right panel - Thông tin chi tiết và giỏ hàng
        rightPanel = new javax.swing.JPanel();
        
        // Book details panel
        bookDetailsPanel = new javax.swing.JPanel();
        bookDetailsTitle = new javax.swing.JLabel();
        bookIdLabel = new javax.swing.JLabel();
        bookIdField = new javax.swing.JTextField();
        bookNameLabel = new javax.swing.JLabel();
        bookNameField = new javax.swing.JTextField();
        authorLabel = new javax.swing.JLabel();
        authorField = new javax.swing.JTextField();
        publisherLabel = new javax.swing.JLabel();
        publisherField = new javax.swing.JTextField();
        priceLabel = new javax.swing.JLabel();
        priceField = new javax.swing.JTextField();
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
        discountLabel = new javax.swing.JLabel();
        discountField = new javax.swing.JTextField();
        pointsUsedLabel = new javax.swing.JLabel();
        pointsUsedField = new javax.swing.JTextField();
        pointsEarnedLabel = new javax.swing.JLabel();
        pointsEarnedField = new javax.swing.JTextField();
        totalLabel = new javax.swing.JLabel();
        totalField = new javax.swing.JTextField();
        checkoutButton = new javax.swing.JButton();
        clearCartButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 20));

        // Title
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Mua sách");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        mainContainer.setLayout(new java.awt.BorderLayout(20, 0));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 30, 40));
        mainContainer.setBackground(new java.awt.Color(245, 245, 250));

        // ============ LEFT PANEL - DANH SÁCH SÁCH ============
        leftPanel.setBackground(new java.awt.Color(255, 255, 255));
        leftPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Danh sách sách bán",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)));
        leftPanel.setLayout(new java.awt.BorderLayout(0, 10));
        leftPanel.setPreferredSize(new java.awt.Dimension(600, 0));
        leftPanel.setMinimumSize(new java.awt.Dimension(500, 0));
        leftPanel.setMaximumSize(new java.awt.Dimension(700, Integer.MAX_VALUE));

        // Search panel
        javax.swing.JPanel searchPanel = new javax.swing.JPanel();
        searchPanel.setLayout(new java.awt.BorderLayout(10, 0));
        searchPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        searchPanel.setBackground(new java.awt.Color(255, 255, 255));

        searchLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        searchLabel.setText("Tìm kiếm:");
        searchField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        searchField.setColumns(20);
        searchButton.setBackground(new java.awt.Color(0, 120, 215));
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("🔍 Tìm");
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchBooks());

        searchPanel.add(searchLabel, java.awt.BorderLayout.WEST);
        searchPanel.add(searchField, java.awt.BorderLayout.CENTER);
        searchPanel.add(searchButton, java.awt.BorderLayout.EAST);

        // Books table - sẽ được load từ database
        String[] columns = {"Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Giá", "Số lượng"};
        Object[][] data = {};
        booksTableScroll = new javax.swing.JTable(data, columns);
        booksTableScroll.setFont(new java.awt.Font("Segoe UI", 0, 12));
        booksTableScroll.setRowHeight(25);
        booksTableScroll.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
        booksTable.setViewportView(booksTableScroll);

        leftPanel.add(searchPanel, java.awt.BorderLayout.NORTH);
        leftPanel.add(booksTable, java.awt.BorderLayout.CENTER);

        // ============ RIGHT PANEL ============
        // Sử dụng BoxLayout để phân bổ không gian tốt hơn
        rightPanel.setLayout(new javax.swing.BoxLayout(rightPanel, javax.swing.BoxLayout.Y_AXIS));
        rightPanel.setBackground(new java.awt.Color(245, 245, 250));

        // Book details panel
        bookDetailsPanel.setBackground(new java.awt.Color(255, 255, 255));
        bookDetailsPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Thông tin sách",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        javax.swing.GroupLayout detailsLayout = new javax.swing.GroupLayout(bookDetailsPanel);
        bookDetailsPanel.setLayout(detailsLayout);
        
        detailsLayout.setHorizontalGroup(
            detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bookIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bookNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(publisherLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(priceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(quantityLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bookIdField)
                    .addComponent(bookNameField)
                    .addComponent(authorField)
                    .addComponent(publisherField)
                    .addComponent(priceField)
                    .addGroup(detailsLayout.createSequentialGroup()
                        .addComponent(quantitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addToCartButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        
        detailsLayout.setVerticalGroup(
            detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bookIdLabel)
                    .addComponent(bookIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bookNameLabel)
                    .addComponent(bookNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authorLabel)
                    .addComponent(authorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(publisherLabel)
                    .addComponent(publisherField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(priceLabel)
                    .addComponent(priceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quantityLabel)
                    .addComponent(quantitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addToCartButton))
                .addContainerGap())
        );

        // Labels setup
        bookIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        bookIdLabel.setText("Mã sách:");
        bookNameLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        bookNameLabel.setText("Tên sách:");
        authorLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        authorLabel.setText("Tác giả:");
        publisherLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        publisherLabel.setText("Nhà xuất bản:");
        priceLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        priceLabel.setText("Giá bán:");
        quantityLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        quantityLabel.setText("Số lượng:");

        // Fields setup
        bookIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        bookIdField.setEditable(false);
        bookNameField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        bookNameField.setEditable(false);
        authorField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        authorField.setEditable(false);
        publisherField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        publisherField.setEditable(false);
        priceField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        priceField.setEditable(false);
        quantitySpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 100, 1));

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

        String[] cartColumns = {"Mã sách", "Tên sách", "Số lượng", "Đơn giá", "Giảm giá (%)", "Thành tiền"};
        Object[][] cartData = {};
        cartTableScroll = new javax.swing.JTable(cartData, cartColumns);
        cartTableScroll.setFont(new java.awt.Font("Segoe UI", 0, 12));
        cartTableScroll.setRowHeight(25);
        cartTable.setViewportView(cartTableScroll);

        // Sử dụng GridLayout 2x2 để tiết kiệm diện tích
        javax.swing.JPanel cartInfoPanel = new javax.swing.JPanel();
        cartInfoPanel.setLayout(new java.awt.GridLayout(2, 2, 10, 10));
        cartInfoPanel.setBackground(new java.awt.Color(255, 255, 255));
        cartInfoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Cột 1 - Hàng 1: Mã thẻ
        javax.swing.JPanel cardIdPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        cardIdPanel.setBackground(new java.awt.Color(255, 255, 255));
        cardIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cardIdLabel.setText("Mã thẻ:");
        cardIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cardIdField.setText(currentCardId);
        cardIdField.setEditable(false);
        cardIdPanel.add(cardIdLabel, java.awt.BorderLayout.WEST);
        cardIdPanel.add(cardIdField, java.awt.BorderLayout.CENTER);
        
        // Cột 2 - Hàng 1: Giảm giá
        javax.swing.JPanel discountPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        discountPanel.setBackground(new java.awt.Color(255, 255, 255));
        discountLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        discountLabel.setText("Giảm giá (%):");
        discountField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        discountField.setText("0");
        discountField.setEditable(false);
        discountPanel.add(discountLabel, java.awt.BorderLayout.WEST);
        discountPanel.add(discountField, java.awt.BorderLayout.CENTER);
        
        // Cột 1 - Hàng 2: Điểm sử dụng
        javax.swing.JPanel pointsUsedPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        pointsUsedPanel.setBackground(new java.awt.Color(255, 255, 255));
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
        // Cập nhật tổng tiền khi thay đổi nội dung (với delay để tránh cập nhật quá nhiều)
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
        pointsUsedPanel.add(pointsUsedLabel, java.awt.BorderLayout.WEST);
        pointsUsedPanel.add(pointsUsedField, java.awt.BorderLayout.CENTER);
        
        // Cột 2 - Hàng 2: Điểm tích lũy
        javax.swing.JPanel pointsPanel = new javax.swing.JPanel(new java.awt.BorderLayout(10, 0));
        pointsPanel.setBackground(new java.awt.Color(255, 255, 255));
        pointsEarnedLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        pointsEarnedLabel.setText("Điểm tích lũy sẽ được cộng:");
        pointsEarnedField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        pointsEarnedField.setText("0");
        pointsEarnedField.setEditable(false);
        pointsPanel.add(pointsEarnedLabel, java.awt.BorderLayout.WEST);
        pointsPanel.add(pointsEarnedField, java.awt.BorderLayout.CENTER);
        
        // Thêm vào GridLayout theo thứ tự: hàng 1 (cột 1, cột 2), hàng 2 (cột 1, cột 2)
        cartInfoPanel.add(cardIdPanel);
        cartInfoPanel.add(discountPanel);
        cartInfoPanel.add(pointsUsedPanel);
        cartInfoPanel.add(pointsPanel);

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

        // Không cần scroll pane nữa vì đã dùng GridLayout 2x2 tiết kiệm diện tích
        // cartInfoPanel giờ chỉ cần chiều cao khoảng 100px cho 2 hàng
        
        // Tạo container cho info và bottom panel
        javax.swing.JPanel cartBottomContainer = new javax.swing.JPanel();
        cartBottomContainer.setLayout(new java.awt.BorderLayout(0, 10));
        cartBottomContainer.setBackground(new java.awt.Color(255, 255, 255));
        cartBottomContainer.add(cartInfoPanel, java.awt.BorderLayout.CENTER);
        cartBottomContainer.add(cartBottomPanel, java.awt.BorderLayout.SOUTH);

        // Giới hạn chiều cao của cartTable để có nhiều không gian cho phần dưới
        cartTable.setPreferredSize(new java.awt.Dimension(0, 150));
        
        cartPanel.add(cartTable, java.awt.BorderLayout.CENTER);
        cartPanel.add(cartBottomContainer, java.awt.BorderLayout.SOUTH);

        // Đặt chiều cao tối thiểu cho bookDetailsPanel để hiển thị đầy đủ nội dung
        // Tính toán: 6 dòng thông tin + padding + border ≈ 250-280px
        bookDetailsPanel.setPreferredSize(new java.awt.Dimension(0, 280));
        bookDetailsPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 300));
        bookDetailsPanel.setMinimumSize(new java.awt.Dimension(0, 250));
        
        // Giới hạn chiều cao của cartTable để phần thông tin giỏ hàng có thể hiển thị
        cartTable.setPreferredSize(new java.awt.Dimension(0, 150));
        cartTable.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 200));
        
        // Đảm bảo cartPanel có thể mở rộng để chiếm không gian còn lại
        cartPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        cartPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        rightPanel.add(bookDetailsPanel);
        rightPanel.add(javax.swing.Box.createVerticalStrut(15));
        rightPanel.add(cartPanel);

        mainContainer.add(leftPanel, java.awt.BorderLayout.WEST);
        mainContainer.add(rightPanel, java.awt.BorderLayout.CENTER);

        add(mainContainer, java.awt.BorderLayout.CENTER);
    }

    private void loadBooks() {
        List<BookService.Book> books = bookService.getAllBooks();
        String[] columns = {"Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Giá", "Số lượng"};
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
            double discount = 0;
            if (card.memberType.equals("Basic")) discount = 5;
            else if (card.memberType.equals("Premium")) discount = 10;
            else if (card.memberType.equals("VIP")) discount = 15;
            discountField.setText(String.format("%.0f%%", discount));
            pointsEarnedField.setText("0");
            
            // Tự động set điểm sử dụng = điểm tích lũy hiện có
            if (pointsUsedField != null) {
                pointsUsedField.setText(String.valueOf(card.totalPoints));
            }
        }
    }
    
    private void updateCartTable() {
        String[] columns = {"Mã sách", "Tên sách", "Số lượng", "Đơn giá", "Giảm giá (%)", "Thành tiền"};
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
            totalPoints += (int)(finalPrice * 0.03);
        }
        
        // Lấy số điểm sử dụng từ field (nếu có)
        int pointsUsed = 0;
        if (pointsUsedField != null) {
            try {
                String pointsText = pointsUsedField.getText().trim();
                if (!pointsText.isEmpty()) {
                    pointsUsed = Integer.parseInt(pointsText);
                    if (pointsUsed < 0) pointsUsed = 0;
                    
                    // Kiểm tra không được vượt quá điểm tích lũy hiện có
                    CardService.Card card = cardService.getCardById(currentCardId);
                    if (card != null && pointsUsed > card.totalPoints) {
                        pointsUsed = card.totalPoints; // Giới hạn bằng điểm tích lũy
                        pointsUsedField.setText(String.valueOf(pointsUsed));
                    }
                }
            } catch (NumberFormatException e) {
                pointsUsed = 0;
            }
        }
        
        // Trừ điểm vào tổng tiền (1 điểm = 1 VND)
        double finalTotal = total - pointsUsed;
        if (finalTotal < 0) finalTotal = 0;
        
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
        String[] columns = {"Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Giá", "Số lượng"};
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
        
        // Get discount from member type
        CardService.Card card = cardService.getCardById(currentCardId);
        double discount = 0;
        if (card != null) {
            if (card.memberType.equals("Basic")) discount = 5;
            else if (card.memberType.equals("Premium")) discount = 10;
            else if (card.memberType.equals("VIP")) discount = 15;
        }
        
        cartItems.add(new CartItem(bookId, book.title, quantity, book.price, discount));
        
        // Nếu đây là sách đầu tiên trong giỏ hàng, tự động set điểm sử dụng = điểm tích lũy
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
                if (pointsUsed < 0) pointsUsed = 0;
            }
        } catch (NumberFormatException e) {
            pointsUsed = 0;
        }
        
        // Kiểm tra đủ điểm không
        if (pointsUsed > 0) {
            CardService.Card card = cardService.getCardById(currentCardId);
            if (card == null || pointsUsed > card.totalPoints) {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "Không đủ điểm! Bạn có " + (card != null ? card.totalPoints : 0) + " điểm.", 
                    "Thông báo", 
                    javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        int option = javax.swing.JOptionPane.showConfirmDialog(this, 
            "Xác nhận thanh toán?", "Xác nhận",
            javax.swing.JOptionPane.YES_NO_OPTION);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        
        boolean success = true;
        for (CartItem item : cartItems) {
            CardService.Card card = cardService.getCardById(currentCardId);
            double discount = 0;
            if (card != null) {
                if (card.memberType.equals("Basic")) discount = 5;
                else if (card.memberType.equals("Premium")) discount = 10;
                else if (card.memberType.equals("VIP")) discount = 15;
            }
            if (!purchaseService.purchaseBook(currentCardId, item.bookId, item.quantity, discount)) {
                success = false;
                break;
            }
        }
        
        // Trừ điểm sau khi mua sách thành công
        if (success && pointsUsed > 0) {
            cardService.usePoints(currentCardId, pointsUsed);
        }
        
        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this, "Thanh toán thành công!", "Thông báo", 
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
            cartItems.clear();
            pointsUsedField.setText("0");
            updateCartTable();
            loadBooks();
            updateCardInfo();
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi thanh toán!", "Lỗi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
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
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton searchButton;
    private javax.swing.JScrollPane booksTable;
    private javax.swing.JTable booksTableScroll;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel bookDetailsPanel;
    private javax.swing.JLabel bookDetailsTitle;
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
    private javax.swing.JPanel cartPanel;
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
