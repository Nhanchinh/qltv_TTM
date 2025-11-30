package ui.screens;

import ui.DBConnect;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class AdminLoginDialog extends JDialog {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel errorLabel;
    private boolean authenticated = false;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(220, 53, 69);
    private static final Color PRIMARY_DARK = new Color(200, 40, 60);
    private static final Color BACKGROUND_GRADIENT_START = new Color(255, 240, 245);
    private static final Color BACKGROUND_GRADIENT_END = new Color(255, 230, 240);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    
    public AdminLoginDialog(Frame parent) {
        super(parent, true);
        initComponents();
        setupDialog();
    }
    
    private void initComponents() {
        setTitle("Đăng nhập");
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
            new EmptyBorder(50, 80, 50, 80)
        ));
        cardPanel.setOpaque(true);
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 40, 0));
        
        JLabel titleLabel = new JLabel("Đăng Nhập");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Vui lòng nhập tên đăng nhập và mật khẩu");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        cardPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center panel for inputs
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 15);
        
        // Username label and field
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        usernameLabel.setForeground(new Color(60, 60, 60));
        
        usernameField = new JTextField() {
            @Override
            protected void paintBorder(Graphics g) {
                super.paintBorder(g);
            }
        };
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(400, 45));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180, 180, 180), 2, false),
            new EmptyBorder(10, 15, 10, 15)
        ));
        usernameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, false),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                usernameField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(180, 180, 180), 2, false),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(usernameField, gbc);
        
        // Password label and field
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passwordLabel.setForeground(new Color(60, 60, 60));
        
        passwordField = new JPasswordField() {
            @Override
            protected void paintBorder(Graphics g) {
                super.paintBorder(g);
            }
        };
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(400, 45));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180, 180, 180), 2, false),
            new EmptyBorder(10, 15, 10, 15)
        ));
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, false),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(180, 180, 180), 2, false),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        passwordField.addActionListener(e -> attemptLogin());
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 0, 15);
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(passwordField, gbc);
        
        centerPanel.add(formPanel, BorderLayout.NORTH);
        
        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        errorLabel.setForeground(new Color(220, 53, 69));
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        errorLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        centerPanel.add(errorLabel, BorderLayout.CENTER);
        
        cardPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        
        // Login button with modern styling
        loginButton = new JButton("Đăng Nhập") {
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
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(150, 50));
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> attemptLogin());
        buttonPanel.add(loginButton);
        
        // Cancel button with modern styling
        cancelButton = new JButton("THOÁT") {
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
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.setForeground(new Color(60, 60, 60));
        cancelButton.setPreferredSize(new Dimension(150, 50));
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
                System.exit(0);
            }
        });
        buttonPanel.add(cancelButton);
        
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add card panel to main container with padding
        mainContainer.setBorder(new EmptyBorder(40, 40, 40, 40));
        mainContainer.add(cardPanel, BorderLayout.CENTER);
        
        add(mainContainer);
    }
    
    private void setupDialog() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Focus on username field
        usernameField.requestFocusInWindow();
        
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
                cancelButton.doClick();
            }
        });
    }
    
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty()) {
            errorLabel.setText("Vui long nhap username!");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            errorLabel.setText("Vui long nhap password!");
            passwordField.requestFocus();
            return;
        }
        
        // Hardcoded admin credentials (can be replaced with database check)
        if (username.equals("admin") && password.equals("admin123")) {
            authenticated = true;
            dispose();
        } else {
            errorLabel.setText("Username hoac password khong dung!");
            passwordField.setText("");
            usernameField.requestFocus();
            
            // Shake animation
            shakeDialog();
        }
    }
    
    private void shakeDialog() {
        Point originalLocation = getLocation();
        int shakeDistance = 10;
        int shakeCount = 5;
        
        Timer timer = new Timer(50, new AbstractAction() {
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
    
    public static boolean showAdminLoginDialog(Frame parent) {
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
        
        // Show admin login dialog
        AdminLoginDialog dialog = new AdminLoginDialog(parent);
        dialog.setVisible(true);
        return dialog.isAuthenticated();
    }
}
