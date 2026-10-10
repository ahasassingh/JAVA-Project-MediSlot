# Test Cases & Verification: MediSlot

| Test Case ID | Category | Scenario / Action | Expected Result | Status |
|---|---|---|---|---|
| **TC-01** | Auth | Login with valid admin credentials (`admin` / `password123`) | Successfully navigates to Admin Dashboard with full privileges | Passed |
| **TC-02** | Auth | Login with incorrect password | Shows warning: "Invalid username or password" | Passed |
| **TC-03** | Auth | Register new patient account | Adds user and patient profile; allows immediate login | Passed |
| **TC-04** | Slots | Doctor creates availability slot (e.g. 09:00 - 09:30) | Slot saved as `AVAILABLE` and displayed in doctor list | Passed |
| **TC-05** | Booking | Patient books available doctor slot | Appointment created, slot marked `BOOKED`, shows in table | Passed |
| **TC-06** | Concurrency | Attempt to book already booked slot | Blocked by procedure/constraint with error notification | Passed |
| **TC-07** | Cancel | Patient/Doctor cancels an appointment | Status updated to `CANCELLED`, slot reopened to `AVAILABLE` | Passed |
| **TC-08** | Audit | Trigger executes on booking / cancellation | Entry created in `appointment_audit_logs` table | Passed |
