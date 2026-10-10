package com.medislot.ui;

import com.medislot.model.User;
import com.medislot.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("MediSlot - Hospital Appointment System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        JLabel titleLabel = new JLabel("MediSlot Portal", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 118, 210));

        JLabel subtitleLabel = new JLabel("Sign in to manage appointments & schedules", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(25, 118, 210));
        loginButton.setForeground(Color.WHITE);

        registerButton = new JButton("New Patient? Create Account");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event listeners
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.authenticate(username, password);
            JOptionPane.showMessageDialog(this,
                    "Welcome back, " + user.getUsername() + " (" + user.getRole() + ")!",
                    "Login Successful", JOptionPane.INFORMATION_MESSAGE);

            dispose();
            SwingUtilities.invokeLater(() -> new DashboardFrame(user).setVisible(true));

        } catch (AuthService.AuthenticationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        JTextField regUser = new JTextField();
        JPasswordField regPass = new JPasswordField();
        JTextField regFirst = new JTextField();
        JTextField regLast = new JTextField();
        JTextField regPhone = new JTextField();
        JTextField regEmail = new JTextField();

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("Username:")); panel.add(regUser);
        panel.add(new JLabel("Password (min 6):")); panel.add(regPass);
        panel.add(new JLabel("First Name:")); panel.add(regFirst);
        panel.add(new JLabel("Last Name:")); panel.add(regLast);
        panel.add(new JLabel("Phone:")); panel.add(regPhone);
        panel.add(new JLabel("Email:")); panel.add(regEmail);

        int result = JOptionPane.showConfirmDialog(this, panel, "Patient Registration",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                User user = authService.registerPatient(
                        regUser.getText(),
                        new String(regPass.getPassword()),
                        regFirst.getText(),
                        regLast.getText(),
                        regPhone.getText(),
                        regEmail.getText()
                );
                JOptionPane.showMessageDialog(this, "Registration successful! You may now login.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText(user.getUsername());
                passwordField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
