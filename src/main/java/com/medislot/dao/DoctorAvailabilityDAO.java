package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.DoctorAvailability;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorAvailabilityDAO {

    public List<DoctorAvailability> getByDoctorId(int doctorId) throws SQLException {
        List<DoctorAvailability> list = new ArrayList<>();
        String sql = "SELECT * FROM doctor_availability WHERE doctor_id = ? AND is_active = TRUE ORDER BY FIELD(day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), start_time";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new DoctorAvailability(
                            rs.getInt("availability_id"),
                            rs.getInt("doctor_id"),
                            DoctorAvailability.DayOfWeek.valueOf(rs.getString("day_of_week")),
                            rs.getTime("start_time"),
                            rs.getTime("end_time"),
                            rs.getInt("slot_duration_minutes"),
                            rs.getBoolean("is_active")
                    ));
                }
            }
        }
        return list;
    }

    public boolean createAvailability(DoctorAvailability da) throws SQLException {
        String sql = "INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time, slot_duration_minutes, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, da.getDoctorId());
            stmt.setString(2, da.getDayOfWeek().name());
            stmt.setTime(3, da.getStartTime());
            stmt.setTime(4, da.getEndTime());
            stmt.setInt(5, da.getSlotDurationMinutes());
            stmt.setBoolean(6, da.isActive());
            return stmt.executeUpdate() > 0;
        }
    }
}
