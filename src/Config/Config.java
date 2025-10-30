package Config;

import java.sql.*;

public class Config {
    // Connection Method to SQLite
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:pcstoreDB.db"); // Database path
            System.out.println("Connection Successful");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
        return con;
    }

    // Method to add a record (insert or update)
    public void addRecord(String sql, Object... values) {
        try (Connection conn = Config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) pstmt.setInt(i + 1, (Integer) values[i]);
                else if (values[i] instanceof Double) pstmt.setDouble(i + 1, (Double) values[i]);
                else if (values[i] instanceof Float) pstmt.setFloat(i + 1, (Float) values[i]);
                else if (values[i] instanceof Long) pstmt.setLong(i + 1, (Long) values[i]);
                else if (values[i] instanceof Boolean) pstmt.setBoolean(i + 1, (Boolean) values[i]);
                else if (values[i] instanceof java.util.Date)
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
                else if (values[i] instanceof java.sql.Date) pstmt.setDate(i + 1, (java.sql.Date) values[i]);
                else if (values[i] instanceof java.sql.Timestamp)
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);
                else pstmt.setString(i + 1, values[i].toString());
            }

            pstmt.executeUpdate();
            System.out.println("Record added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

    // Method to delete a record
    public void deleteRecord(String sql, Object... params) {
        try (Connection conn = Config.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Integer) pstmt.setInt(i + 1, (Integer) params[i]);
                else if (params[i] instanceof Double) pstmt.setDouble(i + 1, (Double) params[i]);
                else if (params[i] instanceof Float) pstmt.setFloat(i + 1, (Float) params[i]);
                else if (params[i] instanceof Long) pstmt.setLong(i + 1, (Long) params[i]);
                else if (params[i] instanceof Boolean) pstmt.setBoolean(i + 1, (Boolean) params[i]);
                else if (params[i] instanceof java.util.Date)
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) params[i]).getTime()));
                else if (params[i] instanceof java.sql.Date) pstmt.setDate(i + 1, (java.sql.Date) params[i]);
                else if (params[i] instanceof java.sql.Timestamp)
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) params[i]);
                else pstmt.setString(i + 1, params[i].toString());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Record deleted successfully!");
            } else {
                System.out.println("⚠ No record found with the given criteria.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    // ✅ FIXED METHOD
    // This replaces the unsupported one and allows your Main.java to call it safely
    public static Connection connect() {
        return connectDB();
    }
}
