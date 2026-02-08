package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controller for Electronics page - products are built directly in FXML
 */
public class ElectronicsController {

    @FXML
    private Button btnAll;

    @FXML
    private Button btnMobile;

    @FXML
    private Button btnLaptop;

    @FXML
    private Button btnAccessories;

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane productGrid;

    @FXML
    private void initialize() {
        statusLabel.setText("⚡ 12 Latest Electronics - All with Amazing Discounts!");
    }

    @FXML
    private void onFilterAll() {
        updateFilterButtons("All");
        filterProducts("All");
        statusLabel.setText("⚡ Showing all 12 products!");
    }

    @FXML
    private void onFilterMobile() {
        updateFilterButtons("Mobile");
        filterProducts("Mobile");
        int count = countVisibleProducts();
        statusLabel.setText("📱 Mobile category selected - " + count + " smart devices!");
    }

    @FXML
    private void onFilterLaptop() {
        updateFilterButtons("Laptop");
        filterProducts("Laptop");
        int count = countVisibleProducts();
        statusLabel.setText("💻 Laptop category selected - " + count + " powerful machines!");
    }

    @FXML
    private void onFilterAccessories() {
        updateFilterButtons("Accessories");
        filterProducts("Accessories");
        int count = countVisibleProducts();
        statusLabel.setText("🎧 Accessories category selected - " + count + " essential items!");
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
        btnAll.getStyleClass().removeAll("electronics-filter-active");
        btnMobile.getStyleClass().removeAll("electronics-filter-active");
        btnLaptop.getStyleClass().removeAll("electronics-filter-active");
        btnAccessories.getStyleClass().removeAll("electronics-filter-active");

        // Add active class to selected button
        Button activeButton = switch (activeFilter) {
            case "Mobile" -> btnMobile;
            case "Laptop" -> btnLaptop;
            case "Accessories" -> btnAccessories;
            default -> btnAll;
        };

        if (!activeButton.getStyleClass().contains("electronics-filter-active")) {
            activeButton.getStyleClass().add("electronics-filter-active");
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

