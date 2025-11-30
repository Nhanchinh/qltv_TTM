package ui.screens;

import services.SettingsService;
import services.CardService;
import ui.DBConnect;
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
        
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(new Color(245, 245, 250));
        
        tabbedPane.addTab("Reset PIN", createResetPINPanel());
        tabbedPane.addTab("Nap Data The", createImportCardDataPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
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
            String newPIN = new String(newPINField.getPassword()).trim();
            String confirmPIN = new String(confirmPINField.getPassword()).trim();
            
            if (newPIN.isEmpty()) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Vui lòng nhập mã PIN mới!");
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
            
            // Show confirmation dialog
            showPINConfirmationDialog(newPIN, newPINField, confirmPINField, messageLabel);
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
        dialog.setSize(600, 600);
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
        
        // Phone
        JLabel phoneLabel = new JLabel("Số Điện Thoại:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField phoneField = new JTextField();
        phoneField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        phoneField.setPreferredSize(new Dimension(200, 35));
        
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
        dobField.setPreferredSize(new Dimension(200, 35));
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(dobField, gbc);
        
        // Member Type
        JLabel memberTypeLabel = new JLabel("Loại Thẻ:");
        memberTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField memberTypeField = new JTextField();
        memberTypeField.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        memberTypeField.setPreferredSize(new Dimension(200, 35));
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(memberTypeLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(memberTypeField, gbc);
        
        // Photo upload
        JLabel photoLabel = new JLabel("Ảnh:");
        photoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        photoPanel.setOpaque(false);
        
        JLabel photoPathLabel = new JLabel("Chưa chọn ảnh");
        photoPathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        photoPathLabel.setForeground(new Color(100, 100, 100));
        
        JButton choosePhotoButton = new JButton("Chọn Ảnh") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(0, 100, 180));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(0, 120, 215).brighter());
                } else {
                    g2d.setColor(new Color(0, 120, 215));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        choosePhotoButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        choosePhotoButton.setForeground(Color.WHITE);
        choosePhotoButton.setPreferredSize(new Dimension(100, 32));
        choosePhotoButton.setBorderPainted(false);
        choosePhotoButton.setContentAreaFilled(false);
        choosePhotoButton.setFocusPainted(false);
        choosePhotoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        final String[] selectedImagePath = {null};
        choosePhotoButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImagePath[0] = fileChooser.getSelectedFile().getAbsolutePath();
                photoPathLabel.setText(fileChooser.getSelectedFile().getName());
            }
        });
        
        photoPanel.add(choosePhotoButton);
        photoPanel.add(photoPathLabel);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 15, 15);
        contentPanel.add(photoLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        contentPanel.add(photoPanel, gbc);
        
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
            String memberType = memberTypeField.getText().trim();
            
            if (fullName.isEmpty() || phone.isEmpty() || dob.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (insertCard(fullName, phone, dob, memberType, selectedImagePath[0])) {
                JOptionPane.showMessageDialog(dialog, "Thêm thẻ thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                // Refresh table
                loadCardsToTable(tableModel);
            } else {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm thẻ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
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
    private boolean insertCard(String fullName, String phone, String dob, String memberType, String imagePath) {
        // Generate CardID (e.g., CARD + timestamp or auto-increment)
        String cardId = "CARD_" + System.currentTimeMillis();
        
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
}
