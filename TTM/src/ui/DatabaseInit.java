/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author quang
 */
package ui;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseInit {
    public static void main(String[] args) {
        try {
            // Đường dẫn tương đối bên trong project; bạn có thể đổi thành đường dẫn tuyệt đối
            Path dbDir = Paths.get("src", "database");
            // tạo thư mục nếu chưa có
            try {
                Files.createDirectories(dbDir);
            } catch (IOException ioe) {
                System.err.println("Không tạo được thư mục database: " + ioe.getMessage());
                ioe.printStackTrace(System.err);
                // vẫn tiếp tục thử tạo DB (nếu permission không cho thì sẽ ném ngoại lệ tiếp)
            }
            Path dbFile = dbDir.resolve("library.db");
            System.out.println("Đường dẫn file DB mong đợi: " + dbFile.toAbsolutePath());
            String url = "jdbc:sqlite:" + dbFile.toString();
            // try-with-resources: Connection và Statement tự đóng
            try (Connection conn = DriverManager.getConnection(url);
                 Statement st = conn.createStatement()) {
                System.out.println("Kết nối/tạo database thành công tại: " + dbFile.toAbsolutePath());
                
                // Tạo bảng Cards
                String sqlCards = """
                    CREATE TABLE IF NOT EXISTS Cards (
                        CardID TEXT PRIMARY KEY,
                        FullName TEXT,
                        Phone TEXT,
                        DOB DATE,
                        RegisterDate DATE,
                        MemberType TEXT,
                        TotalSpent REAL,
                        TotalPoints INTEGER,
                        FineDebt REAL,
                        IsBlocked INTEGER,
                        CardPublicKey BLOB,
                        CreatedAt DATETIME,
                        UpdatedAt DATETIME
                    );
                """;
                st.execute(sqlCards);
                System.out.println("Tạo bảng Cards thành công!");
                
                // Tạo bảng Books
                String sqlBooks = """
                    CREATE TABLE IF NOT EXISTS Books (
                        BookID TEXT PRIMARY KEY,
                        Title TEXT,
                        Author TEXT,
                        Publisher TEXT,
                        Price REAL,
                        Stock INTEGER,
                        BorrowStock INTEGER,
                        Category TEXT,
                        ImagePath TEXT
                    );
                """;
                st.execute(sqlBooks);
                System.out.println("Tạo bảng Books thành công!");
                
                // Tạo bảng Stationery
                String sqlStationery = """
                    CREATE TABLE IF NOT EXISTS Stationery (
                        ItemID TEXT PRIMARY KEY,
                        Name TEXT,
                        Price REAL,
                        Stock INTEGER,
                        ImagePath TEXT
                    );
                """;
                st.execute(sqlStationery);
                System.out.println("Tạo bảng Stationery thành công!");
                
                // Tạo bảng BorrowHistory
                String sqlBorrowHistory = """
                    CREATE TABLE IF NOT EXISTS BorrowHistory (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        CardID TEXT,
                        BookID TEXT,
                        BorrowDate DATE,
                        DueDate DATE,
                        ReturnDate DATE,
                        Fine REAL,
                        Status TEXT
                    );
                """;
                st.execute(sqlBorrowHistory);
                System.out.println("Tạo bảng BorrowHistory thành công!");
                
                // Tạo bảng PurchaseBookHistory
                String sqlPurchaseBookHistory = """
                    CREATE TABLE IF NOT EXISTS PurchaseBookHistory (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        CardID TEXT,
                        BookID TEXT,
                        Quantity INTEGER,
                        UnitPrice REAL,
                        DiscountPercent REAL,
                        FinalPrice REAL,
                        PointsEarned INTEGER,
                        PurchaseDate DATETIME,
                        SignatureStore BLOB,
                        SignatureCard BLOB
                    );
                """;
                st.execute(sqlPurchaseBookHistory);
                System.out.println("Tạo bảng PurchaseBookHistory thành công!");
                
                // Tạo bảng StationerySales
                String sqlStationerySales = """
                    CREATE TABLE IF NOT EXISTS StationerySales (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        CardID TEXT,
                        ItemID TEXT,
                        Quantity INTEGER,
                        UnitPrice REAL,
                        FinalPrice REAL,
                        PointsUsed INTEGER,
                        SaleDate DATETIME
                    );
                """;
                st.execute(sqlStationerySales);
                System.out.println("Tạo bảng StationerySales thành công!");
                
                // Tạo bảng Transactions
                String sqlTransactions = """
                    CREATE TABLE IF NOT EXISTS Transactions (
                        TransID TEXT PRIMARY KEY,
                        CardID TEXT,
                        Type TEXT,
                        Amount REAL,
                        PointsChanged INTEGER,
                        DateTime DATETIME,
                        SignatureCard BLOB,
                        SignatureStore BLOB
                    );
                """;
                st.execute(sqlTransactions);
                System.out.println("Tạo bảng Transactions thành công!");
                
                // Tạo bảng Fines
                String sqlFines = """
                    CREATE TABLE IF NOT EXISTS Fines (
                        ID INTEGER PRIMARY KEY AUTOINCREMENT,
                        CardID TEXT,
                        Amount REAL,
                        CreatedDate DATETIME,
                        PaidDate DATETIME,
                        TransID TEXT
                    );
                """;
                st.execute(sqlFines);
                System.out.println("Tạo bảng Fines thành công!");
                
                // Tạo bảng Settings
                String sqlSettings = """
                    CREATE TABLE IF NOT EXISTS Settings (
                        Key TEXT PRIMARY KEY,
                        Value TEXT
                    );
                """;
                st.execute(sqlSettings);
                System.out.println("Tạo bảng Settings thành công!");
                
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo DB:");
            e.printStackTrace(System.err); // in đầy đủ stack trace ra Output
        }
    }
}