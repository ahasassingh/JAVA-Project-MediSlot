package com.medislot.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database configuration and provides thread-safe JDBC connections.
 * Credentials are read dynamically from db.properties or environment variables.
 */
public class DatabaseConfig {

    private static String url = "jdbc:mysql://localhost:3306/medislot_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String username = "root";
    private static String password = "root";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // 1. Check environment variables first (ideal for 12-factor apps / CI)
        String envUrl = System.getenv("MEDISLOT_DB_URL");
        String envUser = System.getenv("MEDISLOT_DB_USER");
        String envPass = System.getenv("MEDISLOT_DB_PASSWORD");

        if (envUrl != null) url = envUrl;
        if (envUser != null) username = envUser;
        if (envPass != null) password = envPass;

        // 2. Fallback to db.properties from classpath
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                if (envUrl == null) url = props.getProperty("db.url", url);
                if (envUser == null) username = props.getProperty("db.username", username);
                if (envPass == null) password = props.getProperty("db.password", password);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load db.properties, using configured defaults. Error: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath.", e);
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connectivity test failed: " + e.getMessage());
            return false;
        }
    }
}
