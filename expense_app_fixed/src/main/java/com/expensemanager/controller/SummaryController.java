package com.expensemanager.controller;

import com.expensemanager.MainApp;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;
import com.expensemanager.service.TransactionService;
import com.expensemanager.util.PDFGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

public class SummaryController {

    @FXML private ComboBox<String> periodComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private Label monthLabel;

    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label balanceLabel;

    @FXML private TableView<CategorySummary> categoryTable;
    @FXML private TableColumn<CategorySummary, String> categoryColumn;
    @FXML private TableColumn<CategorySummary, Double> amountColumn;
    @FXML private TableColumn<CategorySummary, String> percentageColumn;

    @FXML private PieChart categoryPieChart;

    private TransactionService transactionService;
    private User currentUser;
    private List<Transaction> currentTransactions = new ArrayList<>();

    public void initialize() {
        transactionService = new TransactionService();
        currentUser = AuthService.getCurrentUser();
        setupPeriodFilter();
        setupTable();
        handleGenerate();
    }

    private void setupPeriodFilter() {
        periodComboBox.setItems(FXCollections.observableArrayList("Monthly", "Yearly"));
        periodComboBox.setValue("Monthly");

        periodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isMonthly = "Monthly".equals(newVal);
            monthComboBox.setVisible(isMonthly);
            monthComboBox.setManaged(isMonthly);
            monthLabel.setVisible(isMonthly);
            monthLabel.setManaged(isMonthly);
        });

        int currentYear = Year.now().getValue();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) years.add(i);
        yearComboBox.setItems(years);
        yearComboBox.setValue(currentYear);

        monthComboBox.setItems(FXCollections.observableArrayList(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        ));
        String currentMonthName = Month.of(LocalDate.now().getMonthValue()).name();
        monthComboBox.setValue(currentMonthName.charAt(0) + currentMonthName.substring(1).toLowerCase());
    }

    private void setupTable() {
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        percentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("৳%.2f", amount));
            }
        });
    }

    @FXML
    private void handleGenerate() {
        String period = periodComboBox.getValue();
        int year = yearComboBox.getValue();

        if ("Monthly".equals(period)) {
            int month = Month.valueOf(monthComboBox.getValue().toUpperCase()).getValue();
            currentTransactions = transactionService.getMonthlyTransactions(currentUser.getId(), year, month);
        } else {
            currentTransactions = transactionService.getYearlyTransactions(currentUser.getId(), year);
        }

        updateSummary(currentTransactions);
    }

    private void updateSummary(List<Transaction> transactions) {
        double totalIncome = transactions.stream()
            .filter(t -> "INCOME".equals(t.getType()))
            .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .mapToDouble(Transaction::getAmount).sum();

        double balance = totalIncome - totalExpense;

        totalIncomeLabel.setText(String.format("৳%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("৳%.2f", totalExpense));
        balanceLabel.setText(String.format("৳%.2f", balance));
        balanceLabel.setStyle(balance >= 0 ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #FF6B6B;");

        updateCategoryBreakdown(transactions, totalExpense);
    }

    private void updateCategoryBreakdown(List<Transaction> transactions, double totalExpense) {
        Map<String, Double> categoryMap = transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()))
            .collect(Collectors.groupingBy(
                t -> t.getCategoryName() != null ? t.getCategoryName() : "Uncategorized",
                Collectors.summingDouble(Transaction::getAmount)
            ));

        ObservableList<CategorySummary> summaryList = FXCollections.observableArrayList();
        categoryMap.forEach((cat, amt) -> {
            double pct = totalExpense > 0 ? (amt / totalExpense) * 100 : 0;
            summaryList.add(new CategorySummary(cat, amt, String.format("%.1f%%", pct)));
        });
        summaryList.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        categoryTable.setItems(summaryList);
        updatePieChart(categoryMap);
    }

    private void updatePieChart(Map<String, Double> categoryMap) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categoryMap.forEach((cat, amt) -> pieData.add(new PieChart.Data(cat, amt)));
        categoryPieChart.setData(pieData);
    }

    @FXML
    private void handleViewChart() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chart Info");
        alert.setHeaderText("Expense Breakdown Chart");
        alert.setContentText("The pie chart on the right displays the category-wise expense breakdown for the selected period.");
        alert.showAndWait();
    }

    @FXML
    private void handleExportPDF() {
        if (currentTransactions == null || currentTransactions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No transactions found for the selected period. Please generate a summary first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Summary PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("ExpenseSummary.pdf");

        File file = fileChooser.showSaveDialog(categoryTable.getScene().getWindow());
        if (file != null) {
            try {
                PDFGenerator.generateReport(file.getAbsolutePath(), currentTransactions);
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Summary exported to: " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEmailSummary() {
        showAlert(Alert.AlertType.INFORMATION, "Email Summary",
            "Email functionality requires SMTP configuration.\nPlease use 'Export to PDF' and send the file manually.");
    }

    @FXML
    private void handleBack() throws IOException {
        MainApp.setRoot("view/dashboard");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class CategorySummary {
        private String categoryName;
        private double amount;
        private String percentage;

        public CategorySummary(String categoryName, double amount, String percentage) {
            this.categoryName = categoryName;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getCategoryName() { return categoryName; }
        public double getAmount() { return amount; }
        public String getPercentage() { return percentage; }
    }
}
