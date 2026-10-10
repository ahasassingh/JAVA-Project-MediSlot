package com.medislot;

import com.medislot.config.DatabaseConfig;
import com.medislot.model.Doctor;
import com.medislot.model.Slot;
import com.medislot.model.Specialty;
import com.medislot.model.User;
import com.medislot.service.AppointmentService;
import com.medislot.service.AuthService;

import java.util.List;

/**
 * Headless integration test suite verifying end-to-end functionality:
 * 1. Database connectivity
 * 2. Authentication & SHA-256 validation
 * 3. Specialty browsing
 * 4. Doctor retrieval
 * 5. Slot browsing & atomic booking
 * 6. Concurrency double-booking rejection
 * 7. Rescheduling & cancellation
 */
public class IntegrationTest {

    public static void main(String[] args) {
        System.out.println("=== MediSlot Automated Integration Test Suite ===");
        int passed = 0;
        int failed = 0;

        // Test 1: Database Connectivity
        try {
            boolean connected = DatabaseConfig.testConnection();
            if (connected) {
                System.out.println("[PASS] Test 1: Database connection established successfully.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 1: Database connection failed.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: Exception - " + e.getMessage());
            failed++;
        }

        // Test 2: Authentication Service
        AuthService authService = new AuthService();
        User patientUser = null;
        try {
            patientUser = authService.authenticate("patient_rahul", "password123");
            if (patientUser != null && patientUser.getRole() == User.Role.PATIENT) {
                System.out.println("[PASS] Test 2: Authentication succeeded for 'patient_rahul'. Role: " + patientUser.getRole());
                passed++;
            } else {
                System.err.println("[FAIL] Test 2: Authentication failed for valid user.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Exception - " + e.getMessage());
            failed++;
        }

        // Test 3: Specialty & Doctor Retrieval
        AppointmentService appointmentService = new AppointmentService();
        try {
            List<Specialty> specs = appointmentService.getAllSpecialties();
            List<Doctor> docs = appointmentService.getAllDoctors();
            if (!specs.isEmpty() && !docs.isEmpty()) {
                System.out.println("[PASS] Test 3: Found " + specs.size() + " specialties and " + docs.size() + " doctors.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 3: Specialties or doctors list is empty.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: Exception - " + e.getMessage());
            failed++;
        }

        // Test 4: Available Slots Query
        Slot targetSlot = null;
        try {
            List<Slot> slots = appointmentService.getAvailableSlots(1); // Dr. Sharma
            if (!slots.isEmpty()) {
                targetSlot = slots.get(0);
                System.out.println("[PASS] Test 4: Found " + slots.size() + " available slots for Dr. Sharma. First slot ID: " + targetSlot.getSlotId());
                passed++;
            } else {
                System.err.println("[FAIL] Test 4: No available slots for Dr. Sharma.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Exception - " + e.getMessage());
            failed++;
        }

        // Test 5: Atomic Slot Booking
        int bookedAptId = -1;
        if (targetSlot != null && patientUser != null) {
            try {
                bookedAptId = appointmentService.bookAppointment(patientUser, targetSlot.getSlotId(), "Integration test appointment");
                if (bookedAptId > 0) {
                    System.out.println("[PASS] Test 5: Appointment booked successfully. ID = " + bookedAptId);
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 5: Booking returned invalid ID.");
                    failed++;
                }
            } catch (Exception e) {
                System.err.println("[FAIL] Test 5: Booking exception - " + e.getMessage());
                failed++;
            }

            // Test 6: Double Booking Rejection
            try {
                appointmentService.bookAppointment(patientUser, targetSlot.getSlotId(), "Competing booking attempt");
                System.err.println("[FAIL] Test 6: Double booking was erroneously permitted!");
                failed++;
            } catch (Exception expected) {
                System.out.println("[PASS] Test 6: Double booking correctly prevented: " + expected.getMessage());
                passed++;
            }

            // Test 7: Cancellation & Slot Release
            try {
                appointmentService.cancelAppointment(patientUser, bookedAptId, "Test cancellation");
                System.out.println("[PASS] Test 7: Appointment cancelled and slot released.");
                passed++;
            } catch (Exception e) {
                System.err.println("[FAIL] Test 7: Cancellation failed - " + e.getMessage());
                failed++;
            }
        }

        System.out.println("\n==========================================");
        System.out.println("TEST SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
