package ui.screens;

import services.SettingsService;
import services.CardService;
import smartcard.CardConnectionManager;
import smartcard.CardVerifyManager;
import smartcard.CardKeyManager;
import smartcard.CardIdExtractor;
import ui.DBConnect;
import java.awt.*;
import java.awt.event.*;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SecureRandom;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigInteger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.util.Arrays;

public class PinLoginDialog extends JDialog {
    
    private JPasswordField pinField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel errorLabel;
    private boolean authenticated = false;
    private boolean cardBlocked = false;
    private SettingsService settingsService;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(0, 120, 215);
    private static final Color PRIMARY_DARK = new Color(0, 100, 180);
    private static final Color BACKGROUND_GRADIENT_START = new Color(240, 248, 255);
    private static final Color BACKGROUND_GRADIENT_END = new Color(230, 240, 255);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    
    public PinLoginDialog(Frame parent) {
        super(parent, true);
        settingsService = new SettingsService();
        initComponents();
        setupDialog();
    }
    
    private void initComponents() {
        setTitle("Dang nhap he thong");
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Main container with gradient background
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_GRADIENT_START, 0, h, BACKGROUND_GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        
        // Card panel (white background with shadow effect)
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(60, 80, 60, 80)
        ));
        cardPanel.setOpaque(true);
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 50, 0));
        
        JLabel titleLabel = new JLabel("NHAP MA PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Vui long nhap ma PIN de truy cap he thong");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        cardPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center panel for PIN input - simplified layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // PIN field - MUCH LARGER
        pinField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 32));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setPreferredSize(new Dimension(600, 90));
        pinField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180, 180, 180), 3, true),
            new EmptyBorder(20, 25, 20, 25)
        ));
        pinField.setEchoChar('●');
        pinField.setMargin(new Insets(10, 10, 10, 10));
        pinField.addActionListener(e -> attemptLogin());
        pinField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                pinField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 4, true),
                    new EmptyBorder(20, 25, 20, 25)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                pinField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(180, 180, 180), 3, true),
                    new EmptyBorder(20, 25, 20, 25)
                ));
            }
        });
        
        centerPanel.add(pinField, BorderLayout.CENTER);
        
        // Error label below PIN field
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorLabel.setForeground(new Color(220, 53, 69));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        centerPanel.add(errorLabel, BorderLayout.SOUTH);
        
        cardPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(40, 0, 0, 0));
        
        // Login button with modern styling - larger
        loginButton = new JButton("DANG NHAP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(PRIMARY_DARK);
                } else if (getModel().isRollover()) {
                    g2d.setColor(PRIMARY_COLOR.brighter());
                } else {
                    g2d.setColor(PRIMARY_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(200, 60));
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> attemptLogin());
        buttonPanel.add(loginButton);
        
        // Cancel button with modern styling - larger
        cancelButton = new JButton("THOAT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(190, 190, 190));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(240, 240, 240));
                } else {
                    g2d.setColor(new Color(220, 220, 220));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cancelButton.setForeground(new Color(60, 60, 60));
        cancelButton.setPreferredSize(new Dimension(200, 60));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Ban co chac chan muon thoat?",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (option == JOptionPane.YES_OPTION) {
                // Do not exit app; just close dialog and return false
                authenticated = false;
                cardBlocked = false;
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add card panel to main container with padding
        mainContainer.setBorder(new EmptyBorder(50, 50, 50, 50));
        mainContainer.add(cardPanel, BorderLayout.CENTER);
        
        add(mainContainer);
    }
    
    private void setupDialog() {
        // Set dialog size - MUCH LARGER
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Focus on PIN field
        pinField.requestFocusInWindow();
        
        // Close on ESC
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel"
        );
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });
        
        // Prevent window closing without authentication
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Close dialog without exiting app
                authenticated = false;
                cardBlocked = false;
                dispose();
            }
        });
    }
    
    private void attemptLogin() {
        String enteredPin = new String(pinField.getPassword());
        
        if (enteredPin.isEmpty()) {
            errorLabel.setText("Vui long nhap ma PIN!");
            pinField.setText("");
            pinField.requestFocus();
            return;
        }
        
        if (enteredPin.length() != 6) {
            errorLabel.setText("Ma PIN phai co dung 6 ky tu!");
            pinField.setText("");
            pinField.requestFocus();
            return;
        }
        
        // Disable button and show loading
        loginButton.setEnabled(false);
        errorLabel.setForeground(PRIMARY_COLOR);
        errorLabel.setText("Dang ket noi the...");
        
        // Verify PIN on smart card in background thread
        new Thread(() -> {
            CardConnectionManager connManager = null;
            try {
                // Connect to card
                connManager = new CardConnectionManager();
                connManager.connectCard();
                
                SwingUtilities.invokeLater(() -> {
                    errorLabel.setText("Dang xac thuc PIN...");
                });
                
                // Verify PIN on card
                CardVerifyManager verifyManager = new CardVerifyManager(connManager.getChannel());
                boolean verified = verifyManager.verifyPin(enteredPin);
                
                if (verified) {
                    // PIN verified - now authenticate user (BEFORE disconnecting)
                    try {
                        SwingUtilities.invokeLater(() -> {
                            errorLabel.setText("Dang xac thuc the...");
                        });
                        
                        // Get card public key and authenticate (channel still connected)
                        authenticateUserAfterLogin(connManager.getChannel());
                        
                        SwingUtilities.invokeLater(() -> {
                            authenticated = true;
                            dispose();
                        });
                    } catch (Exception authEx) {
                        authEx.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            errorLabel.setForeground(new Color(220, 53, 69));
                            errorLabel.setText("Loi xac thuc: " + authEx.getMessage());
                            loginButton.setEnabled(true);
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(() -> {
                        errorLabel.setForeground(new Color(220, 53, 69));
                        errorLabel.setText("Ma PIN khong dung!");
                        pinField.setText("");
                        pinField.requestFocus();
                        loginButton.setEnabled(true);
                        shakeDialog();
                    });
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    String errorMsg = ex.getMessage();
                    
                    if ("WRONG_PIN".equals(errorMsg)) {
                        // PIN sai - cho phép thử lại
                        errorLabel.setForeground(new Color(220, 53, 69));
                        errorLabel.setText("Sai ma PIN! Vui long thu lai.");
                        pinField.setText("");
                        pinField.requestFocus();
                        loginButton.setEnabled(true);
                        shakeDialog();
                    } else if ("CARD_BLOCKED".equals(errorMsg)) {
                        // Thẻ bị khóa - thoát ra màn hình chọn vai trò
                        JOptionPane.showMessageDialog(
                            PinLoginDialog.this,
                            "The da bi khoa!\nVui long lien he quan tri vien.",
                            "The bi khoa",
                            JOptionPane.ERROR_MESSAGE
                        );
                        // Immediately route to role selection without exiting app
                        authenticated = false;
                        PinLoginDialog.this.cardBlocked = true;
                        // Show role selection dialog and handle choice
                        Frame owner = (Frame) SwingUtilities.getWindowAncestor(PinLoginDialog.this);
                        int loginMode = LoginSelectDialog.showSelectionDialog(owner);
                        
                        if (loginMode == 0) {
                            // User chose to exit login flow only
                            dispose();
                            return;
                        } else if (loginMode == 1) {
                            // Loop back to PIN login
                            dispose();
                            // Relaunch PIN dialog
                            SwingUtilities.invokeLater(() -> {
                                PinLoginDialog.showPinDialog(owner);
                            });
                            return;
                        } else if (loginMode == 2) {
                            // Launch admin login
                            dispose();
                            SwingUtilities.invokeLater(() -> {
                                AdminLoginDialog.showAdminLoginDialog(owner);
                            });
                            return;
                        }
                    } else {
                        // Lỗi khác
                        errorLabel.setForeground(new Color(220, 53, 69));
                        errorLabel.setText("Loi: " + errorMsg);
                        pinField.setText("");
                        pinField.requestFocus();
                        loginButton.setEnabled(true);
                        shakeDialog();
                    }
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
    
    private void shakeDialog() {
        Point originalLocation = getLocation();
        int shakeDistance = 10;
        int shakeCount = 5;
        
        Timer timer = new Timer(50, new ActionListener() {
            int count = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count < shakeCount) {
                    int x = originalLocation.x + (count % 2 == 0 ? shakeDistance : -shakeDistance);
                    int y = originalLocation.y;
                    setLocation(x, y);
                    count++;
                } else {
                    setLocation(originalLocation);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public boolean isCardBlocked() {
        return cardBlocked;
    }
    
    /**
     * Authenticate user after PIN verification
     * Step 1: Get CardID from smart card using CardInfoManager
     * Step 2: Get public key from database using CardID
     * Step 3: Verify public key exists (consistency check)
     */
    private void authenticateUserAfterLogin(javax.smartcardio.CardChannel channel) throws Exception {
        System.out.println("\n========== USER AUTHENTICATION AFTER LOGIN ==========");
        
        // Step 1: Get app keypair first (required for decrypting card info)
        CardKeyManager keyManager = new CardKeyManager(channel);
        keyManager.getPublicKey();
        if (!keyManager.loadAppKeyPair()) {
            throw new Exception("App KeyPair not found. Please register card first.");
        }
        System.out.println(">>> App KeyPair loaded");
        
        // Step 2: Get CardID from smart card (decrypt encrypted response)
        String cardId = CardIdExtractor.extractCardId(channel, keyManager);
        
        if (cardId == null || cardId.isEmpty()) {
            throw new Exception("Failed to get Card ID from card");
        }
        System.out.println(">>> Card ID retrieved from card: " + cardId);
        
        // Step 3: Get public key from database using CardID
        byte[] publicKeyBytes = getCardPublicKeyFromDatabase(cardId);
        if (publicKeyBytes == null || publicKeyBytes.length == 0) {
            throw new Exception("Public key not found in database for card: " + cardId);
        }
        System.out.println(">>> Public Key retrieved from database (" + publicKeyBytes.length + " bytes)");
        
        System.out.println("✓ AUTHENTICATION SUCCESSFUL!");
        System.out.println("✓ Card ID: " + cardId);
        System.out.println("========== AUTHENTICATION COMPLETED SUCCESSFULLY ==========\n");
    }
    

    
    /**
     * Query database to get card's public key
     * Public key is stored as BLOB in Cards table
     */
    private byte[] getCardPublicKeyFromDatabase(String cardId) throws Exception {
        String sql = "SELECT CardPublicKey FROM Cards WHERE CardID = ?";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cardId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    byte[] pubKeyBytes = rs.getBytes("CardPublicKey");
                    if (pubKeyBytes != null && pubKeyBytes.length > 0) {
                        return pubKeyBytes;
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * Convert public key bytes (X.509 encoded) to PublicKey object
     */
    private PublicKey bytesToPublicKey(byte[] pubKeyBytes) throws Exception {
        // This assumes pubKeyBytes is in X.509 format
        // If stored as raw RSA modulus/exponent, parse accordingly
        
        try {
            java.security.spec.X509EncodedKeySpec keySpec = 
                new java.security.spec.X509EncodedKeySpec(pubKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            // If X.509 format fails, try parsing as raw RSA key (modulus only, assume exponent 65537)
            // Assume first 128 bytes are the modulus
            if (pubKeyBytes.length >= 128) {
                BigInteger modulus = new BigInteger(1, Arrays.copyOfRange(pubKeyBytes, 0, 128));
                BigInteger exponent = BigInteger.valueOf(65537);
                
                try {
                    RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(modulus, exponent);
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    return keyFactory.generatePublic(rsaSpec);
                } catch (Exception ex) {
                    throw new Exception("Failed to convert public key bytes: " + ex.getMessage());
                }
            }
            throw e;
        }
    }
    
    
    public static boolean showPinDialog(Frame parent) {
        // Check database connection first
        if (DBConnect.getConnection() == null) {
            JOptionPane.showMessageDialog(
                parent,
                "Loi ket noi database!\nVui long kiem tra SQLite JDBC driver.",
                "Loi ket noi",
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        
        // Loop to handle card blocked case
        while (true) {
            // Initialize default PIN if needed
            SettingsService settingsService = new SettingsService();
            settingsService.initializeDefaultPin();
            
            // Show PIN dialog
            PinLoginDialog dialog = new PinLoginDialog(parent);
            dialog.setVisible(true);
            
            if (dialog.isAuthenticated()) {
                // User authenticated successfully
                return true;
            }
            
            if (dialog.isCardBlocked()) {
                // Card is blocked - automatically return to login selection screen
                // Return false so caller can show login selection again
                return false;
            } else {
                // Other error or user cancelled
                return false;
            }
        }
    }
}

