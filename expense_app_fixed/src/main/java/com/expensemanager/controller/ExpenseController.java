package com.expensemanager.controller;

import com.expensemanager.MainApp;
import com.expensemanager.dao.CategoryDAO;
import com.expensemanager.model.Category;
import com.expensemanager.model.Transaction;
import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;
import com.expensemanager.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseController {

    @FXML private ComboBox<String>   typeComboBox;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField          customCategoryField;
    @FXML private TextField          amountField;
    @FXML private DatePicker         datePicker;
    @FXML private TextArea           noteArea;
    @FXML private Label              errorLabel;

    // Title row — updated dynamically for add vs edit
    @FXML private Label    formTitleLabel;
    @FXML private FontIcon formIcon;
    @FXML private VBox     formIconBox;
    @FXML private Button   saveButton;

    private TransactionService transactionService;
    private CategoryDAO        categoryDAO;
    private User               currentUser;

    private static Transaction editingTransaction;

    public static void setEditingTransaction(Transaction transaction) {
        editingTransaction = transaction;
    }

    public void initialize() {
        transactionService = new TransactionService();
        categoryDAO        = new CategoryDAO();
        currentUser        = AuthService.getCurrentUser();

        typeComboBox.setItems(FXCollections.observableArrayList("EXPENSE", "INCOME"));
        typeComboBox.getSelectionModel().selectFirst();

        updateCategories();
        typeComboBox.valueProperty().addListener((obs, o, n) -> updateCategories());

        categoryComboBox.valueProperty().addListener((obs, o, newVal) -> {
            boolean isOther = newVal != null && "Other".equalsIgnoreCase(newVal.getName());
            customCategoryField.setVisible(isOther);
            customCategoryField.setManaged(isOther);
            if (!isOther) customCategoryField.clear();
        });

        datePicker.setValue(LocalDate.now());

        if (editingTransaction != null) {
            // ── Edit mode ────────────────────────────────────────────
            if (formTitleLabel != null) formTitleLabel.setText("Edit Transaction");
            if (saveButton     != null) saveButton.setText("Update Transaction");
            if (formIcon       != null) {
                formIcon.setIconLiteral("fas-edit");
            }
            if (formIconBox    != null) {
                formIconBox.getStyleClass().remove("icon-badge-blue");
                formIconBox.getStyleClass().add("icon-badge-green");
            }
            prefillForm();
        } else {
            // ── Add mode ─────────────────────────────────────────────
            if (formTitleLabel != null) formTitleLabel.setText("Add New Transaction");
            if (saveButton     != null) saveButton.setText("Save Transaction");
            if (formIcon       != null) formIcon.setIconLiteral("fas-plus");
            if (formIconBox    != null) {
                formIconBox.getStyleClass().remove("icon-badge-green");
                formIconBox.getStyleClass().add("icon-badge-blue");
            }
        }
    }

    private void prefillForm() {
        typeComboBox.setValue(editingTransaction.getType());
        amountField.setText(String.valueOf(editingTransaction.getAmount()));
        datePicker.setValue(editingTransaction.getDate());
        noteArea.setText(editingTransaction.getNote());
        for (Category c : categoryComboBox.getItems()) {
            if (c.getId() == editingTransaction.getCategoryId()) {
                categoryComboBox.setValue(c);
                break;
            }
        }
    }

    private void updateCategories() {
        String type = typeComboBox.getValue();
        List<Category> all      = categoryDAO.getAllCategories();
        List<Category> filtered = all.stream()
            .filter(c -> c.getType().equals(type))
            .collect(Collectors.toList());
        categoryComboBox.setItems(FXCollections.observableArrayList(filtered));
        if (!filtered.isEmpty()) categoryComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSave() {
        try {
            String   type             = typeComboBox.getValue();
            Category category         = categoryComboBox.getValue();
            String   customCategoryName = customCategoryField.getText().trim();

            if (category != null && "Other".equalsIgnoreCase(category.getName())) {
                if (customCategoryName.isEmpty()) {
                    errorLabel.setText("Please enter a custom category name.");
                    return;
                }
                Category existing = categoryDAO.getCategoryByName(customCategoryName);
                if (existing != null) {
                    category = existing;
                } else {
                    Category newCat = new Category(0, customCategoryName, type);
                    int newId = categoryDAO.addCategory(newCat);
                    if (newId != -1) { newCat.setId(newId); category = newCat; }
                    else { errorLabel.setText("Failed to save custom category."); return; }
                }
            }

            double    amount = Double.parseDouble(amountField.getText());
            LocalDate date   = datePicker.getValue();
            String    note   = noteArea.getText();

            if (category == null || date == null) {
                errorLabel.setText("Please fill all fields.");
                return;
            }

            Transaction t = new Transaction(currentUser.getId(), category.getId(), amount, type, date, note);

            boolean success;
            if (editingTransaction != null) {
                t.setId(editingTransaction.getId());
                success = transactionService.updateTransaction(t);
            } else {
                success = transactionService.addTransaction(t);
            }

            if (success) {
                editingTransaction = null;
                MainApp.setRoot("view/dashboard");
            } else {
                errorLabel.setText("Failed to save transaction.");
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid amount.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() throws IOException {
        editingTransaction = null;
        MainApp.setRoot("view/dashboard");
    }
}
