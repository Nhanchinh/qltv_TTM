/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.TransactionService;
import services.CardService;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 *
 * @author admin
 */
public class naptien extends javax.swing.JPanel {
    
    private TransactionService transactionService;
    private CardService cardService;
    private String currentCardId = "CARD001";

    /**
     * Creates new form TopUpPanel
     */
    public naptien() {
        transactionService = new TransactionService();
        cardService = new CardService();
        initComponents();
        loadCardInfo();
    }
    
    /**
     * Set CardID từ thẻ đăng nhập
     */
    public void setCurrentCardId(String cardId) {
        if (cardId != null && !cardId.isEmpty()) {
            this.currentCardId = cardId;
            loadCardInfo();
        }
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
        
        // Left panel - Thông tin tài khoản
        accountInfoPanel = new javax.swing.JPanel();
        accountInfoTitle = new javax.swing.JLabel();
        currentBalanceLabel = new javax.swing.JLabel();
        currentBalanceField = new javax.swing.JTextField();
        accountIdLabel = new javax.swing.JLabel();
        accountIdField = new javax.swing.JTextField();
        accountNameLabel = new javax.swing.JLabel();
        accountNameField = new javax.swing.JTextField();
        
        // Right panel - Nạp tiền
        topUpPanel = new javax.swing.JPanel();
        topUpTitle = new javax.swing.JLabel();
        
        // Quick amount buttons
        quickAmountPanel = new javax.swing.JPanel();
        quickAmountLabel = new javax.swing.JLabel();
        
        // Payment method panel
        paymentMethodPanel = new javax.swing.JPanel();
        paymentMethodLabel = new javax.swing.JLabel();
        paymentMethodCombo = new javax.swing.JComboBox<>();
        
        // Amount input
        amountInputPanel = new javax.swing.JPanel();
        amountLabel = new javax.swing.JLabel();
        amountField = new javax.swing.JTextField();
        
        // Note input
        notePanel = new javax.swing.JPanel();
        noteLabel = new javax.swing.JLabel();
        noteField = new javax.swing.JTextField();
        
        // Buttons
        confirmButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 20));

        // Title
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Nạp tiền vào tài khoản");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        mainContainer.setLayout(new java.awt.BorderLayout(25, 0));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 50, 40, 50));
        mainContainer.setBackground(new java.awt.Color(245, 245, 250));

        // ============ LEFT PANEL - THÔNG TIN TÀI KHOẢN ============
        accountInfoPanel.setBackground(new java.awt.Color(255, 255, 255));
        accountInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Thông tin tài khoản",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        accountInfoPanel.setLayout(new java.awt.BorderLayout(0, 20));
        accountInfoPanel.setPreferredSize(new java.awt.Dimension(380, 0));
        accountInfoPanel.setMinimumSize(new java.awt.Dimension(320, 0));
        accountInfoPanel.setMaximumSize(new java.awt.Dimension(450, Integer.MAX_VALUE));

        accountInfoTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        accountInfoTitle.setForeground(new java.awt.Color(0, 120, 215));
        accountInfoTitle.setText("Tài khoản hiện tại");

        javax.swing.JPanel infoFormPanel = new javax.swing.JPanel();
        javax.swing.GroupLayout infoLayout = new javax.swing.GroupLayout(infoFormPanel);
        infoFormPanel.setLayout(infoLayout);
        infoFormPanel.setBackground(new java.awt.Color(255, 255, 255));

        infoLayout.setHorizontalGroup(
            infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(currentBalanceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(accountIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(accountNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(currentBalanceField)
                    .addComponent(accountIdField)
                    .addComponent(accountNameField))
                .addContainerGap())
        );

        infoLayout.setVerticalGroup(
            infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentBalanceLabel)
                    .addComponent(currentBalanceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accountIdLabel)
                    .addComponent(accountIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accountNameLabel)
                    .addComponent(accountNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        // Labels
        currentBalanceLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        currentBalanceLabel.setText("Số dư hiện tại:");
        accountIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        accountIdLabel.setText("Mã thẻ:");
        accountNameLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        accountNameLabel.setText("Tên tài khoản:");

        // Fields
        currentBalanceField.setFont(new java.awt.Font("Segoe UI", 1, 20));
        currentBalanceField.setText("500,000 đ");
        currentBalanceField.setEditable(false);
        currentBalanceField.setForeground(new java.awt.Color(0, 120, 215));
        currentBalanceField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        accountIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        accountIdField.setText(currentCardId);
        accountIdField.setEditable(false);
        accountNameField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        accountNameField.setText("Nguyễn Văn A");
        accountNameField.setEditable(false);

        accountInfoPanel.add(accountInfoTitle, java.awt.BorderLayout.NORTH);
        accountInfoPanel.add(infoFormPanel, java.awt.BorderLayout.CENTER);

        // ============ RIGHT PANEL - NẠP TIỀN ============
        topUpPanel.setBackground(new java.awt.Color(255, 255, 255));
        topUpPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Nạp tiền",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        topUpPanel.setLayout(new java.awt.BorderLayout(0, 20));

        topUpTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        topUpTitle.setForeground(new java.awt.Color(0, 120, 215));
        topUpTitle.setText("Chọn số tiền nạp");

        // Quick amount buttons
        quickAmountPanel.setLayout(new java.awt.BorderLayout(0, 10));
        quickAmountPanel.setBackground(new java.awt.Color(255, 255, 255));

        quickAmountLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        quickAmountLabel.setText("Số tiền nhanh:");
        quickAmountLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 10, 0));

        javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
        buttonsPanel.setLayout(new java.awt.GridLayout(2, 3, 12, 12));
        buttonsPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        javax.swing.JButton[] quickButtons = {
            createQuickAmountButton("100,000 đ", 100000),
            createQuickAmountButton("200,000 đ", 200000),
            createQuickAmountButton("500,000 đ", 500000),
            createQuickAmountButton("1,000,000 đ", 1000000),
            createQuickAmountButton("2,000,000 đ", 2000000),
            createQuickAmountButton("5,000,000 đ", 5000000)
        };

        for (javax.swing.JButton btn : quickButtons) {
            buttonsPanel.add(btn);
        }

        quickAmountPanel.add(quickAmountLabel, java.awt.BorderLayout.NORTH);
        quickAmountPanel.add(buttonsPanel, java.awt.BorderLayout.CENTER);

        // Payment method
        paymentMethodPanel.setLayout(new java.awt.BorderLayout(15, 0));
        paymentMethodPanel.setBackground(new java.awt.Color(255, 255, 255));
        paymentMethodPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        paymentMethodLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        paymentMethodLabel.setText("Phương thức thanh toán:");
        paymentMethodLabel.setPreferredSize(new java.awt.Dimension(180, 30));
        paymentMethodCombo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        paymentMethodCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "Thẻ ngân hàng", "Ví điện tử", "Chuyển khoản", "Tiền mặt"
        }));
        paymentMethodCombo.setPreferredSize(new java.awt.Dimension(200, 35));
        paymentMethodCombo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        paymentMethodPanel.add(paymentMethodLabel, java.awt.BorderLayout.WEST);
        paymentMethodPanel.add(paymentMethodCombo, java.awt.BorderLayout.CENTER);

        // Amount input
        amountInputPanel.setLayout(new java.awt.BorderLayout(15, 0));
        amountInputPanel.setBackground(new java.awt.Color(255, 255, 255));
        amountInputPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        amountLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        amountLabel.setText("Số tiền nạp:");
        amountLabel.setPreferredSize(new java.awt.Dimension(180, 30));
        amountField.setFont(new java.awt.Font("Segoe UI", 1, 16));
        amountField.setColumns(15);
        amountField.setPreferredSize(new java.awt.Dimension(250, 40));
        amountField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        amountField.setText("0");
        amountField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        
        javax.swing.JLabel vndLabel = new javax.swing.JLabel("VNĐ");
        vndLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        vndLabel.setPreferredSize(new java.awt.Dimension(50, 40));

        amountInputPanel.add(amountLabel, java.awt.BorderLayout.WEST);
        amountInputPanel.add(amountField, java.awt.BorderLayout.CENTER);
        amountInputPanel.add(vndLabel, java.awt.BorderLayout.EAST);

        // Note
        notePanel.setLayout(new java.awt.BorderLayout(15, 10));
        notePanel.setBackground(new java.awt.Color(255, 255, 255));
        notePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));

        noteLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        noteLabel.setText("Ghi chú:");
        noteLabel.setPreferredSize(new java.awt.Dimension(180, 30));
        noteField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        noteField.setColumns(25);
        noteField.setPreferredSize(new java.awt.Dimension(300, 80));
        noteField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        notePanel.add(noteLabel, java.awt.BorderLayout.NORTH);
        notePanel.add(noteField, java.awt.BorderLayout.CENTER);

        // Buttons
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 10, 0));

        cancelButton.setBackground(new java.awt.Color(150, 150, 150));
        cancelButton.setForeground(new java.awt.Color(255, 255, 255));
        cancelButton.setText("Hủy");
        cancelButton.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new java.awt.Dimension(130, 45));
        cancelButton.setMinimumSize(new java.awt.Dimension(120, 45));
        cancelButton.setMaximumSize(new java.awt.Dimension(150, 45));
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> cancelTopUp());
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelButton.setBackground(new java.awt.Color(170, 170, 170));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelButton.setBackground(new java.awt.Color(150, 150, 150));
            }
        });

        confirmButton.setBackground(new java.awt.Color(0, 120, 215));
        confirmButton.setForeground(new java.awt.Color(255, 255, 255));
        confirmButton.setText("💳 Xác nhận nạp tiền");
        confirmButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        confirmButton.setFocusPainted(false);
        confirmButton.setPreferredSize(new java.awt.Dimension(220, 45));
        confirmButton.setMinimumSize(new java.awt.Dimension(200, 45));
        confirmButton.setMaximumSize(new java.awt.Dimension(250, 45));
        confirmButton.setBorderPainted(false);
        confirmButton.addActionListener(e -> confirmTopUp());
        confirmButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                confirmButton.setBackground(new java.awt.Color(0, 100, 180));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                confirmButton.setBackground(new java.awt.Color(0, 120, 215));
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        // Main content panel with all input fields
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));
        contentPanel.setBackground(new java.awt.Color(255, 255, 255));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Add panels in order
        contentPanel.add(quickAmountPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));
        contentPanel.add(paymentMethodPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));
        contentPanel.add(amountInputPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(15));
        contentPanel.add(notePanel);
        contentPanel.add(javax.swing.Box.createVerticalGlue());

        topUpPanel.add(topUpTitle, java.awt.BorderLayout.NORTH);
        topUpPanel.add(contentPanel, java.awt.BorderLayout.CENTER);
        topUpPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        mainContainer.add(accountInfoPanel, java.awt.BorderLayout.WEST);
        mainContainer.add(topUpPanel, java.awt.BorderLayout.CENTER);

        add(mainContainer, java.awt.BorderLayout.CENTER);
    }

    private javax.swing.JButton createQuickAmountButton(String text, int amount) {
        javax.swing.JButton button = new javax.swing.JButton(text);
        button.setBackground(new java.awt.Color(245, 245, 250));
        button.setForeground(new java.awt.Color(60, 60, 60));
        button.setFont(new java.awt.Font("Segoe UI", 1, 13));
        button.setFocusPainted(false);
        button.setPreferredSize(new java.awt.Dimension(150, 45));
        button.setMinimumSize(new java.awt.Dimension(120, 45));
        button.setMaximumSize(new java.awt.Dimension(180, 45));
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 220, 220), 1),
            javax.swing.BorderFactory.createEmptyBorder(10, 5, 10, 5)));
        button.addActionListener(e -> {
            amountField.setText(String.format("%,d", amount));
        });
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new java.awt.Color(0, 120, 215));
                button.setForeground(new java.awt.Color(255, 255, 255));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new java.awt.Color(245, 245, 250));
                button.setForeground(new java.awt.Color(60, 60, 60));
            }
        });
        return button;
    }

    private void loadCardInfo() {
        CardService.Card card = cardService.getCardById(currentCardId);
        if (card != null) {
            accountIdField.setText(card.cardId);
            accountNameField.setText(card.fullName);
            // Calculate balance from transactions
            List<TransactionService.Transaction> transactions = transactionService.getTransactionsByCard(currentCardId);
            double balance = 0;
            for (TransactionService.Transaction t : transactions) {
                if (t.type.equals("Deposit")) {
                    balance += t.amount;
                } else if (t.type.equals("Payment")) {
                    balance += t.amount; // amount is negative for payment
                }
            }
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            currentBalanceField.setText(nf.format(balance) + " đ");
        }
    }
    
    /**
     * Reload card info (public method for external refresh)
     */
    public void reloadCardInfo() {
        loadCardInfo();
    }
    
    private void confirmTopUp() {
        String amountText = amountField.getText().replace(",", "").trim();
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
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            int option = javax.swing.JOptionPane.showConfirmDialog(this, 
                "Xác nhận nạp " + String.format("%,d", amount) + " VNĐ\nPhương thức: " + paymentMethod + "\n\nBạn có muốn tiếp tục?",
                "Xác nhận nạp tiền",
                javax.swing.JOptionPane.YES_NO_OPTION);
            if (option == javax.swing.JOptionPane.YES_OPTION) {
                // Save to database
                String transId = UUID.randomUUID().toString();
                if (transactionService.createTransaction(transId, currentCardId, "Deposit", amount, 0)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Nạp tiền thành công!\nSố tiền: " + String.format("%,d", amount) + " VNĐ", 
                        "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    amountField.setText("0");
                    noteField.setText("");
                    loadCardInfo(); // Refresh balance
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi nạp tiền!", "Lỗi", 
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelTopUp() {
        amountField.setText("0");
        noteField.setText("");
        javax.swing.JOptionPane.showMessageDialog(this, "Đã hủy nạp tiền!", "Thông báo", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel accountInfoPanel;
    private javax.swing.JLabel accountInfoTitle;
    private javax.swing.JLabel currentBalanceLabel;
    private javax.swing.JTextField currentBalanceField;
    private javax.swing.JLabel accountIdLabel;
    private javax.swing.JTextField accountIdField;
    private javax.swing.JLabel accountNameLabel;
    private javax.swing.JTextField accountNameField;
    private javax.swing.JPanel topUpPanel;
    private javax.swing.JLabel topUpTitle;
    private javax.swing.JPanel quickAmountPanel;
    private javax.swing.JLabel quickAmountLabel;
    private javax.swing.JPanel paymentMethodPanel;
    private javax.swing.JLabel paymentMethodLabel;
    private javax.swing.JComboBox<String> paymentMethodCombo;
    private javax.swing.JPanel amountInputPanel;
    private javax.swing.JLabel amountLabel;
    private javax.swing.JTextField amountField;
    private javax.swing.JPanel notePanel;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JTextField noteField;
    private javax.swing.JButton confirmButton;
    private javax.swing.JButton cancelButton;
}

