-- =====================================================================
-- Script 03: Constraints, Indexes, and Optimizations
-- Project: MediSlot — Online Doctor Appointment Booking System
-- Database Engine: MySQL 8.x
-- =====================================================================

USE medislot_db;

-- 1. Index on users role & status for rapid authentication/filtering
CREATE INDEX idx_users_role_status ON users(role, status);

-- 2. Indexes on doctors for search by specialty and name
CREATE INDEX idx_doctors_specialty ON doctors(specialty_id);
CREATE INDEX idx_doctors_name ON doctors(last_name, first_name);

-- 3. Indexes on time_slots for calendar lookup and status filtering
CREATE INDEX idx_slots_doctor_date ON time_slots(doctor_id, slot_date);
CREATE INDEX idx_slots_date_status ON time_slots(slot_date, status);

-- 4. Indexes on appointments for patient and doctor roster lookups
CREATE INDEX idx_appointments_patient ON appointments(patient_id, appointment_date);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id, appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);

-- 5. Index on appointment_history for fast audit trail retrieval
CREATE INDEX idx_history_appointment ON appointment_history(appointment_id, changed_at);
