package ui;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility to rebuild corrupted database
 */
public class RebuildDatabase {

    private static final String DB_PATH = "src/database/library.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" REBUILD DATABASE");
        System.out.println("========================================");
        System.out.println();

        try {
            // Step 1: Check if database exists
            File dbFile = new File(DB_PATH);
            if (dbFile.exists()) {
                System.out.println("[WARNING] Database already exists!");
                System.out.println("Path: " + dbFile.getAbsolutePath());
                System.out.println();
                System.out.print("Do you want to DELETE and rebuild? (yes/no): ");

                // Simple confirmation (in real app, use Scanner)
                String confirm = System.console() != null ? System.console().readLine() : "yes";

                if (!confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Operation cancelled.");
                    return;
                }

                // Backup old database
                String backupPath = DB_PATH.replace(".db", "_backup_" + System.currentTimeMillis() + ".db");
                File backupFile = new File(backupPath);
                if (dbFile.renameTo(backupFile)) {
                    System.out.println("[✓] Backed up to: " + backupPath);
                }

                // Delete corrupted database
                dbFile = new File(DB_PATH);
                if (dbFile.exists()) {
                    dbFile.delete();
                }
            }

            // Step 2: Create new database
            System.out.println();
            System.out.println("[1] Creating new database...");
            Class.forName("org.sqlite.JDBC");

            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();

            // Step 3: Create tables
            System.out.println("[2] Creating tables...");

            // Books table - Fixed schema to match InsertData.java
            stmt.execute("CREATE TABLE IF NOT EXISTS Books (" +
                    "BookID TEXT PRIMARY KEY," +
                    "Title TEXT NOT NULL," +
                    "Author TEXT," +
                    "Publisher TEXT," +
                    "Price REAL," +
                    "Stock INTEGER DEFAULT 0," +
                    "BorrowStock INTEGER DEFAULT 0," +
                    "Category TEXT," +
                    "ImagePath TEXT" +
                    ")");
            System.out.println("    ✓ Books table created");

            // Cards table
            stmt.execute("CREATE TABLE IF NOT EXISTS Cards (" +
                    "CardID TEXT PRIMARY KEY," +
                    "FullName TEXT NOT NULL," +
                    "Phone TEXT," +
                    "Address TEXT," +
                    "DOB TEXT," +
                    "RegisterDate TEXT," +
                    "MemberType TEXT DEFAULT 'Basic'," +
                    "TotalSpent REAL DEFAULT 0," +
                    "TotalPoints INTEGER DEFAULT 0," +
                    "FineDebt REAL DEFAULT 0," +
                    "IsBlocked INTEGER DEFAULT 0," +
                    "CardPublicKey BLOB," +
                    "CreatedAt TEXT," +
                    "UpdatedAt TEXT" +
                    ")");
            System.out.println("    ✓ Cards table created");

            // BorrowHistory table - Fixed schema
            stmt.execute("CREATE TABLE IF NOT EXISTS BorrowHistory (" +
                    "HistoryID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "CardID TEXT NOT NULL," +
                    "BookID TEXT NOT NULL," +
                    "BorrowDate TEXT," +
                    "DueDate TEXT," +
                    "ReturnDate TEXT," +
                    "Fine REAL DEFAULT 0," +
                    "Status TEXT DEFAULT 'mượn'," +
                    "FOREIGN KEY (CardID) REFERENCES Cards(CardID)," +
                    "FOREIGN KEY (BookID) REFERENCES Books(BookID)" +
                    ")");
            System.out.println("    ✓ BorrowHistory table created");

            // PurchaseBookHistory table (NEW)
            stmt.execute("CREATE TABLE IF NOT EXISTS PurchaseBookHistory (" +
                    "PurchaseID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "CardID TEXT NOT NULL," +
                    "BookID TEXT NOT NULL," +
                    "Quantity INTEGER DEFAULT 1," +
                    "UnitPrice REAL," +
                    "DiscountPercent REAL DEFAULT 0," +
                    "FinalPrice REAL," +
                    "PointsEarned INTEGER DEFAULT 0," +
                    "PurchaseDate TEXT," +
                    "SignatureStore BLOB," +
                    "SignatureCard BLOB," +
                    "FOREIGN KEY (CardID) REFERENCES Cards(CardID)," +
                    "FOREIGN KEY (BookID) REFERENCES Books(BookID)" +
                    ")");
            System.out.println("    ✓ PurchaseBookHistory table created");

            // Stationery table - Fixed column name
            stmt.execute("CREATE TABLE IF NOT EXISTS Stationery (" +
                    "ItemID TEXT PRIMARY KEY," +
                    "Name TEXT NOT NULL," +
                    "Price REAL," +
                    "Stock INTEGER DEFAULT 0," +
                    "ImagePath TEXT" +
                    ")");
            System.out.println("    ✓ Stationery table created");

            // StationerySales table (NEW)
            stmt.execute("CREATE TABLE IF NOT EXISTS StationerySales (" +
                    "SaleID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "CardID TEXT NOT NULL," +
                    "ItemID TEXT NOT NULL," +
                    "Quantity INTEGER DEFAULT 1," +
                    "UnitPrice REAL," +
                    "FinalPrice REAL," +
                    "PointsUsed INTEGER DEFAULT 0," +
                    "SaleDate TEXT," +
                    "FOREIGN KEY (CardID) REFERENCES Cards(CardID)," +
                    "FOREIGN KEY (ItemID) REFERENCES Stationery(ItemID)" +
                    ")");
            System.out.println("    ✓ StationerySales table created");

            // Transactions table (NEW)
            stmt.execute("CREATE TABLE IF NOT EXISTS Transactions (" +
                    "TransID TEXT PRIMARY KEY," +
                    "CardID TEXT NOT NULL," +
                    "Type TEXT," +
                    "Amount REAL," +
                    "PointsChanged INTEGER DEFAULT 0," +
                    "DateTime TEXT," +
                    "SignatureCard BLOB," +
                    "SignatureStore BLOB," +
                    "FOREIGN KEY (CardID) REFERENCES Cards(CardID)" +
                    ")");
            System.out.println("    ✓ Transactions table created");

            // Fines table (NEW)
            stmt.execute("CREATE TABLE IF NOT EXISTS Fines (" +
                    "FineID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "CardID TEXT NOT NULL," +
                    "Amount REAL," +
                    "CreatedDate TEXT," +
                    "PaidDate TEXT," +
                    "TransID TEXT," +
                    "FOREIGN KEY (CardID) REFERENCES Cards(CardID)" +
                    ")");
            System.out.println("    ✓ Fines table created");

            // Settings table (NEW)
            stmt.execute("CREATE TABLE IF NOT EXISTS Settings (" +
                    "Key TEXT PRIMARY KEY," +
                    "Value TEXT" +
                    ")");
            System.out.println("    ✓ Settings table created");

            stmt.close();
            conn.close();

            System.out.println();
            System.out.println("========================================");
            System.out.println(" DATABASE REBUILT SUCCESSFULLY!");
            System.out.println("========================================");
            System.out.println();
            System.out.println("Next steps:");
            System.out.println("1. Run DatabaseInit to verify database");
            System.out.println("2. Run InsertData to add sample data");
            System.out.println();

        } catch (Exception e) {
            System.err.println();
            System.err.println("[ERROR] Failed to rebuild database!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
