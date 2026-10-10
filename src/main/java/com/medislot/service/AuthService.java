package com.medislot.service;

import com.medislot.dao.PatientDAO;
import com.medislot.dao.UserDAO;
import com.medislot.model.Patient;
import com.medislot.model.User;
import com.medislot.util.PasswordUtil;
import com.medislot.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public User authenticate(String username, String password) throws SQLException, AuthenticationException {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
            throw new AuthenticationException("Username and password are required.");
        }

        Optional<User> optUser = userDAO.findByUsername(username.trim());
        if (optUser.isEmpty()) {
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = optUser.get();
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new AuthenticationException("Account is inactive. Please contact administrator.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        return user;
    }

    public User registerPatient(String username, String password, String firstName, String lastName,
                                String phone, String email) throws SQLException, AuthenticationException {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isValidPassword(password)) {
            throw new AuthenticationException("Username and password (min 6 chars) are required.");
        }
        if (!ValidationUtil.isNotEmpty(firstName) || !ValidationUtil.isNotEmpty(lastName)) {
            throw new AuthenticationException("First and last names are required.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new AuthenticationException("Valid phone number is required.");
        }

        if (userDAO.findByUsername(username.trim()).isPresent()) {
            throw new AuthenticationException("Username already exists.");
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        int userId = userDAO.createUser(username.trim(), passwordHash, User.Role.PATIENT);
        if (userId <= 0) {
            throw new AuthenticationException("Failed to create user account.");
        }

        Patient patient = new Patient();
        patient.setUserId(userId);
        patient.setFirstName(firstName.trim());
        patient.setLastName(lastName.trim());
        patient.setPhone(phone.trim());
        patient.setEmail(email != null ? email.trim() : null);

        patientDAO.createPatient(patient);

        User user = new User();
        user.setUserId(userId);
        user.setUsername(username.trim());
        user.setRole(User.Role.PATIENT);
        user.setStatus(User.Status.ACTIVE);
        return user;
    }

    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }
}
