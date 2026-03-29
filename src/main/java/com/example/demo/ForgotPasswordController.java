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
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
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

        // Validate username format (only letters, numbers, and underscore)
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            setError("Username can only contain letters, numbers, and underscores.");
            return;
        }

        if (!UserStore.userExists(username)) {
            setError("No account found for this username.");
            return;
        }

        // Validate password length (6-8 characters)
        if (newPassword.length() < 6 || newPassword.length() > 8) {
            setError("New password must be 6-8 characters long.");
            newPasswordField.clear();
            confirmPasswordField.clear();
            return;
        }

        if (!newPassword.equals(confirm)) {
            setError("Passwords do not match.");
            confirmPasswordField.clear();
            return;
        }

        boolean success = UserStore.resetPassword(username, personalData, newPassword);
        if (success) {
            setSuccess("Password reset successfully! Please log in.");
            // auto navigate to login on success
            Session.goToLogin(statusLabel);
        } else {
            setError("Failed to reset password. Please try again.");
            newPasswordField.clear();
            confirmPasswordField.clear();
        }
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
