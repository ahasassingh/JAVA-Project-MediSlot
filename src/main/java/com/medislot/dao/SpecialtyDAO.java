package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.Specialty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpecialtyDAO {

    public List<Specialty> getAllSpecialties() throws SQLException {
        List<Specialty> list = new ArrayList<>();
        String sql = "SELECT specialty_id, specialty_name, description FROM specialties ORDER BY specialty_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Specialty(
                        rs.getInt("specialty_id"),
                        rs.getString("specialty_name"),
                        rs.getString("description")
                ));
            }
        }
        return list;
    }

    public Optional<Specialty> getById(int specialtyId) throws SQLException {
        String sql = "SELECT specialty_id, specialty_name, description FROM specialties WHERE specialty_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, specialtyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Specialty(
                            rs.getInt("specialty_id"),
                            rs.getString("specialty_name"),
                            rs.getString("description")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public int createSpecialty(String name, String description) throws SQLException {
        String sql = "INSERT INTO specialties (specialty_name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }
}
