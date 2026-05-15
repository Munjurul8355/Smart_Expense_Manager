package com.expensemanager.dao;

import com.expensemanager.model.Transaction;
import com.expensemanager.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions(user_id, category_id, amount, type, date, note) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transaction.getUserId());
            pstmt.setInt(2, transaction.getCategoryId());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getType());
            pstmt.setString(5, transaction.getDate().format(DATE_FORMATTER));
            pstmt.setString(6, transaction.getNote());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactionsByUserId(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, c.name as category_name FROM transactions t " +
                     "JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.user_id = ? ORDER BY t.date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("category_id"),
                    rs.getDouble("amount"),
                    rs.getString("type"),
                    LocalDate.parse(rs.getString("date"), DATE_FORMATTER),
                    rs.getString("note")
                );
                t.setCategoryName(rs.getString("category_name"));
                transactions.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }


    public boolean updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET category_id = ?, amount = ?, type = ?, date = ?, note = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transaction.getCategoryId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getType());
            pstmt.setString(4, transaction.getDate().format(DATE_FORMATTER));
            pstmt.setString(5, transaction.getNote());
            pstmt.setInt(6, transaction.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactionsByUser(int userId) {
        return getTransactionsByUserId(userId);
    }
}
