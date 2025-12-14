/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.BookService;
import services.BorrowService;
import services.CardService;
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
import ui.DBConnect;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Panel Mượn/Trả sách với tích hợp Smart Card
 * Hỗ trợ mượn nhiều sách cùng lúc
 * 
 * @author admin
 */
public class muontra extends javax.swing.JPanel {

    private BookService bookService;
    private BorrowService borrowService;
    private CardService cardService;
    private String currentCardId = "CARD001";

    // Giỏ mượn sách (để mượn nhiều quyển cùng lúc)
    private List<BorrowCartItem> borrowCart = new ArrayList<>();

    // Hằng số theo hạng thành viên
    private static final int MAX_BOOKS_NORMAL = 3;
    private static final int MAX_BOOKS_SILVER = 5;
    private static final int MAX_BOOKS_GOLD = 10;
    private static final int MAX_BOOKS_DIAMOND = 15;

    private static final int FREE_BORROWS_NORMAL = 1;
    private static final int FREE_BORROWS_SILVER = 3;
    private static final int FREE_BORROWS_GOLD = 5;
    private static final int FREE_BORROWS_DIAMOND = 10;

    // Phí thuê và phạt
    private static final int RENTAL_FEE_PER_DAY = 1000; // 1k/ngày
    private static final int LATE_FEE_PER_DAY = 5000; // 5k/ngày trễ
    private static final int FREE_DURATION_DAYS = 14; // 14 ngày đầu miễn phí

    // Biến lưu thông tin thành viên
    private String currentMemberType = "Normal";
    private int maxBooksAllowed = MAX_BOOKS_NORMAL;
    private int freeBorrowsPerMonth = FREE_BORROWS_NORMAL;
    private int currentBorrowedCount = 0;
    private int usedFreeBorrowsThisMonth = 0;

    // Class đại diện cho item trong giỏ mượn
    private static class BorrowCartItem {
        String bookId;
        String title;
        double price;
        int days;
        boolean useFreeSlot;

        BorrowCartItem(String bookId, String title, double price, int days, boolean useFreeSlot) {
            this.bookId = bookId;
            this.title = title;
            this.price = price;
            this.days = days;
            this.useFreeSlot = useFreeSlot;
        }

        int getRentalFee() {
            if (useFreeSlot || days <= FREE_DURATION_DAYS) {
                return 0;
            }
            return (days - FREE_DURATION_DAYS) * RENTAL_FEE_PER_DAY;
        }

        int getTotalCost() {
            return (int) price + getRentalFee();
        }
    }

    /**
     * Creates new form BorrowPanel
     */
    public muontra() {
        bookService = new BookService();
        borrowService = new BorrowService();
        cardService = new CardService();
        initComponents();
        loadMemberInfo();
        loadAvailableBooks();
        loadBorrowedBooks();
    }

    /**
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            if (cardIdField != null) {
                cardIdField.setText(cardId);
            }
            loadMemberInfo();
            loadAvailableBooks();
            loadBorrowedBooks();
        }
    }

    /**
     * Load thông tin thành viên và đếm lượt free đã dùng trong tháng từ DB
     */
    private void loadMemberInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);
        if (card != null && card.memberType != null) {
            String memberType = card.memberType;

            // Xác định hạng và giới hạn
            if (memberType.equalsIgnoreCase("Normal") || memberType.equalsIgnoreCase("ThanhVien")) {
                currentMemberType = "Normal";
                maxBooksAllowed = MAX_BOOKS_NORMAL;
                freeBorrowsPerMonth = FREE_BORROWS_NORMAL;
            } else if (memberType.equalsIgnoreCase("Silver") || memberType.equalsIgnoreCase("Bac")) {
                currentMemberType = "Silver";
                maxBooksAllowed = MAX_BOOKS_SILVER;
                freeBorrowsPerMonth = FREE_BORROWS_SILVER;
            } else if (memberType.equalsIgnoreCase("Gold") || memberType.equalsIgnoreCase("Vang")) {
                currentMemberType = "Gold";
                maxBooksAllowed = MAX_BOOKS_GOLD;
                freeBorrowsPerMonth = FREE_BORROWS_GOLD;
            } else if (memberType.equalsIgnoreCase("Diamond") || memberType.equalsIgnoreCase("KimCuong")) {
                currentMemberType = "Diamond";
                maxBooksAllowed = MAX_BOOKS_DIAMOND;
                freeBorrowsPerMonth = FREE_BORROWS_DIAMOND;
            }
        }

        // Đếm số sách đang mượn từ DB (Status = 'mượn')
        currentBorrowedCount = countBorrowedBooksFromDB();

        // Đếm lượt free đã dùng trong tháng hiện tại từ DB
        usedFreeBorrowsThisMonth = countFreeBorrowsThisMonth();

        // Cập nhật UI hiển thị thông tin
        if (memberInfoLabel != null) {
            memberInfoLabel.setText(String.format("Hạng: %s | Đang mượn: %d/%d | Lượt free: %d/%d",
                    currentMemberType, currentBorrowedCount, maxBooksAllowed,
                    Math.max(0, freeBorrowsPerMonth - usedFreeBorrowsThisMonth), freeBorrowsPerMonth));
        }
    }

    /**
     * Đếm số sách đang mượn từ DB
     */
    private int countBorrowedBooksFromDB() {
        String sql = "SELECT COUNT(*) FROM BorrowHistory WHERE CardID = ? AND Status = 'mượn'";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentCardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MUONTRA] Error counting borrowed books: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Đếm số lượt mượn free đã sử dụng trong tháng hiện tại
     * Dùng SQLite syntax (julianday thay vì DATEDIFF)
     */
    private int countFreeBorrowsThisMonth() {
        // Lấy ngày đầu và cuối tháng hiện tại
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String startOfMonth = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        cal.add(Calendar.MONTH, 1);
        String startOfNextMonth = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

        // SQLite: dùng julianday() để tính số ngày giữa 2 ngày
        // Hoặc đơn giản hơn: đếm các bản ghi có thời gian mượn <= 14 ngày
        String sql = "SELECT COUNT(*) FROM BorrowHistory WHERE CardID = ? " +
                "AND BorrowDate >= ? AND BorrowDate < ? " +
                "AND (julianday(DueDate) - julianday(BorrowDate)) <= 14";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentCardId);
            pstmt.setString(2, startOfMonth);
            pstmt.setString(3, startOfNextMonth);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            // Nếu vẫn lỗi, fallback về 0
            System.err.println("[MUONTRA] Error counting free borrows: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Khởi tạo các component của giao diện
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        memberInfoLabel = new javax.swing.JLabel();

        // TabbedPane
        tabbedPane = new javax.swing.JTabbedPane();

        // Panel Mượn sách
        borrowPanel = new javax.swing.JPanel();
        cardIdLabel = new javax.swing.JLabel();
        cardIdField = new javax.swing.JTextField();
        bookIdLabel = new javax.swing.JLabel();
        bookIdField = new javax.swing.JTextField();
        daysLabel = new javax.swing.JLabel();
        daysSpinner = new javax.swing.JSpinner();
        addToCartButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        availableBooksTable = new javax.swing.JScrollPane();
        booksTable = new javax.swing.JTable();
        borrowCartTable = new javax.swing.JScrollPane();
        cartTable = new javax.swing.JTable();
        removeFromCartButton = new javax.swing.JButton();
        clearCartButton = new javax.swing.JButton();
        borrowAllButton = new javax.swing.JButton();
        cartTotalLabel = new javax.swing.JLabel();

        // Panel Trả sách
        returnPanel = new javax.swing.JPanel();
        borrowedBooksTable = new javax.swing.JScrollPane();
        borrowedTable = new javax.swing.JTable();
        returnInfoLabel = new javax.swing.JLabel();
        returnButton = new javax.swing.JButton();
        lostBookButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 10));

        // Header panel
        javax.swing.JPanel headerPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        headerPanel.setBackground(new java.awt.Color(245, 245, 250));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 0, 40));

        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Mượn / Trả sách");
        headerPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        memberInfoLabel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        memberInfoLabel.setForeground(new java.awt.Color(0, 120, 215));
        memberInfoLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        headerPanel.add(memberInfoLabel, java.awt.BorderLayout.EAST);

        add(headerPanel, java.awt.BorderLayout.NORTH);

        // ============ PANEL MƯỢN SÁCH ============
        borrowPanel.setBackground(new java.awt.Color(245, 245, 250));
        borrowPanel.setLayout(new java.awt.BorderLayout(0, 10));
        borrowPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Top form panel
        javax.swing.JPanel topPanel = new javax.swing.JPanel();
        topPanel.setBackground(new java.awt.Color(255, 255, 255));
        topPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createTitledBorder(null, "Thêm sách vào giỏ mượn",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(60, 60, 60)),
                javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        topPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 5));

        cardIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cardIdLabel.setText("Mã thẻ:");
        topPanel.add(cardIdLabel);
        cardIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cardIdField.setColumns(10);
        cardIdField.setText(currentCardId);
        cardIdField.setEditable(false);
        topPanel.add(cardIdField);

        bookIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        bookIdLabel.setText("Mã sách:");
        topPanel.add(bookIdLabel);
        bookIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        bookIdField.setColumns(10);
        topPanel.add(bookIdField);

        searchButton.setBackground(new java.awt.Color(0, 120, 215));
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("🔍");
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchBook());
        topPanel.add(searchButton);

        daysLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        daysLabel.setText("Số ngày:");
        topPanel.add(daysLabel);
        daysSpinner.setModel(new javax.swing.SpinnerNumberModel(14, 1, 60, 1));
        daysSpinner.setFont(new java.awt.Font("Segoe UI", 0, 13));
        topPanel.add(daysSpinner);

        addToCartButton.setBackground(new java.awt.Color(50, 150, 50));
        addToCartButton.setForeground(new java.awt.Color(255, 255, 255));
        addToCartButton.setText("➕ Thêm vào giỏ");
        addToCartButton.setFont(new java.awt.Font("Segoe UI", 1, 13));
        addToCartButton.setFocusPainted(false);
        addToCartButton.addActionListener(e -> addToCart());
        topPanel.add(addToCartButton);

        borrowPanel.add(topPanel, java.awt.BorderLayout.NORTH);

        // Center - Split panel for books list and cart
        javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(500);

        // Left - Available books
        javax.swing.JPanel leftPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 5));
        leftPanel.setBackground(new java.awt.Color(245, 245, 250));

        javax.swing.JLabel booksLabel = new javax.swing.JLabel("📚 Sách có sẵn:");
        booksLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        leftPanel.add(booksLabel, java.awt.BorderLayout.NORTH);

        String[] bookColumns = { "Mã sách", "Tên sách", "Tác giả", "Giá", "SL có sẵn" };
        booksTable = new javax.swing.JTable(new Object[][] {}, bookColumns);
        booksTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        booksTable.setRowHeight(25);
        booksTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            int row = booksTable.getSelectedRow();
            if (row >= 0) {
                bookIdField.setText(booksTable.getValueAt(row, 0).toString());
            }
        });
        availableBooksTable.setViewportView(booksTable);
        leftPanel.add(availableBooksTable, java.awt.BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // Right - Borrow cart
        javax.swing.JPanel rightPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 5));
        rightPanel.setBackground(new java.awt.Color(245, 245, 250));

        javax.swing.JLabel cartLabel = new javax.swing.JLabel("🛒 Giỏ mượn:");
        cartLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        rightPanel.add(cartLabel, java.awt.BorderLayout.NORTH);

        String[] cartColumns = { "Mã sách", "Tên sách", "Số ngày", "Tiền cọc", "Phí thuê", "Tổng" };
        cartTable = new javax.swing.JTable(new Object[][] {}, cartColumns);
        cartTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        cartTable.setRowHeight(25);
        borrowCartTable.setViewportView(cartTable);
        rightPanel.add(borrowCartTable, java.awt.BorderLayout.CENTER);

        // Cart buttons
        javax.swing.JPanel cartButtonPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
        cartButtonPanel.setBackground(new java.awt.Color(245, 245, 250));

        cartTotalLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        cartTotalLabel.setForeground(new java.awt.Color(0, 150, 0));
        cartTotalLabel.setText("Tổng: 0 đ");
        cartButtonPanel.add(cartTotalLabel);

        removeFromCartButton.setText("❌ Xóa");
        removeFromCartButton.setBackground(new java.awt.Color(200, 100, 100));
        removeFromCartButton.setForeground(java.awt.Color.WHITE);
        removeFromCartButton.setFocusPainted(false);
        removeFromCartButton.addActionListener(e -> removeFromCart());
        cartButtonPanel.add(removeFromCartButton);

        clearCartButton.setText("🗑️ Xóa hết");
        clearCartButton.setBackground(new java.awt.Color(150, 150, 150));
        clearCartButton.setForeground(java.awt.Color.WHITE);
        clearCartButton.setFocusPainted(false);
        clearCartButton.addActionListener(e -> clearCart());
        cartButtonPanel.add(clearCartButton);

        rightPanel.add(cartButtonPanel, java.awt.BorderLayout.SOUTH);
        splitPane.setRightComponent(rightPanel);

        borrowPanel.add(splitPane, java.awt.BorderLayout.CENTER);

        // Bottom - Borrow all button
        borrowAllButton.setBackground(new java.awt.Color(0, 120, 215));
        borrowAllButton.setForeground(new java.awt.Color(255, 255, 255));
        borrowAllButton.setText("✓ XÁC NHẬN MƯỢN TẤT CẢ");
        borrowAllButton.setFont(new java.awt.Font("Segoe UI", 1, 16));
        borrowAllButton.setFocusPainted(false);
        borrowAllButton.setPreferredSize(new java.awt.Dimension(0, 50));
        borrowAllButton.addActionListener(e -> borrowAllBooks());
        borrowPanel.add(borrowAllButton, java.awt.BorderLayout.SOUTH);

        // ============ PANEL TRẢ SÁCH ============
        returnPanel.setBackground(new java.awt.Color(245, 245, 250));
        returnPanel.setLayout(new java.awt.BorderLayout(0, 10));
        returnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Borrowed books table
        javax.swing.JPanel returnTopPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 5));
        returnTopPanel.setBackground(new java.awt.Color(245, 245, 250));

        javax.swing.JPanel returnHeaderPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        returnHeaderPanel.setBackground(new java.awt.Color(245, 245, 250));

        javax.swing.JLabel borrowedLabel = new javax.swing.JLabel("📖 Sách đang mượn:");
        borrowedLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        returnHeaderPanel.add(borrowedLabel, java.awt.BorderLayout.WEST);

        javax.swing.JButton selectAllReturnButton = new javax.swing.JButton("Chọn tất cả");
        selectAllReturnButton.setBackground(new java.awt.Color(0, 120, 215));
        selectAllReturnButton.setForeground(java.awt.Color.WHITE);
        selectAllReturnButton.setFocusPainted(false);
        selectAllReturnButton.addActionListener(e -> {
            boolean allSelected = true;
            // Kiểm tra xem đã chọn hết chưa để toggle
            for (int i = 0; i < borrowedTable.getRowCount(); i++) {
                if (!(Boolean) borrowedTable.getValueAt(i, 0)) {
                    allSelected = false;
                    break;
                }
            }
            // Set value
            for (int i = 0; i < borrowedTable.getRowCount(); i++) {
                borrowedTable.setValueAt(!allSelected, i, 0);
            }
        });
        returnHeaderPanel.add(selectAllReturnButton, java.awt.BorderLayout.EAST);

        returnTopPanel.add(returnHeaderPanel, java.awt.BorderLayout.NORTH);

        String[] returnColumns = { "Chọn", "ID", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Số ngày", "Phí thuê",
                "Trạng thái",
                "Tiền cọc", "Phí phạt" };

        // Custom Model cho Checkbox
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(new Object[][] {},
                returnColumns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        borrowedTable = new javax.swing.JTable(model);
        borrowedTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        borrowedTable.setRowHeight(25);
        // TableModelListener để bắt sự kiện checkbox thay đổi
        borrowedTable.getModel().addTableModelListener(e -> updateReturnInfo());

        borrowedBooksTable.setViewportView(borrowedTable);
        returnTopPanel.add(borrowedBooksTable, java.awt.BorderLayout.CENTER);

        returnPanel.add(returnTopPanel, java.awt.BorderLayout.CENTER);

        // Bottom buttons
        javax.swing.JPanel returnBottomPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        returnBottomPanel.setBackground(new java.awt.Color(245, 245, 250));

        returnInfoLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        returnInfoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        returnInfoLabel.setText("Chọn sách để trả");
        returnBottomPanel.add(returnInfoLabel, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel returnButtonPanel = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 5));
        returnButtonPanel.setBackground(new java.awt.Color(245, 245, 250));

        returnButton.setBackground(new java.awt.Color(50, 150, 50));
        returnButton.setForeground(new java.awt.Color(255, 255, 255));
        returnButton.setText("✓ Trả sách đã chọn");
        returnButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        returnButton.setFocusPainted(false);
        returnButton.addActionListener(e -> returnBook());
        returnButtonPanel.add(returnButton);

        lostBookButton.setBackground(new java.awt.Color(200, 50, 50));
        lostBookButton.setForeground(new java.awt.Color(255, 255, 255));
        lostBookButton.setText("❌ Báo mất sách");
        lostBookButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lostBookButton.setFocusPainted(false);
        lostBookButton.addActionListener(e -> reportLostBook());
        returnButtonPanel.add(lostBookButton);

        refreshButton.setBackground(new java.awt.Color(100, 100, 100));
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setText("🔄 Làm mới");
        refreshButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshBorrowedList());
        returnButtonPanel.add(refreshButton);

        returnBottomPanel.add(returnButtonPanel, java.awt.BorderLayout.CENTER);
        returnPanel.add(returnBottomPanel, java.awt.BorderLayout.SOUTH);

        // Thêm các tab
        tabbedPane.addTab("📚 Mượn sách", borrowPanel);
        tabbedPane.addTab("📖 Trả sách", returnPanel);
        tabbedPane.setFont(new java.awt.Font("Segoe UI", 1, 14));
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                loadBorrowedBooks();
            }
        });

        add(tabbedPane, java.awt.BorderLayout.CENTER);
    }

    private void loadAvailableBooks() {
        List<BookService.Book> books = bookService.getAllBooks();
        String[] columns = { "Mã sách", "Tên sách", "Tác giả", "Giá", "SL có sẵn" };
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        Object[][] data = new Object[books.size()][5];
        for (int i = 0; i < books.size(); i++) {
            BookService.Book book = books.get(i);
            // Số lượng có sẵn = Stock - BorrowStock (số đang mượn)
            int available = book.stock - book.borrowStock;
            data[i][0] = book.bookId;
            data[i][1] = book.title;
            data[i][2] = book.author;
            data[i][3] = nf.format(book.price) + " đ";
            data[i][4] = available > 0 ? available : "Hết";
        }
        booksTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    /**
     * Load sách đang mượn từ DB
     */
    private void loadBorrowedBooks() {
        List<BorrowService.BorrowRecord> records = borrowService.getBorrowedBooksByCard(currentCardId);
        // Cột: Chọn, ID, Mã sách, Tên sách, Ngày mượn, Hạn trả, Số ngày, Phí thuê,
        // Trạng thái, Tiền cọc, Phí phạt
        String[] columns = { "Chọn", "ID", "Mã sách", "Tên sách", "Ngày mượn", "Hạn trả", "Số ngày", "Phí thuê",
                "Trạng thái", "Tiền cọc", "Phí phạt" };
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

        Object[][] data = new Object[records.size()][11];

        for (int i = 0; i < records.size(); i++) {
            BorrowService.BorrowRecord record = records.get(i);
            BookService.Book book = bookService.getBookById(record.bookId);

            data[i][0] = false; // Checkbox unchecked by default
            data[i][1] = record.id;
            data[i][2] = record.bookId;
            data[i][3] = book != null ? book.title : record.bookId;

            // Format dates và tính phí
            try {
                Date borrowDate = inputFormat.parse(record.borrowDate);
                Date dueDate = inputFormat.parse(record.dueDate);
                data[i][4] = outputFormat.format(borrowDate);
                data[i][5] = outputFormat.format(dueDate);

                long borrowDays = (dueDate.getTime() - borrowDate.getTime()) / (1000 * 60 * 60 * 24);
                data[i][6] = borrowDays + " ngày";

                int rentalFee = 0;
                if (borrowDays > FREE_DURATION_DAYS) {
                    rentalFee = (int) ((borrowDays - FREE_DURATION_DAYS) * RENTAL_FEE_PER_DAY);
                    data[i][7] = nf.format(rentalFee) + " đ";
                } else {
                    data[i][7] = "Miễn phí";
                }

                Date today = new Date();
                int lateFee = 0;
                if (today.after(dueDate)) {
                    long lateDays = (today.getTime() - dueDate.getTime()) / (1000 * 60 * 60 * 24);
                    lateFee = (int) (lateDays * LATE_FEE_PER_DAY);
                    if (book != null && lateFee >= book.price) {
                        data[i][8] = "⚠️ MẤT SÁCH";
                        lateFee = (int) book.price;
                    } else {
                        data[i][8] = "TRỄ " + lateDays + " ngày";
                    }
                } else {
                    data[i][8] = "Đang mượn";
                }
                data[i][10] = lateFee > 0 ? nf.format(lateFee) + " đ" : "0 đ";
            } catch (Exception e) {
                data[i][4] = record.borrowDate;
                data[i][5] = record.dueDate;
                data[i][6] = "--";
                data[i][7] = "--";
                data[i][8] = "Đang mượn";
                data[i][10] = "0 đ";
            }

            data[i][9] = book != null ? nf.format(book.price) + " đ" : "--";
        }

        // Preserve TableModelListener
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        borrowedTable.setModel(model);
        borrowedTable.getModel().addTableModelListener(e -> updateReturnInfo());

        // Căn chỉnh độ rộng cột checkbox
        borrowedTable.getColumnModel().getColumn(0).setMaxWidth(50);

        updateReturnInfo();
        loadMemberInfo();
    }

    private void searchBook() {
        String bookId = bookIdField.getText().trim();
        if (bookId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập mã sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        BookService.Book book = bookService.getBookById(bookId);
        if (book == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy sách với mã: " + bookId, "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
        } else {
            // Highlight in table
            for (int i = 0; i < booksTable.getRowCount(); i++) {
                if (booksTable.getValueAt(i, 0).toString().equals(bookId)) {
                    booksTable.setRowSelectionInterval(i, i);
                    booksTable.scrollRectToVisible(booksTable.getCellRect(i, 0, true));
                    break;
                }
            }
        }
    }

    /**
     * Thêm sách vào giỏ mượn
     */
    private void addToCart() {
        String bookId = bookIdField.getText().trim();
        if (bookId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kiểm tra đã có trong giỏ chưa
        for (BorrowCartItem item : borrowCart) {
            if (item.bookId.equals(bookId)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Sách đã có trong giỏ!", "Thông báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Kiểm tra giới hạn mượn
        if (currentBorrowedCount + borrowCart.size() >= maxBooksAllowed) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Đã đạt giới hạn " + maxBooksAllowed + " quyển (hạng " + currentMemberType + ")!",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookService.Book book = bookService.getBookById(bookId);
        if (book == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy sách!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra còn sách không
        int available = book.stock - book.borrowStock;
        if (available <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Sách đã hết!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int days = (Integer) daysSpinner.getValue();

        // Kiểm tra còn lượt free không
        int usedFreeInCart = 0;
        for (BorrowCartItem item : borrowCart) {
            if (item.useFreeSlot)
                usedFreeInCart++;
        }
        boolean canUseFree = (usedFreeBorrowsThisMonth + usedFreeInCart) < freeBorrowsPerMonth;

        borrowCart.add(new BorrowCartItem(bookId, book.title, book.price, days, canUseFree));
        updateCartTable();
        bookIdField.setText("");
    }

    /**
     * Xóa sách khỏi giỏ
     */
    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < borrowCart.size()) {
            borrowCart.remove(row);
            updateCartTable();
        }
    }

    /**
     * Xóa toàn bộ giỏ
     */
    private void clearCart() {
        borrowCart.clear();
        updateCartTable();
    }

    /**
     * Cập nhật bảng giỏ mượn
     */
    private void updateCartTable() {
        String[] columns = { "Mã sách", "Tên sách", "Số ngày", "Tiền cọc", "Phí thuê", "Tổng" };
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        Object[][] data = new Object[borrowCart.size()][6];
        int grandTotal = 0;

        for (int i = 0; i < borrowCart.size(); i++) {
            BorrowCartItem item = borrowCart.get(i);
            data[i][0] = item.bookId;
            data[i][1] = item.title;
            data[i][2] = item.days + " ngày" + (item.useFreeSlot ? " (Free)" : "");
            data[i][3] = nf.format(item.price) + " đ";
            data[i][4] = item.getRentalFee() > 0 ? nf.format(item.getRentalFee()) + " đ" : "Miễn phí";
            data[i][5] = nf.format(item.getTotalCost()) + " đ";
            grandTotal += item.getTotalCost();
        }

        cartTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
        cartTotalLabel.setText("Tổng: " + nf.format(grandTotal) + " đ");
    }

    /**
     * Mượn tất cả sách trong giỏ
     */
    private void borrowAllBooks() {
        if (borrowCart.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giỏ mượn trống!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tính tổng tiền
        int totalAmount = 0;
        for (BorrowCartItem item : borrowCart) {
            totalAmount += item.getTotalCost();
        }

        // Kiểm tra số dư thẻ
        int cardBalance = 0;
        try {
            CardConnectionManager connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                if (info.success) {
                    cardBalance = info.balance;
                }
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[MUONTRA] Error reading balance: " + e.getMessage());
        }

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        if (cardBalance < totalAmount) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Số dư thẻ không đủ!\nSố dư: " + nf.format(cardBalance) + " đ\nCần: " + nf.format(totalAmount)
                            + " đ",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Xác nhận
        StringBuilder sb = new StringBuilder("Xác nhận mượn " + borrowCart.size() + " quyển sách?\n\n");
        for (BorrowCartItem item : borrowCart) {
            sb.append("• ").append(item.title).append(" (").append(item.days).append(" ngày)\n");
        }
        sb.append("\nTổng tiền: ").append(nf.format(totalAmount)).append(" đ");

        int option = javax.swing.JOptionPane.showConfirmDialog(this, sb.toString(), "Xác nhận mượn sách",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Thực hiện thanh toán và mượn
        boolean success = false;
        CardConnectionManager connManager = null;
        try {
            connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

                // 1. Thanh toán
                boolean paymentOk = balanceManager.payment(totalAmount);
                if (!paymentOk) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Thanh toán thất bại!", "Lỗi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2. Lưu từng sách vào thẻ và DB
                int successCount = 0;
                for (BorrowCartItem item : borrowCart) {
                    int bookType = item.useFreeSlot ? 1 : 0;
                    boolean borrowOk = balanceManager.borrowBook(item.bookId, item.days, bookType);

                    if (borrowOk) {
                        // Lưu vào DB
                        borrowService.borrowBook(currentCardId, item.bookId, item.days);

                        // Cập nhật BorrowStock trong DB (tăng lên 1)
                        BookService.Book book = bookService.getBookById(item.bookId);
                        if (book != null) {
                            bookService.updateBorrowStock(item.bookId, book.borrowStock + 1);
                        }
                        successCount++;
                    }
                }

                if (successCount == borrowCart.size()) {
                    success = true;
                } else if (successCount > 0) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Mượn thành công " + successCount + "/" + borrowCart.size() + " quyển!",
                            "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    success = true;
                }

                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[MUONTRA] Error borrowing: " + e.getMessage());
        } finally {
            try {
                if (connManager != null)
                    connManager.disconnectCard();
            } catch (Exception ignored) {
            }
        }

        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this, "Mượn sách thành công!",
                    "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            borrowCart.clear();
            updateCartTable();
            loadMemberInfo();
            loadAvailableBooks();
        }
    }

    /**
     * Cập nhật thông tin khi chọn sách để trả
     */
    /**
     * Cập nhật thông tin khi chọn sách để trả (hỗ trợ nhiều sách qua checkbox)
     */
    private void updateReturnInfo() {
        List<Integer> checkedRows = new ArrayList<>();
        for (int i = 0; i < borrowedTable.getRowCount(); i++) {
            if ((Boolean) borrowedTable.getValueAt(i, 0)) {
                checkedRows.add(i);
            }
        }

        if (checkedRows.isEmpty()) {
            returnInfoLabel.setText("Chọn sách để trả (tích vào ô vuông bên trái)");
            return;
        }

        long totalRefund = 0;
        long totalDeposit = 0;
        long totalFine = 0;

        // Định dạng tiền tệ
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

        for (int row : checkedRows) {
            // Cột: Check(0), ID(1), Mã sách(2), Tên sách(3), Ngày mượn(4), Hạn trả(5), Số
            // ngày(6),
            // Phí thuê(7), Trạng thái(8), Tiền cọc(9), Phí phạt(10)
            String status = borrowedTable.getValueAt(row, 8).toString();
            String depositStr = borrowedTable.getValueAt(row, 9).toString().replace(" đ", "").replace(".", "");
            String fineStr = borrowedTable.getValueAt(row, 10).toString().replace(" đ", "").replace(".", "");

            long deposit = 0;
            long fine = 0;
            try {
                deposit = Long.parseLong(depositStr);
                fine = Long.parseLong(fineStr);
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }

            if (status.contains("MẤT SÁCH")) {
                totalDeposit += deposit;
                // Mất sách -> Phạt = Cọc, Hoàn = 0
                totalFine += deposit;
            } else {
                totalDeposit += deposit;
                totalFine += fine;
                long refund = deposit - fine;
                if (refund > 0)
                    totalRefund += refund;
            }
        }

        if (checkedRows.size() == 1) {
            int row = checkedRows.get(0);
            String status = borrowedTable.getValueAt(row, 8).toString();
            String rentalFee = borrowedTable.getValueAt(row, 7).toString();
            if (status.contains("MẤT SÁCH")) {
                returnInfoLabel.setText("⚠️ Sách coi như đã mất. Tiền cọc bị tịch thu.");
                returnInfoLabel.setForeground(new java.awt.Color(200, 0, 0));
            } else {
                returnInfoLabel.setText("Cọc: " + nf.format(totalDeposit) + " đ | Thuê: " + rentalFee + " | Phạt: "
                        + nf.format(totalFine) + " đ | Hoàn: " + nf.format(totalRefund) + " đ");
                returnInfoLabel
                        .setForeground(totalFine > 0 ? new java.awt.Color(200, 100, 0) : new java.awt.Color(0, 150, 0));
            }
        } else {
            returnInfoLabel.setText("Đang chọn " + checkedRows.size() + " quyển | Tổng hoàn: " + nf.format(totalRefund)
                    + " đ (Cọc: " + nf.format(totalDeposit) + " - Phạt: " + nf.format(totalFine) + ")");
            returnInfoLabel.setForeground(new java.awt.Color(0, 100, 200));
        }
    }

    /**
     * Trả sách (hỗ trợ nhiều sách qua checkbox)
     */
    private void returnBook() {
        List<Integer> checkedRows = new ArrayList<>();
        for (int i = 0; i < borrowedTable.getRowCount(); i++) {
            if ((Boolean) borrowedTable.getValueAt(i, 0)) {
                checkedRows.add(i);
            }
        }

        if (checkedRows.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để trả!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        long totalRefund = 0;
        List<String> bookTitles = new ArrayList<>();

        // Tính toán trước tổng tiền và danh sách sách
        for (int row : checkedRows) {
            String bookId = borrowedTable.getValueAt(row, 2).toString();
            String title = borrowedTable.getValueAt(row, 3).toString();
            String status = borrowedTable.getValueAt(row, 8).toString();

            BookService.Book book = bookService.getBookById(bookId);
            if (book != null) {
                long deposit = (long) book.price;
                long fine = 0;

                if (status.contains("MẤT SÁCH")) {
                    fine = deposit;
                } else if (status.contains("TRỄ")) {
                    try {
                        String[] parts = status.split(" ");
                        int lateDays = Integer.parseInt(parts[1]);
                        fine = lateDays * LATE_FEE_PER_DAY;
                    } catch (Exception e) {
                    }
                }

                long refund = deposit - fine;
                if (refund < 0)
                    refund = 0;

                totalRefund += refund;
                bookTitles.add(title + (refund == 0 ? " (Không hoàn tiền)" : ""));
            }
        }

        // Xác nhận
        StringBuilder confirmMsg = new StringBuilder("Xác nhận trả " + checkedRows.size() + " quyển sách?\n\n");
        for (String title : bookTitles) {
            confirmMsg.append("• ").append(title).append("\n");
        }
        confirmMsg.append("\nTỔNG HOÀN TRẢ: ").append(nf.format(totalRefund)).append(" đ vào thẻ.");

        int option = javax.swing.JOptionPane.showConfirmDialog(this, confirmMsg.toString(), "Xác nhận trả sách",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Thực hiện trả sách
        int successCount = 0;
        long actualRefunded = 0;

        try {
            CardConnectionManager connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

                for (int row : checkedRows) {
                    int borrowId = Integer.parseInt(borrowedTable.getValueAt(row, 1).toString());
                    String bookId = borrowedTable.getValueAt(row, 2).toString();
                    String status = borrowedTable.getValueAt(row, 8).toString();
                    BookService.Book book = bookService.getBookById(bookId);

                    if (book == null)
                        continue;

                    // 1. Xóa sách khỏi thẻ
                    boolean returnOk = balanceManager.returnBook(bookId);
                    if (returnOk) {
                        // Tính lại tiền hoàn cho quyển này để cộng dồn
                        long deposit = (long) book.price;
                        long fine = 0;
                        if (status.contains("MẤT SÁCH"))
                            fine = deposit;
                        else if (status.contains("TRỄ")) {
                            try {
                                String[] parts = status.split(" ");
                                int lateDays = Integer.parseInt(parts[1]);
                                fine = lateDays * LATE_FEE_PER_DAY;
                            } catch (Exception e) {
                            }
                        }
                        long refund = Math.max(0, deposit - fine);
                        actualRefunded += refund;

                        // 2. Cập nhật DB
                        borrowService.returnBook(borrowId, currentCardId);

                        // 3. Giảm BorrowStock
                        bookService.updateBorrowStock(bookId, Math.max(0, book.borrowStock - 1));

                        successCount++;
                    } else {
                        System.err.println("[MUONTRA] Failed to remove book from card: " + bookId);
                    }
                }

                // 4. Hoàn tiền tổng vào thẻ (1 giao dịch)
                if (actualRefunded > 0) {
                    balanceManager.deposit((int) actualRefunded);
                }

                connManager.disconnectCard();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể kết nối thẻ!", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("[MUONTRA] Error returning: " + e.getMessage());
        }

        if (successCount > 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Đã trả thành công " + successCount + "/" + checkedRows.size() + " quyển!\nĐã hoàn: "
                            + nf.format(actualRefunded) + " đ vào thẻ.",
                    "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadBorrowedBooks();
            loadAvailableBooks();
            returnInfoLabel.setText("Chọn sách để trả");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Trả sách thất bại! Vui lòng thử lại.",
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Báo mất sách
     */
    private void reportLostBook() {
        int row = borrowedTable.getSelectedRow();
        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int borrowId = Integer.parseInt(borrowedTable.getValueAt(row, 0).toString());
        String bookId = borrowedTable.getValueAt(row, 1).toString();
        BookService.Book book = bookService.getBookById(bookId);

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        String confirmMsg = "⚠️ Báo mất sách?\n\n" +
                "Sách: " + book.title + "\n" +
                "Giá: " + nf.format(book.price) + " đ\n\n" +
                "TIỀN CỌC SẼ KHÔNG ĐƯỢC HOÀN TRẢ!";

        int option = javax.swing.JOptionPane.showConfirmDialog(this, confirmMsg, "Xác nhận mất sách",
                javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
        if (option != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        // Xóa khỏi thẻ, không hoàn tiền, giảm BorrowStock
        try {
            CardConnectionManager connManager = new CardConnectionManager();
            if (connManager.connectCard()) {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());
                balanceManager.returnBook(bookId);
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[MUONTRA] Error reporting lost: " + e.getMessage());
        }

        // Cập nhật DB
        borrowService.returnBook(borrowId, currentCardId);
        bookService.updateBorrowStock(bookId, Math.max(0, book.borrowStock - 1));

        // Giảm Stock vĩnh viễn (mất sách)
        bookService.updateBookStock(bookId, Math.max(0, book.stock - 1));

        javax.swing.JOptionPane.showMessageDialog(this,
                "Đã ghi nhận sách bị mất.\nTiền cọc đã bị tịch thu.",
                "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);

        loadBorrowedBooks();
        loadAvailableBooks();
    }

    private void refreshBorrowedList() {
        loadBorrowedBooks();
        loadMemberInfo();
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel memberInfoLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel borrowPanel;
    private javax.swing.JLabel cardIdLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JLabel bookIdLabel;
    private javax.swing.JTextField bookIdField;
    private javax.swing.JLabel daysLabel;
    private javax.swing.JSpinner daysSpinner;
    private javax.swing.JButton addToCartButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JScrollPane availableBooksTable;
    private javax.swing.JTable booksTable;
    private javax.swing.JScrollPane borrowCartTable;
    private javax.swing.JTable cartTable;
    private javax.swing.JButton removeFromCartButton;
    private javax.swing.JButton clearCartButton;
    private javax.swing.JButton borrowAllButton;
    private javax.swing.JLabel cartTotalLabel;
    private javax.swing.JPanel returnPanel;
    private javax.swing.JScrollPane borrowedBooksTable;
    private javax.swing.JTable borrowedTable;
    private javax.swing.JLabel returnInfoLabel;
    private javax.swing.JButton returnButton;
    private javax.swing.JButton lostBookButton;
    private javax.swing.JButton refreshButton;
}
