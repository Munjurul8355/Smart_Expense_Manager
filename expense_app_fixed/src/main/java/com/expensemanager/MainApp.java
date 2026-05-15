package com.expensemanager;

import com.expensemanager.util.DBConnection;
import com.expensemanager.util.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        primaryStage.getIcons().add(new javafx.scene.image.Image(
        MainApp.class.getResourceAsStream("/icon.png")));
        
        try {
            DBConnection.initializeDatabase();
            setRoot("view/login");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("FATAL ERROR: Failed to initialize application.");
        }

        primaryStage.setTitle("Smart Expense Manager");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(fxml + ".fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = primaryStage.getScene();
        if (scene == null) {
            // First load — create scene, register with ThemeManager in light mode
            scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
            ThemeManager.setScene(scene);
            ThemeManager.setDarkMode(false); // default: light mode
        } else {
            // Navigation — swap root but PRESERVE current dark/light state
            scene.setRoot(root);
            ThemeManager.setScene(scene);
            ThemeManager.applyCurrentTheme(); // re-apply dark-mode class to new root
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
