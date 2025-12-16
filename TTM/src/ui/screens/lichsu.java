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
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import javax.swing.JLabel;

/**
 * Modern History Screen
 */
public class lichsu extends javax.swing.JPanel {

    // Services
    private BorrowService borrowService;
    private PurchaseService purchaseService;
    private StationeryService stationeryService;
    private TransactionService transactionService;
    private BookService bookService;
    private String currentCardId = "CARD001";

    // Custom Tab System
    private javax.swing.JPanel tabBar;
    private javax.swing.JPanel contentPanel; // CardLayout
    private java.awt.CardLayout cardLayout;
    private javax.swing.JButton activeTabButton;

    public lichsu() {
        borrowService = new BorrowService();
        purchaseService = new PurchaseService();
        stationeryService = new StationeryService();
        transactionService = new TransactionService();
        bookService = new BookService();

        initComponents();
        loadAllHistory();
    }

    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            loadAllHistory();
        }
    }

    private void initComponents() {
        setBackground(new java.awt.Color(248, 250, 252)); // Slate 50
        setLayout(new java.awt.BorderLayout(0, 0));

        // 1. Header
        add(createHeaderPanel(), java.awt.BorderLayout.NORTH);

        // 2. Main Wrapper
        javax.swing.JPanel mainWrapper = new javax.swing.JPanel(new java.awt.BorderLayout(0, 20));
        mainWrapper.setOpaque(false);
        mainWrapper.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 0, 30));

        // 3. Content (CardLayout) - MUST BE INTIALIZED BEFORE TAB BAR
        cardLayout = new java.awt.CardLayout();
        contentPanel = new javax.swing.JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // Add Panels
        contentPanel.add(createBorrowHistoryPanel(), "BORROW");
        contentPanel.add(createPurchaseHistoryPanel(), "PURCHASE");
        contentPanel.add(createTransactionHistoryPanel(), "TRANS");

        // 4. Custom Tab Bar - USES CARD LAYOUT
        mainWrapper.add(createCustomTabBar(), java.awt.BorderLayout.NORTH);

        mainWrapper.add(contentPanel, java.awt.BorderLayout.CENTER);

        add(mainWrapper, java.awt.BorderLayout.CENTER);
    }

    private javax.swing.JPanel createCustomTabBar() {
        tabBar = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        tabBar.setOpaque(false);

        // Create Tab Buttons
        javax.swing.JButton tabBorrow = createTabButton("MƯỢN TRẢ", "BORROW");
        javax.swing.JButton tabPurchase = createTabButton("MUA SẮM", "PURCHASE");
        javax.swing.JButton tabTrans = createTabButton("GIAO DỊCH", "TRANS");

        tabBar.add(tabBorrow);
        tabBar.add(tabPurchase);
        tabBar.add(tabTrans);

        // Activate first tab
        setActiveTab(tabBorrow, "BORROW");

        return tabBar;
    }

    private javax.swing.JButton createTabButton(String text, String cardName) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btn.setForeground(new java.awt.Color(100, 116, 139)); // Default gray
        btn.setBackground(new java.awt.Color(248, 250, 252));
        btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btn.addActionListener(e -> setActiveTab(btn, cardName));
        return btn;
    }

    private void setActiveTab(javax.swing.JButton btn, String cardName) {
        // Reset old active button
        if (activeTabButton != null) {
            activeTabButton.setForeground(new java.awt.Color(100, 116, 139));
            activeTabButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }

        // Set new active button style
        activeTabButton = btn;
        activeTabButton.setForeground(new java.awt.Color(15, 23, 42)); // Darker
        // Bottom border indicator
        activeTabButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 3, 0, new java.awt.Color(37, 99, 235)), // Blue line
                javax.swing.BorderFactory.createEmptyBorder(10, 20, 7, 20)));

        // Switch card
        if (cardLayout != null) {
            cardLayout.show(contentPanel, cardName);
        }
    }

    // Header only (Removed Summary Panel to declutter as requested)
    private javax.swing.JPanel createHeaderPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));
        p.setPreferredSize(new java.awt.Dimension(0, 70));

        javax.swing.JLabel title = new javax.swing.JLabel("Lịch Sử Hoạt Động");
        title.setFont(new java.awt.Font("Segoe UI", 1, 24));
        title.setForeground(new java.awt.Color(15, 23, 42));
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));

        p.add(title, java.awt.BorderLayout.WEST);

        javax.swing.JPanel actions = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 15));
        actions.setOpaque(false);

        javax.swing.JButton btnRefresh = createModernButton("Làm mới", new java.awt.Color(59, 130, 246),
                new java.awt.Color(239, 246, 255));
        btnRefresh.setPreferredSize(new java.awt.Dimension(100, 36));
        btnRefresh.addActionListener(e -> loadAllHistory());
        actions.add(btnRefresh);

        p.add(actions, java.awt.BorderLayout.EAST);

        return p;
    }

    // 1. Borrow Panel
    private javax.swing.JScrollPane borrowTableScroll;
    private javax.swing.JTable borrowTable;

    private javax.swing.JPanel createBorrowHistoryPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));

        String[] cols = { "MÃ SÁCH", "TÊN SÁCH", "NGÀY MƯỢN", "HẠN TRẢ", "NGÀY TRẢ", "TRẠNG THÁI" };
        borrowTableScroll = createStyledTable(new Object[][] {}, cols);
        borrowTable = (javax.swing.JTable) borrowTableScroll.getViewport().getView();

        // Custom Status Renderer
        borrowTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        p.add(borrowTableScroll, java.awt.BorderLayout.CENTER);
        return p;
    }

    // 2. Purchase Panel
    private javax.swing.JScrollPane purchaseTableScroll;
    private javax.swing.JTable purchaseTable;

    private javax.swing.JPanel createPurchaseHistoryPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));

        String[] cols = { "LOẠI", "MÃ SP/SÁCH", "SỐ LƯỢNG", "ĐƠN GIÁ", "TỔNG TIỀN", "NGÀY MUA" };
        purchaseTableScroll = createStyledTable(new Object[][] {}, cols);
        purchaseTable = (javax.swing.JTable) purchaseTableScroll.getViewport().getView();

        // Right align money columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        purchaseTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        purchaseTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        p.add(purchaseTableScroll, java.awt.BorderLayout.CENTER);
        return p;
    }

    // 3. Transaction Panel
    private javax.swing.JScrollPane transTableScroll;
    private javax.swing.JTable transTable;

    private javax.swing.JPanel createTransactionHistoryPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setOpaque(false);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));

        String[] cols = { "MÃ GD", "LOẠI GD", "SỐ TIỀN", "THỜI GIAN" };
        transTableScroll = createStyledTable(new Object[][] {}, cols);
        transTable = (javax.swing.JTable) transTableScroll.getViewport().getView();

        // Custom Renderer for Amount (+ Green / - Red)
        transTable.getColumnModel().getColumn(2).setCellRenderer(new AmountCellRenderer());

        p.add(transTableScroll, java.awt.BorderLayout.CENTER);
        return p;
    }

    // --- Data Loading ---

    private void loadAllHistory() {
        loadBorrowHistory();
        loadPurchaseHistory();
        loadTransactionHistory();
    }

    private void loadBorrowHistory() {
        List<BorrowService.BorrowRecord> records = borrowService.getAllBorrowHistory(currentCardId);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) borrowTable.getModel();
        model.setRowCount(0);

        for (BorrowService.BorrowRecord r : records) {
            BookService.Book book = bookService.getBookById(r.bookId);
            model.addRow(new Object[] {
                    r.bookId,
                    book != null ? book.title : "Unknown",
                    formatDate(r.borrowDate),
                    formatDate(r.dueDate),
                    r.returnDate != null ? formatDate(r.returnDate) : "--",
                    r.status // Renderer will handle visual
            });
        }
    }

    private void loadPurchaseHistory() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) purchaseTable.getModel();
        model.setRowCount(0);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        // Books
        List<PurchaseService.PurchaseRecord> purchases = purchaseService.getPurchaseHistory(currentCardId);
        for (PurchaseService.PurchaseRecord p : purchases) {
            model.addRow(new Object[] {
                    "Sách", p.bookId, p.quantity, nf.format(p.unitPrice), nf.format(p.finalPrice),
                    formatDateTime(p.purchaseDate)
            });
        }

        // Stationery
        List<StationeryService.SaleRecord> sales = stationeryService.getSaleHistory(currentCardId);
        for (StationeryService.SaleRecord s : sales) {
            model.addRow(new Object[] {
                    "VPP", s.itemId, s.quantity, nf.format(s.unitPrice), nf.format(s.finalPrice),
                    formatDateTime(s.saleDate)
            });
        }
    }

    private void loadTransactionHistory() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) transTable.getModel();
        model.setRowCount(0);

        List<TransactionService.Transaction> trans = transactionService.getTransactionsByCard(currentCardId);
        for (TransactionService.Transaction t : trans) {
            String typeDisplay = t.type;
            if (t.type.equals("Deposit"))
                typeDisplay = "Nạp tiền";
            else if (t.type.equals("Payment"))
                typeDisplay = "Thanh toán";
            else if (t.type.equals("MembershipFee"))
                typeDisplay = "Phí hội viên";

            // Raw amount, renderer puts sign
            model.addRow(new Object[] {
                    t.transId, typeDisplay, t.amount, formatDateTime(t.dateTime)
            });
        }
    }

    // --- Custom Renderers ---

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = (String) value;
            c.setFont(new java.awt.Font("Segoe UI", 1, 13));
            if (status != null) {
                if (status.equalsIgnoreCase("Đang mượn") || status.equalsIgnoreCase("mượn")
                        || status.equalsIgnoreCase("Borrowed")) {
                    c.setForeground(new java.awt.Color(245, 158, 11)); // Amber
                    c.setText("⏳ Đang mượn");
                } else if (status.equalsIgnoreCase("Đã trả") || status.equalsIgnoreCase("tra")
                        || status.equalsIgnoreCase("Returned")) {
                    c.setForeground(new java.awt.Color(16, 185, 129)); // Emerald
                    c.setText("✔ Đã trả");
                } else {
                    c.setForeground(new java.awt.Color(239, 68, 68)); // Red
                    c.setText("⚠ " + status);
                }
            }
            return c;
        }
    }

    class AmountCellRenderer extends DefaultTableCellRenderer {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        @Override
        public Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Double) {
                double amount = (Double) value;
                String type = (String) table.getModel().getValueAt(row, 1); // Get Type

                if (type.contains("Nạp")) {
                    c.setForeground(new java.awt.Color(22, 163, 74));
                    c.setText("+" + nf.format(Math.abs(amount)));
                } else {
                    c.setForeground(new java.awt.Color(220, 38, 38));
                    c.setText("-" + nf.format(Math.abs(amount)));
                }
                c.setFont(new java.awt.Font("Segoe UI", 1, 14));
            }
            return c;
        }
    }

    // --- Helpers ---
    private javax.swing.JScrollPane createStyledTable(Object[][] data, String[] columns) {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        javax.swing.JTable table = new javax.swing.JTable(model);
        table.setRowHeight(45); // Taller rows
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new java.awt.Color(241, 245, 249));
        table.setFont(new java.awt.Font("Segoe UI", 0, 14));
        table.setSelectionBackground(new java.awt.Color(239, 246, 255));
        table.setSelectionForeground(new java.awt.Color(30, 58, 138));

        // Header style
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(new java.awt.Font("Segoe UI", 1, 12));
        header.setForeground(new java.awt.Color(100, 116, 139));
        header.setBackground(java.awt.Color.WHITE);
        header.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(226, 232, 240)));
        header.setPreferredSize(new java.awt.Dimension(0, 40));

        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        scroll.getViewport().setBackground(java.awt.Color.WHITE);

        return scroll;
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

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty())
            return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            return outputFormat.format(inputFormat.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String formatDateTime(String dt) {
        if (dt == null || dt.isEmpty())
            return "";
        try {
            String normalized = dt.replace("T", " ");
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Adjust if DB format varies
            try {
                return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(inputFormat.parse(normalized));
            } catch (Exception e2) {
                return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.time.LocalDateTime.parse(dt));
            }
        } catch (Exception e) {
            return dt;
        }
    }
}
