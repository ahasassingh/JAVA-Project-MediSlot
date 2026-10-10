package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.Slot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotDAO {

    public List<Slot> getAvailableSlotsByDoctor(int doctorId) throws SQLException {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT * FROM time_slots WHERE doctor_id = ? AND status = 'AVAILABLE' AND slot_date >= CURDATE() " +
                     "ORDER BY slot_date, start_time";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSlot(rs));
                }
            }
        }
        return list;
    }

    public List<Slot> getSlotsByDoctor(int doctorId) throws SQLException {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT * FROM time_slots WHERE doctor_id = ? ORDER BY slot_date DESC, start_time ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSlot(rs));
                }
            }
        }
        return list;
    }

    public boolean createSlot(Slot slot) throws SQLException {
        String sql = "INSERT INTO time_slots (doctor_id, slot_date, start_time, end_time, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, slot.getDoctorId());
            stmt.setDate(2, slot.getSlotDate());
            stmt.setTime(3, slot.getStartTime());
            stmt.setTime(4, slot.getEndTime());
            stmt.setString(5, slot.getStatus() != null ? slot.getStatus().name() : "AVAILABLE");
            return stmt.executeUpdate() > 0;
        }
    }

    public Optional<Slot> getSlotById(int slotId) throws SQLException {
        String sql = "SELECT * FROM time_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, slotId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSlot(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Slot mapResultSetToSlot(ResultSet rs) throws SQLException {
        return new Slot(
                rs.getInt("slot_id"),
                rs.getInt("doctor_id"),
                rs.getDate("slot_date"),
                rs.getTime("start_time"),
                rs.getTime("end_time"),
                1,
                Slot.Status.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at")
        );
    }
}
