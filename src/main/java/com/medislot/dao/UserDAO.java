package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, role, status, created_at FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            User.Role.valueOf(rs.getString("role")),
                            User.Status.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    public int createUser(String username, String passwordHash, User.Role role) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, status) VALUES (?, ?, ?, 'ACTIVE')";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role.name());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }
}
