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

    // Store all products
    private java.util.List<VBox> allProductCards = null;

    // ── Networking: productId → UI node maps ─────────────────────────────────
    private final java.util.Map<String, Label>  netStockLabels  = new java.util.HashMap<>();
    private final java.util.Map<String, Button> netAddBtns      = new java.util.HashMap<>();
    private final java.util.Map<VBox, String>   cardProductIds  = new java.util.HashMap<>();
    private final java.util.Map<String, Label>  cartStatusLabels = new java.util.HashMap<>();

    @FXML
    private void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("🏡 12 Home & Living Products - All with Amazing Discounts!");
        }

        // Initialize category combo box
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll("Furniture", "Decor", "Kitchen");
            categoryComboBox.setValue("Furniture");
        }

        // Setup arrow button click handlers for quantity increase/decrease
        try {
            setupArrowButtons();
            System.out.println("HomeLivingController initialized.");
        } catch (Throwable e) {
            System.err.println("Failed to setup HomeLivingController arrow buttons: " + e.getMessage());
            e.printStackTrace();
        }
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
    private void onOpenCart() {
        Session.goToCartFrom(statusLabel, "homeliving-view.fxml");
    }

    @FXML
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }

    /**
     * Setup arrow button click handlers for quantity increase/decrease
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

            System.out.println("✓ HomeLiving: Stored " + allProductCards.size() + " products");

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
     */
    private void buildNetworkMaps() {
        for (VBox productCard : allProductCards) {
            String productName = getProductNameFromCard(productCard);
            String productId   = StockManager.findProductIdByName(productName);
            if (productId == null) continue;

            cardProductIds.put(productCard, productId);

            // Stock label
            Label stockLabel = getStockLabelFromCard(productCard);
            if (stockLabel == null) {
                stockLabel = new Label("📦 Stock: 25");
                stockLabel.getStyleClass().add("stock-label");
                stockLabel.setAlignment(javafx.geometry.Pos.CENTER);
                stockLabel.setMaxWidth(Double.MAX_VALUE);
                // Insert before the last child (Action Box)
                int pos = Math.max(0, productCard.getChildren().size() - 1);
                productCard.getChildren().add(pos, stockLabel);
            }
            netStockLabels.put(productId, stockLabel);

            Button addBtn = getAddBtnFromCard(productCard);
            if (addBtn != null) netAddBtns.put(productId, addBtn);

            // Cart status label
            Label cartStatus = new Label("");
            cartStatus.setStyle("-fx-text-fill:#27ae60; -fx-font-size:12px; -fx-font-weight:bold;");
            cartStatus.setAlignment(javafx.geometry.Pos.CENTER);
            cartStatus.setMaxWidth(Double.MAX_VALUE);
            cartStatus.setVisible(false);
            cartStatus.setManaged(false);
            int insertIdx = Math.max(0, productCard.getChildren().size() - 1);
            productCard.getChildren().add(insertIdx, cartStatus);
            cartStatusLabels.put(productId, cartStatus);

            if (Cart.containsItem(productId)) {
                int qty = Cart.getItem(productId).getQuantity();
                cartStatus.setText("✅ In Cart: " + qty + " + pcs");
                cartStatus.setVisible(true);
                cartStatus.setManaged(true);
            }

            // Sync stock
            int stock = StockManager.getStock(productId);
            applyStockToCard(productId, stock);
        }
    }

    private void syncCustomProductsFromStock() {
        if (allProductCards == null) return;

        boolean changed = false;
        for (StockItem item : StockManager.getAllStockItems()) {
            if (!"Home & Living".equals(item.getCategory()) || !item.getProductId().startsWith("H_custom_")) {
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

    private void refreshAllStockFromManager() {
        for (String pid : netStockLabels.keySet()) {
            int currentStock = StockManager.getStock(pid);
            applyStockToCard(pid, currentStock);
        }
    }

    private void handleNetworkStockUpdate(String productId, int newQuantity) {
        javafx.application.Platform.runLater(() -> applyStockToCard(productId, newQuantity));
    }

    private void applyStockToCard(String productId, int quantity) {
        // Label
        Label lbl = netStockLabels.get(productId);
        if (lbl != null) {
            if (quantity <= 0) {
                lbl.setText("❌ Out of Stock");
                lbl.getStyleClass().removeAll("stock-label", "stock-label-low");
                if (!lbl.getStyleClass().contains("stock-label-out")) lbl.getStyleClass().add("stock-label-out");
            } else if (quantity <= 5) {
                lbl.setText("🔥 Low Stock: " + quantity);
                lbl.getStyleClass().removeAll("stock-label", "stock-label-out");
                if (!lbl.getStyleClass().contains("stock-label-low")) lbl.getStyleClass().add("stock-label-low");
            } else {
                lbl.setText("📦 Stock: " + quantity);
                lbl.getStyleClass().removeAll("stock-label-low", "stock-label-out");
                if (!lbl.getStyleClass().contains("stock-label")) lbl.getStyleClass().add("stock-label");
            }
        }
        // Button
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
            if (n instanceof Label lbl && (lbl.getStyleClass().contains("stock-label") || lbl.getStyleClass().contains("stock-label-low") || lbl.getStyleClass().contains("stock-label-out"))) {
                return lbl;
            }
        }
        return null;
    }

    private Button getAddBtnFromCard(VBox card) {
        for (javafx.scene.Node n : card.getChildren()) {
            if (n instanceof javafx.scene.layout.HBox hbox) {
                for (javafx.scene.Node h : hbox.getChildren()) {
                    if (h instanceof Button btn && (btn.getStyleClass().contains("add-cart-btn") || btn.getText().contains("Cart"))) {
                        return btn;
                    }
                }
            }
        }
        return null;
    }

    @FXML
    private void onAddToCart(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String originalText = btn.getText();

        // 1. Identify Product
        javafx.scene.Node parent = btn.getParent();
        VBox card = null;
        if (parent instanceof javafx.scene.layout.HBox) {
            card = (VBox) parent.getParent();
        } else if (parent instanceof VBox) {
            card = (VBox) parent;
        }

        if (card == null) return;

        String productName = getProductNameFromCard(card);
        String productId   = StockManager.findProductIdByName(productName);
        if (productId == null) {
             System.out.println("❌ Product ID not found for: " + productName);
             return;
        }

        // 2. Quantity
        int selectedQty = 1;
        if (parent instanceof javafx.scene.layout.HBox hbox) {
             for (javafx.scene.Node node : hbox.getChildren()) {
                 if (node instanceof javafx.scene.layout.HBox qtyBox) {
                     for (javafx.scene.Node child : qtyBox.getChildren()) {
                         if (child instanceof Label qtyLbl && qtyLbl.getStyleClass().contains("qty-count")) {
                             try { selectedQty = Integer.parseInt(qtyLbl.getText()); } catch (Exception e) {}
                         }
                     }
                 }
             }
        }

        // 3. Check Stock
        if (StockManager.getStock(productId) < selectedQty) {
            statusLabel.setText("❌ Not enough stock!");
            return;
        }

        // 4. Price
        double price = 0;
        int discountPercent = 0;
        for (javafx.scene.Node node : card.getChildren()) {
            if (node instanceof Label label && label.getStyleClass().contains("product-price-discount")) {
                String priceText = label.getText(); 
                if (priceText.contains("(was")) {
                    // Extract discount
                    try {
                        String[] parts = priceText.split("\\(was");
                        double p = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                        double old = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                        discountPercent = (int) Math.round((1 - p/old) * 100);
                        price = old; // Cart expects original price often, or discounted? 
                        // Actually in FashionController we used original price and discount percent.
                        // Let's stick to consistent logic.
                    } catch (Exception e) {}
                }
                if (price == 0) {
                     try { price = Double.parseDouble(priceText.replaceAll("[^0-9.]", "")); } catch (Exception e) {}
                }
                break;
            }
        }

        // 5. Update Stock
        if (StockManager.reduceStock(productId, selectedQty)) {
             NetworkManager.getInstance().broadcastStockUpdate(productId, StockManager.getStock(productId));
             Cart.addItem(productId, productName, "Home & Living", price, selectedQty, "", discountPercent);
             
             applyStockToCard(productId, StockManager.getStock(productId));
             
             // Badge
             Label cartStatus = cartStatusLabels.get(productId);
             if (cartStatus != null) {
                 cartStatus.setText("✅ In Cart: " + Cart.getItem(productId).getQuantity() + " pcs");
                 cartStatus.setVisible(true);
                 cartStatus.setManaged(true);
             }
             
             btn.setText("✓ Added!");
             statusLabel.setText("✓ Added to cart: " + productName);
             
             new Thread(() -> {
                 try { Thread.sleep(1500); } catch (Exception e) {}
                 javafx.application.Platform.runLater(() -> btn.setText(originalText));
             }).start();
        } else {
             statusLabel.setText("❌ Failed to update stock.");
        }
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
        String newProductId = "H_custom_" + System.currentTimeMillis();

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
         StockManager.addStock(newProductId, productName, "Home & Living", category, stockQuantity, price, imagePath);

        // Add to network maps so real-time updates work for this card too
        Label  newStockLabel = getStockLabelFromCard(newProductCard);
        Button newAddBtn     = getAddBtnFromCard(newProductCard);
        if (newStockLabel != null) netStockLabels.put(newProductId, newStockLabel);
        if (newAddBtn     != null) netAddBtns.put(newProductId, newAddBtn);
        cardProductIds.put(newProductCard, newProductId);

        StockItem createdItem = new StockItem(newProductId, productName, "Home & Living", category, stockQuantity, price, imagePath);
        applyStockToCard(newProductId, stockQuantity);

        // Broadcast new product to other machines
        NetworkManager.getInstance().broadcastNewProduct(createdItem);

        // Add to allProductCards list
        allProductCards.add(newProductCard);

        // Sort all product cards by name alphabetically
        allProductCards.sort((card1, card2) -> {
            String name1 = getProductNameFromCard(card1);
            String name2 = getProductNameFromCard(card2);
            return name1.compareToIgnoreCase(name2);
        });

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
        if (categoryComboBox != null) categoryComboBox.setValue("Furniture");
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

    private void updateFilterButtons(String activeFilter) {
        // Remove active class from all buttons
        if (btnAll == null) return;
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
        java.util.List<VBox> filteredProducts = new java.util.ArrayList<>();
        
        for (VBox productCard : allProductCards) {
             String productCategory = (String) productCard.getUserData();
            
            if ("All".equals(category)) {
                filteredProducts.add(productCard);
            } else if (productCategory != null && productCategory.equals(category)) {
                filteredProducts.add(productCard);
            }
        }
        
        // Re-add filtered products to grid
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

    private int countVisibleProducts() {
         if (allProductCards != null && !allProductCards.isEmpty()) {
             // If we are using allProductCards, the grid is already filtered to only show visible items
             return productGrid.getChildren().size();
         }
         
        if (productGrid == null) return 0;
        return (int) productGrid.getChildren().stream()
                .filter(node -> node instanceof VBox && node.isVisible())
                .count();
     }

      private String getDefaultImagePathForProduct(String productName) {
          // Use StockManager's method which has comprehensive product mappings
          return StockManager.getDefaultImagePathForProduct(productName, "Home and Living");
      }
}
