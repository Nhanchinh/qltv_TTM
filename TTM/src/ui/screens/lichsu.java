/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.BorrowService;
import services.PurchaseService;
import services.StationeryService;
import services.TransactionService;
import services.BookService;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author admin
 */
public class lichsu extends javax.swing.JPanel {
    
    private BorrowService borrowService;
    private PurchaseService purchaseService;
    private StationeryService stationeryService;
    private TransactionService transactionService;
    private BookService bookService;
    private String currentCardId = "CARD001";

    /**
     * Creates new form lichsu
     */
    public lichsu() {
        borrowService = new BorrowService();
        purchaseService = new PurchaseService();
        stationeryService = new StationeryService();
        transactionService = new TransactionService();
        bookService = new BookService();
        initComponents();
        loadAllHistory();
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
        
        // Filter panel
        filterPanel = new javax.swing.JPanel();
        filterTitle = new javax.swing.JLabel();
        transactionTypeLabel = new javax.swing.JLabel();
        transactionTypeCombo = new javax.swing.JComboBox<>();
        dateFromLabel = new javax.swing.JLabel();
        dateFromField = new javax.swing.JTextField();
        dateToLabel = new javax.swing.JLabel();
        dateToField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        
        // Tabbed pane for different history types
        historyTabbedPane = new javax.swing.JTabbedPane();
        
        // Borrow/Return history
        borrowHistoryPanel = new javax.swing.JPanel();
        borrowHistoryTable = new javax.swing.JScrollPane();
        borrowTable = new javax.swing.JTable();
        
        // Payment history
        paymentHistoryPanel = new javax.swing.JPanel();
        paymentHistoryTable = new javax.swing.JScrollPane();
        paymentTable = new javax.swing.JTable();
        
        // Top up history
        topUpHistoryPanel = new javax.swing.JPanel();
        topUpHistoryTable = new javax.swing.JScrollPane();
        topUpTable = new javax.swing.JTable();
        
        // Summary panel
        summaryPanel = new javax.swing.JPanel();
        summaryTitle = new javax.swing.JLabel();
        totalBorrowsLabel = new javax.swing.JLabel();
        totalBorrowsField = new javax.swing.JTextField();
        totalPaymentsLabel = new javax.swing.JLabel();
        totalPaymentsField = new javax.swing.JTextField();
        totalTopUpsLabel = new javax.swing.JLabel();
        totalTopUpsField = new javax.swing.JTextField();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 20));

        // Title
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Lịch sử giao dịch");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        mainContainer.setLayout(new java.awt.BorderLayout(0, 20));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 30, 40));
        mainContainer.setBackground(new java.awt.Color(245, 245, 250));

        // ============ FILTER PANEL ============
        filterPanel.setBackground(new java.awt.Color(255, 255, 255));
        filterPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Bộ lọc",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        filterPanel.setLayout(new java.awt.BorderLayout(0, 10));

        javax.swing.JPanel filterFormPanel = new javax.swing.JPanel();
        filterFormPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 5));
        filterFormPanel.setBackground(new java.awt.Color(255, 255, 255));

        transactionTypeLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        transactionTypeLabel.setText("Loại giao dịch:");
        transactionTypeCombo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        transactionTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "Tất cả", "Mượn/Trả sách", "Mua sách", "Mua VPP", "Nạp tiền", "Phí hội viên"
        }));

        dateFromLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        dateFromLabel.setText("Từ ngày:");
        dateFromField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        dateFromField.setColumns(12);
        dateFromField.setText("01/01/2024");

        dateToLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        dateToLabel.setText("Đến ngày:");
        dateToField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        dateToField.setColumns(12);
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
        dateToField.setText(dateFormat.format(new java.util.Date()));

        searchButton.setBackground(new java.awt.Color(0, 120, 215));
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setText("🔍 Tìm kiếm");
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchHistory());

        refreshButton.setBackground(new java.awt.Color(100, 100, 100));
        refreshButton.setForeground(new java.awt.Color(255, 255, 255));
        refreshButton.setText("🔄 Làm mới");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshHistory());

        filterFormPanel.add(transactionTypeLabel);
        filterFormPanel.add(transactionTypeCombo);
        filterFormPanel.add(dateFromLabel);
        filterFormPanel.add(dateFromField);
        filterFormPanel.add(dateToLabel);
        filterFormPanel.add(dateToField);
        filterFormPanel.add(searchButton);
        filterFormPanel.add(refreshButton);

        filterPanel.add(filterFormPanel, java.awt.BorderLayout.CENTER);

        // ============ HISTORY TABBED PANE ============
        
        // Borrow/Return History
        borrowHistoryPanel.setLayout(new java.awt.BorderLayout());
        borrowHistoryPanel.setBackground(new java.awt.Color(255, 255, 255));

        // Theo bảng BorrowHistory: ID, CardID, BookID, BorrowDate, DueDate, ReturnDate, Fine, Status
        String[] borrowColumns = {"ID", "Mã thẻ", "Mã sách", "Ngày mượn", "Ngày trả dự kiến", "Ngày trả thực tế", "Phí phạt", "Trạng thái"};
        Object[][] borrowData = {
            {"1", "CARD001", "BK001", "01/01/2024", "15/01/2024", "15/01/2024", "0 đ", "đã trả"},
            {"2", "CARD001", "BK002", "10/01/2024", "25/01/2024", "", "0 đ", "mượn"},
            {"3", "CARD002", "BK001", "15/01/2024", "15/01/2024", "15/01/2024", "0 đ", "đã trả"}
        };
        borrowTable = new javax.swing.JTable(borrowData, borrowColumns);
        borrowTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        borrowTable.setRowHeight(25);
        borrowHistoryTable.setViewportView(borrowTable);
        borrowHistoryPanel.add(borrowHistoryTable, java.awt.BorderLayout.CENTER);

        // Payment History
        paymentHistoryPanel.setLayout(new java.awt.BorderLayout());
        paymentHistoryPanel.setBackground(new java.awt.Color(255, 255, 255));

        // Tab này hiển thị PurchaseBookHistory và StationerySales
        // PurchaseBookHistory: ID, CardID, BookID, Quantity, UnitPrice, DiscountPercent, FinalPrice, PointsEarned, PurchaseDate
        String[] paymentColumns = {"Loại", "ID", "Mã thẻ", "Mã SP/Sách", "Số lượng", "Đơn giá", "Giảm giá (%)", "Tổng tiền", "Điểm", "Ngày"};
        Object[][] paymentData = {
            {"Mua sách", "1", "CARD001", "BK001", "1", "200,000 đ", "10%", "180,000 đ", "180", "05/01/2024"},
            {"Mua sách", "2", "CARD002", "BK002", "2", "300,000 đ", "5%", "570,000 đ", "570", "06/01/2024"},
            {"Mua VPP", "1", "CARD001", "ITEM001", "5", "5,000 đ", "0%", "25,000 đ", "0", "08/01/2024"},
            {"Mua VPP", "2", "CARD002", "ITEM002", "3", "20,000 đ", "0%", "60,000 đ", "-10", "09/01/2024"}
        };
        paymentTable = new javax.swing.JTable(paymentData, paymentColumns);
        paymentTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        paymentTable.setRowHeight(25);
        paymentHistoryTable.setViewportView(paymentTable);
        paymentHistoryPanel.add(paymentHistoryTable, java.awt.BorderLayout.CENTER);

        // Top Up History
        topUpHistoryPanel.setLayout(new java.awt.BorderLayout());
        topUpHistoryPanel.setBackground(new java.awt.Color(255, 255, 255));

        // Theo bảng Transactions: TransID, CardID, Type, Amount, PointsChanged, DateTime
        String[] topUpColumns = {"Mã giao dịch", "Mã thẻ", "Loại", "Số tiền", "Điểm thay đổi", "Ngày giờ"};
        Object[][] topUpData = {
            {"TXN001", "CARD001", "Deposit", "500,000 đ", "0", "03/01/2024 10:30:00"},
            {"TXN002", "CARD001", "Deposit", "1,000,000 đ", "0", "10/01/2024 14:20:00"},
            {"TXN003", "CARD002", "Payment", "-200,000 đ", "200", "15/01/2024 09:15:00"},
            {"TXN004", "CARD002", "Deposit", "2,000,000 đ", "0", "20/01/2024 16:45:00"}
        };
        topUpTable = new javax.swing.JTable(topUpData, topUpColumns);
        topUpTable.setFont(new java.awt.Font("Segoe UI", 0, 12));
        topUpTable.setRowHeight(25);
        topUpHistoryTable.setViewportView(topUpTable);
        topUpHistoryPanel.add(topUpHistoryTable, java.awt.BorderLayout.CENTER);

        historyTabbedPane.addTab("Mượn/Trả sách", borrowHistoryPanel);
        historyTabbedPane.addTab("Mua hàng (Sách/VPP)", paymentHistoryPanel);
        historyTabbedPane.addTab("Giao dịch", topUpHistoryPanel);

        // ============ SUMMARY PANEL ============
        summaryPanel.setBackground(new java.awt.Color(255, 255, 255));
        summaryPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Tổng kết",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 14), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        summaryPanel.setLayout(new java.awt.BorderLayout(0, 10));

        summaryTitle.setFont(new java.awt.Font("Segoe UI", 1, 14));
        summaryTitle.setText("Thống kê giao dịch");

        javax.swing.JPanel summaryFormPanel = new javax.swing.JPanel();
        javax.swing.GroupLayout summaryLayout = new javax.swing.GroupLayout(summaryFormPanel);
        summaryFormPanel.setLayout(summaryLayout);
        summaryFormPanel.setBackground(new java.awt.Color(255, 255, 255));

        summaryLayout.setHorizontalGroup(
            summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalBorrowsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalPaymentsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalTopUpsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(totalBorrowsField)
                    .addComponent(totalPaymentsField)
                    .addComponent(totalTopUpsField))
                .addContainerGap())
        );

        summaryLayout.setVerticalGroup(
            summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(summaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalBorrowsLabel)
                    .addComponent(totalBorrowsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPaymentsLabel)
                    .addComponent(totalPaymentsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(summaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalTopUpsLabel)
                    .addComponent(totalTopUpsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        totalBorrowsLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        totalBorrowsLabel.setText("Tổng số lượt mượn:");
        totalPaymentsLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        totalPaymentsLabel.setText("Tổng số thanh toán:");
        totalTopUpsLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        totalTopUpsLabel.setText("Tổng số nạp tiền:");

        totalBorrowsField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        totalBorrowsField.setText("15");
        totalBorrowsField.setEditable(false);
        totalPaymentsField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        totalPaymentsField.setText("3");
        totalPaymentsField.setEditable(false);
        totalTopUpsField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        totalTopUpsField.setText("3");
        totalTopUpsField.setEditable(false);

        summaryPanel.add(summaryTitle, java.awt.BorderLayout.NORTH);
        summaryPanel.add(summaryFormPanel, java.awt.BorderLayout.CENTER);

        mainContainer.add(filterPanel, java.awt.BorderLayout.NORTH);
        mainContainer.add(historyTabbedPane, java.awt.BorderLayout.CENTER);
        mainContainer.add(summaryPanel, java.awt.BorderLayout.SOUTH);

        add(mainContainer, java.awt.BorderLayout.CENTER);
    }

    private void loadAllHistory() {
        loadBorrowHistory();
        loadPurchaseHistory();
        loadTransactionHistory();
        updateSummary();
    }
    
    private void loadBorrowHistory() {
        List<BorrowService.BorrowRecord> records = borrowService.getAllBorrowHistory(currentCardId);
        String[] columns = {"ID", "Mã sách", "Tên sách", "Ngày mượn", "Ngày trả dự kiến", "Ngày trả thực tế", "Phí phạt", "Trạng thái"};
        Object[][] data = new Object[records.size()][8];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        for (int i = 0; i < records.size(); i++) {
            BorrowService.BorrowRecord record = records.get(i);
            BookService.Book book = bookService.getBookById(record.bookId);
            data[i][0] = record.id;
            data[i][1] = record.bookId;
            data[i][2] = book != null ? book.title : record.bookId;
            data[i][3] = formatDate(record.borrowDate);
            data[i][4] = formatDate(record.dueDate);
            data[i][5] = record.returnDate != null ? formatDate(record.returnDate) : "";
            data[i][6] = nf.format(record.fine) + " đ";
            data[i][7] = record.status;
        }
        borrowTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    private void loadPurchaseHistory() {
        List<PurchaseService.PurchaseRecord> purchases = purchaseService.getPurchaseHistory(currentCardId);
        List<StationeryService.SaleRecord> sales = stationeryService.getSaleHistory(currentCardId);
        
        String[] columns = {"Loại", "ID", "Mã thẻ", "Mã SP/Sách", "Số lượng", "Đơn giá", "Giảm giá (%)", "Tổng tiền", "Điểm", "Ngày"};
        Object[][] data = new Object[purchases.size() + sales.size()][10];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        int idx = 0;
        
        for (PurchaseService.PurchaseRecord p : purchases) {
            data[idx][0] = "Mua sách";
            data[idx][1] = p.id;
            data[idx][2] = p.cardId;
            data[idx][3] = p.bookId;
            data[idx][4] = p.quantity;
            data[idx][5] = nf.format(p.unitPrice) + " đ";
            data[idx][6] = String.format("%.0f%%", p.discountPercent);
            data[idx][7] = nf.format(p.finalPrice) + " đ";
            data[idx][8] = p.pointsEarned;
            data[idx][9] = formatDateTime(p.purchaseDate);
            idx++;
        }
        
        for (StationeryService.SaleRecord s : sales) {
            data[idx][0] = "Mua VPP";
            data[idx][1] = s.id;
            data[idx][2] = s.cardId;
            data[idx][3] = s.itemId;
            data[idx][4] = s.quantity;
            data[idx][5] = nf.format(s.unitPrice) + " đ";
            data[idx][6] = "0%";
            data[idx][7] = nf.format(s.finalPrice) + " đ";
            data[idx][8] = -s.pointsUsed;
            data[idx][9] = formatDateTime(s.saleDate);
            idx++;
        }
        
        paymentTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    private void loadTransactionHistory() {
        List<TransactionService.Transaction> transactions = transactionService.getTransactionsByCard(currentCardId);
        String[] columns = {"Mã giao dịch", "Mã thẻ", "Loại", "Số tiền", "Điểm thay đổi", "Ngày giờ"};
        Object[][] data = new Object[transactions.size()][6];
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        for (int i = 0; i < transactions.size(); i++) {
            TransactionService.Transaction t = transactions.get(i);
            data[i][0] = t.transId;
            data[i][1] = t.cardId;
            data[i][2] = t.type;
            data[i][3] = nf.format(t.amount) + " đ";
            data[i][4] = t.pointsChanged;
            data[i][5] = formatDateTime(t.dateTime);
        }
        topUpTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
    
    private void updateSummary() {
        List<BorrowService.BorrowRecord> borrows = borrowService.getAllBorrowHistory(currentCardId);
        List<PurchaseService.PurchaseRecord> purchases = purchaseService.getPurchaseHistory(currentCardId);
        List<StationeryService.SaleRecord> sales = stationeryService.getSaleHistory(currentCardId);
        List<TransactionService.Transaction> deposits = transactionService.getTransactionsByType(currentCardId, "Deposit");
        
        totalBorrowsField.setText(String.valueOf(borrows.size()));
        totalPaymentsField.setText(String.valueOf(purchases.size() + sales.size()));
        totalTopUpsField.setText(String.valueOf(deposits.size()));
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            return outputFormat.format(inputFormat.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "";
        try {
            String normalized = dateTimeStr.replace("T", " ");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return outputFormat.format(inputFormat.parse(normalized));
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    private void searchHistory() {
        String type = (String) transactionTypeCombo.getSelectedItem();
        loadAllHistory();
        javax.swing.JOptionPane.showMessageDialog(this, 
            "Đã tìm kiếm lịch sử theo loại: " + type,
            "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshHistory() {
        loadAllHistory();
        javax.swing.JOptionPane.showMessageDialog(this, "Đã làm mới danh sách lịch sử!", "Thông báo", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel filterTitle;
    private javax.swing.JLabel transactionTypeLabel;
    private javax.swing.JComboBox<String> transactionTypeCombo;
    private javax.swing.JLabel dateFromLabel;
    private javax.swing.JTextField dateFromField;
    private javax.swing.JLabel dateToLabel;
    private javax.swing.JTextField dateToField;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JTabbedPane historyTabbedPane;
    private javax.swing.JPanel borrowHistoryPanel;
    private javax.swing.JScrollPane borrowHistoryTable;
    private javax.swing.JTable borrowTable;
    private javax.swing.JPanel paymentHistoryPanel;
    private javax.swing.JScrollPane paymentHistoryTable;
    private javax.swing.JTable paymentTable;
    private javax.swing.JPanel topUpHistoryPanel;
    private javax.swing.JScrollPane topUpHistoryTable;
    private javax.swing.JTable topUpTable;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JLabel summaryTitle;
    private javax.swing.JLabel totalBorrowsLabel;
    private javax.swing.JTextField totalBorrowsField;
    private javax.swing.JLabel totalPaymentsLabel;
    private javax.swing.JTextField totalPaymentsField;
    private javax.swing.JLabel totalTopUpsLabel;
    private javax.swing.JTextField totalTopUpsField;
}
