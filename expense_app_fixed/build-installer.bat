@echo off
REM ============================================================
REM  Smart Expense Manager — Windows Installer Builder
REM  Requirements: JDK 21 with jpackage (included in JDK 14+)
REM  Run this from the project root folder
REM ============================================================

setlocal

set APP_NAME=SmartExpenseManager
set APP_VERSION=1.0
set MAIN_CLASS=com.expensemanager.Launcher
set FAT_JAR=target\SmartExpenseManager-fat.jar
set OUTPUT_DIR=installer-output
set ICON_PATH=%~dp0app-icon.ico

echo [1/3] Building fat JAR with Maven...
call mvn clean package -q
if errorlevel 1 (
    echo ERROR: Maven build failed. Check error output above.
    pause
    exit /b 1
)

echo [2/3] Cleaning old installer output...
if exist "%OUTPUT_DIR%" rmdir /s /q "%OUTPUT_DIR%"
mkdir "%OUTPUT_DIR%"

echo [3/3] Creating Windows installer with jpackage...
jpackage --type exe --name "%APP_NAME%" --app-version "%APP_VERSION%" --input target --main-jar SmartExpenseManager-fat.jar --main-class %MAIN_CLASS% --dest "%OUTPUT_DIR%" --icon "%ICON_PATH%" --win-shortcut --win-menu --win-dir-chooser --description "Smart Expense Manager" --vendor "SmartExpenseManager" --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (
    echo.
    echo ERROR: jpackage failed.
    echo Make sure you have JDK 21 installed ^(not just JRE^).
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

echo.
echo ============================================================
echo  SUCCESS! Installer created in: %OUTPUT_DIR%\
echo  File: %OUTPUT_DIR%\%APP_NAME%-%APP_VERSION%.exe
echo.
echo  The installer bundles its own JRE — no Java needed on
echo  the target machine. DB stored in:
echo    C:\Users\^<name^>\SmartExpenseManager\expenses.db
echo ============================================================
pause
