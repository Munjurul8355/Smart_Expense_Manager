package com.expensemanager.controller;

import java.io.IOException;
import com.expensemanager.MainApp;
import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;
import com.expensemanager.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SidebarController {

    @FXML private VBox sidebar;
    @FXML private HBox menuDashboard;
    @FXML private HBox menuReports;
    @FXML private HBox menuSummary;
    @FXML private HBox menuLogout;
    @FXML private Label sidebarAccountLabel;
    @FXML private ToggleButton darkModeToggle;

    public void initialize() {
        User user = AuthService.getCurrentUser();
        if (user != null && sidebarAccountLabel != null) {
            sidebarAccountLabel.setText("@" + user.getName().toLowerCase().replace(" ", ""));
        }
        setupHoverEffects();
        setupDarkModeToggle();
    }

    private void setupDarkModeToggle() {
        if (darkModeToggle == null) return;
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        updateToggleText();
        darkModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ThemeManager.setDarkMode(newVal);
            updateToggleText();
        });
    }

    private void updateToggleText() {
        if (darkModeToggle == null) return;
        darkModeToggle.setText(ThemeManager.isDarkMode() ? "\u2600 Light" : "\uD83C\uDF19 Dark");
    }

    private void setupHoverEffects() {
        applyHoverEffect(menuDashboard);
        applyHoverEffect(menuReports);
        applyHoverEffect(menuSummary);
        applyHoverEffect(menuLogout);
    }

    private void applyHoverEffect(HBox item) {
        if (item == null) return;
        String baseStyle = item.getStyle();
        item.setOnMouseEntered(e -> item.setStyle(baseStyle + "-fx-background-color: rgba(99, 102, 241, 0.22); -fx-translate-x: 5;"));
        item.setOnMouseExited(e -> item.setStyle(baseStyle));
        item.setOnMousePressed(e -> item.setStyle(baseStyle + "-fx-background-color: rgba(99, 102, 241, 0.38); -fx-translate-x: 5;"));
        item.setOnMouseReleased(e -> item.setStyle(baseStyle + "-fx-background-color: rgba(99, 102, 241, 0.22); -fx-translate-x: 5;"));
    }

    @FXML private void handleDashboard() { navigate("view/dashboard"); }
    @FXML private void handleReports()   { navigate("view/reports"); }
    @FXML private void handleSummary()   { navigate("view/summary"); }

    @FXML private void handleLogout() {
        AuthService.logout();
        navigate("view/login");
    }

    private void navigate(String view) {
        try { MainApp.setRoot(view); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
