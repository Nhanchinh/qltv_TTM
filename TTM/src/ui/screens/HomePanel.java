/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.BorrowService;
import services.TransactionService;
import services.CardService;
import ui.DBConnect;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author admin
 */
public class HomePanel extends javax.swing.JPanel {
    
    private BorrowService borrowService;
    private TransactionService transactionService;
    private CardService cardService;
    private String currentCardId = "CARD001";

    /**
     * Creates new form HomePanel
     */
    public HomePanel() {
        borrowService = new BorrowService();
        transactionService = new TransactionService();
        cardService = new CardService();
        initComponents();
        loadStats();
    }
    
    /**
     * Load statistics from database
     */
    private void loadStats() {
        // Card 1: Sách đang mượn
        List<BorrowService.BorrowRecord> borrowedBooks = borrowService.getBorrowedBooksByCard(currentCardId);
        int currentlyBorrowed = borrowedBooks != null ? borrowedBooks.size() : 0;
        card1Value.setText(String.valueOf(currentlyBorrowed));
        
        // Card 2: Sách đã mượn (đã trả)
        int totalReturned = 0;
        try {
            Connection conn = DBConnect.getConnection();
            if (conn != null) {
                String sql = "SELECT COUNT(*) as count FROM BorrowHistory WHERE CardID = ? AND Status = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, currentCardId);
                    pstmt.setString(2, "đã trả");
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            totalReturned = rs.getInt("count");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi dem sach da muon: " + e.getMessage());
        }
        card2Value.setText(String.valueOf(totalReturned));
        
        // Card 3: Số dư tài khoản
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
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        card3Value.setText(nf.format(balance) + " đ");
        
        // Card 4: Giao dịch tháng này
        int transactionsThisMonth = 0;
        if (transactions != null) {
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            for (TransactionService.Transaction t : transactions) {
                try {
                    // DateTime format: "2024-01-15T10:30:00"
                    String dateTime = t.dateTime;
                    if (dateTime != null && dateTime.length() >= 7) {
                        String yearMonth = dateTime.substring(0, 7); // "2024-01"
                        String[] parts = yearMonth.split("-");
                        if (parts.length == 2) {
                            int year = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]);
                            if (year == currentYear && month == currentMonth) {
                                transactionsThisMonth++;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
        }
        card4Value.setText(String.valueOf(transactionsThisMonth));
    }
    
    /**
     * Reload statistics (public method for external refresh)
     */
    public void reloadStats() {
        loadStats();
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        welcomeLabel = new javax.swing.JLabel();
        subtitleLabel = new javax.swing.JLabel();
        statsPanel = new javax.swing.JPanel();
        card1 = new javax.swing.JPanel();
        card1Label = new javax.swing.JLabel();
        card1Value = new javax.swing.JLabel();
        card2 = new javax.swing.JPanel();
        card2Label = new javax.swing.JLabel();
        card2Value = new javax.swing.JLabel();
        card3 = new javax.swing.JPanel();
        card3Label = new javax.swing.JLabel();
        card3Value = new javax.swing.JLabel();
        card4 = new javax.swing.JPanel();
        card4Label = new javax.swing.JLabel();
        card4Value = new javax.swing.JLabel();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 30));

        // Welcome Section
        javax.swing.JPanel welcomePanel = new javax.swing.JPanel();
        welcomePanel.setBackground(new java.awt.Color(245, 245, 250));
        welcomePanel.setLayout(new java.awt.BorderLayout());
        welcomePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 40, 20, 40));

        welcomeLabel.setFont(new java.awt.Font("Segoe UI", 1, 32));
        welcomeLabel.setForeground(new java.awt.Color(45, 45, 48));
        welcomeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        welcomeLabel.setText("Chào mừng bạn quay trở lại!");

        subtitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 16));
        subtitleLabel.setForeground(new java.awt.Color(100, 100, 100));
        subtitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        subtitleLabel.setText("Hệ thống quản lý thư viện TTM");

        welcomePanel.add(welcomeLabel, java.awt.BorderLayout.CENTER);
        welcomePanel.add(subtitleLabel, java.awt.BorderLayout.SOUTH);

        // Stats Cards Panel
        statsPanel.setBackground(new java.awt.Color(245, 245, 250));
        statsPanel.setLayout(new java.awt.GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 40, 40));

        // Card 1 - Sách đang mượn
        card1.setBackground(new java.awt.Color(255, 255, 255));
        card1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230), 1),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card1.setLayout(new java.awt.BorderLayout(0, 10));

        card1Label.setFont(new java.awt.Font("Segoe UI", 0, 14));
        card1Label.setForeground(new java.awt.Color(100, 100, 100));
        card1Label.setText("Sách đang mượn");

        card1Value.setFont(new java.awt.Font("Segoe UI", 1, 36));
        card1Value.setForeground(new java.awt.Color(0, 120, 215));
        card1Value.setText("0");

        card1.add(card1Label, java.awt.BorderLayout.NORTH);
        card1.add(card1Value, java.awt.BorderLayout.CENTER);

        // Card 2 - Sách đã mượn
        card2.setBackground(new java.awt.Color(255, 255, 255));
        card2.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230), 1),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card2.setLayout(new java.awt.BorderLayout(0, 10));

        card2Label.setFont(new java.awt.Font("Segoe UI", 0, 14));
        card2Label.setForeground(new java.awt.Color(100, 100, 100));
        card2Label.setText("Sách đã mượn");

        card2Value.setFont(new java.awt.Font("Segoe UI", 1, 36));
        card2Value.setForeground(new java.awt.Color(50, 150, 50));
        card2Value.setText("0");

        card2.add(card2Label, java.awt.BorderLayout.NORTH);
        card2.add(card2Value, java.awt.BorderLayout.CENTER);

        // Card 3 - Số tiền trong tài khoản
        card3.setBackground(new java.awt.Color(255, 255, 255));
        card3.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230), 1),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card3.setLayout(new java.awt.BorderLayout(0, 10));

        card3Label.setFont(new java.awt.Font("Segoe UI", 0, 14));
        card3Label.setForeground(new java.awt.Color(100, 100, 100));
        card3Label.setText("Số dư tài khoản");

        card3Value.setFont(new java.awt.Font("Segoe UI", 1, 36));
        card3Value.setForeground(new java.awt.Color(255, 140, 0));
        card3Value.setText("0 đ");

        card3.add(card3Label, java.awt.BorderLayout.NORTH);
        card3.add(card3Value, java.awt.BorderLayout.CENTER);

        // Card 4 - Lịch sử giao dịch
        card4.setBackground(new java.awt.Color(255, 255, 255));
        card4.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230), 1),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        card4.setLayout(new java.awt.BorderLayout(0, 10));

        card4Label.setFont(new java.awt.Font("Segoe UI", 0, 14));
        card4Label.setForeground(new java.awt.Color(100, 100, 100));
        card4Label.setText("Giao dịch tháng này");

        card4Value.setFont(new java.awt.Font("Segoe UI", 1, 36));
        card4Value.setForeground(new java.awt.Color(150, 50, 150));
        card4Value.setText("0");

        card4.add(card4Label, java.awt.BorderLayout.NORTH);
        card4.add(card4Value, java.awt.BorderLayout.CENTER);

        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);
        statsPanel.add(card4);

        add(welcomePanel, java.awt.BorderLayout.NORTH);
        add(statsPanel, java.awt.BorderLayout.CENTER);
    }

    // Variables declaration
    private javax.swing.JPanel card1;
    private javax.swing.JLabel card1Label;
    private javax.swing.JLabel card1Value;
    private javax.swing.JPanel card2;
    private javax.swing.JLabel card2Label;
    private javax.swing.JLabel card2Value;
    private javax.swing.JPanel card3;
    private javax.swing.JLabel card3Label;
    private javax.swing.JLabel card3Value;
    private javax.swing.JPanel card4;
    private javax.swing.JLabel card4Label;
    private javax.swing.JLabel card4Value;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JLabel subtitleLabel;
    private javax.swing.JLabel welcomeLabel;
}

