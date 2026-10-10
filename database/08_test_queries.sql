-- =====================================================================
-- Script 08: Demonstration & Verification Queries
-- Project: MediSlot — Online Doctor Appointment Booking System
-- Database Engine: MySQL 8.x
-- =====================================================================

USE medislot_db;

-- ---------------------------------------------------------------------
-- Demo 1: Multi-table INNER JOIN (Comprehensive Appointment View)
-- ---------------------------------------------------------------------
SELECT * FROM view_appointment_details;

-- ---------------------------------------------------------------------
-- Demo 2: Available Slots View with Doctor Specialties
-- ---------------------------------------------------------------------
SELECT * FROM view_available_doctor_slots;

-- ---------------------------------------------------------------------
-- Demo 3: LEFT JOIN, GROUP BY, and Aggregate Reporting
-- ---------------------------------------------------------------------
SELECT * FROM view_doctor_performance;

-- ---------------------------------------------------------------------
-- Demo 4: Specialty Demand with HAVING Clause
-- ---------------------------------------------------------------------
SELECT 
    sp.specialty_name,
    COUNT(a.appointment_id) AS total_bookings,
    COALESCE(AVG(d.consultation_fee), 0) AS avg_fee
FROM specialties sp
LEFT JOIN doctors d ON sp.specialty_id = d.specialty_id
LEFT JOIN appointments a ON d.doctor_id = a.doctor_id
GROUP BY sp.specialty_id, sp.specialty_name
HAVING total_bookings >= 0;

-- ---------------------------------------------------------------------
-- Demo 5: Correlated Subquery
-- (Find doctors charging more than the average consultation fee in their specialty)
-- ---------------------------------------------------------------------
SELECT 
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    sp.specialty_name,
    d.consultation_fee
FROM doctors d
INNER JOIN specialties sp ON d.specialty_id = sp.specialty_id
WHERE d.consultation_fee >= (
    SELECT AVG(d2.consultation_fee)
    FROM doctors d2
    WHERE d2.specialty_id = d.specialty_id
);

-- ---------------------------------------------------------------------
-- Demo 6: Test Stored Function
-- ---------------------------------------------------------------------
SELECT 
    d.doctor_id,
    CONCAT('Dr. ', d.first_name, ' ', d.last_name) AS doctor_name,
    fn_calculate_doctor_revenue(d.doctor_id, '2026-01-01', '2026-12-31') AS total_revenue_2026,
    fn_is_slot_available(2) AS is_slot_2_available
FROM doctors d;

-- ---------------------------------------------------------------------
-- Demo 7: Test Trigger & Stored Procedure Execution
-- Book a new appointment via procedure and check audit history
-- ---------------------------------------------------------------------
CALL sp_book_appointment(2, 3, 'Severe recurring migraine', 7, @new_apt_id, @status_code, @status_msg);
SELECT @new_apt_id AS booked_appointment_id, @status_code AS code, @status_msg AS msg;

-- Verify audit trail wrote to appointment_history
SELECT * FROM appointment_history WHERE appointment_id = @new_apt_id;

-- Verify slot 2 is now BOOKED
SELECT slot_id, status FROM time_slots WHERE slot_id = 2;
