# Smart Expense Manager

A JavaFX desktop application for tracking personal income and expenses.

---

## Why the old EXE did nothing

Two bugs caused the silent failure:

1. **JavaFX fat-JAR problem** — JavaFX needs native `.dll` files that can't be
   bundled into a plain fat JAR via `maven-shade`. When Launch4j or a similar
   tool wraps such a JAR, the app starts, JavaFX crashes internally, and the
   window never appears — no error shown.

2. **DB path bug** — `getProtectionDomain().getCodeSource().getLocation()`
   returns a temp folder when running from an EXE wrapper, so the `database/`
   directory was created in a random temp location and then lost.

Both are now fixed.

---

## Database Location

The database is always stored here — writable on every Windows machine:

```
C:\Users\YourName\SmartExpenseManager\expenses.db
```

The folder is created automatically on first launch.

---

## How to Build

### Requirements
- **JDK 21** with `jpackage` included — download from https://adoptium.net/
- **Maven 3.8+** — download from https://maven.apache.org/

### Option A — Build a real Windows installer (recommended)

```bat
build-installer.bat
```

This creates `installer-output\SmartExpenseManager-1.0.exe`.  
The installer bundles its own JRE — **no Java needed on the target machine**.

### Option B — Quick test run (no installer)

```bat
run.bat
```

Builds the fat JAR and runs it directly. Good for development.

### Option C — Maven command line

```bat
mvn clean package
java --add-opens=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED -jar target\SmartExpenseManager-fat.jar
```

---

## Project Structure

```
src/main/java/com/expensemanager/
  MainApp.java                  — entry point
  controller/                   — FXML controllers
  dao/                          — database access
  model/                        — data models
  service/                      — business logic
  util/
    DBConnection.java           — DB stored in user home dir
    PDFGenerator.java           — export to PDF

src/main/resources/
  com/expensemanager/view/      — FXML screens
  database/schema.sql           — bundled inside JAR
  css/style.css

database/
  schema.sql                    — source copy of schema
```

---

## Default Login

Register a new account on first launch.  
An admin account can be created via the Register screen.
