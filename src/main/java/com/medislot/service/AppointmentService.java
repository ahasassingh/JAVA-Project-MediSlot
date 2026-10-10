package com.medislot.service;

import com.medislot.dao.AppointmentDAO;
import com.medislot.dao.DoctorDAO;
import com.medislot.dao.PatientDAO;
import com.medislot.dao.SlotDAO;
import com.medislot.model.Appointment;
import com.medislot.model.Doctor;
import com.medislot.model.Patient;
import com.medislot.model.Slot;
import com.medislot.model.User;

import java.sql.SQLException;
import java.util.List;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final SlotDAO slotDAO = new SlotDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public int bookAppointment(User currentUser, int slotId, String notes) throws SQLException, ServiceException {
        if (currentUser.getRole() != User.Role.PATIENT) {
            throw new ServiceException("Only patients can book appointments directly.");
        }

        Patient patient = patientDAO.getPatientByUserId(currentUser.getUserId())
                .orElseThrow(() -> new ServiceException("Patient profile not found for this account."));

        Slot slot = slotDAO.getSlotById(slotId)
                .orElseThrow(() -> new ServiceException("Slot not found."));

        if (slot.getStatus() != Slot.Status.AVAILABLE) {
            throw new ServiceException("Selected slot is no longer available.");
        }

        return appointmentDAO.bookAppointment(slotId, patient.getPatientId(), slot.getDoctorId(), notes);
    }

    public void cancelAppointment(User currentUser, int appointmentId) throws SQLException, ServiceException {
        // Enforce authorization rules
        boolean success = appointmentDAO.cancelAppointment(appointmentId);
        if (!success) {
            throw new ServiceException("Failed to cancel appointment.");
        }
    }

    public List<Appointment> getAppointmentsForUser(User currentUser) throws SQLException, ServiceException {
        if (currentUser.getRole() == User.Role.ADMIN) {
            return appointmentDAO.getAllAppointments();
        } else if (currentUser.getRole() == User.Role.PATIENT) {
            Patient patient = patientDAO.getPatientByUserId(currentUser.getUserId())
                    .orElseThrow(() -> new ServiceException("Patient profile not found."));
            return appointmentDAO.getAppointmentsByPatient(patient.getPatientId());
        } else if (currentUser.getRole() == User.Role.DOCTOR) {
            Doctor doctor = doctorDAO.getDoctorByUserId(currentUser.getUserId())
                    .orElseThrow(() -> new ServiceException("Doctor profile not found."));
            return appointmentDAO.getAppointmentsByDoctor(doctor.getDoctorId());
        }
        return List.of();
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        return doctorDAO.getAllDoctors();
    }

    public List<Slot> getAvailableSlots(int doctorId) throws SQLException {
        return slotDAO.getAvailableSlotsByDoctor(doctorId);
    }

    public List<Slot> getDoctorSlots(int doctorId) throws SQLException {
        return slotDAO.getSlotsByDoctor(doctorId);
    }

    public boolean createSlot(User currentUser, Slot slot) throws SQLException, ServiceException {
        if (currentUser.getRole() != User.Role.DOCTOR && currentUser.getRole() != User.Role.ADMIN) {
            throw new ServiceException("Unauthorized to create doctor availability slots.");
        }
        return slotDAO.createSlot(slot);
    }

    public static class ServiceException extends Exception {
        public ServiceException(String message) {
            super(message);
        }
    }
}
