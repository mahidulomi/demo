package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalTime;
import java.util.Locale;

public class HomeController {

    @FXML
    private Label userLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField searchField;

    @FXML
    private void initialize() {
        String user = Session.getCurrentUser();
        userLabel.setText(buildGreeting(user));

        statusLabel.setText("Welcome! Try search: fashion, electronics, beauty, cart, free delivery.");

        if (searchField != null) {
            searchField.setOnAction(e -> onSearch());
        }
    }

    @FXML
    private void onSearch() {
        String q = safe(searchField.getText()).toLowerCase(Locale.ROOT);

        if (q.isEmpty()) {
            statusLabel.setText("Type a category name to jump quickly.");
            return;
        }

        if (q.contains("fashion")) {
            statusLabel.setText("Opening Fashion...");
            Session.goToFashion(statusLabel);
            return;
        }
        if (q.contains("electronic") || q.contains("mobile") || q.contains("laptop")) {
            statusLabel.setText("Opening Electronics...");
            Session.goToElectronics(statusLabel);
            return;
        }
        if (q.contains("beauty") || q.contains("cosmetic")) {
            statusLabel.setText("Opening Beauty...");
            Session.goToBeauty(statusLabel);
            return;
        }
        if (q.contains("home") || q.contains("living")) {
            statusLabel.setText("Opening Home & Living...");
            Session.goToHomeLiving(statusLabel);
            return;
        }
        if (q.contains("arrival") || q.contains("new")) {
            statusLabel.setText("Opening New Arrivals...");
            Session.goToNewArrivals(statusLabel);
            return;
        }
        if (q.contains("free") || q.contains("delivery")) {
            statusLabel.setText("Opening Free Delivery...");
            Session.goToFreeDelivery(statusLabel);
            return;
        }
        if (q.contains("stock")) {
            statusLabel.setText("Opening Stock...");
            Session.goToStock(statusLabel);
            return;
        }
        if (q.contains("cart")) {
            statusLabel.setText("Opening Cart...");
            Session.goToCartFrom(statusLabel, "home-view.fxml");
            return;
        }

        statusLabel.setText("No quick match for: " + q + " (try: fashion, electronics, beauty, cart)");
    }

    @FXML
    private void onLogout() {
        Session.logoutToLogin(userLabel);
    }

    @FXML
    private void onProfileClick() {
        String user = Session.getCurrentUser();
        statusLabel.setText("Profile: " + (user == null ? "Guest" : user) + " (Profile page coming soon)");
    }

    @FXML
    private void openFashion() {
        Session.goToFashion(statusLabel);
    }

    @FXML
    private void openElectronics() {
        Session.goToElectronics(statusLabel);
    }

    @FXML
    private void openHomeLiving() {
        try {
            System.out.println("Opening Home & Living...");
            statusLabel.setText("Opening Home & Living...");
            Session.goToHomeLiving(statusLabel);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void openBeauty() {
        Session.goToBeauty(statusLabel);
    }

    @FXML
    private void openCart() {
        Session.goToCartFrom(statusLabel, "home-view.fxml");
    }

    @FXML
    private void openOrders() {
        statusLabel.setText("Orders: demo screen");
    }

    @FXML
    private void openFreeDelivery() {
        Session.goToFreeDelivery(statusLabel);
    }

    @FXML
    private void openNewArrivals() {
        Session.goToNewArrivals(statusLabel);
    }

    @FXML
    private void openStock() {
        Session.goToStock(statusLabel);
    }

    private static String buildGreeting(String user) {
        int hour = LocalTime.now().getHour();
        String prefix = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
        return user == null ? prefix + "!" : prefix + ", " + user + "!";
    }

    private static String safe(String text) {
        return text == null ? "" : text.trim();
    }
}
