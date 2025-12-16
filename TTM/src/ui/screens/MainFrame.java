/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ui.screens;

import java.sql.Connection;
import java.awt.CardLayout;
import ui.DBConnect;

/**
 *
 * @author admin
 */
public class MainFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(MainFrame.class.getName());

    private CardLayout cardLayout;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton currentActiveButton;
    private naptien topUpPanel;
    private HomePanel homePanel;
    private thongtincanhan personalPanel;
    private phihv membershipPanel;
    private doipin changePinPanel;
    private muontra borrowPanel; // Panel mượn/trả sách
    private bansach buyPanel; // Panel mua sách
    private vpp officePanel; // Panel mua VPP
    private lichsu historyPanel; // Panel lịch sử
    private String currentCardId; // CardID từ thẻ đăng nhập

    // Colors

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        // Lấy CardID từ PinLoginDialog
        this.currentCardId = PinLoginDialog.getLastAuthenticatedCardId();
        if (this.currentCardId == null || this.currentCardId.isEmpty()) {
            // Fallback nếu không có CardID (có thể là admin login)
            this.currentCardId = "CARD001";
        }
        initComponents();
        setupCardLayout();
    }

    /**
     * Setup CardLayout và thêm các panel vào
     */
    private void setupCardLayout() {
        cardLayout = new CardLayout();
        mainPanel = new javax.swing.JPanel(cardLayout);

        // Thêm các panel vào CardLayout
        homePanel = new HomePanel();
        homePanel.setCurrentCardId(currentCardId);
        mainPanel.add(homePanel, "home");

        personalPanel = new thongtincanhan();
        personalPanel.setCurrentCardId(currentCardId);
        mainPanel.add(personalPanel, "personal");

        borrowPanel = new muontra();
        borrowPanel.setCurrentCardId(currentCardId);
        mainPanel.add(borrowPanel, "borrow");

        buyPanel = new bansach();
        buyPanel.setCurrentCardId(currentCardId);
        mainPanel.add(buyPanel, "buy");

        officePanel = new vpp();
        officePanel.setCurrentCardId(currentCardId);
        mainPanel.add(officePanel, "office");

        membershipPanel = new phihv();
        membershipPanel.setCurrentCardId(currentCardId);
        mainPanel.add(membershipPanel, "membership");

        topUpPanel = new naptien();
        topUpPanel.setCurrentCardId(currentCardId);
        mainPanel.add(topUpPanel, "topup");

        historyPanel = new lichsu();
        historyPanel.setCurrentCardId(currentCardId);
        mainPanel.add(historyPanel, "history");

        // quanlithe removed

        changePinPanel = new doipin();
        // changePinPanel không có setCurrentCardId, bỏ qua
        mainPanel.add(changePinPanel, "changepin");

        // Thêm mainPanel vào content pane
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        // Hiển thị màn hình home mặc định
        showScreen("home");
    }

    /**
     * Chuyển màn hình
     * 
     * @param name Tên màn hình cần chuyển đến
     */
    public void showScreen(String name) {
        cardLayout.show(mainPanel, name);
        updateActiveButton(name);

        // Reload data when switching to specific screens
        if (name.equals("topup") && topUpPanel != null) {
            topUpPanel.reloadCardInfo();
        }
        if (name.equals("home") && homePanel != null) {
            homePanel.reloadStats();
        }
        if (name.equals("personal") && personalPanel != null) {
            personalPanel.reloadCardInfo();
        }
        if (name.equals("membership") && membershipPanel != null) {
            membershipPanel.reloadCardInfo();
        }
        if (name.equals("borrow") && borrowPanel != null) {
            // Reload dữ liệu mượn/trả sách với CardID hiện tại
            borrowPanel.setCurrentCardId(currentCardId);
        }
        if (name.equals("buy") && buyPanel != null) {
            // Reload dữ liệu mua sách với CardID hiện tại
            buyPanel.setCurrentCardId(currentCardId);
        }
        if (name.equals("office") && officePanel != null) {
            // Reload dữ liệu mua VPP với CardID hiện tại
            officePanel.setCurrentCardId(currentCardId);
        }
        if (name.equals("history") && historyPanel != null) {
            // Reload lịch sử với CardID hiện tại
            historyPanel.setCurrentCardId(currentCardId);
        }
    }

    /**
     * Cập nhật nút đang được chọn (active)
     * 
     * @param screenName Tên màn hình hiện tại
     */

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công, KHÔNG phải từ GUI Builder
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        sidebarPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        btnHome = createSidebarButton("Trang chủ", "home"); // House icon
        btnPersonal = createSidebarButton("Thông tin cá nhân", "user"); // Person icon
        btnBorrow = createSidebarButton("Thuê/ Trả", "book"); // Book icon
        btnBuy = createSidebarButton("Mua sách", "cart"); // Cart icon
        btnOffice = createSidebarButton("Mua VPP", "pen"); // Pen icon
        btnMembership = createSidebarButton("Phí hội viên", "badge"); // Badge icon
        btnTopUp = createSidebarButton("Nạp tiền", "money"); // Money icon
        btnHistory = createSidebarButton("Lịch sử", "clock"); // Clock icon
        // btnCard removed
        btnChangePin = createSidebarButton("Đổi mã PIN", "lock"); // Lock icon
        separator = new javax.swing.JSeparator();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Hệ thống quản lý nhà sách - TTM");
        setResizable(true);
        getContentPane().setLayout(new java.awt.BorderLayout());

        // Add window listener for logout
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                handleLogout();
            }
        });

        // Thiết lập Sidebar Panel - Dark Theme
        sidebarPanel.setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
        sidebarPanel.setPreferredSize(new java.awt.Dimension(280, 0)); // Slightly wider
        sidebarPanel.setLayout(new java.awt.BorderLayout(0, 0));

        // Header Panel
        javax.swing.JPanel headerPanel = new javax.swing.JPanel();
        headerPanel.setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(35, 25, 25, 25));

        // Brand logo/text
        headerLabel.setFont(new java.awt.Font("Segoe UI", 1, 26));
        headerLabel.setForeground(java.awt.Color.WHITE);
        headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        headerLabel.setText("Nhà Sách TTM");
        headerLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        headerPanel.add(headerLabel);

        headerPanel.add(javax.swing.Box.createVerticalStrut(8));

        // Welcome Label
        welcomeLabel.setFont(new java.awt.Font("Segoe UI", 0, 14));
        welcomeLabel.setForeground(new java.awt.Color(148, 163, 184)); // Slate 400
        welcomeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        welcomeLabel.setText("Xin chào bạn trở lại");
        welcomeLabel.setAlignmentX(javax.swing.JComponent.LEFT_ALIGNMENT);
        headerPanel.add(welcomeLabel);

        // Menu Panel
        javax.swing.JPanel menuPanel = new javax.swing.JPanel();
        menuPanel.setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
        menuPanel.setLayout(new java.awt.BorderLayout(0, 0));

        // Buttons Panel
        javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
        buttonsPanel.setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
        buttonsPanel.setLayout(new java.awt.BorderLayout(0, 0));
        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Setup actions and listeners for generated buttons
        configureButton(btnHome, this::btnHomeActionPerformed);
        configureButton(btnPersonal, this::btnPersonalActionPerformed);
        configureButton(btnBorrow, this::btnBorrowActionPerformed);
        configureButton(btnBuy, this::btnBuyActionPerformed);
        configureButton(btnOffice, this::btnOfficeActionPerformed);
        configureButton(btnMembership, this::btnMembershipActionPerformed);
        configureButton(btnTopUp, this::btnTopUpActionPerformed);
        configureButton(btnHistory, this::btnHistoryActionPerformed);

        configureButton(btnChangePin, this::btnChangePinActionPerformed);

        currentActiveButton = btnHome;
        // Styles are handled inside SidebarButton repaint

        // Separator
        separator.setBackground(new java.awt.Color(51, 65, 85)); // Slate 700
        separator.setForeground(new java.awt.Color(51, 65, 85));

        // Footer Actions (Logout/Exit)
        // Redesigned Logout Button to look like Sidebar Button but different color
        javax.swing.JButton btnLogout = createSidebarButton("Đăng xuất", "logout");
        btnLogout.addActionListener(this::handleLogout);

        btnExit.setText("Thoát");
        btnExit.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnExit.setForeground(new java.awt.Color(239, 68, 68)); // Red text
        btnExit.setBackground(new java.awt.Color(15, 23, 42)); // Dark bg
        btnExit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 65, 85)));
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExit.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
        btnExit.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
        btnExit.addActionListener(this::btnExitActionPerformed);
        btnExit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnExit.setBackground(new java.awt.Color(30, 41, 59));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnExit.setBackground(new java.awt.Color(15, 23, 42));
            }
        });

        // Layout Buttons
        javax.swing.Box buttonsBox = javax.swing.Box.createVerticalBox();
        buttonsBox.add(btnHome);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));
        buttonsBox.add(btnPersonal);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));
        buttonsBox.add(btnBorrow);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));
        buttonsBox.add(btnBuy);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));
        buttonsBox.add(btnOffice);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));
        buttonsBox.add(btnMembership);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));
        buttonsBox.add(btnTopUp);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));
        buttonsBox.add(btnHistory);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(8));

        buttonsBox.add(btnChangePin);

        buttonsBox.add(javax.swing.Box.createVerticalGlue());
        buttonsBox.add(separator);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(20));
        buttonsBox.add(btnLogout);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(15));
        buttonsBox.add(btnExit);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(25));

        buttonsPanel.add(buttonsBox, java.awt.BorderLayout.CENTER);
        menuPanel.add(buttonsPanel, java.awt.BorderLayout.CENTER);

        sidebarPanel.add(headerPanel, java.awt.BorderLayout.NORTH);
        sidebarPanel.add(menuPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(sidebarPanel, java.awt.BorderLayout.WEST);

        setSize(new java.awt.Dimension(1280, 720));
        setLocationRelativeTo(null);
        setBackground(new java.awt.Color(248, 250, 252)); // Main Frame Background
    }

    // --- Helper Methods ---

    /**
     * Helper to create consistent sidebar buttons with custom painting
     */
    private javax.swing.JButton createSidebarButton(String text, String iconType) {
        return new SidebarButton(text, iconType);
    }

    /**
     * Custom Button Class for Sidebar
     */
    private class SidebarButton extends javax.swing.JButton {
        private String iconType;
        private java.awt.Color hoverColor = new java.awt.Color(30, 41, 59); // Slate 800
        private java.awt.Color activeColor = new java.awt.Color(51, 65, 85); // Slate 700
        // private java.awt.Color activeColor = new java.awt.Color(37, 99, 235); // Blue
        // 600 - Alternative

        private java.awt.Color activeTextColor = java.awt.Color.WHITE;
        private java.awt.Color normalTextColor = new java.awt.Color(148, 163, 184); // Slate 400

        public SidebarButton(String text, String iconType) {
            super(text);
            this.iconType = iconType;
            setFont(new java.awt.Font("Segoe UI", 1, 14));
            setForeground(normalTextColor);
            setBackground(new java.awt.Color(15, 23, 42)); // Slate 900
            setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 55, 12, 20)); // Left padding for icon
            setFocusPainted(false);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 50));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (SidebarButton.this != currentActiveButton) {
                        setBackground(hoverColor);
                        repaint();
                    }
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (SidebarButton.this != currentActiveButton) {
                        setBackground(new java.awt.Color(15, 23, 42));
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Draw Background
            if (this == currentActiveButton) {
                // Gradient for active state
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                        0, 0, new java.awt.Color(59, 130, 246), // Blue 500
                        getWidth(), 0, new java.awt.Color(37, 99, 235) // Blue 600
                );
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 15, 15);
                setForeground(java.awt.Color.WHITE);
            } else if (getModel().isRollover()) {
                g2d.setColor(hoverColor);
                g2d.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 15, 15);
                setForeground(java.awt.Color.WHITE);
            } else {
                setForeground(normalTextColor);
            }

            // 2. Draw Icon (Simple Vector Graphics)
            int iconX = 20;
            int iconY = getHeight() / 2;
            g2d.setStroke(new java.awt.BasicStroke(2f));
            g2d.setColor(getForeground());

            drawIcon(g2d, iconType, iconX, iconY);

            // 3. Draw Text
            super.paintComponent(g);

            g2d.dispose();
        }

        private void drawIcon(java.awt.Graphics2D g2, String type, int x, int y) {
            // Increase base scale for icons (approx 18x18px)
            switch (type) {
                case "home":
                    // House - Modern Outline
                    int[] hx = { x - 9, x, x + 9 };
                    int[] hy = { y - 4, y - 12, y - 4 };
                    g2.drawPolyline(hx, hy, 3); // Roof
                    g2.drawRect(x - 7, y - 4, 14, 11); // Body
                    g2.drawRect(x - 2, y + 1, 4, 6); // Door
                    break;
                case "user":
                    // User - Rounder shoulders
                    g2.drawOval(x - 4, y - 10, 8, 8); // Head
                    g2.drawArc(x - 9, y - 1, 18, 14, 0, 180); // Shoulders
                    break;
                case "book":
                    // Book - Open book style
                    g2.drawRoundRect(x - 9, y - 8, 7, 16, 3, 3); // Left page
                    g2.drawRoundRect(x, y - 8, 7, 16, 3, 3); // Right page
                    g2.drawLine(x - 6, y - 2, x - 3, y - 2); // Text lines
                    g2.drawLine(x - 6, y + 2, x - 3, y + 2);
                    g2.drawLine(x + 3, y - 2, x + 6, y - 2);
                    g2.drawLine(x + 3, y + 2, x + 6, y + 2);
                    break;
                case "cart":
                    // Shopping Cart
                    g2.drawPolyline(new int[] { x - 10, x - 7, x - 5, x + 7, x + 9, x + 6 },
                            new int[] { y - 7, y - 7, y + 4, y + 4, y - 7, y - 7 }, 6); // Basket
                    g2.drawOval(x - 4, y + 6, 3, 3); // Wheel 1
                    g2.drawOval(x + 3, y + 6, 3, 3); // Wheel 2
                    break;
                case "pen":
                    // Pen/Pencil - Diagonally
                    java.awt.Shape oldClip = g2.getClip();
                    g2.rotate(Math.toRadians(45), x, y);
                    g2.drawRoundRect(x - 2, y - 8, 4, 14, 2, 2); // Body
                    g2.drawPolygon(new int[] { x - 2, x + 2, x }, new int[] { y + 6, y + 6, y + 9 }, 3); // Tip
                    g2.rotate(Math.toRadians(-45), x, y);
                    break;
                case "badge":
                    // Membership Badge
                    g2.drawOval(x - 6, y - 8, 12, 12); // Circle
                    g2.drawPolyline(new int[] { x - 3, x - 3, x, x + 3, x + 3 },
                            new int[] { y + 3, y + 9, y + 7, y + 9, y + 3 }, 5); // Ribbon
                    break;
                case "money":
                    // Banknote/Money
                    g2.drawRoundRect(x - 10, y - 6, 20, 12, 3, 3);
                    g2.drawOval(x - 3, y - 3, 6, 6);
                    g2.drawLine(x - 7, y, x - 5, y);
                    g2.drawLine(x + 5, y, x + 7, y);
                    break;
                case "clock":
                    // Clock
                    g2.drawOval(x - 8, y - 8, 16, 16);
                    g2.drawLine(x, y, x, y - 5); // Hour hand
                    g2.drawLine(x, y, x + 4, y + 2); // Minute hand
                    break;
                case "card":
                    // ID Card
                    g2.drawRoundRect(x - 10, y - 7, 20, 14, 2, 2); // Card
                    g2.drawRect(x - 7, y - 3, 5, 4); // Chip
                    g2.drawLine(x, y - 3, x + 7, y - 3); // Lines
                    g2.drawLine(x, y, x + 5, y);
                    break;
                case "lock":
                    // Lock
                    g2.drawRoundRect(x - 7, y - 2, 14, 10, 2, 2); // Body
                    g2.drawArc(x - 5, y - 9, 10, 14, 0, 180); // Shackle
                    g2.fillOval(x - 1, y + 2, 2, 2); // Keyhole
                    break;
                case "logout":
                    // Logout - Arrow exiting door
                    g2.drawLine(x, y - 6, x, y + 6); // Door line
                    g2.drawLine(x, y - 6, x + 6, y - 6); // Top
                    g2.drawLine(x + 6, y - 6, x + 6, y + 6); // Right
                    g2.drawLine(x + 6, y + 6, x, y + 6); // Bottom

                    g2.drawLine(x - 8, y, x - 2, y); // Arrow shaft
                    g2.drawLine(x - 4, y - 2, x - 2, y); // Arrow head top
                    g2.drawLine(x - 4, y + 2, x - 2, y); // Arrow head bot
                    break;
                default:
                    g2.drawOval(x - 5, y - 5, 10, 10);
            }
        }
    }

    // Updated configureButton method -> No longer needed as SidebarButton handles
    // it internally
    // We will just use updated updateActiveButton logic

    /**
     * Helper to configure button actions
     */
    private void configureButton(javax.swing.JButton btn, java.awt.event.ActionListener action) {
        btn.addActionListener(action);
    }

    private javax.swing.JButton createFooterButton(String text, java.awt.Color fgColor) {
        javax.swing.JButton btn = new javax.swing.JButton(text);
        btn.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btn.setForeground(fgColor);
        btn.setBackground(new java.awt.Color(255, 255, 255));
        btn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240))); // Slate 200
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new java.awt.Color(248, 250, 252));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new java.awt.Color(255, 255, 255));
            }
        });
        return btn;
    }

    // New updateActiveButton method for light theme
    private void updateActiveButton(String screenName) {

        switch (screenName) {
            case "home":
                currentActiveButton = btnHome;
                break;
            case "personal":
                currentActiveButton = btnPersonal;
                break;
            case "borrow":
                currentActiveButton = btnBorrow;
                break;
            case "buy":
                currentActiveButton = btnBuy;
                break;
            case "office":
                currentActiveButton = btnOffice;
                break;
            case "membership":
                currentActiveButton = btnMembership;
                break;
            case "topup":
                currentActiveButton = btnTopUp;
                break;
            case "history":
                currentActiveButton = btnHistory;
                break;
            // card case removed
            case "changepin":
                currentActiveButton = btnChangePin;
                break;
            default:
                currentActiveButton = null;
        }

        repaint(); // Re-paint to show active state
    }

    // updateButtonStyles is no longer needed as SidebarButton handles state via
    // paintComponent

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("home");
    }

    private void btnPersonalActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPersonalActionPerformed
        showScreen("personal");
    }// GEN-LAST:event_btnPersonalActionPerformed

    private void btnBorrowActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnBorrowActionPerformed
        showScreen("borrow");
    }// GEN-LAST:event_btnBorrowActionPerformed

    private void btnBuyActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnBuyActionPerformed
        showScreen("buy");
    }// GEN-LAST:event_btnBuyActionPerformed

    private void btnTopUpActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnTopUpActionPerformed
        showScreen("topup");
    }// GEN-LAST:event_btnTopUpActionPerformed

    private void btnHistoryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHistoryActionPerformed
        showScreen("history");
    }// GEN-LAST:event_btnHistoryActionPerformed

    // btnCardActionPerformed removed

    private void btnChangePinActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("changepin");
    }

    private void btnOfficeActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("office");
    }

    private void btnMembershipActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("membership");
    }

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnExitActionPerformed
        int option = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn thoát?",
                "Xác nhận",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (option == javax.swing.JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }// GEN-LAST:event_btnExitActionPerformed

    private void handleLogout(java.awt.event.ActionEvent evt) {
        int option = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Ban co chac chan muon dang xuat?",
                "Xac nhan",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (option == javax.swing.JOptionPane.YES_OPTION) {
            // Close this window and return to login screen
            this.dispose();
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

    private void handleLogout() {
        int option = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Ban co chac chan muon dang xuat?",
                "Xac nhan",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (option == javax.swing.JOptionPane.YES_OPTION) {
            // Close this window and return to login screen
            this.dispose();
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
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the card connection panel first */
        java.awt.EventQueue.invokeLater(() -> {
            new CardConnectionPanel().setVisible(true);
        });
    }

    // Variables declaration
    private javax.swing.JButton btnBorrow;
    private javax.swing.JButton btnBuy;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnHistory;
    // btnCard removed
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnMembership;
    private javax.swing.JButton btnOffice;
    private javax.swing.JButton btnPersonal;
    private javax.swing.JButton btnTopUp;
    private javax.swing.JButton btnChangePin;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel welcomeLabel;
}
