package com.medislot.ui;

import com.medislot.model.Appointment;
import com.medislot.model.Doctor;
import com.medislot.model.Slot;
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
    private JComboBox<Doctor> doctorComboBox;
    private JComboBox<Slot> slotComboBox;
    private JTextField notesField;
    private JButton bookButton;
    private JButton cancelAppointmentButton;
    private JButton refreshButton;
    private JButton logoutButton;

    public DashboardFrame(User user) {
        this.currentUser = user;
        setTitle("MediSlot Dashboard - " + user.getUsername() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(25, 118, 210));
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel("MediSlot Management Portal | Logged in as: " + currentUser.getUsername() + " [" + currentUser.getRole() + "]");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Appointments Table
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Your Appointments"));

        String[] columns = {"ID", "Date", "Time", "Doctor", "Specialization", "Room", "Patient", "Status", "Notes"};
        appointmentsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        appointmentsTable = new JTable(appointmentsModel);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        centerPanel.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        // Table Action Controls
        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshButton = new JButton("Refresh Table");
        cancelAppointmentButton = new JButton("Cancel Selected Appointment");

        refreshButton.addActionListener(e -> loadAppointments());
        cancelAppointmentButton.addActionListener(e -> handleCancelAppointment());

        tableControlPanel.add(refreshButton);
        tableControlPanel.add(cancelAppointmentButton);
        centerPanel.add(tableControlPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom or Side Panel: Booking Form (For Patients) or Slot Creation (For Doctors)
        if (currentUser.getRole() == User.Role.PATIENT) {
            add(createPatientBookingPanel(), BorderLayout.SOUTH);
        } else if (currentUser.getRole() == User.Role.DOCTOR) {
            add(createDoctorSlotCreationPanel(), BorderLayout.SOUTH);
        }
    }

    private JPanel createPatientBookingPanel() {
        JPanel bookingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bookingPanel.setBorder(BorderFactory.createTitledBorder("Book a New Doctor Slot"));

        doctorComboBox = new JComboBox<>();
        slotComboBox = new JComboBox<>();
        notesField = new JTextField(15);
        bookButton = new JButton("Book Appointment");

        bookingPanel.add(new JLabel("Select Doctor:"));
        bookingPanel.add(doctorComboBox);
        bookingPanel.add(new JLabel("Select Available Slot:"));
        bookingPanel.add(slotComboBox);
        bookingPanel.add(new JLabel("Notes:"));
        bookingPanel.add(notesField);
        bookingPanel.add(bookButton);

        // Load doctors
        try {
            List<Doctor> doctors = appointmentService.getAllDoctors();
            for (Doctor d : doctors) {
                doctorComboBox.addItem(d);
            }
        } catch (Exception ex) {
            System.err.println("Could not load doctors: " + ex.getMessage());
        }

        doctorComboBox.addActionListener(e -> loadSlotsForSelectedDoctor());
        if (doctorComboBox.getItemCount() > 0) {
            doctorComboBox.setSelectedIndex(0);
        }

        bookButton.addActionListener(e -> handleBookAppointment());

        return bookingPanel;
    }

    private JPanel createDoctorSlotCreationPanel() {
        JPanel slotPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        slotPanel.setBorder(BorderFactory.createTitledBorder("Add Availability Slot"));

        JTextField dateField = new JTextField(java.time.LocalDate.now().toString(), 10);
        JTextField startTimeField = new JTextField("09:00:00", 8);
        JTextField endTimeField = new JTextField("09:30:00", 8);
        JButton addSlotButton = new JButton("Create Slot");

        slotPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        slotPanel.add(dateField);
        slotPanel.add(new JLabel("Start Time (HH:MM:SS):"));
        slotPanel.add(startTimeField);
        slotPanel.add(new JLabel("End Time (HH:MM:SS):"));
        slotPanel.add(endTimeField);
        slotPanel.add(addSlotButton);

        addSlotButton.addActionListener(e -> {
            try {
                // Find doctor id for user
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
                JOptionPane.showMessageDialog(this, "Slot created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to create slot: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return slotPanel;
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
            JOptionPane.showMessageDialog(this, "Appointment successfully booked!", "Success", JOptionPane.INFORMATION_MESSAGE);
            notesField.setText("");
            loadSlotsForSelectedDoctor();
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Booking failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel appointment #" + appointmentId + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                appointmentService.cancelAppointment(currentUser, appointmentId);
                JOptionPane.showMessageDialog(this, "Appointment cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments();
                if (doctorComboBox != null) {
                    loadSlotsForSelectedDoctor();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadAppointments() {
        appointmentsModel.setRowCount(0);
        try {
            List<Appointment> list = appointmentService.getAppointmentsForUser(currentUser);
            for (Appointment a : list) {
                appointmentsModel.addRow(new Object[]{
                        a.getAppointmentId(),
                        a.getAppointmentDate(),
                        a.getStartTime() + " - " + a.getEndTime(),
                        a.getDoctorName(),
                        a.getSpecialization(),
                        a.getRoomNumber(),
                        a.getPatientName(),
                        a.getStatus(),
                        a.getNotes()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching appointments: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
