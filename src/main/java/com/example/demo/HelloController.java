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
    private void initialize() {
        setInfo("Please login. If you don't have an account, click Sign Up.");
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