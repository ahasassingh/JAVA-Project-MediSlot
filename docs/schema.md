# MediSlot - Relational Schema, ER Mapping & Normalization Analysis

## 1. Entity-Relationship Overview

```
 [users] 1 ──── 1 [patients] 1 ──── 0..* [appointments] 0..* ──── 1 [time_slots]
    │                                           │                          │
    1                                           1                          │
    │                                           │                          *
 [doctors] 1 ──── 0..* [doctor_availability]    │                          │
    │  │                                        │                          │
    *  └───────────────── 0..* ─────────────────┘                          │
 [specialties]                                                             │
                                                                           │
 [appointments] 1 ──── 0..* [appointment_history]                          │
```

---

## 2. Table Specifications & DDL Outline

### 2.1 Table: `users`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `user_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | Login handle |
| `password_hash` | VARCHAR(255) | NOT NULL | SHA-256 digested hash |
| `role` | ENUM('ADMIN', 'DOCTOR', 'PATIENT') | NOT NULL | Role-Based Access Control |
| `status` | ENUM('ACTIVE', 'INACTIVE') | DEFAULT 'ACTIVE' | Account state |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |

### 2.2 Table: `patients`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `patient_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `user_id` | INT | NOT NULL, UNIQUE, FK -> `users(user_id)` ON DELETE CASCADE | 1-to-1 login mapping |
| `first_name` | VARCHAR(50) | NOT NULL | Given name |
| `last_name` | VARCHAR(50) | NOT NULL | Family name |
| `date_of_birth` | DATE | NULL | Birth date |
| `gender` | ENUM('MALE', 'FEMALE', 'OTHER') | NULL | Gender |
| `phone` | VARCHAR(20) | NOT NULL | Primary contact number |
| `email` | VARCHAR(100) | NULL | Electronic mail |
| `address` | TEXT | NULL | Residential address |
| `blood_group` | VARCHAR(5) | NULL | ABO/Rh group (e.g. A+, O-) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record created timestamp |

### 2.3 Table: `specialties`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `specialty_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `specialty_name` | VARCHAR(100) | NOT NULL, UNIQUE | Department title (e.g. Cardiology, Neurology) |
| `description` | TEXT | NULL | Specialty description |

### 2.4 Table: `doctors`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `doctor_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `user_id` | INT | NOT NULL, UNIQUE, FK -> `users(user_id)` ON DELETE CASCADE | 1-to-1 login mapping |
| `specialty_id` | INT | NOT NULL, FK -> `specialties(specialty_id)` ON DELETE RESTRICT | Department category |
| `first_name` | VARCHAR(50) | NOT NULL | Doctor given name |
| `last_name` | VARCHAR(50) | NOT NULL | Doctor family name |
| `qualification` | VARCHAR(100) | NOT NULL | E.g., MBBS, MD, MS, DM |
| `phone` | VARCHAR(20) | NOT NULL | Contact telephone |
| `email` | VARCHAR(100) | NULL | Professional email |
| `consultation_fee` | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00, CHECK (consultation_fee >= 0) | Fee per session |
| `room_number` | VARCHAR(20) | NULL | OPD chamber / room number |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |

### 2.5 Table: `doctor_availability`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `availability_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `doctor_id` | INT | NOT NULL, FK -> `doctors(doctor_id)` ON DELETE CASCADE | Assigned practitioner |
| `day_of_week` | ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') | NOT NULL | Regular working day |
| `start_time` | TIME | NOT NULL | Shift start |
| `end_time` | TIME | NOT NULL | Shift end |
| `slot_duration_minutes` | INT | DEFAULT 30, CHECK (slot_duration_minutes >= 10) | Duration per patient slot |
| `is_active` | BOOLEAN | DEFAULT TRUE | Active toggle |
| *Constraint* | `CHECK (start_time < end_time)` | Table-level check | Enforces valid time span |
| *Constraint* | `UNIQUE (doctor_id, day_of_week, start_time)` | Table-level unique | Prevents duplicate schedule rule |

### 2.6 Table: `time_slots`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `slot_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `doctor_id` | INT | NOT NULL, FK -> `doctors(doctor_id)` ON DELETE CASCADE | Doctor reference |
| `slot_date` | DATE | NOT NULL | Date of appointment |
| `start_time` | TIME | NOT NULL | Slot starting time |
| `end_time` | TIME | NOT NULL | Slot ending time |
| `status` | ENUM('AVAILABLE', 'LOCKED', 'BOOKED', 'CANCELLED') | DEFAULT 'AVAILABLE' | Current state |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| *Constraint* | `UNIQUE (doctor_id, slot_date, start_time)` | Unique constraint | Prevents duplicate slots for doctor |
| *Constraint* | `CHECK (start_time < end_time)` | Check constraint | Validates time sequence |

### 2.7 Table: `appointments`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `appointment_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `slot_id` | INT | NOT NULL, UNIQUE, FK -> `time_slots(slot_id)` ON DELETE RESTRICT | Strict 1-to-1 active booking lock |
| `patient_id` | INT | NOT NULL, FK -> `patients(patient_id)` ON DELETE CASCADE | Patient record |
| `doctor_id` | INT | NOT NULL, FK -> `doctors(doctor_id)` ON DELETE RESTRICT | Doctor record |
| `appointment_date` | DATE | NOT NULL | Date of session |
| `status` | ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') | DEFAULT 'CONFIRMED' | Booking status |
| `notes` | TEXT | NULL | Symptoms / visit remarks |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Booking timestamp |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Last modified timestamp |

### 2.8 Table: `appointment_history`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `history_id` | INT AUTO_INCREMENT | PK | Surrogate identifier |
| `appointment_id` | INT | NOT NULL, FK -> `appointments(appointment_id)` ON DELETE CASCADE | Parent appointment |
| `action` | VARCHAR(50) | NOT NULL | 'BOOKED', 'CANCELLED', 'RESCHEDULED', 'COMPLETED' |
| `previous_status` | VARCHAR(30) | NULL | Status prior to mutation |
| `new_status` | VARCHAR(30) | NOT NULL | Status after mutation |
| `changed_by` | INT | NULL, FK -> `users(user_id)` ON DELETE SET NULL | User who triggered mutation |
| `remarks` | TEXT | NULL | Explanation or audit note |
| `changed_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Timestamp of change |

---

## 3. Database Integrity & Design Decisions

### 3.1 Double-Booking Elimination Strategy (3 Layers of Defense)
1. **Database Constraint Level**: `appointments.slot_id` has a `UNIQUE` constraint. Even if two concurrent threads pass application checks, only one insert will succeed; the second will be rejected by MySQL.
2. **Database Stored Procedure Level**: `sp_book_appointment` uses `SELECT status FROM time_slots WHERE slot_id = ? FOR UPDATE`. This applies a pessimistic row-level exclusive lock, serializing competing bookings.
3. **Application Level**: JDBC transactions with `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()` inside a `try-catch` block.

### 3.2 Cancellation and Rescheduling Lifecycle
- When an appointment is marked `CANCELLED`:
  - `appointments.status` is updated to `'CANCELLED'`.
  - The associated slot in `time_slots` is updated back to `'AVAILABLE'`.
  - The `trg_appointment_status_audit` trigger automatically writes the change event into `appointment_history`.
- When an appointment is rescheduled:
  - Inside a single atomic transaction, the previous slot is updated to `'AVAILABLE'`, the new slot is locked and updated to `'BOOKED'`, and `appointments.slot_id` is updated to point to the new slot.
