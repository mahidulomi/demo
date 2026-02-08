package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
        userLabel.setText(user == null ? "Hello!" : "Hello, " + user + "!");

        if (searchField != null) {
            searchField.setOnAction(e -> {
                String q = searchField.getText() == null ? "" : searchField.getText().trim();
                if (!q.isEmpty()) {
                    statusLabel.setText("Searching for: " + q + " (demo)");
                } else {
                    statusLabel.setText("");
                }
            });
        }
    }

    @FXML
    private void onLogout() {
        Session.logoutToLogin(userLabel);
    }

    @FXML
    private void onProfileClick() {
        String user = Session.getCurrentUser();
        statusLabel.setText("👤 Profile: " + (user == null ? "Guest" : user) + " (Profile page coming soon)");
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
        Session.goToHomeLiving(statusLabel);
    }

    @FXML
    private void openBeauty() {
        Session.goToBeauty(statusLabel);
    }

    @FXML
    private void openCart() {
        statusLabel.setText("Cart: demo screen (checkout coming next)");
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
}
