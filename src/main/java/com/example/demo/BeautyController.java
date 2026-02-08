package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controller for Beauty page - products are built directly in FXML
 */
public class BeautyController {

    @FXML
    private Button btnAll;

    @FXML
    private Button btnSkincare;

    @FXML
    private Button btnMakeup;

    @FXML
    private Button btnHaircare;

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane productGrid;

    @FXML
    private void initialize() {
        statusLabel.setText("✨ 12 Premium Beauty Products - All with Amazing Discounts!");
    }

    @FXML
    private void onFilterAll() {
        updateFilterButtons("All");
        filterProducts("All");
        statusLabel.setText("✨ Showing all 12 products!");
    }

    @FXML
    private void onFilterSkincare() {
        updateFilterButtons("Skincare");
        filterProducts("Skincare");
        int count = countVisibleProducts();
        statusLabel.setText("🧴 Skincare category selected - " + count + " glowing products!");
    }

    @FXML
    private void onFilterMakeup() {
        updateFilterButtons("Makeup");
        filterProducts("Makeup");
        int count = countVisibleProducts();
        statusLabel.setText("💄 Makeup category selected - " + count + " stunning products!");
    }

    @FXML
    private void onFilterHaircare() {
        updateFilterButtons("Haircare");
        filterProducts("Haircare");
        int count = countVisibleProducts();
        statusLabel.setText("💇 Haircare category selected - " + count + " beautiful products!");
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
        btnAll.getStyleClass().removeAll("beauty-filter-active");
        btnSkincare.getStyleClass().removeAll("beauty-filter-active");
        btnMakeup.getStyleClass().removeAll("beauty-filter-active");
        btnHaircare.getStyleClass().removeAll("beauty-filter-active");

        // Add active class to selected button
        Button activeButton = switch (activeFilter) {
            case "Skincare" -> btnSkincare;
            case "Makeup" -> btnMakeup;
            case "Haircare" -> btnHaircare;
            default -> btnAll;
        };

        if (!activeButton.getStyleClass().contains("beauty-filter-active")) {
            activeButton.getStyleClass().add("beauty-filter-active");
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

