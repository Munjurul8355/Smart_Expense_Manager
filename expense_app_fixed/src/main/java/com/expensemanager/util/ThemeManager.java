package com.expensemanager.util;

import javafx.scene.Scene;

public class ThemeManager {

    private static boolean darkMode = false;
    private static Scene currentScene;

    public static void setScene(Scene scene) {
        currentScene = scene;
    }

    public static boolean isDarkMode() { return darkMode; }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
        applyCurrentTheme();
    }

    public static void toggle() {
        darkMode = !darkMode;
        applyCurrentTheme();
    }

    /** Re-applies the current dark/light state to the scene root.
     *  Call this after swapping the scene root during navigation. */
    public static void applyCurrentTheme() {
        if (currentScene == null) return;
        currentScene.getRoot().getStyleClass().remove("dark-mode");
        currentScene.getRoot().getStyleClass().remove("light-mode");
        if (darkMode) {
            currentScene.getRoot().getStyleClass().add("dark-mode");
        } else {
            currentScene.getRoot().getStyleClass().add("light-mode");
        }
    }
}
