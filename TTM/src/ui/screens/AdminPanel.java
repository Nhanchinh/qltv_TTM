package ui.screens;

import services.SettingsService;
import services.CardService;
import ui.DBConnect;
import smartcard.CardConnectionManager;
import smartcard.CardSetupManager;
import smartcard.CardKeyManager;
import smartcard.CardInfoManager;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Admin Panel - manage PIN reset and card data import
 */
public class AdminPanel extends JPanel {
    
    private JTabbedPane tabbedPane;
    private SettingsService settingsService;
    private CardService cardService;
    private CardConnectionManager connManager;
    private JLabel cardStatusLabel;
    private JButton unlockButton;
    
    // Colors
    private static final Color ADMIN_COLOR = new Color(220, 53, 69);
    private static final Color ADMIN_DARK = new Color(200, 40, 60);
    private static final Color SUCCESS_COLOR = new Color(50, 150, 50);
    
    public AdminPanel() {
        settingsService = new SettingsService();
        cardService = new CardService();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 250));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(ADMIN_COLOR);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(25, 40, 25, 40));
        
        JLabel headerLabel = new JLabel("ADMIN PANEL");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Right side panel: Card status + buttons
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(ADMIN_COLOR);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.add(Box.createHorizontalStrut(20));
        
        // Card status label
        cardStatusLabel = new JLabel("Trạng thái thẻ: Đang kiểm tra...");
        cardStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cardStatusLabel.setForeground(Color.WHITE);
        rightPanel.add(cardStatusLabel);
        
        rightPanel.add(Box.createHorizontalStrut(15));
        
        // Unlock button (hidden by default)
        unlockButton = new JButton("MỞ KHÓA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(255, 152, 0));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(255, 167, 38));
                } else {
                    g2d.setColor(new Color(255, 140, 0));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        unlockButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        unlockButton.setForeground(Color.WHITE);
        unlockButton.setPreferredSize(new Dimension(90, 35));
        unlockButton.setMaximumSize(new Dimension(90, 35));
        unlockButton.setBorderPainted(false);
        unlockButton.setContentAreaFilled(false);
        unlockButton.setFocusPainted(false);
        unlockButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        unlockButton.setVisible(false);
        unlockButton.addActionListener(e -> performUnlock());
        rightPanel.add(unlockButton);
        
        rightPanel.add(Box.createHorizontalStrut(15));
        
        // Logout button
        JButton logoutButton = new JButton("ĐĂNG XUẤT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(220, 100, 100));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(240, 120, 120));
                } else {
                    g2d.setColor(new Color(200, 80, 80));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> handleLogout());
        
        rightPanel.add(logoutButton);
        rightPanel.add(Box.createHorizontalStrut(10));
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(new Color(245, 245, 250));
        
        tabbedPane.addTab("Reset PIN", createResetPINPanel());
        tabbedPane.addTab("Nap Data The", createImportCardDataPanel());
        tabbedPane.addTab("Lay Thong Tin", createGetInfoPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Check card status after UI initialized
        checkCardStatusOnStartup();
    }
    
    /**
     * Handle logout
     */
    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn đăng xuất?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (option == JOptionPane.YES_OPTION) {
            // Close the application and show login again
            javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
            
            // Show login selection dialog again
            int loginMode = LoginSelectDialog.showSelectionDialog(null);
            
            if (loginMode == 0) {
                System.exit(0);
                return;
            }
            
            boolean authenticated = false;
            if (loginMode == 1) {
                authenticated = PinLoginDialog.showPinDialog(null);
            } else if (loginMode == 2) {
                authenticated = AdminLoginDialog.showAdminLoginDialog(null);
            }
            
            if (!authenticated) {
                System.exit(0);
                return;
            }
            
            // Show appropriate interface
            java.awt.EventQueue.invokeLater(() -> {
                if (loginMode == 1) {
                    new MainFrame().setVisible(true);
                } else {
                    new AppFrame(loginMode);
                }
            });
        }
    }
    
    /**
     * Check card status automatically when admin panel loads
     */
    private void checkCardStatusOnStartup() {
        new Thread(() -> {
            try {
                Thread.sleep(500); // Give UI time to render
                connManager = new CardConnectionManager();
                connManager.connectCard();
                
                CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                setupManager.getPublicKey();
                
                byte[] pinTries = setupManager.getPinTries();
                
                SwingUtilities.invokeLater(() -> {
                    if (pinTries != null && pinTries.length > 0) {
                        byte tries = pinTries[0];
                        if (tries >= 3) {
                            // Card is blocked
                            cardStatusLabel.setText("Trạng thái thẻ: THẺ BỊ KHÓA");
                            cardStatusLabel.setForeground(new Color(255, 200, 0));
                            unlockButton.setVisible(true);
                        } else {
                            // Card is normal
                            cardStatusLabel.setText("Trạng thái thẻ: Normal");
                            cardStatusLabel.setForeground(SUCCESS_COLOR);
                            unlockButton.setVisible(false);
                        }
                    }
                });
                
            } catch (Exception e) {
                System.err.println("Error checking card status: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    cardStatusLabel.setText("Trạng thái thẻ: Lỗi");
                    cardStatusLabel.setForeground(new Color(220, 53, 69));
                });
            } finally {
                if (connManager != null) {
                    try {
                        connManager.disconnectCard();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }).start();
    }
    
    /**
     * Perform unlock card operation
     */
    private void performUnlock() {
        String adminPin = JOptionPane.showInputDialog(this, 
            "Nhập mã PIN Admin để mở khóa thẻ:", 
            "");
        
        if (adminPin == null || adminPin.isEmpty()) {
            return;
        }
        
        if (adminPin.length() != 6) {
            JOptionPane.showMessageDialog(this, 
                "Mã PIN phải đúng 6 ký tự!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        new Thread(() -> {
            try {
                connManager = new CardConnectionManager();
                connManager.connectCard();
                
                CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                setupManager.getPublicKey();
                
                // Send unlock command (0x26)
                byte INS_UNBLOCK_PIN = (byte) 0x26;
                byte[] payload = adminPin.getBytes();
                
                byte[] paddedData = new byte[16];
                System.arraycopy(payload, 0, paddedData, 0, Math.min(payload.length, 16));
                
                javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
                keyGen.init(128);
                javax.crypto.SecretKey sessionKey = keyGen.generateKey();
                
                javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance("RSA/ECB/PKCS1Padding");
                rsaCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, setupManager.getKeyManager().getCardPublicKey());
                byte[] encryptedSessionKey = rsaCipher.doFinal(sessionKey.getEncoded());
                
                javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(new byte[16]);
                javax.crypto.Cipher aesCipher = javax.crypto.Cipher.getInstance("AES/CBC/NoPadding");
                aesCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sessionKey, ivSpec);
                byte[] encryptedData = aesCipher.doFinal(paddedData);
                
                byte[] apduData = new byte[encryptedSessionKey.length + encryptedData.length];
                System.arraycopy(encryptedSessionKey, 0, apduData, 0, encryptedSessionKey.length);
                System.arraycopy(encryptedData, 0, apduData, encryptedSessionKey.length, encryptedData.length);
                
                javax.smartcardio.ResponseAPDU response = connManager.getChannel().transmit(
                    new javax.smartcardio.CommandAPDU(0x00, INS_UNBLOCK_PIN, 0x00, 0x00, apduData)
                );
                
                if (response.getSW() == 0x9000) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Mở khóa thẻ thành công!\nSố lần thử đã được đặt lại.", 
                            "Thành công", 
                            JOptionPane.INFORMATION_MESSAGE);
                        cardStatusLabel.setText("Trạng thái thẻ: Normal");
                        cardStatusLabel.setForeground(SUCCESS_COLOR);
                        unlockButton.setVisible(false);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Lỗi mở khóa thẻ. Mã lỗi: " + String.format("0x%04X", response.getSW()), 
                            "Lỗi", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Lỗi: " + e.getMessage(), 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            } finally {
                if (connManager != null) {
                    try {
                        connManager.disconnectCard();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }).start();
    }
    
    /**
     * Create Reset PIN tab
     */
    private JPanel createResetPINPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel("RESET MÃ PIN THẺ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ADMIN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // Admin PIN input
        JLabel adminPINLabel = new JLabel("MÃ PIN ADMIN:");
        adminPINLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        adminPINLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(adminPINLabel);
        
        JPasswordField adminPINField = new JPasswordField();
        adminPINField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        adminPINField.setMaximumSize(new Dimension(300, 45));
        adminPINField.setPreferredSize(new Dimension(300, 45));
        adminPINField.setBorder(new LineBorder(new Color(180, 180, 180), 2));
        adminPINField.setHorizontalAlignment(JTextField.CENTER);
        centerPanel.add(adminPINField);
        centerPanel.add(Box.createVerticalStrut(15));
        
        // New PIN input
        JLabel newPINLabel = new JLabel("MÃ PIN MỚI:");
        newPINLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newPINLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(newPINLabel);
        
        JPasswordField newPINField = new JPasswordField();
        newPINField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        newPINField.setMaximumSize(new Dimension(300, 45));
        newPINField.setPreferredSize(new Dimension(300, 45));
        newPINField.setBorder(new LineBorder(new Color(180, 180, 180), 2));
        newPINField.setHorizontalAlignment(JTextField.CENTER);
        centerPanel.add(newPINField);
        centerPanel.add(Box.createVerticalStrut(15));
        
        // Confirm PIN input
        JLabel confirmPINLabel = new JLabel("XÁC NHẬN MÃ PIN:");
        confirmPINLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmPINLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(confirmPINLabel);
        
        JPasswordField confirmPINField = new JPasswordField();
        confirmPINField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        confirmPINField.setMaximumSize(new Dimension(300, 45));
        confirmPINField.setPreferredSize(new Dimension(300, 45));
        confirmPINField.setBorder(new LineBorder(new Color(180, 180, 180), 2));
        confirmPINField.setHorizontalAlignment(JTextField.CENTER);
        centerPanel.add(confirmPINField);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // Message label
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(messageLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        JButton resetButton = new JButton("RESET PIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(ADMIN_DARK);
                } else if (getModel().isRollover()) {
                    g2d.setColor(ADMIN_COLOR.brighter());
                } else {
                    g2d.setColor(ADMIN_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setForeground(Color.WHITE);
        resetButton.setPreferredSize(new Dimension(150, 50));
        resetButton.setBorderPainted(false);
        resetButton.setContentAreaFilled(false);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> {
            String adminPIN = new String(adminPINField.getPassword()).trim();
            String newPIN = new String(newPINField.getPassword()).trim();
            String confirmPIN = new String(confirmPINField.getPassword()).trim();
            
            if (adminPIN.isEmpty()) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Vui lòng nhập mã PIN admin!");
                return;
            }
            
            if (adminPIN.length() != 6) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Mã PIN admin phải đúng 6 ký tự!");
                return;
            }
            
            if (newPIN.isEmpty()) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Vui lòng nhập mã PIN mới!");
                return;
            }
            
            if (newPIN.length() != 6) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Mã PIN mới phải đúng 6 ký tự!");
                return;
            }
            
            if (confirmPIN.isEmpty()) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Vui lòng xác nhận mã PIN!");
                return;
            }
            
            if (!newPIN.equals(confirmPIN)) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Mã PIN xác nhận không khớp!");
                return;
            }
            
            // Reset PIN on card
            resetPINOnCard(adminPIN, newPIN, adminPINField, newPINField, confirmPINField, messageLabel);
        });
        buttonPanel.add(resetButton);
        
        JButton cancelButton = new JButton("CLEAR") {
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setForeground(new Color(60, 60, 60));
        cancelButton.setPreferredSize(new Dimension(150, 50));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            adminPINField.setText("");
            newPINField.setText("");
            confirmPINField.setText("");
            messageLabel.setText(" ");
        });
        buttonPanel.add(cancelButton);
        
        centerPanel.add(buttonPanel);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create Import Card Data tab
     */
    private JPanel createImportCardDataPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Top panel with controls
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("NẠP DỮ LIỆU THẺ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ADMIN_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        // Create table model and table as instance variables for later access
        String[] columns = {"CardID", "Ten Day Du", "Phone", "DOB", "Loai The", "Tong Chi", "Tong Diem", "Blocked"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(ADMIN_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 220, 255));
        
        JButton addButton = new JButton("THÊM THẺ MỚI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(SUCCESS_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(SUCCESS_COLOR.brighter());
                } else {
                    g2d.setColor(SUCCESS_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(130, 40));
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddCardDialog(tableModel));
        buttonPanel.add(addButton);
        
        topPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BorderLayout());
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Load all cards
        loadCardsToTable(tableModel);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Load all cards from database to table
     */
    private void loadCardsToTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String sql = "SELECT CardID, FullName, Phone, DOB, MemberType, TotalSpent, TotalPoints, IsBlocked FROM Cards";
        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("CardID"),
                    rs.getString("FullName"),
                    rs.getString("Phone"),
                    rs.getString("DOB"),
                    rs.getString("MemberType"),
                    String.format("%.2f", rs.getDouble("TotalSpent")),
                    rs.getInt("TotalPoints"),
                    rs.getInt("IsBlocked") == 1 ? "Yes" : "No"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Show dialog to add new card
     */
    private void showAddCardDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Thẻ Mới", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 15);
        
        // Full Name
        JLabel fullNameLabel = new JLabel("Họ và Tên:");
        fullNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField fullNameField = new JTextField();
        fullNameField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        fullNameField.setPreferredSize(new Dimension(200, 35));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        contentPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(fullNameField, gbc);
        
        // DOB
        JLabel dobLabel = new JLabel("Ngày Sinh (YYYY-MM-DD):");
        dobLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField dobField = new JTextField();
        dobField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        dobField.setPreferredSize(new Dimension(200, 35));
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(dobField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton saveButton = new JButton("LƯU");
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String dob = dobField.getText().trim();
            
            if (fullName.isEmpty() || dob.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Process in background thread
            new Thread(() -> {
                try {
                    // Step 1: Show PIN input dialog
                    String[] pins = showPinInputDialog();
                    if (pins == null || pins.length < 2) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(dialog, "Nhập PIN bị hủy!", "Lỗi", JOptionPane.ERROR_MESSAGE)
                        );
                        return;
                    }
                    
                    String userPin = pins[0];
                    String adminPin = pins[1];
                    
                    // Validate PINs
                    if (userPin.length() != 6 || adminPin.length() != 6) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(dialog, "PIN phải có đúng 6 số!", "Lỗi", JOptionPane.ERROR_MESSAGE)
                        );
                        return;
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        dialog.dispose();
                    });
                    
                    // Step 2: Generate CardID
                    String cardId = generateCardId();
                    
                    // Step 3: Format DOB from YYYY-MM-DD to DDMMYYYY
                    String formattedDob = convertDateFormat(dob);
                    String regDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
                    
                    // Step 4: Setup card (connect, getPublicKey, setupCard, initUserData)
                    connManager = new CardConnectionManager();
                    connManager.connectCard();
                    
                    CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                    setupManager.getPublicKey();
                    setupManager.setupCard(userPin, adminPin);
                    setupManager.verifyPin(userPin);  // REQUIRED: Verify PIN after setup
                    setupManager.initUserData(cardId, fullName, formattedDob, regDate);
                    
                    connManager.disconnectCard();
                    
                    // Step 5: Insert to database
                    if (insertCard(cardId, fullName, "", dob, "Basic", null)) {
                        // Step 6: Save card public key to database AFTER successful insert
                        try {
                            byte[] pubBytes = setupManager.getKeyManager().getCardPublicKeyEncoded();
                            if (pubBytes != null) {
                                cardService.updateCardPublicKey(cardId, pubBytes);
                            }
                        } catch (Exception _e) {
                            System.err.println("Warning: failed to save card public key to DB: " + _e.getMessage());
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(AdminPanel.this),
                                "Thêm thẻ thành công!\nCardID: " + cardId + 
                                "\nUser PIN: " + userPin +
                                "\nAdmin PIN: " + adminPin, 
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            loadCardsToTable(tableModel);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(AdminPanel.this),
                                "Lỗi khi thêm thẻ vào database!", "Lỗi", JOptionPane.ERROR_MESSAGE)
                        );
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(AdminPanel.this),
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE)
                    );
                    ex.printStackTrace();
                }
            }).start();
        });
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("HỦY");
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(buttonPanel, gbc);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Insert new card to database
     */
    /**
     * Insert new card to database
     */
    private boolean insertCard(String cardId, String fullName, String phone, String dob, String memberType, String imagePath) {
        String sql = "INSERT INTO Cards (CardID, FullName, Phone, DOB, RegisterDate, MemberType, TotalSpent, TotalPoints, FineDebt, IsBlocked, CreatedAt, UpdatedAt) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, 0, datetime('now'), datetime('now'))";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, phone);
            pstmt.setString(4, dob);
            pstmt.setString(5, LocalDate.now().toString());
            pstmt.setString(6, memberType);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi them the: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Show PIN confirmation dialog
     */
    /**
     * Reset PIN on smart card
     */
    private void resetPINOnCard(String adminPIN, String newUserPIN, 
                                JPasswordField adminPINField, 
                                JPasswordField newPINField, 
                                JPasswordField confirmPINField, 
                                JLabel messageLabel) {
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    messageLabel.setForeground(new Color(0, 123, 255));
                    messageLabel.setText("Đang kết nối thẻ...");
                });
                
                // Connect to card (like other functions)
                connManager = new CardConnectionManager();
                connManager.connectCard();
                
                SwingUtilities.invokeLater(() -> {
                    messageLabel.setText("Đang reset PIN trên thẻ...");
                });
                
                // Create PIN manager and reset
                smartcard.CardPinManager pinManager = new smartcard.CardPinManager(connManager.getChannel());
                boolean success = pinManager.resetUserPin(adminPIN, newUserPIN);
                
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        messageLabel.setForeground(SUCCESS_COLOR);
                        messageLabel.setText("Reset PIN thành công! PIN mới: " + newUserPIN);
                        adminPINField.setText("");
                        newPINField.setText("");
                        confirmPINField.setText("");
                    });
                } else {
                    throw new Exception("Reset PIN thất bại");
                }
                
                // Disconnect
                connManager.disconnectCard();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    messageLabel.setForeground(new Color(220, 53, 69));
                    messageLabel.setText("Lỗi: " + ex.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Show PIN confirmation dialog (DEPRECATED - No longer used)
     */
    private void showPINConfirmationDialog(String newPIN, JPasswordField newPINField, JPasswordField confirmPINField, JLabel messageLabel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xac Nhan PIN", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        contentPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Xac Nhan Reset PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ADMIN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Message
        JLabel messageLabel2 = new JLabel("Hay nhap ma PIN hien tai de xac nhan:");
        messageLabel2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(messageLabel2);
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        // PIN input
        JPasswordField confirmationPINField = new JPasswordField();
        confirmationPINField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        confirmationPINField.setMaximumSize(new Dimension(250, 45));
        confirmationPINField.setPreferredSize(new Dimension(250, 45));
        confirmationPINField.setBorder(new LineBorder(new Color(180, 180, 180), 2));
        confirmationPINField.setHorizontalAlignment(JTextField.CENTER);
        confirmationPINField.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(confirmationPINField);
        
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Error label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 53, 69));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(errorLabel);
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton confirmButton = new JButton("XAC NHAN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(SUCCESS_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(SUCCESS_COLOR.brighter());
                } else {
                    g2d.setColor(SUCCESS_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setPreferredSize(new Dimension(130, 45));
        confirmButton.setBorderPainted(false);
        confirmButton.setContentAreaFilled(false);
        confirmButton.setFocusPainted(false);
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmButton.addActionListener(e -> {
            String enteredPIN = new String(confirmationPINField.getPassword());
            if (enteredPIN.isEmpty()) {
                errorLabel.setText("Vui long nhap ma PIN!");
                return;
            }
            
            // Get current PIN from settings
            String currentPIN = settingsService.getSetting("app_pin");
            if (currentPIN == null || currentPIN.isEmpty()) {
                currentPIN = "1234";
            }
            
            if (!enteredPIN.equals(currentPIN)) {
                errorLabel.setText("Ma PIN khong dung!");
                confirmationPINField.setText("");
                confirmationPINField.requestFocus();
                return;
            }
            
            // PIN correct, proceed with reset
            if (settingsService.setSetting("app_pin", newPIN)) {
                dialog.dispose();
                messageLabel.setForeground(SUCCESS_COLOR);
                messageLabel.setText("Reset ma PIN thanh cong!");
                newPINField.setText("");
                confirmPINField.setText("");
            } else {
                errorLabel.setText("Loi khi reset ma PIN!");
            }
        });
        buttonPanel.add(confirmButton);
        
        JButton cancelButton = new JButton("HUY") {
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setForeground(new Color(60, 60, 60));
        cancelButton.setPreferredSize(new Dimension(130, 45));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);
        
        contentPanel.add(buttonPanel);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Generate unique Card ID with format CARDLIBRARY + 6-digit counter
     */
    /**
     * Generate unique Card ID with format CARDLIBRARY + 5-digit counter
     * Limited to 16 characters total
     */
    private String generateCardId() {
        String prefix = "CARD";
        long counter = System.currentTimeMillis() % 1000000000000L; // 12 digits max
        String cardId = prefix + String.format("%012d", counter);
        
        // Ensure exactly 16 characters
        if (cardId.length() > 16) {
            cardId = cardId.substring(0, 16);
        }
        
        return cardId;
    }
    
    /**
     * Convert date format from YYYY-MM-DD to DDMMYYYY
     */
    private String convertDateFormat(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) {
                return "";
            }
            String[] parts = dateStr.split("-");
            if (parts.length != 3) {
                return "";
            }
            return parts[2] + parts[1] + parts[0]; // DD + MM + YYYY
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Create Get User Info tab
     */
    private JPanel createGetInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel("LẤY THÔNG TIN NGƯỜI DÙNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ADMIN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // Info text area
        JTextArea infoArea = new JTextArea();
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        infoArea.setBackground(Color.WHITE);
        infoArea.setPreferredSize(new Dimension(300, 150));
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        centerPanel.add(scrollPane);
        centerPanel.add(Box.createVerticalStrut(20));
        
        // Get Info button
        JButton getInfoButton = new JButton("LẤY THÔNG TIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(SUCCESS_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(SUCCESS_COLOR.brighter());
                } else {
                    g2d.setColor(SUCCESS_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        getInfoButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        getInfoButton.setForeground(Color.WHITE);
        getInfoButton.setPreferredSize(new Dimension(200, 45));
        getInfoButton.setMaximumSize(new Dimension(200, 45));
        getInfoButton.setBorderPainted(false);
        getInfoButton.setContentAreaFilled(false);
        getInfoButton.setFocusPainted(false);
        getInfoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        getInfoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        getInfoButton.addActionListener(e -> {
            // Run in background thread
            new Thread(() -> {
                try {
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText("Đang kết nối đến thẻ...");
                    });
                    
                    // Connect to card
                    connManager = new CardConnectionManager();
                    connManager.connectCard();
                    
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText("Đang lấy thông tin...");
                    });
                    
                    // Get info using CardKeyManager and CardInfoManager
                    CardKeyManager keyManager = new CardKeyManager(connManager.getChannel());
                    keyManager.getPublicKey();
                    
                    // Load app keypair from file (don't generate new one!)
                    if (!keyManager.loadAppKeyPair()) {
                        throw new Exception("Không tìm thấy app keypair. Vui lòng thêm thẻ mới trước.");
                    }
                    
                    CardInfoManager infoManager = new CardInfoManager(connManager.getChannel(), keyManager);
                    CardInfoManager.UserInfo userInfo = infoManager.getInfo();
                    
                    // Save card public key into DB for this card
                    try {
                        byte[] pubBytes = keyManager.getCardPublicKeyEncoded();
                        if (pubBytes != null && userInfo != null && userInfo.cardId != null && !userInfo.cardId.isEmpty()) {
                            cardService.updateCardPublicKey(userInfo.cardId, pubBytes);
                        }
                    } catch (Exception _e) {
                        System.err.println("Warning: failed to save card public key to DB: " + _e.getMessage());
                    }
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText(userInfo.toString());
                    });
                    
                    // Disconnect
                    connManager.disconnectCard();
                    
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText("Lỗi: " + ex.getMessage());
                        JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(centerPanel),
                            "Lỗi khi lấy thông tin: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                    ex.printStackTrace();
                }
            }).start();
        });
        
        centerPanel.add(getInfoButton);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Show PIN input dialog for user and admin
     * @return String array [userPin, adminPin] or null if cancelled
     */
    private String[] showPinInputDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nhập Mã PIN", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        contentPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Nhập Mã PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ADMIN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // User PIN
        JLabel userPinLabel = new JLabel("PIN Người Dùng (6 số):");
        userPinLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userPinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(userPinLabel);
        
        JPasswordField userPinField = new JPasswordField();
        userPinField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        userPinField.setPreferredSize(new Dimension(300, 35));
        userPinField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        contentPanel.add(userPinField);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Admin PIN
        JLabel adminPinLabel = new JLabel("PIN Admin (6 số):");
        adminPinLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminPinLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(adminPinLabel);
        
        JPasswordField adminPinField = new JPasswordField();
        adminPinField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        adminPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        adminPinField.setPreferredSize(new Dimension(300, 35));
        adminPinField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        contentPanel.add(adminPinField);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Error label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 53, 69));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(errorLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        final String[][] result = {{null, null}};
        
        JButton okButton = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(SUCCESS_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(SUCCESS_COLOR.brighter());
                } else {
                    g2d.setColor(SUCCESS_COLOR);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        okButton.setForeground(Color.WHITE);
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        okButton.addActionListener(e -> {
            String userPin = new String(userPinField.getPassword());
            String adminPin = new String(adminPinField.getPassword());
            
            if (userPin.isEmpty() || adminPin.isEmpty()) {
                errorLabel.setText("Vui lòng nhập cả 2 mã PIN!");
                return;
            }
            
            if (userPin.length() != 6 || adminPin.length() != 6) {
                errorLabel.setText("Mỗi mã PIN phải có đúng 6 ký tự!");
                return;
            }
            
            result[0][0] = userPin;
            result[0][1] = adminPin;
            dialog.dispose();
        });
        
        buttonPanel.add(okButton);
        
        JButton cancelButton = new JButton("HỦY") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(190, 190, 190));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(220, 220, 220));
                } else {
                    g2d.setColor(new Color(200, 200, 200));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setForeground(new Color(60, 60, 60));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelButton);
        contentPanel.add(buttonPanel);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
        
        return (result[0][0] != null && result[0][1] != null) ? result[0] : null;
    }
}