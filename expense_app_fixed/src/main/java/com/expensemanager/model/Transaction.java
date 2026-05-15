package com.expensemanager.model;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private int userId;
    private int categoryId;
    private double amount;
    private String type; // "EXPENSE" or "INCOME"
    private LocalDate date;
    private String note;

    // Derived/Display properties
    private String categoryName;

    public Transaction(int id, int userId, int categoryId, double amount, String type, LocalDate date, String note) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.note = note;
    }

    public Transaction(int userId, int categoryId, double amount, String type, LocalDate date, String note) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
