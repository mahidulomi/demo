package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ComboBox;
import java.io.File;
import java.net.URI;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

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

    // Store all products on initialization
    private java.util.List<VBox> allProductCards = null;

    // ── Networking: productId → UI node maps ─────────────────────────────────
    private final java.util.Map<String, Label>  netStockLabels  = new java.util.HashMap<>();
    private final java.util.Map<String, Button> netAddBtns      = new java.util.HashMap<>();
    private final java.util.Map<VBox, String>   cardProductIds  = new java.util.HashMap<>();
    private final java.util.Map<String, Label>  cartStatusLabels = new java.util.HashMap<>();

    @FXML
    private void initialize() {
        statusLabel.setText("✨ 12 Beautiful Products - All with Amazing Discounts!");

        // Initialize category combo box
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll("Baby", "Male", "Female");
            categoryComboBox.setValue("Baby");
        }

        // Setup arrow button click handlers for quantity increase/decrease
        setupArrowButtons();

        // Setup search field action
        if (searchField != null) {
            searchField.setOnAction(e -> onSearch());
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

    // ==================== ADD PRODUCT METHODS ====================

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
        String newProductId = "F_custom_" + System.currentTimeMillis();

        // Create new product row with stock
        VBox newProductCard = createProductCard(productName, price, category, newProductId, stockQuantity);

         // Use selected image path if available, otherwise check for defaults
         String imagePath = "";
         if (selectedImageFile != null && selectedImageFile.exists()) {
             imagePath = selectedImageFile.toURI().toString();
         } else {
             // Auto-assign image path for known products
             imagePath = getDefaultImagePathForProduct(productName);
         }
         StockManager.addStock(newProductId, productName, "Fashion", category, stockQuantity, price, imagePath);

        // Add to network maps so real-time updates work for this card too
        Label  newStockLabel = getStockLabelFromCard(newProductCard);
        Button newAddBtn     = getAddBtnFromCard(newProductCard);
        if (newStockLabel != null) netStockLabels.put(newProductId, newStockLabel);
        if (newAddBtn     != null) netAddBtns.put(newProductId, newAddBtn);
        cardProductIds.put(newProductCard, newProductId);

        StockItem createdItem = new StockItem(newProductId, productName, "Fashion", category, stockQuantity, price, imagePath);
        applyStockToCard(newProductId, stockQuantity);

        // Broadcast new product to other machines
        NetworkManager.getInstance().broadcastNewProduct(createdItem);

        // Add to allProductCards list
        allProductCards.add(newProductCard);

        // Refresh the grid
        refreshProductGrid();

        // Show success message
        addProductStatus.setText("✓ Product added successfully!");
        addProductStatus.setStyle("-fx-text-fill: #27ae60;");
        statusLabel.setText("✓ Added: " + productName + " (Stock: " + stockQuantity + ") | Total: " + allProductCards.size() + " products");

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
        card.setPrefHeight(120);
        card.getStyleClass().add("product-card");
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

        // Stock Status Label (will be updated by network map logic)
        Label stockLabel = new Label("📦 Stock: " + stockQuantity);
        stockLabel.getStyleClass().add(getStockStyleClass(stockQuantity));
        card.getChildren().addAll(nameLabel, idLabel, priceLabel, stockLabel);

        return card;
    }


    private void refreshProductGrid() {
        if (productGrid == null) return;

        productGrid.getChildren().clear();

        int row = 0;
        int col = 0;
        for (VBox productCard : allProductCards) {
            productGrid.add(productCard, col, row);
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    private void resetAddProductForm() {
        if (productNameField != null) productNameField.clear();
        if (productPriceField != null) productPriceField.clear();
        if (stockQuantityField != null) stockQuantityField.clear();
        if (categoryComboBox != null) categoryComboBox.setValue("Baby");
        if (previewImageView != null) previewImageView.setImage(null);
        if (noImageLabel != null) noImageLabel.setVisible(true);
        if (addProductStatus != null) addProductStatus.setText("");
        selectedImageFile = null;
    }

    private File parseImageFile(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        try {
            if (imagePath.startsWith("file:/")) {
                return new File(new URI(imagePath));
            }
            return new File(imagePath);
        } catch (Exception e) {
            return null;
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

    private void syncCustomProductsFromStock() {
        if (allProductCards == null) return;

        boolean changed = false;
        for (StockItem item : StockManager.getAllStockItems()) {
            if (!"Fashion".equals(item.getCategory()) || !item.getProductId().startsWith("F_custom_")) {
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
                Button newAddBtn = getAddBtnFromCard(productCard);
                if (newStockLabel != null) netStockLabels.put(item.getProductId(), newStockLabel);
                if (newAddBtn != null) netAddBtns.put(item.getProductId(), newAddBtn);
                cardProductIds.put(productCard, item.getProductId());
                allProductCards.add(productCard);
                changed = true;
            }

            applyStockToCard(item.getProductId(), item.getQuantity());
        }

        if (changed) {
            // Sort all product cards by name alphabetically before refreshing grid
            allProductCards.sort((card1, card2) -> {
                String name1 = getProductNameFromCard(card1);
                String name2 = getProductNameFromCard(card2);
                return name1.compareToIgnoreCase(name2);
            });
            refreshProductGrid();
        }
    }

    /**
     * Setup arrow button click handlers for quantity increase/decrease
     * Layout: [Add to Cart] [▼ 1 ▲]
     */
    private void setupArrowButtons() {
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

            // Sort all product cards by name alphabetically
            allProductCards.sort((card1, card2) -> {
                String name1 = getProductNameFromCard(card1);
                String name2 = getProductNameFromCard(card2);
                return name1.compareToIgnoreCase(name2);
            });

            System.out.println("✓ Fashion: Stored " + allProductCards.size() + " products for search");
            // Wire up network UI maps (FXML-defined products)
            buildNetworkMaps();
            
            // HIDE PRICE
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

    private void setupArrowButtons(VBox productCard) {
        // List-only rows do not include cart/quantity controls.
        if (getAddBtnFromCard(productCard) == null) {
            return;
        }

        // 1. Ensure there is an HBox with "Add to Cart" button
        javafx.scene.layout.HBox actionBox = null;
        Button addBtn = null;

        // Search for existing
        for (javafx.scene.Node node : productCard.getChildren()) {
            if (node instanceof javafx.scene.layout.HBox hbox) {
                for (javafx.scene.Node child : hbox.getChildren()) {
                    if (child instanceof Button btn && (btn.getStyleClass().contains("add-cart-btn") || btn.getText().contains("Cart"))) {
                        actionBox = hbox;
                        addBtn = btn;
                        break;
                    }
                }
            }
            if (actionBox != null) break;
        }

        if (actionBox == null) return;

        // 2. Add Quantity Controls if missing
        boolean hasQty = false;
        for (javafx.scene.Node child : actionBox.getChildren()) {
            if (child.getStyleClass().contains("qty-box")) {
                hasQty = true;
                break;
            }
        }
        
        if (!hasQty) {
             Button btnDown = new Button("▼");
             btnDown.getStyleClass().add("arrow-btn");
             Label lblQty = new Label("1");
             lblQty.getStyleClass().add("qty-count");
             Button btnUp = new Button("▲");
             btnUp.getStyleClass().add("arrow-btn");
             
             javafx.scene.layout.HBox qtyBox = new javafx.scene.layout.HBox(4, btnDown, lblQty, btnUp);
             qtyBox.setAlignment(javafx.geometry.Pos.CENTER);
             qtyBox.getStyleClass().add("qty-box");
             
             actionBox.getChildren().add(qtyBox);
             
             // Add handlers
             btnDown.setOnAction(e -> {
                 int q = Integer.parseInt(lblQty.getText());
                 if (q > 1) lblQty.setText(String.valueOf(q - 1));
             });
             
             btnUp.setOnAction(e -> {
                 int q = Integer.parseInt(lblQty.getText());
                 if (q < 10) lblQty.setText(String.valueOf(q + 1));
             });
        }
    }

    /**
     * Build productId → stock-label / add-button / cart-status-label maps.
     * Creates stock labels dynamically for FXML cards that don't have them.
     * Always syncs from StockManager so stock is correct after page navigation.
     */
    private void buildNetworkMaps() {
        for (VBox productCard : allProductCards) {
            String productName = getProductNameFromCard(productCard);
            String productId   = StockManager.findProductIdByName(productName);
            if (productId == null) continue;

            cardProductIds.put(productCard, productId);

            // ── Stock label: find existing or create new one ──
            Label stockLabel = getStockLabelFromCard(productCard);
            if (stockLabel == null) {
                // FXML cards don't have stock labels — add one dynamically
                stockLabel = new Label("📦 Stock: 25");
                stockLabel.getStyleClass().add("stock-label");
                stockLabel.setAlignment(javafx.geometry.Pos.CENTER);
                stockLabel.setMaxWidth(Double.MAX_VALUE);
                // Insert before the last child (the cart-section HBox)
                int pos = Math.max(0, productCard.getChildren().size() - 1);
                productCard.getChildren().add(pos, stockLabel);
            }
            netStockLabels.put(productId, stockLabel);

            Button addBtn = getAddBtnFromCard(productCard);
            if (addBtn != null) netAddBtns.put(productId, addBtn);

            // ── Cart status label ──
            Label cartStatus = new Label("");
            cartStatus.setStyle("-fx-text-fill:#27ae60; -fx-font-size:12px; -fx-font-weight:bold;");
            cartStatus.setAlignment(javafx.geometry.Pos.CENTER);
            cartStatus.setMaxWidth(Double.MAX_VALUE);
            cartStatus.setVisible(false);
            cartStatus.setManaged(false);
            // Insert before the last child (the cart-section HBox)
            int insertIdx = Math.max(0, productCard.getChildren().size() - 1);
            productCard.getChildren().add(insertIdx, cartStatus);
            cartStatusLabels.put(productId, cartStatus);

            // Restore "In Cart" badge if user already added this product
            if (Cart.containsItem(productId)) {
                int qty = Cart.getItem(productId).getQuantity();
                cartStatus.setText("✅ In Cart: " + qty + " pcs");
                cartStatus.setVisible(true);
                cartStatus.setManaged(true);
            }

            // Always read stock from StockManager
            int stock = StockManager.getStock(productId);
            applyStockToCard(productId, stock);
        }
    }



    private void refreshAllStockFromManager() {
        for (String pid : netStockLabels.keySet()) {
            int currentStock = StockManager.getStock(pid);
            applyStockToCard(pid, currentStock);
        }
    }

    private void handleNetworkStockUpdate(String productId, int newQuantity) {
        javafx.application.Platform.runLater(() -> {
            applyStockToCard(productId, newQuantity);
        });
    }

    /**
     * Updates the UI for a single product (label text, color, button disable).
     */
    private void applyStockToCard(String productId, int quantity) {
        // 1. Update Label
        Label lbl = netStockLabels.get(productId);
        if (lbl != null) {
            if (quantity <= 0) {
                lbl.setText("❌ Out of Stock");
                lbl.getStyleClass().removeAll("stock-label", "stock-label-low");
                if (!lbl.getStyleClass().contains("stock-label-out")) {
                    lbl.getStyleClass().add("stock-label-out");
                }
            } else if (quantity <= 5) {
                lbl.setText("🔥 Low Stock: " + quantity);
                lbl.getStyleClass().removeAll("stock-label", "stock-label-out");
                if (!lbl.getStyleClass().contains("stock-label-low")) {
                    lbl.getStyleClass().add("stock-label-low");
                }
            } else {
                lbl.setText("📦 Stock: " + quantity);
                lbl.getStyleClass().removeAll("stock-label-low", "stock-label-out");
                if (!lbl.getStyleClass().contains("stock-label")) {
                    lbl.getStyleClass().add("stock-label");
                }
            }
        }

        // 2. Disable/Enable "Add to Cart" button
        Button btn = netAddBtns.get(productId);
        if (btn != null) {
            btn.setDisable(quantity <= 0);
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

    private Button getAddBtnFromCard(VBox card) {
        for (javafx.scene.Node n : card.getChildren()) {
            if (n instanceof javafx.scene.layout.HBox hbox) {
                for (javafx.scene.Node h : hbox.getChildren()) {
                    if (h instanceof Button btn &&
                            (btn.getStyleClass().contains("add-cart-btn"))) { // || btn.getText().contains("Cart"))) {
                        return btn;
                    }
                }
            }
        }
        return null;
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
        VBox card = null;
        
        if (parent instanceof javafx.scene.layout.HBox) {
            card = (VBox) parent.getParent();
        } else if (parent instanceof VBox) {
            card = (VBox) parent;
        }
        
        if (card == null) return;

        // 1. Identify Product & Quantity
        String productName = getProductNameFromCard(card);
        String productId   = StockManager.findProductIdByName(productName);
        if (productId == null) {
             System.out.println("❌ Product ID not found for: " + productName);
             return;
        }

        // Get selected quantity from UI
        int selectedQty = 1;
        if (parent instanceof javafx.scene.layout.HBox hbox) {
             for (javafx.scene.Node node : hbox.getChildren()) {
                 if (node instanceof javafx.scene.layout.HBox qtyBox) {
                     for (javafx.scene.Node child : qtyBox.getChildren()) {
                         if (child instanceof Label qtyLbl && qtyLbl.getStyleClass().contains("qty-count")) {
                             try {
                                 selectedQty = Integer.parseInt(qtyLbl.getText());
                             } catch (NumberFormatException ignored) {}
                         }
                     }
                 }
             }
        }
        
        // 2. Check Stock
        int currentStock = StockManager.getStock(productId);
        if (currentStock < selectedQty) {
            statusLabel.setText("❌ Not enough stock! Available: " + currentStock);
            return;
        }

        // 3. Get Price
        double price = 0;
        for (javafx.scene.Node node : card.getChildren()) {
            if (node instanceof Label label && label.getStyleClass().contains("product-price-discount")) {
                String priceText = label.getText();
                // Extract price logic
                String priceStr = "0";
                if (priceText.contains("(was")) {
                    priceStr = priceText.split("\\(was")[0];
                } else {
                    priceStr = priceText;
                }
                priceStr = priceStr.replaceAll("[^0-9.]", "");
                try { price = Double.parseDouble(priceStr); } catch (Exception e) {}
                break;
            }
        }

        // 4. Update Stock (Local + Network)
        boolean success = StockManager.reduceStock(productId, selectedQty);
        if (success) {
            // Send update to server
             NetworkManager.getInstance().broadcastStockUpdate(productId, StockManager.getStock(productId));

             // Add to cart
             Cart.addItem(productId, productName, "Fashion", price, selectedQty, "", 0);

             // Update UI
             applyStockToCard(productId, StockManager.getStock(productId));
             
             // Update cart status badge
             Label cartStatus = cartStatusLabels.get(productId);
             if (cartStatus != null) {
                 int cartQty = Cart.getItem(productId).getQuantity();
                 cartStatus.setText("✅ In Cart: " + cartQty + " pcs");
                 cartStatus.setVisible(true);
                 cartStatus.setManaged(true);
             }

             btn.setText("✓ Added!");
             statusLabel.setText("✓ Added to cart: " + productName + " (" + selectedQty + "x)");

             // Reset button text
             new Thread(() -> {
                 try { Thread.sleep(1500); } catch (Exception e) {}
                 javafx.application.Platform.runLater(() -> btn.setText(originalText));
             }).start();
        } else {
             statusLabel.setText("❌ Failed to update stock. Try again.");
        }
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

    private String getDefaultImagePathForProduct(String productName) {
        // Use StockManager's method which has comprehensive product mappings
        return StockManager.getDefaultImagePathForProduct(productName, "Fashion");
    }
}
