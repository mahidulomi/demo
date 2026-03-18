package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
    private GridPane productsGrid;

    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label discountLabel;

    @FXML
    private Label totalLabel;

    private String currentCategory = "All";
    private Map<String, CartItem> cartMap = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        try {
            System.out.println("🔄 SalesController initializing...");
            
            if (productsGrid == null) {
                System.err.println("⚠️ ERROR: productsGrid not injected!");
            }
             if (cartItemsContainer == null) {
                System.err.println("⚠️ ERROR: cartItemsContainer not injected!");
            }

            // Safe check for nulls
            if (productsGrid != null) {
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
        if (productsGrid == null) {
            return;
        }
        
        try {
            productsGrid.getChildren().clear();
            List<Product> products = getProductsByCategory(category);

            int col = 0, row = 0;
            for (Product product : products) {
                VBox productCard = createProductCard(product);
                productsGrid.add(productCard, col, row);
                
                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
    }

    /**
     * Create product card
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-color: #fff;");
        card.setPrefWidth(250);

        // Product Name
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        nameLabel.setWrapText(true);

        // Price
        Label priceLabel = new Label("₹" + String.format("%.2f", product.getPrice()));
        priceLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Stock
        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #666;");

        // Add Button + Qty Controls
        HBox controlBox = new HBox();
        controlBox.setSpacing(5);
        controlBox.setAlignment(Pos.CENTER);

        Spinner<Integer> qtySpinner = new Spinner<>(0, product.getStock(), 1);
        qtySpinner.setPrefWidth(80);

        Button addBtn = new Button("Add");
        addBtn.setPrefWidth(80);
        addBtn.setStyle("-fx-padding: 8px; -fx-font-size: 12;");
        addBtn.setOnAction(e -> {
            int qty = qtySpinner.getValue();
            if (qty > 0) {
                addToCart(product, qty);
                qtySpinner.getValueFactory().setValue(1);
            }
        });

        controlBox.getChildren().addAll(qtySpinner, addBtn);

        card.getChildren().addAll(nameLabel, priceLabel, stockLabel, controlBox);
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
        if (cartItemsContainer == null || subtotalLabel == null || discountLabel == null || totalLabel == null) {
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

            double discount = subtotal * 0.05;
            double total = subtotal - discount;

            subtotalLabel.setText(String.format("₹%.2f", subtotal));
            discountLabel.setText(String.format("₹%.2f", discount));
            totalLabel.setText(String.format("₹%.2f", total));
        } catch (Exception e) {
            System.err.println("Error updating cart: " + e.getMessage());
        }
    }

    /**
     * Create cart item row
     */
    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox();
        row.setSpacing(10);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-border-color: #eee; -fx-background-color: #f9f9f9;");

        Label nameLabel = new Label(item.productName);
        nameLabel.setPrefWidth(150);
        nameLabel.setStyle("-fx-font-size: 12;");

        Label priceLabel = new Label("₹" + String.format("%.2f", item.price));
        priceLabel.setPrefWidth(80);

        // Quantity Spinner
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, item.quantity);
        qtySpinner.setPrefWidth(70);
        qtySpinner.setOnMouseClicked(e -> {
            item.quantity = qtySpinner.getValue();
            updateCartDisplay();
        });

        Label totalAmountLabel = new Label("₹" + String.format("%.2f", item.getTotalPrice()));
        totalAmountLabel.setPrefWidth(80);
        totalAmountLabel.setStyle("-fx-font-weight: bold;");

        Button removeBtn = new Button("Remove");
        removeBtn.setPrefWidth(80);
        removeBtn.setStyle("-fx-padding: 5px;");
        removeBtn.setOnAction(e -> {
            cartMap.remove(item.productName);
            updateCartDisplay();
        });

        row.getChildren().addAll(nameLabel, priceLabel, qtySpinner, totalAmountLabel, removeBtn);
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
        double discount = subtotal * 0.05;
        double total = subtotal - discount;

        // Record sales with actual product categories
        for (CartItem item : cartMap.values()) {
            SalesTracker.addSale(item.productName, item.category, item.price, item.quantity);
        }

        showAlert("Sale Confirmed", String.format(
            "Sale Confirmed!\n\nSubtotal: ₹%.2f\nDiscount: ₹%.2f\nTotal: ₹%.2f\n\nItems: %d",
            subtotal, discount, total, cartMap.size()
        ));

        cartMap.clear();
        updateCartDisplay();
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
        // Close the current sales window
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
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
        productsGrid.getChildren().clear();
        List<Product> allProducts = getAllProducts();
        List<Product> searchResults = new ArrayList<>();

        String queryLower = query.toLowerCase();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(queryLower)) {
                searchResults.add(p);
            }
        }

        int col = 0, row = 0;
        for (Product product : searchResults) {
            VBox productCard = createProductCard(product);
            productsGrid.add(productCard, col, row);
            
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Get all products
     */
    private List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        // Beauty Products
        products.add(new Product("Face Cream", "Beauty", 800, 10));
        products.add(new Product("Lipstick", "Beauty", 400, 3));
        products.add(new Product("Hair Oil", "Beauty", 600, 5));
        products.add(new Product("Face Mask", "Beauty", 350, 8));

        // Electronics
        products.add(new Product("iPhone 15", "Electronics", 80000, 2));
        products.add(new Product("Wireless Mouse", "Electronics", 2500, 15));
        products.add(new Product("USB-C Cable", "Electronics", 500, 20));
        products.add(new Product("Headphones", "Electronics", 5000, 4));

        // Home & Living
        products.add(new Product("Bed Sheet", "Home and Living", 1200, 25));
        products.add(new Product("Pillow", "Home and Living", 800, 30));
        products.add(new Product("Towel", "Home and Living", 400, 50));

        // Fashion
        products.add(new Product("T-Shirt", "Fashion", 600, 40));
        products.add(new Product("Jeans", "Fashion", 1500, 20));
        products.add(new Product("Shoes", "Fashion", 2500, 10));

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

        public Product(String name, String category, double price, int stock) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
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
