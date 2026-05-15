@echo off
echo Building...
call mvn clean package -q
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo Starting Smart Expense Manager...
java --module-path "%USERPROFILE%\.m2\repository\org\openjfx" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     -jar target\SmartExpenseManager-fat.jar

pause