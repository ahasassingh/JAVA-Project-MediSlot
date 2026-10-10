-- =====================================================================
-- Script 06: Stored Procedures & Stored Functions
-- Project: MediSlot — Online Doctor Appointment Booking System
-- Database Engine: MySQL 8.x
-- =====================================================================

USE medislot_db;

DELIMITER $$

-- ---------------------------------------------------------------------
-- Procedure 1: sp_book_appointment
-- Description: Atomically books an appointment with pessimistic row locking.
-- Protection against race conditions and double-booking.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_book_appointment$$
CREATE PROCEDURE sp_book_appointment(
    IN p_slot_id INT,
    IN p_patient_id INT,
    IN p_notes TEXT,
    IN p_user_id INT,
    OUT p_appointment_id INT,
    OUT p_status_code INT,
    OUT p_status_message VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_slot_status VARCHAR(20);
    DECLARE v_slot_date DATE;
    DECLARE v_doctor_id INT;
    DECLARE v_existing_booking_count INT DEFAULT 0;

    -- Error Handler for unexpected exceptions
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_appointment_id = -1;
        SET p_status_code = 500;
        SET p_status_message = 'ERROR: Database transaction exception occurred.';
    END;

    START TRANSACTION;

    -- 1. Pessimistic Lock on time_slots row
    SELECT status, slot_date, doctor_id
    INTO v_slot_status, v_slot_date, v_doctor_id
    FROM time_slots
    WHERE slot_id = p_slot_id
    FOR UPDATE;

    -- 2. Validate existence of slot
    IF v_slot_status IS NULL THEN
        ROLLBACK;
        SET p_appointment_id = -1;
        SET p_status_code = 404;
        SET p_status_message = 'ERROR: Selected slot does not exist.';
        LEAVE proc_label;
    END IF;

    -- 3. Validate slot availability
    IF v_slot_status <> 'AVAILABLE' THEN
        ROLLBACK;
        SET p_appointment_id = -1;
        SET p_status_code = 409;
        SET p_status_message = 'ERROR: Slot is no longer available (already booked or cancelled).';
        LEAVE proc_label;
    END IF;

    -- 4. Check for existing active booking in appointments table
    SELECT COUNT(*) INTO v_existing_booking_count
    FROM appointments
    WHERE slot_id = p_slot_id AND status IN ('PENDING', 'CONFIRMED');

    IF v_existing_booking_count > 0 THEN
        ROLLBACK;
        SET p_appointment_id = -1;
        SET p_status_code = 409;
        SET p_status_message = 'ERROR: Duplicate booking detected for this slot.';
        LEAVE proc_label;
    END IF;

    -- 5. Insert new appointment
    INSERT INTO appointments (slot_id, patient_id, doctor_id, appointment_date, status, notes)
    VALUES (p_slot_id, p_patient_id, v_doctor_id, v_slot_date, 'CONFIRMED', p_notes);

    SET p_appointment_id = LAST_INSERT_ID();

    -- 6. Update slot status to BOOKED
    UPDATE time_slots
    SET status = 'BOOKED'
    WHERE slot_id = p_slot_id;

    -- 7. Record initial lifecycle history (if not recorded by trigger)
    INSERT INTO appointment_history (appointment_id, action, previous_status, new_status, changed_by, remarks)
    VALUES (p_appointment_id, 'BOOKED', NULL, 'CONFIRMED', p_user_id, 'Booked via sp_book_appointment');

    COMMIT;
    SET p_status_code = 200;
    SET p_status_message = 'SUCCESS: Appointment confirmed successfully.';
END proc_label$$

-- ---------------------------------------------------------------------
-- Procedure 2: sp_cancel_appointment
-- Description: Atomically cancels an appointment and restores the time slot.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_cancel_appointment$$
CREATE PROCEDURE sp_cancel_appointment(
    IN p_appointment_id INT,
    IN p_user_id INT,
    IN p_reason TEXT,
    OUT p_status_code INT,
    OUT p_status_message VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_slot_id INT;
    DECLARE v_current_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status_code = 500;
        SET p_status_message = 'ERROR: Failed to cancel appointment due to database error.';
    END;

    START TRANSACTION;

    SELECT slot_id, status INTO v_slot_id, v_current_status
    FROM appointments
    WHERE appointment_id = p_appointment_id
    FOR UPDATE;

    IF v_slot_id IS NULL THEN
        ROLLBACK;
        SET p_status_code = 404;
        SET p_status_message = 'ERROR: Appointment record not found.';
        LEAVE proc_label;
    END IF;

    IF v_current_status = 'CANCELLED' THEN
        ROLLBACK;
        SET p_status_code = 400;
        SET p_status_message = 'NOTICE: Appointment is already cancelled.';
        LEAVE proc_label;
    END IF;

    -- Update appointment
    UPDATE appointments
    SET status = 'CANCELLED'
    WHERE appointment_id = p_appointment_id;

    -- Re-open slot to AVAILABLE
    UPDATE time_slots
    SET status = 'AVAILABLE'
    WHERE slot_id = v_slot_id;

    -- Record in appointment_history
    INSERT INTO appointment_history (appointment_id, action, previous_status, new_status, changed_by, remarks)
    VALUES (p_appointment_id, 'CANCELLED', v_current_status, 'CANCELLED', p_user_id, p_reason);

    COMMIT;
    SET p_status_code = 200;
    SET p_status_message = 'SUCCESS: Appointment cancelled and slot released to AVAILABLE.';
END proc_label$$

-- ---------------------------------------------------------------------
-- Procedure 3: sp_reschedule_appointment
-- Description: Atomically transitions an appointment from old slot to new slot.
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_reschedule_appointment$$
CREATE PROCEDURE sp_reschedule_appointment(
    IN p_appointment_id INT,
    IN p_new_slot_id INT,
    IN p_user_id INT,
    OUT p_status_code INT,
    OUT p_status_message VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_old_slot_id INT;
    DECLARE v_current_status VARCHAR(20);
    DECLARE v_new_slot_status VARCHAR(20);
    DECLARE v_new_slot_date DATE;
    DECLARE v_new_doctor_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status_code = 500;
        SET p_status_message = 'ERROR: Transaction failure during rescheduling.';
    END;

    START TRANSACTION;

    -- 1. Lock existing appointment
    SELECT slot_id, status INTO v_old_slot_id, v_current_status
    FROM appointments
    WHERE appointment_id = p_appointment_id
    FOR UPDATE;

    IF v_old_slot_id IS NULL THEN
        ROLLBACK;
        SET p_status_code = 404;
        SET p_status_message = 'ERROR: Appointment not found.';
        LEAVE proc_label;
    END IF;

    IF v_current_status = 'CANCELLED' OR v_current_status = 'COMPLETED' THEN
        ROLLBACK;
        SET p_status_code = 400;
        SET p_status_message = 'ERROR: Cannot reschedule a cancelled or completed appointment.';
        LEAVE proc_label;
    END IF;

    -- 2. Lock and check new slot
    SELECT status, slot_date, doctor_id
    INTO v_new_slot_status, v_new_slot_date, v_new_doctor_id
    FROM time_slots
    WHERE slot_id = p_new_slot_id
    FOR UPDATE;

    IF v_new_slot_status IS NULL OR v_new_slot_status <> 'AVAILABLE' THEN
        ROLLBACK;
        SET p_status_code = 409;
        SET p_status_message = 'ERROR: The requested new slot is not available.';
        LEAVE proc_label;
    END IF;

    -- 3. Release old slot
    UPDATE time_slots
    SET status = 'AVAILABLE'
    WHERE slot_id = v_old_slot_id;

    -- 4. Reserve new slot
    UPDATE time_slots
    SET status = 'BOOKED'
    WHERE slot_id = p_new_slot_id;

    -- 5. Update appointment
    UPDATE appointments
    SET slot_id = p_new_slot_id,
        appointment_date = v_new_slot_date,
        doctor_id = v_new_doctor_id,
        status = 'CONFIRMED'
    WHERE appointment_id = p_appointment_id;

    -- 6. Audit entry
    INSERT INTO appointment_history (appointment_id, action, previous_status, new_status, changed_by, remarks)
    VALUES (p_appointment_id, 'RESCHEDULED', v_current_status, 'CONFIRMED', p_user_id,
            CONCAT('Rescheduled from slot #', v_old_slot_id, ' to slot #', p_new_slot_id));

    COMMIT;
    SET p_status_code = 200;
    SET p_status_message = 'SUCCESS: Appointment rescheduled successfully.';
END proc_label$$

-- ---------------------------------------------------------------------
-- Function 1: fn_calculate_doctor_revenue
-- Description: Computes cumulative revenue collected by a doctor in a date range.
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_calculate_doctor_revenue$$
CREATE FUNCTION fn_calculate_doctor_revenue(
    p_doctor_id INT,
    p_start_date DATE,
    p_end_date DATE
)
RETURNS DECIMAL(10, 2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_total DECIMAL(10, 2) DEFAULT 0.00;

    SELECT COALESCE(SUM(d.consultation_fee), 0.00)
    INTO v_total
    FROM appointments a
    INNER JOIN doctors d ON a.doctor_id = d.doctor_id
    WHERE a.doctor_id = p_doctor_id
      AND a.status IN ('CONFIRMED', 'COMPLETED')
      AND a.appointment_date BETWEEN p_start_date AND p_end_date;

    RETURN v_total;
END$$

-- ---------------------------------------------------------------------
-- Function 2: fn_is_slot_available
-- Description: Boolean helper returning 1 if slot is available, 0 otherwise.
-- ---------------------------------------------------------------------
DROP FUNCTION IF EXISTS fn_is_slot_available$$
CREATE FUNCTION fn_is_slot_available(p_slot_id INT)
RETURNS TINYINT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_status VARCHAR(20);
    SELECT status INTO v_status FROM time_slots WHERE slot_id = p_slot_id;
    IF v_status = 'AVAILABLE' THEN
        RETURN 1;
    ELSE
        RETURN 0;
    END IF;
END$$

DELIMITER ;
