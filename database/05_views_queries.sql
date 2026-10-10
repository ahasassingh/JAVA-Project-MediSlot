-- =====================================================================
-- Script 05: DBMS Views, Analytical Queries, and Reporting
-- Project: MediSlot — Online Doctor Appointment Booking System
-- Database Engine: MySQL 8.x
-- =====================================================================

USE medislot_db;

-- ---------------------------------------------------------------------
-- View 1: Available Doctor Slots (Active, future slots with fees & specialty)
-- Demonstrates: INNER JOIN across 3 tables (time_slots, doctors, specialties)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_available_doctor_slots AS
SELECT 
    s.slot_id,
    s.slot_date,
    s.start_time,
    s.end_time,
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    sp.specialty_name,
    d.qualification,
    d.consultation_fee,
    d.room_number
FROM time_slots s
INNER JOIN doctors d ON s.doctor_id = d.doctor_id
INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id
WHERE s.status = 'AVAILABLE' 
  AND (s.slot_date > CURDATE() OR (s.slot_date = CURDATE() AND s.start_time >= CURTIME()));

-- ---------------------------------------------------------------------
-- View 2: Complete Appointment Details
-- Demonstrates: Multi-table INNER JOIN (appointments, patients, doctors, specialties, time_slots)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_appointment_details AS
SELECT 
    a.appointment_id,
    a.appointment_date,
    s.slot_id,
    s.start_time,
    s.end_time,
    a.status AS appointment_status,
    p.patient_id,
    CONCAT(p.first_name, ' ', p.last_name) AS patient_name,
    p.phone AS patient_phone,
    p.email AS patient_email,
    p.blood_group,
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    sp.specialty_name,
    d.consultation_fee,
    d.room_number,
    a.notes,
    a.created_at AS booked_at
FROM appointments a
INNER JOIN time_slots s ON a.slot_id = s.slot_id
INNER JOIN patients p ON a.patient_id = p.patient_id
INNER JOIN doctors d ON a.doctor_id = d.doctor_id
INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id;

-- ---------------------------------------------------------------------
-- View 3: Doctor Performance & Workload Statistics
-- Demonstrates: LEFT JOIN, GROUP BY, Aggregate functions (COUNT, SUM, AVG)
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_doctor_performance AS
SELECT 
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    sp.specialty_name,
    d.consultation_fee,
    COUNT(DISTINCT s.slot_id) AS total_slots_created,
    COUNT(DISTINCT CASE WHEN a.status = 'CONFIRMED' THEN a.appointment_id END) AS confirmed_appointments,
    COUNT(DISTINCT CASE WHEN a.status = 'COMPLETED' THEN a.appointment_id END) AS completed_appointments,
    COUNT(DISTINCT CASE WHEN a.status = 'CANCELLED' THEN a.appointment_id END) AS cancelled_appointments,
    COALESCE(SUM(CASE WHEN a.status IN ('CONFIRMED', 'COMPLETED') THEN d.consultation_fee ELSE 0 END), 0) AS estimated_revenue
FROM doctors d
INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id
LEFT JOIN time_slots s ON d.doctor_id = s.doctor_id
LEFT JOIN appointments a ON s.slot_id = a.slot_id
GROUP BY d.doctor_id, d.first_name, d.last_name, sp.specialty_name, d.consultation_fee;

-- ---------------------------------------------------------------------
-- View 4: Specialty Demand Summary
-- Demonstrates: INNER JOIN, LEFT JOIN, GROUP BY, and HAVING clause
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW view_specialty_demand AS
SELECT 
    sp.specialty_id,
    sp.specialty_name,
    COUNT(DISTINCT d.doctor_id) AS total_doctors,
    COUNT(a.appointment_id) AS total_appointments_booked,
    COALESCE(SUM(CASE WHEN a.status = 'CONFIRMED' THEN d.consultation_fee ELSE 0 END), 0) AS active_booking_value
FROM specialties sp
LEFT JOIN doctors d ON sp.specialty_id = d.specialty_id
LEFT JOIN appointments a ON d.doctor_id = a.doctor_id
GROUP BY sp.specialty_id, sp.specialty_name;
