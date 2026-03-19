package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class HelloController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label welcomeText;

    @FXML
    private javafx.scene.layout.AnchorPane networkPane;

    @FXML
    private TextField serverIpField;

    @FXML
    private TextField serverPortField;

    @FXML
    private Label networkStatusLabel;


    @FXML
    private void initialize() {
        setInfo("Please login. If you don't have an account, click Sign Up.");
        
        // Listen for user sync completion
        NetworkManager.getInstance().setUserSyncCallback(() -> {
            javafx.application.Platform.runLater(() -> {
                if (networkStatusLabel != null) networkStatusLabel.setText("✅ Users Synced Successfully!");
            });
        });
    }

    @FXML
    private void onToggleNetwork() {
        if (networkPane != null) {
            networkPane.setVisible(!networkPane.isVisible());
        }
    }

    @FXML
    private void onNetworkConnect() {
        String ip = safe(serverIpField.getText());
        String portTxt = safe(serverPortField.getText());

        if (ip.isEmpty() || portTxt.isEmpty()) {
            if (networkStatusLabel != null) networkStatusLabel.setText("IP and Port required.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portTxt);
        } catch (NumberFormatException e) {
            if (networkStatusLabel != null) networkStatusLabel.setText("Invalid port.");
            return;
        }

        if (networkStatusLabel != null) networkStatusLabel.setText("Connecting...");

        new Thread(() -> {
            try {
                NetworkManager.getInstance().shutdown();
                NetworkManager.getInstance().connectToServer(ip, port);
                
                javafx.application.Platform.runLater(() -> {
                     if (networkStatusLabel != null) networkStatusLabel.setText("Connected! Waiting for users...");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    if (networkStatusLabel != null) networkStatusLabel.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void onLogin() {
        String username = safe(usernameField.getText());
        String password = safe(passwordField.getText());

        if (username.isEmpty() || password.isEmpty()) {
            setError("Username & password required.");
            return;
        }

        // If no users exist or this user doesn't exist yet, block login.
        if (!UserStore.userExists(username)) {
            setError("No account found. Please Sign Up first.");
            return;
        }

        if (!UserStore.validateLogin(username, password)) {
            setError("Wrong username or password.");
            return;
        }

        Session.login(username);
        setSuccess("Login success! Opening home...");
        Session.goToHome(welcomeText);
    }

    @FXML
    private void onSignUp() {
        Session.goToSignUp(welcomeText);
    }

    @FXML
    private void onForgotPassword() {
        Session.goToForgotPassword(welcomeText);
    }

    private void setError(String msg) {
        welcomeText.getStyleClass().removeAll("success-label");
        if (!welcomeText.getStyleClass().contains("error-label")) {
            welcomeText.getStyleClass().add("error-label");
        }
        welcomeText.setText(msg);
    }

    private void setSuccess(String msg) {
        welcomeText.getStyleClass().removeAll("error-label");
        if (!welcomeText.getStyleClass().contains("success-label")) {
            welcomeText.getStyleClass().add("success-label");
        }
        welcomeText.setText(msg);
    }

    private void setInfo(String msg) {
        welcomeText.getStyleClass().removeAll("error-label", "success-label");
        welcomeText.setText(msg);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}