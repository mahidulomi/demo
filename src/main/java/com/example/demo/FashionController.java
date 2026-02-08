package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Controller for Fashion page - products are built directly in FXML
 */
public class FashionController {

    @FXML
    private Button btnAll;

    @FXML
    private Button btnBaby;

    @FXML
    private Button btnMale;

    @FXML
    private Button btnFemale;

    @FXML
    private Label statusLabel;

    @FXML
    private void initialize() {
        statusLabel.setText("✨ 12 Beautiful Products - All with Amazing Discounts!");
    }

    @FXML
    private void onFilterAll() {
        updateFilterButtons("All");
        statusLabel.setText("✨ Showing all 12 products!");
    }

    @FXML
    private void onFilterBaby() {
        updateFilterButtons("Baby");
        statusLabel.setText("👶 Baby category selected - 4 adorable products!");
    }

    @FXML
    private void onFilterMale() {
        updateFilterButtons("Male");
        statusLabel.setText("👔 Male category selected - 4 stylish products!");
    }

    @FXML
    private void onFilterFemale() {
        updateFilterButtons("Female");
        statusLabel.setText("👗 Female category selected - 4 elegant products!");
    }

    @FXML
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }

    @FXML
    private void onAddToCart(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String originalText = btn.getText();

        // Get product name from the parent VBox
        javafx.scene.layout.VBox card = (javafx.scene.layout.VBox) btn.getParent();
        String productName = "Product";

        for (javafx.scene.Node node : card.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyleClass().contains("product-name")) {
                    productName = label.getText();
                    break;
                }
            }
        }

        // Update button and status
        btn.setText("✓ Added!");
        statusLabel.setText("✓ Added to cart: " + productName);

        // Reset button text after delay
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            javafx.application.Platform.runLater(() -> btn.setText(originalText));
        }).start();
    }

    private void updateFilterButtons(String activeFilter) {
        // Remove active class from all buttons
        btnAll.getStyleClass().removeAll("fashion-filter-active");
        btnBaby.getStyleClass().removeAll("fashion-filter-active");
        btnMale.getStyleClass().removeAll("fashion-filter-active");
        btnFemale.getStyleClass().removeAll("fashion-filter-active");

        // Add active class to selected button
        Button activeButton = switch (activeFilter) {
            case "Baby" -> btnBaby;
            case "Male" -> btnMale;
            case "Female" -> btnFemale;
            default -> btnAll;
        };

        if (!activeButton.getStyleClass().contains("fashion-filter-active")) {
            activeButton.getStyleClass().add("fashion-filter-active");
        }
    }
}
