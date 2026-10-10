package com.medislot.ui;

import com.medislot.model.Appointment;
import com.medislot.model.Doctor;
import com.medislot.model.Slot;
import com.medislot.model.Specialty;
import com.medislot.model.User;
import com.medislot.service.AppointmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final User currentUser;
    private final AppointmentService appointmentService = new AppointmentService();

    private JTable appointmentsTable;
    private DefaultTableModel appointmentsModel;
    private JComboBox<Specialty> specialtyFilterComboBox;
    private JComboBox<Doctor> doctorComboBox;
    private JComboBox<Slot> slotComboBox;
    private JTextField notesField;
    private JButton bookButton;
    private JButton cancelAppointmentButton;
    private JButton rescheduleButton;
    private JButton refreshButton;
    private JButton logoutButton;

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("MediSlot Portal — " + user.getUsername() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);

        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(25, 118, 210));
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel titleLabel = new JLabel("MediSlot Clinical Management | User: " + currentUser.getUsername() + " [" + currentUser.getRole() + "]");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Appointments Table
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Active Appointments & Consultations"));

        String[] columns = {"Appt ID", "Slot ID", "Date", "Time", "Doctor", "Specialty", "Room", "Patient", "Status", "Notes"};
        appointmentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        appointmentsTable = new JTable(appointmentsModel);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.setRowHeight(24);
        centerPanel.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        // Table Action Controls
        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        refreshButton = new JButton("Refresh Roster");
        rescheduleButton = new JButton("Reschedule Selected");
        cancelAppointmentButton = new JButton("Cancel Selected Appointment");

        refreshButton.addActionListener(e -> loadAppointments());
        rescheduleButton.addActionListener(e -> handleRescheduleAppointment());
        cancelAppointmentButton.addActionListener(e -> handleCancelAppointment());

        tableControlPanel.add(refreshButton);
        if (currentUser.getRole() == User.Role.PATIENT || currentUser.getRole() == User.Role.ADMIN) {
            tableControlPanel.add(rescheduleButton);
        }
        tableControlPanel.add(cancelAppointmentButton);
        centerPanel.add(tableControlPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Role Action Panel
        if (currentUser.getRole() == User.Role.PATIENT) {
            add(createPatientBookingPanel(), BorderLayout.SOUTH);
        } else if (currentUser.getRole() == User.Role.DOCTOR) {
            add(createDoctorSlotCreationPanel(), BorderLayout.SOUTH);
        } else if (currentUser.getRole() == User.Role.ADMIN) {
            add(createAdminActionPanel(), BorderLayout.SOUTH);
        }
    }

    private JPanel createPatientBookingPanel() {
        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBorder(BorderFactory.createTitledBorder("Book New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        specialtyFilterComboBox = new JComboBox<>();
        doctorComboBox = new JComboBox<>();
        slotComboBox = new JComboBox<>();
        notesField = new JTextField(20);
        bookButton = new JButton("Confirm Booking");
        bookButton.setBackground(new Color(25, 118, 210));
        bookButton.setForeground(Color.WHITE);
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Load Specialties
        try {
            specialtyFilterComboBox.addItem(new Specialty(0, "-- All Specialties --", ""));
            List<Specialty> specs = appointmentService.getAllSpecialties();
            for (Specialty s : specs) {
                specialtyFilterComboBox.addItem(s);
            }
        } catch (Exception ex) {
            System.err.println("Could not load specialties: " + ex.getMessage());
        }

        specialtyFilterComboBox.addActionListener(e -> filterDoctorsBySpecialty());
        doctorComboBox.addActionListener(e -> loadSlotsForSelectedDoctor());
        bookButton.addActionListener(e -> handleBookAppointment());

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        bookingPanel.add(new JLabel("Specialty:"), gbc);
        gbc.gridx = 1;
        bookingPanel.add(specialtyFilterComboBox, gbc);

        gbc.gridx = 2;
        bookingPanel.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 3;
        bookingPanel.add(doctorComboBox, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        bookingPanel.add(new JLabel("Available Slot:"), gbc);
        gbc.gridx = 1;
        bookingPanel.add(slotComboBox, gbc);

        gbc.gridx = 2;
        bookingPanel.add(new JLabel("Visit Notes:"), gbc);
        gbc.gridx = 3;
        bookingPanel.add(notesField, gbc);

        gbc.gridx = 4;
        bookingPanel.add(bookButton, gbc);

        filterDoctorsBySpecialty();

        return bookingPanel;
    }

    private JPanel createDoctorSlotCreationPanel() {
        JPanel slotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        slotPanel.setBorder(BorderFactory.createTitledBorder("Doctor Schedule Manager — Add Available Time Slot"));

        JTextField dateField = new JTextField(java.time.LocalDate.now().plusDays(1).toString(), 10);
        JTextField startTimeField = new JTextField("09:00:00", 8);
        JTextField endTimeField = new JTextField("09:30:00", 8);
        JButton addSlotButton = new JButton("Publish Slot");
        addSlotButton.setBackground(new Color(46, 125, 50));
        addSlotButton.setForeground(Color.WHITE);

        slotPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        slotPanel.add(dateField);
        slotPanel.add(new JLabel("Start Time (HH:MM:SS):"));
        slotPanel.add(startTimeField);
        slotPanel.add(new JLabel("End Time (HH:MM:SS):"));
        slotPanel.add(endTimeField);
        slotPanel.add(addSlotButton);

        addSlotButton.addActionListener(e -> {
            try {
                var doctorDAO = new com.medislot.dao.DoctorDAO();
                var docOpt = doctorDAO.getDoctorByUserId(currentUser.getUserId());
                if (docOpt.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Doctor profile missing.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Slot s = new Slot();
                s.setDoctorId(docOpt.get().getDoctorId());
                s.setSlotDate(java.sql.Date.valueOf(dateField.getText().trim()));
                s.setStartTime(Time.valueOf(startTimeField.getText().trim()));
                s.setEndTime(Time.valueOf(endTimeField.getText().trim()));
                s.setMaxCapacity(1);
                s.setStatus(Slot.Status.AVAILABLE);

                appointmentService.createSlot(currentUser, s);
                JOptionPane.showMessageDialog(this, "Slot published successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to publish slot: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return slotPanel;
    }

    private JPanel createAdminActionPanel() {
        JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        adminPanel.setBorder(BorderFactory.createTitledBorder("System Administration"));

        JButton viewReportsBtn = new JButton("View Doctor Workload & Revenue Report");
        adminPanel.add(viewReportsBtn);

        viewReportsBtn.addActionListener(e -> showAdminReportsDialog());
        return adminPanel;
    }

    private void filterDoctorsBySpecialty() {
        doctorComboBox.removeAllItems();
        Specialty selectedSpec = (Specialty) specialtyFilterComboBox.getSelectedItem();

        try {
            List<Doctor> doctors;
            if (selectedSpec == null || selectedSpec.getSpecialtyId() == 0) {
                doctors = appointmentService.getAllDoctors();
            } else {
                doctors = appointmentService.getDoctorsBySpecialty(selectedSpec.getSpecialtyId());
            }

            for (Doctor d : doctors) {
                doctorComboBox.addItem(d);
            }
        } catch (Exception ex) {
            System.err.println("Could not filter doctors: " + ex.getMessage());
        }

        if (doctorComboBox.getItemCount() > 0) {
            doctorComboBox.setSelectedIndex(0);
        } else {
            slotComboBox.removeAllItems();
        }
    }

    private void loadSlotsForSelectedDoctor() {
        slotComboBox.removeAllItems();
        Doctor selected = (Doctor) doctorComboBox.getSelectedItem();
        if (selected == null) return;

        try {
            List<Slot> slots = appointmentService.getAvailableSlots(selected.getDoctorId());
            for (Slot s : slots) {
                slotComboBox.addItem(s);
            }
        } catch (Exception ex) {
            System.err.println("Error loading slots: " + ex.getMessage());
        }
    }

    private void handleBookAppointment() {
        Slot selectedSlot = (Slot) slotComboBox.getSelectedItem();
        if (selectedSlot == null) {
            JOptionPane.showMessageDialog(this, "Please select an available slot.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            appointmentService.bookAppointment(currentUser, selectedSlot.getSlotId(), notesField.getText());
            JOptionPane.showMessageDialog(this, "Appointment successfully booked via atomic transaction!", "Success", JOptionPane.INFORMATION_MESSAGE);
            notesField.setText("");
            loadSlotsForSelectedDoctor();
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRescheduleAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment to reschedule.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) appointmentsModel.getValueAt(selectedRow, 8);

        if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Cannot reschedule an appointment that is " + currentStatus + ".", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter Target Available Slot ID to reschedule into:", "Reschedule Appointment #" + appointmentId, JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            try {
                int newSlotId = Integer.parseInt(input.trim());
                appointmentService.rescheduleAppointment(currentUser, appointmentId, newSlotId);
                JOptionPane.showMessageDialog(this, "Appointment #" + appointmentId + " successfully rescheduled to slot #" + newSlotId + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments();
                if (doctorComboBox != null) loadSlotsForSelectedDoctor();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric slot ID.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Reschedule failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCancelAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsModel.getValueAt(selectedRow, 0);
        String reason = JOptionPane.showInputDialog(this, "Cancellation reason:", "Cancel Appointment #" + appointmentId, JOptionPane.QUESTION_MESSAGE);
        if (reason == null) return; // User cancelled prompt

        try {
            appointmentService.cancelAppointment(currentUser, appointmentId, reason);
            JOptionPane.showMessageDialog(this, "Appointment #" + appointmentId + " cancelled and slot released to AVAILABLE.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadAppointments();
            if (doctorComboBox != null) {
                loadSlotsForSelectedDoctor();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAdminReportsDialog() {
        JDialog dialog = new JDialog(this, "DBMS Analytical View: Doctor Workload & Revenue", true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"Doctor", "Specialty", "Fee", "Total Slots", "Confirmed", "Completed", "Cancelled", "Est. Revenue"};
        DefaultTableModel reportModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(reportModel);

        try (java.sql.Connection conn = com.medislot.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM view_doctor_performance");
             java.sql.ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reportModel.addRow(new Object[]{
                        rs.getString("doctor_name"),
                        rs.getString("specialty_name"),
                        "₹ " + rs.getBigDecimal("consultation_fee"),
                        rs.getInt("total_slots_created"),
                        rs.getInt("confirmed_appointments"),
                        rs.getInt("completed_appointments"),
                        rs.getInt("cancelled_appointments"),
                        "₹ " + rs.getBigDecimal("estimated_revenue")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching reports: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dialog.add(new JScrollPane(table));
        dialog.setVisible(true);
    }

    private void loadAppointments() {
        appointmentsModel.setRowCount(0);
        try {
            List<Appointment> list = appointmentService.getAppointmentsForUser(currentUser);
            for (Appointment a : list) {
                appointmentsModel.addRow(new Object[]{
                        a.getAppointmentId(),
                        a.getSlotId(),
                        a.getAppointmentDate(),
                        a.getStartTime() + " - " + a.getEndTime(),
                        a.getDoctorName(),
                        a.getSpecialization(),
                        a.getRoomNumber(),
                        a.getPatientName(),
                        a.getStatus().name(),
                        a.getNotes()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching appointments: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
