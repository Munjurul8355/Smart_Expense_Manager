package com.expensemanager.service;

import com.expensemanager.dao.UserDAO;
import com.expensemanager.model.User;
import com.expensemanager.util.PasswordUtil;

public class AuthService {
    private UserDAO userDAO;
    private static User currentUser;
    private static boolean isAdmin = false;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public boolean register(String name, String email, String password) {
        if (userDAO.getUserByEmail(email) != null) {
            return false; // User already exists
        }
        String hashedPassword = PasswordUtil.hashPassword(password);
        User newUser = new User(name, email, hashedPassword);
        return userDAO.registerUser(newUser);
    }

    public boolean login(String email, String password) {
        if ("admin@gmail.com".equals(email) && "admin1".equals(password)) {
            isAdmin = true;
            currentUser = new User("Admin", "admin@gmail.com", ""); // Dummy admin user
            return true;
        }

        User user = userDAO.getUserByEmail(email);
        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            currentUser = user;
            isAdmin = false;
            return true;
        }
        return false;
    }

    public static void logout() {
        currentUser = null;
        isAdmin = false;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

    public boolean resetPassword(String email, String newPassword) {
        if (userDAO.getUserByEmail(email) == null) {
            return false; // User not found
        }
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(email, hashedPassword);
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}
