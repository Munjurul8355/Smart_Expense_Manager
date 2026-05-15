<div align="center">

# 💰 Smart Expense Manager

**A modern JavaFX desktop application for tracking personal income and expenses**

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql)
![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven)
![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?style=for-the-badge&logo=windows)

</div>

---

## 📋 Overview

Smart Expense Manager is a feature-rich desktop application built with **Java 21 + JavaFX** that helps you manage your daily income and expenses. It includes a full authentication system, expense tracking, PDF report generation, and an admin dashboard.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Login / Register** | Secure user authentication with password hashing |
| 📊 **Expense Tracking** | Add, edit, and delete income & expense records |
| 📈 **Dashboard** | Visual summary of your financial activity |
| 📄 **PDF Export** | Generate and download expense reports as PDF |
| 🛡️ **Admin Dashboard** | Manage all users and view system-wide data |
| 🎨 **Theme Support** | Light and dark mode support |
| 🔑 **Forgot Password** | Built-in password recovery flow |

---

## 🗄️ Database Location

The database is stored automatically in a writable location on every Windows machine:

```
C:\Users\YourName\SmartExpenseManager\expenses.db
```

> The folder is created automatically on first launch — no manual setup needed.

---

## 🚀 How to Build & Run

### Requirements

- **JDK 21** (with `jpackage`) → [Download from Adoptium](https://adoptium.net/)
- **Maven 3.8+** → [Download from Maven](https://maven.apache.org/)

---

### ▶️ Option A — Build Windows Installer *(Recommended)*

```bash
build-installer.bat
```

Generates: `installer-output\SmartExpenseManager-1.0.exe`

> ✅ The installer **bundles its own JRE** — no Java needed on the target machine.

---

### ▶️ Option B — Quick Run *(For Development)*

```bash
run.bat
```

Builds the fat JAR and runs it directly. Best for development and testing.

---

### ▶️ Option C — Maven Command Line

```bash
mvn clean package
java --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED -jar target\SmartExpenseManager-fat.jar
```

---

## 📁 Project Structure

```
Smart_Expense_Manager/
├── expense_app_fixed/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/expensemanager/
│   │   │   │   ├── MainApp.java                  # Entry point
│   │   │   │   ├── controller/                   # FXML controllers
│   │   │   │   │   ├── LoginController.java
│   │   │   │   │   ├── RegisterController.java
│   │   │   │   │   ├── DashboardController.java
│   │   │   │   │   ├── ExpenseController.java
│   │   │   │   │   ├── ReportController.java
│   │   │   │   │   ├── AdminDashboardController.java
│   │   │   │   │   └── ...
│   │   │   │   ├── dao/                          # Database access layer
│   │   │   │   ├── model/                        # Data models
│   │   │   │   ├── service/                      # Business logic
│   │   │   │   └── util/
│   │   │   │       ├── DBConnection.java          # DB stored in user home
│   │   │   │       └── PDFGenerator.java          # PDF export utility
│   │   │   └── resources/
│   │   │       ├── com/expensemanager/view/       # FXML screens
│   │   │       ├── database/schema.sql            # Bundled schema
│   │   │       └── css/style.css
│   ├── database/
│   │   └── schema.sql                            # Source copy of schema
│   ├── pom.xml
│   ├── run.bat
│   └── build-installer.bat
└── README.md
```

---

## 🔑 Default Login

Register a **new account** on first launch.

> An **Admin account** can also be created via the Register screen by selecting the Admin role.

---

## 🐛 Bug Fixes (v1.0)

Two critical bugs in the previous EXE were fixed:

**1. JavaFX Fat-JAR Problem**
> JavaFX requires native `.dll` files that cannot be bundled into a plain fat JAR via `maven-shade`. When wrapped with Launch4j, the app would start, JavaFX would crash internally, and the window would never appear — with no error shown. **Fixed by using `jpackage` for proper bundling.**

**2. Database Path Bug**
> `getProtectionDomain().getCodeSource().getLocation()` returns a temp folder when running from an EXE wrapper, so the `database/` directory was being created in a random temp location and then lost. **Fixed by storing the DB in the user's home directory.**

---

## 🛠️ Tech Stack

- **Language:** Java 21
- **UI Framework:** JavaFX
- **Build Tool:** Maven 3.8+
- **Database:** SQLite (via JDBC)
- **PDF Generation:** iText / Apache PDFBox
- **Packaging:** jpackage (Windows installer)

---

## 📜 License

This project is for academic and personal use.

---

<div align="center">
Made with ❤️ by <a href="https://github.com/Munjurul8355">Munjurul</a> and <a href=<a href="https://github.com>
</div>
