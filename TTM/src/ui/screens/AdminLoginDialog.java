package ui.screens;

import ui.DBConnect;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AdminLoginDialog extends JDialog {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel errorLabel;
    private boolean authenticated = false;

    // Modern Dark Theme Colors
    private static final Color PRIMARY_GRADIENT_START = new Color(99, 102, 241);
    private static final Color PRIMARY_GRADIENT_END = new Color(168, 85, 247);
    private static final Color ADMIN_COLOR = new Color(239, 68, 68);
    private static final Color ADMIN_DARK = new Color(185, 28, 28);
    private static final Color BACKGROUND_DARK = new Color(15, 23, 42);
    private static final Color BACKGROUND_CARD = new Color(30, 41, 59);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(71, 85, 105);
    private static final Color INPUT_BG = new Color(51, 65, 85);

    public AdminLoginDialog(Frame parent) {
        super(parent, true);
        initComponents();
        setupDialog();
    }

    private void initComponents() {
        setTitle("Đăng nhập Admin");
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);

        // Main container with gradient background
        JPanel mainContainer = new JPanel() {
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
                        w, h, new Color(30, 27, 75));
                g2d.setPaint(bgGradient);
                g2d.fillRoundRect(0, 0, w, h, 24, 24);

                // Gradient orbs
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                GradientPaint orb1 = new GradientPaint(
                        w - 100, -50, ADMIN_COLOR,
                        w, 150, new Color(239, 68, 68, 0));
                g2d.setPaint(orb1);
                g2d.fillOval(w - 200, -100, 350, 350);

                GradientPaint orb2 = new GradientPaint(
                        -50, h - 100, PRIMARY_GRADIENT_END,
                        150, h, new Color(168, 85, 247, 0));
                g2d.setPaint(orb2);
                g2d.fillOval(-100, h - 200, 350, 350);

                // Border glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2d.setColor(new Color(239, 68, 68, 60));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, w - 3, h - 3, 24, 24);

                g2d.dispose();
            }
        };
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBorder(new EmptyBorder(50, 60, 50, 60));

        // Admin shield icon
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                // Shield glow
                g2d.setColor(new Color(239, 68, 68, 30));
                g2d.fillOval(centerX - 40, centerY - 40, 80, 80);

                // Shield
                GradientPaint gradient = new GradientPaint(
                        centerX - 25, centerY - 30, ADMIN_COLOR,
                        centerX + 25, centerY + 30, ADMIN_DARK);
                g2d.setPaint(gradient);
                int[] xPoints = { centerX - 28, centerX, centerX + 28, centerX + 28, centerX, centerX - 28 };
                int[] yPoints = { centerY - 20, centerY - 30, centerY - 20, centerY + 15, centerY + 35, centerY + 15 };
                g2d.fillPolygon(xPoints, yPoints, 6);

                // Star on shield
                g2d.setColor(Color.WHITE);
                g2d.fillOval(centerX - 8, centerY - 10, 16, 16);

                g2d.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(80, 80));
        iconLabel.setMaximumSize(new Dimension(80, 80));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(iconLabel);

        mainContainer.add(Box.createVerticalStrut(20));

        // Title
        JLabel titleLabel = new JLabel("Đăng Nhập Admin");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(titleLabel);

        mainContainer.add(Box.createVerticalStrut(8));

        // Subtitle
        JLabel subtitleLabel = new JLabel("Nhập thông tin đăng nhập quản trị viên");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(subtitleLabel);

        mainContainer.add(Box.createVerticalStrut(35));

        // Username section
        JLabel usernameLabel = new JLabel("Tên đăng nhập");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        usernameLabel.setForeground(TEXT_SECONDARY);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(usernameLabel);

        mainContainer.add(Box.createVerticalStrut(8));

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBackground(INPUT_BG);
        usernameField.setForeground(TEXT_PRIMARY);
        usernameField.setCaretColor(TEXT_PRIMARY);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(10, 16, 10, 16)));
        usernameField.setPreferredSize(new Dimension(350, 48));
        usernameField.setMaximumSize(new Dimension(350, 48));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(usernameField);

        mainContainer.add(Box.createVerticalStrut(18));

        // Password section
        JLabel passwordLabel = new JLabel("Mật khẩu");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passwordLabel.setForeground(TEXT_SECONDARY);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(passwordLabel);

        mainContainer.add(Box.createVerticalStrut(8));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setBackground(INPUT_BG);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setCaretColor(TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(10, 16, 10, 16)));
        passwordField.setPreferredSize(new Dimension(350, 48));
        passwordField.setMaximumSize(new Dimension(350, 48));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.addActionListener(e -> attemptLogin());
        mainContainer.add(passwordField);

        mainContainer.add(Box.createVerticalStrut(12));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        errorLabel.setForeground(ADMIN_COLOR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(errorLabel);

        mainContainer.add(Box.createVerticalStrut(25));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(400, 55));

        // Login button
        loginButton = new JButton("Đăng Nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, ADMIN_DARK, getWidth(), getHeight(), ADMIN_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(248, 113, 113), getWidth(), getHeight(), ADMIN_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, ADMIN_COLOR, getWidth(), getHeight(), ADMIN_DARK);
                }

                // Shadow
                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(239, 68, 68, 40));
                    g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 2, 12, 12);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 3, 12, 12);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 3 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(150, 48));
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> attemptLogin());
        buttonPanel.add(loginButton);

        // Cancel button
        cancelButton = new JButton("Thoát") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(BORDER_COLOR);
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(71, 85, 105, 200));
                } else {
                    g2d.setColor(BACKGROUND_CARD);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                g2d.setColor(TEXT_SECONDARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cancelButton.setPreferredSize(new Dimension(150, 48));
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
                    JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        buttonPanel.add(cancelButton);

        mainContainer.add(buttonPanel);

        add(mainContainer);
    }

    private void setupDialog() {
        setSize(550, 580);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        // Focus on username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());

        // Close on ESC
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
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
            errorLabel.setText("Vui lòng nhập tên đăng nhập!");
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập mật khẩu!");
            passwordField.requestFocus();
            return;
        }

        // Hardcoded admin credentials (can be replaced with database check)
        if (username.equals("admin") && password.equals("admin123")) {
            authenticated = true;
            dispose();
        } else {
            errorLabel.setText("Tên đăng nhập hoặc mật khẩu không đúng!");
            passwordField.setText("");
            usernameField.requestFocus();
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
                    "Lỗi kết nối database!\nVui lòng kiểm tra SQLite JDBC driver.",
                    "Lỗi kết nối",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Show admin login dialog
        AdminLoginDialog dialog = new AdminLoginDialog(parent);
        dialog.setVisible(true);
        return dialog.isAuthenticated();
    }
}
