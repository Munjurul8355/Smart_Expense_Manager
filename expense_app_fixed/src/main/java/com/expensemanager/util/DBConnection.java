package com.expensemanager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private static final String dbPath;

    static {
        // Store DB in user's home directory — ALWAYS writable, works everywhere
        // Path: C:\Users\YourName\SmartExpenseManager\expenses.db
        File appDir = new File(System.getProperty("user.home"), "SmartExpenseManager");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File dbFile = new File(appDir, "expenses.db");
        dbPath = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        System.out.println("DB path: " + dbFile.getAbsolutePath());
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(dbPath);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found", e);
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Try loading schema.sql from filesystem first, then classpath
            String schemaSql = loadSchemaFromClasspath();
            if (schemaSql != null && !schemaSql.isBlank()) {
                String[] statements = schemaSql.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
                System.out.println("Database initialized successfully.");
            } else {
                System.err.println("schema.sql not found — creating tables inline.");
                createTablesInline(stmt);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String loadSchemaFromClasspath() throws IOException {
        // Load from classpath only (bundled inside JAR — works from EXE too)
        InputStream is = DBConnection.class.getResourceAsStream("/database/schema.sql");
        if (is == null) is = DBConnection.class.getResourceAsStream("/schema.sql");
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
            }
            return sb.toString();
        }
        return null;
    }

    private static void createTablesInline(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
            "email TEXT UNIQUE NOT NULL, password_hash TEXT NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
            "type TEXT CHECK(type IN ('EXPENSE','INCOME')) NOT NULL, " +
            "UNIQUE(name, type))");

        stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, " +
            "category_id INTEGER, amount REAL NOT NULL, " +
            "type TEXT CHECK(type IN ('EXPENSE','INCOME')) NOT NULL, " +
            "date TEXT NOT NULL, note TEXT, " +
            "FOREIGN KEY (user_id) REFERENCES users(id), " +
            "FOREIGN KEY (category_id) REFERENCES categories(id))");

        // Default EXPENSE categories
        String[] expenseCats = {"Food","Transport","Bills","Shopping","Health","Entertainment","Others"};
        // Default INCOME categories (includes Others)
        String[] incomeCats = {"Salary","Freelancing","Business","Investments","Gift","Rental","Others"};
        for (String c : expenseCats)
            stmt.execute("INSERT OR IGNORE INTO categories(name,type) VALUES('" + c + "','EXPENSE')");
        for (String c : incomeCats)
            stmt.execute("INSERT OR IGNORE INTO categories(name,type) VALUES('" + c + "','INCOME')");
    }
}
