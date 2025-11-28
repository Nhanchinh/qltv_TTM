/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import smartcard.SmartCardService;
import smartcard.SmartCardService.SmartCardStatus;
import smartcard.SmartCardService.SmartCardServiceException;

/**
 * Smart-card tab that lets the operator confirm whether the Java Card applet is
 * reachable through the PC/SC interface.
 */
public class quanlithe extends javax.swing.JPanel {

    private final SmartCardService cardService = new SmartCardService();

    private JLabel statusValueLabel;
    private JTextField atrField;
    private JTextField swField;
    private JTextField lastCommandField;
    private JTextArea dataArea;
    private JTextArea logArea;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton refreshButton;
    private JButton readTextButton;

    public quanlithe() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Quản lý thẻ thông minh");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(33, 64, 154));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel statusLabel = new JLabel("Trạng thái:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        contentPanel.add(statusLabel, gbc);

        statusValueLabel = new JLabel("Chưa kết nối");
        statusValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusValueLabel.setForeground(Color.DARK_GRAY);
        gbc.gridx = 1;
        contentPanel.add(statusValueLabel, gbc);

        JLabel atrLabel = new JLabel("ATR:");
        atrLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(atrLabel, gbc);

        atrField = createReadOnlyField();
        gbc.gridx = 1;
        contentPanel.add(atrField, gbc);

        JLabel swLabel = new JLabel("SW (Status Word):");
        swLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(swLabel, gbc);

        swField = createReadOnlyField();
        gbc.gridx = 1;
        contentPanel.add(swField, gbc);

        JLabel commandLabel = new JLabel("Lệnh cuối cùng (APDU):");
        commandLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(commandLabel, gbc);

        lastCommandField = createReadOnlyField();
        gbc.gridx = 1;
        contentPanel.add(lastCommandField, gbc);

        JLabel dataLabel = new JLabel("Dữ liệu nhận về (hex):");
        dataLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(dataLabel, gbc);

        dataArea = createTextArea();
        gbc.gridx = 1;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(new JScrollPane(dataArea), gbc);

        JLabel logLabel = new JLabel("Nhật ký thao tác:");
        logLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(logLabel, gbc);

        logArea = createTextArea();
        logArea.setRows(5);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setText("Hãy bấm \"Kết nối thẻ\" để kiểm tra thiết bị PC/SC.\n");
        gbc.gridx = 1;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(new JScrollPane(logArea), gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        add(buttonPanel, BorderLayout.SOUTH);

        connectButton = createActionButton("Kết nối thẻ");
        connectButton.addActionListener(e -> doConnect());
        buttonPanel.add(connectButton);

        refreshButton = createActionButton("Kiểm tra lại");
        refreshButton.addActionListener(e -> refreshStatus());
        refreshButton.setEnabled(false);
        buttonPanel.add(refreshButton);

        readTextButton = createActionButton("Đọc dữ liệu demo");
        readTextButton.addActionListener(e -> readText());
        readTextButton.setEnabled(false);
        buttonPanel.add(readTextButton);

        disconnectButton = createActionButton("Ngắt kết nối");
        disconnectButton.addActionListener(e -> doDisconnect());
        disconnectButton.setEnabled(false);
        buttonPanel.add(disconnectButton);
    }

    private JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 225)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 225)));
        return area;
    }

    private JButton createActionButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void doConnect() {
        runAsync(() -> {
            try {
                SmartCardStatus status = cardService.connect();
                appendLog(status.getMessage());
                updateStatus(status);
                refreshButton.setEnabled(true);
                readTextButton.setEnabled(true);
                disconnectButton.setEnabled(true);
            } catch (SmartCardServiceException ex) {
                appendLog("Không thể kết nối: " + ex.getMessage());
                setStatusError(ex.getMessage());
            }
        });
    }

    private void doDisconnect() {
        runAsync(() -> {
            try {
                SmartCardStatus status = cardService.disconnect();
                appendLog(status.getMessage());
                updateStatus(status);
                refreshButton.setEnabled(false);
                readTextButton.setEnabled(false);
                disconnectButton.setEnabled(false);
            } catch (SmartCardServiceException ex) {
                appendLog("Lỗi khi ngắt kết nối: " + ex.getMessage());
                setStatusError(ex.getMessage());
            }
        });
    }

    private void refreshStatus() {
        runAsync(() -> {
            try {
                SmartCardStatus status = cardService.refreshStatus();
                appendLog(status.getMessage());
                updateStatus(status);
            } catch (SmartCardServiceException ex) {
                appendLog("Không thể kiểm tra lại: " + ex.getMessage());
                setStatusError(ex.getMessage());
            }
        });
    }

    private void readText() {
        runAsync(() -> {
            try {
                SmartCardStatus status = cardService.readDemoText();
                String hexData = status.getData();
                String readableText = hexToAscii(hexData);
                appendLog("Hex nhận về: " + hexData);
                appendLog("Nội dung giải mã: " + readableText);
                updateStatus(status);
                SwingUtilities.invokeLater(() -> {
                    dataArea.setText("HEX: " + hexData + "\nTEXT: " + readableText);
                });
            } catch (SmartCardServiceException ex) {
                appendLog("Không thể đọc demo: " + ex.getMessage());
                setStatusError(ex.getMessage());
            }
        });
    }

    private void updateStatus(SmartCardStatus status) {
        SwingUtilities.invokeLater(() -> {
            boolean connected = status != null && status.isConnected();
            statusValueLabel.setText(connected ? "Đã kết nối (SW = " + status.getSw() + ")" : "Chưa kết nối");
            statusValueLabel.setForeground(connected ? new Color(0, 153, 0) : Color.DARK_GRAY);
            atrField.setText(status != null ? status.getAtr() : "");
            swField.setText(status != null ? status.getSw() : "");
            lastCommandField.setText(status != null ? status.getLastCommand() : "");
            dataArea.setText(status != null ? status.getData() : "");
        });
    }

    private void setStatusError(String message) {
        SwingUtilities.invokeLater(() -> {
            statusValueLabel.setText("Lỗi: " + message);
            statusValueLabel.setForeground(new Color(200, 50, 50));
        });
    }

    private void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void runAsync(Runnable task) {
        new Thread(task, "SmartCardServiceThread").start();
    }

    private String hexToAscii(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) {
            return "";
        }
        StringBuilder output = new StringBuilder(hexStr.length() / 2);
        try {
            for (int i = 0; i < hexStr.length(); i += 2) {
                String str = hexStr.substring(i, i + 2);
                output.append((char) Integer.parseInt(str, 16));
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return "Lỗi convert: " + hexStr;
        }
        return output.toString();
    }
}
