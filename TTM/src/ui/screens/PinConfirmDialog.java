/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import smartcard.CardConnectionManager;
import smartcard.CardVerifyManager;

/**
 * Dialog xác nhận mã PIN trước khi thực hiện giao dịch quan trọng
 */
public class PinConfirmDialog extends JDialog {

    private JPasswordField pinField;
    private boolean confirmed = false;
    private JLabel statusLabel;
    private JButton confirmButton;
    private JButton cancelButton;

    public PinConfirmDialog(Frame parent) {
        super(parent, "Xác nhận PIN", true);
        initComponents();
        setSize(400, 300);
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Xác nhận mã PIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(15, 23, 42)); // Slate 900
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 30, 400, 30);
        panel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Vui lòng nhập mã PIN để tiếp tục");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139)); // Slate 500
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBounds(0, 70, 400, 20);
        panel.add(subtitleLabel);

        pinField = new JPasswordField();
        pinField.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pinField.setHorizontalAlignment(SwingConstants.CENTER);
        pinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        pinField.setBounds(50, 110, 300, 50);

        // Allow Enter key to trigger confirm
        pinField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onConfirm();
                }
            }
        });

        panel.add(pinField);

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(220, 38, 38)); // Red 600
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBounds(0, 170, 400, 20);
        panel.add(statusLabel);

        cancelButton = new JButton("Hủy bỏ");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setForeground(new Color(100, 116, 139));
        cancelButton.setBackground(new Color(241, 245, 249)); // Slate 100
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cancelButton.setBounds(50, 200, 140, 40);
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        panel.add(cancelButton);

        confirmButton = new JButton("Xác nhận");
        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setBackground(new Color(15, 23, 42)); // Slate 900
        confirmButton.setBorderPainted(false);
        confirmButton.setFocusPainted(false);
        confirmButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        confirmButton.setBounds(210, 200, 140, 40);
        confirmButton.addActionListener(e -> onConfirm());
        panel.add(confirmButton);

        add(panel);
    }

    private void onConfirm() {
        String pin = new String(pinField.getPassword());
        if (pin.length() != 6) {
            statusLabel.setText("Mã PIN phải có đúng 6 ký tự số");
            return;
        }

        // Disable UI
        setLoading(true);

        new SwingWorker<Boolean, Void>() {
            String errorMsg = "";

            @Override
            protected Boolean doInBackground() throws Exception {
                CardConnectionManager connManager = CardConnectionManager.getInstance();
                try {
                    // Try to connect
                    if (!connManager.connectCard()) {
                        errorMsg = "Không thể kết nối tới thẻ!";
                        return false;
                    }

                    // Verify PIN
                    CardVerifyManager verifyManager = new CardVerifyManager(connManager.getChannel());
                    return verifyManager.verifyPin(pin);

                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    if (errorMsg.contains("WRONG_PIN")) {
                        // Extract retries if available logic allows
                        if (errorMsg.contains(":")) {
                            String[] parts = errorMsg.split(":");
                            errorMsg = "Sai mã PIN! Còn lại " + parts[1] + " lần thử.";
                        } else {
                            errorMsg = "Sai mã PIN!";
                        }
                    } else if (errorMsg.contains("CARD_BLOCKED")) {
                        errorMsg = "Thẻ đã bị KHÓA do nhập sai quá nhiều lần!";
                    }
                    return false;
                } finally {
                    connManager.disconnectCard();
                }
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    boolean success = get();
                    if (success) {
                        confirmed = true;
                        dispose();
                    } else {
                        statusLabel.setText(errorMsg.isEmpty() ? "Xác thực thất bại!" : errorMsg);
                        pinField.setText("");
                        pinField.requestFocus();
                    }
                } catch (Exception e) {
                    statusLabel.setText("Lỗi hệ thống: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void setLoading(boolean loading) {
        confirmButton.setEnabled(!loading);
        cancelButton.setEnabled(!loading);
        pinField.setEnabled(!loading);
        if (loading) {
            confirmButton.setText("Đang kiểm tra...");
        } else {
            confirmButton.setText("Xác nhận");
        }
    }
}
