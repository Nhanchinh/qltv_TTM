package services;

import ui.DBConnect;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionService {
    
    public static class Transaction {
        public String transId;
        public String cardId;
        public String type;
        public double amount;
        public int pointsChanged;
        public String dateTime;
        
        public Transaction(String transId, String cardId, String type, double amount, int pointsChanged, String dateTime) {
            this.transId = transId;
            this.cardId = cardId;
            this.type = type;
            this.amount = amount;
            this.pointsChanged = pointsChanged;
            this.dateTime = dateTime;
        }
    }
    
    public boolean createTransaction(String transId, String cardId, String type, double amount, int pointsChanged) {
        String sql = "INSERT INTO Transactions (TransID, CardID, Type, Amount, PointsChanged, DateTime, SignatureCard, SignatureStore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transId);
            pstmt.setString(2, cardId);
            pstmt.setString(3, type);
            pstmt.setDouble(4, amount);
            pstmt.setInt(5, pointsChanged);
            pstmt.setString(6, LocalDateTime.now().toString());
            pstmt.setBytes(7, new byte[]{});
            pstmt.setBytes(8, new byte[]{});
            
            if (pstmt.executeUpdate() > 0) {
                // Update card balance if deposit
                if (type.equals("Deposit")) {
                    CardService cardService = new CardService();
                    // Note: You might want to add a balance field to Cards table
                    // For now, we'll just update TotalSpent
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Transaction> getTransactionsByCard(String cardId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM Transactions WHERE CardID = ? ORDER BY DateTime DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getString("TransID"),
                        rs.getString("CardID"),
                        rs.getString("Type"),
                        rs.getDouble("Amount"),
                        rs.getInt("PointsChanged"),
                        rs.getString("DateTime")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    public List<Transaction> getTransactionsByType(String cardId, String type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM Transactions WHERE CardID = ? AND Type = ? ORDER BY DateTime DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getString("TransID"),
                        rs.getString("CardID"),
                        rs.getString("Type"),
                        rs.getDouble("Amount"),
                        rs.getInt("PointsChanged"),
                        rs.getString("DateTime")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}

