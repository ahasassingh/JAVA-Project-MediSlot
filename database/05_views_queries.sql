-- ===================================================
-- Script 05: Database Views and Common Reporting Queries
-- Project: MediSlot Hospital Appointment System
-- ===================================================

USE medislot_db;

-- 1. View: Comprehensive Appointment Details
CREATE OR REPLACE VIEW view_appointment_details AS
SELECT 
    a.appointment_id,
    a.appointment_date,
    s.start_time,
    s.end_time,
    a.status AS appointment_status,
    p.patient_id,
    CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
    p.phone AS patient_phone,
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    d.specialization,
    d.consultation_fee,
    d.room_number,
    a.notes,
    a.booked_at
FROM appointments a
JOIN slots s ON a.slot_id = s.slot_id
JOIN patients p ON a.patient_id = p.patient_id
JOIN doctors d ON a.doctor_id = d.doctor_id;

-- 2. View: Available Doctor Slots
CREATE OR REPLACE VIEW view_available_doctor_slots AS
SELECT 
    s.slot_id,
    s.slot_date,
    s.start_time,
    s.end_time,
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    d.specialization,
    d.consultation_fee,
    d.room_number
FROM slots s
JOIN doctors d ON s.doctor_id = d.doctor_id
WHERE s.status = 'AVAILABLE' AND s.slot_date >= CURDATE();

-- 3. View: Doctor Daily Summary
CREATE OR REPLACE VIEW view_doctor_schedule_summary AS
SELECT 
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    s.slot_date,
    COUNT(s.slot_id) AS total_slots,
    SUM(CASE WHEN s.status = 'BOOKED' THEN 1 ELSE 0 END) AS booked_slots,
    SUM(CASE WHEN s.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_slots
FROM doctors d
LEFT JOIN slots s ON d.doctor_id = s.doctor_id
GROUP BY d.doctor_id, d.first_name, d.last_name, s.slot_date;
