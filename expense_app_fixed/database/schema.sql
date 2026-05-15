-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT CHECK(type IN ('EXPENSE', 'INCOME')) NOT NULL,
    UNIQUE(name, type)
);

-- Create Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    category_id INTEGER,
    amount REAL NOT NULL,
    type TEXT CHECK(type IN ('EXPENSE', 'INCOME')) NOT NULL,
    date TEXT NOT NULL,
    note TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- EXPENSE categories (7 unique)
INSERT OR IGNORE INTO categories (name, type) VALUES ('Food',          'EXPENSE');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Transport',     'EXPENSE');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Bills',         'EXPENSE');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Shopping',      'EXPENSE');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Health',        'EXPENSE');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Entertainment', 'EXPENSE');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Others',        'EXPENSE');

-- INCOME categories (7 unique)
INSERT OR IGNORE INTO categories (name, type) VALUES ('Salary',        'INCOME');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Freelancing',   'INCOME');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Business',      'INCOME');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Investments',   'INCOME');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Gift',          'INCOME');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Rental',        'INCOME');
INSERT OR IGNORE INTO categories (name, type) VALUES ('Others',        'INCOME');
