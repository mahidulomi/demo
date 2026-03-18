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

    public static void goToForgotPassword(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "forgot-password-view.fxml", 1000, 700);
    }

    public static void goToFashion(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "fashion-view.fxml", 1000, 700);
    }

    public static void goToBeauty(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "beauty-view.fxml", 1000, 700);
    }

    public static void goToElectronics(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "electronics-view.fxml", 1000, 700);
    }

    public static void goToHomeLiving(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "homeliving-view.fxml", 1000, 700);
    }

    public static void goToFreeDelivery(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "free-delivery-view.fxml", 1000, 700);
    }

    public static void goToNewArrivals(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "new-arrivals-view.fxml", 1000, 700);
    }

    public static void goToStock(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "stock-view.fxml", 1000, 700);
    }

    public static void goToCart(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "cart-view.fxml", 1000, 700);
    }

    /**
     * Go to cart while remembering which page we came from
     */
    public static void goToCartFrom(Node anyNodeInScene, String fromPage) {
        Cart.setLastVisitedPage(fromPage);
        changeScene(anyNodeInScene, "cart-view.fxml", 1000, 700);
    }

    /**
     * Go back to the last visited page (used from cart)
     */
    public static void goBackFromCart(Node anyNodeInScene) {
        String lastPage = Cart.getLastVisitedPage();
        changeScene(anyNodeInScene, lastPage, 1000, 700);
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
            e.printStackTrace(); // Print full stack trace
            throw new RuntimeException("Failed to load " + fxml, e);
        }
    }
}
