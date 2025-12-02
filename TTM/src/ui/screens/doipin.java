package ui.screens;

import smartcard.CardConnectionManager;
import smartcard.CardVerifyManager;
import smartcard.CardSetupManager;

/**
 * Screen for changing user PIN
 * User enters old PIN and new PIN
 * Communicates with smart card to change PIN
 */
public class doipin extends javax.swing.JPanel {

    private CardConnectionManager connManager;

    /**
     * Creates new form ChangePinPanel
     */
    public doipin() {
        initComponents();
    }

    /**
     * Initialize GUI components
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        mainContainer = new javax.swing.JPanel();
        
        // Left panel - Hướng dẫn
        instructionPanel = new javax.swing.JPanel();
        instructionTitle = new javax.swing.JLabel();
        instructionText = new javax.swing.JTextArea();
        
        // Right panel - Form đổi PIN
        formPanel = new javax.swing.JPanel();
        formTitle = new javax.swing.JLabel();
        
        // Old PIN
        oldPinLabel = new javax.swing.JLabel();
        oldPinField = new javax.swing.JPasswordField();
        
        // New PIN
        newPinLabel = new javax.swing.JLabel();
        newPinField = new javax.swing.JPasswordField();
        
        // Confirm PIN
        confirmPinLabel = new javax.swing.JLabel();
        confirmPinField = new javax.swing.JPasswordField();
        
        // Status label
        statusLabel = new javax.swing.JLabel();
        
        // Buttons
        changeButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 20));

        // Title
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Đổi mã PIN");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        mainContainer.setLayout(new java.awt.BorderLayout(25, 0));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 50, 40, 50));
        mainContainer.setBackground(new java.awt.Color(245, 245, 250));

        // ============ LEFT PANEL - INSTRUCTION ============
        instructionPanel.setBackground(new java.awt.Color(255, 255, 255));
        instructionPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createTitledBorder(null, "Hướng dẫn",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        instructionPanel.setLayout(new java.awt.BorderLayout());
        instructionPanel.setPreferredSize(new java.awt.Dimension(380, 0));

        instructionTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        instructionTitle.setForeground(new java.awt.Color(0, 120, 215));
        instructionTitle.setText("Cách thay đổi mã PIN");
        instructionPanel.add(instructionTitle, java.awt.BorderLayout.NORTH);

        instructionText.setEditable(false);
        instructionText.setLineWrap(true);
        instructionText.setWrapStyleWord(true);
        instructionText.setText("1. Hãy nhập mã PIN cũ của bạn\n\n" +
                                "2. Nhập mã PIN mới (6 ký tự)\n\n" +
                                "3. Xác nhận lại mã PIN mới\n\n" +
                                "4. Nhấn nút 'Đổi PIN'\n\n" +
                                "Lưu ý:\n" +
                                "- Mã PIN phải có đúng 6 ký tự\n" +
                                "- Mã PIN mới phải khác mã PIN cũ\n" +
                                "- Hãy bảo quản mã PIN của bạn");
        instructionText.setFont(new java.awt.Font("Segoe UI", 0, 12));
        instructionText.setForeground(new java.awt.Color(100, 100, 100));
        instructionText.setBorder(null);
        instructionText.setBackground(new java.awt.Color(255, 255, 255));
        instructionPanel.add(instructionText, java.awt.BorderLayout.CENTER);

        mainContainer.add(instructionPanel, java.awt.BorderLayout.WEST);

        // ============ RIGHT PANEL - FORM ============
        formPanel.setBackground(new java.awt.Color(255, 255, 255));
        formPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createTitledBorder(null, "Form đổi PIN",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        formPanel.setLayout(new java.awt.BorderLayout(0, 20));

        formTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        formTitle.setForeground(new java.awt.Color(0, 120, 215));
        formTitle.setText("Nhập thông tin");

        // Create form fields layout
        javax.swing.JPanel formFieldsPanel = new javax.swing.JPanel();
        javax.swing.GroupLayout formLayout = new javax.swing.GroupLayout(formFieldsPanel);
        formFieldsPanel.setLayout(formLayout);
        formFieldsPanel.setBackground(new java.awt.Color(255, 255, 255));

        oldPinLabel.setText("Mã PIN cũ:");
        oldPinLabel.setFont(new java.awt.Font("Segoe UI", 0, 13));

        oldPinField.setFont(new java.awt.Font("Segoe UI", 0, 13));

        newPinLabel.setText("Mã PIN mới:");
        newPinLabel.setFont(new java.awt.Font("Segoe UI", 0, 13));

        newPinField.setFont(new java.awt.Font("Segoe UI", 0, 13));

        confirmPinLabel.setText("Xác nhận PIN mới:");
        confirmPinLabel.setFont(new java.awt.Font("Segoe UI", 0, 13));

        confirmPinField.setFont(new java.awt.Font("Segoe UI", 0, 13));

        statusLabel.setText(" ");
        statusLabel.setFont(new java.awt.Font("Segoe UI", 0, 12));
        statusLabel.setForeground(new java.awt.Color(220, 53, 69));

        formLayout.setHorizontalGroup(
            formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(oldPinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newPinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(confirmPinLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(oldPinField)
                    .addComponent(newPinField)
                    .addComponent(confirmPinField)
                    .addComponent(statusLabel))
                .addContainerGap())
        );

        formLayout.setVerticalGroup(
            formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(oldPinLabel)
                    .addComponent(oldPinField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newPinLabel)
                    .addComponent(newPinField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(confirmPinLabel)
                    .addComponent(confirmPinField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(statusLabel)
                .addContainerGap())
        );

        formPanel.add(formTitle, java.awt.BorderLayout.NORTH);
        formPanel.add(formFieldsPanel, java.awt.BorderLayout.CENTER);

        // Buttons panel
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 0));

        changeButton.setText("Đổi PIN");
        changeButton.setFont(new java.awt.Font("Segoe UI", 1, 13));
        changeButton.setBackground(new java.awt.Color(0, 120, 215));
        changeButton.setForeground(java.awt.Color.WHITE);
        changeButton.setBorderPainted(false);
        changeButton.setFocusPainted(false);
        changeButton.setPreferredSize(new java.awt.Dimension(120, 40));
        changeButton.addActionListener(this::changeButtonActionPerformed);
        buttonPanel.add(changeButton);

        cancelButton.setText("Xóa");
        cancelButton.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cancelButton.setBackground(new java.awt.Color(108, 117, 125));
        cancelButton.setForeground(java.awt.Color.WHITE);
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new java.awt.Dimension(120, 40));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonPanel.add(cancelButton);

        formPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        mainContainer.add(formPanel, java.awt.BorderLayout.CENTER);
        add(mainContainer, java.awt.BorderLayout.CENTER);
    }

    /**
     * Change PIN button action
     */
    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String oldPin = new String(oldPinField.getPassword());
        String newPin = new String(newPinField.getPassword());
        String confirmPin = new String(confirmPinField.getPassword());

        // Validation
        if (oldPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            setStatus("Vui lòng nhập đầy đủ thông tin!", false);
            return;
        }

        if (oldPin.length() != 6 || newPin.length() != 6) {
            setStatus("Mã PIN phải có đúng 6 ký tự!", false);
            return;
        }

        if (!newPin.equals(confirmPin)) {
            setStatus("Mã PIN xác nhận không khớp!", false);
            newPinField.setText("");
            confirmPinField.setText("");
            newPinField.requestFocus();
            return;
        }

        if (oldPin.equals(newPin)) {
            setStatus("Mã PIN mới phải khác mã PIN cũ!", false);
            return;
        }

        // Disable button and show loading
        changeButton.setEnabled(false);
        setStatus("Đang kết nối thẻ...", true);

        // Change PIN in background thread
        new Thread(() -> {
            CardConnectionManager connManager = null;
            try {
                // Connect to card
                connManager = new CardConnectionManager();
                connManager.connectCard();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang xác thực PIN cũ...", true);
                });

                // Get card setup manager
                CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                // Lấy public key (giống các luồng khác, nhưng dùng cho changePin PLAINTEXT)
                if (!setupManager.getPublicKey()) {
                    throw new Exception("Failed to get card public key");
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang xác thực PIN cũ...", true);
                });

                // Step 1: Verify old PIN first
                CardVerifyManager verifyManager = new CardVerifyManager(connManager.getChannel());
                boolean verified = verifyManager.verifyPin(oldPin);
                
                if (!verified) {
                    throw new Exception("Old PIN verification failed");
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang thay đổi mã PIN...", true);
                });

                // Step 2: Gửi lệnh đổi PIN dạng PLAINTEXT thông qua CardSetupManager
                setupManager.changePin(oldPin, newPin);
                
                System.out.println("PIN change command sent. Reconnecting to verify...");
                
                // Step 3: Disconnect and reconnect to verify PIN change
                connManager.disconnectCard();
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang xác thực PIN mới...", true);
                });
                
                Thread.sleep(500); // Wait a bit for card to settle
                
                // Reconnect and verify new PIN
                connManager = new CardConnectionManager();
                connManager.connectCard();
                
                CardVerifyManager newVerifyManager = new CardVerifyManager(connManager.getChannel());
                boolean newPinVerified = newVerifyManager.verifyPin(newPin);
                
                if (!newPinVerified) {
                    throw new Exception("New PIN verification failed - change may not have been applied");
                }
                
                System.out.println("New PIN verified successfully!");

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Thay đổi mã PIN thành công!", true);
                    
                    // Clear fields
                    oldPinField.setText("");
                    newPinField.setText("");
                    confirmPinField.setText("");
                    
                    // Re-enable button after 2 seconds
                    javax.swing.Timer timer = new javax.swing.Timer(2000, e -> changeButton.setEnabled(true));
                    timer.setRepeats(false);
                    timer.start();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    String errorMsg = ex.getMessage();
                    if ("CARD_BLOCKED".equals(errorMsg)) {
                        setStatus("Thẻ đã bị khóa!", false);
                    } else {
                        setStatus("Loi: " + (errorMsg != null ? errorMsg : ex.getClass().getSimpleName()), false);
                    }
                    changeButton.setEnabled(true);
                });
            } finally {
                // Always disconnect card at the end
                if (connManager != null) {
                    try {
                        connManager.disconnectCard();
                    } catch (Exception e) {
                        System.err.println("Error disconnecting card: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Cancel button action
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        oldPinField.setText("");
        newPinField.setText("");
        confirmPinField.setText("");
        statusLabel.setText(" ");
        oldPinField.requestFocus();
    }

    /**
     * Set status message
     */
    private void setStatus(String message, boolean isSuccess) {
        statusLabel.setForeground(isSuccess ? new java.awt.Color(40, 167, 69) : new java.awt.Color(220, 53, 69));
        statusLabel.setText(message);
    }

    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel mainContainer;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JLabel instructionTitle;
    private javax.swing.JTextArea instructionText;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel formTitle;
    private javax.swing.JLabel oldPinLabel;
    private javax.swing.JPasswordField oldPinField;
    private javax.swing.JLabel newPinLabel;
    private javax.swing.JPasswordField newPinField;
    private javax.swing.JLabel confirmPinLabel;
    private javax.swing.JPasswordField confirmPinField;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton changeButton;
    private javax.swing.JButton cancelButton;
}
