package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForgotPasswordController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField personalDataField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("Enter username + personal data, then set a new password.");
    }

    @FXML
    private void onResetPassword() {
        String username = safe(usernameField.getText());
        String personalData = safe(personalDataField.getText());
        String newPassword = safe(newPasswordField.getText());
        String confirm = safe(confirmPasswordField.getText());

        if (username.isEmpty() || personalData.isEmpty() || newPassword.isEmpty() || confirm.isEmpty()) {
            setError("All fields are required.");
            return;
        }

        if (!UserStore.userExists(username)) {
            setError("No account found for this username.");
            return;
        }

        if (!newPassword.equals(confirm)) {
            setError("New password and confirmation must match.");
            confirmPasswordField.clear();
            return;
        }

        boolean ok = UserStore.resetPassword(username, personalData, newPassword);
        if (!ok) {
            setError("Recovery data didn't match. Try again.");
            newPasswordField.clear();
            confirmPasswordField.clear();
            return;
        }

        Session.login(username);
        setSuccess("Password updated! Opening home...");
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
