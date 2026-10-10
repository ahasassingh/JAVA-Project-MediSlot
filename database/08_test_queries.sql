-- ===================================================
-- Script 08: Test Queries & Verification
-- Project: MediSlot Hospital Appointment System
-- ===================================================

USE medislot_db;

-- 1. Check all users
SELECT user_id, username, role, status, created_at FROM users;

-- 2. Check all doctors with their user login mapping
SELECT d.doctor_id, u.username, d.first_name, d.last_name, d.specialization, d.consultation_fee
FROM doctors d
JOIN users u ON d.user_id = u.user_id;

-- 3. Check all slots and their current statuses
SELECT s.slot_id, CONCAT(d.first_name, ' ', d.last_name) AS doctor_name, s.slot_date, s.start_time, s.end_time, s.status
FROM slots s
JOIN doctors d ON s.doctor_id = d.doctor_id
ORDER BY s.slot_date, s.start_time;

-- 4. Test View: view_appointment_details
SELECT * FROM view_appointment_details;

-- 5. Test Function: fn_get_doctor_appointment_count for doctor 1
SELECT fn_get_doctor_appointment_count(1) AS doctor_1_confirmed_appointments;

-- 6. Check Audit Logs populated by trigger
SELECT * FROM appointment_audit_logs;
