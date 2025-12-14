package ui.screens;

import services.SettingsService;
import services.CardService;
import ui.DBConnect;
import smartcard.CardConnectionManager;
import smartcard.CardSetupManager;
import smartcard.CardKeyManager;
import smartcard.CardInfoManager;
import smartcard.CardImageManager;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
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

        tabbedPane.addTab("Đổi Mã PIN", createResetPINPanel());
        tabbedPane.addTab("Nạp Dữ Liệu Thẻ", createImportCardDataPanel());
        tabbedPane.addTab("Lấy Thông Tin", createGetInfoPanel());
        tabbedPane.addTab("Thêm Sách", createAddBookPanel());

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
                JOptionPane.QUESTION_MESSAGE);
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
            boolean cardBlocked = false;
            if (loginMode == 1) {
                PinLoginDialog.LoginResult result = PinLoginDialog.showPinDialog(null);
                cardBlocked = result == PinLoginDialog.LoginResult.CARD_BLOCKED;
                authenticated = result == PinLoginDialog.LoginResult.SUCCESS;
            } else if (loginMode == 2) {
                authenticated = AdminLoginDialog.showAdminLoginDialog(null);
            }

            if (cardBlocked) {
                java.awt.EventQueue.invokeLater(() -> new CardConnectionPanel().setVisible(true));
                return;
            }

            if (!authenticated) {
                System.exit(0);
                return;
            }

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

                // Gửi lệnh UNBLOCK (0x26) dạng PLAINTEXT giống
                // BookstoreClientTest.unblockCard()
                byte INS_UNBLOCK_PIN = (byte) 0x26;
                byte[] payload = new byte[6];
                System.arraycopy(adminPin.getBytes(), 0, payload, 0, 6);

                javax.smartcardio.ResponseAPDU response = connManager.getChannel().transmit(
                        new javax.smartcardio.CommandAPDU(0x00, INS_UNBLOCK_PIN, 0x00, 0x00, payload));

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
        JLabel titleLabel = new JLabel("ĐỔI MÃ PIN THẺ");
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
        adminPINField.setMaximumSize(new Dimension(360, 45));
        adminPINField.setPreferredSize(new Dimension(360, 45));
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
        newPINField.setMaximumSize(new Dimension(360, 45));
        newPINField.setPreferredSize(new Dimension(360, 45));
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
        confirmPINField.setMaximumSize(new Dimension(360, 45));
        confirmPINField.setPreferredSize(new Dimension(360, 45));
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

        JButton resetButton = new JButton("ĐỔI MÃ PIN") {
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
        String[] columns = { "CardID", "Ten Day Du", "Phone", "DOB", "Loai The", "Tong Chi", "Tong Diem", "Blocked",
                "Hành Động" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing the Action column (index 8)
                return column == 8;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(ADMIN_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 220, 255));

        // Add Button Renderer and Editor for the "Hành Động" column
        table.getColumn("Hành Động").setCellRenderer(new ButtonRenderer());
        table.getColumn("Hành Động").setCellEditor(new ButtonEditor(new JCheckBox(), tableModel));

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
                        rs.getInt("IsBlocked") == 1 ? "Yes" : "No",
                        "Sửa" // Action column value
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
        dialog.setSize(760, 520);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(true);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        // Giảm padding dưới để không che nút, giữ top/left/right đẹp
        contentPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        contentPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 15);

        // Full Name
        JLabel fullNameLabel = new JLabel("Họ và Tên:");
        fullNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField fullNameField = new JTextField();
        fullNameField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        fullNameField.setPreferredSize(new Dimension(360, 40));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        contentPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(fullNameField, gbc);

        // Phone
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField phoneField = new JTextField();
        phoneField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        phoneField.setPreferredSize(new Dimension(240, 40));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(phoneField, gbc);

        // DOB
        JLabel dobLabel = new JLabel("Ngày Sinh (YYYY-MM-DD):");
        dobLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField dobField = new JTextField();
        dobField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        dobField.setPreferredSize(new Dimension(200, 40));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(dobField, gbc);

        // Address
        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField addressField = new JTextField();
        addressField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        addressField.setPreferredSize(new Dimension(360, 40));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(addressLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(addressField, gbc);

        // Image chooser (optional)
        JLabel imageLabelTitle = new JLabel("Ảnh đại diện:");
        imageLabelTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(200, 140));
        imagePreview.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        JButton chooseImageButton = new JButton("Chọn Ảnh");
        chooseImageButton.setPreferredSize(new Dimension(130, 32));

        final String[] selectedImage = new String[1];

        chooseImageButton.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg",
                    "jpeg", "png", "gif", "bmp"));
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedImage[0] = f.getAbsolutePath();
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(imagePreview.getWidth(), imagePreview.getHeight(),
                                Image.SCALE_SMOOTH);
                        imagePreview.setIcon(new ImageIcon(scaled));
                    } else {
                        imagePreview.setIcon(null);
                    }
                } catch (Exception ex) {
                    imagePreview.setIcon(null);
                }
            }
        });

        JPanel imageRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        imageRow.setOpaque(false);
        imageRow.add(chooseImageButton);
        imageRow.add(imagePreview);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(imageLabelTitle, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(imageRow, gbc);
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton saveButton = new JButton("LƯU");
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String dob = dobField.getText().trim();
            String address = addressField.getText().trim();

            if (fullName.isEmpty() || dob.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ Họ tên, SĐT, Ngày sinh và Địa chỉ!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Process in background thread
            new Thread(() -> {
                try {
                    // Step 1: Show PIN input dialog
                    String[] pins = showPinInputDialog();
                    if (pins == null || pins.length < 2) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "Nhập PIN bị hủy!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE));
                        return;
                    }

                    String userPin = pins[0];
                    String adminPin = pins[1];

                    // Validate PINs
                    if (userPin.length() != 6 || adminPin.length() != 6) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "PIN phải có đúng 6 số!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE));
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
                    setupManager.verifyPin(userPin); // REQUIRED: Verify PIN after setup
                    setupManager.initUserData(cardId, fullName, formattedDob, phone, address, regDate);

                    // Step 4.5: Upload image to card if selected
                    boolean imageUploaded = false;
                    if (selectedImage[0] != null && !selectedImage[0].isEmpty()) {
                        File imageFile = new File(selectedImage[0]);
                        if (imageFile.exists()) {
                            System.out.println("[ADD_CARD] Uploading image to card...");
                            CardImageManager imageManager = new CardImageManager(connManager.getChannel());
                            imageUploaded = imageManager.uploadImage(imageFile);
                            if (imageUploaded) {
                                System.out.println("[ADD_CARD] Image uploaded successfully!");
                            } else {
                                System.out.println("[ADD_CARD] Image upload failed!");
                            }
                        }
                    }

                    connManager.disconnectCard();

                    // Step 5: Insert to database
                    final boolean imgUploaded = imageUploaded;
                    if (insertCard(cardId, fullName, phone, dob, address, "Basic", selectedImage[0])) {
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
                            String imageStatus = "";
                            if (selectedImage[0] != null && !selectedImage[0].isEmpty()) {
                                imageStatus = imgUploaded ? "\nẢnh: Đã upload lên thẻ ✓" : "\nẢnh: Upload thất bại ✗";
                            }
                            JOptionPane.showMessageDialog(
                                    SwingUtilities.getWindowAncestor(AdminPanel.this),
                                    "Thêm thẻ thành công!\nCardID: " + cardId +
                                            "\nUser PIN: " + userPin +
                                            "\nAdmin PIN: " + adminPin + imageStatus,
                                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            loadCardsToTable(tableModel);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(AdminPanel.this),
                                "Lỗi khi thêm thẻ vào database!", "Lỗi", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(AdminPanel.this),
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
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
        gbc.gridy = 5;
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
    private boolean insertCard(String cardId, String fullName, String phone, String dob, String address,
            String memberType, String imagePath) {
        // Detect whether the Cards table has an ImagePath column. If yes, include it in
        // insert.
        try (Connection conn = DBConnect.getConnection()) {
            boolean hasImagePath = false;
            try (Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery("PRAGMA table_info(Cards);")) {
                while (rs.next()) {
                    String colName = rs.getString("name");
                    if ("ImagePath".equalsIgnoreCase(colName)) {
                        hasImagePath = true;
                        break;
                    }
                }
            }

            String sql;
            if (hasImagePath) {
                sql = "INSERT INTO Cards (CardID, FullName, Phone, Address, DOB, RegisterDate, MemberType, ImagePath, TotalSpent, TotalPoints, FineDebt, IsBlocked, CreatedAt, UpdatedAt) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, datetime('now'), datetime('now'))";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, cardId);
                    pstmt.setString(2, fullName);
                    pstmt.setString(3, phone);
                    pstmt.setString(4, address == null ? "" : address);
                    pstmt.setString(5, dob);
                    pstmt.setString(6, LocalDate.now().toString());
                    pstmt.setString(7, memberType);
                    pstmt.setString(8, imagePath == null ? "" : imagePath);
                    return pstmt.executeUpdate() > 0;
                }
            } else {
                sql = "INSERT INTO Cards (CardID, FullName, Phone, Address, DOB, RegisterDate, MemberType, TotalSpent, TotalPoints, FineDebt, IsBlocked, CreatedAt, UpdatedAt) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, datetime('now'), datetime('now'))";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, cardId);
                    pstmt.setString(2, fullName);
                    pstmt.setString(3, phone);
                    pstmt.setString(4, address == null ? "" : address);
                    pstmt.setString(5, dob);
                    pstmt.setString(6, LocalDate.now().toString());
                    pstmt.setString(7, memberType);
                    return pstmt.executeUpdate() > 0;
                }
            }
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
                    messageLabel.setText("Đang đổi mã PIN trên thẻ...");
                });

                // Create PIN manager and reset
                smartcard.CardPinManager pinManager = new smartcard.CardPinManager(connManager.getChannel());
                boolean success = pinManager.resetUserPin(adminPIN, newUserPIN);

                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        messageLabel.setForeground(SUCCESS_COLOR);
                        messageLabel.setText("Đổi mã PIN thành công! Mã PIN mới: " + newUserPIN);
                        adminPINField.setText("");
                        newPINField.setText("");
                        confirmPINField.setText("");
                    });
                } else {
                    throw new Exception("Đổi mã PIN thất bại");
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
    private void showPINConfirmationDialog(String newPIN, JPasswordField newPINField, JPasswordField confirmPINField,
            JLabel messageLabel) {
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
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel titleLabel = new JLabel("LẤY THÔNG TIN NGƯỜI DÙNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ADMIN_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Main content panel (horizontal layout)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Left side - Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)));
        imagePanel.setPreferredSize(new Dimension(250, 300));

        JLabel imageLabel = new JLabel("Ảnh thẻ", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        imageLabel.setForeground(new Color(150, 150, 150));
        imageLabel.setPreferredSize(new Dimension(230, 250));
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        JLabel imageTitleLabel = new JLabel("ẢNH THẺ", SwingConstants.CENTER);
        imageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imageTitleLabel.setForeground(ADMIN_COLOR);
        imagePanel.add(imageTitleLabel, BorderLayout.NORTH);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 20);
        gbc.anchor = GridBagConstraints.NORTH;
        contentPanel.add(imagePanel, gbc);

        // Right side - Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Info text area
        JTextArea infoArea = new JTextArea();
        infoArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoArea.setBackground(Color.WHITE);
        infoArea.setText("Nhấn nút 'LẤY THÔNG TIN' để đọc dữ liệu từ thẻ...");

        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        infoPanel.add(scrollPane);
        infoPanel.add(Box.createVerticalStrut(20));

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
                        imageLabel.setIcon(null);
                        imageLabel.setText("Đang tải...");
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
                        if (pubBytes != null && userInfo != null && userInfo.cardId != null
                                && !userInfo.cardId.isEmpty()) {
                            cardService.updateCardPublicKey(userInfo.cardId, pubBytes);
                        }
                    } catch (Exception _e) {
                        System.err.println("Warning: failed to save card public key to DB: " + _e.getMessage());
                    }

                    // Get image from card
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText(userInfo.toString() + "\n\nĐang tải ảnh từ thẻ...");
                    });

                    CardImageManager imageManager = new CardImageManager(connManager.getChannel());
                    byte[] imageData = imageManager.downloadImage();

                    // Disconnect
                    connManager.disconnectCard();

                    // Update UI
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText(userInfo.toString());

                        // Display image
                        if (imageData != null && imageManager.isValidJpeg(imageData)) {
                            try {
                                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                                if (img != null) {
                                    // Scale image to fit
                                    int maxW = 220;
                                    int maxH = 240;
                                    int w = img.getWidth();
                                    int h = img.getHeight();
                                    double scale = Math.min((double) maxW / w, (double) maxH / h);
                                    int newW = (int) (w * scale);
                                    int newH = (int) (h * scale);

                                    Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                                    imageLabel.setIcon(new ImageIcon(scaled));
                                    imageLabel.setText("");
                                } else {
                                    imageLabel.setIcon(null);
                                    imageLabel.setText("Không thể đọc ảnh");
                                }
                            } catch (Exception imgEx) {
                                imageLabel.setIcon(null);
                                imageLabel.setText("Lỗi: " + imgEx.getMessage());
                            }
                        } else {
                            imageLabel.setIcon(null);
                            imageLabel.setText("Thẻ chưa có ảnh");
                        }
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText("Lỗi: " + ex.getMessage());
                        imageLabel.setIcon(null);
                        imageLabel.setText("Lỗi");
                        JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(contentPanel),
                                "Lỗi khi lấy thông tin: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    });
                    ex.printStackTrace();
                }
            }).start();
        });

        infoPanel.add(getInfoButton);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(infoPanel, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create Add Book tab
     */
    private JPanel createAddBookPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel titleLabel = new JLabel("THÊM SÁCH MỚI");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ADMIN_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Book ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Mã Sách:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField bookIdField = createTextField(20);
        bookIdField.setText(generateBookId());
        formPanel.add(bookIdField, gbc);

        // Title
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Tên Sách:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField titleField = createTextField(30);
        formPanel.add(titleField, gbc);

        // Author
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Tác Giả:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField authorField = createTextField(30);
        formPanel.add(authorField, gbc);

        // Publisher
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Nhà Xuất Bản:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField publisherField = createTextField(30);
        formPanel.add(publisherField, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Giá (VNĐ):"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField priceField = createTextField(15);
        priceField.setText("0");
        formPanel.add(priceField, gbc);

        // Stock
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Số Lượng:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField stockField = createTextField(10);
        stockField.setText("1");
        formPanel.add(stockField, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Thể Loại:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        String[] categories = { "Văn học", "Khoa học", "Thiếu nhi", "Manga", "Self-help", "Lập trình", "Kinh tế",
                "Tâm lý", "Lịch sử", "Khác" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryCombo.setPreferredSize(new Dimension(200, 35));
        formPanel.add(categoryCombo, gbc);

        // Image path
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(createLabel("Ảnh Bìa:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        imagePanel.setOpaque(false);
        JTextField imagePathField = createTextField(20);
        imagePathField.setEditable(false);
        JButton browseButton = new JButton("Chọn...");
        browseButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                imagePathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        imagePanel.add(imagePathField);
        imagePanel.add(Box.createHorizontalStrut(10));
        imagePanel.add(browseButton);
        formPanel.add(imagePanel, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 10, 10, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        // Add button
        JButton addButton = new JButton("THÊM SÁCH") {
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
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(150, 45));
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addButton.addActionListener(e -> {
            String bookId = bookIdField.getText().trim();
            String bookTitle = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String imagePath = imagePathField.getText().trim();

            // Validation
            if (bookId.isEmpty() || bookTitle.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Vui lòng điền đầy đủ Mã sách, Tên sách và Tác giả!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double price;
            int stock;
            try {
                price = Double.parseDouble(priceStr);
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Giá và Số lượng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert to database
            if (insertBook(bookId, bookTitle, author, publisher, price, stock, category, imagePath)) {
                JOptionPane.showMessageDialog(panel, "Thêm sách thành công!\nMã sách: " + bookId, "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                // Reset form
                bookIdField.setText(generateBookId());
                titleField.setText("");
                authorField.setText("");
                publisherField.setText("");
                priceField.setText("0");
                stockField.setText("1");
                categoryCombo.setSelectedIndex(0);
                imagePathField.setText("");
            } else {
                JOptionPane.showMessageDialog(panel, "Lỗi khi thêm sách vào database!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Clear button
        JButton clearButton = new JButton("XÓA FORM") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(Color.GRAY.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(Color.GRAY.brighter());
                } else {
                    g2d.setColor(Color.GRAY);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearButton.setForeground(Color.WHITE);
        clearButton.setPreferredSize(new Dimension(120, 45));
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        clearButton.addActionListener(e -> {
            bookIdField.setText(generateBookId());
            titleField.setText("");
            authorField.setText("");
            publisherField.setText("");
            priceField.setText("0");
            stockField.setText("1");
            categoryCombo.setSelectedIndex(0);
            imagePathField.setText("");
        });

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        formPanel.add(buttonPanel, gbc);

        // Center the form
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.setOpaque(false);
        centerWrapper.add(formPanel);

        panel.add(centerWrapper, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Helper: Create styled label
     */
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return label;
    }

    /**
     * Helper: Create styled text field
     */
    private JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 10, 5, 10)));
        return field;
    }

    /**
     * Generate unique Book ID (B00001 - B99999)
     */
    private String generateBookId() {
        int nextNum = 1;
        String sql = "SELECT MAX(CAST(SUBSTR(BookID, 2) AS INTEGER)) as maxNum FROM Books WHERE BookID LIKE 'B%'";
        try (Connection conn = DBConnect.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int maxNum = rs.getInt("maxNum");
                if (!rs.wasNull()) {
                    nextNum = maxNum + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi sinh BookID: " + e.getMessage());
        }
        return "B" + String.format("%05d", nextNum);
    }

    /**
     * Insert book to database
     */
    private boolean insertBook(String bookId, String title, String author, String publisher,
            double price, int stock, String category, String imagePath) {
        String sql = "INSERT INTO Books (BookID, Title, Author, Publisher, Price, Stock, BorrowStock, Category, ImagePath) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setString(4, publisher);
            pstmt.setDouble(5, price);
            pstmt.setInt(6, stock);
            pstmt.setString(7, category);
            pstmt.setString(8, imagePath.isEmpty() ? null : imagePath);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm sách: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Show PIN input dialog for user and admin
     * 
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

        final String[][] result = { { null, null } };

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

    /**
     * Get card details by ID
     */
    private java.util.Map<String, String> getCardDetails(String cardId) {
        java.util.Map<String, String> details = new java.util.HashMap<>();
        String sql = "SELECT * FROM Cards WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    details.put("CardID", rs.getString("CardID"));
                    details.put("FullName", rs.getString("FullName"));
                    details.put("Phone", rs.getString("Phone"));
                    details.put("Address", rs.getString("Address"));
                    details.put("DOB", rs.getString("DOB"));
                    details.put("ImagePath", rs.getString("ImagePath"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    /**
     * Update card information
     */
    private boolean updateCard(String cardId, String fullName, String phone, String address, String dob,
            String imagePath) {
        String sql = "UPDATE Cards SET FullName = ?, Phone = ?, Address = ?, DOB = ?, UpdatedAt = datetime('now')";
        if (imagePath != null) {
            sql += ", ImagePath = ?";
        }
        sql += " WHERE CardID = ?";

        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, phone);
            pstmt.setString(3, address == null ? "" : address);
            pstmt.setString(4, dob);

            if (imagePath != null) {
                pstmt.setString(5, imagePath);
                pstmt.setString(6, cardId);
            } else {
                pstmt.setString(5, cardId);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating card: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Show dialog to edit card
     */
    private void showEditCardDialog(String cardId, DefaultTableModel tableModel) {
        java.util.Map<String, String> details = getCardDetails(cardId);
        if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin thẻ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Thông Tin Thẻ", true);
        dialog.setSize(1000, 700);
        dialog.setMinimumSize(new java.awt.Dimension(800, 500));
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(true);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        contentPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 15);

        // Card ID (Read only)
        JLabel cardIdLabel = new JLabel("Mã Thẻ:");
        cardIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField cardIdField = new JTextField(details.get("CardID"));
        cardIdField.setEditable(false);
        cardIdField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        cardIdField.setPreferredSize(new Dimension(600, 40));
        cardIdField.setBackground(new Color(230, 230, 230));
        cardIdField.setEditable(false);
        cardIdField.setFocusable(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        contentPanel.add(cardIdLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(cardIdField, gbc);

        // Full Name
        JLabel fullNameLabel = new JLabel("Họ và Tên:");
        fullNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField fullNameField = new JTextField(details.get("FullName"));
        fullNameField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        fullNameField.setPreferredSize(new Dimension(600, 40));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(fullNameField, gbc);

        // Phone
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField phoneField = new JTextField(details.get("Phone"));
        phoneField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        phoneField.setPreferredSize(new Dimension(360, 40));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(phoneField, gbc);

        // DOB
        JLabel dobLabel = new JLabel("Ngày Sinh (YYYY-MM-DD):");
        dobLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField dobField = new JTextField(details.get("DOB"));
        dobField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        dobField.setPreferredSize(new Dimension(200, 40));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(dobField, gbc);

        // Address
        JLabel addressLabel = new JLabel("Địa chỉ:");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField addressField = new JTextField(details.get("Address"));
        addressField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        addressField.setPreferredSize(new Dimension(360, 40));

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(addressLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(addressField, gbc);

        // Image chooser
        JLabel imageLabelTitle = new JLabel("Ảnh đại diện:");
        imageLabelTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(200, 140));
        imagePreview.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        JButton chooseImageButton = new JButton("Đổi Ảnh");
        chooseImageButton.setPreferredSize(new Dimension(130, 32));

        final String[] selectedImage = new String[1];
        selectedImage[0] = details.get("ImagePath");

        // Load existing image if available
        if (selectedImage[0] != null && !selectedImage[0].isEmpty()) {
            File f = new File(selectedImage[0]);
            if (f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(200, 140, Image.SCALE_SMOOTH);
                        imagePreview.setIcon(new ImageIcon(scaled));
                    }
                } catch (Exception ex) {
                }
            }
        }

        chooseImageButton.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "jpeg", "png"));
            int res = chooser.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedImage[0] = f.getAbsolutePath();
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(200, 140, Image.SCALE_SMOOTH);
                        imagePreview.setIcon(new ImageIcon(scaled));
                    }
                } catch (Exception ex) {
                    imagePreview.setIcon(null);
                }
            }
        });

        JPanel imageRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        imageRow.setOpaque(false);
        imageRow.add(chooseImageButton);
        imageRow.add(imagePreview);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(imageLabelTitle, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(imageRow, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton saveButton = new JButton("LƯU THAY ĐỔI");
        saveButton.setPreferredSize(new Dimension(140, 40));
        saveButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String dob = dobField.getText().trim();
            String address = addressField.getText().trim();

            if (fullName.isEmpty() || dob.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (updateCard(cardId, fullName, phone, address, dob, selectedImage[0])) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật thông tin thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadCardsToTable(tableModel);
            } else {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi cập nhật thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);

        JButton cancelButton = new JButton("HỦY");
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(buttonPanel, gbc);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    // Button Renderer for JTable
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("Sửa");
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBackground(new Color(0, 123, 255));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Button Editor for JTable
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String cardId;
        private boolean isPushed;
        private DefaultTableModel tableModel;

        public ButtonEditor(JCheckBox checkBox, DefaultTableModel model) {
            super(checkBox);
            this.tableModel = model;
            button = new JButton();
            button.setOpaque(true);
            button.setText("Sửa");
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setBackground(table.getBackground());
            }

            cardId = (String) table.getValueAt(row, 0);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                showEditCardDialog(cardId, tableModel);
            }
            isPushed = false;
            return "Sửa";
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
