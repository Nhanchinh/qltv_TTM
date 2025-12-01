package ui.screens;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import smartcard.CardConnectionManager;

/**
 * Panel for connecting to smart card before login
 * Shows connection status and provides connect/disconnect functionality
 */
public class CardConnectionPanel extends JFrame {
    
    private JButton connectButton;
    private JButton continueButton;
    private JLabel statusLabel;
    private JLabel statusIconLabel;
    private JPanel statusPanel;
    private boolean isConnected = false;
    private CardConnectionManager cardConnectionManager;
    
    // Colors - Modern palette
    private static final Color PRIMARY_COLOR = new Color(25, 135, 205);
    private static final Color PRIMARY_DARK = new Color(15, 110, 175);
    private static final Color PRIMARY_LIGHT = new Color(200, 230, 255);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color SUCCESS_LIGHT = new Color(180, 240, 200);
    private static final Color BACKGROUND_GRADIENT_START = new Color(245, 250, 255);
    private static final Color BACKGROUND_GRADIENT_END = new Color(225, 240, 255);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color DISCONNECTED_COLOR = new Color(180, 180, 180);
    private static final Color DISCONNECTED_LIGHT = new Color(220, 220, 220);
    
    public CardConnectionPanel() {
        cardConnectionManager = new CardConnectionManager();
        initComponents();
        setupFrame();
    }
    
    private void initComponents() {
        setTitle("Ket noi the thong minh - TTM");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
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
            BorderFactory.createLineBorder(new Color(230, 235, 240), 2, true),
            new EmptyBorder(50, 70, 50, 70)
        ));
        cardPanel.setOpaque(true);
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 40, 0));
        
        JLabel titleLabel = new JLabel("Kết Nối Thẻ Thông Minh");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel subtitleLabel = new JLabel("Vui lòng kết nối thẻ thông minh trước khi đăng nhập");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(140, 140, 140));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBorder(new EmptyBorder(12, 0, 0, 0));
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        cardPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center panel for status display and connect button
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Status panel with icon and message
        statusPanel = new JPanel(new BorderLayout(30, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isConnected) {
                    g2d.setColor(SUCCESS_LIGHT);
                } else {
                    g2d.setColor(new Color(240, 240, 240));
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Status icon - larger and better
        statusIconLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 90;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                if (isConnected) {
                    // Draw circle background
                    g2d.setColor(new Color(46, 204, 113, 20));
                    g2d.fillOval(x, y, size, size);
                    
                    // Draw circle border
                    g2d.setColor(SUCCESS_COLOR);
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawOval(x, y, size, size);
                    
                    // Draw checkmark
                    g2d.setColor(SUCCESS_COLOR);
                    g2d.setStroke(new BasicStroke(5));
                    g2d.drawLine(x + 20, y + 45, x + 35, y + 60);
                    g2d.drawLine(x + 35, y + 60, x + 65, y + 30);
                } else {
                    // Draw circle background
                    g2d.setColor(new Color(200, 200, 200, 15));
                    g2d.fillOval(x, y, size, size);
                    
                    // Draw circle border
                    g2d.setColor(DISCONNECTED_COLOR);
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawOval(x, y, size, size);
                    
                    // Draw X mark
                    g2d.setColor(DISCONNECTED_COLOR);
                    g2d.setStroke(new BasicStroke(5));
                    g2d.drawLine(x + 20, y + 20, x + 70, y + 70);
                    g2d.drawLine(x + 70, y + 20, x + 20, y + 70);
                }
            }
        };
        statusIconLabel.setPreferredSize(new Dimension(120, 120));
        statusPanel.add(statusIconLabel, BorderLayout.WEST);
        
        // Status text
        JPanel statusTextPanel = new JPanel(new BorderLayout(0, 8));
        statusTextPanel.setOpaque(false);
        
        JLabel statusTitleLabel = new JLabel("Trạng Thái Kết Nối:");
        statusTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusTitleLabel.setForeground(PRIMARY_COLOR);
        statusTextPanel.add(statusTitleLabel, BorderLayout.NORTH);
        
        statusLabel = new JLabel("Chưa kết nối");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        statusLabel.setForeground(DISCONNECTED_COLOR);
        statusTextPanel.add(statusLabel, BorderLayout.CENTER);
        
        JLabel detailLabel = new JLabel("Kết nối thẻ để tiếp tục");
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailLabel.setForeground(new Color(160, 160, 160));
        statusTextPanel.add(detailLabel, BorderLayout.SOUTH);
        
        statusPanel.add(statusTextPanel, BorderLayout.CENTER);
        centerPanel.add(statusPanel);
        centerPanel.add(Box.createVerticalStrut(15));
        
        // Connect button with improved styling
        connectButton = new ModernButton("Kết Nối", PRIMARY_COLOR, PRIMARY_DARK) {
            @Override
            public void performAction() {
                handleConnectButtonClick();
            }
        };
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        connectButton.setPreferredSize(new Dimension(280, 70));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        buttonPanel.add(connectButton);
        
        centerPanel.add(buttonPanel);
        
        cardPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with continue button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        
        continueButton = new ModernButton("Tiếp Tục", PRIMARY_COLOR, PRIMARY_DARK) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (!isConnected) {
                    g2d.setColor(DISCONNECTED_LIGHT);
                    setForeground(new Color(170, 170, 170));
                } else {
                    if (getModel().isPressed()) {
                        g2d.setColor(PRIMARY_DARK);
                    } else if (getModel().isRollover()) {
                        g2d.setColor(new Color(50, 160, 225));
                    } else {
                        g2d.setColor(PRIMARY_COLOR);
                    }
                    setForeground(Color.WHITE);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
            
            @Override
            public void performAction() {
                if (isConnected) {
                    handleContinueButtonClick();
                }
            }
        };
        
        continueButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        continueButton.setPreferredSize(new Dimension(160, 60));
        continueButton.setEnabled(false);
        bottomPanel.add(continueButton);
        
        // Exit button
        JButton exitButton = new ModernButton("Thoát", new Color(220, 100, 100), new Color(200, 70, 70)) {
            @Override
            public void performAction() {
                int option = JOptionPane.showConfirmDialog(
                    CardConnectionPanel.this,
                    "Bạn có chắc chắn muốn thoát?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        };
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        exitButton.setPreferredSize(new Dimension(160, 60));
        bottomPanel.add(exitButton);
        
        cardPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add card panel to main container with padding
        mainContainer.setBorder(new EmptyBorder(50, 60, 50, 60));
        mainContainer.add(cardPanel, BorderLayout.CENTER);
        
        add(mainContainer);
    }
    
    private void setupFrame() {
        setSize(1000, 900);
        setLocationRelativeTo(null);
        
        // Close on ESC
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit"
        );
        getRootPane().getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(
                    CardConnectionPanel.this,
                    "Bạn có chắc chắn muốn thoát?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }
    
    /**
     * Handle connect button click - placeholder for card connection logic
     */
    private void handleConnectButtonClick() {
        if (!isConnected) {
            // Try to connect to card
            connectButton.setEnabled(false);
            connectButton.setText("Đang kết nối...");
            
            // Run connection in separate thread to avoid blocking UI
            new Thread(() -> {
                try {
                    boolean success = cardConnectionManager.connectCard();
                    
                    if (success) {
                        // Connection successful
                        SwingUtilities.invokeLater(() -> {
                            isConnected = true;
                            statusLabel.setText("Đã kết nối");
                            statusLabel.setForeground(SUCCESS_COLOR);
                            statusPanel.repaint();
                            continueButton.setEnabled(true);
                            continueButton.repaint();
                            connectButton.setText("Ngắt Kết Nối");
                            connectButton.setEnabled(true);
                        });
                    }
                } catch (Exception e) {
                    // Connection failed
                    SwingUtilities.invokeLater(() -> {
                        isConnected = false;
                        statusLabel.setText("Chua ket noi");
                        statusLabel.setForeground(DISCONNECTED_COLOR);
                        statusPanel.repaint();
                        continueButton.setEnabled(false);
                        continueButton.repaint();
                        connectButton.setText("Ket Noi");
                        connectButton.setEnabled(true);
                        
                        // Show error message
                        JOptionPane.showMessageDialog(
                            CardConnectionPanel.this,
                            "Lỗi kết nối thẻ:\n" + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                }
            }).start();
        } else {
            // Try to disconnect from card
            connectButton.setEnabled(false);
            connectButton.setText("Dang ngat ket noi...");
            
            // Run disconnection in separate thread
            new Thread(() -> {
                try {
                    boolean success = cardConnectionManager.disconnectCard();
                    
                    if (success) {
                        // Disconnection successful
                        SwingUtilities.invokeLater(() -> {
                            isConnected = false;
                            statusLabel.setText("Chua ket noi");
                            statusLabel.setForeground(DISCONNECTED_COLOR);
                            statusPanel.repaint();
                            continueButton.setEnabled(false);
                            continueButton.repaint();
                            connectButton.setText("Ket Noi");
                            connectButton.setEnabled(true);
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        connectButton.setEnabled(true);
                        connectButton.setText("Ngat Ket Noi");
                        
                        JOptionPane.showMessageDialog(
                            CardConnectionPanel.this,
                            "Lỗi ngắt kết nối:\n" + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                        );
                    });
                }
            }).start();
        }
    }
    
    /**
     * Handle continue button click - proceed to login role selection
     */
    private void handleContinueButtonClick() {
        if (isConnected) {
            // Close this frame
            this.dispose();
            
            // Show login selection dialog
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
     * Modern button styling class
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
            
            // Draw shadow effect when hovered
            if (getModel().isRollover()) {
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(2, 2, getWidth(), getHeight(), 15, 15);
            }
            
            g2d.setColor(drawColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2d.dispose();
            
            super.paintComponent(g);
        }
        
        protected abstract void performAction();
    }
    
    /**
     * Main method to start the application with card connection
     */
    public static void main(String[] args) {
        // Set look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CardConnectionPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        // Show card connection panel first
        java.awt.EventQueue.invokeLater(() -> {
            new CardConnectionPanel().setVisible(true);
        });
    }
}
