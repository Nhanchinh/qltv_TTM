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
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());
    
    private CardLayout cardLayout;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton currentActiveButton;
    private naptien topUpPanel;
    private HomePanel homePanel;
    private thongtincanhan personalPanel;
    private phihv membershipPanel;
    
    // Colors
    private static final java.awt.Color ACTIVE_COLOR = new java.awt.Color(0, 120, 215);
    private static final java.awt.Color INACTIVE_COLOR = new java.awt.Color(60, 60, 65);
    private static final java.awt.Color HOVER_COLOR = new java.awt.Color(0, 100, 180);

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
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
        mainPanel.add(homePanel, "home");
        personalPanel = new thongtincanhan();
        mainPanel.add(personalPanel, "personal");
        mainPanel.add(new muontra(), "borrow");
        mainPanel.add(new bansach(), "buy");
        mainPanel.add(new vpp(), "office");
        membershipPanel = new phihv();
        mainPanel.add(membershipPanel, "membership");
        topUpPanel = new naptien();
        mainPanel.add(topUpPanel, "topup");
        mainPanel.add(new lichsu(), "history");
        mainPanel.add(new quanlithe(), "card");
        
        // Thêm mainPanel vào content pane
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
        
        // Hiển thị màn hình home mặc định
        showScreen("home");
    }
    
    /**
     * Chuyển màn hình
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
    }
    
    /**
     * Cập nhật nút đang được chọn (active)
     * @param screenName Tên màn hình hiện tại
     */
    private void updateActiveButton(String screenName) {
        // Reset tất cả các nút về màu inactive
        resetAllButtons();
        
        // Set nút tương ứng với màn hình hiện tại thành active
        switch (screenName) {
            case "home":
                setButtonActive(btnHome);
                break;
            case "personal":
                setButtonActive(btnPersonal);
                break;
            case "borrow":
                setButtonActive(btnBorrow);
                break;
            case "buy":
                setButtonActive(btnBuy);
                break;
            case "office":
                setButtonActive(btnOffice);
                break;
            case "membership":
                setButtonActive(btnMembership);
                break;
            case "topup":
                setButtonActive(btnTopUp);
                break;
            case "history":
                setButtonActive(btnHistory);
                break;
            case "card":
                setButtonActive(btnCard);
                break;
        }
    }
    
    /**
     * Set nút thành active (màu xanh)
     */
    private void setButtonActive(javax.swing.JButton button) {
        if (button != null) {
            button.setBackground(ACTIVE_COLOR);
            currentActiveButton = button;
        }
    }
    
    /**
     * Reset tất cả các nút về inactive (màu xám)
     */
    private void resetAllButtons() {
        btnHome.setBackground(INACTIVE_COLOR);
        btnPersonal.setBackground(INACTIVE_COLOR);
        btnBorrow.setBackground(INACTIVE_COLOR);
        btnBuy.setBackground(INACTIVE_COLOR);
        btnOffice.setBackground(INACTIVE_COLOR);
        btnMembership.setBackground(INACTIVE_COLOR);
        btnTopUp.setBackground(INACTIVE_COLOR);
        btnHistory.setBackground(INACTIVE_COLOR);
        btnCard.setBackground(INACTIVE_COLOR);
    }
    
    /**
     * Tạo MouseAdapter cho button với hiệu ứng hover
     */
    private java.awt.event.MouseAdapter createButtonHoverAdapter(javax.swing.JButton button) {
        return new java.awt.event.MouseAdapter() {
            private java.awt.Color savedColor;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Chi hover neu khong phai nut dang active
                if (button != currentActiveButton) {
                    savedColor = button.getBackground();
                    button.setBackground(ACTIVE_COLOR);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Giu mau xanh neu dang active, khong thi ve mau xam
                if (button != currentActiveButton && savedColor != null) {
                    button.setBackground(savedColor);
                }
            }
        };
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công, KHÔNG phải từ GUI Builder
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        sidebarPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        welcomeLabel = new javax.swing.JLabel();
        btnHome = new javax.swing.JButton();
        btnPersonal = new javax.swing.JButton();
        btnBorrow = new javax.swing.JButton();
        btnBuy = new javax.swing.JButton();
        btnOffice = new javax.swing.JButton();
        btnMembership = new javax.swing.JButton();
        btnTopUp = new javax.swing.JButton();
        btnHistory = new javax.swing.JButton();
        btnCard = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hệ thống quản lý thư viện - TTM");
        setResizable(true);
        getContentPane().setLayout(new java.awt.BorderLayout());

        // Thiết lập Sidebar Panel
        sidebarPanel.setBackground(new java.awt.Color(45, 45, 48));
        sidebarPanel.setPreferredSize(new java.awt.Dimension(250, 0));
        sidebarPanel.setLayout(new java.awt.BorderLayout(0, 0));

        // Header Panel
        javax.swing.JPanel headerPanel = new javax.swing.JPanel();
        headerPanel.setBackground(new java.awt.Color(0, 120, 215));
        headerPanel.setLayout(new java.awt.BorderLayout());
        
        headerLabel.setFont(new java.awt.Font("Segoe UI", 1, 20));
        headerLabel.setForeground(new java.awt.Color(255, 255, 255));
        headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        headerLabel.setText("THƯ VIỆN TTM");
        headerLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 20, 10));
        headerPanel.add(headerLabel, java.awt.BorderLayout.CENTER);

        // Menu Panel
        javax.swing.JPanel menuPanel = new javax.swing.JPanel();
        menuPanel.setBackground(new java.awt.Color(45, 45, 48));
        menuPanel.setLayout(new java.awt.BorderLayout(0, 10));
        
        // Welcome Label
        welcomeLabel.setFont(new java.awt.Font("Segoe UI", 2, 12));
        welcomeLabel.setForeground(new java.awt.Color(200, 200, 200));
        welcomeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        welcomeLabel.setText("Chào mừng bạn quay trở lại");
        welcomeLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 10, 10, 10));
        menuPanel.add(welcomeLabel, java.awt.BorderLayout.NORTH);

        // Buttons Panel
        javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
        buttonsPanel.setBackground(new java.awt.Color(45, 45, 48));
        buttonsPanel.setLayout(new java.awt.BorderLayout(0, 8));
        buttonsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Button Home (Trang chủ)
        btnHome.setBackground(ACTIVE_COLOR);
        btnHome.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnHome.setForeground(new java.awt.Color(255, 255, 255));
        btnHome.setText("Trang chủ");
        btnHome.setBorderPainted(false);
        btnHome.setFocusPainted(false);
        btnHome.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnHome.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHome.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHome.addActionListener(this::btnHomeActionPerformed);
        btnHome.addMouseListener(createButtonHoverAdapter(btnHome));
        currentActiveButton = btnHome;

        // Button Personal Info
        btnPersonal.setBackground(INACTIVE_COLOR);
        btnPersonal.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnPersonal.setForeground(new java.awt.Color(255, 255, 255));
        btnPersonal.setText("Thông tin cá nhân");
        btnPersonal.setBorderPainted(false);
        btnPersonal.setFocusPainted(false);
        btnPersonal.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnPersonal.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnPersonal.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnPersonal.addActionListener(this::btnPersonalActionPerformed);
        btnPersonal.addMouseListener(createButtonHoverAdapter(btnPersonal));

        // Button Borrow/Return
        btnBorrow.setBackground(INACTIVE_COLOR);
        btnBorrow.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnBorrow.setForeground(new java.awt.Color(255, 255, 255));
        btnBorrow.setText("Mượn/ Trả");
        btnBorrow.setBorderPainted(false);
        btnBorrow.setFocusPainted(false);
        btnBorrow.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnBorrow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBorrow.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBorrow.addActionListener(this::btnBorrowActionPerformed);
        btnBorrow.addMouseListener(createButtonHoverAdapter(btnBorrow));

        // Button Buy Books
        btnBuy.setBackground(INACTIVE_COLOR);
        btnBuy.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnBuy.setForeground(new java.awt.Color(255, 255, 255));
        btnBuy.setText("Mua sách");
        btnBuy.setBorderPainted(false);
        btnBuy.setFocusPainted(false);
        btnBuy.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnBuy.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBuy.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBuy.addActionListener(this::btnBuyActionPerformed);
        btnBuy.addMouseListener(createButtonHoverAdapter(btnBuy));

        // Button Buy Office Supplies
        btnOffice.setBackground(INACTIVE_COLOR);
        btnOffice.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnOffice.setForeground(new java.awt.Color(255, 255, 255));
        btnOffice.setText("Mua VPP");
        btnOffice.setBorderPainted(false);
        btnOffice.setFocusPainted(false);
        btnOffice.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnOffice.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnOffice.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnOffice.addActionListener(this::btnOfficeActionPerformed);
        btnOffice.addMouseListener(createButtonHoverAdapter(btnOffice));

        // Button Membership Fee
        btnMembership.setBackground(INACTIVE_COLOR);
        btnMembership.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnMembership.setForeground(new java.awt.Color(255, 255, 255));
        btnMembership.setText("Phí hội viên");
        btnMembership.setBorderPainted(false);
        btnMembership.setFocusPainted(false);
        btnMembership.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnMembership.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnMembership.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnMembership.addActionListener(this::btnMembershipActionPerformed);
        btnMembership.addMouseListener(createButtonHoverAdapter(btnMembership));

        // Button Top Up
        btnTopUp.setBackground(INACTIVE_COLOR);
        btnTopUp.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnTopUp.setForeground(new java.awt.Color(255, 255, 255));
        btnTopUp.setText("Nạp tiền");
        btnTopUp.setBorderPainted(false);
        btnTopUp.setFocusPainted(false);
        btnTopUp.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnTopUp.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnTopUp.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnTopUp.addActionListener(this::btnTopUpActionPerformed);
        btnTopUp.addMouseListener(createButtonHoverAdapter(btnTopUp));

        // Button History
        btnHistory.setBackground(INACTIVE_COLOR);
        btnHistory.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnHistory.setForeground(new java.awt.Color(255, 255, 255));
        btnHistory.setText("Lịch sử");
        btnHistory.setBorderPainted(false);
        btnHistory.setFocusPainted(false);
        btnHistory.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnHistory.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHistory.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHistory.addActionListener(this::btnHistoryActionPerformed);
        btnHistory.addMouseListener(createButtonHoverAdapter(btnHistory));

        // Button Smart Card
        btnCard.setBackground(INACTIVE_COLOR);
        btnCard.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnCard.setForeground(new java.awt.Color(255, 255, 255));
        btnCard.setText("Quản lý thẻ");
        btnCard.setBorderPainted(false);
        btnCard.setFocusPainted(false);
        btnCard.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnCard.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnCard.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnCard.addActionListener(this::btnCardActionPerformed);
        btnCard.addMouseListener(createButtonHoverAdapter(btnCard));

        // Separator
        separator.setBackground(new java.awt.Color(100, 100, 100));
        separator.setForeground(new java.awt.Color(100, 100, 100));

        // Button Exit
        btnExit.setBackground(new java.awt.Color(200, 50, 50));
        btnExit.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnExit.setForeground(new java.awt.Color(255, 255, 255));
        btnExit.setText("Thoát");
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnExit.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnExit.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnExit.addActionListener(this::btnExitActionPerformed);
        // Button Exit - hover effect riêng
        btnExit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnExit.setBackground(new java.awt.Color(220, 70, 70));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnExit.setBackground(new java.awt.Color(200, 50, 50));
            }
        });

        // Add buttons to panel
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
        buttonsBox.add(btnCard);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(15));
        buttonsBox.add(separator);
        buttonsBox.add(javax.swing.Box.createVerticalStrut(15));
        buttonsBox.add(btnExit);
        buttonsBox.add(javax.swing.Box.createVerticalGlue());
        
        buttonsPanel.add(buttonsBox, java.awt.BorderLayout.CENTER);
        menuPanel.add(buttonsPanel, java.awt.BorderLayout.CENTER);

        sidebarPanel.add(headerPanel, java.awt.BorderLayout.NORTH);
        sidebarPanel.add(menuPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(sidebarPanel, java.awt.BorderLayout.WEST);

        setSize(new java.awt.Dimension(1200, 700));
        setLocationRelativeTo(null);
    }

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("home");
    }

    private void btnPersonalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPersonalActionPerformed
        showScreen("personal");
    }//GEN-LAST:event_btnPersonalActionPerformed

    private void btnBorrowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBorrowActionPerformed
        showScreen("borrow");
    }//GEN-LAST:event_btnBorrowActionPerformed

    private void btnBuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuyActionPerformed
        showScreen("buy");
    }//GEN-LAST:event_btnBuyActionPerformed

    private void btnTopUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTopUpActionPerformed
        showScreen("topup");
    }//GEN-LAST:event_btnTopUpActionPerformed

    private void btnHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistoryActionPerformed
        showScreen("history");
    }//GEN-LAST:event_btnHistoryActionPerformed

    private void btnCardActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("card");
    }

    private void btnOfficeActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("office");
    }

    private void btnMembershipActionPerformed(java.awt.event.ActionEvent evt) {
        showScreen("membership");
    }

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        int option = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn thoát?",
            "Xác nhận",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE
        );
        if (option == javax.swing.JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_btnExitActionPerformed


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
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
        //</editor-fold>
        
        // Show PIN login dialog first
        boolean authenticated = PinLoginDialog.showPinDialog(null);
        
        if (!authenticated) {
            // User cancelled or PIN was wrong - exit application
            System.exit(0);
            return;
        }
        
        // Check database connection
        Connection conn = (Connection) DBConnect.getConnection();
        if (conn == null) {
            System.err.println("Ket noi DB loi!");
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Loi ket noi database!\nVui long kiem tra SQLite JDBC driver da duoc them vao project chua.",
                "Loi ket noi", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // Variables declaration
    private javax.swing.JButton btnBorrow;
    private javax.swing.JButton btnBuy;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnHistory;
    private javax.swing.JButton btnCard;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnMembership;
    private javax.swing.JButton btnOffice;
    private javax.swing.JButton btnPersonal;
    private javax.swing.JButton btnTopUp;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel welcomeLabel;
}
