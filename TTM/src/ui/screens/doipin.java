package ui.screens;

import smartcard.CardConnectionManager;
import smartcard.CardVerifyManager;
import smartcard.CardSetupManager;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.security.PublicKey;

/**
 * Screen for changing user PIN
 * User enters old PIN and new PIN
 * Communicates with smart card to change PIN
 */
public class doipin extends javax.swing.JPanel {
    
    private static final byte INS_CHANGE_PIN = (byte) 0x25;
    private static final byte CLA = 0x00;
    
    private CardConnectionManager connManager;
    private PublicKey cardPublicKey;
    private CardChannel channel;

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
        titleLabel.setText("Doi ma PIN");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 10, 40));
        add(titleLabel, java.awt.BorderLayout.NORTH);

        mainContainer.setLayout(new java.awt.BorderLayout(25, 0));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 50, 40, 50));
        mainContainer.setBackground(new java.awt.Color(245, 245, 250));

        // ============ LEFT PANEL - INSTRUCTION ============
        instructionPanel.setBackground(new java.awt.Color(255, 255, 255));
        instructionPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Huong dan",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        instructionPanel.setLayout(new java.awt.BorderLayout());
        instructionPanel.setPreferredSize(new java.awt.Dimension(380, 0));

        instructionTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        instructionTitle.setForeground(new java.awt.Color(0, 120, 215));
        instructionTitle.setText("Cach thay doi ma PIN");
        instructionPanel.add(instructionTitle, java.awt.BorderLayout.NORTH);

        instructionText.setEditable(false);
        instructionText.setLineWrap(true);
        instructionText.setWrapStyleWord(true);
        instructionText.setText("1. Hay nhap ma PIN cu cua ban\n\n" +
                                "2. Nhap ma PIN moi (6 ky tu)\n\n" +
                                "3. Xac nhan lai ma PIN moi\n\n" +
                                "4. Nhan nut 'Doi PIN'\n\n" +
                                "Luu y:\n" +
                                "- Ma PIN phai co dung 6 ky tu\n" +
                                "- Ma PIN moi phai khac ma PIN cu\n" +
                                "- Hay bao quan ma PIN cua ban");
        instructionText.setFont(new java.awt.Font("Segoe UI", 0, 12));
        instructionText.setForeground(new java.awt.Color(100, 100, 100));
        instructionText.setBorder(null);
        instructionText.setBackground(new java.awt.Color(255, 255, 255));
        instructionPanel.add(instructionText, java.awt.BorderLayout.CENTER);

        mainContainer.add(instructionPanel, java.awt.BorderLayout.WEST);

        // ============ RIGHT PANEL - FORM ============
        formPanel.setBackground(new java.awt.Color(255, 255, 255));
        formPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(null, "Form doi PIN",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16), new java.awt.Color(60, 60, 60)),
            javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));
        formPanel.setLayout(new java.awt.BorderLayout(0, 20));

        formTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        formTitle.setForeground(new java.awt.Color(0, 120, 215));
        formTitle.setText("Nhap thong tin");

        // Create form fields layout
        javax.swing.JPanel formFieldsPanel = new javax.swing.JPanel();
        javax.swing.GroupLayout formLayout = new javax.swing.GroupLayout(formFieldsPanel);
        formFieldsPanel.setLayout(formLayout);
        formFieldsPanel.setBackground(new java.awt.Color(255, 255, 255));

        oldPinLabel.setText("Ma PIN cu:");
        oldPinLabel.setFont(new java.awt.Font("Segoe UI", 0, 13));

        oldPinField.setFont(new java.awt.Font("Segoe UI", 0, 13));

        newPinLabel.setText("Ma PIN moi:");
        newPinLabel.setFont(new java.awt.Font("Segoe UI", 0, 13));

        newPinField.setFont(new java.awt.Font("Segoe UI", 0, 13));

        confirmPinLabel.setText("Xac nhan PIN moi:");
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

        changeButton.setText("Doi PIN");
        changeButton.setFont(new java.awt.Font("Segoe UI", 1, 13));
        changeButton.setBackground(new java.awt.Color(0, 120, 215));
        changeButton.setForeground(java.awt.Color.WHITE);
        changeButton.setBorderPainted(false);
        changeButton.setFocusPainted(false);
        changeButton.setPreferredSize(new java.awt.Dimension(120, 40));
        changeButton.addActionListener(this::changeButtonActionPerformed);
        buttonPanel.add(changeButton);

        cancelButton.setText("Xoa");
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
            setStatus("Vui long nhap day du thong tin!", false);
            return;
        }

        if (oldPin.length() != 6 || newPin.length() != 6) {
            setStatus("Ma PIN phai co dung 6 ky tu!", false);
            return;
        }

        if (!newPin.equals(confirmPin)) {
            setStatus("Ma PIN xac nhan khong khop!", false);
            newPinField.setText("");
            confirmPinField.setText("");
            newPinField.requestFocus();
            return;
        }

        if (oldPin.equals(newPin)) {
            setStatus("Ma PIN moi phai khac ma PIN cu!", false);
            return;
        }

        // Disable button and show loading
        changeButton.setEnabled(false);
        setStatus("Dang ket noi the...", true);

        // Change PIN in background thread
        new Thread(() -> {
            CardConnectionManager connManager = null;
            try {
                // Connect to card
                connManager = new CardConnectionManager();
                connManager.connectCard();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Dang xac thuc PIN cu...", true);
                });

                // Get card setup manager
                CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                
                // Load card public key
                if (!setupManager.getPublicKey()) {
                    throw new Exception("Failed to get card public key");
                }

                this.channel = connManager.getChannel();
                this.cardPublicKey = setupManager.getKeyManager().getCardPublicKey();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Dang xac thuc PIN cu...", true);
                });

                // Step 1: Verify old PIN first
                CardVerifyManager verifyManager = new CardVerifyManager(connManager.getChannel());
                boolean verified = verifyManager.verifyPin(oldPin);
                
                if (!verified) {
                    throw new Exception("Old PIN verification failed");
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Dang thay doi ma PIN...", true);
                });

                // Step 2: Send change PIN command (old PIN + new PIN)
                byte[] payload = new byte[12];
                System.arraycopy(oldPin.getBytes(), 0, payload, 0, 6);
                System.arraycopy(newPin.getBytes(), 0, payload, 6, 6);

                sendSecureCommand(INS_CHANGE_PIN, payload);
                
                System.out.println("PIN change command sent. Reconnecting to verify...");
                
                // Step 3: Disconnect and reconnect to verify PIN change
                connManager.disconnectCard();
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Dang xac thuc PIN moi...", true);
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
                    setStatus("Thay doi ma PIN thanh cong!", true);
                    
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
                        setStatus("The da bi khoa!", false);
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
     * Send secure command to smart card
     * Using hybrid encryption: RSA for session key, AES for data
     */
    private void sendSecureCommand(byte ins, byte[] rawData) throws Exception {
        if (channel == null || cardPublicKey == null) {
            throw new Exception("Card not connected or public key not loaded");
        }

        // Generate AES session key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey sessionKey = keyGen.generateKey();

        // Encrypt session key with RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, cardPublicKey);
        byte[] encryptedSessionKey = rsaCipher.doFinal(sessionKey.getEncoded());

        // Pad data to multiple of 16 bytes
        int blockSize = 16;
        int paddedLength = ((rawData.length / blockSize) + 1) * blockSize;
        if (rawData.length % blockSize == 0 && rawData.length > 0) {
            paddedLength = rawData.length;
        } else if (rawData.length == 0) {
            paddedLength = 16;
        }

        byte[] paddedData = new byte[paddedLength];
        System.arraycopy(rawData, 0, paddedData, 0, rawData.length);

        // Encrypt data with AES
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        Cipher aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);
        byte[] encryptedData = aesCipher.doFinal(paddedData);

        // Build APDU: [encrypted session key][encrypted data]
        byte[] apduData = new byte[encryptedSessionKey.length + encryptedData.length];
        System.arraycopy(encryptedSessionKey, 0, apduData, 0, encryptedSessionKey.length);
        System.arraycopy(encryptedData, 0, apduData, encryptedSessionKey.length, encryptedData.length);

        System.out.println("Sending Secure CMD (INS: " + String.format("0x%02X", ins) + ")...");
        ResponseAPDU response = channel.transmit(new CommandAPDU(CLA, ins, 0x00, 0x00, apduData));

        System.out.println("Response SW: " + String.format("0x%04X", response.getSW()));
        if (response.getSW() != 0x9000) {
            throw new Exception("Change PIN failed (SW: " + String.format("0x%04X", response.getSW()) + ")");
        }

        System.out.println(">>> SUCCESS");
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
