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
        statusLabel.setText("⚡ 13 Latest Electronics - All with Amazing Discounts!");
        // Store all products on initialization before any filtering
        if (productGrid != null) {
            allProductCards = new java.util.ArrayList<>();
            for (javafx.scene.Node node : productGrid.getChildren()) {
                if (node instanceof VBox) {
                    allProductCards.add((VBox) node);
                }
            }
        }
    }

    @FXML
    private void onFilterAll() {
        updateFilterButtons("All");
        filterProducts("All");
        statusLabel.setText("⚡ Showing all 13 products!");
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
    private void onProductClick(javafx.scene.input.MouseEvent event) {
        // Get the clicked VBox (product card)
        javafx.scene.layout.VBox card = (javafx.scene.layout.VBox) event.getSource();
        String productName = "Product";

        // Get product name from the label
        for (javafx.scene.Node node : card.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyleClass().contains("product-name")) {
                    productName = label.getText();
                    break;
                }
            }
        }

        // Navigate to product details page
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("product-details-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            // Pass product name to details controller
            ProductDetailsController controller = loader.getController();
            controller.setProductByName(productName);

            javafx.stage.Stage stage = (javafx.stage.Stage) card.getScene().getWindow();
            stage.setScene(scene);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            statusLabel.setText("⚠️ Error loading product details");
        }
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

        // Clear the grid
        productGrid.getChildren().clear();

        // Get all product cards from FXML and filter them
        java.util.List<VBox> allProducts = getAllProductCards();
        java.util.List<VBox> filteredProducts = new java.util.ArrayList<>();

        for (VBox productCard : allProducts) {
            String productCategory = (String) productCard.getUserData();

            if ("All".equals(category)) {
                filteredProducts.add(productCard);
            } else if (productCategory != null && productCategory.equals(category)) {
                filteredProducts.add(productCard);
            }
        }

        // Re-add filtered products to grid in proper positions (3 columns per row)
        int row = 0;
        int col = 0;
        for (VBox productCard : filteredProducts) {
            productGrid.add(productCard, col, row);
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    // Store all products on first load
    private java.util.List<VBox> allProductCards = null;

    private java.util.List<VBox> getAllProductCards() {
        if (allProductCards == null) {
            allProductCards = new java.util.ArrayList<>();
        }
        return allProductCards;
    }

    private int countVisibleProducts() {
        if (productGrid == null) return 0;

        return (int) productGrid.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .count();
    }
}

