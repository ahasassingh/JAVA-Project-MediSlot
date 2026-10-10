-- =====================================================================
-- Script 04: Seed and Sample Mock Data
-- Project: MediSlot — Online Doctor Appointment Booking System
-- Database Engine: MySQL 8.x
-- Note: All sample passwords are 'password123' hashed with SHA-256:
--       'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'
-- =====================================================================

USE medislot_db;

-- ---------------------------------------------------------------------
-- 1. Insert Initial Users
-- ---------------------------------------------------------------------
INSERT INTO users (user_id, username, password_hash, role, status) VALUES
(1, 'admin', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN', 'ACTIVE'),
(2, 'dr_sharma', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'DOCTOR', 'ACTIVE'),
(3, 'dr_patel', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'DOCTOR', 'ACTIVE'),
(4, 'dr_menon', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'DOCTOR', 'ACTIVE'),
(5, 'patient_rahul', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'PATIENT', 'ACTIVE'),
(6, 'patient_ananya', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'PATIENT', 'ACTIVE'),
(7, 'patient_vikram', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'PATIENT', 'ACTIVE');

-- ---------------------------------------------------------------------
-- 2. Insert Specialties
-- ---------------------------------------------------------------------
INSERT INTO specialties (specialty_id, specialty_name, description) VALUES
(1, 'Cardiology', 'Heart health, cardiovascular diseases, hypertension, and ECG/ECHO care'),
(2, 'Dermatology', 'Skin, hair, nails, and cosmetic dermatology treatments'),
(3, 'Neurology', 'Brain disorders, nervous system evaluation, and migraine management'),
(4, 'Orthopedics', 'Musculoskeletal system, joint replacements, and sports injuries'),
(5, 'Pediatrics', 'Child healthcare, vaccinations, and adolescent medicine');

-- ---------------------------------------------------------------------
-- 3. Insert Doctors
-- ---------------------------------------------------------------------
INSERT INTO doctors (doctor_id, user_id, specialty_id, first_name, last_name, qualification, phone, email, consultation_fee, room_number) VALUES
(1, 2, 1, 'Rohan', 'Sharma', 'MBBS, MD (Cardiology)', '+91 9876543210', 'dr.sharma@medislot.com', 800.00, 'Room 101'),
(2, 3, 2, 'Priya', 'Patel', 'MBBS, MD (Dermatology)', '+91 9876543211', 'dr.patel@medislot.com', 600.00, 'Room 105'),
(3, 4, 3, 'Karthik', 'Menon', 'MBBS, DM (Neurology)', '+91 9876543212', 'dr.menon@medislot.com', 1000.00, 'Room 203');

-- ---------------------------------------------------------------------
-- 4. Insert Patients
-- ---------------------------------------------------------------------
INSERT INTO patients (patient_id, user_id, first_name, last_name, date_of_birth, gender, phone, email, address, blood_group) VALUES
(1, 5, 'Rahul', 'Verma', '1995-04-12', 'MALE', '+91 9123456780', 'rahul.verma@example.com', '12 MG Road, Bangalore', 'O+'),
(2, 6, 'Ananya', 'Iyer', '1998-08-25', 'FEMALE', '+91 9123456781', 'ananya.iyer@example.com', '45 Indiranagar, Bangalore', 'B+'),
(3, 7, 'Vikram', 'Singh', '1989-11-03', 'MALE', '+91 9123456782', 'vikram.singh@example.com', '78 Koramangala, Bangalore', 'A+');

-- ---------------------------------------------------------------------
-- 5. Insert Doctor Availability Templates
-- ---------------------------------------------------------------------
INSERT INTO doctor_availability (availability_id, doctor_id, day_of_week, start_time, end_time, slot_duration_minutes, is_active) VALUES
(1, 1, 'MONDAY', '09:00:00', '13:00:00', 30, TRUE),
(2, 1, 'WEDNESDAY', '09:00:00', '13:00:00', 30, TRUE),
(3, 2, 'TUESDAY', '10:00:00', '14:00:00', 30, TRUE),
(4, 2, 'THURSDAY', '10:00:00', '14:00:00', 30, TRUE),
(5, 3, 'FRIDAY', '14:00:00', '18:00:00', 30, TRUE);

-- ---------------------------------------------------------------------
-- 6. Insert Time Slots
-- ---------------------------------------------------------------------
INSERT INTO time_slots (slot_id, doctor_id, slot_date, start_time, end_time, status) VALUES
-- Dr. Sharma (Today)
(1, 1, CURDATE(), '09:00:00', '09:30:00', 'BOOKED'),
(2, 1, CURDATE(), '09:30:00', '10:00:00', 'AVAILABLE'),
(3, 1, CURDATE(), '10:00:00', '10:30:00', 'AVAILABLE'),
(4, 1, CURDATE(), '10:30:00', '11:00:00', 'AVAILABLE'),
-- Dr. Sharma (Tomorrow)
(5, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', '09:30:00', 'AVAILABLE'),
(6, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', '10:00:00', 'AVAILABLE'),
-- Dr. Patel (Today)
(7, 2, CURDATE(), '10:00:00', '10:30:00', 'BOOKED'),
(8, 2, CURDATE(), '10:30:00', '11:00:00', 'AVAILABLE'),
(9, 2, CURDATE(), '11:00:00', '11:30:00', 'AVAILABLE'),
-- Dr. Menon (Tomorrow)
(10, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '14:30:00', 'AVAILABLE'),
(11, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:30:00', '15:00:00', 'AVAILABLE');

-- ---------------------------------------------------------------------
-- 7. Insert Appointments
-- ---------------------------------------------------------------------
INSERT INTO appointments (appointment_id, slot_id, patient_id, doctor_id, appointment_date, status, notes) VALUES
(1, 1, 1, 1, CURDATE(), 'CONFIRMED', 'Chest tightness and annual cardiac checkup'),
(2, 7, 2, 2, CURDATE(), 'CONFIRMED', 'Skin rash and allergy consultation');

-- ---------------------------------------------------------------------
-- 8. Insert Initial Appointment History
-- ---------------------------------------------------------------------
INSERT INTO appointment_history (history_id, appointment_id, action, previous_status, new_status, changed_by, remarks) VALUES
(1, 1, 'BOOKED', NULL, 'CONFIRMED', 5, 'Initial booking by patient Rahul'),
(2, 2, 'BOOKED', NULL, 'CONFIRMED', 6, 'Initial booking by patient Ananya');
