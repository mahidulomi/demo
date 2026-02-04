package com.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Simple session + navigation helper.
 */
public final class Session {
    private static volatile String currentUser;

    private Session() {}

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void login(String username) {
        currentUser = username;
    }

    public static void logout() {
        currentUser = null;
    }

    public static void goToHome(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "home-view.fxml", 1000, 700);
    }

    public static void goToSignUp(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "signup-view.fxml", 1000, 700);
    }

    public static void goToLogin(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "hello-view.fxml", 1000, 700);
    }

    public static void logoutToLogin(Node anyNodeInScene) {
        logout();
        goToLogin(anyNodeInScene);
    }

    private static void changeScene(Node anyNodeInScene, String fxml, double w, double h) {
        try {
            Stage stage = (Stage) anyNodeInScene.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }
}
