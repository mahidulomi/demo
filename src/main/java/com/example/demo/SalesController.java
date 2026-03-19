package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.util.*;

/**
 * Sales/POS Interface Controller
 */
public class SalesController {

    @FXML
    private TextField searchField;

    @FXML
    private TilePane productsTilePane;

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label totalStockLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label totalLabel;

    private String currentCategory = "All";
    private Map<String, CartItem> cartMap = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        try {
            System.out.println("🔄 SalesController initializing...");
            StockManager.initializeStock(); // Ensure global stock is ready
            
            // Listen for stock updates from other windows/instances
            StockManager.addExternalChangeListener(() -> {
                javafx.application.Platform.runLater(() -> {
                    System.out.println("🔄 Stock update received! Refreshing Sales UI...");
                    loadProducts(currentCategory);
                });
            });
            
            if (productsTilePane == null) {
                System.err.println("⚠️ ERROR: productsTilePane not injected!");
            }
             if (cartItemsContainer == null) {
                System.err.println("⚠️ ERROR: cartItemsContainer not injected!");
            }

            // Safe check for nulls
            if (productsTilePane != null) {
                loadProducts("All");
            }
            
            if (cartItemsContainer != null) {
               updateCartDisplay();
            }
            
            if (searchField != null) {
                searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal == null || newVal.trim().isEmpty()) {
                        loadProducts(currentCategory);
                    } else {
                        searchProducts(newVal.trim());
                    }
                });
            }
            
            System.out.println("✅ SalesController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error in initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle category click
     */
    @FXML
    private void onCategoryClick(javafx.event.ActionEvent event) {
        Button btn = (Button) event.getSource();
        String category = btn.getText();
        
        // Extract category name (remove emoji)
        if (category.contains("All")) {
            currentCategory = "All";
        } else if (category.contains("Beauty")) {
            currentCategory = "Beauty";
        } else if (category.contains("Electronics")) {
            currentCategory = "Electronics";
        } else if (category.contains("Home")) {
            currentCategory = "Home and Living";
        } else if (category.contains("Fashion")) {
            currentCategory = "Fashion";
        }
        
        loadProducts(currentCategory);
    }

    /**
     * Load products for category
     */
    private void loadProducts(String category) {
        if (productsTilePane == null) {
            return;
        }
        
        try {
            productsTilePane.getChildren().clear();
            List<Product> products = getProductsByCategory(category);

            for (Product product : products) {
                VBox productCard = createProductCard(product);
                productsTilePane.getChildren().add(productCard);
            }

            // Update Total Stock Label whenever products are reloaded
            updateTotalStockCount();
            
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
    }

    private void updateTotalStockCount() {
        if (totalStockLabel == null) return;
        
        // Calculate total number of UNIQUE items in stock
        int totalProducts = StockManager.getAllStockItems().size();
                
        javafx.application.Platform.runLater(() -> 
            totalStockLabel.setText("Total Products: " + totalProducts)
        );
    }

    /**
     * Create product card
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        // aligned with CSS: spacing and padding are now handled by .product-card style
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(240); // Adjusted for 3 columns per row
        card.setPrefHeight(340); // Maintain proportion

        // Product Image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);
        
        try {
            String imagePath = product.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                imageView.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Could not load image: " + product.getImagePath());
        }

        // Product Name
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Price
        Label priceLabel = new Label("Tk." + String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        // Stock
        Label stockLabel = new Label("Stock: " + product.getStock());
        if (product.getStock() < 5) {
            stockLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            stockLabel.getStyleClass().add("product-stock");
        }

        // Add Button + Qty Controls
        HBox controlBox = new HBox();
        controlBox.setSpacing(10);
        controlBox.setAlignment(Pos.CENTER);

        if (product.getStock() > 0) {
            Spinner<Integer> qtySpinner = new Spinner<>(1, product.getStock(), 1);
            qtySpinner.setPrefWidth(70);
            qtySpinner.setStyle("-fx-font-size: 14px;");

            Button addBtn = new Button("Add");
            addBtn.getStyleClass().add("add-button");
            addBtn.setOnAction(e -> {
                int qty = qtySpinner.getValue();
                if (qty > 0) {
                    addToCart(product, qty);
                    qtySpinner.getValueFactory().setValue(1);
                }
            });
            controlBox.getChildren().addAll(qtySpinner, addBtn);
        } else {
            Label outOfStockLabel = new Label("Running Out"); // Or "Out of Stock"
            outOfStockLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
            controlBox.getChildren().add(outOfStockLabel);
        }

        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, controlBox);
        return card;
    }

    /**
     * Add product to cart
     */
    private void addToCart(Product product, int quantity) {
        String key = product.getName();
        
        if (cartMap.containsKey(key)) {
            CartItem item = cartMap.get(key);
            item.quantity += quantity;
        } else {
            CartItem item = new CartItem(product.getName(), product.getPrice(), quantity, product.getCategory());
            cartMap.put(key, item);
        }
        
        updateCartDisplay();
    }

    /**
     * Update cart display
     */
    private void updateCartDisplay() {
        if (cartItemsContainer == null || subtotalLabel == null || totalLabel == null) {
            return;
        }
        
        try {
            cartItemsContainer.getChildren().clear();

            double subtotal = 0;
            
            for (CartItem item : cartMap.values()) {
                HBox itemRow = createCartItemRow(item);
                cartItemsContainer.getChildren().add(itemRow);
                subtotal += item.getTotalPrice();
            }

            // Discount removed as per request
            double total = subtotal;

            subtotalLabel.setText(String.format("Tk.%.2f", subtotal));
            totalLabel.setText(String.format("Tk.%.2f", total));
        } catch (Exception e) {
            System.err.println("Error updating cart: " + e.getMessage());
        }
    }

    /**
     * Create cart item row
     */
    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox();
        row.getStyleClass().add("cart-item-row");
        // spacing handled by CSS .cart-item-row
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.productName);
        nameLabel.setPrefWidth(130);
        nameLabel.setStyle("-fx-font-weight: bold;");

        // Unit Price removed to save space and avoid duplicate "Tk." display
        // User requested showing Tk only once (for total)

        // Quantity Label (Replacement for Spinner)
        Label qtyLabel = new Label("*" + item.quantity);
        qtyLabel.setPrefWidth(40);
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-text-fill: #7f8c8d;");

        Label totalAmountLabel = new Label("Tk." + String.format("%.2f", item.getTotalPrice()));
        totalAmountLabel.setPrefWidth(90);
        totalAmountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-min-width: 25px;");
        removeBtn.setOnAction(e -> {
            cartMap.remove(item.productName);
            updateCartDisplay();
        });

        row.getChildren().addAll(nameLabel, qtyLabel, totalAmountLabel, removeBtn);
        return row;
    }

    /**
     * Confirm sale
     */
    @FXML
    private void onConfirmSale() {
        if (cartMap.isEmpty()) {
            showAlert("Empty Cart", "Please add products before confirming sale!");
            return;
        }

        double subtotal = cartMap.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
        double total = subtotal; // No discount
        int totalQty = cartMap.values().stream().mapToInt(item -> item.quantity).sum();

        // Record sales with actual product categories
        StringBuilder itemsSummary = new StringBuilder();
        for (CartItem item : cartMap.values()) {
            SalesTracker.addSale(item.productName, item.category, item.price, item.quantity);

            // Build summary string for the bill
            if (itemsSummary.length() > 0) itemsSummary.append(" | ");
            itemsSummary.append(item.productName).append(" x").append(item.quantity)
                    .append(" @ ").append(String.format("%.2f", item.price));
            
            // Update Stock
            String stockId = StockManager.findProductIdByName(item.productName);
            if (stockId != null) {
                StockManager.reduceStock(stockId, item.quantity);
                NetworkManager.getInstance().broadcastStockUpdate(stockId, StockManager.getStock(stockId));
            }
        }

        // Create and save permanent Bill Record
        String user = Session.getCurrentUser();
        String soldBy = (user == null || user.isBlank()) ? "Guest" : user;

        // Build JSON for items
        StringBuilder jsonBuilder = new StringBuilder("[");
        boolean first = true;
        for (CartItem item : cartMap.values()) {
            if (!first) jsonBuilder.append(",");
            // Simple JSON construction
            jsonBuilder.append(String.format("{\"name\":\"%s\",\"price\":%.2f,\"quantity\":%d,\"category\":\"%s\"}",
                    item.productName.replace("\"", "\\\""),
                    item.price,
                    item.quantity,
                    item.category));
            first = false;
        }
        jsonBuilder.append("]");

        SaleRecord sale = new SaleRecord(
                SalesManager.getNextBillId(), // Sequential ID
                java.time.LocalDateTime.now().toString(),
                soldBy,
                "POS",
                totalQty,
                total,
                itemsSummary.toString(),
                jsonBuilder.toString()
        );
        SalesManager.recordSale(sale);
        NetworkManager.getInstance().broadcastSaleRecord(sale);

        showAlert("Sale Confirmed", String.format(
            "Sale Confirmed!\n\nSubtotal: Tk.%.2f\nTotal: Tk.%.2f\n\nItems: %d\n\nBill ID: %s",
            subtotal, total, totalQty, sale.getSaleId()
        ));

        cartMap.clear();
        updateCartDisplay();
        loadProducts(currentCategory); // Refresh UI to show updated stock
        HomeController.refreshDashboard();
    }

    /**
     * Clear cart
     */
    @FXML
    private void onClearCart() {
        cartMap.clear();
        updateCartDisplay();
    }

    /**
     * Back to home
     */
    @FXML
    private void onBackClick() {
        if (searchField != null && searchField.getScene() != null) {
            Session.goToHome(searchField);
        } else if (productsTilePane != null && productsTilePane.getScene() != null) {
            Session.goToHome(productsTilePane);
        } else {
            System.err.println("❌ ERROR: Cannot go back, scene is missing!");
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Get products by category
     */
    private List<Product> getProductsByCategory(String category) {
        List<Product> allProducts = getAllProducts();
        List<Product> filtered = new ArrayList<>();

        for (Product p : allProducts) {
            if (category.equals("All") || p.getCategory().equalsIgnoreCase(category)) {
                filtered.add(p);
            }
        }

        return filtered;
    }

    /**
     * Search for products by name
     */
    private void searchProducts(String query) {
        if (productsTilePane == null) return;
        
        productsTilePane.getChildren().clear();
        List<Product> allProducts = getAllProducts();
        List<Product> searchResults = new ArrayList<>();

        String queryLower = query.toLowerCase();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(queryLower)) {
                searchResults.add(p);
            }
        }

        for (Product product : searchResults) {
            VBox productCard = createProductCard(product);
            productsTilePane.getChildren().add(productCard);
        }
    }

    /**
     * Get all products from StockManager
     */
    private List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        
        // Fetch real-time data from StockManager
        List<StockItem> stockItems = StockManager.getAllStockItems();
        
        for (StockItem item : stockItems) {
            products.add(new Product(
                item.getProductName(),
                item.getCategory(), 
                item.getPrice(),
                item.getQuantity(),
                item.getImagePath()
            ));
        }

        return products;
    }

    /**
     * Product class
     */
    public static class Product {
        private String name;
        private String category;
        private double price;
        private int stock;
        private String imagePath;

        public Product(String name, String category, double price, int stock) {
            this(name, category, price, stock, null);
        }

        public Product(String name, String category, double price, int stock, String imagePath) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.imagePath = imagePath;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public String getImagePath() { return imagePath; }
    }

    /**
     * Cart item class
     */
    public static class CartItem {
        public String productName;
        public double price;
        public int quantity;
        public String category;

        public CartItem(String productName, double price, int quantity, String category) {
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.category = category;
        }

        public double getTotalPrice() {
            return price * quantity;
        }
    }
}
