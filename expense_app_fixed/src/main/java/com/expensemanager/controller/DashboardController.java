package com.expensemanager.controller;

import com.expensemanager.MainApp;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;
import com.expensemanager.service.FinanceAnalyzerService;
import com.expensemanager.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController {

    // Sidebar is now handled by SidebarController via fx:include

    // Header labels
    // welcomeLabel is now in SidebarController scope
    @FXML private Label dashboardWelcomeLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label insightLabel;

    // Summary cards
    @FXML private Label balanceLabel;
    @FXML private Label incomeLabel;
    @FXML private Label expenseLabel;

    // Charts
    @FXML private PieChart expensePieChart;
    @FXML private BarChart<String, Number> expenseBarChart;

    // Table
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, Void> actionColumn;
    @FXML private ComboBox<String> sortComboBox;

    // Footer stats (NEW)
    @FXML private Label totalTransactionsLabel;
    @FXML private Label monthlyTransactionsLabel;
    @FXML private Label avgDailyExpenseLabel;
    @FXML private Label topCategoryLabel;
    @FXML private Label topCategoryAmountLabel;

    private TransactionService transactionService;
    private FinanceAnalyzerService analyzerService;
    private User currentUser;
    private Tooltip barTooltip;


    public void initialize() {
        try {
            transactionService = new TransactionService();
            analyzerService = new FinanceAnalyzerService();
            currentUser = AuthService.getCurrentUser();

            if (currentUser == null) {
                System.err.println("DashboardController: Current user is null. Redirecting to login.");
                javafx.application.Platform.runLater(() -> {
                    try {
                        MainApp.setRoot("view/login");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                return;
            }

            // Set current date
            if (currentDateLabel != null) {
                currentDateLabel.setText(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
                );
            }

            // Set user name
            // User name is shown in sidebar
            if (dashboardWelcomeLabel != null) {
                dashboardWelcomeLabel.setText("Welcome back, " + currentUser.getName());
            }

            setupTable();
            setupSorting();

            barTooltip = new Tooltip();
            barTooltip.setStyle("-fx-background-color:#1E293B;-fx-text-fill:white;" +
                    "-fx-background-radius:8;-fx-padding:8 14;-fx-font-size:13px;-fx-font-weight:bold;");

            loadData();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("DashboardController: Critical error in initialize.");
        }
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        // Format amount column
        amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("৳%.2f", amount));
                    // Color code based on row type
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if ("INCOME".equals(transaction.getType())) {
                        setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Type column with color coding
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    if ("INCOME".equals(type)) {
                        setStyle("-fx-text-fill: #22C55E; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #EF4444; -fx-font-weight: 600;");
                    }
                }
            }
        });

        // Action column with Edit/Delete buttons
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(editBtn, deleteBtn);

            {
                pane.setSpacing(10);
                editBtn.getStyleClass().add("btn-action");
                deleteBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    handleEditTransaction(transaction);
                });

                deleteBtn.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    handleDeleteTransaction(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupSorting() {
        sortComboBox.setItems(FXCollections.observableArrayList(
            "Date (Newest)", "Date (Oldest)",
            "Amount (High → Low)", "Amount (Low → High)"
        ));
        sortComboBox.getSelectionModel().selectFirst();
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadData());
    }

    private void loadData() {
        List<Transaction> transactions = transactionService.getTransactionsByUser(currentUser.getId());

        // Sorting Logic
        String sortType = sortComboBox.getValue();
        if (sortType != null) {
            switch (sortType) {
                case "Date (Newest)":
                    transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
                    break;
                case "Date (Oldest)":
                    transactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));
                    break;
                case "Amount (High → Low)":
                    transactions.sort((t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()));
                    break;
                case "Amount (Low → High)":
                    transactions.sort((t1, t2) -> Double.compare(t1.getAmount(), t2.getAmount()));
                    break;
            }
        }

        ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(transactions);
        transactionTable.setItems(observableTransactions);

        // Calculate totals
        double income = transactionService.getTotalIncome(transactions);
        double expense = transactionService.getTotalExpense(transactions);
        double balance = income - expense;

        // Update summary cards
        incomeLabel.setText(String.format("+৳%.2f", income));
        expenseLabel.setText(String.format("-৳%.2f", expense));

        if (balance >= 0) {
            balanceLabel.setText(String.format("৳%.2f", balance));
            balanceLabel.setStyle("-fx-text-fill: #4F46E5; -fx-font-size: 28px; -fx-font-weight: bold;");
        } else {
            balanceLabel.setText(String.format("-৳%.2f", Math.abs(balance)));
            balanceLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 28px; -fx-font-weight: bold;");
        }

        // Update insights
        List<String> insights = analyzerService.generateInsights(transactions);
        if (!insights.isEmpty() && insightLabel != null) {
            insightLabel.setText(insights.get(0));
        }

        // Update footer stats
        updateFooterStats(transactions);

        // Update charts
        loadCharts(transactions);
    }

    private void updateFooterStats(List<Transaction> transactions) {
        // Total transactions
        if (totalTransactionsLabel != null) {
            totalTransactionsLabel.setText("Total: " + transactions.size() + " transactions");
        }

        // Monthly transactions
        LocalDate now = LocalDate.now();
        long monthlyCount = transactions.stream()
            .filter(t -> t.getDate().getMonth() == now.getMonth() 
                      && t.getDate().getYear() == now.getYear())
            .count();
        if (monthlyTransactionsLabel != null) {
            monthlyTransactionsLabel.setText(String.valueOf(monthlyCount));
        }

        // Average daily expense
        double monthlyExpense = transactions.stream()
            .filter(t -> "EXPENSE".equals(t.getType()) 
                      && t.getDate().getMonth() == now.getMonth()
                      && t.getDate().getYear() == now.getYear())
            .mapToDouble(Transaction::getAmount)
            .sum();
        int daysInMonth = now.lengthOfMonth();
        double avgDaily = monthlyExpense / daysInMonth;
        if (avgDailyExpenseLabel != null) {
            avgDailyExpenseLabel.setText(String.format("৳%.2f", avgDaily));
        }

        // Top category
        Map<String, Double> categoryMap = transactionService.getExpenseByCategory(transactions);
        if (!categoryMap.isEmpty() && topCategoryLabel != null) {
            Map.Entry<String, Double> topEntry = categoryMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
            if (topEntry != null) {
                topCategoryLabel.setText(topEntry.getKey());
                if (topCategoryAmountLabel != null) {
                    topCategoryAmountLabel.setText(String.format("৳%.2f", topEntry.getValue()));
                }
            }
        }
    }

    private void loadCharts(List<Transaction> transactions) {
        // Pie Chart with hover effects
        if (expensePieChart != null) {
            expensePieChart.setTitle(null);
            Map<String, Double> expensesByCategory = transactionService.getExpenseByCategory(transactions);
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            expensesByCategory.forEach((category, amount) -> pieData.add(new PieChart.Data(category, amount)));
            expensePieChart.setData(pieData);

            Tooltip pt = new Tooltip();
            pt.setStyle("-fx-background-color:#1E293B;-fx-text-fill:white;-fx-background-radius:8;-fx-padding:8 14;-fx-font-size:13px;-fx-font-weight:bold;");
            for (PieChart.Data d : expensePieChart.getData()) {
                d.getNode().setCursor(Cursor.HAND);
                d.getNode().setOnMouseEntered(e -> {
                    pt.setText(d.getName() + ": " + String.format("৳%.2f", d.getPieValue()));
                    d.getNode().setScaleX(1.08);
                    d.getNode().setScaleY(1.08);
                });
                d.getNode().setOnMouseMoved(e -> pt.show(d.getNode(), e.getScreenX() + 12, e.getScreenY() + 12));
                d.getNode().setOnMouseExited(e -> {
                    pt.hide();
                    d.getNode().setScaleX(1.0);
                    d.getNode().setScaleY(1.0);
                });
            }
        }

        // Bar Chart with hover effects
        if (expenseBarChart != null) {
            double totalIncome  = transactionService.getTotalIncome(transactions);
            double totalExpense = transactionService.getTotalExpense(transactions);

            XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
            incomeSeries.setName("Income");
            XYChart.Data<String, Number> incomeData = new XYChart.Data<>("Total", totalIncome);
            incomeSeries.getData().add(incomeData);

            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Expense");
            XYChart.Data<String, Number> expenseData = new XYChart.Data<>("Total", totalExpense);
            expenseSeries.getData().add(expenseData);

            expenseBarChart.getData().clear();
            expenseBarChart.getData().addAll(incomeSeries, expenseSeries);

            javafx.application.Platform.runLater(() -> {
                addBarHover(incomeData,  "Total Income",  totalIncome);
                addBarHover(expenseData, "Total Expense", totalExpense);
            });
        }
    }

    private void addBarHover(XYChart.Data<String, Number> data, String label, double amount) {
        if (data.getNode() == null) return;
        data.getNode().setCursor(Cursor.HAND);
        data.getNode().setOnMouseEntered(e -> {
            barTooltip.setText(label + "\n" + String.format("৳%.2f", amount));
            data.getNode().setScaleX(1.06);
            data.getNode().setScaleY(1.06);
            barTooltip.show(data.getNode(), e.getScreenX() + 12, e.getScreenY() + 12);
        });
        data.getNode().setOnMouseMoved(e -> barTooltip.show(data.getNode(), e.getScreenX() + 12, e.getScreenY() + 12));
        data.getNode().setOnMouseExited(e -> {
            barTooltip.hide();
            data.getNode().setScaleX(1.0);
            data.getNode().setScaleY(1.0);
        });
    }

    private void handleEditTransaction(Transaction transaction) {
        try {
            ExpenseController.setEditingTransaction(transaction);
            MainApp.setRoot("view/expense_form");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteTransaction(Transaction transaction) {
        // Create confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Transaction");
        alert.setHeaderText("Are you sure you want to delete this transaction?");
        alert.setContentText(
            "Type: " + transaction.getType() + "\n" +
            "Category: " + transaction.getCategoryName() + "\n" +
            "Amount: ৳" + String.format("%.2f", transaction.getAmount()) + "\n" +
            "Date: " + transaction.getDate()
        );
        
        ButtonType buttonTypeYes = new ButtonType("Yes, Delete");
        ButtonType buttonTypeNo = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                if (transactionService.deleteTransaction(transaction.getId())) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Transaction deleted successfully!");
                    successAlert.showAndWait();
                    
                    loadData();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Failed to delete transaction");
                    errorAlert.setContentText("An error occurred. Please try again.");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    // Navigation handler (only Add Transaction is triggered from dashboard FXML directly)
    @FXML
    private void handleAddTransaction() throws IOException {
        ExpenseController.setEditingTransaction(null);
        MainApp.setRoot("view/expense_form");
    }
}