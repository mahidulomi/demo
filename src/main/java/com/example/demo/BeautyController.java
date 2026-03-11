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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    @FXML
    private void initialize() {
        statusLabel.setText("✨ 12 Premium Beauty Products - All with Amazing Discounts!");

        // Initialize category combo box
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll("Skincare", "Makeup", "Haircare");
            categoryComboBox.setValue("Skincare");
        }

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
        productGrid.getChildren().clear();
        
        int row = 0;
        int col = 0;
        for (VBox productCard : matchedProducts) {
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
        
        System.out.println("✓ Beauty Search: " + matchCount + " matches for \"" + query + "\"");
    }

    /**
     * Setup arrow button click handlers for quantity increase/decrease
     * Layout: [Add to Cart] [▼ 1 ▲]
     */
    private void setupArrowButtons(VBox productCard) {
        // Find stock label to get max quantity
        Label stockLabelRef = null;
        for (javafx.scene.Node cardChild : productCard.getChildren()) {
            if (cardChild instanceof Label) {
                Label label = (Label) cardChild;
                if (label.getStyleClass().contains("stock-label") ||
                    label.getStyleClass().contains("stock-label-low") ||
                    label.getStyleClass().contains("stock-label-out")) {
                    stockLabelRef = label;
                    break;
                }
            }
        }
        final Label finalStockLabel = stockLabelRef;

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
                                    int currentQty = Integer.parseInt(finalQtyLabel.getText());
                                    // Get current stock from stock label
                                    int maxStock = 999;
                                    if (finalStockLabel != null) {
                                        try {
                                            maxStock = Integer.parseInt(finalStockLabel.getText().replaceAll("[^0-9]", ""));
                                        } catch (NumberFormatException ex) {
                                            maxStock = 0;
                                        }
                                    }
                                    if (currentQty < maxStock) {
                                        finalQtyLabel.setText(String.valueOf(currentQty + 1));
                                    }
                                });
                            }

                            if (downBtn != null && qtyLabel != null) {
                                final Label finalQtyLabel = qtyLabel;
                                downBtn.setOnAction(e -> {
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

        // Create new product card with stock
        VBox newProductCard = createProductCard(productName, price, category, selectedImageFile, stockQuantity);

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

    private VBox createProductCard(String name, double price, String category, File imageFile, int stockQuantity) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(290);
        card.setPrefHeight(340);
        card.getStyleClass().add("product-card");
        card.setUserData(category);
        card.setPadding(new Insets(15));

        // Image StackPane
        StackPane imagePane = new StackPane();
        imagePane.setPrefSize(260, 180);
        imagePane.getStyleClass().add("product-image");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        if (imageFile != null) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
            } catch (Exception e) {
                // Use emoji as fallback
                Label emojiLabel = new Label(getCategoryEmoji(category));
                emojiLabel.setStyle("-fx-font-size: 64px;");
                imagePane.getChildren().add(emojiLabel);
            }
        } else {
            // Use emoji as fallback
            Label emojiLabel = new Label(getCategoryEmoji(category));
            emojiLabel.setStyle("-fx-font-size: 64px;");
            imagePane.getChildren().add(emojiLabel);
        }

        if (imageFile != null) {
            imagePane.getChildren().add(imageView);
        }

        // NEW badge
        Label newBadge = new Label("✨ NEW");
        newBadge.getStyleClass().add("product-new-badge");
        StackPane.setAlignment(newBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(newBadge, new Insets(10, 10, 0, 0));
        imagePane.getChildren().add(newBadge);

        // Product Name
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(260);
        nameLabel.setAlignment(Pos.CENTER);

        // Product Price
        Label priceLabel = new Label(String.format("৳%.0f", price));
        priceLabel.getStyleClass().add("product-price-discount");
        priceLabel.setWrapText(true);
        priceLabel.setMaxWidth(260);
        priceLabel.setAlignment(Pos.CENTER);

        // Stock Label
        Label stockLabel = new Label("📦 Stock: " + stockQuantity);
        stockLabel.getStyleClass().add(getStockStyleClass(stockQuantity));

        // Add to Cart section
        HBox cartSection = new HBox(8);
        cartSection.setAlignment(Pos.CENTER);

        Button addBtn = new Button("🛒 Add to Cart");
        addBtn.getStyleClass().add("add-cart-btn");
        if (stockQuantity <= 0) {
            addBtn.setText("❌ Out of Stock");
            addBtn.setDisable(true);
        }
        addBtn.setOnAction(this::onAddToCart);

        // Quantity box
        HBox qtyBox = new HBox(4);
        qtyBox.setAlignment(Pos.CENTER);
        qtyBox.getStyleClass().add("qty-box");

        Button downBtn = new Button("▼");
        downBtn.getStyleClass().add("arrow-btn");

        Label qtyLabel = new Label("1");
        qtyLabel.getStyleClass().add("qty-count");

        Button upBtn = new Button("▲");
        upBtn.getStyleClass().add("arrow-btn");

        // Set up qty button handlers with stock limit
        final int maxStock = stockQuantity;
        upBtn.setOnAction(e -> {
            int qty = Integer.parseInt(qtyLabel.getText());
            if (qty < maxStock) {
                qtyLabel.setText(String.valueOf(qty + 1));
            }
        });

        downBtn.setOnAction(e -> {
            int qty = Integer.parseInt(qtyLabel.getText());
            if (qty > 1) {
                qtyLabel.setText(String.valueOf(qty - 1));
            }
        });

        qtyBox.getChildren().addAll(downBtn, qtyLabel, upBtn);
        cartSection.getChildren().addAll(addBtn, qtyBox);

        card.getChildren().addAll(imagePane, nameLabel, priceLabel, stockLabel, cartSection);

        return card;
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

    private String getCategoryEmoji(String category) {
        return switch (category) {
            case "Skincare" -> "🧴";
            case "Makeup" -> "💄";
            case "Haircare" -> "💇";
            default -> "✨";
        };
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

    @FXML
    private void onAddToCart(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String originalText = btn.getText();

        // Get parent HBox, then VBox (product card)
        javafx.scene.Node parent = btn.getParent();
        javafx.scene.layout.VBox card = null;
        if (parent instanceof HBox) {
            card = (javafx.scene.layout.VBox) parent.getParent();
        }

        String productName = "Product";
        String productPrice = "0";
        int discountPercent = 0;
        int quantity = 1;
        String imagePath = "";
        String productId = "";
        Label stockLabelRef = null;
        Label qtyLabelRef = null;
        int currentStock = 0;

        if (card != null) {
            // Find product name, price, quantity, stock, and image
            for (javafx.scene.Node node : card.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getStyleClass().contains("product-name")) {
                        productName = label.getText();
                        productId = "B_" + productName.hashCode(); // Generate unique ID
                    } else if (label.getStyleClass().contains("product-price-discount")) {
                        String priceText = label.getText();
                        // Extract price: "৳1125  (was ৳1500)" or "৳800"
                        if (priceText.contains("(was")) {
                            // Has discount
                            String[] parts = priceText.split("\\(was");
                            productPrice = parts[0].replace("BDT", "").replace("৳", "").replace(",", "").trim();
                            String originalPrice = parts[1].replace("BDT", "").replace("৳", "").replace(")", "").replace(",", "").trim();
                            try {
                                double discounted = Double.parseDouble(productPrice);
                                double original = Double.parseDouble(originalPrice);
                                discountPercent = (int) Math.round((1 - discounted / original) * 100);
                                productPrice = originalPrice; // Store original price
                            } catch (NumberFormatException e) {
                                productPrice = "0";
                            }
                        } else {
                            productPrice = priceText.replace("BDT", "").replace("৳", "").replace(",", "").trim();
                        }
                    } else if (label.getStyleClass().contains("stock-label") ||
                               label.getStyleClass().contains("stock-label-low") ||
                               label.getStyleClass().contains("stock-label-out")) {
                        stockLabelRef = label;
                        // Extract stock number from "📦 Stock: 50"
                        String stockText = label.getText();
                        try {
                            currentStock = Integer.parseInt(stockText.replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException e) {
                            currentStock = 0;
                        }
                    }
                } else if (node instanceof javafx.scene.layout.StackPane) {
                    // Get image path from StackPane > ImageView
                    javafx.scene.layout.StackPane stackPane = (javafx.scene.layout.StackPane) node;
                    for (javafx.scene.Node stackChild : stackPane.getChildren()) {
                        if (stackChild instanceof javafx.scene.image.ImageView) {
                            javafx.scene.image.ImageView iv = (javafx.scene.image.ImageView) stackChild;
                            if (iv.getImage() != null) {
                                String url = iv.getImage().getUrl();
                                if (url != null && url.contains("beautyimages")) {
                                    imagePath = "/beautyimages/" + url.substring(url.lastIndexOf("/") + 1);
                                }
                            }
                        }
                    }
                } else if (node instanceof HBox) {
                    // Find quantity from qty-box
                    HBox hbox = (HBox) node;
                    for (javafx.scene.Node hboxChild : hbox.getChildren()) {
                        if (hboxChild instanceof HBox) {
                            HBox qtyBox = (HBox) hboxChild;
                            if (qtyBox.getStyleClass().contains("qty-box")) {
                                for (javafx.scene.Node qtyChild : qtyBox.getChildren()) {
                                    if (qtyChild instanceof Label) {
                                        Label qtyLabel = (Label) qtyChild;
                                        if (qtyLabel.getStyleClass().contains("qty-count")) {
                                            qtyLabelRef = qtyLabel;
                                            try {
                                                quantity = Integer.parseInt(qtyLabel.getText());
                                            } catch (NumberFormatException e) {
                                                quantity = 1;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check if enough stock available
        if (currentStock < quantity) {
            statusLabel.setText("❌ Not enough stock! Available: " + currentStock);
            return;
        }

        // Reduce stock
        int newStock = currentStock - quantity;
        if (stockLabelRef != null) {
            // Update stock label text
            if (newStock <= 0) {
                stockLabelRef.setText("❌ Out of Stock");
            } else {
                stockLabelRef.setText("📦 Stock: " + newStock);
            }

            // Update style class based on new stock level
            stockLabelRef.getStyleClass().removeAll("stock-label", "stock-label-low", "stock-label-out");
            stockLabelRef.getStyleClass().add(getStockStyleClass(newStock));

            // If stock is 0, disable Add to Cart button
            if (newStock <= 0) {
                btn.setText("❌ Out of Stock");
                btn.setDisable(true);
            }
        }

        // Reset quantity to 1 after adding
        if (qtyLabelRef != null) {
            qtyLabelRef.setText("1");
        }

        // Add to cart
        try {
            double price = Double.parseDouble(productPrice);
            Cart.addItem(productId, productName, "Beauty", price, quantity, imagePath, discountPercent);
        } catch (NumberFormatException e) {
            Cart.addItem(productId, productName, "Beauty", 0, quantity, imagePath, 0);
        }

        // Update button and status
        if (newStock > 0) {
            btn.setText("✓ Added!");
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

        statusLabel.setText("✓ Added to cart: " + productName + " (Qty: " + quantity + ") | Stock left: " + newStock + " | Cart Total: " + Cart.getTotalQuantity() + " items");
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
                .filter(node -> node instanceof VBox && node.isVisible())
                .count();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}

