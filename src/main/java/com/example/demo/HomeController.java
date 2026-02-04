package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label userLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        String user = Session.getCurrentUser();
        userLabel.setText(user == null ? "Hello!" : "Hello, " + user + "!");
    }

    @FXML
    private void onLogout() {
        Session.logoutToLogin(userLabel);
    }

    @FXML
    private void openFashion() {
        statusLabel.setText("Fashion: demo screen (products coming next)");
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
