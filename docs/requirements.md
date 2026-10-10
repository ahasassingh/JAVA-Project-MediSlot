# MediSlot - System Requirements Specification (SRS) & Architecture Plan

## 1. Executive Summary
**MediSlot** is a desktop-based Online Doctor Appointment Booking System designed for a college DBMS laboratory project. It demonstrates full-stack Java (Swing + JDBC) and MySQL relational database concepts, including normalization (1NF through 3NF), atomicity, ACID transactional locking, stored procedures, functions, triggers, and analytical SQL.

---

## 2. Environment Audit & Technology Stack

| Component | Target Version | Detected on Machine | Status |
|---|---|---|---|
| **Operating System** | Windows 10/11 x64 | Windows (PowerShell) | Verified |
| **Java Platform** | Java 21+ | Java 25 LTS (`build 25.0.3+9-LTS-195`) | Compatible |
| **MySQL Server** | MySQL 8.x | MySQL Server 8.0.46 (`MySQL80` service running) | Running & Verified |
| **Database Driver** | MySQL Connector/J | `mysql-connector-j-8.0.33.jar` in local repository | Available |
| **UI Framework** | Java Swing + FlatLaf | FlatLaf 3.4 in local repository | Available |
| **Build Tool** | Apache Maven / CLI javac | `pom.xml` configured (Java 21 target); direct script & Maven ready | Configured |
| **Version Control** | Git | Git 2.53.0.2 | Initialized repository |

---

## 3. User Roles & Functional Requirements

### 3.1 Role 1: Patient
- **Registration & Authentication**: Sign up with demographic information (name, contact, DOB, gender, blood group); secure login with SHA-256 password hashing.
- **Search & Browse**: Browse doctors by specialty, qualification, consultation fee, and room number.
- **Slot Availability**: Inspect real-time available time slots per doctor for specific dates.
- **Appointment Booking**: Select an available slot and atomically book it. Protected against race conditions / double booking.
- **Reschedule & Cancel**: Reschedule to a different open slot or cancel an upcoming appointment.
- **Appointment History**: View past, confirmed, completed, and cancelled visits.

### 3.2 Role 2: Doctor
- **Authentication**: Secure role-restricted login.
- **Schedule & Availability Management**: Define recurring weekly availability (`doctor_availability`) and generate/manage specific date-time slots (`time_slots`).
- **Appointment Roster**: View scheduled patient appointments for today/upcoming dates with patient details and notes.
- **Appointment Completion**: Mark appointments as COMPLETED or CANCELLED with clinical notes.

### 3.3 Role 3: Admin
- **Specialty Management**: Add, update, and manage medical specialties (`specialties`).
- **Doctor Onboarding**: Create doctor profiles, assign specialties, consultation fees, and assign rooms.
- **User Activation**: Toggle account status (`ACTIVE` / `INACTIVE`).
- **Reporting & DBMS Analytics**: View department-wise slot utilization, doctor workloads, and cancellation rates.

---

## 4. Required Database Entities (8 Core Tables)

The database `medislot_db` contains exactly the eight required tables:

1. **`users`**: Central authentication table.
   - `user_id` (PK, INT AUTO_INCREMENT)
   - `username` (VARCHAR(50), UNIQUE, NOT NULL)
   - `password_hash` (VARCHAR(255), NOT NULL)
   - `role` (ENUM('ADMIN', 'DOCTOR', 'PATIENT'), NOT NULL)
   - `status` (ENUM('ACTIVE', 'INACTIVE'), DEFAULT 'ACTIVE')
   - `created_at` (TIMESTAMP)

2. **`patients`**: Patient personal and clinical contact details.
   - `patient_id` (PK, INT AUTO_INCREMENT)
   - `user_id` (FK -> `users.user_id`, UNIQUE, NOT NULL, ON DELETE CASCADE)
   - `first_name` (VARCHAR(50), NOT NULL)
   - `last_name` (VARCHAR(50), NOT NULL)
   - `date_of_birth` (DATE)
   - `gender` (ENUM('MALE', 'FEMALE', 'OTHER'))
   - `phone` (VARCHAR(20), NOT NULL)
   - `email` (VARCHAR(100))
   - `address` (TEXT)
   - `blood_group` (VARCHAR(5))
   - `created_at` (TIMESTAMP)

3. **`specialties`**: Medical branches / specialties.
   - `specialty_id` (PK, INT AUTO_INCREMENT)
   - `specialty_name` (VARCHAR(100), UNIQUE, NOT NULL)
   - `description` (TEXT)

4. **`doctors`**: Doctor profile and professional data.
   - `doctor_id` (PK, INT AUTO_INCREMENT)
   - `user_id` (FK -> `users.user_id`, UNIQUE, NOT NULL, ON DELETE CASCADE)
   - `specialty_id` (FK -> `specialties.specialty_id`, NOT NULL)
   - `first_name` (VARCHAR(50), NOT NULL)
   - `last_name` (VARCHAR(50), NOT NULL)
   - `qualification` (VARCHAR(100), NOT NULL)
   - `phone` (VARCHAR(20), NOT NULL)
   - `email` (VARCHAR(100))
   - `consultation_fee` (DECIMAL(10,2), NOT NULL, CHECK (consultation_fee >= 0))
   - `room_number` (VARCHAR(20))
   - `created_at` (TIMESTAMP)

5. **`doctor_availability`**: Weekly recurring schedule template.
   - `availability_id` (PK, INT AUTO_INCREMENT)
   - `doctor_id` (FK -> `doctors.doctor_id`, NOT NULL, ON DELETE CASCADE)
   - `day_of_week` (ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'), NOT NULL)
   - `start_time` (TIME, NOT NULL)
   - `end_time` (TIME, NOT NULL)
   - `slot_duration_minutes` (INT DEFAULT 30)
   - `is_active` (BOOLEAN DEFAULT TRUE)
   - **Constraint**: `CHECK (start_time < end_time)`

6. **`time_slots`**: Granular booking slot instances.
   - `slot_id` (PK, INT AUTO_INCREMENT)
   - `doctor_id` (FK -> `doctors.doctor_id`, NOT NULL, ON DELETE CASCADE)
   - `slot_date` (DATE, NOT NULL)
   - `start_time` (TIME, NOT NULL)
   - `end_time` (TIME, NOT NULL)
   - `status` (ENUM('AVAILABLE', 'LOCKED', 'BOOKED', 'CANCELLED'), DEFAULT 'AVAILABLE')
   - `created_at` (TIMESTAMP)
   - **Constraints**: UNIQUE (`doctor_id`, `slot_date`, `start_time`), `CHECK (start_time < end_time)`

7. **`appointments`**: Active booking records.
   - `appointment_id` (PK, INT AUTO_INCREMENT)
   - `slot_id` (FK -> `time_slots.slot_id`, UNIQUE, NOT NULL, ON DELETE RESTRICT)
   - `patient_id` (FK -> `patients.patient_id`, NOT NULL, ON DELETE CASCADE)
   - `doctor_id` (FK -> `doctors.doctor_id`, NOT NULL, ON DELETE RESTRICT)
   - `appointment_date` (DATE, NOT NULL)
   - `status` (ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'), DEFAULT 'CONFIRMED')
   - `notes` (TEXT)
   - `created_at` (TIMESTAMP)
   - `updated_at` (TIMESTAMP)

8. **`appointment_history`**: Audit log and status lifecycle tracker.
   - `history_id` (PK, INT AUTO_INCREMENT)
   - `appointment_id` (FK -> `appointments.appointment_id`, NOT NULL, ON DELETE CASCADE)
   - `action` (VARCHAR(50), NOT NULL) -- 'BOOKED', 'CANCELLED', 'RESCHEDULED', 'COMPLETED'
   - `previous_status` (VARCHAR(30))
   - `new_status` (VARCHAR(30))
   - `changed_by` (INT, NULLABLE FK -> `users.user_id`)
   - `remarks` (TEXT)
   - `changed_at` (TIMESTAMP DEFAULT CURRENT_TIMESTAMP)

---

## 5. Relational Analysis & Normalization (1NF through 3NF)

### 5.1 Functional Dependencies (FDs)
- **`users`**: $user\_id \rightarrow username, password\_hash, role, status, created\_at$
- **`patients`**: $patient\_id \rightarrow user\_id, first\_name, last\_name, DOB, gender, phone, email, address, blood\_group$ ($user\_id \rightarrow patient\_id$)
- **`specialties`**: $specialty\_id \rightarrow specialty\_name, description$
- **`doctors`**: $doctor\_id \rightarrow user\_id, specialty\_id, first\_name, last\_name, qualification, phone, email, fee, room\_number$ ($user\_id \rightarrow doctor\_id$)
- **`doctor_availability`**: $availability\_id \rightarrow doctor\_id, day\_of\_week, start\_time, end\_time, slot\_duration\_minutes, is\_active$
- **`time_slots`**: $slot\_id \rightarrow doctor\_id, slot\_date, start\_time, end\_time, status$ and $(doctor\_id, slot\_date, start\_time) \rightarrow slot\_id$
- **`appointments`**: $appointment\_id \rightarrow slot\_id, patient\_id, doctor\_id, appointment\_date, status, notes, created\_at$ and $slot\_id \rightarrow appointment\_id$
- **`appointment_history`**: $history\_id \rightarrow appointment\_id, action, previous\_status, new\_status, changed\_by, remarks, changed\_at$

### 5.2 Normal Form Verification
1. **1NF**: All attributes are atomic (no multi-valued lists or nested tables).
2. **2NF**: Every non-prime attribute is fully functionally dependent on the entire primary key (all single-attribute primary keys; no partial dependencies).
3. **3NF**: No transitive dependencies ($X \rightarrow Y$ and $Y \rightarrow Z$). Specialty details are factored out of `doctors` into `specialties`; user credentials are separated from patient/doctor profiles.

---

## 6. Required DBMS Demonstrations Plan

1. **DML**:
   - `INSERT`: Registration, slot generation, appointment booking, history audit.
   - `UPDATE`: Slot status transitions, rescheduling, cancellation, doctor fee/profile updates.
   - `DELETE`: Clean removal of unused slots or inactive schedules.
   - `SELECT`: Parameterized queries with filtering, ordering, and pagination.
2. **JOIN Demonstrations**:
   - `INNER JOIN`: `appointments` $\bowtie$ `patients` $\bowtie$ `doctors` $\bowtie$ `specialties` $\bowtie$ `time_slots`.
   - `LEFT JOIN`: Doctors with or without active slots; all patients with or without appointment history.
3. **Aggregates & Advanced SQL**:
   - `GROUP BY` and `HAVING`: Doctor appointments count per month (`HAVING count >= X`), revenue per specialty.
   - Subqueries: Find doctors whose consultation fee is above the specialty average; patients with appointments today.
4. **Views**:
   - `view_available_slots`: Active slots with doctor name, specialty, room, and fee.
   - `view_appointment_details`: Denormalized master view for Swing table rendering.
   - `view_doctor_performance`: Analytical aggregate view for admin reports.
5. **Stored Procedures**:
   - `sp_book_appointment`: Transactional locking (`SELECT ... FOR UPDATE`), double-booking validation, atomic insert and slot status update.
   - `sp_cancel_appointment`: Updates appointment status, reopens slot to `AVAILABLE`, and records history.
   - `sp_reschedule_appointment`: Atomically releases old slot and books new slot.
6. **Stored Function**:
   - `fn_calculate_doctor_revenue(doctor_id, start_date, end_date)`: Returns total revenue generated from completed appointments.
   - `fn_is_slot_available(slot_id)`: Checks real-time availability.
7. **Triggers**:
   - `trg_appointment_status_audit`: Automatically inserts into `appointment_history` on appointment insert/update.
   - `trg_prevent_slot_double_booking`: Validates slot status before appointment insertion.
8. **Transaction Isolation & Double-Booking Protection**:
   - Row-level locking with `SELECT ... FOR UPDATE`.
   - Isolation level `READ COMMITTED` or `SERIALIZABLE`.
   - Database unique constraint on `appointments(slot_id)` as the ultimate safeguard.

---

## 7. Phased Implementation Roadmap

- **Phase 1 (Current)**: Project scaffold, environment validation, requirements specification, schema proposal, and architectural approval.
- **Phase 2**: Update `pom.xml` to Java 21, establish `01_create_database.sql` through `04_sample_data.sql` with the 8 core tables, constraints, and seed data.
- **Phase 3**: Stored routines (`05_views_queries.sql`, `06_procedures_functions.sql`, `07_triggers.sql`, `08_test_queries.sql`), test against running MySQL 8.0 server.
- **Phase 4**: JDBC infrastructure ([DatabaseConfig.java](file:///c:/Users/Admin/Desktop/dbms+java/MediSlot/src/main/java/com/medislot/config/DatabaseConfig.java)), configuration loader, [PasswordUtil.java](file:///c:/Users/Admin/Desktop/dbms+java/MediSlot/src/main/java/com/medislot/util/PasswordUtil.java), and [AuthService.java](file:///c:/Users/Admin/Desktop/dbms+java/MediSlot/src/main/java/com/medislot/service/AuthService.java) with [LoginFrame.java](file:///c:/Users/Admin/Desktop/dbms+java/MediSlot/src/main/java/com/medislot/ui/LoginFrame.java).
- **Phase 5**: Patient booking workflow (Search doctor by specialty, view slots, atomic book/cancel/reschedule).
- **Phase 6**: Doctor & Admin dashboards (Doctor schedule builder & roster, Admin doctor management, user toggle, reports).
- **Phase 7**: End-to-end integration testing, compilation verification, documentation, and screenshots setup.
