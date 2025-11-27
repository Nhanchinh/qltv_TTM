package services;

import ui.DBConnect;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StationeryService {
    
    public static class StationeryItem {
        public String itemId;
        public String name;
        public double price;
        public int stock;
        public String imagePath;
        
        public StationeryItem(String itemId, String name, double price, int stock, String imagePath) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.imagePath = imagePath;
        }
    }
    
    public static class SaleRecord {
        public int id;
        public String cardId;
        public String itemId;
        public int quantity;
        public double unitPrice;
        public double finalPrice;
        public int pointsUsed;
        public String saleDate;
        
        public SaleRecord(int id, String cardId, String itemId, int quantity,
                         double unitPrice, double finalPrice, int pointsUsed, String saleDate) {
            this.id = id;
            this.cardId = cardId;
            this.itemId = itemId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.finalPrice = finalPrice;
            this.pointsUsed = pointsUsed;
            this.saleDate = saleDate;
        }
    }
    
    public List<StationeryItem> getAllItems() {
        List<StationeryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM Stationery";
        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new StationeryItem(
                    rs.getString("ItemID"),
                    rs.getString("Name"),
                    rs.getDouble("Price"),
                    rs.getInt("Stock"),
                    rs.getString("ImagePath")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public StationeryItem getItemById(String itemId) {
        String sql = "SELECT * FROM Stationery WHERE ItemID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new StationeryItem(
                        rs.getString("ItemID"),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getInt("Stock"),
                        rs.getString("ImagePath")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean sellItem(String cardId, String itemId, int quantity, int pointsUsed) {
        try (Connection conn = DBConnect.getConnection()) {
            if (conn == null) {
                return false;
            }
            
            conn.setAutoCommit(false); // Bat dau transaction
            
            try {
                // Get item info - su dung cung connection
                String getItemSql = "SELECT * FROM Stationery WHERE ItemID = ?";
                StationeryItem item = null;
                try (PreparedStatement getItemStmt = conn.prepareStatement(getItemSql)) {
                    getItemStmt.setString(1, itemId);
                    try (ResultSet rs = getItemStmt.executeQuery()) {
                        if (rs.next()) {
                            item = new StationeryItem(
                                rs.getString("ItemID"),
                                rs.getString("Name"),
                                rs.getDouble("Price"),
                                rs.getInt("Stock"),
                                rs.getString("ImagePath")
                            );
                        }
                    }
                }
                
                if (item == null || item.stock < quantity) {
                    conn.rollback();
                    return false;
                }
                
                double unitPrice = item.price;
                double finalPrice = (unitPrice * quantity) - (pointsUsed * 1000.0); // 1 point = 1000 VND
                if (finalPrice < 0) finalPrice = 0;
                
                // Insert sale - su dung cung connection
                String sql = "INSERT INTO StationerySales (CardID, ItemID, Quantity, UnitPrice, FinalPrice, PointsUsed, SaleDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, cardId);
                    pstmt.setString(2, itemId);
                    pstmt.setInt(3, quantity);
                    pstmt.setDouble(4, unitPrice);
                    pstmt.setDouble(5, finalPrice);
                    pstmt.setInt(6, pointsUsed);
                    pstmt.setString(7, LocalDateTime.now().toString());
                    pstmt.executeUpdate();
                }
                
                // Update stock - su dung cung connection
                String updateSql = "UPDATE Stationery SET Stock = ? WHERE ItemID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, item.stock - quantity);
                    updateStmt.setString(2, itemId);
                    updateStmt.executeUpdate();
                }
                
                // Update card points and total spent - su dung cung connection
                if (pointsUsed > 0) {
                    String updateCardSql = "UPDATE Cards SET TotalPoints = TotalPoints - ?, TotalSpent = TotalSpent + ? WHERE CardID = ? AND TotalPoints >= ?";
                    try (PreparedStatement updateCardStmt = conn.prepareStatement(updateCardSql)) {
                        updateCardStmt.setInt(1, pointsUsed);
                        updateCardStmt.setDouble(2, finalPrice);
                        updateCardStmt.setString(3, cardId);
                        updateCardStmt.setInt(4, pointsUsed);
                        updateCardStmt.executeUpdate();
                    }
                } else {
                    // Update only total spent if no points used
                    String updateCardSql = "UPDATE Cards SET TotalSpent = TotalSpent + ? WHERE CardID = ?";
                    try (PreparedStatement updateCardStmt = conn.prepareStatement(updateCardSql)) {
                        updateCardStmt.setDouble(1, finalPrice);
                        updateCardStmt.setString(2, cardId);
                        updateCardStmt.executeUpdate();
                    }
                }
                
                // Create transaction - su dung cung connection
                String transSql = "INSERT INTO Transactions (TransID, CardID, Type, Amount, PointsChanged, DateTime, SignatureCard, SignatureStore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement transStmt = conn.prepareStatement(transSql)) {
                    transStmt.setString(1, UUID.randomUUID().toString());
                    transStmt.setString(2, cardId);
                    transStmt.setString(3, "Payment");
                    transStmt.setDouble(4, -finalPrice);
                    transStmt.setInt(5, -pointsUsed);
                    transStmt.setString(6, LocalDateTime.now().toString());
                    transStmt.setBytes(7, new byte[]{});
                    transStmt.setBytes(8, new byte[]{});
                    transStmt.executeUpdate();
                }
                
                conn.commit(); // Commit transaction
                return true;
                
            } catch (SQLException e) {
                conn.rollback(); // Rollback neu co loi
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Loi khi ban VPP: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public List<SaleRecord> getSaleHistory(String cardId) {
        List<SaleRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM StationerySales WHERE CardID = ? ORDER BY SaleDate DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new SaleRecord(
                        rs.getInt("ID"),
                        rs.getString("CardID"),
                        rs.getString("ItemID"),
                        rs.getInt("Quantity"),
                        rs.getDouble("UnitPrice"),
                        rs.getDouble("FinalPrice"),
                        rs.getInt("PointsUsed"),
                        rs.getString("SaleDate")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}

