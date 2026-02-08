package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Controller for New Arrivals page - latest products from all categories
 */
public class NewArrivalsController {

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane productGrid;

    @FXML
    private void initialize() {
        statusLabel.setText("✨ 12 Latest New Arrivals - Fresh Products from All Categories!");
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
        statusLabel.setText("✓ Added to cart: " + productName + " (New Arrival!)");

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
}

