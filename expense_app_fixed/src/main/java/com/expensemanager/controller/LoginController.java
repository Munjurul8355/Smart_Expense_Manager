package com.expensemanager.controller;

import com.expensemanager.MainApp;
import com.expensemanager.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private FontIcon eyeIcon;
    @FXML private Label errorLabel;

    private AuthService authService;
    private boolean showingPassword = false;

    public void initialize() {
        authService = new AuthService();
        // Keep both fields in sync
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility() {
        showingPassword = !showingPassword;
        if (showingPassword) {
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeIcon.setIconLiteral("fas-eye");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            eyeIcon.setIconLiteral("fas-eye-slash");
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill all fields.");
            return;
        }

        if (authService.login(email, password)) {
            try {
                if (AuthService.isAdmin()) {
                    MainApp.setRoot("view/admin_dashboard");
                } else {
                    MainApp.setRoot("view/dashboard");
                }
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Error loading dashboard.");
            }
        } else {
            errorLabel.setText("Invalid email or password.");
        }
    }

    @FXML
    private void goToRegister() throws IOException {
        MainApp.setRoot("view/register");
    }

    @FXML
    private void goToForgotPassword() throws IOException {
        MainApp.setRoot("view/forgot_password");
    }
}
