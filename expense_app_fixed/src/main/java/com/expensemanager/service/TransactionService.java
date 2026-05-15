package com.expensemanager.service;

import com.expensemanager.dao.TransactionDAO;
import com.expensemanager.model.Transaction;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;

public class TransactionService {
    
    private TransactionDAO transactionDAO;
    
    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
    }
    
    // Get all transactions for a user
    public List<Transaction> getTransactionsByUser(int userId) {
        return transactionDAO.getTransactionsByUser(userId);
    }
    
    // Add new transaction
    public boolean addTransaction(Transaction transaction) {
        if (transaction.getAmount() <= 0) {
            return false;
        }
        return transactionDAO.addTransaction(transaction);
    }
    
    // Update transaction
    public boolean updateTransaction(Transaction transaction) {
        return transactionDAO.updateTransaction(transaction);
    }
    
    // Delete transaction
    public boolean deleteTransaction(int transactionId) {
        return transactionDAO.deleteTransaction(transactionId);
    }
    
    // Calculate total income
    public double getTotalIncome(List<Transaction> transactions) {
        double total = 0.0;
        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                total += t.getAmount();
            }
        }
        return total;
    }
    
    // Calculate total expense
    public double getTotalExpense(List<Transaction> transactions) {
        double total = 0.0;
        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.getType())) {
                total += t.getAmount();
            }
        }
        return total;
    }
    
    // Get expenses grouped by category
    public Map<String, Double> getExpenseByCategory(List<Transaction> transactions) {
        Map<String, Double> categoryMap = new HashMap<>();
        
        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.getType())) {
                String category = t.getCategoryName();
                double currentAmount = categoryMap.getOrDefault(category, 0.0);
                categoryMap.put(category, currentAmount + t.getAmount());
            }
        }
        
        return categoryMap;
    }
    
    // Get monthly transactions (NO STREAMS)
    public List<Transaction> getMonthlyTransactions(int userId, int year, int month) {
        List<Transaction> allTransactions = transactionDAO.getTransactionsByUser(userId);
        List<Transaction> monthlyTransactions = new ArrayList<>();
        
        for (Transaction t : allTransactions) {
            LocalDate date = t.getDate();
            if (date.getYear() == year && date.getMonthValue() == month) {
                monthlyTransactions.add(t);
            }
        }
        
        return monthlyTransactions;
    }
    
    // Get yearly transactions (NO STREAMS)
    public List<Transaction> getYearlyTransactions(int userId, int year) {
        List<Transaction> allTransactions = transactionDAO.getTransactionsByUser(userId);
        List<Transaction> yearlyTransactions = new ArrayList<>();
        
        for (Transaction t : allTransactions) {
            if (t.getDate().getYear() == year) {
                yearlyTransactions.add(t);
            }
        }
        
        return yearlyTransactions;
    }
}