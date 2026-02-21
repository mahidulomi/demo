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
        statusLabel.setText("⚡ 13 Latest Electronics - 5 with Amazing Discounts!");

        // Initialize category combo box
        if (categoryComboBox != null) {
            categoryComboBox.getItems().addAll("Mobile", "Laptop", "Accessories");
            categoryComboBox.setValue("Mobile");
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
                                    e.consume(); // Prevent card click
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

        // Create new product card with stock
        VBox newProductCard = createProductCard(productName, price, category, selectedImageFile, stockQuantity);

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
                imagePane.getChildren().add(imageView);
            } catch (Exception e) {
                Label emojiLabel = new Label(getCategoryEmoji(category));
                emojiLabel.setStyle("-fx-font-size: 64px;");
                imagePane.getChildren().add(emojiLabel);
            }
        } else {
            Label emojiLabel = new Label(getCategoryEmoji(category));
            emojiLabel.setStyle("-fx-font-size: 64px;");
            imagePane.getChildren().add(emojiLabel);
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
        addBtn.getStyleClass().add("add-cart-btn-electronics");
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
            case "Mobile" -> "📱";
            case "Laptop" -> "💻";
            case "Accessories" -> "🎧";
            default -> "⚡";
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
        if (categoryComboBox != null) categoryComboBox.setValue("Mobile");
        if (previewImageView != null) previewImageView.setImage(null);
        if (noImageLabel != null) noImageLabel.setVisible(true);
        if (addProductStatus != null) addProductStatus.setText("");
        selectedImageFile = null;
    }

    // ==================== FILTER METHODS ====================

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
    private void onOpenCart() {
        Session.goToCartFrom(statusLabel, "electronics-view.fxml");
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
                        productId = "E_" + productName.hashCode(); // Generate unique ID
                    } else if (label.getStyleClass().contains("product-price-discount")) {
                        String priceText = label.getText();
                        // Extract price: "BDT 85000  (was BDT 100000)" or "BDT 80000"
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
                                if (url != null && url.contains("images")) {
                                    imagePath = "/images/" + url.substring(url.lastIndexOf("/") + 1);
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
            Cart.addItem(productId, productName, "Electronics", price, quantity, imagePath, discountPercent);
        } catch (NumberFormatException e) {
            Cart.addItem(productId, productName, "Electronics", 0, quantity, imagePath, 0);
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

