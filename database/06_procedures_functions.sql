-- ===================================================
-- Script 06: Stored Procedures and Stored Functions
-- Project: MediSlot Hospital Appointment System
-- ===================================================

USE medislot_db;

DELIMITER $$

-- Procedure 1: Book an appointment safely
DROP PROCEDURE IF EXISTS sp_book_appointment$$
CREATE PROCEDURE sp_book_appointment(
    IN p_slot_id INT,
    IN p_patient_id INT,
    IN p_doctor_id INT,
    IN p_notes TEXT,
    OUT p_appointment_id INT,
    OUT p_status_message VARCHAR(255)
)
BEGIN
    DECLARE v_slot_status VARCHAR(20);
    DECLARE v_slot_date DATE;

    -- Check current slot status
    SELECT status, slot_date INTO v_slot_status, v_slot_date
    FROM slots
    WHERE slot_id = p_slot_id FOR UPDATE;

    IF v_slot_status IS NULL THEN
        SET p_status_message = 'ERROR: Slot does not exist';
        SET p_appointment_id = -1;
    ELSEIF v_slot_status <> 'AVAILABLE' THEN
        SET p_status_message = 'ERROR: Slot is not available';
        SET p_appointment_id = -1;
    ELSE
        -- Insert appointment
        INSERT INTO appointments (slot_id, patient_id, doctor_id, appointment_date, status, notes)
        VALUES (p_slot_id, p_patient_id, p_doctor_id, v_slot_date, 'CONFIRMED', p_notes);

        SET p_appointment_id = LAST_INSERT_ID();

        -- Mark slot as BOOKED
        UPDATE slots
        SET status = 'BOOKED'
        WHERE slot_id = p_slot_id;

        SET p_status_message = 'SUCCESS: Appointment booked successfully';
    END IF;
END$$

-- Procedure 2: Cancel an appointment
DROP PROCEDURE IF EXISTS sp_cancel_appointment$$
CREATE PROCEDURE sp_cancel_appointment(
    IN p_appointment_id INT,
    OUT p_status_message VARCHAR(255)
)
BEGIN
    DECLARE v_slot_id INT;

    SELECT slot_id INTO v_slot_id
    FROM appointments
    WHERE appointment_id = p_appointment_id;

    IF v_slot_id IS NULL THEN
        SET p_status_message = 'ERROR: Appointment not found';
    ELSE
        UPDATE appointments
        SET status = 'CANCELLED'
        WHERE appointment_id = p_appointment_id;

        UPDATE slots
        SET status = 'AVAILABLE'
        WHERE slot_id = v_slot_id;

        SET p_status_message = 'SUCCESS: Appointment cancelled and slot reopened';
    END IF;
END$$

-- Function 1: Get count of booked appointments for a doctor
DROP FUNCTION IF EXISTS fn_get_doctor_appointment_count$$
CREATE FUNCTION fn_get_doctor_appointment_count(p_doctor_id INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT DEFAULT 0;
    SELECT COUNT(*) INTO v_count
    FROM appointments
    WHERE doctor_id = p_doctor_id AND status = 'CONFIRMED';
    RETURN v_count;
END$$

DELIMITER ;
