package com.expensemanager.controller;

import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;
import com.expensemanager.service.TransactionService;
import com.expensemanager.util.PDFGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class ReportController {

    @FXML private Label statusLabel;

    private TransactionService transactionService;
    private User currentUser;

    public void initialize() {
        transactionService = new TransactionService();
        currentUser = AuthService.getCurrentUser();
    }

    @FXML
    private void handleMonthlyReport() {
        generateReport(true);
    }

    @FXML
    private void handleYearlyReport() {
        generateReport(false);
    }

    private void generateReport(boolean isMonthly) {
        String type = isMonthly ? "Monthly" : "Yearly";

        List<Transaction> transactions = transactionService.getTransactionsByUser(currentUser.getId());

        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        List<Transaction> filtered;
        if (isMonthly) {
            filtered = transactions.stream()
                .filter(t -> t.getDate().getYear() == currentYear && t.getDate().getMonthValue() == currentMonth)
                .toList();
        } else {
            filtered = transactions.stream()
                .filter(t -> t.getDate().getYear() == currentYear)
                .toList();
        }

        if (filtered.isEmpty()) {
            statusLabel.setText("No transactions found for this period.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save " + type + " Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(type + "_ExpenseReport.pdf");

        // Default to Desktop for easy access
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        if (desktop.exists()) fileChooser.setInitialDirectory(desktop);

        File file = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());

        if (file != null) {
            try {
                PDFGenerator.generateReport(file.getAbsolutePath(), filtered);
                statusLabel.setText("✔ " + type + " report saved: " + file.getName());
                statusLabel.setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold;");
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("✖ Failed: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }
}
