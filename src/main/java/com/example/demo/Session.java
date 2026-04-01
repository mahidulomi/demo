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
        changeScene(anyNodeInScene, "home-view.fxml", 1250, 750);
    }

    public static void goToCustomerHome(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "customer-home-view.fxml", 1250, 750);
    }

    public static void goToSignUp(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "signup-view.fxml", 1250, 750);
    }

    public static void goToLogin(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "hello-view.fxml", 1250, 750);
    }

    public static void logoutToLogin(Node anyNodeInScene) {
        logout();
        goToLogin(anyNodeInScene);
    }

    public static void goToForgotPassword(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "forgot-password-view.fxml", 1250, 750);
    }

    public static void goToProductList(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "product-list-view.fxml", 1250, 750);
    }

    public static void goToFashion(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "fashion-view.fxml", 1250, 750);
    }

    public static void goToBeauty(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "beauty-view.fxml", 1250, 750);
    }

    public static void goToElectronics(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "electronics-view.fxml", 1250, 750);
    }

    public static void goToHomeLiving(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "homeliving-view.fxml", 1250, 750);
    }

    public static void goToFreeDelivery(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "free-delivery-view.fxml", 1250, 750);
    }

    public static void goToNewArrivals(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "new-arrivals-view.fxml", 1250, 750);
    }

    public static void goToStock(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "stock-view.fxml", 1250, 750);
    }

    public static void goToSales(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "sales-view.fxml", 1250, 750);
    }
    
    public static void goToReports(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "reports-view.fxml", 1250, 750);
    }

    public static void goToAboutUs(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "about-view.fxml", 1250, 750);
    }

    public static void goToCustomers(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "customer-view.fxml", 1250, 750);
    }


    public static void goToRestock(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "restock-view.fxml", 1250, 750);
    }

    public static void goToCart(Node anyNodeInScene) {
        changeScene(anyNodeInScene, "cart-view.fxml", 1250, 750);
    }

    /**
     * Go to cart while remembering which page we came from
     */
    public static void goToCartFrom(Node anyNodeInScene, String fromPage) {
        Cart.setLastVisitedPage(fromPage);
        changeScene(anyNodeInScene, "cart-view.fxml", 1250, 750);
    }

    /**
     * Go back to the last visited page (used from cart)
     */
    public static void goBackFromCart(Node anyNodeInScene) {
        String lastPage = Cart.getLastVisitedPage();
        changeScene(anyNodeInScene, lastPage, 1250, 750);
    }

    private static void changeScene(Node anyNodeInScene, String fxml, double w, double h) {
        try {
            System.out.println("[Session] Loading FXML: " + fxml);
            
            // Get stage from node
            Scene currentScene = anyNodeInScene.getScene();
            if (currentScene == null) {
                System.err.println("[Session] Node scene is null - trying to find stage from parent");
                throw new RuntimeException("Node's scene is null. Cannot get window.");
            }
            
            Stage stage = (Stage) currentScene.getWindow();
            if (stage == null) {
                System.err.println("[Session] Could not get Stage from scene");
                throw new RuntimeException("Could not get Stage from scene");
            }
            
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxml));
            if (loader.getLocation() == null) {
                System.err.println("[Session] FXML resource not found: " + fxml);
                throw new RuntimeException("FXML resource not found: " + fxml);
            }
            
            Parent root = loader.load();
            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
            stage.centerOnScreen();
            System.out.println("[Session] Successfully loaded: " + fxml);
        } catch (IOException e) {
            System.err.println("[Session] Failed to load " + fxml + ": " + e.getMessage());
            e.printStackTrace(); // Print full stack trace
            throw new RuntimeException("Failed to load " + fxml, e);
        } catch (Exception e) {
            System.err.println("[Session] Unexpected error loading " + fxml + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error loading " + fxml + ": " + e.getMessage(), e);
        }
    }
}
