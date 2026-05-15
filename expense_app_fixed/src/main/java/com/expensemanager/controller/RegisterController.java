package com.expensemanager.controller;

import com.expensemanager.MainApp;
import com.expensemanager.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private AuthService authService;

    public void initialize() {
        authService = new AuthService();
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        if (authService.register(name, email, password)) {
            try {
                // Auto login or redirect to login? Let's redirect to login for simplicity
                MainApp.setRoot("view/login");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Registration failed. Email might be taken.");
        }
    }

    @FXML
    private void goToLogin() throws IOException {
        MainApp.setRoot("view/login");
    }
}
