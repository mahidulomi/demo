package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Locale;

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
    private VBox productList;

    @FXML
    private TextField searchField;

    // Add Product Panel fields
    @FXML
    private VBox addProductPanel;

    @FXML
    private StackPane overlayPane;

    @FXML
    private ImageView previewImageView;

    @FXML
    private Label noImageLabel;

    @FXML
    private TextField productNameField;

    @FXML
    private TextField productPriceField;

    @FXML
    private TextField stockQuantityField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label addProductStatus;

    // Store selected image file
    private File selectedImageFile = null;

    // ── Networking: productId → UI node maps ─────────────────────────────────
    private final java.util.Map<String, Label>  netStockLabels  = new java.util.HashMap<>();

    @FXML
    private void initialize() {
        statusLabel.setText("✨ 12 Premium Beauty Products - List View");

        // Initialize category combo box
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll("Skincare", "Makeup", "Haircare");
            categoryComboBox.setValue("Skincare");
        }

        // Store all products on initialization before any filtering
        if (productList != null) {
            allProductCards = new java.util.ArrayList<>();
            for (javafx.scene.Node node : productList.getChildren()) {
                if (node instanceof VBox) {
                    VBox productRow = (VBox) node;
                    allProductCards.add(productRow);
                }
            }
            // Wire up network UI maps (FXML-defined products)
            buildNetworkMaps();
            
            // HIDE PRICES based on user request (only show in Sales)
            hidePriceLabels();
        }

        // Register as network listener
        NetworkManager.getInstance().setCurrentListener(new StockUpdateListener() {
            @Override
            public void onStockUpdated(String productId, int newQuantity) {
                handleNetworkStockUpdate(productId, newQuantity);
            }

            @Override
            public void onProductCatalogChanged() {
                syncCustomProductsFromStock();
                refreshAllStockFromManager();
            }
        });
        syncCustomProductsFromStock();
        refreshAllStockFromManager();
    }

    /**
     * Build productId → stock-label maps.
     * Always syncs from StockManager so stock is correct after page navigation.
     */
    private void buildNetworkMaps() {
        for (VBox productCard : allProductCards) {
             String productName = getProductNameFromCard(productCard);
             String productId   = StockManager.findProductIdByName(productName);
             if (productId == null) continue;
 
             // Update ID label
             for (javafx.scene.Node node : productCard.getChildren()) {
                 if (node instanceof Label lbl && lbl.getStyleClass().contains("product-id-label")) {
                     lbl.setText("ID: " + productId);
                     break;
                 }
             }

             // ── Stock label ──
             Label stockLabel = getStockLabelFromCard(productCard);
             if (stockLabel != null) {
                 netStockLabels.put(productId, stockLabel);
             }
 
             // ── KEY FIX: always read stock from StockManager
             int stock = StockManager.getStock(productId);
             applyStockToCard(productId, stock);
        }
    }

    private String getProductNameFromCard(VBox card) {
        for (javafx.scene.Node n : card.getChildren()) {
            if (n instanceof Label lbl && lbl.getStyleClass().contains("product-name")) {
                return lbl.getText();
            }
        }
        return "";
    }

    private Label getStockLabelFromCard(VBox card) {
        for (javafx.scene.Node n : card.getChildren()) {
            if (n instanceof Label lbl &&
                (lbl.getStyleClass().contains("stock-label") ||
                 lbl.getStyleClass().contains("stock-label-low") ||
                 lbl.getStyleClass().contains("stock-label-out"))) {
                return lbl;
            }
        }
        return null;
    }

    /** Called by NetworkManager when another machine changes a stock quantity. */
    private void handleNetworkStockUpdate(String productId, int newQty) {
        applyStockToCard(productId, newQty);
    }

    private void applyStockToCard(String productId, int qty) {
        Label  stockLabel = netStockLabels.get(productId);
        if (stockLabel == null) return;

        stockLabel.getStyleClass().removeAll("stock-label", "stock-label-low", "stock-label-out");
        if (qty <= 0) {
            stockLabel.setText("Stock: 0");
            stockLabel.getStyleClass().add("stock-label-out");
        } else {
            stockLabel.setText("Stock: " + qty);
            stockLabel.getStyleClass().add(getStockStyleClass(qty));
        }
        
        // Setup search field action
        if (searchField != null) {
            searchField.setOnAction(e -> onSearch());
        }
    }

    private void refreshAllStockFromManager() {
        for (String productId : netStockLabels.keySet()) {
            applyStockToCard(productId, StockManager.getStock(productId));
        }
    }

    private void hidePriceLabels() {
        if (allProductCards == null) return;
        for (VBox card : allProductCards) {
             for (javafx.scene.Node n : card.getChildren()) {
                 if (n instanceof Label) {
                     Label lbl = (Label) n;
                     // Check style classes used for prices
                     if (lbl.getStyleClass().contains("product-price") || 
                         lbl.getStyleClass().contains("product-price-discount")) {
                         lbl.setVisible(false);
                         lbl.setManaged(false);
                     }
                 }
             }
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
        java.util.List<VBox> matchedProducts = new java.util.ArrayList<>();
        
        for (VBox productCard : allProductCards) {
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
        productList.getChildren().clear();
        
        for (VBox productCard : matchedProducts) {
            productList.getChildren().add(productCard);
        }
        
        if (matchCount > 0) {
            statusLabel.setText("🔍 Found " + matchCount + " product(s) matching: \"" + query + "\"");
        } else {
            statusLabel.setText("❌ No products found matching: \"" + query + "\"");
        }
        
        System.out.println("✓ Beauty Search: " + matchCount + " matches for \"" + query + "\"");
    }


    @FXML
    private void onToggleAddProduct() {
        if (addProductPanel != null) {
            boolean isVisible = addProductPanel.isVisible();
            showAddProductPanel(!isVisible);
        }
    }

    private void showAddProductPanel(boolean show) {
        if (addProductPanel != null) {
            addProductPanel.setVisible(show);
            addProductPanel.setManaged(show);
        }
        if (overlayPane != null) {
            overlayPane.setVisible(show);
            overlayPane.setManaged(show);
        }
        if (show) {
            resetAddProductForm();
        }
    }

    @FXML
    private void onChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Open file dialog
        File file = fileChooser.showOpenDialog(addProductPanel.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            try {
                Image image = new Image(file.toURI().toString());
                previewImageView.setImage(image);
                noImageLabel.setVisible(false);
                addProductStatus.setText("✓ Image selected: " + file.getName());
                addProductStatus.setStyle("-fx-text-fill: #27ae60;");
            } catch (Exception e) {
                addProductStatus.setText("❌ Error loading image!");
                addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
            }
        }
    }

    @FXML
    private void onCancelAddProduct() {
        showAddProductPanel(false);
    }

    @FXML
    private void onSubmitProduct() {
        // Validate inputs
        String productName = productNameField.getText().trim();
        String priceText = productPriceField.getText().trim();
        String stockText = stockQuantityField.getText().trim();
        String category = categoryComboBox.getValue();

        if (productName.isEmpty()) {
            addProductStatus.setText("❌ Please enter product name!");
            addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        if (priceText.isEmpty()) {
            addProductStatus.setText("❌ Please enter product price!");
            addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText.replace(",", ""));
        } catch (NumberFormatException e) {
            addProductStatus.setText("❌ Invalid price format!");
            addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        if (stockText.isEmpty()) {
            addProductStatus.setText("❌ Please enter stock quantity!");
            addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        int stockQuantity;
        try {
            stockQuantity = Integer.parseInt(stockText.replace(",", ""));
            if (stockQuantity < 0) {
                addProductStatus.setText("❌ Stock quantity cannot be negative!");
                addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }
        } catch (NumberFormatException e) {
            addProductStatus.setText("❌ Invalid stock quantity!");
            addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        if (category == null || category.isEmpty()) {
            addProductStatus.setText("❌ Please select a category!");
            addProductStatus.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Register in StockManager with unique ID
        String newProductId = "B_custom_" + System.currentTimeMillis();

        // Create new text-only product row with stock
        VBox newProductCard = createProductCard(productName, price, category, newProductId, stockQuantity);

        // Note: Image paths from external files cannot be shared across network
        // We store empty string for now. For multi-machine support, images should be bundled with resources
        String imagePath = "";
        StockManager.addStock(newProductId, productName, "Beauty", category, stockQuantity, price, imagePath);

        // Add to network maps so real-time updates work for this card too
        Label  newStockLabel = getStockLabelFromCard(newProductCard);
        if (newStockLabel != null) netStockLabels.put(newProductId, newStockLabel);

        StockItem createdItem = new StockItem(newProductId, productName, "Beauty", category, stockQuantity, price, imagePath);
        applyStockToCard(newProductId, stockQuantity);

        // Broadcast to other machines
        NetworkManager.getInstance().broadcastNewProduct(createdItem);

        // Add to allProductCards list
        allProductCards.add(newProductCard);

        // Refresh the grid
        refreshProductGrid();

        // Show success message
        addProductStatus.setText("✓ Product added successfully!");
        addProductStatus.setStyle("-fx-text-fill: #27ae60;");
        statusLabel.setText("✓ Added: " + productName + " | Total: " + allProductCards.size() + " products");

        // Close panel after delay
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            javafx.application.Platform.runLater(() -> {
                showAddProductPanel(false);
            });
        }).start();
    }

    private VBox createProductCard(String name, double price, String category, String productId, int stockQuantity) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(920);
        card.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        card.getStyleClass().add("beauty-list-row");
        card.setUserData(category);
        card.setPadding(new Insets(14));

        // Product Name
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID: " + productId);
        idLabel.getStyleClass().add("product-id-label");

        // Product Price
        Label priceLabel = new Label(String.format("Price: ৳%.0f", price));
        priceLabel.getStyleClass().add("product-price-discount");
        priceLabel.setWrapText(true);
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setAlignment(Pos.CENTER_LEFT);
        
        // Hide price by default per request
        priceLabel.setVisible(false);
        priceLabel.setManaged(false);

        // Stock Label
        Label stockLabel = new Label("Stock: " + stockQuantity);
        stockLabel.getStyleClass().add(getStockStyleClass(stockQuantity));
        stockLabel.setAlignment(Pos.CENTER_LEFT);
        stockLabel.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(nameLabel, idLabel, priceLabel, stockLabel);

        return card;
    }

    private void syncCustomProductsFromStock() {
        if (allProductCards == null) return;

        boolean changed = false;
        for (StockItem item : StockManager.getAllStockItems()) {
            if (!"Beauty".equals(item.getCategory()) || !item.getProductId().startsWith("B_custom_")) {
                continue;
            }

            if (!netStockLabels.containsKey(item.getProductId())) {
                VBox productCard = createProductCard(
                        item.getProductName(),
                        item.getPrice(),
                        item.getSubCategory(),
                        item.getProductId(),
                        item.getQuantity()
                );

                Label newStockLabel = getStockLabelFromCard(productCard);
                if (newStockLabel != null) netStockLabels.put(item.getProductId(), newStockLabel);
                allProductCards.add(productCard);
                changed = true;
            }

            applyStockToCard(item.getProductId(), item.getQuantity());
        }

        if (changed) {
            refreshProductGrid();
        }
    }

    /**
     * Get appropriate style class based on stock level
     */
    private String getStockStyleClass(int stock) {
        if (stock <= 0) {
            return "stock-label-out";
        } else if (stock <= 10) {
            return "stock-label-low";
        } else {
            return "stock-label";
        }
    }

    private void refreshProductGrid() {
        if (productList == null) return;

        productList.getChildren().clear();

        for (VBox productCard : allProductCards) {
            productList.getChildren().add(productCard);
        }
    }

    private void resetAddProductForm() {
        if (productNameField != null) productNameField.clear();
        if (productPriceField != null) productPriceField.clear();
        if (stockQuantityField != null) stockQuantityField.clear();
        if (categoryComboBox != null) categoryComboBox.setValue("Skincare");
        if (previewImageView != null) previewImageView.setImage(null);
        if (noImageLabel != null) noImageLabel.setVisible(true);
        if (addProductStatus != null) addProductStatus.setText("");
        selectedImageFile = null;
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
    private void onOpenCart() {
        Session.goToCartFrom(statusLabel, "beauty-view.fxml");
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
        if (productList == null) return;

        // Clear the grid
        productList.getChildren().clear();

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

        // Re-add filtered products as a single-column list
        for (VBox productCard : filteredProducts) {
            productList.getChildren().add(productCard);
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
        if (productList == null) return 0;

        return (int) productList.getChildren().stream()
                .filter(node -> node instanceof VBox && node.isVisible())
                .count();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}







