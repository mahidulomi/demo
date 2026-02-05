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
    private void openFashion() {
        Session.goToFashion(statusLabel);
    }

    @FXML
    private void openElectronics() {
        statusLabel.setText("Electronics: demo screen (products coming next)");
    }

    @FXML
    private void openHomeLiving() {
        statusLabel.setText("Home & Living: demo screen (products coming next)");
    }

    @FXML
    private void openBeauty() {
        statusLabel.setText("Beauty: demo screen (products coming next)");
    }

    @FXML
    private void openCart() {
        statusLabel.setText("Cart: demo screen (checkout coming next)");
    }

    @FXML
    private void openOrders() {
        statusLabel.setText("Orders: demo screen");
    }
}
