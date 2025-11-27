package services;

import ui.DBConnect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    
    public static class Book {
        public String bookId;
        public String title;
        public String author;
        public String publisher;
        public double price;
        public int stock;
        public int borrowStock;
        public String category;
        public String imagePath;
        
        public Book(String bookId, String title, String author, String publisher, 
                   double price, int stock, int borrowStock, String category, String imagePath) {
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.publisher = publisher;
            this.price = price;
            this.stock = stock;
            this.borrowStock = borrowStock;
            this.category = category;
            this.imagePath = imagePath;
        }
    }
    
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM Books";
        try (Connection conn = DBConnect.getConnection()) {
            if (conn == null) {
                System.err.println("Loi: Khong the ket noi database!");
                return books;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    books.add(new Book(
                        rs.getString("BookID"),
                        rs.getString("Title"),
                        rs.getString("Author"),
                        rs.getString("Publisher"),
                        rs.getDouble("Price"),
                        rs.getInt("Stock"),
                        rs.getInt("BorrowStock"),
                        rs.getString("Category"),
                        rs.getString("ImagePath")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi lay danh sach sach: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Loi: Connection la null!");
            e.printStackTrace();
        }
        return books;
    }
    
    public Book getBookById(String bookId) {
        String sql = "SELECT * FROM Books WHERE BookID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean updateBookStock(String bookId, int newStock) {
        String sql = "UPDATE Books SET Stock = ? WHERE BookID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setString(2, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateBorrowStock(String bookId, int newBorrowStock) {
        String sql = "UPDATE Books SET BorrowStock = ? WHERE BookID = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newBorrowStock);
            pstmt.setString(2, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

