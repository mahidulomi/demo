package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField personalDataField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("Fill all fields to create your account.");
    }

    @FXML
    private void onCreateAccount() {
        String username = safe(usernameField.getText());
        String password = safe(passwordField.getText());
        String confirm = safe(confirmPasswordField.getText());
        String personalData = safe(personalDataField.getText());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || personalData.isEmpty()) {
            setError("All fields are required.");
            return;
        }

        if (!password.equals(confirm)) {
            setError("Password and Confirm Password must match.");
            confirmPasswordField.clear();
            return;
        }

        boolean created = UserStore.createUser(username, password, personalData);
        if (!created) {
            setError("Account create failed: username exists or local save failed.");
            return;
        }

        // Auto-login and go to home directly.
        Session.login(username);
        setSuccess("Account created! Opening home...");
        Session.goToHome(statusLabel);
    }

    @FXML
    private void onBackToLogin() {
        Session.goToLogin(statusLabel);
    }

    private void setError(String msg) {
        statusLabel.getStyleClass().removeAll("success-label");
        if (!statusLabel.getStyleClass().contains("error-label")) {
            statusLabel.getStyleClass().add("error-label");
        }
        statusLabel.setText(msg);
    }

    private void setSuccess(String msg) {
        statusLabel.getStyleClass().removeAll("error-label");
        if (!statusLabel.getStyleClass().contains("success-label")) {
            statusLabel.getStyleClass().add("success-label");
        }
        statusLabel.setText(msg);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
