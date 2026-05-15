package com.expensemanager.controller;

import com.expensemanager.MainApp;
import com.expensemanager.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private AuthService authService;

    public void initialize() {
        authService = new AuthService();
    }

    @FXML
    private void handleSubmit() {
        String email = emailField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        if (authService.resetPassword(email, newPassword)) {
            statusLabel.setText("Password reset successfully. Redirecting...");
            // Optionally redirect after a delay
            try {
                MainApp.setRoot("view/login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Email not found.");
        }
    }

    @FXML
    private void handleCancel() throws IOException {
        MainApp.setRoot("view/login");
    }
}
