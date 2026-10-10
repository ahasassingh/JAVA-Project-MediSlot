package com.medislot;

import com.medislot.ui.LoginFrame;

import javax.swing.*;

/**
 * Main application entry point for MediSlot.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize Look and Feel (FlatLaf if available, fallback to System)
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
