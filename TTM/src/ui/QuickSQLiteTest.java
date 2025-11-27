/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author quang
 */
import java.sql.*;

public class QuickSQLiteTest {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:src/database/library.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Cards")) {
            while (rs.next()) {
                System.out.println("CardID: " + rs.getString("CardID") + ", FullName: " + rs.getString("FullName"));
            }
        } catch (SQLException e) {
        }
    }
}