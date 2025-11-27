package services;

import ui.DBConnect;
import java.sql.*;

public class SettingsService {
    
    /**
     * Get a setting value by key
     */
    public String getSetting(String key) {
        String sql = "SELECT Value FROM Settings WHERE Key = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) {
                return null;
            }
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Value");
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi khi lay setting: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Set a setting value by key
     */
    public boolean setSetting(String key, String value) {
        String sql = "INSERT OR REPLACE INTO Settings (Key, Value) VALUES (?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) {
                return false;
            }
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi khi luu setting: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Initialize default PIN if not exists
     */
    public void initializeDefaultPin() {
        String currentPin = getSetting("app_pin");
        if (currentPin == null || currentPin.isEmpty()) {
            // Default PIN: 1234
            setSetting("app_pin", "1234");
        }
    }
}

