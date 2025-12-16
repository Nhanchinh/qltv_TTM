package ui.screens;

import services.SettingsService;
import smartcard.CardConnectionManager;
import smartcard.CardVerifyManager;
import smartcard.CardKeyManager;
import smartcard.CardIdExtractor;
import javax.smartcardio.CardChannel;
import ui.DBConnect;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class PinLoginDialog extends JDialog {

    private JPasswordField pinField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel errorLabel;
    private boolean authenticated = false;
    private boolean cardBlocked = false;
    private SettingsService settingsService;
    private String authenticatedCardId = null;
    private static String lastAuthenticatedCardId = null;

    // Modern Colors
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235); // Blue 600
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42); // Slate 900
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate 500
    private static final Color BG_COLOR = Color.WHITE;

    private static final byte INS_GET_PIN_TRIES = (byte) 0x33;

    public PinLoginDialog(Frame parent) {
        super(parent, true);
        settingsService = new SettingsService();
        initComponents();
        setupDialog();
    }

    private void initComponents() {
        setTitle("Đăng nhập hệ thống");
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // USE A SINGLE WHITE PANEL - NO NESTED CARDS
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // 1. Icon (Lock)
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Circle bg
                g2.setColor(new Color(239, 246, 255)); // Very light blue
                g2.fillOval(0, 0, 80, 80);

                // Icon (Lock)
                g2.setColor(PRIMARY_COLOR);
                int cx = 40, cy = 40;
                // Body
                g2.fillRoundRect(cx - 14, cy - 10 + 5, 28, 20, 5, 5);
                // Shackle
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawArc(cx - 9, cy - 22, 18, 18, 0, 180);
                // Keyhole
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - 3, cy + 3, 6, 6);
                g2.fillRect(cx - 1, cy + 6, 2, 6);

                g2.dispose();
            }
        };
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setPreferredSize(new Dimension(80, 80));
        iconLabel.setMaximumSize(new Dimension(80, 80));

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(25));

        // 2. Title
        JLabel titleLabel = new JLabel("XÁC THỰC THẺ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);

        contentPanel.add(Box.createVerticalStrut(8));

        // 3. Subtitle
        JLabel subLabel = new JLabel("Nhập mã PIN 6 số để truy cập");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subLabel);

        contentPanel.add(Box.createVerticalStrut(40));

        // 4. PIN Field
        JPanel pinWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pinWrapper.setOpaque(false);

        pinField = new JPasswordField();
        pinField.setPreferredSize(new Dimension(280, 50));
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setBackground(new Color(248, 250, 252));
        // Single clean border
        pinField.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));

        // Focus state
        pinField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                pinField.setBackground(Color.WHITE);
                pinField.setBorder(new LineBorder(PRIMARY_COLOR, 2, true));
            }

            public void focusLost(FocusEvent e) {
                pinField.setBackground(new Color(248, 250, 252));
                pinField.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
            }
        });

        pinWrapper.add(pinField);
        pinWrapper.setMaximumSize(new Dimension(400, 60));
        contentPanel.add(pinWrapper);

        // 5. Error Label
        errorLabel = new JLabel(" "); // Spacer
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        errorLabel.setForeground(new Color(220, 38, 38));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(errorLabel);

        contentPanel.add(Box.createVerticalStrut(30));

        // 6. Action Buttons (Full width stacked or big grid)
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(320, 50)); // Taller container

        loginButton = createModernButton("ĐĂNG NHẬP", PRIMARY_COLOR, Color.WHITE);
        loginButton.addActionListener(e -> attemptLogin());

        cancelButton = createModernButton("THOÁT", new Color(241, 245, 249), TEXT_SECONDARY);
        cancelButton.addActionListener(e -> {
            authenticated = false;
            dispose();
        });

        btnPanel.add(loginButton);
        btnPanel.add(cancelButton);

        contentPanel.add(btnPanel);
        contentPanel.add(Box.createVerticalGlue()); // Push everything slightly up

        setContentPane(contentPanel);
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Slightly bigger font
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 50)); // Explicit tall height
        return btn;
    }

    private void setupDialog() {
        // Slightly rectangular vertical ratio like a mobile screen or auth card
        setSize(420, 520);
        setLocationRelativeTo(null);
        pinField.requestFocusInWindow();

        pinField.addActionListener(e -> attemptLogin()); // Enter to submit

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                authenticated = false;
                cardBlocked = false;
                dispose();
            }
        });
    }

    // --- Business Logic ---

    private void attemptLogin() {
        String enteredPin = new String(pinField.getPassword());
        if (enteredPin.isEmpty() || enteredPin.length() != 6) {
            errorLabel.setText("Vui lòng nhập mã PIN 6 số!");
            pinField.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        errorLabel.setForeground(PRIMARY_COLOR);
        errorLabel.setText("Đang kết nối thẻ...");

        new Thread(() -> {
            CardConnectionManager connManager = null;
            try {
                connManager = new CardConnectionManager();
                connManager.connectCard();

                SwingUtilities.invokeLater(() -> errorLabel.setText("Đang xác thực..."));

                CardVerifyManager verifyManager = new CardVerifyManager(connManager.getChannel());
                boolean verified = verifyManager.verifyPin(enteredPin);

                if (verified) {
                    authenticateUserAfterLogin(connManager.getChannel());
                    SwingUtilities.invokeLater(() -> {
                        authenticated = true;
                        dispose();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        errorLabel.setForeground(new Color(220, 38, 38));
                        errorLabel.setText("Mã PIN không đúng!");
                        pinField.setText("");
                        loginButton.setEnabled(true);
                        shakeDialog();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                handleLoginError(ex);
            } finally {
                if (connManager != null) {
                    try {
                        connManager.disconnectCard();
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    private void handleLoginError(Exception ex) {
        SwingUtilities.invokeLater(() -> {
            String msg = ex.getMessage();
            if ("CARD_BLOCKED".equals(msg)) {
                JOptionPane.showMessageDialog(this, "Thẻ đã bị khóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                cardBlocked = true;
                dispose();
            } else if (msg != null && msg.startsWith("WRONG_PIN")) {
                errorLabel.setForeground(new Color(220, 38, 38));
                errorLabel.setText("Sai PIN! (" + (msg.contains(":") ? msg.split(":")[1] : "?") + " lần thử)");
            } else {
                errorLabel.setForeground(new Color(220, 38, 38));
                errorLabel.setText("Lỗi: " + msg);
            }
            pinField.setText("");
            loginButton.setEnabled(true);
            shakeDialog();
        });
    }

    private void authenticateUserAfterLogin(javax.smartcardio.CardChannel channel) throws Exception {
        CardKeyManager keyManager = new CardKeyManager(channel);
        keyManager.getPublicKey();
        if (!keyManager.loadAppKeyPair())
            throw new Exception("App KeyPair error");

        String cardId = CardIdExtractor.extractCardId(channel, keyManager);
        if (cardId == null)
            throw new Exception("Card ID error");

        this.authenticatedCardId = cardId;
        lastAuthenticatedCardId = cardId;
    }

    private void shakeDialog() {
        Point p = getLocation();
        Timer t = new Timer(40, new ActionListener() {
            int c = 0;

            public void actionPerformed(ActionEvent e) {
                if (c++ < 5)
                    setLocation(p.x + (c % 2 == 0 ? 5 : -5), p.y);
                else {
                    setLocation(p);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        t.start();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isCardBlocked() {
        return cardBlocked;
    }

    public static String getLastAuthenticatedCardId() {
        return lastAuthenticatedCardId;
    }

    public enum LoginResult {
        SUCCESS, CANCELLED, CARD_BLOCKED
    }

    public static LoginResult showPinDialog(Frame parent) {
        if (DBConnect.getConnection() == null) {
            JOptionPane.showMessageDialog(parent, "Lỗi kết nối DB!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return LoginResult.CANCELLED;
        }
        new SettingsService().initializeDefaultPin();
        lastAuthenticatedCardId = null;
        PinLoginDialog dialog = new PinLoginDialog(parent);
        dialog.setVisible(true);
        if (dialog.isAuthenticated())
            return LoginResult.SUCCESS;
        if (dialog.isCardBlocked())
            return LoginResult.CARD_BLOCKED;
        return LoginResult.CANCELLED;
    }
}