-- =====================================================================
-- Script 07: Triggers & Audit Automation
-- Project: MediSlot — Online Doctor Appointment Booking System
-- Database Engine: MySQL 8.x
-- =====================================================================

USE medislot_db;

DELIMITER $$

-- ---------------------------------------------------------------------
-- Trigger 1: trg_appointment_update_audit
-- Description: Automatically logs status changes on appointments to appointment_history
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_appointment_update_audit$$
CREATE TRIGGER trg_appointment_update_audit
AFTER UPDATE ON appointments
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO appointment_history (
            appointment_id,
            action,
            previous_status,
            new_status,
            changed_by,
            remarks
        )
        VALUES (
            NEW.appointment_id,
            CASE 
                WHEN NEW.status = 'CANCELLED' THEN 'CANCELLED'
                WHEN NEW.status = 'COMPLETED' THEN 'COMPLETED'
                ELSE 'STATUS_CHANGED'
            END,
            OLD.status,
            NEW.status,
            NULL,
            CONCAT('Status automatically transitioned from ', OLD.status, ' to ', NEW.status)
        );
    END IF;
END$$

-- ---------------------------------------------------------------------
-- Trigger 2: trg_prevent_slot_double_booking
-- Description: Business rule validation preventing booking an already BOOKED slot.
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_prevent_slot_double_booking$$
CREATE TRIGGER trg_prevent_slot_double_booking
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE v_slot_status VARCHAR(20);

    SELECT status INTO v_slot_status
    FROM time_slots
    WHERE slot_id = NEW.slot_id;

    IF v_slot_status IS NOT NULL AND v_slot_status <> 'AVAILABLE' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Validation Error: Slot is not available for booking.';
    END IF;
END$$

DELIMITER ;
