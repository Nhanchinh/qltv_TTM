package ui.screens;

import ui.DBConnect;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginSelectDialog extends JDialog {
    
    private JButton userButton;
    private JButton adminButton;
    private JButton cancelButton;
    private int selectedOption = 0; // 0: none, 1: user, 2: admin
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(0, 120, 215);
    private static final Color PRIMARY_DARK = new Color(0, 100, 180);
    private static final Color ADMIN_COLOR = new Color(220, 53, 69);
    private static final Color ADMIN_DARK = new Color(200, 40, 60);
    private static final Color BACKGROUND_GRADIENT_START = new Color(240, 248, 255);
    private static final Color BACKGROUND_GRADIENT_END = new Color(230, 240, 255);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    
    public LoginSelectDialog(Frame parent) {
        super(parent, true);
        initComponents();
        setupDialog();
    }
    
    private void initComponents() {
        setTitle("Lua chon che do dang nhap");
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
        
        // Card panel (white background)
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(50, 60, 50, 60)
        ));
        cardPanel.setOpaque(true);
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 40, 0));
        
        JLabel titleLabel = new JLabel("Đăng Nhập Với Vai Trò");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Vui lòng chọn vai trò của bạn");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        cardPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center panel for buttons
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 0, 30, 0));
        
        // User button
        userButton = new ModernButton("USER", PRIMARY_COLOR, PRIMARY_DARK) {
            @Override
            public void performAction() {
                selectedOption = 1;
                dispose();
            }
        };
        userButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        centerPanel.add(userButton);
        
        // Admin button
        adminButton = new ModernButton("ADMIN", ADMIN_COLOR, ADMIN_DARK) {
            @Override
            public void performAction() {
                selectedOption = 2;
                dispose();
            }
        };
        adminButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        centerPanel.add(adminButton);
        
        cardPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Button panel for cancel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Cancel button
        cancelButton = new JButton("EXIT") {
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
                "Bạn có chắc chắn muốn thoát?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (option == JOptionPane.YES_OPTION) {
                selectedOption = 0;
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
        setSize(700, 500);
        setLocationRelativeTo(null);
        
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
        
        // Prevent window closing without selection
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelButton.doClick();
            }
        });
    }
    
    public int getSelectedOption() {
        return selectedOption;
    }
    
    /**
     * Hien thi dialog chon che do dang nhap
     * @return 1 = User, 2 = Admin, 0 = Cancel/Exit
     */
    public static int showSelectionDialog(Frame parent) {
        // Check database connection first
        if (DBConnect.getConnection() == null) {
            JOptionPane.showMessageDialog(
                parent,
                "Loi ket noi database!\nVui long kiem tra SQLite JDBC driver.",
                "Loi ket noi",
                JOptionPane.ERROR_MESSAGE
            );
            return 0;
        }
        
        // Show selection dialog
        LoginSelectDialog dialog = new LoginSelectDialog(parent);
        dialog.setVisible(true);
        return dialog.getSelectedOption();
    }
    
    /**
     * Abstract class cho modern button styling
     */
    private static abstract class ModernButton extends JButton {
        private Color primaryColor;
        private Color darkColor;
        
        public ModernButton(String text, Color primaryColor, Color darkColor) {
            super(text);
            this.primaryColor = primaryColor;
            this.darkColor = darkColor;
            setupUI();
        }
        
        private void setupUI() {
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(200, 80));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addActionListener(e -> performAction());
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            Color drawColor;
            if (getModel().isPressed()) {
                drawColor = darkColor;
            } else if (getModel().isRollover()) {
                drawColor = primaryColor.brighter();
            } else {
                drawColor = primaryColor;
            }
            
            g2d.setColor(drawColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2d.dispose();
            
            super.paintComponent(g);
        }
        
        protected abstract void performAction();
    }
}
