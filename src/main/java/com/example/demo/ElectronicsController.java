package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
        statusLabel.setText("⚡ 13 Latest Electronics - 5 with Amazing Discounts!");
        // Store all products on initialization before any filtering
        if (productGrid != null) {
            allProductCards = new java.util.ArrayList<>();
            for (javafx.scene.Node node : productGrid.getChildren()) {
                if (node instanceof VBox) {
                    VBox productCard = (VBox) node;
                    allProductCards.add(productCard);

                    // Setup arrow button handlers for each product card
                    setupArrowButtons(productCard);
                }
            }
        }
    }

    /**
     * Setup arrow button click handlers for quantity increase/decrease
     * Layout: [Add to Cart] [▼ 1 ▲]
     */
    private void setupArrowButtons(VBox productCard) {
        for (javafx.scene.Node child : productCard.getChildren()) {
            if (child instanceof HBox) {
                HBox mainHbox = (HBox) child;
                for (javafx.scene.Node hboxChild : mainHbox.getChildren()) {
                    // Find the qty-box HBox
                    if (hboxChild instanceof HBox) {
                        HBox qtyBox = (HBox) hboxChild;
                        if (qtyBox.getStyleClass().contains("qty-box")) {
                            Button downBtn = null;
                            Button upBtn = null;
                            Label qtyLabel = null;

                            for (javafx.scene.Node qtyChild : qtyBox.getChildren()) {
                                if (qtyChild instanceof Button) {
                                    Button btn = (Button) qtyChild;
                                    if ("▼".equals(btn.getText())) {
                                        downBtn = btn;
                                    } else if ("▲".equals(btn.getText())) {
                                        upBtn = btn;
                                    }
                                } else if (qtyChild instanceof Label) {
                                    Label lbl = (Label) qtyChild;
                                    if (lbl.getStyleClass().contains("qty-count")) {
                                        qtyLabel = lbl;
                                    }
                                }
                            }

                            // Set up event handlers - ▲ increases, ▼ decreases
                            if (upBtn != null && qtyLabel != null) {
                                final Label finalQtyLabel = qtyLabel;
                                upBtn.setOnAction(e -> {
                                    e.consume(); // Prevent card click
                                    int currentQty = Integer.parseInt(finalQtyLabel.getText());
                                    finalQtyLabel.setText(String.valueOf(currentQty + 1));
                                });
                            }

                            if (downBtn != null && qtyLabel != null) {
                                final Label finalQtyLabel = qtyLabel;
                                downBtn.setOnAction(e -> {
                                    e.consume(); // Prevent card click
                                    int currentQty = Integer.parseInt(finalQtyLabel.getText());
                                    if (currentQty > 1) {
                                        finalQtyLabel.setText(String.valueOf(currentQty - 1));
                                    }
                                });
                            }
                        }
                    }
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

