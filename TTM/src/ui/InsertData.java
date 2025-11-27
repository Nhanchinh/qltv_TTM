/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author quang
 */
package ui;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class InsertData {
    public static void main(String[] args) {
        try {
            // Đường dẫn đến file DB
            String dbPath = Paths.get("src", "database", "library.db").toString();
            String url = "jdbc:sqlite:" + dbPath;
            System.out.println("Đường dẫn file DB: " + dbPath);

            insertSampleData(url);
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertSampleData(String url) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url)) {
            // Thêm vào bảng Cards
            String insertCard = "INSERT OR IGNORE INTO Cards (CardID, FullName, Phone, DOB, RegisterDate, MemberType, TotalSpent, TotalPoints, FineDebt, IsBlocked, CardPublicKey, CreatedAt, UpdatedAt) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertCard)) {
                ps.setString(1, "CARD001");
                ps.setString(2, "Nguyen Van A");
                ps.setString(3, "0123456789");
                ps.setString(4, "1990-01-01");
                ps.setString(5, LocalDate.now().toString());
                ps.setString(6, "Basic");
                ps.setDouble(7, 0.0);
                ps.setInt(8, 0);
                ps.setDouble(9, 0.0);
                ps.setInt(10, 0);
                ps.setBytes(11, new byte[]{});
                ps.setString(12, LocalDateTime.now().toString());
                ps.setString(13, LocalDateTime.now().toString());
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào Cards thành công!");
            }

            // Thêm một card khác để đa dạng
            try (PreparedStatement ps = conn.prepareStatement(insertCard)) {
                ps.setString(1, "CARD002");
                ps.setString(2, "Tran Thi B");
                ps.setString(3, "0987654321");
                ps.setString(4, "1985-05-15");
                ps.setString(5, LocalDate.now().toString());
                ps.setString(6, "VIP");
                ps.setDouble(7, 500000.0);
                ps.setInt(8, 500);
                ps.setDouble(9, 10000.0);
                ps.setInt(10, 0);
                ps.setBytes(11, new byte[]{});
                ps.setString(12, LocalDateTime.now().toString());
                ps.setString(13, LocalDateTime.now().toString());
                ps.executeUpdate();
            }

            // Thêm vào bảng Books
            String insertBook = "INSERT OR IGNORE INTO Books (BookID, Title, Author, Publisher, Price, Stock, BorrowStock, Category, ImagePath) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBook)) {
                ps.setString(1, "BOOK001");
                ps.setString(2, "Harry Potter");
                ps.setString(3, "J.K. Rowling");
                ps.setString(4, "Bloomsbury");
                ps.setDouble(5, 200000.0);
                ps.setInt(6, 10);
                ps.setInt(7, 0);
                ps.setString(8, "Fantasy");
                ps.setString(9, "/images/harry.jpg");
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào Books thành công!");
            }

            // Thêm một book khác
            try (PreparedStatement ps = conn.prepareStatement(insertBook)) {
                ps.setString(1, "BOOK002");
                ps.setString(2, "The Lord of the Rings");
                ps.setString(3, "J.R.R. Tolkien");
                ps.setString(4, "Allen & Unwin");
                ps.setDouble(5, 300000.0);
                ps.setInt(6, 5);
                ps.setInt(7, 1);
                ps.setString(8, "Fantasy");
                ps.setString(9, "/images/lotr.jpg");
                ps.executeUpdate();
            }

            // Thêm vào bảng Stationery
            String insertStationery = "INSERT OR IGNORE INTO Stationery (ItemID, Name, Price, Stock, ImagePath) " +
                                      "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertStationery)) {
                ps.setString(1, "ITEM001");
                ps.setString(2, "Bút chì");
                ps.setDouble(3, 5000.0);
                ps.setInt(4, 100);
                ps.setString(5, "/images/butchi.jpg");
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào Stationery thành công!");
            }

            // Thêm một item khác
            try (PreparedStatement ps = conn.prepareStatement(insertStationery)) {
                ps.setString(1, "ITEM002");
                ps.setString(2, "Sổ tay");
                ps.setDouble(3, 20000.0);
                ps.setInt(4, 50);
                ps.setString(5, "/images/sotay.jpg");
                ps.executeUpdate();
            }

            // Thêm vào bảng BorrowHistory
            String insertBorrow = "INSERT INTO BorrowHistory (CardID, BookID, BorrowDate, DueDate, ReturnDate, Fine, Status) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
                ps.setString(1, "CARD001");
                ps.setString(2, "BOOK001");
                ps.setString(3, LocalDate.now().toString());
                ps.setString(4, LocalDate.now().plusDays(7).toString());
                ps.setString(5, null);
                ps.setDouble(6, 0.0);
                ps.setString(7, "mượn");
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào BorrowHistory thành công!");
            }

            // Thêm một borrow khác
            try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
                ps.setString(1, "CARD002");
                ps.setString(2, "BOOK002");
                ps.setString(3, LocalDate.now().minusDays(10).toString());
                ps.setString(4, LocalDate.now().minusDays(3).toString());
                ps.setString(5, LocalDate.now().toString());
                ps.setDouble(6, 5000.0);
                ps.setString(7, "quá hạn");
                ps.executeUpdate();
            }

            // Thêm vào bảng PurchaseBookHistory
            String insertPurchaseBook = "INSERT INTO PurchaseBookHistory (CardID, BookID, Quantity, UnitPrice, DiscountPercent, FinalPrice, PointsEarned, PurchaseDate, SignatureStore, SignatureCard) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPurchaseBook)) {
                ps.setString(1, "CARD001");
                ps.setString(2, "BOOK001");
                ps.setInt(3, 1);
                ps.setDouble(4, 200000.0);
                ps.setDouble(5, 10.0);
                ps.setDouble(6, 180000.0);
                ps.setInt(7, 180);
                ps.setString(8, LocalDateTime.now().toString());
                ps.setBytes(9, new byte[]{});
                ps.setBytes(10, new byte[]{});
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào PurchaseBookHistory thành công!");
            }

            // Thêm một purchase khác
            try (PreparedStatement ps = conn.prepareStatement(insertPurchaseBook)) {
                ps.setString(1, "CARD002");
                ps.setString(2, "BOOK002");
                ps.setInt(3, 2);
                ps.setDouble(4, 300000.0);
                ps.setDouble(5, 5.0);
                ps.setDouble(6, 570000.0);
                ps.setInt(7, 570);
                ps.setString(8, LocalDateTime.now().toString());
                ps.setBytes(9, new byte[]{});
                ps.setBytes(10, new byte[]{});
                ps.executeUpdate();
            }

            // Thêm vào bảng StationerySales
            String insertStationerySales = "INSERT INTO StationerySales (CardID, ItemID, Quantity, UnitPrice, FinalPrice, PointsUsed, SaleDate) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertStationerySales)) {
                ps.setString(1, "CARD001");
                ps.setString(2, "ITEM001");
                ps.setInt(3, 5);
                ps.setDouble(4, 5000.0);
                ps.setDouble(5, 25000.0);
                ps.setInt(6, 0);
                ps.setString(7, LocalDateTime.now().toString());
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào StationerySales thành công!");
            }

            // Thêm một sale khác
            try (PreparedStatement ps = conn.prepareStatement(insertStationerySales)) {
                ps.setString(1, "CARD002");
                ps.setString(2, "ITEM002");
                ps.setInt(3, 3);
                ps.setDouble(4, 20000.0);
                ps.setDouble(5, 60000.0);
                ps.setInt(6, 10);
                ps.setString(7, LocalDateTime.now().toString());
                ps.executeUpdate();
            }

            // Thêm vào bảng Transactions
            String insertTransaction = "INSERT OR IGNORE INTO Transactions (TransID, CardID, Type, Amount, PointsChanged, DateTime, SignatureCard, SignatureStore) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertTransaction)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, "CARD001");
                ps.setString(3, "Deposit");
                ps.setDouble(4, 1000000.0);
                ps.setInt(5, 0);
                ps.setString(6, LocalDateTime.now().toString());
                ps.setBytes(7, new byte[]{});
                ps.setBytes(8, new byte[]{});
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào Transactions thành công!");
            }

            // Thêm một transaction khác
            try (PreparedStatement ps = conn.prepareStatement(insertTransaction)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, "CARD002");
                ps.setString(3, "Payment");
                ps.setDouble(4, -200000.0);
                ps.setInt(5, 200);
                ps.setString(6, LocalDateTime.now().toString());
                ps.setBytes(7, new byte[]{});
                ps.setBytes(8, new byte[]{});
                ps.executeUpdate();
            }

            // Thêm vào bảng Fines
            String insertFine = "INSERT INTO Fines (CardID, Amount, CreatedDate, PaidDate, TransID) " +
                                "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertFine)) {
                ps.setString(1, "CARD001");
                ps.setDouble(2, 50000.0);
                ps.setString(3, LocalDateTime.now().toString());
                ps.setString(4, null);
                ps.setString(5, UUID.randomUUID().toString()); // Giả sử TransID mới
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào Fines thành công!");
            }

            // Thêm một fine khác
            try (PreparedStatement ps = conn.prepareStatement(insertFine)) {
                ps.setString(1, "CARD002");
                ps.setDouble(2, 10000.0);
                ps.setString(3, LocalDateTime.now().toString());
                ps.setString(4, LocalDateTime.now().toString());
                ps.setString(5, UUID.randomUUID().toString());
                ps.executeUpdate();
            }

            // Thêm vào bảng Settings
            String insertSetting = "INSERT OR IGNORE INTO Settings (Key, Value) " +
                                   "VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSetting)) {
                ps.setString(1, "fine_rate_per_day");
                ps.setString(2, "1000");
                ps.executeUpdate();
                System.out.println("Thêm dữ liệu vào Settings thành công!");
            }

            // Thêm một setting khác
            try (PreparedStatement ps = conn.prepareStatement(insertSetting)) {
                ps.setString(1, "point_per_1000_vnd");
                ps.setString(2, "1");
                ps.executeUpdate();
            }
        }
    }
}