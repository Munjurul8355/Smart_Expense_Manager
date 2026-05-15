package com.expensemanager.controller;

import com.expensemanager.MainApp;
import com.expensemanager.dao.UserDAO;
import com.expensemanager.model.User;
import com.expensemanager.service.AuthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;

import javafx.beans.property.SimpleStringProperty;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class AdminDashboardController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> dateColumn;
    @FXML private Label statusLabel;

    private UserDAO userDAO;
    private ObservableList<User> userList;

    public void initialize() {
        userDAO = new UserDAO();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getCreatedAt();
            if (date != null) {
                return new SimpleStringProperty(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("");
        });

        loadUsers();
    }

    private void loadUsers() {
        userList = FXCollections.observableArrayList(userDAO.getAllUsers());
        userTable.setItems(userList);
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            if (userDAO.deleteUser(selectedUser.getId())) {
                userList.remove(selectedUser);
                statusLabel.setText("User deleted successfully.");
            } else {
                statusLabel.setText("Failed to delete user.");
            }
        } else {
            statusLabel.setText("Please select a user to delete.");
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        AuthService.logout();
        MainApp.setRoot("view/login");
    }
}
