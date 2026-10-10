package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDAO {

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.*, sp.specialty_name " +
                     "FROM doctors d " +
                     "INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id " +
                     "ORDER BY d.first_name, d.last_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        }
        return list;
    }

    public List<Doctor> getDoctorsBySpecialty(int specialtyId) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.*, sp.specialty_name " +
                     "FROM doctors d " +
                     "INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id " +
                     "WHERE d.specialty_id = ? " +
                     "ORDER BY d.first_name, d.last_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, specialtyId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        }
        return list;
    }

    public Optional<Doctor> getDoctorById(int doctorId) throws SQLException {
        String sql = "SELECT d.*, sp.specialty_name " +
                     "FROM doctors d " +
                     "INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id " +
                     "WHERE d.doctor_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Doctor> getDoctorByUserId(int userId) throws SQLException {
        String sql = "SELECT d.*, sp.specialty_name " +
                     "FROM doctors d " +
                     "INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id " +
                     "WHERE d.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int createDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (user_id, specialty_id, first_name, last_name, qualification, phone, email, consultation_fee, room_number) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, doctor.getUserId());
            stmt.setInt(2, doctor.getSpecialtyId());
            stmt.setString(3, doctor.getFirstName());
            stmt.setString(4, doctor.getLastName());
            stmt.setString(5, doctor.getQualification());
            stmt.setString(6, doctor.getPhone());
            stmt.setString(7, doctor.getEmail());
            stmt.setBigDecimal(8, doctor.getConsultationFee());
            stmt.setString(9, doctor.getRoomNumber());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("doctor_id"),
                rs.getInt("user_id"),
                rs.getInt("specialty_id"),
                rs.getString("specialty_name"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("qualification"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getBigDecimal("consultation_fee"),
                rs.getString("room_number"),
                rs.getTimestamp("created_at")
        );
    }
}
