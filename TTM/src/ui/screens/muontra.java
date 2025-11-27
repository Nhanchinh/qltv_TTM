/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.BookService;
import services.BorrowService;
import services.CardService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author admin
 */
public class muontra extends javax.swing.JPanel {
    
    private BookService bookService;
    private BorrowService borrowService;
    private CardService cardService;
    private String currentCardId = "CARD001"; // Default card ID

    /**
     * Creates new form BorrowPanel
     */
    public muontra() {
        bookService = new BookService();
        borrowService = new BorrowService();
        cardService = new CardService();
        initComponents();
        loadAvailableBooks();
        loadBorrowedBooks();
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        
        // TabbedPane để phân biệt Mượn sách và Trả sách
        tabbedPane = new javax.swing.JTabbedPane();
        
        // Panel Mượn sách
        borrowPanel = new javax.swing.JPanel();
        borrowTitleLabel = new javax.swing.JLabel();
        cardIdLabel = new javax.swing.JLabel();
        cardIdField = new javax.swing.JTextField();
        bookIdLabel = new javax.swing.JLabel();
        bookIdField = new javax.swing.JTextField();
        bookNameLabel = new javax.swing.JLabel();
        bookNameField = new javax.swing.JTextField();
        authorLabel = new javax.swing.JLabel();
        authorField = new javax.swing.JTextField();
        borrowDateLabel = new javax.swing.JLabel();
        borrowDateField = new javax.swing.JTextField();
        returnDateLabel = new javax.swing.JLabel();
        returnDateField = new javax.swing.JTextField();
        borrowButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        availableBooksTable = new javax.swing.JScrollPane();
        booksTable = new javax.swing.JTable();
        
        // Panel Trả sách
        returnPanel = new javax.swing.JPanel();
        returnTitleLabel = new javax.swing.JLabel();
        borrowedBooksTable = new javax.swing.JScrollPane();
        borrowedTable = new javax.swing.JTable();
        returnBookIdLabel = new javax.swing.JLabel();
        returnBookIdField = new javax.swing.JTextField();
        returnDateLabel2 = new javax.swing.JLabel();
        returnDateField2 = new javax.swing.JTextField();
        lateFeeLabel = new javax.swing.JLabel();
        lateFeeField = new javax.swing.JTextField();
        returnButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 20));

        // Title
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Mượn / Trả sách");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        // ============ PANEL MƯỢN SÁCH ============
        borrowPanel.setBackground(new java.awt.Color(245, 245, 250));
        borrowPanel.setLayout(new java.awt.BorderLayout(0, 20));
        borrowPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 30, 30));

        borrowTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 20));
        borrowTitleLabel.setForeground(new java.awt.Color(0, 120, 215));
        borrowTitleLabel.setText("Mượn sách");
        borrowPanel.add(borrowTitleLabel, java.awt.BorderLayout.NORTH);

        // Form panel
        javax.swing.JPanel borrowFormPanel = new javax.swing.JPanel();
        borrowFormPanel.setBackground(new java.awt.Color(255, 255, 255));
        borrowFormPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Thông tin sách", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        javax.swing.GroupLayout formLayout = new javax.swing.GroupLayout(borrowFormPanel);
        borrowFormPanel.setLayout(formLayout);
        
        formLayout.setHorizontalGroup(
            formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bookIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bookNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(borrowDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(returnDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdField)
                    .addComponent(bookIdField)
                    .addComponent(bookNameField)
                    .addComponent(authorField)
                    .addComponent(borrowDateField)
                    .addComponent(returnDateField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(searchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                    .addComponent(borrowButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(bookIdLabel)
                    .addComponent(bookIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bookNameLabel)
                    .addComponent(bookNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authorLabel)
                    .addComponent(authorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(borrowDateLabel)
                    .addComponent(borrowDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(returnDateLabel)
                    .addComponent(returnDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(borrowButton))
                .addContainerGap())
        );

        // Labels và Fields setup
        cardIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cardIdLabel.setText("Mã thẻ:");
        bookIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        bookIdLabel.setText("Mã sách:");
        bookNameLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        bookNameLabel.setText("Tên sách:");
        authorLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        authorLabel.setText("Tác giả:");
        borrowDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        borrowDateLabel.setText("Ngày mượn:");
        returnDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        returnDateLabel.setText("Ngày trả dự kiến:");

        cardIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cardIdField.setColumns(20);
        cardIdField.setText(currentCardId);
        cardIdField.setEditable(false);
        bookIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        bookIdField.setColumns(20);
        bookNameField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        bookNameField.setEditable(false);
        bookNameField.setColumns(20);
        authorField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        authorField.setEditable(false);
        authorField.setColumns(20);
        borrowDateField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        borrowDateField.setEditable(false);
        borrowDateField.setColumns(20);
        returnDateField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        returnDateField.setEditable(false);
        returnDateField.setColumns(20);

        searchButton.setBackground(new java.awt.Color(0, 120, 215));
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("🔍 Tìm kiếm");
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchBook());

        borrowButton.setBackground(new java.awt.Color(50, 150, 50));
        borrowButton.setForeground(new java.awt.Color(255, 255, 255));
        borrowButton.setText("✓ Mượn sách");
        borrowButton.setFocusPainted(false);
        borrowButton.addActionListener(e -> borrowBook());

        // Table cho danh sách sách có sẵn - sẽ được load từ database
        String[] columns = {"Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Số lượng", "Trạng thái"};
        Object[][] data = {};
        booksTable = new javax.swing.JTable(data, columns);
        booksTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        booksTable.setRowHeight(25);
        booksTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow >= 0) {
                bookIdField.setText(booksTable.getValueAt(selectedRow, 0).toString());
                bookNameField.setText(booksTable.getValueAt(selectedRow, 1).toString());
                authorField.setText(booksTable.getValueAt(selectedRow, 2).toString());
            }
        });
        availableBooksTable.setViewportView(booksTable);

        javax.swing.JPanel centerPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        centerPanel.setBackground(new java.awt.Color(245, 245, 250));
        centerPanel.add(borrowFormPanel, java.awt.BorderLayout.NORTH);
        
        // Wrapper panel for table with label - label ở trên bảng
        javax.swing.JPanel tablePanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 10));
        tablePanel.setBackground(new java.awt.Color(245, 245, 250));
        
        // Table label - đặt ở trên bảng
        javax.swing.JLabel tableLabel = new javax.swing.JLabel("Danh sách sách có sẵn:");
        tableLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        tableLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 0, 5, 0));
        tablePanel.add(tableLabel, java.awt.BorderLayout.NORTH);
        tablePanel.add(availableBooksTable, java.awt.BorderLayout.CENTER);
        
        centerPanel.add(tablePanel, java.awt.BorderLayout.CENTER);
        
        borrowPanel.add(centerPanel, java.awt.BorderLayout.CENTER);

        // ============ PANEL TRẢ SÁCH ============
        returnPanel.setBackground(new java.awt.Color(245, 245, 250));
        returnPanel.setLayout(new java.awt.BorderLayout(0, 20));
        returnPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 30, 30));

        returnTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 20));
        returnTitleLabel.setForeground(new java.awt.Color(215, 100, 50));
        returnTitleLabel.setText("Trả sách");
        returnPanel.add(returnTitleLabel, java.awt.BorderLayout.NORTH);

        // Table cho sách đang mượn - sẽ được load từ database
        String[] returnColumns = {"ID", "Mã sách", "Tên sách", "Ngày mượn", "Ngày trả dự kiến", "Ngày trả thực tế", "Phí phạt", "Trạng thái"};
        Object[][] returnData = {};
        borrowedTable = new javax.swing.JTable(returnData, returnColumns);
        borrowedTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        borrowedTable.setRowHeight(25);
        borrowedTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        borrowedTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = borrowedTable.getSelectedRow();
            if (selectedRow >= 0) {
                returnBookIdField.setText(borrowedTable.getValueAt(selectedRow, 1).toString());
            }
        });
        borrowedBooksTable.setViewportView(borrowedTable);

        // Return form panel
        javax.swing.JPanel returnFormPanel = new javax.swing.JPanel();
        returnFormPanel.setBackground(new java.awt.Color(255, 255, 255));
        returnFormPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Thông tin trả sách",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        javax.swing.GroupLayout returnFormLayout = new javax.swing.GroupLayout(returnFormPanel);
        returnFormPanel.setLayout(returnFormLayout);
        
        returnFormLayout.setHorizontalGroup(
            returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(returnFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(returnBookIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(returnDateLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lateFeeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(returnBookIdField)
                    .addComponent(returnDateField2)
                    .addComponent(lateFeeField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(returnButton, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        
        returnFormLayout.setVerticalGroup(
            returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(returnFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(returnBookIdLabel)
                    .addComponent(returnBookIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(returnButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(returnDateLabel2)
                    .addComponent(returnDateField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(returnFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lateFeeLabel)
                    .addComponent(lateFeeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        returnBookIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        returnBookIdLabel.setText("Mã sách:");
        returnDateLabel2.setFont(new java.awt.Font("Segoe UI", 1, 13));
        returnDateLabel2.setText("Ngày trả thực tế:");
        lateFeeLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lateFeeLabel.setText("Phí phạt:");

        returnBookIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        returnBookIdField.setColumns(20);
        returnDateField2.setFont(new java.awt.Font("Segoe UI", 0, 13));
        returnDateField2.setColumns(20);
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
        returnDateField2.setText(dateFormat.format(new java.util.Date()));
        lateFeeField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lateFeeField.setEditable(false);
        lateFeeField.setColumns(20);

        returnButton.setBackground(new java.awt.Color(215, 100, 50));
        returnButton.setForeground(new java.awt.Color(255, 255, 255));
        returnButton.setText("✓ Trả sách");
        returnButton.setFocusPainted(false);
        returnButton.addActionListener(e -> returnBook());

        refreshButton.setBackground(new java.awt.Color(100, 100, 100));
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setText("🔄 Làm mới");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshBorrowedList());

        javax.swing.JPanel returnCenterPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 15));
        returnCenterPanel.setBackground(new java.awt.Color(245, 245, 250));
        returnCenterPanel.add(borrowedBooksTable, java.awt.BorderLayout.CENTER);
        returnCenterPanel.add(returnFormPanel, java.awt.BorderLayout.SOUTH);
        
        javax.swing.JLabel returnTableLabel = new javax.swing.JLabel("Danh sách sách đang mượn:");
        returnTableLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        returnTableLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));
        returnCenterPanel.add(returnTableLabel, java.awt.BorderLayout.NORTH);
        
        returnPanel.add(returnCenterPanel, java.awt.BorderLayout.CENTER);

        // Thêm các tab vào TabbedPane
        tabbedPane.addTab("Mượn sách", borrowPanel);
        tabbedPane.addTab("Trả sách", returnPanel);

        add(tabbedPane, java.awt.BorderLayout.CENTER);
    }

    private void loadAvailableBooks() {
        List<BookService.Book> books = bookService.getAllBooks();
        String[] columns = {"Mã sách", "Tên sách", "Tác giả", "Nhà xuất bản", "Số lượng", "Trạng thái"};
        Object[][] data = new Object[books.size()][6];
        for (int i = 0; i < books.size(); i++) {
            BookService.Book book = books.get(i);
            data[i][0] = book.bookId;
            data[i][1] = book.title;
            data[i][2] = book.author;
            data[i][3] = book.publisher;
            data[i][4] = book.stock;
            data[i][5] = book.stock > 0 ? "Có sẵn" : "Hết sách";
        }
        booksTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    private void loadBorrowedBooks() {
        List<BorrowService.BorrowRecord> records = borrowService.getBorrowedBooksByCard(currentCardId);
        String[] columns = {"ID", "Mã sách", "Tên sách", "Ngày mượn", "Ngày trả dự kiến", "Ngày trả thực tế", "Phí phạt", "Trạng thái"};
        Object[][] data = new Object[records.size()][8];
        for (int i = 0; i < records.size(); i++) {
            BorrowService.BorrowRecord record = records.get(i);
            BookService.Book book = bookService.getBookById(record.bookId);
            data[i][0] = record.id;
            data[i][1] = record.bookId;
            data[i][2] = book != null ? book.title : record.bookId;
            data[i][3] = formatDate(record.borrowDate);
            data[i][4] = formatDate(record.dueDate);
            data[i][5] = record.returnDate != null ? formatDate(record.returnDate) : "";
            data[i][6] = String.format("%.0f đ", record.fine);
            data[i][7] = record.status;
        }
        borrowedTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void searchBook() {
        String bookId = bookIdField.getText().trim();
        if (bookId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập mã sách!", "Thông báo", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        BookService.Book book = bookService.getBookById(bookId);
        if (book != null) {
            bookIdField.setText(book.bookId);
            bookNameField.setText(book.title);
            authorField.setText(book.author);
            borrowDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            // Default 7 days
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, 7);
            returnDateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime()));
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy sách với mã: " + bookId, "Thông báo", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    private void borrowBook() {
        String bookId = bookIdField.getText().trim();
        if (bookId.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để mượn!", "Thông báo", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        BookService.Book book = bookService.getBookById(bookId);
        if (book == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy sách!", "Lỗi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (book.stock <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Sách đã hết!", "Thông báo", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Default 7 days
        if (borrowService.borrowBook(currentCardId, bookId, 7)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Mượn sách thành công!", "Thông báo", 
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadAvailableBooks();
            loadBorrowedBooks();
            // Clear fields
            bookIdField.setText("");
            bookNameField.setText("");
            authorField.setText("");
            borrowDateField.setText("");
            returnDateField.setText("");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi mượn sách!", "Lỗi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook() {
        int selectedRow = borrowedTable.getSelectedRow();
        if (selectedRow < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để trả!", "Thông báo", 
                javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int borrowId = Integer.parseInt(borrowedTable.getValueAt(selectedRow, 0).toString());
        
        if (borrowService.returnBook(borrowId, currentCardId)) {
            // Calculate fine
            double fine = borrowService.calculateFine(borrowId);
            if (fine > 0) {
                lateFeeField.setText(String.format("%.0f đ", fine));
            }
            
            javax.swing.JOptionPane.showMessageDialog(this, "Trả sách thành công!", "Thông báo", 
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
            loadAvailableBooks();
            loadBorrowedBooks();
            // Clear fields
            returnBookIdField.setText("");
            returnDateField2.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            lateFeeField.setText("0 đ");
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi trả sách!", "Lỗi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshBorrowedList() {
        loadBorrowedBooks();
        javax.swing.JOptionPane.showMessageDialog(this, "Đã làm mới danh sách!", "Thông báo", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel borrowPanel;
    private javax.swing.JLabel borrowTitleLabel;
    private javax.swing.JLabel cardIdLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JLabel bookIdLabel;
    private javax.swing.JTextField bookIdField;
    private javax.swing.JLabel bookNameLabel;
    private javax.swing.JTextField bookNameField;
    private javax.swing.JLabel authorLabel;
    private javax.swing.JTextField authorField;
    private javax.swing.JLabel borrowDateLabel;
    private javax.swing.JTextField borrowDateField;
    private javax.swing.JLabel returnDateLabel;
    private javax.swing.JTextField returnDateField;
    private javax.swing.JButton borrowButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JScrollPane availableBooksTable;
    private javax.swing.JTable booksTable;
    private javax.swing.JPanel returnPanel;
    private javax.swing.JLabel returnTitleLabel;
    private javax.swing.JScrollPane borrowedBooksTable;
    private javax.swing.JTable borrowedTable;
    private javax.swing.JLabel returnBookIdLabel;
    private javax.swing.JTextField returnBookIdField;
    private javax.swing.JLabel returnDateLabel2;
    private javax.swing.JTextField returnDateField2;
    private javax.swing.JLabel lateFeeLabel;
    private javax.swing.JTextField lateFeeField;
    private javax.swing.JButton returnButton;
    private javax.swing.JButton refreshButton;
}
