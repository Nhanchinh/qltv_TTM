/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.TransactionService;
import services.CardService;
import smartcard.CardConnectionManager;
import smartcard.CardBalanceManager;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JButton;

/**
 * Premium Deposit Panel
 */
public class naptien extends javax.swing.JPanel {

    // Services
    private TransactionService transactionService;
    private CardService cardService;
    private String currentCardId = "CARD001";

    // UI Components
    private javax.swing.JLabel balanceLabel; // On the virtual card
    private javax.swing.JLabel cardIdLabel; // On the virtual card
    private javax.swing.JTextField amountField;
    private javax.swing.JComboBox<String> paymentMethodCombo;
    private javax.swing.JButton confirmButton;

    public naptien() {
        transactionService = new TransactionService();
        cardService = new CardService();
        initComponents();
        loadCardInfo();
    }

    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            loadCardInfo();
        }
    }

    public void reloadCardInfo() {
        loadCardInfo();
    }

    private void loadCardInfo() {
        if (cardIdLabel != null)
            cardIdLabel.setText(formatCardId(currentCardId));

        // Only use smart card for balance
        int cardBalance = getBalanceFromCard();
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        if (cardBalance >= 0) {
            String text = nf.format(cardBalance) + " VNĐ";
            if (balanceLabel != null)
                balanceLabel.setText(text);
        } else {
            if (balanceLabel != null)
                balanceLabel.setText("----");
        }
    }

    private String formatCardId(String id) {
        // Format like credit card: CARD 001 -> CARD 001
        return id != null ? id.toUpperCase() : "----";
    }

    /**
     * Get Balance from Smart Card
     */
    private int getBalanceFromCard() {
        try {
            CardConnectionManager manager = new CardConnectionManager();
            if (manager.connectCard()) {
                javax.smartcardio.CardChannel channel = manager.getChannel();
                if (channel != null) {
                    CardBalanceManager balanceManager = new CardBalanceManager(channel);
                    CardBalanceManager.BalanceInfo info = balanceManager.getBalance();
                    manager.disconnectCard();
                    if (info.success) {
                        return info.balance;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[NAPTIEN] Warning: Cannot read card balance: " + e.getMessage());
        }
        return -1;
    }

    private void initComponents() {
        setBackground(new java.awt.Color(248, 250, 252)); // Slate 50
        setLayout(new java.awt.BorderLayout(0, 0));

        // 1. Header
        add(createHeaderPanel(), java.awt.BorderLayout.NORTH);

        // 2. Content
        javax.swing.JPanel contentPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 40, 40)); // More padding

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // LEFT: Quick Selection Grid (60%)
        gbc.gridx = 0;
        gbc.weightx = 0.6;
        gbc.insets = new java.awt.Insets(0, 0, 0, 30);
        contentPanel.add(createQuickSelectPanel(), gbc);

        // RIGHT: Virtual Card + Form (40%)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        contentPanel.add(createRightPanel(), gbc);

        add(contentPanel, java.awt.BorderLayout.CENTER);
    }

    // --- UI Creators ---

    private javax.swing.JPanel createHeaderPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));
        p.setPreferredSize(new java.awt.Dimension(0, 80));

        javax.swing.JLabel title = new javax.swing.JLabel("Nạp Tiền");
        title.setFont(new java.awt.Font("Segoe UI", 1, 28));
        title.setForeground(new java.awt.Color(15, 23, 42));
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));

        p.add(title, java.awt.BorderLayout.WEST);
        return p;
    }

    private javax.swing.JPanel createQuickSelectPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(0, 20));
        p.setOpaque(false);

        javax.swing.JLabel lbl = new javax.swing.JLabel("Chọn mệnh giá nạp");
        lbl.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lbl.setForeground(new java.awt.Color(51, 65, 85));
        p.add(lbl, java.awt.BorderLayout.NORTH);

        // Grid: 2 columns, 3 rows, big gaps
        javax.swing.JPanel grid = new javax.swing.JPanel(new java.awt.GridLayout(3, 2, 20, 20));
        grid.setOpaque(false);

        grid.add(createDenominationCard("10.000", new java.awt.Color(20, 184, 166), 10000)); // Teal
        grid.add(createDenominationCard("20.000", new java.awt.Color(6, 182, 212), 20000)); // Cyan
        grid.add(createDenominationCard("50.000", new java.awt.Color(59, 130, 246), 50000)); // Blue
        grid.add(createDenominationCard("100.000", new java.awt.Color(99, 102, 241), 100000)); // Indigo
        grid.add(createDenominationCard("200.000", new java.awt.Color(139, 92, 246), 200000)); // Violet
        grid.add(createDenominationCard("500.000", new java.awt.Color(236, 72, 153), 500000)); // Pink

        p.add(grid, java.awt.BorderLayout.CENTER);
        return p;
    }

    private javax.swing.JButton createDenominationCard(String displayValue, java.awt.Color color, int amount) {
        javax.swing.JButton b = new javax.swing.JButton() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                if (getModel().isRollover()) {
                    g2.setColor(color);
                } else {
                    g2.setColor(java.awt.Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Border
                if (!getModel().isRollover()) {
                    g2.setColor(new java.awt.Color(226, 232, 240));
                    g2.setStroke(new java.awt.BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

                    // Color bar at bottom
                    g2.setColor(color);
                    g2.fillRoundRect(0, getHeight() - 10, getWidth(), 10, 20, 20);
                    g2.fillRect(0, getHeight() - 10, getWidth(), 5); // square off top of bottom bar
                }

                // Text
                g2.setFont(new java.awt.Font("Segoe UI", 1, 24));
                if (getModel().isRollover()) {
                    g2.setColor(java.awt.Color.WHITE);
                } else {
                    g2.setColor(new java.awt.Color(30, 41, 59));
                }
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String text = displayValue;
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 5;
                g2.drawString(text, x, y);

                g2.dispose();
            }
        };
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        b.addActionListener(e -> {
            amountField.setText(String.valueOf(amount));
        });

        return b;
    }

    private javax.swing.JPanel createRightPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel();
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // 1. Virtual Card
        p.add(createVirtualCard());
        p.add(javax.swing.Box.createVerticalStrut(25));

        // 2. Input Form
        p.add(createProcessPanel());

        return p;
    }

    private javax.swing.JPanel createVirtualCard() {
        javax.swing.JPanel card = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient Background (Deep Blue to Purple)
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                        0, 0, new java.awt.Color(30, 58, 138), // Blue 900
                        getWidth(), getHeight(), new java.awt.Color(79, 70, 229) // Indigo 600
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // Chip (Fake visual)
                g2.setColor(new java.awt.Color(253, 224, 71)); // Yellow
                g2.fillRoundRect(30, 60, 50, 40, 8, 8);

                g2.dispose();
            }
        };
        card.setLayout(null); // Absolute layout for card look
        card.setPreferredSize(new java.awt.Dimension(340, 200));
        card.setMinimumSize(new java.awt.Dimension(340, 200));
        card.setMaximumSize(new java.awt.Dimension(340, 200));

        // Label: Library Card
        javax.swing.JLabel lblTitle = new javax.swing.JLabel("THẺ NHÀ SÁCH");
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblTitle.setForeground(new java.awt.Color(255, 255, 255, 180));
        lblTitle.setBounds(30, 25, 200, 20);
        card.add(lblTitle);

        // Card ID
        cardIdLabel = new javax.swing.JLabel(formatCardId(currentCardId));
        cardIdLabel.setFont(new java.awt.Font("Monospaced", 1, 18));
        cardIdLabel.setForeground(java.awt.Color.WHITE);
        cardIdLabel.setBounds(30, 110, 300, 30);
        card.add(cardIdLabel);

        // Balance Title
        javax.swing.JLabel lblBal = new javax.swing.JLabel("Số dư");
        lblBal.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblBal.setForeground(new java.awt.Color(255, 255, 255, 180));
        lblBal.setBounds(30, 145, 100, 20);
        card.add(lblBal);

        // Balance Value
        balanceLabel = new javax.swing.JLabel("Checking...");
        balanceLabel.setFont(new java.awt.Font("Segoe UI", 1, 22));
        balanceLabel.setForeground(java.awt.Color.WHITE);
        balanceLabel.setBounds(30, 160, 280, 30);
        card.add(balanceLabel);

        return card;
    }

    private javax.swing.JPanel createProcessPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.GridBagLayout());
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 0, 5, 0);

        // Title
        javax.swing.JLabel title = new javax.swing.JLabel("Thực hiện nạp tiền");
        title.setFont(new java.awt.Font("Segoe UI", 1, 16));
        title.setForeground(new java.awt.Color(15, 23, 42));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(0, 0, 15, 0);
        p.add(title, gbc);

        // Amount Field
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new java.awt.Insets(0, 0, 5, 0);
        javax.swing.JLabel lblAm = new javax.swing.JLabel("Số tiền (VNĐ)");
        lblAm.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblAm.setForeground(new java.awt.Color(100, 116, 139));
        p.add(lblAm, gbc);

        gbc.gridy = 2;
        amountField = new javax.swing.JTextField("0");
        amountField.setFont(new java.awt.Font("Segoe UI", 1, 24));
        amountField.setForeground(new java.awt.Color(15, 23, 42));
        amountField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        amountField
                .setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new java.awt.Color(226, 232, 240)));
        amountField.setBackground(java.awt.Color.WHITE);
        p.add(amountField, gbc);

        // Payment Method
        gbc.gridy = 3;
        gbc.insets = new java.awt.Insets(15, 0, 5, 0);
        javax.swing.JLabel lblMethod = new javax.swing.JLabel("Phương thức");
        lblMethod.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblMethod.setForeground(new java.awt.Color(100, 116, 139));
        p.add(lblMethod, gbc);

        gbc.gridy = 4;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        paymentMethodCombo = new javax.swing.JComboBox<>(
                new String[] { "Tiền mặt", "Chuyển khoản Ngân hàng", "Momo / ZaloPay" });
        paymentMethodCombo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        paymentMethodCombo.setBackground(java.awt.Color.WHITE);
        paymentMethodCombo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)));
        p.add(paymentMethodCombo, gbc);

        // Confirm Button
        gbc.gridy = 5;
        gbc.insets = new java.awt.Insets(25, 0, 0, 0);
        confirmButton = new javax.swing.JButton("XÁC NHẬN NẠP");
        confirmButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        confirmButton.setForeground(java.awt.Color.WHITE);
        confirmButton.setBackground(new java.awt.Color(0, 0, 0)); // Black button for premium feel
        confirmButton.setFocusPainted(false);
        confirmButton.setBorderPainted(false);
        confirmButton.setPreferredSize(new java.awt.Dimension(0, 45));
        confirmButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        confirmButton.addActionListener(e -> confirmTopUp());
        p.add(confirmButton, gbc);

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
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    // --- Business Logic ---
    private void confirmTopUp() {
        String amountText = amountField.getText().replace(",", "").replace(".", "").trim();
        if (amountText.isEmpty() || amountText.equals("0")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền cần nạp!", "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            long amount = Long.parseLong(amountText);
            if (amount < 10000) {
                javax.swing.JOptionPane.showMessageDialog(this, "Số tiền nạp tối thiểu là 10,000 VNĐ!", "Thông báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amount > Integer.MAX_VALUE) {
                javax.swing.JOptionPane.showMessageDialog(this, "Số tiền nạp vượt quá giới hạn!", "Thông báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            int option = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Xác nhận nạp " + String.format("%,d", amount) + " VNĐ\nPhương thức: " + paymentMethod
                            + "\n\nBạn có muốn tiếp tục?",
                    "Xác nhận nạp tiền",
                    javax.swing.JOptionPane.YES_NO_OPTION);

            if (option == javax.swing.JOptionPane.YES_OPTION) {
                // Yêu cầu xác nhận mã PIN
                java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (parentWindow instanceof java.awt.Frame) {
                    PinConfirmDialog pinDialog = new PinConfirmDialog((java.awt.Frame) parentWindow);
                    pinDialog.setVisible(true);

                    if (!pinDialog.isConfirmed()) {
                        return; // Người dùng hủy hoặc nhập sai PIN
                    }
                }

                boolean cardSuccess = depositToCard((int) amount);
                if (!cardSuccess) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Không thể nạp tiền vào thẻ!\nVui lòng kiểm tra kết nối thẻ.", "Lỗi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String transId = java.util.UUID.randomUUID().toString();
                transactionService.createTransaction(transId, currentCardId, "Deposit", amount, 0);

                javax.swing.JOptionPane.showMessageDialog(this,
                        "Nạp tiền thành công!\nSố tiền: " + String.format("%,d", amount) + " VNĐ", "Thông báo",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("0");
                loadCardInfo();
            }
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean depositToCard(int amount) {
        try {
            CardConnectionManager manager = new CardConnectionManager();
            if (!manager.connectCard())
                return false;

            javax.smartcardio.CardChannel channel = manager.getChannel();
            CardBalanceManager balanceManager = new CardBalanceManager(channel);
            boolean success = balanceManager.deposit(amount);
            manager.disconnectCard();
            if (success)
                System.out.println("[NAPTIEN] Deposited: " + amount);
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
