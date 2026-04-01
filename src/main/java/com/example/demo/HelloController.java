package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

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
        // Only use setInfo implicitly without setting any text to avoid initial label display
        welcomeText.setText("");
        welcomeText.getStyleClass().removeAll("error-label", "success-label");
        
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

        // Reset field styles
        resetFieldStyles();

        boolean hasError = false;

        // Validate username
        if (username.isEmpty()) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Username is required.");
            hasError = true;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Username can only contain letters, numbers, and underscores.");
            hasError = true;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            if (!hasError) setError("Password is required.");
            hasError = true;
        } else if (password.length() < 6 || password.length() > 8) {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Password must be 6 to 8 characters.");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // If no users exist or this user doesn't exist yet, block login.
        if (!UserStore.userExists(username)) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("No account found. Check your username or Sign Up.");
            return;
        }

        if (!UserStore.validateLogin(username, password)) {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Wrong username or password.");
            return;
        }

        Session.login(username);
        setSuccess("Login success! Opening home...");
        
        String role = UserStore.getRole(username);
        try {
            if ("Customer".equalsIgnoreCase(role)) {
                Session.goToCustomerHome(welcomeText);
            } else {
                Session.goToHome(welcomeText);
            }
        } catch (Exception e) {
            String errorMsg = "Error loading page: " + e.getMessage();
            setError(errorMsg);
            System.err.println("[HelloController] Error navigating to home: " + e);
            e.printStackTrace();
            showErrorDialog("Navigation Error", errorMsg, e);
        }
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

    private void resetFieldStyles() {
        usernameField.setStyle("");
        passwordField.setStyle("");
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void showErrorDialog(String title, String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        String fullMessage = message + "\n\nRoot cause: " + e.getClass().getName() + ": " + e.getMessage();
        alert.setContentText(fullMessage);
        alert.showAndWait();
    }
}