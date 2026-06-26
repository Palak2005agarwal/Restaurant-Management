package Projects.RestaurantManagement.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static String dbUrl = "jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String dbUsername = "root";
    private static String dbPassword = "root";

    static {
        Properties props = new Properties();
        boolean loaded = false;

//


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ Warning: MySQL JDBC Driver not found in classpath. Ensure mysql-connector-j is available.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {}
            }
        }
    }
}