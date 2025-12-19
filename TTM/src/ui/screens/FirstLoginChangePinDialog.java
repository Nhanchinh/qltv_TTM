package ui.screens;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import smartcard.CardConnectionManager;
import smartcard.CardConnectionManager;
// import smartcard.CardFirstLoginManager; // Unused import
import smartcard.CardPinManager;
import smartcard.CardPinManager;

public class FirstLoginChangePinDialog extends JDialog {

    private JPasswordField newPinField;
    private JPasswordField confirmPinField;
    private JButton updateButton;
    private JLabel messageLabel;
    private boolean success = false;
    private CardConnectionManager connManager;

    // Colors
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229); // Indigo 600
    // private static final Color BG_COLOR = new Color(249, 250, 251); // Unused
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39); // Gray 900
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128); // Gray 500
    // private static final Color SUCCESS_COLOR = new Color(16, 185, 129); // Unused
    private static final Color ERROR_COLOR = new Color(239, 68, 68); // Red 500

    public FirstLoginChangePinDialog(Frame parent, CardConnectionManager connManager) {
        super(parent, true);
        this.connManager = connManager;
        initComponents();
        setupDialog();
    }

    private void initComponents() {
        setTitle("Đổi Mã PIN Lần Đầu");
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // 1. Icon Header
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Circle bg
                g2.setColor(new Color(238, 242, 255)); // Indigo 50
                g2.fillOval(0, 0, 70, 70);

                // Shield Icon
                g2.setColor(PRIMARY_COLOR);
                int[] xPoints = { 35, 55, 52, 35, 18, 15 };
                int[] yPoints = { 15, 22, 45, 58, 45, 22 };
                g2.fillPolygon(xPoints, yPoints, 6);

                // Checkmark inside
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(28, 38, 34, 44);
                g2.drawLine(34, 44, 44, 32);

                g2.dispose();
            }
        };
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setPreferredSize(new Dimension(70, 70));
        iconLabel.setMaximumSize(new Dimension(70, 70));

        mainPanel.add(iconLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // 2. Title & Subtitle
        JLabel titleLabel = new JLabel("Thiết lập mã PIN mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Để bảo mật, vui lòng đổi PIN 6 số.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subLabel);
        mainPanel.add(Box.createVerticalStrut(25));

        // 3. Form Fields
        // Label New PIN
        JLabel lblNew = new JLabel("Mã PIN Mới");
        lblNew.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNew.setForeground(TEXT_SECONDARY);
        lblNew.setAlignmentX(Component.CENTER_ALIGNMENT); // Simplified align for this layout

        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setOpaque(false);
        formContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // New PIN Input
        newPinField = createStyledPasswordField();

        // Confirm PIN Input
        confirmPinField = createStyledPasswordField();

        formContainer.add(createLabel("Mã PIN Mới"));
        formContainer.add(Box.createVerticalStrut(5));
        formContainer.add(newPinField);
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(createLabel("Xác Nhận Mã PIN"));
        formContainer.add(Box.createVerticalStrut(5));
        formContainer.add(confirmPinField);

        mainPanel.add(formContainer);
        mainPanel.add(Box.createVerticalStrut(20));

        // 4. Message Label
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        messageLabel.setForeground(ERROR_COLOR);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 5. Update Button
        updateButton = new JButton("CẬP NHẬT PIN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? PRIMARY_COLOR : new Color(209, 213, 219));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        updateButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        updateButton.setPreferredSize(new Dimension(250, 45));
        updateButton.setMaximumSize(new Dimension(250, 45));
        updateButton.setBorderPainted(false);
        updateButton.setFocusPainted(false);
        updateButton.setContentAreaFilled(false);
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        updateButton.addActionListener(e -> handleUpdatePin());

        mainPanel.add(updateButton);
        setContentPane(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(250, 40)); // Fixed width match button
        pf.setMaximumSize(new Dimension(250, 40));
        pf.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(209, 213, 219), 1, true),
                new EmptyBorder(5, 10, 5, 10)));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private void setupDialog() {
        setSize(360, 520);
        setLocationRelativeTo(null);

        // Prevent closing window without updating
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!success) {
                    int confirm = JOptionPane.showConfirmDialog(
                            FirstLoginChangePinDialog.this,
                            "Bạn có chắc muốn hủy? Bạn sẽ không thể đăng nhập nếu không đổi PIN.",
                            "Hủy đổi PIN",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        dispose();
                    }
                } else {
                    dispose();
                }
            }
        });
    }

    private void handleUpdatePin() {
        String newPin = new String(newPinField.getPassword());
        String confirmPin = new String(confirmPinField.getPassword());

        if (newPin.length() != 6 || !newPin.matches("\\d+")) {
            showError("Mã PIN phải gồm 6 chữ số!");
            return;
        }

        if (!newPin.equals(confirmPin)) {
            showError("Mã PIN xác nhận không khớp!");
            return;
        }

        updateButton.setEnabled(false);
        messageLabel.setForeground(PRIMARY_COLOR);
        messageLabel.setText("Đang cập nhật...");

        new Thread(() -> {
            try {
                // 1. Change PIN (using default user PIN '123456' as current, or actually we use
                // reset/update logic?)
                // Assuming "First Login" implies the PIN is currently the default one set by
                // admin.
                // However, the card might require verification of the OLD PIN to change it.
                // Or we can use `resetUserPin` if we had admin rights (but we are user here).
                // Standard User Change PIN: Verify Old PIN (passed from Login?) -> Update New
                // PIN.

                // Note: The user just logged in with a PIN (likely default). We should ask for
                // "Current PIN" to be safe,
                // OR since we just authenticated, we might have a session.
                // But CardPinManager usually needs Old PIN to change.
                // For simplicity/UX in "First Login", usually the "Old PIN" is known/entered
                // previously.
                // Let's assume we use the PIN they just logged in with (passed via constructor?
                // No, security risk).
                // Or we ask them to enter it?
                // Let's assume there is an APDU to update PIN if we are already verified?
                // Standard Javacard `PIN.check` updates internal state. `PIN.update` requires
                // no extra auth if programmed that way,
                // BUT usually `update` calculates limits.

                // CRITICAL: The requested flow describes:
                // Check First Login=1 -> Show Screen -> Update PIN -> Disable First Login=0.
                // It doesn't specify HOW to update PIN.
                // I will use `CardPinManager.updatePin(oldPin, newPin)`.
                // But wait, I don't have `oldPin` here.
                // I will add a field for "Old PIN" or pass it from PinLoginDialog if the user
                // just typed it.
                // Passing it is better for UX.

                // REVISION: I will update the constructor to accept the `currentPin` they just
                // used to login.

                showMessage("Đang đổi PIN trên thẻ...");
                CardPinManager pinManager = new CardPinManager(connManager.getChannel());
                // We need the current PIN. I'll modify constructor to take it.
                // For now, let's assume I'll fix constructor.

                // WAIT - I need to modify code below to add currentPin to logic.

            } catch (Exception ex) {
                ex.printStackTrace();
                showMessage("Lỗi: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> updateButton.setEnabled(true));
            }
        }).start();
    }

    // Placeholder to be replaced/augmented in next step
    public void setPinUpdateLogic(String currentPin) {
        updateButton.addActionListener(e -> {
            String newPin = new String(newPinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword());

            if (newPin.length() != 6 || !newPin.matches("\\d+")) {
                showError("Mã PIN phải gồm 6 chữ số!");
                return;
            }
            if (!newPin.equals(confirmPin)) {
                showError("Mã PIN xác nhận không khớp!");
                return;
            }

            updateButton.setEnabled(false);
            messageLabel.setForeground(PRIMARY_COLOR);
            messageLabel.setText("Đang xử lý...");

            new Thread(() -> {
                try {
                    CardPinManager pinManager = new CardPinManager(connManager.getChannel());
                    boolean changed = pinManager.updatePin(currentPin, newPin);

                    if (changed) {
                        // SYNC PUBLIC KEY TO DB
                        smartcard.CardSetupManager setupMgr = new smartcard.CardSetupManager(connManager.getChannel());
                        if (setupMgr.getPublicKey()) {
                            byte[] pubBytes = setupMgr.getKeyManager().getCardPublicKeyEncoded();
                            if (pubBytes != null) {
                                // We need CardID. We can get it from CardIdExtractor since we are connected
                                String cardIdVal = smartcard.CardIdExtractor.extractCardId(connManager.getChannel(),
                                        setupMgr.getKeyManager());
                                if (cardIdVal != null) {
                                    services.CardService cs = new services.CardService();
                                    cs.updateCardPublicKey(cardIdVal, pubBytes);
                                    System.out.println("First Login: Updated Public Key for Card: " + cardIdVal);
                                }
                            }
                        }

                        showMessage("Đang tắt trạng thái đăng nhập lần đầu...");
                        smartcard.CardFirstLoginManager firstLoginMgr = new smartcard.CardFirstLoginManager(
                                connManager.getChannel());
                        if (firstLoginMgr.disableFirstLogin()) {
                            success = true;
                            showMessage("Cập nhật thành công!");
                            Thread.sleep(1000);
                            dispose();
                        }
                    }
                } catch (Exception ex) {
                    showError("Lỗi: " + ex.getMessage());
                    SwingUtilities.invokeLater(() -> updateButton.setEnabled(true));
                }
            }).start();
        });
    }

    private void showError(String msg) {
        SwingUtilities.invokeLater(() -> {
            messageLabel.setForeground(ERROR_COLOR);
            messageLabel.setText(msg);
        });
    }

    private void showMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            messageLabel.setForeground(PRIMARY_COLOR);
            messageLabel.setText(msg);
        });
    }

    public boolean isSuccess() {
        return success;
    }
}
