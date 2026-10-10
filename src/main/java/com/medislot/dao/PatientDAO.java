package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.Patient;

import java.sql.*;
import java.util.Optional;

public class PatientDAO {

    public Optional<Patient> getPatientByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Patient> getPatientById(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int createPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (user_id, first_name, last_name, date_of_birth, gender, phone, email, address, blood_group) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, patient.getUserId());
            stmt.setString(2, patient.getFirstName());
            stmt.setString(3, patient.getLastName());
            stmt.setDate(4, patient.getDateOfBirth());
            stmt.setString(5, patient.getGender() != null ? patient.getGender().name() : null);
            stmt.setString(6, patient.getPhone());
            stmt.setString(7, patient.getEmail());
            stmt.setString(8, patient.getAddress());
            stmt.setString(9, patient.getBloodGroup());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        String genderStr = rs.getString("gender");
        Patient.Gender gender = genderStr != null ? Patient.Gender.valueOf(genderStr) : null;
        return new Patient(
                rs.getInt("patient_id"),
                rs.getInt("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getDate("date_of_birth"),
                gender,
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("blood_group"),
                rs.getTimestamp("created_at")
        );
    }
}
