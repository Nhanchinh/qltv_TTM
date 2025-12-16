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
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
import java.util.List;

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
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            loadStats(); // Reload stats với CardID mới
        }
    }

    /**
     * Load statistics from database
     */
    private void loadStats() {
        // Lấy dữ liệu từ thẻ trước
        int cardBalance = 0;
        int cardPoints = 0;
        int borrowedBooksCount = 0;
        boolean cardDataLoaded = false;

        try {
            CardConnectionManager connManager = CardConnectionManager.getInstance();
            connManager.connectCard();
            try {
                CardBalanceManager balanceManager = new CardBalanceManager(connManager.getChannel());

                // Lấy số dư và điểm từ thẻ
                CardBalanceManager.BalanceInfo balanceInfo = balanceManager.getBalance();
                if (balanceInfo.success) {
                    cardBalance = balanceInfo.balance;
                    cardPoints = balanceInfo.points;
                    cardDataLoaded = true;
                    System.out.println("[HOME] Card Balance: " + cardBalance + " VND");
                    System.out.println("[HOME] Card Points: " + cardPoints);
                }

                // Lấy danh sách sách đang mượn từ thẻ
                List<CardBalanceManager.BorrowedBook> borrowedBooks = balanceManager.getBorrowedBooks();
                borrowedBooksCount = borrowedBooks.size();
                System.out.println("[HOME] Borrowed books on card: " + borrowedBooksCount);

            } finally {
                connManager.disconnectCard();
            }
        } catch (Exception e) {
            System.err.println("[HOME] Không thể lấy dữ liệu từ thẻ: " + e.getMessage());
        }

        // Card 1: Sách đang mượn (từ thẻ)
        if (cardDataLoaded) {
            if (card1 != null)
                card1.setValue(String.valueOf(borrowedBooksCount));
        } else {
            // Fallback: lấy từ database
            List<BorrowService.BorrowRecord> borrowedBooks = borrowService.getBorrowedBooksByCard(currentCardId);
            int currentlyBorrowed = borrowedBooks != null ? borrowedBooks.size() : 0;
            if (card1 != null)
                card1.setValue(String.valueOf(currentlyBorrowed));
        }

        // Card 2: Điểm thưởng (từ thẻ)
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        if (cardDataLoaded) {
            if (card2 != null) {
                card2.setTitle("Điểm thưởng");
                card2.setValue(nf.format(cardPoints) + " điểm");
            }
        } else {
            // Fallback: Sách đã mượn (từ DB)
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
            if (card2 != null) {
                card2.setTitle("Sách đã mượn");
                card2.setValue(String.valueOf(totalReturned));
            }
        }

        // Card 3: Số dư tài khoản (từ thẻ)
        if (cardDataLoaded) {
            if (card3 != null)
                card3.setValue(nf.format(cardBalance) + " đ");
        } else {
            // Fallback: tính từ transactions trong DB
            List<TransactionService.Transaction> transactions = transactionService.getTransactionsByCard(currentCardId);
            double balance = 0;
            if (transactions != null) {
                for (TransactionService.Transaction t : transactions) {
                    if (t.type.equals("Deposit")) {
                        balance += t.amount;
                    } else if (t.type.equals("Payment")) {
                        balance += t.amount;
                    }
                }
            }
            if (card3 != null)
                card3.setValue(nf.format(balance) + " đ");
        }

        // Card 4: Giao dịch tháng này (luôn lấy từ DB)
        List<TransactionService.Transaction> transactions = transactionService.getTransactionsByCard(currentCardId);
        int transactionsThisMonth = 0;
        if (transactions != null) {
            LocalDate now = LocalDate.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            for (TransactionService.Transaction t : transactions) {
                try {
                    String dateTime = t.dateTime;
                    if (dateTime != null && dateTime.length() >= 7) {
                        String yearMonth = dateTime.substring(0, 7);
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
        if (card4 != null)
            card4.setValue(String.valueOf(transactionsThisMonth));
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
    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        welcomeLabel = new javax.swing.JLabel();
        subtitleLabel = new javax.swing.JLabel();
        statsPanel = new javax.swing.JPanel();

        // Init styled cards
        card1 = new StatsCard("Sách đang mượn", "0", "book", new java.awt.Color(37, 99, 235)); // Blue
        card2 = new StatsCard("Điểm thưởng", "0 điểm", "star", new java.awt.Color(22, 163, 74)); // Green
        card3 = new StatsCard("Số dư tài khoản", "0 đ", "wallet", new java.awt.Color(217, 119, 6)); // Orange
        card4 = new StatsCard("Giao dịch tháng này", "0", "chart", new java.awt.Color(147, 51, 234)); // Purple

        // Needed for update logic to find labels/values, though we should update
        // StatsCard directly
        // We will map logical names to the cards for easier updating

        setBackground(new java.awt.Color(248, 250, 252)); // Slate 50
        setLayout(new java.awt.BorderLayout(0, 20));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Welcome Section
        javax.swing.JPanel welcomePanel = new javax.swing.JPanel();
        welcomePanel.setBackground(new java.awt.Color(248, 250, 252));
        welcomePanel.setLayout(new javax.swing.BoxLayout(welcomePanel, javax.swing.BoxLayout.Y_AXIS));
        welcomePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 20, 0));

        welcomeLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        welcomeLabel.setForeground(new java.awt.Color(15, 23, 42)); // Slate 900
        welcomeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        welcomeLabel.setText("Chào mừng trở lại!");

        subtitleLabel.setFont(new java.awt.Font("Segoe UI", 0, 16));
        subtitleLabel.setForeground(new java.awt.Color(100, 116, 139)); // Slate 500
        subtitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        subtitleLabel.setText("Tổng quan hệ thống nhà sách của bạn");

        welcomePanel.add(welcomeLabel);
        welcomePanel.add(javax.swing.Box.createVerticalStrut(5));
        welcomePanel.add(subtitleLabel);

        // Stats Cards Panel
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new java.awt.GridLayout(2, 2, 25, 25)); // Grid with gaps

        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);
        statsPanel.add(card4);

        add(welcomePanel, java.awt.BorderLayout.NORTH);
        add(statsPanel, java.awt.BorderLayout.CENTER);
    }

    // Custom Stats Card Component
    private class StatsCard extends javax.swing.JPanel {
        private String title;
        private String value;
        private String iconType;
        private java.awt.Color accentColor;
        private javax.swing.JLabel valueLabel;
        private javax.swing.JLabel titleLabel;

        public StatsCard(String title, String initialValue, String iconType, java.awt.Color accentColor) {
            this.title = title;
            this.value = initialValue;
            this.iconType = iconType;
            this.accentColor = accentColor;

            setLayout(new java.awt.BorderLayout());
            setOpaque(false);

            initComponents();
        }

        private void initComponents() {
            // Main Container with padding
            javax.swing.JPanel container = new javax.swing.JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                    // Shadow
                    g2.setColor(new java.awt.Color(0, 0, 0, 15));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 20, 20);

                    // Background
                    g2.setColor(java.awt.Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 20, 20);

                    // Accent Line (Left border)
                    // g2.setColor(accentColor);
                    // g2.fillRoundRect(0, 0, 6, getHeight()-6, 20, 20);
                    // g2.fillRect(4, 0, 4, getHeight()-6); // Square off right side of strip

                    g2.dispose();
                }
            };
            container.setLayout(null); // Absolute layout for custom positioning
            container.setOpaque(false);

            // Icon Background Circle
            javax.swing.JPanel iconBg = new javax.swing.JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                    // Light version of accent color
                    float[] hsb = java.awt.Color.RGBtoHSB(accentColor.getRed(), accentColor.getGreen(),
                            accentColor.getBlue(), null);
                    java.awt.Color lightColor = new java.awt.Color(java.awt.Color.HSBtoRGB(hsb[0], 0.15f, 1.0f));

                    g2.setColor(lightColor);
                    g2.fillOval(0, 0, getWidth(), getHeight());

                    // Draw Icon
                    g2.setColor(accentColor);
                    g2.setStroke(new java.awt.BasicStroke(2f));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    switch (iconType) {
                        case "book":
                            g2.drawRect(cx - 6, cy - 8, 12, 16);
                            g2.drawLine(cx - 6, cy + 4, cx + 6, cy + 4);
                            break;
                        case "star":
                            int[] x = { cx, cx + 4, cx + 10, cx + 5, cx + 7, cx, cx - 7, cx - 5, cx - 10, cx - 4 };
                            int[] y = { cy - 10, cy - 3, cy - 3, cy + 3, cy + 10, cy + 6, cy + 10, cy + 3, cy - 3,
                                    cy - 3 };
                            g2.drawPolygon(x, y, 10);
                            break;
                        case "wallet":
                            g2.drawRoundRect(cx - 9, cy - 7, 18, 14, 3, 3);
                            g2.drawOval(cx, cy - 2, 4, 4);
                            break;
                        case "chart":
                            g2.drawLine(cx - 8, cy + 8, cx + 8, cy + 8);
                            g2.drawLine(cx - 5, cy + 8, cx - 5, cy);
                            g2.drawLine(cx, cy + 8, cx, cy - 6);
                            g2.drawLine(cx + 5, cy + 8, cx + 5, cy - 3);
                            break;
                    }

                    g2.dispose();
                }
            };
            iconBg.setBounds(25, 25, 50, 50);
            iconBg.setOpaque(false);
            container.add(iconBg);

            // Labels
            titleLabel = new javax.swing.JLabel(title);
            titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
            titleLabel.setForeground(new java.awt.Color(100, 116, 139));
            titleLabel.setBounds(25, 90, 200, 20);
            container.add(titleLabel);

            valueLabel = new javax.swing.JLabel(value);
            valueLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
            valueLabel.setForeground(new java.awt.Color(15, 23, 42));
            valueLabel.setBounds(25, 115, 250, 40);
            container.add(valueLabel);

            add(container, java.awt.BorderLayout.CENTER);
        }

        public void setValue(String val) {
            this.value = val;
            if (valueLabel != null)
                valueLabel.setText(val);
        }

        public void setTitle(String t) {
            this.title = t;
            if (titleLabel != null)
                titleLabel.setText(t);
        }
    }

    // Update loadStats to use the new StatsCard setters
    // We need to override the cardX variables access since they are no longer
    // JPanels with direct label access
    // But since cardX are now StatsCard objects, we can cast or just declare them
    // as StatsCard

    // Variables declaration
    private StatsCard card1;
    private StatsCard card2;
    private StatsCard card3;
    private StatsCard card4;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JLabel subtitleLabel;
    private javax.swing.JLabel welcomeLabel;

    // Need to update the original logic that referred to card1Value, etc.
    // Since we replaced the variables, we need to adapt the loadStats method too to
    // avoid compilation errors.
    // We will do this by keeping the original component names as fields in the
    // class but updating how loadStats interacts with them.
    // IMPORTANT: The previous step removed the JPanel definitions. I need to make
    // sure I update ALL references in the file.
    // The previous loadStats method used: card1Value.setText(...)
    // My new StatsCard has setValue(...).
    // So I need to update loadStats as well. I will include loadStats in this
    // replacement.

}
