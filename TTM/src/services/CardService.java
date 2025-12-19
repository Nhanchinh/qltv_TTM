package services;

import ui.DBConnect;
import java.sql.*;

public class CardService {
    
    public static class Card {
        public String cardId;
        public String fullName;
        public String phone;
        public String address;
        public String dob;
        public String registerDate;
        public String memberType;
        public double totalSpent;
        public int totalPoints;
        public double fineDebt;
        public boolean isBlocked;
        
        public Card(String cardId, String fullName, String phone, String address, String dob, String registerDate,
                   String memberType, double totalSpent, int totalPoints, double fineDebt, boolean isBlocked) {
            this.cardId = cardId;
            this.fullName = fullName;
            this.phone = phone;
            this.address = address;
            this.dob = dob;
            this.registerDate = registerDate;
            this.memberType = memberType;
            this.totalSpent = totalSpent;
            this.totalPoints = totalPoints;
            this.fineDebt = fineDebt;
            this.isBlocked = isBlocked;
        }
    }
    
    public Card getCardById(String cardId) {
        String sql = "SELECT * FROM Cards WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Card(
                        rs.getString("CardID"),
                        rs.getString("FullName"),
                        rs.getString("Phone"),
                        rs.getString("Address"),
                        rs.getString("DOB"),
                        rs.getString("RegisterDate"),
                        rs.getString("MemberType"),
                        rs.getDouble("TotalSpent"),
                        rs.getInt("TotalPoints"),
                        rs.getDouble("FineDebt"),
                        rs.getInt("IsBlocked") == 1
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean addPoints(String cardId, int points) {
        String sql = "UPDATE Cards SET TotalPoints = TotalPoints + ? WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setString(2, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean usePoints(String cardId, int points) {
        String sql = "UPDATE Cards SET TotalPoints = TotalPoints - ? WHERE CardID = ? AND TotalPoints >= ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setString(2, cardId);
            pstmt.setInt(3, points);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateTotalSpent(String cardId, double amount) {
        String sql = "UPDATE Cards SET TotalSpent = TotalSpent + ? WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateMemberType(String cardId, String memberType) {
        String sql = "UPDATE Cards SET MemberType = ? WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, memberType);
            pstmt.setString(2, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Calculate TotalSpent from purchase and sales history
     */
    public double calculateTotalSpentFromHistory(String cardId) {
        double totalSpent = 0;
        try (Connection conn = DBConnect.getConnection()) {
            if (conn == null) {
                return 0;
            }
            
            // Calculate from PurchaseBookHistory
            String purchaseSql = "SELECT SUM(FinalPrice) as total FROM PurchaseBookHistory WHERE CardID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(purchaseSql)) {
                pstmt.setString(1, cardId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double purchaseTotal = rs.getDouble("total");
                        if (!rs.wasNull()) {
                            totalSpent += purchaseTotal;
                        }
                    }
                }
            }
            
            // Calculate from StationerySales
            String salesSql = "SELECT SUM(FinalPrice) as total FROM StationerySales WHERE CardID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(salesSql)) {
                pstmt.setString(1, cardId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double salesTotal = rs.getDouble("total");
                        if (!rs.wasNull()) {
                            totalSpent += salesTotal;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi tinh TotalSpent: " + e.getMessage());
            e.printStackTrace();
        }
        return totalSpent;
    }
    
    /**
     * Recalculate and update TotalSpent from history
     */
    public boolean recalculateTotalSpent(String cardId) {
        double totalSpent = calculateTotalSpentFromHistory(cardId);
        String sql = "UPDATE Cards SET TotalSpent = ? WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, totalSpent);
            pstmt.setString(2, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if card already has public key stored
     */
    public boolean hasPublicKey(String cardId) {
        String sql = "SELECT CardPublicKey FROM Cards WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    byte[] pubKey = rs.getBytes("CardPublicKey");
                    return pubKey != null && pubKey.length > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Save or update card public key (BLOB) in Cards table
     * Always updates/overwrites the public key
     */
    public boolean updateCardPublicKey(String cardId, byte[] publicKeyBytes) {
        String sql = "UPDATE Cards SET CardPublicKey = ? WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBytes(1, publicKeyBytes);
            pstmt.setString(2, cardId);
            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Card public key updated in database for CardID: " + cardId);
            }
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Pay fine debt (reduce FineDebt)
     * @param cardId Card ID
     * @param amount Amount to pay (will be subtracted from FineDebt)
     * @return true if successful
     */
    public boolean payFineDebt(String cardId, double amount) {
        String sql = "UPDATE Cards SET FineDebt = FineDebt - ? WHERE CardID = ? AND FineDebt >= ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, cardId);
            pstmt.setDouble(3, amount);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Add fine debt (increase FineDebt)
     * @param cardId Card ID
     * @param amount Amount to add
     * @return true if successful
     */
    public boolean addFineDebt(String cardId, double amount) {
        String sql = "UPDATE Cards SET FineDebt = FineDebt + ? WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

