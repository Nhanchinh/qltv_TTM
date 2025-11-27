/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui.screens;

import services.CardService;
import ui.DBConnect;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author admin
 */
public class thongtincanhan extends javax.swing.JPanel {
    
    private CardService cardService;
    private String currentCardId = "CARD001";

    /**
     * Creates new form PersonalInfoPanel
     */
    public thongtincanhan() {
        cardService = new CardService();
        initComponents();
        loadCardInfo();
    }
    
    /**
     * Load card information from database
     */
    private void loadCardInfo() {
        // Recalculate TotalSpent from history to ensure accuracy
        cardService.recalculateTotalSpent(currentCardId);
        
        CardService.Card card = cardService.getCardById(currentCardId);
        if (card != null) {
            cardIdField.setText(card.cardId);
            nameField.setText(card.fullName);
            phoneField.setText(card.phone);
            
            // Format date if exists
            if (card.dob != null && !card.dob.isEmpty()) {
                try {
                    // Parse date from database format (YYYY-MM-DD) to DD/MM/YYYY
                    if (card.dob.contains("-")) {
                        String[] parts = card.dob.split("-");
                        if (parts.length == 3) {
                            dobField.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                        } else {
                            dobField.setText(card.dob);
                        }
                    } else {
                        dobField.setText(card.dob);
                    }
                } catch (Exception e) {
                    dobField.setText(card.dob);
                }
            } else {
                dobField.setText("");
            }
            
            if (card.registerDate != null && !card.registerDate.isEmpty()) {
                try {
                    // Parse date from database format
                    if (card.registerDate.contains("-")) {
                        String[] parts = card.registerDate.split("-");
                        if (parts.length == 3) {
                            registerDateField.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
                        } else {
                            registerDateField.setText(card.registerDate);
                        }
                    } else {
                        registerDateField.setText(card.registerDate);
                    }
                } catch (Exception e) {
                    registerDateField.setText(card.registerDate);
                }
            } else {
                registerDateField.setText("");
            }
            
            // Member info - Calculate TotalSpent from history to ensure accuracy
            double actualTotalSpent = cardService.calculateTotalSpentFromHistory(currentCardId);
            memberTypeField.setText(card.memberType != null ? card.memberType : "Basic");
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            totalSpentField.setText(nf.format(actualTotalSpent) + " đ");
            totalPointsField.setText(nf.format(card.totalPoints) + " điểm");
            fineDebtField.setText(nf.format(card.fineDebt) + " đ");
            isBlockedField.setText(card.isBlocked ? "Bi khoa" : "Hoat dong");
        } else {
            // Set default values if card not found
            cardIdField.setText(currentCardId);
            nameField.setText("");
            phoneField.setText("");
            dobField.setText("");
            registerDateField.setText("");
            memberTypeField.setText("Basic");
            totalSpentField.setText("0 đ");
            totalPointsField.setText("0 điểm");
            fineDebtField.setText("0 đ");
            isBlockedField.setText("Hoạt động");
        }
    }
    
    /**
     * Reload card info (public method for external refresh)
     */
    public void reloadCardInfo() {
        loadCardInfo();
    }

    /**
     * Khởi tạo các component của giao diện
     * Code này được viết thủ công
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        // Tạo các component (các thành phần giao diện)
        titleLabel = new javax.swing.JLabel();
        
        // Thông tin cơ bản
        cardIdLabel = new javax.swing.JLabel();
        cardIdField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        phoneLabel = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        dobLabel = new javax.swing.JLabel();
        dobField = new javax.swing.JTextField();
        registerDateLabel = new javax.swing.JLabel();
        registerDateField = new javax.swing.JTextField();
        
        // Thông tin hội viên
        memberTypeLabel = new javax.swing.JLabel();
        memberTypeField = new javax.swing.JTextField();
        totalSpentLabel = new javax.swing.JLabel();
        totalSpentField = new javax.swing.JTextField();
        totalPointsLabel = new javax.swing.JLabel();
        totalPointsField = new javax.swing.JTextField();
        fineDebtLabel = new javax.swing.JLabel();
        fineDebtField = new javax.swing.JTextField();
        isBlockedLabel = new javax.swing.JLabel();
        isBlockedField = new javax.swing.JTextField();
        
        saveButton = new javax.swing.JButton();
        basicInfoPanel = new javax.swing.JPanel();
        memberInfoPanel = new javax.swing.JPanel();

        setBackground(new java.awt.Color(245, 245, 250));
        setLayout(new java.awt.BorderLayout(0, 0));

        // Thiết lập title
        javax.swing.JPanel titlePanel = new javax.swing.JPanel();
        titlePanel.setBackground(new java.awt.Color(245, 245, 250));
        titlePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 40, 20, 40));
        titleLabel.setFont(new java.awt.Font("Segoe UI", 1, 28));
        titleLabel.setForeground(new java.awt.Color(45, 45, 48));
        titleLabel.setText("Thông tin thẻ");
        titlePanel.add(titleLabel);
        add(titlePanel, java.awt.BorderLayout.NORTH);

        // Thiết lập các label (nhãn) - Thông tin cơ bản
        cardIdLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        cardIdLabel.setForeground(new java.awt.Color(60, 60, 60));
        cardIdLabel.setText("Mã thẻ:");

        nameLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        nameLabel.setForeground(new java.awt.Color(60, 60, 60));
        nameLabel.setText("Họ và tên:");

        phoneLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        phoneLabel.setForeground(new java.awt.Color(60, 60, 60));
        phoneLabel.setText("Số điện thoại:");

        dobLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        dobLabel.setForeground(new java.awt.Color(60, 60, 60));
        dobLabel.setText("Ngày sinh:");

        registerDateLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        registerDateLabel.setForeground(new java.awt.Color(60, 60, 60));
        registerDateLabel.setText("Ngày đăng ký:");

        // Thông tin hội viên
        memberTypeLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        memberTypeLabel.setForeground(new java.awt.Color(60, 60, 60));
        memberTypeLabel.setText("Loại hội viên:");

        totalSpentLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        totalSpentLabel.setForeground(new java.awt.Color(60, 60, 60));
        totalSpentLabel.setText("Tổng tiền đã chi:");

        totalPointsLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        totalPointsLabel.setForeground(new java.awt.Color(60, 60, 60));
        totalPointsLabel.setText("Tổng điểm tích lũy:");

        fineDebtLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        fineDebtLabel.setForeground(new java.awt.Color(60, 60, 60));
        fineDebtLabel.setText("Tiền nợ phạt:");

        isBlockedLabel.setFont(new java.awt.Font("Segoe UI", 1, 13));
        isBlockedLabel.setForeground(new java.awt.Color(60, 60, 60));
        isBlockedLabel.setText("Trạng thái thẻ:");

        // Thiết lập các text field (ô nhập liệu) - Thông tin cơ bản
        cardIdField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cardIdField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        cardIdField.setColumns(30);
        cardIdField.setEditable(false);

        nameField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        nameField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        nameField.setColumns(30);

        phoneField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        phoneField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        phoneField.setColumns(30);

        dobField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        dobField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        dobField.setColumns(30);

        registerDateField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        registerDateField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        registerDateField.setColumns(30);
        registerDateField.setEditable(false);

        // Thông tin hội viên
        memberTypeField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        memberTypeField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        memberTypeField.setColumns(30);
        memberTypeField.setEditable(false);

        totalSpentField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        totalSpentField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        totalSpentField.setColumns(30);
        totalSpentField.setEditable(false);

        totalPointsField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        totalPointsField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        totalPointsField.setColumns(30);
        totalPointsField.setEditable(false);

        fineDebtField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        fineDebtField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        fineDebtField.setColumns(30);
        fineDebtField.setEditable(false);

        isBlockedField.setFont(new java.awt.Font("Segoe UI", 0, 13));
        isBlockedField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
            javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        isBlockedField.setColumns(30);
        isBlockedField.setEditable(false);

        // Thiết lập button
        saveButton.setBackground(new java.awt.Color(0, 120, 215));
        saveButton.setFont(new java.awt.Font("Segoe UI", 1, 14));
        saveButton.setForeground(new java.awt.Color(255, 255, 255));
        saveButton.setText("💾 Lưu thông tin");
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new java.awt.Dimension(0, 40));
        saveButton.addActionListener(this::saveButtonActionPerformed);
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveButton.setBackground(new java.awt.Color(0, 100, 180));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveButton.setBackground(new java.awt.Color(0, 120, 215));
            }
        });

        // Tạo panel chứa thông tin cơ bản
        basicInfoPanel.setBackground(new java.awt.Color(255, 255, 255));
        basicInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null, "Thông tin cơ bản",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16),
                new java.awt.Color(60, 60, 60)
            ),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Sử dụng GroupLayout để sắp xếp các component - Thông tin cơ bản
        javax.swing.GroupLayout basicInfoLayout = new javax.swing.GroupLayout(basicInfoPanel);
        basicInfoPanel.setLayout(basicInfoLayout);
        
        basicInfoLayout.setHorizontalGroup(
            basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(phoneLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dobLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(registerDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, 10)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cardIdField)
                    .addComponent(nameField)
                    .addComponent(phoneField)
                    .addComponent(dobField)
                    .addComponent(registerDateField))
                .addContainerGap())
        );
        
        basicInfoLayout.setVerticalGroup(
            basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cardIdLabel)
                    .addComponent(cardIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(phoneLabel)
                    .addComponent(phoneField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dobLabel)
                    .addComponent(dobField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basicInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(registerDateLabel)
                    .addComponent(registerDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        // Tạo panel chứa thông tin hội viên
        memberInfoPanel.setBackground(new java.awt.Color(255, 255, 255));
        memberInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createTitledBorder(
                null, "Thông tin hội viên",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Segoe UI", 1, 16),
                new java.awt.Color(60, 60, 60)
            ),
            javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        javax.swing.GroupLayout memberInfoLayout = new javax.swing.GroupLayout(memberInfoPanel);
        memberInfoPanel.setLayout(memberInfoLayout);
        
        memberInfoLayout.setHorizontalGroup(
            memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memberInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(memberTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalSpentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(totalPointsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fineDebtLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(isBlockedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(memberTypeField)
                    .addComponent(totalSpentField)
                    .addComponent(totalPointsField)
                    .addComponent(fineDebtField)
                    .addComponent(isBlockedField))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        
        memberInfoLayout.setVerticalGroup(
            memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(memberInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(memberTypeLabel)
                    .addComponent(memberTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalSpentLabel)
                    .addComponent(totalSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPointsLabel)
                    .addComponent(totalPointsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fineDebtLabel)
                    .addComponent(fineDebtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(memberInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isBlockedLabel)
                    .addComponent(isBlockedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        // Layout chính của panel này - dùng BorderLayout với content panel
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setBackground(new java.awt.Color(245, 245, 250));
        contentPanel.setLayout(new java.awt.BorderLayout(0, 0));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Panel chứa 2 info panel nằm ngang
        javax.swing.JPanel infoPanelsContainer = new javax.swing.JPanel();
        infoPanelsContainer.setBackground(new java.awt.Color(245, 245, 250));
        infoPanelsContainer.setLayout(new javax.swing.BoxLayout(infoPanelsContainer, javax.swing.BoxLayout.X_AXIS));
        
        // Đặt kích thước cho các panel để hẹp lại nhưng tự động căn chỉnh
        basicInfoPanel.setAlignmentY(javax.swing.JComponent.TOP_ALIGNMENT);
        basicInfoPanel.setPreferredSize(new java.awt.Dimension(400, basicInfoPanel.getPreferredSize().height));
        basicInfoPanel.setMaximumSize(new java.awt.Dimension(450, Integer.MAX_VALUE));
        memberInfoPanel.setAlignmentY(javax.swing.JComponent.TOP_ALIGNMENT);
        memberInfoPanel.setPreferredSize(new java.awt.Dimension(400, memberInfoPanel.getPreferredSize().height));
        memberInfoPanel.setMaximumSize(new java.awt.Dimension(450, Integer.MAX_VALUE));
        
        infoPanelsContainer.add(basicInfoPanel);
        infoPanelsContainer.add(javax.swing.Box.createHorizontalStrut(20));
        infoPanelsContainer.add(memberInfoPanel);
        
        contentPanel.add(infoPanelsContainer, java.awt.BorderLayout.CENTER);
        
        // Button panel ở dưới
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setBackground(new java.awt.Color(245, 245, 250));
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 0, 10, 0));
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        saveButton.setAlignmentX(javax.swing.JComponent.CENTER_ALIGNMENT);
        buttonPanel.add(saveButton);
        contentPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
        
        add(contentPanel, java.awt.BorderLayout.CENTER);
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String dob = dobField.getText().trim();
        
        // Validate
        if (name.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Vui long nhap ho va ten!",
                "Loi",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (phone.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Vui long nhap so dien thoai!",
                "Loi",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update to database
        try {
            Connection dbConn = DBConnect.getConnection();
            if (dbConn != null) {
                // Convert date format from DD/MM/YYYY to YYYY-MM-DD
                String dobFormatted = dob;
                if (!dob.isEmpty() && dob.contains("/")) {
                    String[] parts = dob.split("/");
                    if (parts.length == 3) {
                        dobFormatted = parts[2] + "-" + parts[1] + "-" + parts[0];
                    }
                }
                
                String sql = "UPDATE Cards SET FullName = ?, Phone = ?, DOB = ? WHERE CardID = ?";
                try (PreparedStatement pstmt = dbConn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, phone);
                    pstmt.setString(3, dobFormatted.isEmpty() ? null : dobFormatted);
                    pstmt.setString(4, currentCardId);
                    
                    if (pstmt.executeUpdate() > 0) {
                        javax.swing.JOptionPane.showMessageDialog(this, 
                            "Da luu thong tin thanh cong!",
                            "Thong bao",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        loadCardInfo(); // Reload to show updated data
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this, 
                            "Loi khi luu thong tin!",
                            "Loi",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, 
                    "Khong the ket noi database!",
                    "Loi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Loi khi luu thong tin: " + e.getMessage(),
                "Loi",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_saveButtonActionPerformed


    // Variables declaration
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel basicInfoPanel;
    private javax.swing.JLabel cardIdLabel;
    private javax.swing.JTextField cardIdField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JTextField phoneField;
    private javax.swing.JLabel dobLabel;
    private javax.swing.JTextField dobField;
    private javax.swing.JLabel registerDateLabel;
    private javax.swing.JTextField registerDateField;
    private javax.swing.JPanel memberInfoPanel;
    private javax.swing.JLabel memberTypeLabel;
    private javax.swing.JTextField memberTypeField;
    private javax.swing.JLabel totalSpentLabel;
    private javax.swing.JTextField totalSpentField;
    private javax.swing.JLabel totalPointsLabel;
    private javax.swing.JTextField totalPointsField;
    private javax.swing.JLabel fineDebtLabel;
    private javax.swing.JTextField fineDebtField;
    private javax.swing.JLabel isBlockedLabel;
    private javax.swing.JTextField isBlockedField;
    private javax.swing.JButton saveButton;
}

