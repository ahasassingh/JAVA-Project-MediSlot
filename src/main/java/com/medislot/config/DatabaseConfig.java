package com.medislot.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database configuration and provides JDBC connections.
 */
public class DatabaseConfig {

    private static String url = "jdbc:mysql://localhost:3306/medislot_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String username = "root";
    private static String password = "password";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                url = props.getProperty("db.url", url);
                username = props.getProperty("db.username", username);
                password = props.getProperty("db.password", password);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load db.properties, using defaults. Error: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath", e);
        }
        return DriverManager.getConnection(url, username, password);
    }
}
