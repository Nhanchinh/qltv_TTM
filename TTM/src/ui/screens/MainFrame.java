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
        mainPanel.add(new HomePanel(), "home");
        mainPanel.add(new thongtincanhan(), "personal");
        mainPanel.add(new muontra(), "borrow");
        mainPanel.add(new bansach(), "buy");
        mainPanel.add(new vpp(), "office");
        mainPanel.add(new phihv(), "membership");
        mainPanel.add(new naptien(), "topup");
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
    }
    
    /**
     * Tạo MouseAdapter cho button với hiệu ứng hover
     */
    private java.awt.event.MouseAdapter createButtonHoverAdapter(
            javax.swing.JButton button, 
            java.awt.Color normalColor, 
            java.awt.Color hoverColor) {
        return new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
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
        btnHome.setBackground(new java.awt.Color(0, 120, 215));
        btnHome.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnHome.setForeground(new java.awt.Color(255, 255, 255));
        btnHome.setText("Trang chủ");
        btnHome.setBorderPainted(false);
        btnHome.setFocusPainted(false);
        btnHome.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnHome.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHome.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHome.addActionListener(this::btnHomeActionPerformed);
        btnHome.addMouseListener(createButtonHoverAdapter(
            btnHome, 
            new java.awt.Color(0, 120, 215), 
            new java.awt.Color(0, 100, 180)
        ));

        // Button Personal Info
        btnPersonal.setBackground(new java.awt.Color(60, 60, 65));
        btnPersonal.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnPersonal.setForeground(new java.awt.Color(255, 255, 255));
        btnPersonal.setText("Thông tin cá nhân");
        btnPersonal.setBorderPainted(false);
        btnPersonal.setFocusPainted(false);
        btnPersonal.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnPersonal.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnPersonal.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnPersonal.addActionListener(this::btnPersonalActionPerformed);
        btnPersonal.addMouseListener(createButtonHoverAdapter(
            btnPersonal, 
            new java.awt.Color(60, 60, 65), 
            new java.awt.Color(0, 120, 215)
        ));

        // Button Borrow/Return
        btnBorrow.setBackground(new java.awt.Color(60, 60, 65));
        btnBorrow.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnBorrow.setForeground(new java.awt.Color(255, 255, 255));
        btnBorrow.setText("Mượn/ Trả");
        btnBorrow.setBorderPainted(false);
        btnBorrow.setFocusPainted(false);
        btnBorrow.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnBorrow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBorrow.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBorrow.addActionListener(this::btnBorrowActionPerformed);
        btnBorrow.addMouseListener(createButtonHoverAdapter(
            btnBorrow, 
            new java.awt.Color(60, 60, 65), 
            new java.awt.Color(0, 120, 215)
        ));

        // Button Buy Books
        btnBuy.setBackground(new java.awt.Color(60, 60, 65));
        btnBuy.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnBuy.setForeground(new java.awt.Color(255, 255, 255));
        btnBuy.setText("Mua sách");
        btnBuy.setBorderPainted(false);
        btnBuy.setFocusPainted(false);
        btnBuy.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnBuy.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBuy.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnBuy.addActionListener(this::btnBuyActionPerformed);
        btnBuy.addMouseListener(createButtonHoverAdapter(
            btnBuy, 
            new java.awt.Color(60, 60, 65), 
            new java.awt.Color(0, 120, 215)
        ));

        // Button Buy Office Supplies
        btnOffice.setBackground(new java.awt.Color(60, 60, 65));
        btnOffice.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnOffice.setForeground(new java.awt.Color(255, 255, 255));
        btnOffice.setText("Mua VPP");
        btnOffice.setBorderPainted(false);
        btnOffice.setFocusPainted(false);
        btnOffice.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnOffice.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnOffice.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnOffice.addActionListener(this::btnOfficeActionPerformed);
        btnOffice.addMouseListener(createButtonHoverAdapter(
            btnOffice, 
            new java.awt.Color(60, 60, 65), 
            new java.awt.Color(0, 120, 215)
        ));

        // Button Membership Fee
        btnMembership.setBackground(new java.awt.Color(60, 60, 65));
        btnMembership.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnMembership.setForeground(new java.awt.Color(255, 255, 255));
        btnMembership.setText("Phí hội viên");
        btnMembership.setBorderPainted(false);
        btnMembership.setFocusPainted(false);
        btnMembership.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnMembership.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnMembership.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnMembership.addActionListener(this::btnMembershipActionPerformed);
        btnMembership.addMouseListener(createButtonHoverAdapter(
            btnMembership, 
            new java.awt.Color(60, 60, 65), 
            new java.awt.Color(0, 120, 215)
        ));

        // Button Top Up
        btnTopUp.setBackground(new java.awt.Color(60, 60, 65));
        btnTopUp.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnTopUp.setForeground(new java.awt.Color(255, 255, 255));
        btnTopUp.setText("Nạp tiền");
        btnTopUp.setBorderPainted(false);
        btnTopUp.setFocusPainted(false);
        btnTopUp.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnTopUp.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnTopUp.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnTopUp.addActionListener(this::btnTopUpActionPerformed);
        btnTopUp.addMouseListener(createButtonHoverAdapter(
            btnTopUp, 
            new java.awt.Color(60, 60, 65), 
            new java.awt.Color(0, 120, 215)
        ));

        // Button History
        btnHistory.setBackground(new java.awt.Color(60, 60, 65));
        btnHistory.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnHistory.setForeground(new java.awt.Color(255, 255, 255));
        btnHistory.setText("Lịch sử");
        btnHistory.setBorderPainted(false);
        btnHistory.setFocusPainted(false);
        btnHistory.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        btnHistory.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHistory.setPreferredSize(new java.awt.Dimension(Integer.MAX_VALUE, 45));
        btnHistory.addActionListener(this::btnHistoryActionPerformed);
        btnHistory.addMouseListener(createButtonHoverAdapter(
            btnHistory, 
            new java.awt.Color(60, 60, 65), 
            new java.awt.Color(0, 120, 215)
        ));

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
        btnExit.addMouseListener(createButtonHoverAdapter(
            btnExit, 
            new java.awt.Color(200, 50, 50), 
            new java.awt.Color(220, 70, 70)
        ));

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
        Connection conn = (Connection) DBConnect.getConnection();
        if (conn != null) {
            System.out.println("Kết nối DB OK!");
        } else {
            System.out.println("Kết nối DB lỗi!");
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }

    // Variables declaration
    private javax.swing.JButton btnBorrow;
    private javax.swing.JButton btnBuy;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnHistory;
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
