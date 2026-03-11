package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

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
    private GridPane productGrid;

    @FXML
    private TextField searchField;

    // Store all products on initialization
    private java.util.List<javafx.scene.layout.VBox> allProductCards = null;

    @FXML
    private void initialize() {
        statusLabel.setText("✨ 12 Beautiful Products - All with Amazing Discounts!");

        // Store all products on initialization before any filtering
        if (productGrid != null) {
            allProductCards = new java.util.ArrayList<>();
            for (javafx.scene.Node node : productGrid.getChildren()) {
                if (node instanceof javafx.scene.layout.VBox) {
                    allProductCards.add((javafx.scene.layout.VBox) node);
                }
            }
            System.out.println("✓ Fashion: Stored " + allProductCards.size() + " products for search");
        }

        // Setup search field action
        if (searchField != null) {
            searchField.setOnAction(e -> onSearch());
        }
    }

    @FXML
    private void onSearch() {
        String query = safe(searchField.getText()).toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {
            statusLabel.setText("Type a product name or category to search.");
            filterProducts("All");
            return;
        }

        // Make sure we have the products list
        if (allProductCards == null || allProductCards.isEmpty()) {
            statusLabel.setText("⚠️ Product list not loaded yet!");
            return;
        }

        // Search through ALL products (not just currently visible ones)
        int matchCount = 0;
        java.util.List<javafx.scene.layout.VBox> matchedProducts = new java.util.ArrayList<>();
        
        for (javafx.scene.layout.VBox productCard : allProductCards) {
            boolean matches = false;

            // Check product name
            for (javafx.scene.Node child : productCard.getChildren()) {
                if (child instanceof Label) {
                    Label label = (Label) child;
                    if (label.getStyleClass().contains("product-name")) {
                        String productName = label.getText().toLowerCase(Locale.ROOT);
                        if (productName.contains(query)) {
                            matches = true;
                            break;
                        }
                    }
                }
            }

            // Check category if name doesn't match
            if (!matches) {
                String category = (String) productCard.getUserData();
                if (category != null && category.toLowerCase(Locale.ROOT).contains(query)) {
                    matches = true;
                }
            }

            if (matches) {
                matchedProducts.add(productCard);
                matchCount++;
            }
        }

        // Clear grid and show only matched products
        productGrid.getChildren().clear();
        
        int row = 0;
        int col = 0;
        for (javafx.scene.layout.VBox productCard : matchedProducts) {
            productGrid.add(productCard, col, row);
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }

        if (matchCount > 0) {
            statusLabel.setText("🔍 Found " + matchCount + " product(s) matching: \"" + query + "\"");
        } else {
            statusLabel.setText("❌ No products found matching: \"" + query + "\"");
        }
        
        System.out.println("✓ Search completed: " + matchCount + " matches for \"" + query + "\"");
    }

    @FXML
    private void onFilterAll() {
        updateFilterButtons("All");
        filterProducts("All");
        statusLabel.setText("✨ Showing all 12 products!");
    }

    @FXML
    private void onFilterBaby() {
        updateFilterButtons("Baby");
        filterProducts("Baby");
        int count = countVisibleProducts();
        statusLabel.setText("👶 Baby category selected - " + count + " adorable products!");
    }

    @FXML
    private void onFilterMale() {
        updateFilterButtons("Male");
        filterProducts("Male");
        int count = countVisibleProducts();
        statusLabel.setText("👔 Male category selected - " + count + " stylish products!");
    }

    @FXML
    private void onFilterFemale() {
        updateFilterButtons("Female");
        filterProducts("Female");
        int count = countVisibleProducts();
        statusLabel.setText("👗 Female category selected - " + count + " elegant products!");
    }

    @FXML
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }

    @FXML
    private void onOpenCart() {
        Session.goToCartFrom(statusLabel, "fashion-view.fxml");
    }

    @FXML
    private void onAddToCart(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String originalText = btn.getText();

        // Get parent HBox first, then VBox (product card)
        javafx.scene.Node parent = btn.getParent();
        javafx.scene.layout.VBox card = null;
        
        if (parent instanceof javafx.scene.layout.HBox) {
            // Button is inside HBox, get HBox's parent which is VBox
            card = (javafx.scene.layout.VBox) parent.getParent();
        } else if (parent instanceof javafx.scene.layout.VBox) {
            // Direct parent is VBox
            card = (javafx.scene.layout.VBox) parent;
        }
        
        if (card == null) {
            System.err.println("❌ Could not find product card!");
            statusLabel.setText("❌ Error adding to cart!");
            return;
        }
        
        String productName = "Product";
        String productPrice = "0";
        int discountPercent = 0;
        String productId = "";

        for (javafx.scene.Node node : card.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyleClass().contains("product-name")) {
                    productName = label.getText();
                    productId = "F_" + productName.hashCode();
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
            Cart.addItem(productId, productName, "Fashion", price, 1, "", discountPercent);
        } catch (NumberFormatException e) {
            Cart.addItem(productId, productName, "Fashion", 0, 1, "", 0);
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

    private void filterProducts(String category) {
        if (productGrid == null) return;
        
        // Use stored products list if available
        if (allProductCards == null || allProductCards.isEmpty()) {
            // Fallback to old method if list not initialized
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
            return;
        }
        
        // Clear grid first
        productGrid.getChildren().clear();
        
        // Filter and re-add products
        java.util.List<javafx.scene.layout.VBox> filteredProducts = new java.util.ArrayList<>();
        
        for (javafx.scene.layout.VBox productCard : allProductCards) {
            String productCategory = (String) productCard.getUserData();
            
            if ("All".equals(category)) {
                filteredProducts.add(productCard);
            } else if (productCategory != null && productCategory.equals(category)) {
                filteredProducts.add(productCard);
            }
        }
        
        // Re-add filtered products to grid (3 columns per row)
        int row = 0;
        int col = 0;
        for (javafx.scene.layout.VBox productCard : filteredProducts) {
            productGrid.add(productCard, col, row);
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
        
        System.out.println("✓ Filter: Showing " + filteredProducts.size() + " products in category: " + category);
    }

    private int countVisibleProducts() {
        if (productGrid == null) return 0;

        return (int) productGrid.getChildren().stream()
                .filter(node -> node instanceof VBox && node.isVisible())
                .count();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
