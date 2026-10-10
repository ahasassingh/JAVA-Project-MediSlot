package com.medislot.dao;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public int bookAppointment(int slotId, int patientId, int doctorId, String notes) throws SQLException {
        String callSql = "{CALL sp_book_appointment(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {
            cstmt.setInt(1, slotId);
            cstmt.setInt(2, patientId);
            cstmt.setInt(3, doctorId);
            cstmt.setString(4, notes);
            cstmt.registerOutParameter(5, Types.INTEGER);
            cstmt.registerOutParameter(6, Types.VARCHAR);

            cstmt.execute();
            int newId = cstmt.getInt(5);
            String message = cstmt.getString(6);
            if (newId <= 0) {
                throw new SQLException("Booking failed: " + message);
            }
            return newId;
        }
    }

    public boolean cancelAppointment(int appointmentId) throws SQLException {
        String callSql = "{CALL sp_cancel_appointment(?, ?)}";
        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {
            cstmt.setInt(1, appointmentId);
            cstmt.registerOutParameter(2, Types.VARCHAR);

            cstmt.execute();
            String msg = cstmt.getString(2);
            return msg != null && msg.startsWith("SUCCESS");
        }
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM view_appointment_details WHERE patient_id = ? ORDER BY appointment_date DESC, start_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return list;
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM view_appointment_details WHERE doctor_id = ? ORDER BY appointment_date DESC, start_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return list;
    }

    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM view_appointment_details ORDER BY appointment_date DESC, start_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        }
        return list;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment apt = new Appointment();
        apt.setAppointmentId(rs.getInt("appointment_id"));
        apt.setAppointmentDate(rs.getDate("appointment_date"));
        apt.setStartTime(rs.getTime("start_time"));
        apt.setEndTime(rs.getTime("end_time"));
        apt.setStatus(Appointment.Status.valueOf(rs.getString("appointment_status")));
        apt.setPatientId(rs.getInt("patient_id"));
        apt.setPatientName(rs.getString("patient_name"));
        apt.setDoctorId(rs.getInt("doctor_id"));
        apt.setDoctorName(rs.getString("doctor_name"));
        apt.setSpecialization(rs.getString("specialization"));
        apt.setRoomNumber(rs.getString("room_number"));
        apt.setNotes(rs.getString("notes"));
        apt.setBookedAt(rs.getTimestamp("booked_at"));
        return apt;
    }
}
