package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;

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
    private ComboBox<String> roleComboBox;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        if (roleComboBox != null) {
            roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Customer"));
            roleComboBox.getSelectionModel().selectFirst();

            // Make role items bold with larger font
            roleComboBox.setCellFactory(param -> new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
                    }
                }
            });

            // Style the button cell (selected value) - EXTRA BOLD
            roleComboBox.setButtonCell(new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white; -fx-padding: 5px;");
                    }
                }
            });

            // Apply CSS styling to the ComboBox itself
            roleComboBox.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        }
    }

    @FXML
    private void onCreateAccount() {
        String username = safe(usernameField.getText());
        String password = safe(passwordField.getText());
        String confirm = safe(confirmPasswordField.getText());
        String personalData = safe(personalDataField.getText());
        String role = roleComboBox != null && roleComboBox.getValue() != null ? roleComboBox.getValue() : "Admin";

        // Reset field styles
        resetFieldStyles();

        boolean hasError = false;

        if (username.isEmpty()) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Username is required.");
            hasError = true;
        }

        if (password.isEmpty()) {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            if (!hasError) setError("Password is required.");
            hasError = true;
        }

        if (confirm.isEmpty()) {
            confirmPasswordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            if (!hasError) setError("Confirm Password is required.");
            hasError = true;
        }

        if (personalData.isEmpty()) {
            personalDataField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            if (!hasError) setError("Recovery Data is required.");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Validate username format (only letters, numbers, and underscore)
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Username can only contain letters, numbers, and underscores.");
            return;
        }

        // Validate password length (6-8 characters)
        if (password.length() < 6 || password.length() > 8) {
            passwordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Password must be 6-8 characters long.");
            passwordField.clear();
            confirmPasswordField.clear();
            return;
        }

        if (!password.equals(confirm)) {
            confirmPasswordField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Password and Confirm Password must match.");
            confirmPasswordField.clear();
            return;
        }

        if (UserStore.userExists(username)) {
            usernameField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            setError("Username already taken!");
            return;
        }

        boolean success = UserStore.createUser(username, password, personalData, role);
        if (success) {
            setSuccess("Account created successfully!");
            // Auto login after sign up
            Session.login(username);
            
            try {
                if ("Customer".equals(role)) {
                    Session.goToCustomerHome(statusLabel);
                } else {
                    Session.goToHome(statusLabel);
                }
            } catch (Exception e) {
                String errorMsg = "Error loading page: " + e.getMessage();
                setError(errorMsg);
                System.err.println("[SignUpController] Error navigating to home: " + e);
                e.printStackTrace();
                showErrorDialog("Navigation Error", errorMsg, e);
            }
        } else {
            setError("Account creation failed. Please try again.");
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

    private void resetFieldStyles() {
        usernameField.setStyle("");
        passwordField.setStyle("");
        confirmPasswordField.setStyle("");
        personalDataField.setStyle("");
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

    private void showErrorDialog(String title, String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        String fullMessage = message + "\n\nRoot cause: " + e.getClass().getName() + ": " + e.getMessage();
        alert.setContentText(fullMessage);
        alert.showAndWait();
    }
}
