/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import smartcard.CardConnectionManager;
import smartcard.CardVerifyManager;
import smartcard.CardSetupManager;
import javax.swing.JButton;
import javax.swing.JPasswordField;

/**
 * Modern Change PIN Screen
 */
public class doipin extends javax.swing.JPanel {

    private CardConnectionManager connManager;

    // UI Components
    private JPasswordField oldPinField;
    private JPasswordField newPinField;
    private JPasswordField confirmPinField;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton changeButton;

    public doipin() {
        initComponents();
    }

    private void initComponents() {
        setBackground(new java.awt.Color(248, 250, 252)); // Slate 50
        setLayout(new java.awt.BorderLayout(0, 0));

        // 1. Header
        add(createHeaderPanel(), java.awt.BorderLayout.NORTH);

        // 2. Content
        javax.swing.JPanel contentPanel = new javax.swing.JPanel(new java.awt.GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(40, 40, 40, 40));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // LEFT: Form (50%)
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new java.awt.Insets(0, 0, 0, 20);
        contentPanel.add(createFormPanel(), gbc);

        // RIGHT: Security Info (50%)
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new java.awt.Insets(0, 20, 0, 0);
        contentPanel.add(createSecurityPanel(), gbc);

        add(contentPanel, java.awt.BorderLayout.CENTER);
    }

    // --- UI Creators ---

    private javax.swing.JPanel createHeaderPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));
        p.setPreferredSize(new java.awt.Dimension(0, 80));

        javax.swing.JLabel title = new javax.swing.JLabel("Đổi Mã PIN");
        title.setFont(new java.awt.Font("Segoe UI", 1, 28));
        title.setForeground(new java.awt.Color(15, 23, 42));
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 30, 0, 0));

        p.add(title, java.awt.BorderLayout.WEST);
        return p;
    }

    private javax.swing.JPanel createFormPanel() {
        javax.swing.JPanel p = createPanelWithShadow();
        p.setLayout(new java.awt.GridBagLayout());
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 30, 40));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(0, 0, 15, 0);
        gbc.gridx = 0;

        // Title
        javax.swing.JLabel title = new javax.swing.JLabel("Thiết lập mã PIN mới");
        title.setFont(new java.awt.Font("Segoe UI", 1, 20));
        title.setForeground(new java.awt.Color(30, 41, 59));
        gbc.insets = new java.awt.Insets(0, 0, 30, 0);
        p.add(title, gbc);

        // Old PIN
        addFormField(p, gbc, 1, "Mã PIN hiện tại", oldPinField = createStyledPasswordField());

        // New PIN
        addFormField(p, gbc, 3, "Mã PIN mới (6 số)", newPinField = createStyledPasswordField());

        // Confirm PIN
        addFormField(p, gbc, 5, "Xác nhận mã PIN mới", confirmPinField = createStyledPasswordField());

        // Status
        gbc.gridy = 7;
        gbc.insets = new java.awt.Insets(10, 0, 10, 0);
        statusLabel = new javax.swing.JLabel(" ");
        statusLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));
        statusLabel.setForeground(new java.awt.Color(220, 38, 38)); // Red
        p.add(statusLabel, gbc);

        // Button
        gbc.gridy = 8;
        gbc.insets = new java.awt.Insets(20, 0, 0, 0);
        changeButton = createModernButton("Cập nhật mã PIN", new java.awt.Color(37, 99, 235));
        changeButton.addActionListener(this::changeButtonActionPerformed);
        p.add(changeButton, gbc);

        // Spacer to push up
        gbc.gridy = 9;
        gbc.weighty = 1.0;
        p.add(new javax.swing.JLabel(), gbc);

        return p;
    }

    private void addFormField(javax.swing.JPanel p, java.awt.GridBagConstraints gbc, int row, String label,
            javax.swing.JComponent field) {
        gbc.gridy = row;
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        javax.swing.JLabel l = new javax.swing.JLabel(label);
        l.setFont(new java.awt.Font("Segoe UI", 1, 14));
        l.setForeground(new java.awt.Color(100, 116, 139));
        p.add(l, gbc);

        gbc.gridy = row + 1;
        gbc.insets = new java.awt.Insets(0, 0, 20, 0);
        p.add(field, gbc);
    }

    private javax.swing.JPanel createSecurityPanel() {
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(0, 20));
        p.setOpaque(false);

        // Visual Card
        javax.swing.JPanel securityCard = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient (Green to Emerald)
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                        0, 0, new java.awt.Color(16, 185, 129),
                        getWidth(), getHeight(), new java.awt.Color(5, 150, 105));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw Lock Icon (Simple shapes)
                g2.setColor(new java.awt.Color(255, 255, 255, 220));
                // Body
                g2.fillRoundRect(getWidth() / 2 - 40, getHeight() / 2 - 20, 80, 70, 10, 10);
                // Shackle
                g2.setStroke(new java.awt.BasicStroke(8));
                g2.drawArc(getWidth() / 2 - 25, getHeight() / 2 - 60, 50, 60, 0, 180);

                // Keyhole
                g2.setColor(new java.awt.Color(5, 150, 105));
                g2.fillOval(getWidth() / 2 - 8, getHeight() / 2 + 10, 16, 16);

                g2.dispose();
            }
        };
        securityCard.setPreferredSize(new java.awt.Dimension(0, 200));
        securityCard.setOpaque(false);
        p.add(securityCard, java.awt.BorderLayout.NORTH);

        // Tips Panel
        javax.swing.JPanel tipsPanel = createPanelWithShadow();
        tipsPanel.setLayout(new java.awt.BorderLayout());
        tipsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));

        String html = "<html><body style='width: 300px'>" +
                "<h3 style='color: #1e293b; font-family: Segoe UI'>Lưu ý bảo mật</h3>" +
                "<ul style='color: #64748b; font-family: Segoe UI; font-size: 13px; line-height: 1.5; padding-left: 15px'>"
                +
                "<li>Không sử dụng mã PIN dễ đoán (như 123456, 000000).</li>" +
                "<li>Mã PIN mới phải khác với mã PIN hiện tại của bạn.</li>" +
                "<li>Không chia sẻ mã PIN cho bất kỳ ai, kể cả nhân viên thư viện.</li>" +
                "<li>Đổi mã PIN thường xuyên để tăng cường bảo mật.</li>" +
                "</ul></body></html>";

        javax.swing.JLabel tips = new javax.swing.JLabel(html);
        tipsPanel.add(tips, java.awt.BorderLayout.CENTER);

        p.add(tipsPanel, java.awt.BorderLayout.CENTER);

        return p;
    }

    // --- Helpers ---

    private javax.swing.JPanel createPanelWithShadow() {
        javax.swing.JPanel p = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(java.awt.Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(new java.awt.Font("Segoe UI", 1, 18));
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240)),
                javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        f.setBackground(new java.awt.Color(248, 250, 252));
        f.setPreferredSize(new java.awt.Dimension(0, 50));
        return f;
    }

    private javax.swing.JButton createModernButton(String text, java.awt.Color bg) {
        javax.swing.JButton b = new javax.swing.JButton(text);
        b.setFont(new java.awt.Font("Segoe UI", 1, 14));
        b.setForeground(java.awt.Color.WHITE);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        b.setPreferredSize(new java.awt.Dimension(0, 50));
        return b;
    }

    // --- Business Logic (Preserved) ---

    private void changeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String oldPin = new String(oldPinField.getPassword());
        String newPin = new String(newPinField.getPassword());
        String confirmPin = new String(confirmPinField.getPassword());

        // Validation
        if (oldPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            setStatus("Vui lòng nhập đầy đủ thông tin!", false);
            return;
        }

        if (oldPin.length() != 6 || newPin.length() != 6) {
            setStatus("Mã PIN phải có đúng 6 ký tự!", false);
            return;
        }

        if (!newPin.equals(confirmPin)) {
            setStatus("Mã PIN xác nhận không khớp!", false);
            newPinField.setText("");
            confirmPinField.setText("");
            newPinField.requestFocus();
            return;
        }

        if (oldPin.equals(newPin)) {
            setStatus("Mã PIN mới phải khác mã PIN cũ!", false);
            return;
        }

        // Disable button and show loading
        changeButton.setEnabled(false);
        setStatus("Đang kết nối thẻ...", true);

        // Change PIN in background thread
        new Thread(() -> {
            CardConnectionManager connManager = null;
            try {
                // Connect to card
                connManager = new CardConnectionManager();
                connManager.connectCard();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang xác thực PIN cũ...", true);
                });

                // Get card setup manager
                CardSetupManager setupManager = new CardSetupManager(connManager.getChannel());
                // Lấy public key
                if (!setupManager.getPublicKey()) {
                    throw new Exception("Failed to get card public key");
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang xác thực PIN cũ...", true);
                });

                // Step 1: Verify old PIN first
                CardVerifyManager verifyManager = new CardVerifyManager(connManager.getChannel());
                boolean verified = verifyManager.verifyPin(oldPin);

                if (!verified) {
                    throw new Exception("Mã PIN cũ không chính xác!");
                }

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang thay đổi mã PIN...", true);
                });

                // Step 2: Gửi lệnh đổi PIN
                setupManager.changePin(oldPin, newPin);

                System.out.println("PIN change command sent. Reconnecting to verify...");

                // Step 3: Disconnect and reconnect to verify PIN change
                connManager.disconnectCard();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Đang xác thực PIN mới...", true);
                });

                Thread.sleep(500); // Wait a bit for card to settle

                // Reconnect and verify new PIN
                connManager = new CardConnectionManager();
                connManager.connectCard();

                CardVerifyManager newVerifyManager = new CardVerifyManager(connManager.getChannel());
                boolean newPinVerified = newVerifyManager.verifyPin(newPin);

                if (!newPinVerified) {
                    throw new Exception("Xác thực mã PIN mới thất bại!");
                }

                System.out.println("New PIN verified successfully!");

                javax.swing.SwingUtilities.invokeLater(() -> {
                    setStatus("Thay đổi mã PIN thành công!", true);

                    // Clear fields
                    oldPinField.setText("");
                    newPinField.setText("");
                    confirmPinField.setText("");

                    // Re-enable button after 2 seconds
                    javax.swing.Timer timer = new javax.swing.Timer(2000, e -> changeButton.setEnabled(true));
                    timer.setRepeats(false);
                    timer.start();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    String errorMsg = ex.getMessage();
                    if ("CARD_BLOCKED".equals(errorMsg)) {
                        setStatus("Thẻ đã bị khóa do nhập sai PIN nhiều lần!", false);
                    } else {
                        setStatus("Lỗi: " + (errorMsg != null ? errorMsg : "Không thể thay đổi PIN"), false);
                    }
                    changeButton.setEnabled(true);
                });
            } finally {
                // Always disconnect card at the end
                if (connManager != null) {
                    try {
                        connManager.disconnectCard();
                    } catch (Exception e) {
                        System.err.println("Error disconnecting card: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void setStatus(String message, boolean isSuccess) {
        statusLabel.setForeground(isSuccess ? new java.awt.Color(22, 163, 74) : new java.awt.Color(220, 38, 38));
        statusLabel.setText(message);
    }
}
