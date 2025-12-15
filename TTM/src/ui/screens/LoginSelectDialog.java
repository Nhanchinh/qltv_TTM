package ui.screens;

import ui.DBConnect;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginSelectDialog extends JDialog {

    private JButton userButton;
    private JButton adminButton;
    private JButton cancelButton;
    private int selectedOption = 0; // 0: none, 1: user, 2: admin

    // Modern Dark Theme Colors
    private static final Color PRIMARY_GRADIENT_START = new Color(99, 102, 241);
    private static final Color PRIMARY_GRADIENT_END = new Color(168, 85, 247);
    private static final Color ADMIN_COLOR = new Color(239, 68, 68);
    private static final Color ADMIN_DARK = new Color(185, 28, 28);
    private static final Color BACKGROUND_DARK = new Color(15, 23, 42);
    private static final Color BACKGROUND_CARD = new Color(30, 41, 59);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(71, 85, 105);

    public LoginSelectDialog(Frame parent) {
        super(parent, true);
        initComponents();
        setupDialog();
    }

    private void initComponents() {
        setTitle("Chọn vai trò đăng nhập");
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
                g2d.fillRoundRect(0, 0, w, h, 20, 20);

                // Gradient orbs
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                GradientPaint orb1 = new GradientPaint(
                        w - 80, -40, PRIMARY_GRADIENT_START,
                        w, 120, new Color(99, 102, 241, 0));
                g2d.setPaint(orb1);
                g2d.fillOval(w - 150, -80, 250, 250);

                GradientPaint orb2 = new GradientPaint(
                        -40, h - 80, PRIMARY_GRADIENT_END,
                        120, h, new Color(168, 85, 247, 0));
                g2d.setPaint(orb2);
                g2d.fillOval(-80, h - 150, 250, 250);

                // Border
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2d.setColor(new Color(99, 102, 241, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, w - 3, h - 3, 20, 20);

                g2d.dispose();
            }
        };
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBorder(new EmptyBorder(60, 80, 60, 80));

        // Icon
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                GradientPaint gradient = new GradientPaint(
                        centerX - 35, centerY - 35, PRIMARY_GRADIENT_START,
                        centerX + 35, centerY + 35, PRIMARY_GRADIENT_END);
                g2d.setPaint(gradient);
                g2d.fillOval(centerX - 35, centerY - 35, 70, 70);

                g2d.setColor(Color.WHITE);
                g2d.fillOval(centerX - 10, centerY - 20, 20, 20);
                g2d.fillRoundRect(centerX - 16, centerY + 4, 32, 22, 12, 12);

                g2d.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(80, 80));
        iconLabel.setMaximumSize(new Dimension(80, 80));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(iconLabel);

        mainContainer.add(Box.createVerticalStrut(25));

        // Title
        JLabel titleLabel = new JLabel("Chọn Vai Trò");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(titleLabel);

        mainContainer.add(Box.createVerticalStrut(12));

        // Subtitle
        JLabel subtitleLabel = new JLabel("Vui lòng chọn vai trò để đăng nhập");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContainer.add(subtitleLabel);

        mainContainer.add(Box.createVerticalStrut(40));

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(800, 150));

        // User button
        userButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(79, 82, 221), getWidth(), getHeight(),
                            new Color(148, 65, 227));
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(129, 132, 255), getWidth(), getHeight(),
                            new Color(188, 115, 255));
                } else {
                    gradient = new GradientPaint(0, 0, PRIMARY_GRADIENT_START, getWidth(), getHeight(),
                            PRIMARY_GRADIENT_END);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18); // Scaled from 12, 12

                // Icon
                int iconX = getWidth() / 2;
                g2d.setColor(Color.WHITE);
                g2d.fillOval(iconX - 14, 20, 28, 28);
                g2d.fillRoundRect(iconX - 20, 52, 40, 24, 12, 12);

                // Text
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "NGƯỜI DÙNG";
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                g2d.drawString(text, textX, 100);

                g2d.dispose();
            }
        };
        userButton.setPreferredSize(new Dimension(220, 120));
        userButton.setBorderPainted(false);
        userButton.setContentAreaFilled(false);
        userButton.setFocusPainted(false);
        userButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userButton.addActionListener(e -> {
            selectedOption = 1;
            dispose();
        });
        buttonsPanel.add(userButton);

        // Admin button
        adminButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, ADMIN_DARK, getWidth(), getHeight(), ADMIN_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(255, 100, 100), getWidth(), getHeight(), ADMIN_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, ADMIN_COLOR, getWidth(), getHeight(), ADMIN_DARK);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18); // Scaled from 12, 12

                // Shield icon
                int iconX = getWidth() / 2;
                g2d.setColor(Color.WHITE);
                int[] xPoints = { iconX - 16, iconX, iconX + 16, iconX + 16, iconX, iconX - 16 };
                int[] yPoints = { 26, 20, 26, 54, 68, 54 };
                g2d.fillPolygon(xPoints, yPoints, 6);

                g2d.setColor(ADMIN_COLOR);
                g2d.fillOval(iconX - 6, 36, 12, 12);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "QUẢN TRỊ";
                int textX = (getWidth() - fm.stringWidth(text)) / 2;
                g2d.drawString(text, textX, 100);

                g2d.dispose();
            }
        };
        adminButton.setPreferredSize(new Dimension(220, 120));
        adminButton.setBorderPainted(false);
        adminButton.setContentAreaFilled(false);
        adminButton.setFocusPainted(false);
        adminButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        adminButton.addActionListener(e -> {
            selectedOption = 2;
            dispose();
        });
        buttonsPanel.add(adminButton);

        mainContainer.add(buttonsPanel);

        mainContainer.add(Box.createVerticalStrut(35));

        // Exit button
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
                    g2d.setColor(new Color(51, 65, 85));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2d.setColor(TEXT_SECONDARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.setPreferredSize(new Dimension(140, 44));
        cancelButton.setMaximumSize(new Dimension(140, 44));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn muốn quay lại màn hình kết nối?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                selectedOption = 0;
                dispose();
            }
        });
        mainContainer.add(cancelButton);

        add(mainContainer);
    }

    private void setupDialog() {
        setSize(700, 550);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });

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

    public static int showSelectionDialog(Frame parent) {
        if (DBConnect.getConnection() == null) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Lỗi kết nối database!\nVui lòng kiểm tra SQLite JDBC driver.",
                    "Lỗi kết nối",
                    JOptionPane.ERROR_MESSAGE);
            return 0;
        }

        LoginSelectDialog dialog = new LoginSelectDialog(parent);
        dialog.setVisible(true);
        return dialog.getSelectedOption();
    }
}
