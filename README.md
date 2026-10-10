# MediSlot - Hospital Appointment & Slot Booking System

MediSlot is a robust hospital doctor appointment and slot booking desktop application engineered with a clean layered architecture using **Java Swing** and **MySQL**.

---

## 📁 Project Architecture & Structure

```
MediSlot/
├── pom.xml                               # Maven build and dependency configuration
├── README.md                             # Project overview and setup instructions
├── .gitignore                            # Git ignore rules
├── database/                             # Pure SQL scripts (Completely separated from Java code)
│   ├── 01_create_database.sql            # Database creation & selection
│   ├── 02_create_tables.sql              # Core schema (users, doctors, patients, slots, appointments)
│   ├── 03_constraints_indexes.sql        # Foreign keys, unique constraints & search indexes
│   ├── 04_sample_data.sql                # Seed data for admins, doctors, patients, and slots
│   ├── 05_views_queries.sql              # Analytical views (appointment details, schedule summaries)
│   ├── 06_procedures_functions.sql       # Safe booking stored procedures & business functions
│   ├── 07_triggers.sql                   # Automated triggers & audit logging
│   └── 08_test_queries.sql               # Verification queries
├── src/main/
│   ├── java/com/medislot/
│   │   ├── Main.java                     # Application entry point
│   │   ├── config/                       # DatabaseConfig (JDBC connection management)
│   │   ├── model/                        # Domain entities (User, Doctor, Patient, Slot, Appointment)
│   │   ├── dao/                          # JDBC operations (UserDAO, DoctorDAO, PatientDAO, SlotDAO, AppointmentDAO)
│   │   ├── service/                      # Business logic & RBAC (AuthService, AppointmentService)
│   │   ├── ui/                           # Java Swing views (LoginFrame, DashboardFrame)
│   │   └── util/                         # Helpers (PasswordUtil SHA-256, ValidationUtil)
│   └── resources/
│       └── db.properties                 # DB connection properties
├── docs/
│   ├── requirements.md                   # Functional & non-functional requirements
│   ├── schema.md                         # Detailed schema & entity-relationship notes
│   └── test-cases.md                     # Test scenarios & validation matrix
└── screenshots/                          # Application screenshots
```

---

## 🛠️ Layered Architecture Details

- **`ui/` (Java Swing)**: UI screens and event handlers.
- **`service/` (Business Logic)**: Authentication checks, validation, business rules, and permissions.
- **`dao/` (Data Access Object)**: Pure JDBC queries, prepared statements, and stored procedure calls.
- **`model/` (Domain Entities)**: Plain Java models representing database records.
- **`config/` (Configuration)**: Centralized JDBC connection provider with `db.properties` support.
- **`util/` (Utilities)**: SHA-256 hashing and input validation rules.

---

## 🚀 Getting Started

### 1. Database Setup
Execute the scripts in order in MySQL Workbench or the MySQL CLI:
```bash
mysql -u root -p < database/01_create_database.sql
mysql -u root -p < database/02_create_tables.sql
mysql -u root -p < database/03_constraints_indexes.sql
mysql -u root -p < database/04_sample_data.sql
mysql -u root -p < database/05_views_queries.sql
mysql -u root -p < database/06_procedures_functions.sql
mysql -u root -p < database/07_triggers.sql
```

### 2. Configure Database Credentials
Adjust database credentials in [src/main/resources/db.properties](file:///c:/Users/Admin/Desktop/dbms+java/MediSlot/src/main/resources/db.properties):
```properties
db.url=jdbc:mysql://localhost:3306/medislot_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your_mysql_password
```

### 3. Build & Run Application
```bash
mvn clean compile
mvn exec:java
```

### 4. Default Seed Accounts
All default sample accounts use password: `password123`
- **Admin**: `admin`
- **Doctor 1**: `dr_sharma`
- **Doctor 2**: `dr_patel`
- **Patient**: `patient_rahul`
