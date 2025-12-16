package services;

import ui.DBConnect;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BorrowService {

    public static class BorrowRecord {
        public int id;
        public String cardId;
        public String bookId;
        public String borrowDate;
        public String dueDate;
        public String returnDate;
        public double fine;
        public String status;

        public BorrowRecord(int id, String cardId, String bookId, String borrowDate,
                String dueDate, String returnDate, double fine, String status) {
            this.id = id;
            this.cardId = cardId;
            this.bookId = bookId;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.fine = fine;
            this.status = status;
        }
    }

    /**
     * Mượn sách với đánh dấu sử dụng lượt FREE
     * 
     * @param cardId       ID thẻ
     * @param bookId       ID sách
     * @param days         Số ngày mượn
     * @param usedFreeSlot true nếu sử dụng lượt FREE, false nếu không
     */
    public boolean borrowBook(String cardId, String bookId, int days, boolean usedFreeSlot) {
        // Su dung cung mot connection cho toan bo transaction
        try (Connection conn = DBConnect.getConnection()) {
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false); // Bat dau transaction

            try {
                // Insert BorrowHistory với UsedFreeSlot
                String sql = "INSERT INTO BorrowHistory (CardID, BookID, BorrowDate, DueDate, ReturnDate, Fine, Status, UsedFreeSlot) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    java.time.LocalDate borrowDate = java.time.LocalDate.now();
                    java.time.LocalDate dueDate = borrowDate.plusDays(days);

                    pstmt.setString(1, cardId);
                    pstmt.setString(2, bookId);
                    pstmt.setString(3, borrowDate.toString());
                    pstmt.setString(4, dueDate.toString());
                    pstmt.setString(5, null);
                    pstmt.setDouble(6, 0.0);
                    pstmt.setString(7, "mượn");
                    pstmt.setInt(8, usedFreeSlot ? 1 : 0);
                    pstmt.executeUpdate();
                }

                // Update BorrowStock - su dung cung connection
                String getStockSql = "SELECT BorrowStock FROM Books WHERE BookID = ?";
                try (PreparedStatement getStockStmt = conn.prepareStatement(getStockSql)) {
                    getStockStmt.setString(1, bookId);
                    try (ResultSet rs = getStockStmt.executeQuery()) {
                        if (rs.next()) {
                            int currentStock = rs.getInt("BorrowStock");
                            String updateStockSql = "UPDATE Books SET BorrowStock = ? WHERE BookID = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                                updateStmt.setInt(1, currentStock + 1);
                                updateStmt.setString(2, bookId);
                                updateStmt.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit(); // Commit transaction
                System.out.println("[BORROW] Success: " + bookId + " (" + days + " days, FREE=" + usedFreeSlot + ")");
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Rollback neu co loi
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Loi khi muon sach: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mượn sách (backward compatible, không dùng FREE slot)
     */
    public boolean borrowBook(String cardId, String bookId, int days) {
        return borrowBook(cardId, bookId, days, false);
    }

    public boolean returnBook(int borrowId, String cardId) {
        // Su dung cung mot connection cho toan bo transaction
        try (Connection conn = DBConnect.getConnection()) {
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false); // Bat dau transaction

            try {
                // Get DueDate before updating ReturnDate - su dung cung connection
                String getDueDateSql = "SELECT DueDate FROM BorrowHistory WHERE HistoryID = ? AND CardID = ?";
                LocalDate dueDate = null;
                try (PreparedStatement getDueDateStmt = conn.prepareStatement(getDueDateSql)) {
                    getDueDateStmt.setInt(1, borrowId);
                    getDueDateStmt.setString(2, cardId);
                    try (ResultSet rs = getDueDateStmt.executeQuery()) {
                        if (rs.next()) {
                            String dueDateStr = rs.getString("DueDate");
                            if (dueDateStr != null) {
                                dueDate = LocalDate.parse(dueDateStr);
                            }
                        }
                    }
                }

                // Update BorrowHistory with ReturnDate and calculate fine
                LocalDate returnDate = LocalDate.now();
                double fine = 0.0;
                if (dueDate != null && returnDate.isAfter(dueDate)) {
                    long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
                    fine = daysLate * 1000.0; // 1000 VND per day
                }

                String sql = "UPDATE BorrowHistory SET ReturnDate = ?, Status = ?, Fine = ? WHERE HistoryID = ? AND CardID = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, returnDate.toString());
                    pstmt.setString(2, "đã trả");
                    pstmt.setDouble(3, fine);
                    pstmt.setInt(4, borrowId);
                    pstmt.setString(5, cardId);
                    pstmt.executeUpdate();
                }

                // Update FineDebt in Cards table if there's a fine - su dung cung connection
                if (fine > 0) {
                    String updateFineDebtSql = "UPDATE Cards SET FineDebt = FineDebt + ? WHERE CardID = ?";
                    try (PreparedStatement updateFineStmt = conn.prepareStatement(updateFineDebtSql)) {
                        updateFineStmt.setDouble(1, fine);
                        updateFineStmt.setString(2, cardId);
                        updateFineStmt.executeUpdate();
                    }
                }

                // Get book ID and update BorrowStock - su dung cung connection
                String getBookSql = "SELECT BookID FROM BorrowHistory WHERE HistoryID = ?";
                try (PreparedStatement getBookStmt = conn.prepareStatement(getBookSql)) {
                    getBookStmt.setInt(1, borrowId);
                    try (ResultSet rs = getBookStmt.executeQuery()) {
                        if (rs.next()) {
                            String bookId = rs.getString("BookID");

                            // Get current borrow stock
                            String getStockSql = "SELECT BorrowStock FROM Books WHERE BookID = ?";
                            try (PreparedStatement getStockStmt = conn.prepareStatement(getStockSql)) {
                                getStockStmt.setString(1, bookId);
                                try (ResultSet stockRs = getStockStmt.executeQuery()) {
                                    if (stockRs.next()) {
                                        int currentStock = stockRs.getInt("BorrowStock");
                                        if (currentStock > 0) {
                                            // Update BorrowStock - su dung cung connection
                                            String updateStockSql = "UPDATE Books SET BorrowStock = ? WHERE BookID = ?";
                                            try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                                                updateStmt.setInt(1, currentStock - 1);
                                                updateStmt.setString(2, bookId);
                                                updateStmt.executeUpdate();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
            System.err.println("Loi khi tra sach: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<BorrowRecord> getBorrowedBooksByCard(String cardId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM BorrowHistory WHERE CardID = ? AND Status = 'mượn'";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new BorrowRecord(
                            rs.getInt("HistoryID"),
                            rs.getString("CardID"),
                            rs.getString("BookID"),
                            rs.getString("BorrowDate"),
                            rs.getString("DueDate"),
                            rs.getString("ReturnDate"),
                            rs.getDouble("Fine"),
                            rs.getString("Status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public List<BorrowRecord> getAllBorrowHistory(String cardId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM BorrowHistory WHERE CardID = ? ORDER BY BorrowDate DESC";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new BorrowRecord(
                            rs.getInt("HistoryID"),
                            rs.getString("CardID"),
                            rs.getString("BookID"),
                            rs.getString("BorrowDate"),
                            rs.getString("DueDate"),
                            rs.getString("ReturnDate"),
                            rs.getDouble("Fine"),
                            rs.getString("Status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public double calculateFine(int borrowId) {
        String sql = "SELECT DueDate, ReturnDate FROM BorrowHistory WHERE HistoryID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, borrowId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dueDateStr = rs.getString("DueDate");
                    String returnDateStr = rs.getString("ReturnDate");
                    if (dueDateStr != null && returnDateStr != null) {
                        LocalDate dueDate = LocalDate.parse(dueDateStr);
                        LocalDate returnDate = LocalDate.parse(returnDateStr);
                        if (returnDate.isAfter(dueDate)) {
                            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
                            // Fine rate: 1000 VND per day (có thể lấy từ Settings)
                            return daysLate * 1000.0;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
