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
 * Modern UI Design with Glassmorphism and Gradient effects
 */
public class CardConnectionPanel extends JFrame {

    private JButton connectButton;
    private JButton continueButton;
    private JLabel statusLabel;
    private JLabel statusIconLabel;
    private JPanel statusPanel;
    private JLabel detailLabel;
    private boolean isConnected = false;
    private CardConnectionManager cardConnectionManager;
    private Timer pulseTimer;
    private float pulseAlpha = 0.0f;
    private boolean pulseGrowing = true;

    // Modern Color Palette - Premium Dark Theme
    private static final Color PRIMARY_GRADIENT_START = new Color(99, 102, 241); // Indigo
    private static final Color PRIMARY_GRADIENT_END = new Color(168, 85, 247); // Purple
    private static final Color ACCENT_COLOR = new Color(34, 211, 238); // Cyan
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129); // Emerald
    private static final Color SUCCESS_GLOW = new Color(16, 185, 129, 60);
    private static final Color BACKGROUND_DARK = new Color(15, 23, 42); // Slate 900
    private static final Color BACKGROUND_CARD = new Color(30, 41, 59); // Slate 800
    private static final Color BACKGROUND_CARD_LIGHT = new Color(51, 65, 85); // Slate 700
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252); // Slate 50
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184); // Slate 400
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // Slate 500
    private static final Color BORDER_COLOR = new Color(71, 85, 105); // Slate 600
    private static final Color DISCONNECTED_COLOR = new Color(239, 68, 68); // Red 500
    private static final Color DISCONNECTED_GLOW = new Color(239, 68, 68, 40);
    private static final Color BUTTON_HOVER = new Color(129, 140, 248); // Indigo 400

    public CardConnectionPanel() {
        cardConnectionManager = new CardConnectionManager();
        initComponents();
        setupFrame();
        startPulseAnimation();
    }

    private void startPulseAnimation() {
        pulseTimer = new Timer(50, e -> {
            if (!isConnected) {
                if (pulseGrowing) {
                    pulseAlpha += 0.05f;
                    if (pulseAlpha >= 1.0f) {
                        pulseAlpha = 1.0f;
                        pulseGrowing = false;
                    }
                } else {
                    pulseAlpha -= 0.05f;
                    if (pulseAlpha <= 0.0f) {
                        pulseAlpha = 0.0f;
                        pulseGrowing = true;
                    }
                }
                if (statusIconLabel != null) {
                    statusIconLabel.repaint();
                }
            }
        });
        pulseTimer.start();
    }

    private void initComponents() {
        setTitle("Kết Nối Thẻ Thông Minh - TTM");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main container with animated gradient background
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            private float gradientOffset = 0;
            {
                Timer animTimer = new Timer(100, e -> {
                    gradientOffset += 0.01f;
                    if (gradientOffset > 1)
                        gradientOffset = 0;
                    repaint();
                });
                animTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Dark gradient background
                GradientPaint bgGradient = new GradientPaint(
                        0, 0, BACKGROUND_DARK,
                        w, h, new Color(30, 27, 75) // Deep purple tint
                );
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, w, h);

                // Animated gradient orbs
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));

                // Top-right orb
                GradientPaint orb1 = new GradientPaint(
                        w - 200, -100, PRIMARY_GRADIENT_START,
                        w, 200, new Color(99, 102, 241, 0));
                g2d.setPaint(orb1);
                g2d.fillOval(w - 400, -200, 600, 600);

                // Bottom-left orb
                GradientPaint orb2 = new GradientPaint(
                        -100, h - 200, PRIMARY_GRADIENT_END,
                        300, h, new Color(168, 85, 247, 0));
                g2d.setPaint(orb2);
                g2d.fillOval(-200, h - 400, 600, 600);

                // Center accent
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                GradientPaint centerOrb = new GradientPaint(
                        w / 2 - 150, h / 2 - 150, ACCENT_COLOR,
                        w / 2 + 150, h / 2 + 150, new Color(34, 211, 238, 0));
                g2d.setPaint(centerOrb);
                g2d.fillOval(w / 2 - 300, h / 2 - 300, 600, 600);

                g2d.dispose();
            }
        };

        // Glassmorphism card panel
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Glass effect background
                g2d.setColor(new Color(30, 41, 59, 230)); // Semi-transparent
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);

                // Subtle gradient overlay
                GradientPaint glassShine = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 8),
                        0, getHeight(), new Color(255, 255, 255, 0));
                g2d.setPaint(glassShine);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() / 2, 32, 32);

                // Border glow
                g2d.setColor(new Color(99, 102, 241, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 32, 32);

                g2d.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Title section with icon
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Smart card icon
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                // Card shape - outer glow
                g2d.setColor(new Color(99, 102, 241, 30));
                g2d.fillRoundRect(centerX - 42, centerY - 28, 84, 56, 12, 12);

                // Card body with gradient
                GradientPaint cardGradient = new GradientPaint(
                        centerX - 40, centerY - 26, PRIMARY_GRADIENT_START,
                        centerX + 40, centerY + 26, PRIMARY_GRADIENT_END);
                g2d.setPaint(cardGradient);
                g2d.fillRoundRect(centerX - 40, centerY - 26, 80, 52, 10, 10);

                // Chip on card
                g2d.setColor(new Color(250, 204, 21)); // Gold
                g2d.fillRoundRect(centerX - 28, centerY - 14, 24, 18, 4, 4);

                // Chip lines
                g2d.setColor(new Color(161, 98, 7));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawLine(centerX - 16, centerY - 14, centerX - 16, centerY + 4);
                g2d.drawLine(centerX - 28, centerY - 5, centerX - 4, centerY - 5);

                // Contactless waves
                g2d.setColor(TEXT_PRIMARY);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 3; i++) {
                    int offset = i * 6;
                    g2d.drawArc(centerX + 8 + offset, centerY - 10, 16, 20, 45, 90);
                }

                g2d.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(100, 70));

        JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconWrapper.setOpaque(false);
        iconWrapper.add(iconLabel);
        titlePanel.add(iconWrapper, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("Kết Nối Thẻ Thông Minh");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subtitleLabel = new JLabel("Vui lòng kết nối thẻ thông minh để tiếp tục");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        cardPanel.add(titlePanel, BorderLayout.NORTH);

        // Center panel for status display
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Status panel with glassmorphism
        statusPanel = new JPanel(new BorderLayout(25, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Glow effect based on status
                if (isConnected) {
                    g2d.setColor(SUCCESS_GLOW);
                } else {
                    g2d.setColor(new Color(51, 65, 85, 200));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Border
                if (isConnected) {
                    g2d.setColor(new Color(16, 185, 129, 100));
                } else {
                    g2d.setColor(BORDER_COLOR);
                }
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);

                g2d.dispose();
            }
        };
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Animated status icon
        statusIconLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 80;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                if (isConnected) {
                    // Success glow
                    g2d.setColor(new Color(16, 185, 129, 40));
                    g2d.fillOval(x - 10, y - 10, size + 20, size + 20);

                    // Main circle
                    GradientPaint successGradient = new GradientPaint(
                            x, y, SUCCESS_COLOR,
                            x + size, y + size, new Color(5, 150, 105));
                    g2d.setPaint(successGradient);
                    g2d.fillOval(x, y, size, size);

                    // Checkmark
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(x + 20, y + 40, x + 32, y + 52);
                    g2d.drawLine(x + 32, y + 52, x + 58, y + 28);
                } else {
                    // Pulse animation for disconnected
                    int pulseSize = (int) (pulseAlpha * 15);
                    g2d.setColor(new Color(239, 68, 68, (int) (pulseAlpha * 40)));
                    g2d.fillOval(x - pulseSize, y - pulseSize, size + pulseSize * 2, size + pulseSize * 2);

                    // Main circle
                    g2d.setColor(BACKGROUND_CARD_LIGHT);
                    g2d.fillOval(x, y, size, size);

                    // Border
                    g2d.setColor(BORDER_COLOR);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawOval(x, y, size, size);

                    // Card icon inside
                    g2d.setColor(TEXT_MUTED);
                    g2d.fillRoundRect(x + 22, y + 28, 36, 24, 4, 4);
                    g2d.setColor(new Color(250, 204, 21, 150));
                    g2d.fillRoundRect(x + 26, y + 32, 12, 8, 2, 2);
                }

                g2d.dispose();
            }
        };
        statusIconLabel.setPreferredSize(new Dimension(80, 80));
        statusPanel.add(statusIconLabel, BorderLayout.WEST);

        // Status text
        JPanel statusTextPanel = new JPanel(new BorderLayout(0, 6));
        statusTextPanel.setOpaque(false);

        JLabel statusTitleLabel = new JLabel("Trạng Thái Kết Nối");
        statusTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusTitleLabel.setForeground(TEXT_SECONDARY);
        statusTextPanel.add(statusTitleLabel, BorderLayout.NORTH);

        statusLabel = new JLabel("Chưa kết nối");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        statusLabel.setForeground(TEXT_MUTED);
        statusTextPanel.add(statusLabel, BorderLayout.CENTER);

        detailLabel = new JLabel("Nhấn nút bên dưới để kết nối thẻ");
        detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detailLabel.setForeground(TEXT_MUTED);
        statusTextPanel.add(detailLabel, BorderLayout.SOUTH);

        statusPanel.add(statusTextPanel, BorderLayout.CENTER);
        centerPanel.add(statusPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // Connect button with gradient
        connectButton = new JButton("Kết Nối Thẻ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Button gradient
                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(
                            0, 0, PRIMARY_GRADIENT_END,
                            getWidth(), getHeight(), PRIMARY_GRADIENT_START);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(
                            0, 0, BUTTON_HOVER,
                            getWidth(), getHeight(), PRIMARY_GRADIENT_END);
                } else {
                    gradient = new GradientPaint(
                            0, 0, PRIMARY_GRADIENT_START,
                            getWidth(), getHeight(), PRIMARY_GRADIENT_END);
                }

                // Shadow
                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(99, 102, 241, 50));
                    g2d.fillRoundRect(3, 5, getWidth() - 6, getHeight() - 4, 16, 16);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 4, 16, 16);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 4 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        connectButton.setPreferredSize(new Dimension(250, 50));
        connectButton.setBorderPainted(false);
        connectButton.setContentAreaFilled(false);
        connectButton.setFocusPainted(false);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        connectButton.addActionListener(e -> handleConnectButtonClick());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        buttonPanel.add(connectButton);

        centerPanel.add(buttonPanel);
        cardPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Continue button
        continueButton = new JButton("Tiếp Tục") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isConnected) {
                    g2d.setColor(BACKGROUND_CARD_LIGHT);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 3, 14, 14);
                    g2d.setColor(TEXT_MUTED);
                } else {
                    // Gradient for enabled
                    GradientPaint gradient;
                    if (getModel().isPressed()) {
                        gradient = new GradientPaint(0, 0, new Color(5, 150, 105), getWidth(), getHeight(),
                                SUCCESS_COLOR);
                    } else if (getModel().isRollover()) {
                        gradient = new GradientPaint(0, 0, new Color(52, 211, 153), getWidth(), getHeight(),
                                SUCCESS_COLOR);
                    } else {
                        gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), getHeight(),
                                new Color(5, 150, 105));
                    }

                    // Shadow
                    if (!getModel().isPressed()) {
                        g2d.setColor(new Color(16, 185, 129, 40));
                        g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 3, 14, 14);
                    }

                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 3, 14, 14);
                    g2d.setColor(Color.WHITE);
                }

                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 3 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        continueButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        continueButton.setPreferredSize(new Dimension(130, 44));
        continueButton.setBorderPainted(false);
        continueButton.setContentAreaFilled(false);
        continueButton.setFocusPainted(false);
        continueButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continueButton.setEnabled(false);
        continueButton.addActionListener(e -> {
            if (isConnected)
                handleContinueButtonClick();
        });
        bottomPanel.add(continueButton);

        // Exit button
        JButton exitButton = new JButton("Thoát") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color baseColor = new Color(239, 68, 68);
                Color darkColor = new Color(185, 28, 28);

                if (getModel().isPressed()) {
                    g2d.setColor(darkColor);
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(248, 113, 113));
                } else {
                    g2d.setColor(baseColor);
                }

                // Shadow
                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(239, 68, 68, 40));
                    g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 3, 14, 14);
                    g2d.setColor(getModel().isRollover() ? new Color(248, 113, 113) : baseColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 3, 14, 14);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 3 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setPreferredSize(new Dimension(130, 44));
        exitButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    CardConnectionPanel.this,
                    "Bạn có chắc chắn muốn thoát?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        bottomPanel.add(exitButton);

        cardPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add card panel to main container with padding
        mainContainer.setBorder(new EmptyBorder(40, 60, 40, 60));
        mainContainer.add(cardPanel, BorderLayout.CENTER);

        add(mainContainer);
    }

    private void setupFrame() {
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Close on ESC
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        getRootPane().getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(
                        CardConnectionPanel.this,
                        "Bạn có chắc chắn muốn thoát?",
                        "Xác nhận",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private void handleConnectButtonClick() {
        if (!isConnected) {
            connectButton.setEnabled(false);
            connectButton.setText("Đang kết nối...");

            new Thread(() -> {
                try {
                    boolean success = cardConnectionManager.connectCard();

                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            isConnected = true;
                            statusLabel.setText("Đã kết nối thành công");
                            statusLabel.setForeground(SUCCESS_COLOR);
                            detailLabel.setText("Thẻ đã sẵn sàng, nhấn Tiếp Tục để đăng nhập");
                            detailLabel.setForeground(SUCCESS_COLOR);
                            statusPanel.repaint();
                            statusIconLabel.repaint();
                            continueButton.setEnabled(true);
                            continueButton.repaint();
                            connectButton.setText("Ngắt Kết Nối");
                            connectButton.setEnabled(true);
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        isConnected = false;
                        statusLabel.setText("Kết nối thất bại");
                        statusLabel.setForeground(DISCONNECTED_COLOR);
                        detailLabel.setText("Vui lòng kiểm tra lại thẻ và thử lại");
                        detailLabel.setForeground(TEXT_MUTED);
                        statusPanel.repaint();
                        statusIconLabel.repaint();
                        continueButton.setEnabled(false);
                        continueButton.repaint();
                        connectButton.setText("Kết Nối Thẻ");
                        connectButton.setEnabled(true);

                        JOptionPane.showMessageDialog(
                                CardConnectionPanel.this,
                                "Lỗi kết nối thẻ:\n" + e.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        } else {
            connectButton.setEnabled(false);
            connectButton.setText("Đang ngắt...");

            new Thread(() -> {
                try {
                    boolean success = cardConnectionManager.disconnectCard();

                    if (success) {
                        SwingUtilities.invokeLater(() -> {
                            isConnected = false;
                            statusLabel.setText("Chưa kết nối");
                            statusLabel.setForeground(TEXT_MUTED);
                            detailLabel.setText("Nhấn nút bên dưới để kết nối thẻ");
                            detailLabel.setForeground(TEXT_MUTED);
                            statusPanel.repaint();
                            statusIconLabel.repaint();
                            continueButton.setEnabled(false);
                            continueButton.repaint();
                            connectButton.setText("Kết Nối Thẻ");
                            connectButton.setEnabled(true);
                        });
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        connectButton.setEnabled(true);
                        connectButton.setText("Ngắt Kết Nối");

                        JOptionPane.showMessageDialog(
                                CardConnectionPanel.this,
                                "Lỗi ngắt kết nối:\n" + e.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }

    private void handleContinueButtonClick() {
        if (isConnected) {
            if (pulseTimer != null) {
                pulseTimer.stop();
            }
            this.dispose();

            boolean systemRunning = true;
            boolean restartConnectionFlow = false;

            while (systemRunning) {
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
                    restartConnectionFlow = true;
                    break;
                }

                if (!authenticated) {
                    continue;
                }

                systemRunning = false;
                java.awt.EventQueue.invokeLater(() -> {
                    if (loginMode == 1) {
                        new MainFrame().setVisible(true);
                    } else {
                        new AppFrame(loginMode);
                    }
                });
            }

            if (restartConnectionFlow) {
                java.awt.EventQueue.invokeLater(() -> new CardConnectionPanel().setVisible(true));
            }
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CardConnectionPanel.class.getName()).log(java.util.logging.Level.SEVERE,
                    null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new CardConnectionPanel().setVisible(true);
        });
    }
}
