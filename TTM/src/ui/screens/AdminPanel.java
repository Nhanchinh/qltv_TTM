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
        JButton logoutButton = new JButton("DANG XUAT") {
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
            "Ban co chac chan muon dang xuat?",
            "Xac nhan",
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
        JLabel titleLabel = new JLabel("RESET MA PIN HE THONG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ADMIN_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // Current PIN
        JPanel currentPanel = new JPanel();
        currentPanel.setOpaque(false);
        currentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        JLabel currentLabel = new JLabel("MA PIN HIEN TAI:");
        currentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel currentPINValue = new JLabel();
        currentPINValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        currentPINValue.setForeground(SUCCESS_COLOR);
        currentPanel.add(currentLabel);
        currentPanel.add(currentPINValue);
        centerPanel.add(currentPanel);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // New PIN input
        JLabel newPINLabel = new JLabel("MA PIN MOI:");
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
        JLabel confirmPINLabel = new JLabel("XAC NHAN MA PIN:");
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
                messageLabel.setText("Vui long nhap ma PIN moi!");
                return;
            }
            
            if (!newPIN.equals(confirmPIN)) {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Ma PIN xac nhan khong khop!");
                return;
            }
            
            if (settingsService.setSetting("app_pin", newPIN)) {
                messageLabel.setForeground(SUCCESS_COLOR);
                messageLabel.setText("Reset ma PIN thanh cong!");
                currentPINValue.setText(newPIN);
                newPINField.setText("");
                confirmPINField.setText("");
            } else {
                messageLabel.setForeground(new Color(220, 53, 69));
                messageLabel.setText("Loi khi reset ma PIN!");
            }
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
        
        // Load current PIN
        String currentPin = settingsService.getSetting("app_pin");
        currentPINValue.setText(currentPin != null ? currentPin : "1234");
        
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
        
        JLabel titleLabel = new JLabel("NAP DU LIEU THE THONG DUNG");
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
        
        JButton addButton = new JButton("THEM THE MOI") {
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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Them The Moi", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // CardID
        addFieldToDialog(contentPanel, "CardID:", new JTextField(), "cardId");
        addFieldToDialog(contentPanel, "Full Name:", new JTextField(), "fullName");
        addFieldToDialog(contentPanel, "Phone:", new JTextField(), "phone");
        addFieldToDialog(contentPanel, "DOB (YYYY-MM-DD):", new JTextField(), "dob");
        addFieldToDialog(contentPanel, "Member Type:", new JTextField(), "memberType");
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton saveButton = new JButton("LUU");
        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.addActionListener(e -> {
            Component[] components = contentPanel.getComponents();
            String cardId = ((JTextField) components[1]).getText().trim();
            String fullName = ((JTextField) components[3]).getText().trim();
            String phone = ((JTextField) components[5]).getText().trim();
            String dob = ((JTextField) components[7]).getText().trim();
            String memberType = ((JTextField) components[9]).getText().trim();
            
            if (cardId.isEmpty() || fullName.isEmpty() || phone.isEmpty() || dob.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui long dien day du thong tin!", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (insertCard(cardId, fullName, phone, dob, memberType)) {
                JOptionPane.showMessageDialog(dialog, "Them the thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                // Refresh table
                loadCardsToTable(tableModel);
            } else {
                JOptionPane.showMessageDialog(dialog, "Loi khi them the!", "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("HUY");
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);
        
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(buttonPanel);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Helper method to add field to dialog
     */
    private void addFieldToDialog(JPanel panel, String label, JTextField field, String fieldName) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(labelComponent);
        
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        panel.add(field);
        
        panel.add(Box.createVerticalStrut(10));
    }
    
    /**
     * Insert new card to database
     */
    private boolean insertCard(String cardId, String fullName, String phone, String dob, String memberType) {
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
}
