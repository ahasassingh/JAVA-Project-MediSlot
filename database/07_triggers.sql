-- ===================================================
-- Script 07: Database Triggers & Audit Logging
-- Project: MediSlot Hospital Appointment System
-- ===================================================

USE medislot_db;

-- 1. Audit Log Table
CREATE TABLE IF NOT EXISTS appointment_audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT,
    action_type VARCHAR(20) NOT NULL, -- 'INSERT', 'UPDATE_CANCEL', etc.
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$

-- Trigger 1: After Appointment Insert
DROP TRIGGER IF EXISTS trg_after_appointment_insert$$
CREATE TRIGGER trg_after_appointment_insert
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO appointment_audit_logs (appointment_id, action_type, old_status, new_status)
    VALUES (NEW.appointment_id, 'INSERT', NULL, NEW.status);
END$$

-- Trigger 2: After Appointment Update (status change tracking)
DROP TRIGGER IF EXISTS trg_after_appointment_update$$
CREATE TRIGGER trg_after_appointment_update
AFTER UPDATE ON appointments
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO appointment_audit_logs (appointment_id, action_type, old_status, new_status)
        VALUES (NEW.appointment_id, 'STATUS_UPDATE', OLD.status, NEW.status);
    END IF;
END$$

DELIMITER ;
