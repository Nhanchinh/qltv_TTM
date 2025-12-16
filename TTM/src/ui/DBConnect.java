/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author quang
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnect {
    private static String dbPath;
    private static String url;

    static {
        try {
            // Load SQLite driver (tuy chon - JDBC 4.0+ tu dong load)
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                // Driver se tu dong load neu co trong classpath
            }

            // Lay duong dan thu muc project - su dung user.dir
            String userDir = System.getProperty("user.dir");

            Path dbDir = Paths.get(userDir, "src", "database");

            // Tao thu muc neu chua co
            if (!Files.exists(dbDir)) {
                Files.createDirectories(dbDir);
            }

            // Tao duong dan file database
            Path dbFile = dbDir.resolve("library.db");
            dbPath = dbFile.toAbsolutePath().toString();

            // Thu ca duong dan tuong doi va tuyet doi
            url = "jdbc:sqlite:" + dbPath;

            // Kiem tra va khoi tao database neu chua co
            initializeDatabase();
        } catch (Exception e) {
            System.err.println("Loi khi khoi tao database: " + e.getMessage());
            e.printStackTrace();
            // Dat url ve null de getConnection co the khoi tao lai
            url = null;
            dbPath = null;
        }
    }

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Tao cac bang neu chua co
                try (Statement stmt = conn.createStatement()) {
                    // Tạo bảng Cards
                    stmt.execute("""
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
                            """);

                    // Tạo bảng Books
                    stmt.execute("""
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
                            """);

                    // Tạo bảng Stationery
                    stmt.execute("""
                                CREATE TABLE IF NOT EXISTS Stationery (
                                    ItemID TEXT PRIMARY KEY,
                                    Name TEXT,
                                    Price REAL,
                                    Stock INTEGER,
                                    ImagePath TEXT
                                );
                            """);

                    // Tạo bảng BorrowHistory
                    stmt.execute("""
                                CREATE TABLE IF NOT EXISTS BorrowHistory (
                                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                    CardID TEXT,
                                    BookID TEXT,
                                    BorrowDate DATE,
                                    DueDate DATE,
                                    ReturnDate DATE,
                                    Fine REAL,
                                    Status TEXT,
                                    UsedFreeSlot INTEGER DEFAULT 0
                                );
                            """);

                    // Thêm cột UsedFreeSlot nếu chưa có (migration)
                    try {
                        stmt.execute("ALTER TABLE BorrowHistory ADD COLUMN UsedFreeSlot INTEGER DEFAULT 0;");
                    } catch (SQLException e) {
                        // Cột đã tồn tại, bỏ qua
                    }

                    // Tạo bảng PurchaseBookHistory
                    stmt.execute("""
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
                            """);

                    // Tạo bảng StationerySales
                    stmt.execute("""
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
                            """);

                    // Tạo bảng Transactions
                    stmt.execute("""
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
                            """);

                    // Tạo bảng Fines
                    stmt.execute("""
                                CREATE TABLE IF NOT EXISTS Fines (
                                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                    CardID TEXT,
                                    Amount REAL,
                                    CreatedDate DATETIME,
                                    PaidDate DATETIME,
                                    TransID TEXT
                                );
                            """);

                    // Tạo bảng Settings
                    stmt.execute("""
                                CREATE TABLE IF NOT EXISTS Settings (
                                    Key TEXT PRIMARY KEY,
                                    Value TEXT
                                );
                            """);

                    // Da khoi tao cac bang database
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi khoi tao database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            // Dam bao database da duoc khoi tao
            if (url == null || dbPath == null) {
                // Khoi tao lai neu chua co
                try {
                    String userDir = System.getProperty("user.dir");
                    Path dbDir = Paths.get(userDir, "src", "database");

                    if (!Files.exists(dbDir)) {
                        Files.createDirectories(dbDir);
                    }

                    Path dbFile = dbDir.resolve("library.db");
                    dbPath = dbFile.toAbsolutePath().toString();
                    url = "jdbc:sqlite:" + dbPath;

                    initializeDatabase();
                } catch (Exception e) {
                    System.err.println("Loi khi khoi tao lai database: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }

            if (url == null) {
                System.err.println("Loi: URL van la null sau khi khoi tao!");
                return null;
            }

            Connection conn = DriverManager.getConnection(url + "?journal_mode=WAL&synchronous=NORMAL");

            if (conn == null) {
                System.err.println("Loi: DriverManager.getConnection() tra ve null!");
                return null;
            }

            // Set timeout de tranh lock
            conn.setAutoCommit(true);

            // Kiem tra connection con hoat dong khong
            if (conn.isClosed()) {
                System.err.println("Loi: Connection da bi dong ngay sau khi tao!");
                return null;
            }

            return conn;

        } catch (SQLException e) {
            System.err.println("Loi khi ket noi database: " + e.getMessage());
            System.err.println("URL: " + url);
            System.err.println("DbPath: " + dbPath);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Loi khong xac dinh khi ket noi database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getDbPath() {
        return dbPath;
    }
}
