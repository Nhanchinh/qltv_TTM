package ui.screens;

import services.SettingsService;
import ui.DBConnect;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class PinLoginDialog extends JDialog {
    
    private JPasswordField pinField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel errorLabel;
    private boolean authenticated = false;
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
                System.exit(0);
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
                cancelButton.doClick();
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
        
        // Get PIN from settings
        String correctPin = settingsService.getSetting("app_pin");
        if (correctPin == null || correctPin.isEmpty()) {
            // Initialize default PIN if not exists
            settingsService.initializeDefaultPin();
            correctPin = "1234";
        }
        
        // Check PIN
        if (enteredPin.equals(correctPin)) {
            authenticated = true;
            dispose();
        } else {
            errorLabel.setText("Ma PIN khong dung! Vui long thu lai.");
            pinField.setText("");
            pinField.requestFocus();
            
            // Shake animation
            shakeDialog();
        }
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
        
        // Initialize default PIN if needed
        SettingsService settingsService = new SettingsService();
        settingsService.initializeDefaultPin();
        
        // Show PIN dialog
        PinLoginDialog dialog = new PinLoginDialog(parent);
        dialog.setVisible(true);
        return dialog.isAuthenticated();
    }
}

