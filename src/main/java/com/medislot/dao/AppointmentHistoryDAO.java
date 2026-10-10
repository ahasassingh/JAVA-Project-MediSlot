package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.AppointmentHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentHistoryDAO {

    public List<AppointmentHistory> getHistoryByAppointmentId(int appointmentId) throws SQLException {
        List<AppointmentHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM appointment_history WHERE appointment_id = ? ORDER BY changed_at ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AppointmentHistory(
                            rs.getInt("history_id"),
                            rs.getInt("appointment_id"),
                            rs.getString("action"),
                            rs.getString("previous_status"),
                            rs.getString("new_status"),
                            (Integer) rs.getObject("changed_by"),
                            rs.getString("remarks"),
                            rs.getTimestamp("changed_at")
                    ));
                }
            }
        }
        return list;
    }
}
