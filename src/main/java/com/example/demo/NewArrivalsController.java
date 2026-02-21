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
    private void onOpenCart() {
        Session.goToCartFrom(statusLabel, "new-arrivals-view.fxml");
    }

    @FXML
    private void onAddToCart(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String originalText = btn.getText();

        // Get product name from the parent VBox
        javafx.scene.layout.VBox card = (javafx.scene.layout.VBox) btn.getParent();
        String productName = "Product";
        String productPrice = "0";
        int discountPercent = 0;
        String productId = "";

        for (javafx.scene.Node node : card.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyleClass().contains("product-name")) {
                    productName = label.getText();
                    productId = "NA_" + productName.hashCode();
                } else if (label.getStyleClass().contains("product-price-discount")) {
                    String priceText = label.getText();
                    if (priceText.contains("(was")) {
                        String[] parts = priceText.split("\\(was");
                        productPrice = parts[0].replace("BDT", "").replace(",", "").trim();
                        String originalPrice = parts[1].replace("BDT", "").replace(")", "").replace(",", "").trim();
                        try {
                            double discounted = Double.parseDouble(productPrice);
                            double original = Double.parseDouble(originalPrice);
                            discountPercent = (int) Math.round((1 - discounted / original) * 100);
                            productPrice = originalPrice;
                        } catch (NumberFormatException e) {
                            productPrice = "0";
                        }
                    } else {
                        productPrice = priceText.replace("BDT", "").replace(",", "").trim();
                    }
                }
            }
        }

        // Add to cart
        try {
            double price = Double.parseDouble(productPrice);
            Cart.addItem(productId, productName, "New Arrivals", price, 1, "", discountPercent);
        } catch (NumberFormatException e) {
            Cart.addItem(productId, productName, "New Arrivals", 0, 1, "", 0);
        }

        // Update button and status
        btn.setText("✓ Added!");
        statusLabel.setText("✓ Added to cart: " + productName + " (New Arrival!) | Cart Total: " + Cart.getTotalQuantity() + " items");

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

