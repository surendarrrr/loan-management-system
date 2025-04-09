package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DBConnUtil {

    public static Connection getDBConnection(String connectionString) {
        Connection conn = null;

        try {
            // Parse the connection string
            String[] parts = connectionString.split(";");
            String url = parts[0].split("=", 2)[1];
            String username = parts[1].split("=", 2)[1];
            String password = parts[2].split("=", 2)[1];

            // Register JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open a connection
            conn = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

        return conn;
    }

    // Convenience method to get connection directly from properties file
    public static Connection getConnection() {
        String connectionString = DBPropertyUtil.getConnectionString("db.properties");
        return getDBConnection(connectionString);
    }

    // Method to close the connection
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
