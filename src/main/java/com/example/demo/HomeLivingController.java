package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controller for Home & Living page - products are built directly in FXML
 */
public class HomeLivingController {

    @FXML
    private Button btnAll;

    @FXML
    private Button btnFurniture;

    @FXML
    private Button btnDecor;

    @FXML
    private Button btnKitchen;

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane productGrid;

    @FXML
    private void initialize() {
        statusLabel.setText("🏡 12 Home & Living Products - All with Amazing Discounts!");
    }

    @FXML
    private void onFilterAll() {
        updateFilterButtons("All");
        filterProducts("All");
        statusLabel.setText("🏡 Showing all 12 products!");
    }

    @FXML
    private void onFilterFurniture() {
        updateFilterButtons("Furniture");
        filterProducts("Furniture");
        int count = countVisibleProducts();
        statusLabel.setText("🛋️ Furniture category selected - " + count + " stylish pieces!");
    }

    @FXML
    private void onFilterDecor() {
        updateFilterButtons("Decor");
        filterProducts("Decor");
        int count = countVisibleProducts();
        statusLabel.setText("🖼️ Decor category selected - " + count + " beautiful items!");
    }

    @FXML
    private void onFilterKitchen() {
        updateFilterButtons("Kitchen");
        filterProducts("Kitchen");
        int count = countVisibleProducts();
        statusLabel.setText("🍳 Kitchen category selected - " + count + " essential tools!");
    }

    @FXML
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }

    @FXML
    private void onOpenCart() {
        Session.goToCartFrom(statusLabel, "homeliving-view.fxml");
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
                    productId = "HL_" + productName.hashCode();
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
            Cart.addItem(productId, productName, "Home & Living", price, 1, "", discountPercent);
        } catch (NumberFormatException e) {
            Cart.addItem(productId, productName, "Home & Living", 0, 1, "", 0);
        }

        // Update button and status
        btn.setText("✓ Added!");
        statusLabel.setText("✓ Added to cart: " + productName + " | Cart Total: " + Cart.getTotalQuantity() + " items");

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
        btnAll.getStyleClass().removeAll("homeliving-filter-active");
        btnFurniture.getStyleClass().removeAll("homeliving-filter-active");
        btnDecor.getStyleClass().removeAll("homeliving-filter-active");
        btnKitchen.getStyleClass().removeAll("homeliving-filter-active");

        // Add active class to selected button
        Button activeButton = switch (activeFilter) {
            case "Furniture" -> btnFurniture;
            case "Decor" -> btnDecor;
            case "Kitchen" -> btnKitchen;
            default -> btnAll;
        };

        if (!activeButton.getStyleClass().contains("homeliving-filter-active")) {
            activeButton.getStyleClass().add("homeliving-filter-active");
        }
    }

    private void filterProducts(String category) {
        if (productGrid == null) return;

        productGrid.getChildren().forEach(node -> {
            if (node instanceof VBox productCard) {
                String productCategory = (String) productCard.getUserData();

                if ("All".equals(category)) {
                    productCard.setVisible(true);
                    productCard.setManaged(true);
                } else if (productCategory != null && productCategory.equals(category)) {
                    productCard.setVisible(true);
                    productCard.setManaged(true);
                } else {
                    productCard.setVisible(false);
                    productCard.setManaged(false);
                }
            }
        });
    }

    private int countVisibleProducts() {
        if (productGrid == null) return 0;

        return (int) productGrid.getChildren().stream()
                .filter(node -> node instanceof VBox && node.isVisible())
                .count();
    }
}

