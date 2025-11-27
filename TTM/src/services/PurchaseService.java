package services;

import ui.DBConnect;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PurchaseService {
    
    public static class PurchaseRecord {
        public int id;
        public String cardId;
        public String bookId;
        public int quantity;
        public double unitPrice;
        public double discountPercent;
        public double finalPrice;
        public int pointsEarned;
        public String purchaseDate;
        
        public PurchaseRecord(int id, String cardId, String bookId, int quantity,
                             double unitPrice, double discountPercent, double finalPrice,
                             int pointsEarned, String purchaseDate) {
            this.id = id;
            this.cardId = cardId;
            this.bookId = bookId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discountPercent = discountPercent;
            this.finalPrice = finalPrice;
            this.pointsEarned = pointsEarned;
            this.purchaseDate = purchaseDate;
        }
    }
    
    public boolean purchaseBook(String cardId, String bookId, int quantity, double discountPercent) {
        try (Connection conn = DBConnect.getConnection()) {
            if (conn == null) {
                return false;
            }
            
            conn.setAutoCommit(false); // Bat dau transaction
            
            try {
                // Get book info - su dung cung connection
                String getBookSql = "SELECT * FROM Books WHERE BookID = ?";
                BookService.Book book = null;
                try (PreparedStatement getBookStmt = conn.prepareStatement(getBookSql)) {
                    getBookStmt.setString(1, bookId);
                    try (ResultSet rs = getBookStmt.executeQuery()) {
                        if (rs.next()) {
                            book = new BookService.Book(
                                rs.getString("BookID"),
                                rs.getString("Title"),
                                rs.getString("Author"),
                                rs.getString("Publisher"),
                                rs.getDouble("Price"),
                                rs.getInt("Stock"),
                                rs.getInt("BorrowStock"),
                                rs.getString("Category"),
                                rs.getString("ImagePath")
                            );
                        }
                    }
                }
                
                if (book == null || book.stock < quantity) {
                    conn.rollback();
                    return false;
                }
                
                double unitPrice = book.price;
                double finalPrice = unitPrice * quantity * (1 - discountPercent / 100.0);
                int pointsEarned = (int) (finalPrice / 1000.0); // 1 point per 1000 VND
                
                // Insert purchase history - su dung cung connection
                String sql = "INSERT INTO PurchaseBookHistory (CardID, BookID, Quantity, UnitPrice, DiscountPercent, FinalPrice, PointsEarned, PurchaseDate, SignatureStore, SignatureCard) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, cardId);
                    pstmt.setString(2, bookId);
                    pstmt.setInt(3, quantity);
                    pstmt.setDouble(4, unitPrice);
                    pstmt.setDouble(5, discountPercent);
                    pstmt.setDouble(6, finalPrice);
                    pstmt.setInt(7, pointsEarned);
                    pstmt.setString(8, LocalDateTime.now().toString());
                    pstmt.setBytes(9, new byte[]{});
                    pstmt.setBytes(10, new byte[]{});
                    pstmt.executeUpdate();
                }
                
                // Update book stock - su dung cung connection
                String updateStockSql = "UPDATE Books SET Stock = ? WHERE BookID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                    updateStmt.setInt(1, book.stock - quantity);
                    updateStmt.setString(2, bookId);
                    updateStmt.executeUpdate();
                }
                
                // Update card points and total spent - su dung cung connection
                String updateCardSql = "UPDATE Cards SET TotalPoints = TotalPoints + ?, TotalSpent = TotalSpent + ? WHERE CardID = ?";
                try (PreparedStatement updateCardStmt = conn.prepareStatement(updateCardSql)) {
                    updateCardStmt.setInt(1, pointsEarned);
                    updateCardStmt.setDouble(2, finalPrice);
                    updateCardStmt.setString(3, cardId);
                    updateCardStmt.executeUpdate();
                }
                
                // Create transaction record - su dung cung connection
                String transSql = "INSERT INTO Transactions (TransID, CardID, Type, Amount, PointsChanged, DateTime, SignatureCard, SignatureStore) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement transStmt = conn.prepareStatement(transSql)) {
                    transStmt.setString(1, UUID.randomUUID().toString());
                    transStmt.setString(2, cardId);
                    transStmt.setString(3, "Payment");
                    transStmt.setDouble(4, -finalPrice);
                    transStmt.setInt(5, pointsEarned);
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
            System.err.println("Loi khi mua sach: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public List<PurchaseRecord> getPurchaseHistory(String cardId) {
        List<PurchaseRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM PurchaseBookHistory WHERE CardID = ? ORDER BY PurchaseDate DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new PurchaseRecord(
                        rs.getInt("ID"),
                        rs.getString("CardID"),
                        rs.getString("BookID"),
                        rs.getInt("Quantity"),
                        rs.getDouble("UnitPrice"),
                        rs.getDouble("DiscountPercent"),
                        rs.getDouble("FinalPrice"),
                        rs.getInt("PointsEarned"),
                        rs.getString("PurchaseDate")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}

