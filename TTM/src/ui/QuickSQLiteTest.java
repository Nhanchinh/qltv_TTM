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
                System.out.println("CardID: " + rs.getString("CardID"));
                System.out.println("  FullName: " + rs.getString("FullName"));
                System.out.println("  Phone: " + rs.getString("Phone"));
                System.out.println("  Address: " + rs.getString("Address"));
                System.out.println("  DOB: " + rs.getString("DOB"));
                System.out.println("  RegisterDate: " + rs.getString("RegisterDate"));
                System.out.println("  MemberType: " + rs.getString("MemberType"));
                System.out.println("  TotalSpent: " + rs.getDouble("TotalSpent"));
                System.out.println("  TotalPoints: " + rs.getInt("TotalPoints"));
                System.out.println("  FineDebt: " + rs.getDouble("FineDebt"));
                System.out.println("  IsBlocked: " + rs.getInt("IsBlocked"));
                byte[] pubKey = rs.getBytes("CardPublicKey");
                System.out.println("  CardPublicKey: " + (pubKey != null ? pubKey.length + " bytes" : "NULL"));
                System.out.println("  CreatedAt: " + rs.getString("CreatedAt"));
                System.out.println("  UpdatedAt: " + rs.getString("UpdatedAt"));
                System.out.println("---");
            }
        } catch (SQLException e) {
        }
    }
}