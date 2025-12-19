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

    // Modern Dark Theme Colors
    private static final Color PRIMARY_GRADIENT_START = new Color(99, 102, 241);
    private static final Color PRIMARY_GRADIENT_END = new Color(168, 85, 247);
    private static final Color ADMIN_COLOR = new Color(239, 68, 68);
    private static final Color ADMIN_DARK = new Color(185, 28, 28);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private static final Color BACKGROUND_DARK = new Color(15, 23, 42);
    private static final Color BACKGROUND_CARD = new Color(30, 41, 59);
    private static final Color BACKGROUND_CARD_LIGHT = new Color(51, 65, 85);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(71, 85, 105);
    private static final Color INPUT_BG = new Color(51, 65, 85);

    public AdminPanel() {
        settingsService = new SettingsService();
        cardService = new CardService();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
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
                g2d.fillRect(0, 0, w, h);

                // Floating orbs effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                GradientPaint orb1 = new GradientPaint(
                        w - 200, -100, ADMIN_COLOR,
                        w, 200, new Color(239, 68, 68, 0));
                g2d.setPaint(orb1);
                g2d.fillOval(w - 400, -200, 600, 600);

                GradientPaint orb2 = new GradientPaint(
                        -100, h - 200, PRIMARY_GRADIENT_END,
                        200, h, new Color(168, 85, 247, 0));
                g2d.setPaint(orb2);
                g2d.fillOval(-200, h - 400, 600, 600);

                g2d.dispose();
            }
        };

        // Header with glassmorphism
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 41, 59, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(71, 85, 105, 100));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2d.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        // Left side with icon and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        // Admin shield icon
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // Glow
                g2d.setColor(new Color(239, 68, 68, 40));
                g2d.fillOval(cx - 22, cy - 22, 44, 44);

                // Shield
                GradientPaint gradient = new GradientPaint(cx - 14, cy - 16, ADMIN_COLOR, cx + 14, cy + 18, ADMIN_DARK);
                g2d.setPaint(gradient);
                int[] xPoints = { cx - 14, cx, cx + 14, cx + 14, cx, cx - 14 };
                int[] yPoints = { cy - 10, cy - 16, cy - 10, cy + 8, cy + 18, cy + 8 };
                g2d.fillPolygon(xPoints, yPoints, 6);

                // Star
                g2d.setColor(Color.WHITE);
                g2d.fillOval(cx - 4, cy - 4, 8, 8);

                g2d.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(44, 44));
        leftPanel.add(iconLabel);

        JLabel headerLabel = new JLabel("ADMIN PANEL");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(TEXT_PRIMARY);
        leftPanel.add(headerLabel);

        headerPanel.add(leftPanel, BorderLayout.WEST);

        // Right side panel: Card status + buttons
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.add(Box.createHorizontalStrut(20));

        // Card status label
        cardStatusLabel = new JLabel("Trạng thái thẻ: Đang kiểm tra...");
        cardStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cardStatusLabel.setForeground(TEXT_SECONDARY);
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

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Custom UI settings for dark tabs
        UIManager.put("TabbedPane.selected", BACKGROUND_CARD);
        UIManager.put("TabbedPane.background", BACKGROUND_DARK);
        UIManager.put("TabbedPane.foreground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.contentAreaColor", BACKGROUND_DARK);
        UIManager.put("TabbedPane.focus", BACKGROUND_CARD);
        UIManager.put("TabbedPane.selectHighlight", ADMIN_COLOR);
        UIManager.put("TabbedPane.darkShadow", BORDER_COLOR);
        UIManager.put("TabbedPane.shadow", BORDER_COLOR);
        UIManager.put("TabbedPane.light", BACKGROUND_CARD);
        UIManager.put("TabbedPane.highlight", BACKGROUND_CARD);

        // Tabbed pane with custom dark styling
        tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                // Transparent - let mainPanel gradient show through
            }
        };
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setOpaque(false);
        tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        tabbedPane.setTabPlacement(JTabbedPane.TOP);

        // Tab content panels
        tabbedPane.addTab("", createResetPINPanel());
        tabbedPane.addTab("", createImportCardDataPanel());
        tabbedPane.addTab("", createGetInfoPanel());
        tabbedPane.addTab("", createManageBooksPanel());
        tabbedPane.addTab("", createManageStationeryPanel());

        // Tab icons and labels - custom styled buttons as tab components
        String[] tabNames = { "Đổi Mã PIN", "Nạp Dữ Liệu", "Lấy Thông Tin", "QL Sách", "QL VPP" };

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            final int tabIndex = i;
            JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    boolean isSelected = tabbedPane.getSelectedIndex() == tabIndex;

                    if (isSelected) {
                        // Selected tab - gradient background
                        GradientPaint gradient = new GradientPaint(
                                0, 0, ADMIN_COLOR,
                                getWidth(), 0, ADMIN_DARK);
                        g2d.setPaint(gradient);
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);
                    } else {
                        // Unselected - subtle dark background
                        g2d.setColor(new Color(51, 65, 85, 150));
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);
                    }

                    g2d.dispose();
                }
            };
            tabComponent.setOpaque(false);
            tabComponent.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tabComponent.setBorder(new EmptyBorder(8, 15, 8, 15)); // Increased vertical padding

            // Icon - Custom Vector Icon
            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    drawTabIcon(g2d, tabIndex, getWidth() / 2, getHeight() / 2);
                    g2d.dispose();
                }
            };
            iconPanel.setOpaque(false);
            iconPanel.setPreferredSize(new Dimension(24, 24)); // Fixed size to prevent cutting
            tabComponent.add(iconPanel);

            // Text
            JLabel textLabel = new JLabel(tabNames[i]);
            textLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            textLabel.setForeground(TEXT_PRIMARY);
            tabComponent.add(textLabel);

            // Mouse listener for repaint on selection change
            tabComponent.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    tabbedPane.setSelectedIndex(tabIndex);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    tabComponent.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    tabComponent.repaint();
                }
            });

            tabbedPane.setTabComponentAt(i, tabComponent);
        }

        // Add tab change listener to repaint tabs
        tabbedPane.addChangeListener(e -> {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                tabbedPane.getTabComponentAt(i).repaint();
            }
        });

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Check card status after UI initialized
        checkCardStatusOnStartup();
    }

    /**
     * Draw custom tab icons
     */
    private void drawTabIcon(Graphics2D g2, int index, int x, int y) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));

        switch (index) {
            case 0: // Change PIN (Lock)
                g2.drawRoundRect(x - 7, y - 2, 14, 10, 2, 2); // Body
                g2.drawArc(x - 5, y - 9, 10, 14, 0, 180); // Shackle
                g2.fillOval(x - 1, y + 2, 2, 2); // Keyhole
                break;
            case 1: // Import Data (Card)
                g2.drawRoundRect(x - 10, y - 7, 20, 14, 2, 2); // Card
                g2.drawRect(x - 7, y - 3, 5, 4); // Chip
                g2.drawLine(x, y - 3, x + 7, y - 3); // Lines
                break;
            case 2: // Get Info (Clipboard)
                g2.drawRoundRect(x - 8, y - 10, 16, 20, 2, 2); // Board
                g2.fillRect(x - 5, y - 10, 10, 4); // Clip
                g2.drawLine(x - 4, y - 2, x + 4, y - 2); // Lines
                g2.drawLine(x - 4, y + 2, x + 4, y + 2);
                g2.drawLine(x - 4, y + 6, x + 4, y + 6);
                break;
            case 3: // Manage Books (List icon)
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(x - 7, y - 6, x + 7, y - 6);
                g2.drawLine(x - 7, y, x + 7, y);
                g2.drawLine(x - 7, y + 6, x + 7, y + 6);
                g2.fillOval(x - 9, y - 8, 4, 4);
                g2.fillOval(x - 9, y - 2, 4, 4);
                g2.fillOval(x - 9, y + 4, 4, 4);
                break;
            case 4: // Manage VPP (Grid icon)
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRect(x - 8, y - 8, 6, 6);
                g2.drawRect(x + 2, y - 8, 6, 6);
                g2.drawRect(x - 8, y + 2, 6, 6);
                g2.drawRect(x + 2, y + 2, 6, 6);
                break;
        }
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
                connManager = CardConnectionManager.getInstance();
                connManager.connectCard();

                CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                // setupManager.getPublicKey();

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
                connManager = CardConnectionManager.getInstance();
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
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Transparent to show mainPanel gradient
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 40, 15, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Glassmorphism card
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Glass background
                g2d.setColor(new Color(30, 41, 59, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Border glow
                g2d.setColor(new Color(239, 68, 68, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 24, 24);

                g2d.dispose();
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Lock icon
        JLabel lockIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // Glow
                g2d.setColor(new Color(239, 68, 68, 40));
                g2d.fillOval(cx - 35, cy - 35, 70, 70);

                // Lock body
                GradientPaint gradient = new GradientPaint(cx - 18, cy - 10, ADMIN_COLOR, cx + 18, cy + 22, ADMIN_DARK);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(cx - 18, cy - 5, 36, 28, 6, 6);

                // Lock shackle
                g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawArc(cx - 12, cy - 22, 24, 24, 0, 180);

                // Keyhole
                g2d.setColor(new Color(30, 41, 59));
                g2d.fillOval(cx - 4, cy + 2, 8, 8);
                g2d.fillRect(cx - 2, cy + 7, 4, 8);

                g2d.dispose();
            }
        };
        lockIcon.setPreferredSize(new Dimension(70, 70));
        lockIcon.setMaximumSize(new Dimension(70, 70));
        lockIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(lockIcon);

        cardPanel.add(Box.createVerticalStrut(15));

        // Title
        JLabel titleLabel = new JLabel("Đổi Mã PIN Thẻ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);

        cardPanel.add(Box.createVerticalStrut(8));

        JLabel subtitleLabel = new JLabel("Nhập thông tin để đổi mã PIN");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(subtitleLabel);

        cardPanel.add(Box.createVerticalStrut(25));

        // Admin PIN input
        JLabel adminPINLabel = new JLabel("Mã PIN Admin");
        adminPINLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminPINLabel.setForeground(TEXT_SECONDARY);
        adminPINLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(adminPINLabel);
        cardPanel.add(Box.createVerticalStrut(6));

        JPasswordField adminPINField = new JPasswordField();
        adminPINField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        adminPINField.setMaximumSize(new Dimension(320, 45));
        adminPINField.setPreferredSize(new Dimension(320, 45));
        adminPINField.setBackground(INPUT_BG);
        adminPINField.setForeground(TEXT_PRIMARY);
        adminPINField.setCaretColor(TEXT_PRIMARY);
        adminPINField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(8, 15, 8, 15)));
        adminPINField.setHorizontalAlignment(JTextField.CENTER);
        adminPINField.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(adminPINField);
        cardPanel.add(Box.createVerticalStrut(15));

        // New PIN input
        JLabel newPINLabel = new JLabel("Mã PIN Mới");
        newPINLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        newPINLabel.setForeground(TEXT_SECONDARY);
        newPINLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(newPINLabel);
        cardPanel.add(Box.createVerticalStrut(6));

        JPasswordField newPINField = new JPasswordField();
        newPINField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        newPINField.setMaximumSize(new Dimension(320, 45));
        newPINField.setPreferredSize(new Dimension(320, 45));
        newPINField.setBackground(INPUT_BG);
        newPINField.setForeground(TEXT_PRIMARY);
        newPINField.setCaretColor(TEXT_PRIMARY);
        newPINField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(8, 15, 8, 15)));
        newPINField.setHorizontalAlignment(JTextField.CENTER);
        newPINField.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(newPINField);
        cardPanel.add(Box.createVerticalStrut(15));

        // Confirm PIN input
        JLabel confirmPINLabel = new JLabel("Xác Nhận Mã PIN");
        confirmPINLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmPINLabel.setForeground(TEXT_SECONDARY);
        confirmPINLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(confirmPINLabel);
        cardPanel.add(Box.createVerticalStrut(6));

        JPasswordField confirmPINField = new JPasswordField();
        confirmPINField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        confirmPINField.setMaximumSize(new Dimension(320, 45));
        confirmPINField.setPreferredSize(new Dimension(320, 45));
        confirmPINField.setBackground(INPUT_BG);
        confirmPINField.setForeground(TEXT_PRIMARY);
        confirmPINField.setCaretColor(TEXT_PRIMARY);
        confirmPINField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(8, 15, 8, 15)));
        confirmPINField.setHorizontalAlignment(JTextField.CENTER);
        confirmPINField.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(confirmPINField);
        cardPanel.add(Box.createVerticalStrut(10));

        // Message label
        JLabel messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(ADMIN_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(messageLabel);
        cardPanel.add(Box.createVerticalStrut(20));

        // Button panel - use BoxLayout to prevent wrapping
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Reset button with gradient
        JButton resetButton = new JButton("ĐỔI MÃ PIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, ADMIN_DARK, getWidth(), 0, ADMIN_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(248, 113, 113), getWidth(), 0, ADMIN_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, ADMIN_COLOR, getWidth(), 0, ADMIN_DARK);
                }

                // Shadow
                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(239, 68, 68, 40));
                    g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 2, 10, 10);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 3, 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 3 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resetButton.setPreferredSize(new Dimension(130, 42));
        resetButton.setMinimumSize(new Dimension(130, 42));
        resetButton.setMaximumSize(new Dimension(130, 42));
        resetButton.setBorderPainted(false);
        resetButton.setContentAreaFilled(false);
        resetButton.setFocusPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> {
            String adminPIN = new String(adminPINField.getPassword()).trim();
            String newPIN = new String(newPINField.getPassword()).trim();
            String confirmPIN = new String(confirmPINField.getPassword()).trim();

            if (adminPIN.isEmpty()) {
                messageLabel.setText("Vui lòng nhập mã PIN admin!");
                return;
            }
            if (adminPIN.length() != 6) {
                messageLabel.setText("Mã PIN admin phải đúng 6 ký tự!");
                return;
            }
            if (newPIN.isEmpty()) {
                messageLabel.setText("Vui lòng nhập mã PIN mới!");
                return;
            }
            if (newPIN.length() != 6) {
                messageLabel.setText("Mã PIN mới phải đúng 6 ký tự!");
                return;
            }
            if (!newPIN.equals(confirmPIN)) {
                messageLabel.setText("Mã PIN xác nhận không khớp!");
                return;
            }

            resetPINOnCard(adminPIN, newPIN, adminPINField, newPINField, confirmPINField, messageLabel);
        });
        buttonPanel.add(resetButton);

        buttonPanel.add(Box.createHorizontalStrut(20));

        // Clear button
        JButton cancelButton = new JButton("XÓA") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(BORDER_COLOR);
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(71, 85, 105, 200));
                } else {
                    g2d.setColor(BACKGROUND_CARD_LIGHT);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
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
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setPreferredSize(new Dimension(130, 42));
        cancelButton.setMinimumSize(new Dimension(130, 42));
        cancelButton.setMaximumSize(new Dimension(130, 42));
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

        cardPanel.add(buttonPanel);

        panel.add(cardPanel, gbc);
        return panel;
    }

    /**
     * Create Import Card Data tab
     */
    private JPanel createImportCardDataPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Transparent to show mainPanel gradient
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Glassmorphism card wrapper
        JPanel cardWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Glass background
                g2d.setColor(new Color(30, 41, 59, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Border
                g2d.setColor(new Color(71, 85, 105, 100));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.dispose();
            }
        };
        cardWrapper.setLayout(new BorderLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header with title and button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Title with icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel cardIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Card shape
                g2d.setColor(new Color(34, 197, 94));
                g2d.fillRoundRect(4, 6, w - 8, h - 12, 4, 4);

                // Chip
                g2d.setColor(new Color(250, 204, 21));
                g2d.fillRoundRect(8, 10, 10, 8, 2, 2);

                // Lines
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRect(22, 14, 12, 2);
                g2d.fillRect(8, 22, 26, 2);

                g2d.dispose();
            }
        };
        cardIcon.setPreferredSize(new Dimension(40, 32));
        titlePanel.add(cardIcon);

        JLabel titleLabel = new JLabel("Quản Lý Thẻ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        titlePanel.add(titleLabel);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        // Add button with gradient
        JButton addButton = new JButton("+ THÊM THẺ MỚI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(16, 150, 100), getWidth(), 0, SUCCESS_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(52, 211, 153), getWidth(), 0, SUCCESS_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                }

                // Shadow
                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(16, 185, 129, 40));
                    g2d.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 1, 10, 10);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setPreferredSize(new Dimension(150, 38));
        addButton.setMinimumSize(new Dimension(150, 38));
        addButton.setMaximumSize(new Dimension(150, 38));
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Create table model - no action column
        String[] columns = { "CardID", "Họ Tên", "SĐT", "Ngày Sinh", "Loại Thẻ", "Tổng Chi", "Điểm", "Khóa" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        addButton.addActionListener(e -> showAddCardDialog(tableModel));

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(addButton);
        headerPanel.add(buttonWrapper, BorderLayout.EAST);

        cardWrapper.add(headerPanel, BorderLayout.NORTH);

        // Table with modern styling - better contrast
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setBackground(new Color(24, 24, 27));
        table.setForeground(new Color(250, 250, 250));
        table.setGridColor(new Color(63, 63, 70));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(99, 102, 241, 100));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Header styling - Green scheme matching Add Button
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setOpaque(true);
                label.setBackground(new Color(16, 150, 100)); // Darker green from button gradient end
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(6, 78, 59)),
                        BorderFactory.createEmptyBorder(0, 5, 0, 5) // Adjust padding
                ));
                return label;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Column widths - no action column
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);

        // Scroll pane with dark styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(24, 24, 27));
        scrollPane.setBackground(new Color(24, 24, 27));

        // Custom scrollbar
        scrollPane.getVerticalScrollBar().setBackground(new Color(39, 39, 42));
        scrollPane.getHorizontalScrollBar().setBackground(new Color(39, 39, 42));

        cardWrapper.add(scrollPane, BorderLayout.CENTER);

        // Load cards
        loadCardsToTable(tableModel);

        panel.add(cardWrapper, BorderLayout.CENTER);
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
        dialog.setSize(550, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        dialog.setUndecorated(true);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105), 1));

        // Main content panel with dark background
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Dark gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(30, 27, 75));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel titleLabel = new JLabel("Thêm Thẻ Mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2d.setColor(ADMIN_COLOR);
                } else {
                    g2d.setColor(TEXT_SECONDARY);
                }
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth("X")) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString("X", textX, textY);
                g2d.dispose();
            }
        };
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dialog.dispose());
        headerPanel.add(closeButton, BorderLayout.EAST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 30, 20, 30));

        // Helper method style - create styled input fields
        JTextField fullNameField = createStyledTextField("Họ và Tên");
        JTextField phoneField = createStyledTextField("Số điện thoại");
        // Date of Birth DatePicker
        JPanel dobPanel = new JPanel();
        dobPanel.setLayout(new BoxLayout(dobPanel, BoxLayout.X_AXIS));
        dobPanel.setOpaque(false);

        String[] days = new String[31];
        for (int i = 0; i < 31; i++)
            days[i] = String.format("%02d", i + 1);
        JComboBox<String> dayCombo = new JComboBox<>(days);

        String[] months = new String[12];
        for (int i = 0; i < 12; i++)
            months[i] = String.format("%02d", i + 1);
        JComboBox<String> monthCombo = new JComboBox<>(months);

        int currentYear = java.time.Year.now().getValue();
        String[] years = new String[100];
        for (int i = 0; i < 100; i++)
            years[i] = String.valueOf(currentYear - i);
        JComboBox<String> yearCombo = new JComboBox<>(years);

        // Style helper
        java.util.function.Consumer<JComboBox<String>> styleCombo = combo -> {
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            combo.setBackground(INPUT_BG);
            combo.setForeground(TEXT_PRIMARY);
            combo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            combo.setFocusable(false);
        };
        // Update days based on month/year
        java.awt.event.ActionListener updateDays = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String selectedDay = (String) dayCombo.getSelectedItem();
                int month = Integer.parseInt((String) monthCombo.getSelectedItem());
                int year = Integer.parseInt((String) yearCombo.getSelectedItem());

                int daysInMonth;
                if (month == 2) {
                    daysInMonth = (java.time.Year.of(year).isLeap()) ? 29 : 28;
                } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                    daysInMonth = 30;
                } else {
                    daysInMonth = 31;
                }

                String[] newDays = new String[daysInMonth];
                for (int i = 0; i < daysInMonth; i++) {
                    newDays[i] = String.format("%02d", i + 1);
                }

                dayCombo.setModel(new DefaultComboBoxModel<>(newDays));

                // Restore selection if possible, otherwise select last day
                if (selectedDay != null && Integer.parseInt(selectedDay) <= daysInMonth) {
                    dayCombo.setSelectedItem(selectedDay);
                } else {
                    dayCombo.setSelectedItem(newDays[daysInMonth - 1]);
                }
            }
        };

        monthCombo.addActionListener(updateDays);
        yearCombo.addActionListener(updateDays);

        styleCombo.accept(dayCombo);
        styleCombo.accept(monthCombo);
        styleCombo.accept(yearCombo);

        dobPanel.add(dayCombo);
        dobPanel.add(Box.createHorizontalStrut(5));
        dobPanel.add(monthCombo);
        dobPanel.add(Box.createHorizontalStrut(5));
        dobPanel.add(yearCombo);

        JTextField addressField = createStyledTextField("Địa chỉ");

        // Add fields with labels
        formPanel.add(createFormRow("Họ và Tên", fullNameField));
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(createFormRow("Số điện thoại", phoneField));
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(createFormRow("Ngày sinh", dobPanel));
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(createFormRow("Địa chỉ", addressField));
        formPanel.add(Box.createVerticalStrut(15));

        // Image chooser
        JLabel imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(120, 90));
        imagePreview.setMinimumSize(new Dimension(120, 90));
        imagePreview.setMaximumSize(new Dimension(120, 90));
        imagePreview.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        imagePreview.setOpaque(true);
        imagePreview.setBackground(INPUT_BG);

        final String[] selectedImage = new String[1];

        JButton chooseImageButton = new JButton("Chọn Ảnh") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(71, 85, 105));
                } else {
                    g2d.setColor(BACKGROUND_CARD);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.setColor(BORDER_COLOR);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2d.setColor(TEXT_SECONDARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        };
        chooseImageButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chooseImageButton.setPreferredSize(new Dimension(110, 35));
        chooseImageButton.setMinimumSize(new Dimension(110, 35));
        chooseImageButton.setMaximumSize(new Dimension(110, 35));
        chooseImageButton.setBorderPainted(false);
        chooseImageButton.setContentAreaFilled(false);
        chooseImageButton.setFocusPainted(false);
        chooseImageButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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
                    }
                } catch (Exception ex) {
                    imagePreview.setIcon(null);
                }
            }
        });

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
        imagePanel.setOpaque(false);
        imagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel imageLabelTitle = new JLabel("Ảnh đại diện");
        imageLabelTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imageLabelTitle.setForeground(TEXT_SECONDARY);
        imageLabelTitle.setPreferredSize(new Dimension(100, 25));
        imageLabelTitle.setMinimumSize(new Dimension(100, 25));
        imageLabelTitle.setMaximumSize(new Dimension(100, 25));

        imagePanel.add(imageLabelTitle);
        imagePanel.add(Box.createHorizontalStrut(10));
        imagePanel.add(chooseImageButton);
        imagePanel.add(Box.createHorizontalStrut(15));
        imagePanel.add(imagePreview);

        formPanel.add(imagePanel);
        formPanel.add(Box.createVerticalStrut(25));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.add(Box.createHorizontalGlue());

        // Save button with gradient
        JButton saveButton = new JButton("LƯU THẺ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(16, 150, 100), getWidth(), 0, SUCCESS_COLOR);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        };
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setPreferredSize(new Dimension(130, 42));
        saveButton.setMinimumSize(new Dimension(130, 42));
        saveButton.setMaximumSize(new Dimension(130, 42));
        saveButton.setBorderPainted(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();

            String day = (String) dayCombo.getSelectedItem();
            String month = (String) monthCombo.getSelectedItem();
            String year = (String) yearCombo.getSelectedItem();
            String dob = year + "-" + month + "-" + day;

            String address = addressField.getText().trim();

            if (fullName.isEmpty() || dob.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Phone Number (10 digits, starts with 0)
            if (!phone.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(dialog, "Số điện thoại không hợp lệ! (Phải có 10 số và bắt đầu bằng 0)",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Full Name (At least 2 chars)
            if (fullName.length() < 2) {
                JOptionPane.showMessageDialog(dialog, "Tên quá ngắn! Vui lòng nhập họ tên đầy đủ.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Address (At least 5 chars)
            if (address.length() < 5) {
                JOptionPane.showMessageDialog(dialog, "Địa chỉ quá ngắn! Vui lòng nhập địa chỉ cụ thể.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            new Thread(() -> {
                try {
                    String[] pins = showPinInputDialog();
                    if (pins == null || pins.length < 2) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "Nhập PIN bị hủy!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE));
                        return;
                    }

                    String userPin = pins[0];
                    String adminPin = pins[1];

                    if (userPin.length() != 6 || adminPin.length() != 6) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "PIN phải có đúng 6 số!",
                                "Lỗi", JOptionPane.ERROR_MESSAGE));
                        return;
                    }

                    SwingUtilities.invokeLater(() -> dialog.dispose());

                    String cardId = generateCardId();
                    String formattedDob = convertDateFormat(dob);
                    String regDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));

                    connManager = CardConnectionManager.getInstance();
                    connManager.connectCard();

                    CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                    setupManager.setupCard(userPin, adminPin);
                    setupManager.getPublicKey();
                    setupManager.verifyPin(userPin);
                    setupManager.initUserData(cardId, fullName, formattedDob, phone, address, regDate);

                    boolean imageUploaded = false;
                    if (selectedImage[0] != null && !selectedImage[0].isEmpty()) {
                        File imageFile = new File(selectedImage[0]);
                        if (imageFile.exists()) {
                            CardImageManager imageManager = new CardImageManager(connManager.getChannel());
                            imageUploaded = imageManager.uploadImage(imageFile);
                        }
                    }

                    connManager.disconnectCard();

                    final boolean imgUploaded = imageUploaded;
                    if (insertCard(cardId, fullName, phone, dob, address, "Basic", selectedImage[0])) {
                        try {
                            byte[] pubBytes = setupManager.getKeyManager().getCardPublicKeyEncoded();
                            if (pubBytes != null) {
                                cardService.updateCardPublicKey(cardId, pubBytes);
                            }
                        } catch (Exception _e) {
                            System.err.println("Warning: failed to save card public key: " + _e.getMessage());
                        }

                        SwingUtilities.invokeLater(() -> {
                            String imageStatus = selectedImage[0] != null && !selectedImage[0].isEmpty()
                                    ? (imgUploaded ? "\nẢnh: ✓" : "\nẢnh: ✗")
                                    : "";
                            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(AdminPanel.this),
                                    "Thêm thẻ thành công!\nCardID: " + cardId + "\nUser PIN: " + userPin
                                            + "\nAdmin PIN: " + adminPin + imageStatus,
                                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            loadCardsToTable(tableModel);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(AdminPanel.this), "Lỗi khi thêm thẻ!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(AdminPanel.this), "Lỗi: " + ex.getMessage(), "Lỗi",
                            JOptionPane.ERROR_MESSAGE));
                    ex.printStackTrace();
                }
            }).start();
        });
        buttonPanel.add(saveButton);

        buttonPanel.add(Box.createHorizontalStrut(15));

        // Cancel button
        JButton cancelButton = new JButton("HỦY") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getModel().isRollover() ? new Color(71, 85, 105) : BACKGROUND_CARD);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.setColor(BORDER_COLOR);
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
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setPreferredSize(new Dimension(100, 42));
        cancelButton.setMinimumSize(new Dimension(100, 42));
        cancelButton.setMaximumSize(new Dimension(100, 42));
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        buttonPanel.add(Box.createHorizontalGlue());
        formPanel.add(buttonPanel);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    // Helper method to create styled text field
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(TEXT_MUTED);
                    g2d.setFont(getFont().deriveFont(Font.ITALIC));
                    g2d.drawString(placeholder, 12, getHeight() / 2 + 5);
                    g2d.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)));
        field.setPreferredSize(new Dimension(300, 42));
        field.setMinimumSize(new Dimension(300, 42));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return field;
    }

    // Helper method to create form row
    private JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setPreferredSize(new Dimension(100, 42));
        label.setMinimumSize(new Dimension(100, 42));
        label.setMaximumSize(new Dimension(100, 42));

        row.add(label);
        row.add(Box.createHorizontalStrut(10));
        row.add(field);

        return row;
    }

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
                connManager = CardConnectionManager.getInstance();
                connManager.connectCard();

                SwingUtilities.invokeLater(() -> {
                    messageLabel.setText("Đang đổi mã PIN trên thẻ...");
                });

                // Create PIN manager and reset
                smartcard.CardPinManager pinManager = new smartcard.CardPinManager(connManager.getChannel());
                boolean success = pinManager.resetUserPin(adminPIN, newUserPIN);

                if (success) {
                    // Retrieve public key after reset as per user request
                    smartcard.CardSetupManager setupMgr = new smartcard.CardSetupManager(connManager.getChannel());
                    if (setupMgr.getPublicKey()) {
                        byte[] pubBytes = setupMgr.getKeyManager().getCardPublicKeyEncoded();
                        if (pubBytes != null) {
                            // Assuming cardId is needed? resetUserPin doesn't have cardId in context?
                            // AdminPanel.resetPINOnCard is likely context specific.
                            // The method `resetPINOnCard` in AdminPanel doesn't seem to have `cardId`
                            // passed to it?
                            // Let's check how it's called.
                            // It's called from `createResetPINPanel`.
                            // This might be an issue. If we don't know WHICH card it is (CardID), we can't
                            // update DB.
                            // However, we can GET CardID from the card itself!

                            // Let's fetch Card ID first to be safe.
                            // We can use INS_AUTH_GET_CARD_ID or extract it.
                            String cardIdVal = smartcard.CardIdExtractor.extractCardId(connManager.getChannel(),
                                    setupMgr.getKeyManager());
                            if (cardIdVal != null) {
                                services.CardService cs = new services.CardService();
                                cs.updateCardPublicKey(cardIdVal, pubBytes);
                                System.out.println("Updated Public Key for Card: " + cardIdVal);
                            }
                        }
                    }

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
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 40, 20, 40));

        // Top section - Title and subtitle
        JPanel headerSection = new JPanel();
        headerSection.setLayout(new BoxLayout(headerSection, BoxLayout.Y_AXIS));
        headerSection.setOpaque(false);
        headerSection.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel titleLabel = new JLabel("LẤY THÔNG TIN NGƯỜI DÙNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerSection.add(titleLabel);

        headerSection.add(Box.createVerticalStrut(8));

        JLabel subtitleLabel = new JLabel("Quét thẻ hoặc nhập thông tin thủ công để truy xuất dữ liệu");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerSection.add(subtitleLabel);

        panel.add(headerSection, BorderLayout.NORTH);

        // Main content - Card container with glassmorphism
        JPanel cardContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark semi-transparent background
                g2d.setColor(new Color(30, 41, 59, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Subtle border
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2d.dispose();
            }
        };
        cardContainer.setLayout(new GridBagLayout());
        cardContainer.setOpaque(false);
        cardContainer.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();

        // Left side - Image section
        JPanel imageSection = new JPanel(new BorderLayout());
        imageSection.setOpaque(false);
        imageSection.setMinimumSize(new Dimension(280, 300));

        JLabel imageTitleLabel = new JLabel("ẢNH THẺ");
        imageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imageTitleLabel.setForeground(ADMIN_COLOR);
        imageTitleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        imageSection.add(imageTitleLabel, BorderLayout.NORTH);

        // Dashed border image area
        JPanel imageArea = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark background
                g2d.setColor(new Color(15, 23, 42));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Dashed border
                g2d.setColor(BORDER_COLOR);
                float[] dash = { 8, 6 };
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2d.drawRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 8, 8);

                g2d.dispose();
            }
        };
        imageArea.setOpaque(false);
        imageArea.setPreferredSize(new Dimension(260, 280));
        imageArea.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Center content for image placeholder
        JPanel imagePlaceholder = new JPanel();
        imagePlaceholder.setLayout(new BoxLayout(imagePlaceholder, BoxLayout.Y_AXIS));
        imagePlaceholder.setOpaque(false);

        // User icon placeholder
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 60;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Draw user icon
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(2));
                // Frame
                g2d.drawRoundRect(x, y, size, size, 8, 8);
                // Head
                g2d.drawOval(x + size / 2 - 12, y + 12, 24, 24);
                // Body
                g2d.drawArc(x + 10, y + 38, size - 20, 30, 0, 180);

                g2d.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(80, 80));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePlaceholder.add(Box.createVerticalGlue());
        imagePlaceholder.add(iconLabel);

        JLabel imageLabel = new JLabel("Chưa có ảnh thẻ");
        imageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        imageLabel.setForeground(TEXT_MUTED);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePlaceholder.add(Box.createVerticalStrut(15));
        imagePlaceholder.add(imageLabel);
        imagePlaceholder.add(Box.createVerticalGlue());

        imageArea.add(imagePlaceholder, BorderLayout.CENTER);
        imageSection.add(imageArea, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 25);
        cardContainer.add(imageSection, gbc);

        // Right side - Log section
        JPanel logSection = new JPanel(new BorderLayout());
        logSection.setOpaque(false);
        logSection.setMinimumSize(new Dimension(350, 300));

        // Log header with status
        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        logHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel logTitleLabel = new JLabel("Nhật ký hệ thống");
        logTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logTitleLabel.setForeground(TEXT_SECONDARY);
        logHeader.add(logTitleLabel, BorderLayout.WEST);

        // Status indicator
        JLabel statusLabel = new JLabel("Sẵn sàng") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Green dot
                g2d.setColor(SUCCESS_COLOR);
                g2d.fillOval(2, (getHeight() - 8) / 2, 8, 8);

                // Text
                g2d.setColor(SUCCESS_COLOR);
                g2d.setFont(getFont());
                g2d.drawString("Sẵn sàng", 14, getHeight() / 2 + 4);

                g2d.dispose();
            }
        };
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setPreferredSize(new Dimension(80, 20));
        logHeader.add(statusLabel, BorderLayout.EAST);

        logSection.add(logHeader, BorderLayout.NORTH);

        // Log text area
        JTextArea infoArea = new JTextArea();
        infoArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(new Color(15, 23, 42));
        infoArea.setForeground(TEXT_SECONDARY);
        infoArea.setCaretColor(SUCCESS_COLOR);
        infoArea.setBorder(new EmptyBorder(15, 15, 15, 15));
        infoArea.setText("Nhấn nút 'LẤY THÔNG TIN' để đọc dữ liệu từ thẻ...\n|");

        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setPreferredSize(new Dimension(380, 200));
        scrollPane.getViewport().setBackground(new Color(15, 23, 42));
        logSection.add(scrollPane, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 0, 0);
        cardContainer.add(logSection, gbc);

        // Card container fills horizontal space
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.weightx = 1.0;
        wrapGbc.weighty = 1.0;
        wrapGbc.fill = GridBagConstraints.HORIZONTAL;
        wrapGbc.anchor = GridBagConstraints.NORTH;
        wrapGbc.insets = new Insets(0, 20, 0, 20);
        centerWrapper.add(cardContainer, wrapGbc);

        panel.add(centerWrapper, BorderLayout.CENTER);

        // Bottom section - Button and helper text
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setOpaque(false);
        bottomSection.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Get Info button with gradient
        JButton getInfoButton = new JButton("LẤY THÔNG TIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(16, 150, 100), getWidth(), 0, SUCCESS_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(52, 211, 153), getWidth(), 0, SUCCESS_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                }

                // Shadow
                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(16, 185, 129, 50));
                    g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 2, 10, 10);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);

                // Icon and text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textX = (getWidth() - textWidth) / 2 + 12;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;

                // Card icon
                int iconX = textX - 25;
                int iconY = textY - 12;
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(iconX, iconY, 16, 12, 2, 2);
                g2d.drawLine(iconX + 4, iconY + 5, iconX + 12, iconY + 5);
                g2d.drawLine(iconX + 4, iconY + 8, iconX + 8, iconY + 8);

                g2d.drawString(text, textX, textY);

                g2d.dispose();
            }
        };
        getInfoButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        getInfoButton.setForeground(Color.WHITE);
        getInfoButton.setPreferredSize(new Dimension(220, 48));
        getInfoButton.setMaximumSize(new Dimension(220, 48));
        getInfoButton.setBorderPainted(false);
        getInfoButton.setContentAreaFilled(false);
        getInfoButton.setFocusPainted(false);
        getInfoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        getInfoButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button action - keep original logic
        getInfoButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText("Đang kết nối đến thẻ...\n");
                        infoArea.setForeground(TEXT_SECONDARY);
                        imageLabel.setIcon(null);
                        imageLabel.setText("Đang tải...");
                        iconLabel.setVisible(true);
                    });

                    connManager = CardConnectionManager.getInstance();
                    connManager.connectCard();

                    SwingUtilities.invokeLater(() -> {
                        infoArea.append("✓ Kết nối thành công\nĐang lấy thông tin...\n");
                    });

                    CardKeyManager keyManager = new CardKeyManager(connManager.getChannel());
                    keyManager.getPublicKey();

                    if (!keyManager.loadAppKeyPair()) {
                        throw new Exception("Không tìm thấy app keypair. Vui lòng thêm thẻ mới trước.");
                    }

                    CardInfoManager infoManager = new CardInfoManager(connManager.getChannel(), keyManager);
                    CardInfoManager.UserInfo userInfo = infoManager.getInfo();

                    try {
                        byte[] pubBytes = keyManager.getCardPublicKeyEncoded();
                        if (pubBytes != null && userInfo != null && userInfo.cardId != null
                                && !userInfo.cardId.isEmpty()) {
                            cardService.updateCardPublicKey(userInfo.cardId, pubBytes);
                        }
                    } catch (Exception _e) {
                        System.err.println("Warning: failed to save card public key to DB: " + _e.getMessage());
                    }

                    SwingUtilities.invokeLater(() -> {
                        infoArea.append("✓ Thông tin đã nhận\nĐang tải ảnh từ thẻ...\n");
                    });

                    CardImageManager imageManager = new CardImageManager(connManager.getChannel());
                    byte[] imageData = imageManager.downloadImage();

                    connManager.disconnectCard();

                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText(" Hoàn tất lấy thông tin\n\n" + userInfo.toString());
                        infoArea.setForeground(TEXT_PRIMARY);

                        if (imageData != null && imageManager.isValidJpeg(imageData)) {
                            try {
                                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                                if (img != null) {
                                    int maxW = 200;
                                    int maxH = 220;
                                    int w = img.getWidth();
                                    int h = img.getHeight();
                                    double scale = Math.min((double) maxW / w, (double) maxH / h);
                                    int newW = (int) (w * scale);
                                    int newH = (int) (h * scale);

                                    Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                                    imageLabel.setIcon(new ImageIcon(scaled));
                                    imageLabel.setText("");
                                    iconLabel.setVisible(false);
                                } else {
                                    imageLabel.setIcon(null);
                                    imageLabel.setText("Không thể đọc ảnh");
                                    iconLabel.setVisible(true);
                                }
                            } catch (Exception imgEx) {
                                imageLabel.setIcon(null);
                                imageLabel.setText("Lỗi: " + imgEx.getMessage());
                            }
                        } else {
                            imageLabel.setIcon(null);
                            imageLabel.setText("Thẻ chưa có ảnh");
                            iconLabel.setVisible(true);
                        }
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        infoArea.setText("✗ Lỗi: " + ex.getMessage());
                        infoArea.setForeground(ADMIN_COLOR);
                        imageLabel.setIcon(null);
                        imageLabel.setText("Lỗi");
                        iconLabel.setVisible(true);
                        JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(panel),
                                "Lỗi khi lấy thông tin: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    });
                    ex.printStackTrace();
                }
            }).start();
        });

        bottomSection.add(getInfoButton);
        bottomSection.add(Box.createVerticalStrut(10));

        JLabel helperText = new JLabel("Đảm bảo thiết bị đọc thẻ đã được kết nối");
        helperText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helperText.setForeground(TEXT_MUTED);
        helperText.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomSection.add(helperText);

        panel.add(bottomSection, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create Add Book tab
     */
    private JPanel createAddBookPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(25, 40, 20, 40));

        // Header section
        JPanel headerSection = new JPanel();
        headerSection.setLayout(new BoxLayout(headerSection, BoxLayout.Y_AXIS));
        headerSection.setOpaque(false);
        headerSection.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel titleLabel = new JLabel("THÊM SÁCH MỚI");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerSection.add(titleLabel);

        headerSection.add(Box.createVerticalStrut(8));

        JLabel subtitleLabel = new JLabel("Điền thông tin chi tiết để thêm sách vào hệ thống kho");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerSection.add(subtitleLabel);

        panel.add(headerSection, BorderLayout.NORTH);

        // Form card with glassmorphism
        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(30, 41, 59, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

                g2d.dispose();
            }
        };
        formCard.setLayout(new GridBagLayout());
        formCard.setOpaque(false);
        formCard.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        // Book ID - with icon
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Mã Sách:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField bookIdField = createDarkTextField("Mã sách tự động");
        bookIdField.setText(generateBookId());
        bookIdField.setEditable(false);
        bookIdField.setBackground(new Color(39, 39, 42));
        formCard.add(bookIdField, gbc);

        // Title
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Tên Sách:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField titleField = createDarkTextField("Nhập tên sách...");
        formCard.add(titleField, gbc);

        // Author
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Tác Giả:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField authorField = createDarkTextField("Nhập tên tác giả...");
        formCard.add(authorField, gbc);

        // Publisher
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Nhà Xuất Bản:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField publisherField = createDarkTextField("Nhập nhà xuất bản...");
        formCard.add(publisherField, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Giá (VNĐ):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField priceField = createDarkTextField("0");
        priceField.setText("0");
        formCard.add(priceField, gbc);

        // Stock
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Số Lượng:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField stockField = createDarkTextField("1");
        stockField.setText("1");
        formCard.add(stockField, gbc);

        // Category - styled combo box
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Thể Loại:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        String[] categories = { "Văn học", "Khoa học", "Thiếu nhi", "Manga", "Self-help", "Lập trình", "Kinh tế",
                "Tâm lý", "Lịch sử", "Khác" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryCombo.setBackground(INPUT_BG);
        categoryCombo.setForeground(TEXT_PRIMARY);
        categoryCombo.setMinimumSize(new Dimension(200, 32));
        categoryCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        formCard.add(categoryCombo, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 5, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        // Add button with gradient and icon
        JButton addButton = new JButton("THÊM SÁCH") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(16, 150, 100), getWidth(), 0, SUCCESS_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(52, 211, 153), getWidth(), 0, SUCCESS_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                }

                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(16, 185, 129, 50));
                    g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 2, 10, 10);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textX = (getWidth() - textWidth) / 2 + 10;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;

                // Plus icon
                int iconX = textX - 22;
                int iconY = textY - 8;
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(iconX, iconY, iconX + 12, iconY);
                g2d.drawLine(iconX + 6, iconY - 6, iconX + 6, iconY + 6);

                g2d.drawString(text, textX, textY);
                g2d.dispose();
            }
        };
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(160, 48));
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add button action - keep original logic
        addButton.addActionListener(e -> {
            String bookId = bookIdField.getText().trim();
            String bookTitle = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String imagePath = ""; // No image path since we removed image picker

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

            if (insertBook(bookId, bookTitle, author, publisher, price, stock, category, imagePath)) {
                JOptionPane.showMessageDialog(panel, "Thêm sách thành công!\nMã sách: " + bookId, "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                bookIdField.setText(generateBookId());
                titleField.setText("");
                authorField.setText("");
                publisherField.setText("");
                priceField.setText("0");
                stockField.setText("1");
                categoryCombo.setSelectedIndex(0);
            } else {
                JOptionPane.showMessageDialog(panel, "Lỗi khi thêm sách vào database!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Clear button with icon
        JButton clearButton = new JButton("XÓA FORM") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(55, 65, 81));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(75, 85, 99));
                } else {
                    g2d.setColor(new Color(55, 65, 81));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);

                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 10, 10);

                g2d.setColor(TEXT_SECONDARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textX = (getWidth() - textWidth) / 2 + 10;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;

                // Trash icon
                int iconX = textX - 20;
                int iconY = textY - 10;
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(iconX, iconY, 12, 14, 2, 2);
                g2d.drawLine(iconX - 2, iconY, iconX + 14, iconY);
                g2d.drawLine(iconX + 4, iconY - 3, iconX + 8, iconY - 3);

                g2d.drawString(text, textX, textY);
                g2d.dispose();
            }
        };
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearButton.setForeground(TEXT_SECONDARY);
        clearButton.setPreferredSize(new Dimension(140, 48));
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
        });

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        formCard.add(buttonPanel, gbc);

        // Center wrapper with horizontal fill
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.weightx = 1.0;
        wrapGbc.weighty = 1.0;
        wrapGbc.fill = GridBagConstraints.HORIZONTAL;
        wrapGbc.anchor = GridBagConstraints.NORTH;
        wrapGbc.insets = new Insets(0, 50, 0, 50);
        centerWrapper.add(formCard, wrapGbc);

        panel.add(centerWrapper, BorderLayout.CENTER);
        return panel;
    }

    // Create Add Stationery tab (Thêm VPP)
    private JPanel createAddStationeryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("THÊM VĂN PHÒNG PHẨM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("Điền thông tin chi tiết để thêm VPP vào hệ thống kho");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitleLabel.setForeground(TEXT_SECONDARY);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subTitleLabel);
        headerPanel.add(Box.createVerticalStrut(30));

        panel.add(headerPanel, BorderLayout.NORTH);

        // Form Card using Glassmorphism
        JPanel formCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(30, 41, 59, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(BORDER_COLOR);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2d.dispose();
            }
        };
        formCard.setOpaque(false);
        formCard.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;

        // ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Mã VPP:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        JTextField idField = createDarkTextField("Mã tự động");
        idField.setText(generateStationeryId());
        idField.setEditable(false);
        idField.setBackground(new Color(39, 39, 42));
        formCard.add(idField, gbc);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Tên VPP:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField nameField = createDarkTextField("Nhập tên VPP...");
        formCard.add(nameField, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Giá (VNĐ):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField priceField = createDarkTextField("0");
        formCard.add(priceField, gbc);

        // Stock
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formCard.add(createDarkLabel("Số Lượng:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField stockField = createDarkTextField("1");
        formCard.add(stockField, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 5, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton addButton = new JButton("THÊM VPP") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(16, 150, 100), getWidth(), 0, SUCCESS_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(52, 211, 153), getWidth(), 0, SUCCESS_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                }

                if (!getModel().isPressed()) {
                    g2d.setColor(new Color(16, 185, 129, 50));
                    g2d.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 2, 10, 10);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textX = (getWidth() - textWidth) / 2 + 10;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;

                // Plus icon
                int iconX = textX - 22;
                int iconY = textY - 8;
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(iconX, iconY, iconX + 12, iconY);
                g2d.drawLine(iconX + 6, iconY - 6, iconX + 6, iconY + 6);

                g2d.drawString(text, textX, textY);
                g2d.dispose();
            }
        };
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setForeground(Color.WHITE);
        addButton.setPreferredSize(new Dimension(160, 48));
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();

            if (name.isEmpty() || name.equals("Nhập tên VPP...")) {
                JOptionPane.showMessageDialog(panel, "Vui lòng nhập tên VPP!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double price = 0;
            int stock = 0;
            try {
                price = Double.parseDouble(priceStr);
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Giá và số lượng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (insertStationery(idField.getText(), name, price, stock, null)) {
                JOptionPane.showMessageDialog(panel, "Thêm VPP thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                idField.setText(generateStationeryId());
                nameField.setText("");
                priceField.setText("0");
                stockField.setText("1");
            } else {
                JOptionPane.showMessageDialog(panel, "Lỗi khi thêm VPP!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton clearButton = new JButton("XÓA FORM") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(55, 65, 81));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(75, 85, 99));
                } else {
                    g2d.setColor(new Color(55, 65, 81));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);

                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 3, 10, 10);

                g2d.setColor(TEXT_SECONDARY);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textX = (getWidth() - textWidth) / 2 + 10;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;

                // Trash icon
                int iconX = textX - 20;
                int iconY = textY - 10;
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(iconX, iconY, 12, 14, 2, 2);
                g2d.drawLine(iconX - 2, iconY, iconX + 14, iconY);
                g2d.drawLine(iconX + 4, iconY - 3, iconX + 8, iconY - 3);

                g2d.drawString(text, textX, textY);
                g2d.dispose();
            }
        };
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        clearButton.setForeground(TEXT_SECONDARY);
        clearButton.setPreferredSize(new Dimension(140, 48));
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> {
            idField.setText(generateStationeryId());
            nameField.setText("");
            priceField.setText("0");
            stockField.setText("1");
        });

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        formCard.add(buttonPanel, gbc);

        // Center Wrapper
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        GridBagConstraints wrapGbc = new GridBagConstraints();
        wrapGbc.fill = GridBagConstraints.HORIZONTAL;
        wrapGbc.anchor = GridBagConstraints.NORTH;
        wrapGbc.weightx = 1.0;
        wrapGbc.gridx = 0;
        wrapGbc.gridy = 0;
        wrapGbc.insets = new Insets(0, 50, 0, 50);

        centerWrapper.add(formCard, wrapGbc);
        panel.add(centerWrapper, BorderLayout.CENTER);

        return panel;
    }

    // Helper: Create dark themed label
    private JLabel createDarkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    // Helper: Create dark themed text field
    private JTextField createDarkTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(TEXT_MUTED);
                    g2d.setFont(getFont().deriveFont(Font.ITALIC));
                    g2d.drawString(placeholder, 12, getHeight() / 2 + 5);
                    g2d.dispose();
                }
            }
        };
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        field.setMinimumSize(new Dimension(200, 32));
        return field;
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
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 40, 10, 40));
        contentPanel.setBackground(new Color(15, 23, 42)); // Dark background

        // Title
        JLabel titleLabel = new JLabel("THIẾT LẬP MÃ PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);

        contentPanel.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel("Nhập PIN để bảo vệ thẻ thông minh");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitleLabel);

        contentPanel.add(Box.createVerticalStrut(15));

        // User PIN
        JLabel userPinLabel = new JLabel("PIN Người Dùng (6 số):");
        userPinLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userPinLabel.setForeground(TEXT_SECONDARY);
        userPinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(userPinLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JPasswordField userPinField = new JPasswordField();
        userPinField.setHorizontalAlignment(JPasswordField.CENTER);
        userPinField.setFont(new Font("Consolas", Font.BOLD, 16));
        userPinField.setBackground(new Color(30, 41, 59));
        userPinField.setForeground(Color.WHITE);
        userPinField.setCaretColor(SUCCESS_COLOR);
        userPinField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)));
        userPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        userPinField.setPreferredSize(new Dimension(300, 40));
        userPinField.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(userPinField);

        contentPanel.add(Box.createVerticalStrut(15));

        // Admin PIN
        JLabel adminPinLabel = new JLabel("PIN Admin (6 số):");
        adminPinLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminPinLabel.setForeground(TEXT_SECONDARY);
        adminPinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(adminPinLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JPasswordField adminPinField = new JPasswordField();
        adminPinField.setHorizontalAlignment(JPasswordField.CENTER);
        adminPinField.setFont(new Font("Consolas", Font.BOLD, 16));
        adminPinField.setBackground(new Color(30, 41, 59));
        adminPinField.setForeground(Color.WHITE);
        adminPinField.setCaretColor(SUCCESS_COLOR);
        adminPinField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)));
        adminPinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        adminPinField.setPreferredSize(new Dimension(300, 40));
        adminPinField.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(adminPinField);

        contentPanel.add(Box.createVerticalStrut(15));

        // Error label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        errorLabel.setForeground(ADMIN_COLOR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(errorLabel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final String[][] result = { { null, null } };

        JButton okButton = new JButton("XÁC NHẬN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, new Color(16, 150, 100), getWidth(), 0, SUCCESS_COLOR);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, new Color(52, 211, 153), getWidth(), 0, SUCCESS_COLOR);
                } else {
                    gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        okButton.setForeground(Color.WHITE);
        okButton.setPreferredSize(new Dimension(120, 38));
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
                    g2d.setColor(new Color(55, 65, 81));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(75, 85, 99));
                } else {
                    g2d.setColor(new Color(55, 65, 81));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2d.setColor(TEXT_SECONDARY); // Text color matches theme
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setForeground(TEXT_SECONDARY);
        cancelButton.setPreferredSize(new Dimension(100, 38));
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

    /**
     * Generate unique Stationery ID (S00001 - S99999)
     */
    private String generateStationeryId() {
        int nextNum = 1;
        String sql = "SELECT MAX(CAST(SUBSTR(ItemID, 2) AS INTEGER)) as maxNum FROM Stationery WHERE ItemID LIKE 'S%'";
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
            System.err.println("Lỗi khi sinh StationeryID: " + e.getMessage());
        }
        return "S" + String.format("%05d", nextNum);
    }

    /**
     * Insert stationery to database
     */
    private boolean insertStationery(String itemId, String name, double price, int stock, String imagePath) {
        String sql = "INSERT INTO Stationery (ItemID, Name, Price, Stock, ImagePath) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.setString(2, name);
            pstmt.setDouble(3, price);
            pstmt.setInt(4, stock);
            pstmt.setString(5, imagePath == null || imagePath.isEmpty() ? null : imagePath);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm VPP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

    /**
     * Create Manage Books Panel (Quản Lý Sách)
     */
    private JPanel createManageBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Card wrapper for glassmorphism
        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(24, 24, 27, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(new Color(63, 63, 70));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2d.dispose();
            }
        };
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("QUẢN LÝ SÁCH");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Table model
        String[] columns = { "Mã Sách", "Tên Sách", "Tác Giả", "NXB", "Giá", "SL", "Thể Loại", "Hành Động" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        // Refresh button
        JButton refreshButton = new JButton("LÀM MỚI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        };
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setPreferredSize(new Dimension(100, 36));
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadBooksToTable(tableModel));

        // Add New Book button
        JButton addNewButton = new JButton("THÊM MỚI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_GRADIENT_START, getWidth(), 0,
                        PRIMARY_GRADIENT_END);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        };
        addNewButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addNewButton.setPreferredSize(new Dimension(110, 36));
        addNewButton.setBorderPainted(false);
        addNewButton.setContentAreaFilled(false);
        addNewButton.setFocusPainted(false);
        addNewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addNewButton.addActionListener(e -> showAddBookDialog(tableModel));

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(addNewButton);
        buttonWrapper.add(refreshButton);
        headerPanel.add(buttonWrapper, BorderLayout.EAST);

        cardWrapper.add(headerPanel, BorderLayout.NORTH);

        // Table with modern styling - matching import tab
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setBackground(new Color(24, 24, 27));
        table.setForeground(new Color(250, 250, 250));
        table.setGridColor(new Color(63, 63, 70));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(99, 102, 241, 100));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Header styling - Green scheme matching import tab
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setOpaque(true);
                label.setBackground(new Color(16, 150, 100));
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(6, 78, 59)),
                        BorderFactory.createEmptyBorder(0, 5, 0, 5)));
                return label;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Action column with buttons
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(7)
                .setCellEditor(new BookActionButtonEditor(new JCheckBox(), tableModel, table));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);

        // Scroll pane with dark styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(24, 24, 27));
        scrollPane.setBackground(new Color(24, 24, 27));
        scrollPane.getVerticalScrollBar().setBackground(new Color(39, 39, 42));
        scrollPane.getHorizontalScrollBar().setBackground(new Color(39, 39, 42));
        scrollPane.getVerticalScrollBar().setUI(new ModernDarkScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernDarkScrollBarUI());

        cardWrapper.add(scrollPane, BorderLayout.CENTER);

        // Load data
        loadBooksToTable(tableModel);

        panel.add(cardWrapper, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create Manage Stationery Panel (Quản Lý VPP)
     */
    private JPanel createManageStationeryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Card wrapper for glassmorphism
        JPanel cardWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(24, 24, 27, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2d.setColor(new Color(63, 63, 70));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2d.dispose();
            }
        };
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("QUẢN LÝ VĂN PHÒNG PHẨM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Table model
        String[] columns = { "Mã VPP", "Tên VPP", "Giá", "Số Lượng", "Hành Động" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        // Refresh button
        JButton refreshButton = new JButton("LÀM MỚI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, SUCCESS_COLOR, getWidth(), 0, new Color(16, 150, 100));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        };
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshButton.setPreferredSize(new Dimension(100, 36));
        refreshButton.setBorderPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadStationeryToTable(tableModel));

        // Add New Stationery button
        JButton addNewButton = new JButton("THÊM MỚI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_GRADIENT_START, getWidth(), 0,
                        PRIMARY_GRADIENT_END);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight() - 2, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - 2 + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }
        };
        addNewButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addNewButton.setPreferredSize(new Dimension(110, 36));
        addNewButton.setBorderPainted(false);
        addNewButton.setContentAreaFilled(false);
        addNewButton.setFocusPainted(false);
        addNewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addNewButton.addActionListener(e -> showAddStationeryDialog(tableModel));

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(addNewButton);
        buttonWrapper.add(refreshButton);
        headerPanel.add(buttonWrapper, BorderLayout.EAST);

        cardWrapper.add(headerPanel, BorderLayout.NORTH);

        // Table with modern styling - matching import tab
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setBackground(new Color(24, 24, 27));
        table.setForeground(new Color(250, 250, 250));
        table.setGridColor(new Color(63, 63, 70));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(99, 102, 241, 100));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Header styling - Green scheme matching import tab
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setOpaque(true);
                label.setBackground(new Color(16, 150, 100));
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(6, 78, 59)),
                        BorderFactory.createEmptyBorder(0, 5, 0, 5)));
                return label;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));

        // Action column with buttons
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(4)
                .setCellEditor(new StationeryActionButtonEditor(new JCheckBox(), tableModel, table));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(350);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);

        // Scroll pane with dark styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(24, 24, 27));
        scrollPane.setBackground(new Color(24, 24, 27));
        scrollPane.getVerticalScrollBar().setBackground(new Color(39, 39, 42));
        scrollPane.getHorizontalScrollBar().setBackground(new Color(39, 39, 42));
        scrollPane.getVerticalScrollBar().setUI(new ModernDarkScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernDarkScrollBarUI());

        cardWrapper.add(scrollPane, BorderLayout.CENTER);

        // Load data
        loadStationeryToTable(tableModel);

        panel.add(cardWrapper, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create a gradient button helper
     */
    private JButton createGradientButton(String text, Color color1, Color color2) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient;
                if (getModel().isPressed()) {
                    gradient = new GradientPaint(0, 0, color2, getWidth(), 0, color1);
                } else if (getModel().isRollover()) {
                    gradient = new GradientPaint(0, 0, color1.brighter(), getWidth(), 0, color1);
                } else {
                    gradient = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                }

                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(110, 38));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Load books to table
     */
    private void loadBooksToTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String sql = "SELECT BookID, Title, Author, Publisher, Price, Stock, Category FROM Books ORDER BY BookID";
        try (Connection conn = DBConnect.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("BookID"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getString("Publisher"),
                        String.format("%,.0f", rs.getDouble("Price")),
                        rs.getInt("Stock"),
                        rs.getString("Category"),
                        "Sửa / Xóa"
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi load sách: " + e.getMessage());
        }
    }

    /**
     * Load stationery to table
     */
    private void loadStationeryToTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String sql = "SELECT ItemID, Name, Price, Stock FROM Stationery ORDER BY ItemID";
        try (Connection conn = DBConnect.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[] {
                        rs.getString("ItemID"),
                        rs.getString("Name"),
                        String.format("%,.0f", rs.getDouble("Price")),
                        rs.getInt("Stock"),
                        "Sửa / Xóa"
                });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi load VPP: " + e.getMessage());
        }
    }

    /**
     * Show Add Book Dialog
     */
    private void showAddBookDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm Sách Mới", true);
        dialog.setSize(500, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        contentPanel.setBackground(BACKGROUND_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Title
        JLabel dialogTitle = new JLabel("THÊM SÁCH MỚI");
        dialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dialogTitle.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(dialogTitle, gbc);

        gbc.gridwidth = 1;

        // Book ID (auto-generated, read-only)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        contentPanel.add(createDarkLabel("Mã Sách:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField bookIdField = createDarkTextField("");
        bookIdField.setText(generateBookId());
        bookIdField.setEditable(false);
        bookIdField.setBackground(new Color(39, 39, 42));
        contentPanel.add(bookIdField, gbc);

        // Title
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        contentPanel.add(createDarkLabel("Tên Sách:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField titleField = createDarkTextField("Nhập tên sách...");
        contentPanel.add(titleField, gbc);

        // Author
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(createDarkLabel("Tác Giả:"), gbc);
        gbc.gridx = 1;
        JTextField authorField = createDarkTextField("Nhập tên tác giả...");
        contentPanel.add(authorField, gbc);

        // Publisher
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(createDarkLabel("Nhà Xuất Bản:"), gbc);
        gbc.gridx = 1;
        JTextField publisherField = createDarkTextField("Nhập nhà xuất bản...");
        contentPanel.add(publisherField, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 5;
        contentPanel.add(createDarkLabel("Giá (VNĐ):"), gbc);
        gbc.gridx = 1;
        JTextField priceField = createDarkTextField("0");
        priceField.setText("0");
        contentPanel.add(priceField, gbc);

        // Stock
        gbc.gridx = 0;
        gbc.gridy = 6;
        contentPanel.add(createDarkLabel("Số Lượng:"), gbc);
        gbc.gridx = 1;
        JTextField stockField = createDarkTextField("1");
        stockField.setText("1");
        contentPanel.add(stockField, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 7;
        contentPanel.add(createDarkLabel("Thể Loại:"), gbc);
        gbc.gridx = 1;
        String[] categories = { "Văn học", "Khoa học", "Thiếu nhi", "Manga", "Self-help", "Lập trình",
                "Kinh tế", "Tâm lý", "Lịch sử", "Khác" };
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setBackground(INPUT_BG);
        categoryCombo.setForeground(TEXT_PRIMARY);
        contentPanel.add(categoryCombo, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 8, 5);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton addButton = createGradientButton("THÊM SÁCH", SUCCESS_COLOR, new Color(16, 150, 100));
        addButton.addActionListener(e -> {
            String bookId = bookIdField.getText().trim();
            String bookTitle = titleField.getText().trim();
            String author = authorField.getText().trim();
            String publisher = publisherField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();

            if (bookId.isEmpty() || bookTitle.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ Mã sách, Tên sách và Tác giả!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);

                if (insertBook(bookId, bookTitle, author, publisher, price, stock, category, "")) {
                    JOptionPane.showMessageDialog(dialog, "Thêm sách thành công!\nMã sách: " + bookId, "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadBooksToTable(tableModel);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm sách vào database!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá và Số lượng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(addButton);

        JButton cancelButton = createGradientButton("HỦY", new Color(100, 116, 139), new Color(71, 85, 105));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, gbc);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    /**
     * Show Add Stationery Dialog
     */
    private void showAddStationeryDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm VPP Mới", true);
        dialog.setSize(450, 420);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        contentPanel.setBackground(BACKGROUND_DARK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Title
        JLabel dialogTitle = new JLabel("THÊM VĂN PHÒNG PHẨM MỚI");
        dialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dialogTitle.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(dialogTitle, gbc);

        gbc.gridwidth = 1;

        // Item ID (auto-generated, read-only)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        contentPanel.add(createDarkLabel("Mã VPP:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField idField = createDarkTextField("");
        idField.setText(generateStationeryId());
        idField.setEditable(false);
        idField.setBackground(new Color(39, 39, 42));
        contentPanel.add(idField, gbc);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(createDarkLabel("Tên VPP:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = createDarkTextField("Nhập tên VPP...");
        contentPanel.add(nameField, gbc);

        // Price
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(createDarkLabel("Giá (VNĐ):"), gbc);
        gbc.gridx = 1;
        JTextField priceField = createDarkTextField("0");
        priceField.setText("0");
        contentPanel.add(priceField, gbc);

        // Stock
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(createDarkLabel("Số Lượng:"), gbc);
        gbc.gridx = 1;
        JTextField stockField = createDarkTextField("1");
        stockField.setText("1");
        contentPanel.add(stockField, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 8, 5);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton addButton = createGradientButton("THÊM VPP", SUCCESS_COLOR, new Color(16, 150, 100));
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();

            if (name.isEmpty() || name.equals("Nhập tên VPP...")) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập tên VPP!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);

                if (insertStationery(idField.getText(), name, price, stock, null)) {
                    JOptionPane.showMessageDialog(dialog, "Thêm VPP thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadStationeryToTable(tableModel);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi khi thêm VPP!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Giá và số lượng phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(addButton);

        JButton cancelButton = createGradientButton("HỦY", new Color(100, 116, 139), new Color(71, 85, 105));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, gbc);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    /**
     * Update book in database
     */
    private boolean updateBook(String bookId, String title, String author, String publisher,
            double price, int stock, String category) {
        String sql = "UPDATE Books SET Title = ?, Author = ?, Publisher = ?, Price = ?, Stock = ?, Category = ? WHERE BookID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, publisher);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, stock);
            pstmt.setString(6, category);
            pstmt.setString(7, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sách: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete book from database
     */
    private boolean deleteBook(String bookId) {
        String sql = "DELETE FROM Books WHERE BookID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sách: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update stationery in database
     */
    private boolean updateStationery(String itemId, String name, double price, int stock) {
        String sql = "UPDATE Stationery SET Name = ?, Price = ?, Stock = ? WHERE ItemID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            pstmt.setString(4, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật VPP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete stationery from database
     */
    private boolean deleteStationery(String itemId) {
        String sql = "DELETE FROM Stationery WHERE ItemID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa VPP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Show edit book dialog
     */
    private void showEditBookDialog(String bookId, DefaultTableModel tableModel) {
        // Get book details
        String sql = "SELECT * FROM Books WHERE BookID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Thông Tin Sách",
                            true);
                    dialog.setSize(500, 560);
                    dialog.setLocationRelativeTo(null);
                    dialog.setResizable(false);

                    JPanel contentPanel = new JPanel();
                    contentPanel.setLayout(new GridBagLayout());
                    contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
                    contentPanel.setBackground(BACKGROUND_DARK);

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.insets = new Insets(8, 5, 8, 5);

                    // Title
                    JLabel dialogTitle = new JLabel("SỬA THÔNG TIN SÁCH");
                    dialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    dialogTitle.setForeground(TEXT_PRIMARY);
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.gridwidth = 2;
                    contentPanel.add(dialogTitle, gbc);

                    gbc.gridwidth = 1;

                    // Book ID (read-only)
                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    gbc.weightx = 0.3;
                    contentPanel.add(createDarkLabel("Mã Sách:"), gbc);
                    gbc.gridx = 1;
                    gbc.weightx = 0.7;
                    JTextField idField = createDarkTextField("");
                    idField.setText(bookId);
                    idField.setEditable(false);
                    idField.setBackground(new Color(39, 39, 42));
                    contentPanel.add(idField, gbc);

                    // Title
                    gbc.gridx = 0;
                    gbc.gridy = 2;
                    gbc.weightx = 0.3;
                    contentPanel.add(createDarkLabel("Tên Sách:"), gbc);
                    gbc.gridx = 1;
                    gbc.weightx = 0.7;
                    JTextField titleField = createDarkTextField("");
                    titleField.setText(rs.getString("Title"));
                    contentPanel.add(titleField, gbc);

                    // Author
                    gbc.gridx = 0;
                    gbc.gridy = 3;
                    contentPanel.add(createDarkLabel("Tác Giả:"), gbc);
                    gbc.gridx = 1;
                    JTextField authorField = createDarkTextField("");
                    authorField.setText(rs.getString("Author"));
                    contentPanel.add(authorField, gbc);

                    // Publisher
                    gbc.gridx = 0;
                    gbc.gridy = 4;
                    contentPanel.add(createDarkLabel("Nhà Xuất Bản:"), gbc);
                    gbc.gridx = 1;
                    JTextField publisherField = createDarkTextField("");
                    publisherField.setText(rs.getString("Publisher"));
                    contentPanel.add(publisherField, gbc);

                    // Price
                    gbc.gridx = 0;
                    gbc.gridy = 5;
                    contentPanel.add(createDarkLabel("Giá (VNĐ):"), gbc);
                    gbc.gridx = 1;
                    JTextField priceField = createDarkTextField("");
                    priceField.setText(String.valueOf((int) rs.getDouble("Price")));
                    contentPanel.add(priceField, gbc);

                    // Stock
                    gbc.gridx = 0;
                    gbc.gridy = 6;
                    contentPanel.add(createDarkLabel("Số Lượng:"), gbc);
                    gbc.gridx = 1;
                    JTextField stockField = createDarkTextField("");
                    stockField.setText(String.valueOf(rs.getInt("Stock")));
                    contentPanel.add(stockField, gbc);

                    // Category
                    gbc.gridx = 0;
                    gbc.gridy = 7;
                    contentPanel.add(createDarkLabel("Thể Loại:"), gbc);
                    gbc.gridx = 1;
                    String[] categories = { "Văn học", "Khoa học", "Thiếu nhi", "Manga", "Self-help", "Lập trình",
                            "Kinh tế", "Tâm lý", "Lịch sử", "Khác" };
                    JComboBox<String> categoryCombo = new JComboBox<>(categories);
                    categoryCombo.setSelectedItem(rs.getString("Category"));
                    categoryCombo.setBackground(INPUT_BG);
                    categoryCombo.setForeground(TEXT_PRIMARY);
                    contentPanel.add(categoryCombo, gbc);

                    // Buttons
                    gbc.gridx = 0;
                    gbc.gridy = 8;
                    gbc.gridwidth = 2;
                    gbc.insets = new Insets(20, 5, 8, 5);
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
                    buttonPanel.setOpaque(false);

                    JButton saveButton = createGradientButton("LƯU", SUCCESS_COLOR, new Color(16, 150, 100));
                    saveButton.addActionListener(e -> {
                        try {
                            double price = Double.parseDouble(priceField.getText().trim());
                            int stock = Integer.parseInt(stockField.getText().trim());
                            if (updateBook(bookId, titleField.getText().trim(), authorField.getText().trim(),
                                    publisherField.getText().trim(), price, stock,
                                    (String) categoryCombo.getSelectedItem())) {
                                JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!", "Thành công",
                                        JOptionPane.INFORMATION_MESSAGE);
                                dialog.dispose();
                                loadBooksToTable(tableModel);
                            } else {
                                JOptionPane.showMessageDialog(dialog, "Lỗi khi cập nhật!", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(dialog, "Giá và số lượng phải là số!", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    buttonPanel.add(saveButton);

                    JButton cancelButton = createGradientButton("HỦY", new Color(100, 116, 139),
                            new Color(71, 85, 105));
                    cancelButton.addActionListener(e -> dialog.dispose());
                    buttonPanel.add(cancelButton);

                    contentPanel.add(buttonPanel, gbc);

                    dialog.add(contentPanel);
                    dialog.setVisible(true);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin sách: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show edit stationery dialog
     */
    private void showEditStationeryDialog(String itemId, DefaultTableModel tableModel) {
        String sql = "SELECT * FROM Stationery WHERE ItemID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa Thông Tin VPP",
                            true);
                    dialog.setSize(450, 420);
                    dialog.setLocationRelativeTo(null);
                    dialog.setResizable(false);

                    JPanel contentPanel = new JPanel();
                    contentPanel.setLayout(new GridBagLayout());
                    contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
                    contentPanel.setBackground(BACKGROUND_DARK);

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.insets = new Insets(8, 5, 8, 5);

                    // Title
                    JLabel dialogTitle = new JLabel("SỬA THÔNG TIN VPP");
                    dialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    dialogTitle.setForeground(TEXT_PRIMARY);
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.gridwidth = 2;
                    contentPanel.add(dialogTitle, gbc);

                    gbc.gridwidth = 1;

                    // Item ID (read-only)
                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    gbc.weightx = 0.3;
                    contentPanel.add(createDarkLabel("Mã VPP:"), gbc);
                    gbc.gridx = 1;
                    gbc.weightx = 0.7;
                    JTextField idField = createDarkTextField("");
                    idField.setText(itemId);
                    idField.setEditable(false);
                    idField.setBackground(new Color(39, 39, 42));
                    contentPanel.add(idField, gbc);

                    // Name
                    gbc.gridx = 0;
                    gbc.gridy = 2;
                    contentPanel.add(createDarkLabel("Tên VPP:"), gbc);
                    gbc.gridx = 1;
                    JTextField nameField = createDarkTextField("");
                    nameField.setText(rs.getString("Name"));
                    contentPanel.add(nameField, gbc);

                    // Price
                    gbc.gridx = 0;
                    gbc.gridy = 3;
                    contentPanel.add(createDarkLabel("Giá (VNĐ):"), gbc);
                    gbc.gridx = 1;
                    JTextField priceField = createDarkTextField("");
                    priceField.setText(String.valueOf((int) rs.getDouble("Price")));
                    contentPanel.add(priceField, gbc);

                    // Stock
                    gbc.gridx = 0;
                    gbc.gridy = 4;
                    contentPanel.add(createDarkLabel("Số Lượng:"), gbc);
                    gbc.gridx = 1;
                    JTextField stockField = createDarkTextField("");
                    stockField.setText(String.valueOf(rs.getInt("Stock")));
                    contentPanel.add(stockField, gbc);

                    // Buttons
                    gbc.gridx = 0;
                    gbc.gridy = 5;
                    gbc.gridwidth = 2;
                    gbc.insets = new Insets(20, 5, 8, 5);
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
                    buttonPanel.setOpaque(false);

                    JButton saveButton = createGradientButton("LƯU", SUCCESS_COLOR, new Color(16, 150, 100));
                    saveButton.addActionListener(e -> {
                        try {
                            double price = Double.parseDouble(priceField.getText().trim());
                            int stock = Integer.parseInt(stockField.getText().trim());
                            if (updateStationery(itemId, nameField.getText().trim(), price, stock)) {
                                JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!", "Thành công",
                                        JOptionPane.INFORMATION_MESSAGE);
                                dialog.dispose();
                                loadStationeryToTable(tableModel);
                            } else {
                                JOptionPane.showMessageDialog(dialog, "Lỗi khi cập nhật!", "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(dialog, "Giá và số lượng phải là số!", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    buttonPanel.add(saveButton);

                    JButton cancelButton = createGradientButton("HỦY", new Color(100, 116, 139),
                            new Color(71, 85, 105));
                    cancelButton.addActionListener(e -> dialog.dispose());
                    buttonPanel.add(cancelButton);

                    contentPanel.add(buttonPanel, gbc);

                    dialog.add(contentPanel);
                    dialog.setVisible(true);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy thông tin VPP: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Action Button Renderer for Edit/Delete
    class ActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton editBtn, deleteBtn;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setOpaque(true);
            setBackground(new Color(30, 41, 59));

            editBtn = new JButton("Sửa");
            editBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            editBtn.setBackground(new Color(59, 130, 246));
            editBtn.setForeground(Color.WHITE);
            editBtn.setPreferredSize(new Dimension(50, 28));
            editBtn.setBorderPainted(false);

            deleteBtn = new JButton("Xóa");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            deleteBtn.setBackground(ADMIN_COLOR);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setPreferredSize(new Dimension(50, 28));
            deleteBtn.setBorderPainted(false);

            add(editBtn);
            add(deleteBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(new Color(51, 65, 85));
            } else {
                setBackground(new Color(30, 41, 59));
            }
            return this;
        }
    }

    // Book Action Button Editor
    class BookActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editBtn, deleteBtn;
        private String bookId;
        private DefaultTableModel tableModel;
        private JTable table;

        public BookActionButtonEditor(JCheckBox checkBox, DefaultTableModel model, JTable table) {
            super(checkBox);
            this.tableModel = model;
            this.table = table;

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(30, 41, 59));

            editBtn = new JButton("Sửa");
            editBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            editBtn.setBackground(new Color(59, 130, 246));
            editBtn.setForeground(Color.WHITE);
            editBtn.setPreferredSize(new Dimension(50, 28));
            editBtn.setBorderPainted(false);
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                showEditBookDialog(bookId, tableModel);
            });

            deleteBtn = new JButton("Xóa");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            deleteBtn.setBackground(ADMIN_COLOR);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setPreferredSize(new Dimension(50, 28));
            deleteBtn.setBorderPainted(false);
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(table);
                int confirm = JOptionPane.showConfirmDialog(
                        parentFrame, "Bạn có chắc chắn muốn xóa sách này?", "Xác nhận xóa",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (deleteBook(bookId)) {
                        JOptionPane.showMessageDialog(parentFrame, "Xóa sách thành công!", "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadBooksToTable(tableModel);
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Lỗi khi xóa sách!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            panel.add(editBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            bookId = (String) table.getValueAt(row, 0);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Sửa / Xóa";
        }
    }

    // Stationery Action Button Editor
    class StationeryActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editBtn, deleteBtn;
        private String itemId;
        private DefaultTableModel tableModel;
        private JTable table;

        public StationeryActionButtonEditor(JCheckBox checkBox, DefaultTableModel model, JTable table) {
            super(checkBox);
            this.tableModel = model;
            this.table = table;

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(30, 41, 59));

            editBtn = new JButton("Sửa");
            editBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            editBtn.setBackground(new Color(59, 130, 246));
            editBtn.setForeground(Color.WHITE);
            editBtn.setPreferredSize(new Dimension(50, 28));
            editBtn.setBorderPainted(false);
            editBtn.addActionListener(e -> {
                fireEditingStopped();
                showEditStationeryDialog(itemId, tableModel);
            });

            deleteBtn = new JButton("Xóa");
            deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            deleteBtn.setBackground(ADMIN_COLOR);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setPreferredSize(new Dimension(50, 28));
            deleteBtn.setBorderPainted(false);
            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(table);
                int confirm = JOptionPane.showConfirmDialog(
                        parentFrame, "Bạn có chắc chắn muốn xóa VPP này?", "Xác nhận xóa",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (deleteStationery(itemId)) {
                        JOptionPane.showMessageDialog(parentFrame, "Xóa VPP thành công!", "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadStationeryToTable(tableModel);
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Lỗi khi xóa VPP!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            panel.add(editBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            itemId = (String) table.getValueAt(row, 0);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Sửa / Xóa";
        }
    }

    // Modern Dark ScrollBar UI for dark theme tables
    private static class ModernDarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(71, 85, 105); // Slate 600
            this.trackColor = new Color(30, 41, 59); // Slate 800
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled())
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
