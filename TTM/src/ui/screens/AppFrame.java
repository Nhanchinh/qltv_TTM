package ui.screens;

import java.awt.BorderLayout;
import java.sql.Connection;
import javax.swing.JFrame;
import ui.DBConnect;

/**
 * Application main frame - handles both User and Admin modes
 */
public class AppFrame extends JFrame {
    
    private int loginMode; // 1 = User, 2 = Admin
    
    public AppFrame(int loginMode) {
        this.loginMode = loginMode;
        initFrame();
        setupContent();
    }
    
    private void initFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Hệ thống quản lý nhà sách - TTM");
        setResizable(true);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }
    
    private void setupContent() {
        if (loginMode == 1) {
            // User mode - show normal UI
            setupUserMode();
        } else if (loginMode == 2) {
            // Admin mode - show admin panel
            setupAdminMode();
        }
    }
    
    private void setupUserMode() {
        // Create and show MainFrame with user interface
        MainFrame mainFrame = new MainFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        
        // Close this frame
        this.dispose();
    }
    
    private void setupAdminMode() {
        AdminPanel adminPanel = new AdminPanel();
        getContentPane().add(adminPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }
}

