package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.io.File;
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
    
    // Customer Fields
    @FXML private Label selectedCustomerLabel;
    @FXML private Button addCustomerButton;
    
    // Modal Elements
    @FXML private javafx.scene.layout.StackPane rootStackPane;
    @FXML private javafx.scene.layout.AnchorPane modalOverlay;
    @FXML private Label modalTitle;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private javafx.scene.control.TextArea addressArea;
    @FXML private javafx.scene.control.ComboBox<String> typeCombo;
    @FXML private ToggleButton dueBalanceToggle;
    @FXML private TextField dueBalanceAmountField;

    private String currentCategory = "All";
    private Map<String, CartItem> cartMap = new LinkedHashMap<>();
    private Customer currentSelectedCustomer = null;

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
            
            // Initialize Type Combo
            if (typeCombo != null) {
                typeCombo.setItems(javafx.collections.FXCollections.observableArrayList("Retail", "Wholesale"));
                typeCombo.getSelectionModel().selectFirst();
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

            updateAddCustomerButtonState();
            
            System.out.println("✅ SalesController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error in initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate back to Home Dashboard
     */
    @FXML
    private void onBackClick() {
        if (rootStackPane != null) {
            Session.goToHome(rootStackPane);
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
        
        boolean imageLoaded = false;
        
        // 1. Try standard path
        try {
            String imagePath = product.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                String resourcePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
                java.net.URL imgUrl = getClass().getResource(resourcePath);
                if (imgUrl != null) {
                    imageView.setImage(new Image(imgUrl.toExternalForm()));
                    imageLoaded = true;
                } else {
                    // Try loading user-imported image path/URI
                    try {
                        if (imagePath.startsWith("file:")) {
                            imageView.setImage(new Image(imagePath));
                            imageLoaded = true;
                        } else {
                            File file = new File(imagePath);
                            if (file.exists()) {
                                imageView.setImage(new Image(file.toURI().toString()));
                                imageLoaded = true;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            // System.err.println("Could not load image: " + product.getImagePath());
        }

        // 2. If not loaded, try loading from /extra/ folder using Product Name
        if (!imageLoaded) {
            String[] extensions = {".jpg", ".png", ".jpeg", ".gif"};
            String safeName = product.getName();
            
            // Try exact name match
            for (String ext : extensions) {
                try {
                    String path = "/extra/" + safeName + ext;
                    java.net.URL imgUrl = getClass().getResource(path);
                    if (imgUrl != null) {
                        imageView.setImage(new Image(imgUrl.toExternalForm()));
                        imageLoaded = true;
                        // System.out.println("Loaded extra image: " + path);
                        break;
                    }
                } catch (Exception e) {}
            }
            
            // Try remove spaces
            if (!imageLoaded) {
                 String noSpaceName = safeName.replace(" ", "");
                 for (String ext : extensions) {
                    try {
                        String path = "/extra/" + noSpaceName + ext;
                        java.net.URL imgUrl = getClass().getResource(path);
                        if (imgUrl != null) {
                            imageView.setImage(new Image(imgUrl.toExternalForm()));
                            imageLoaded = true;
                            break;
                        }
                    } catch (Exception e) {}
                }
            }
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
        String key = product.getId();
        
        if (cartMap.containsKey(key)) {
            CartItem item = cartMap.get(key);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem item = new CartItem(
                product.getId(),
                product.getName(), 
                product.getCategory(), 
                product.getPrice(), 
                quantity, 
                product.getImagePath(), 
                0
            );
            cartMap.put(key, item);
        }
        
        updateCartDisplay();
    }

    /**
     * Update cart display
     */
    private void updateCartDisplay() {
        if (cartItemsContainer == null) return;
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
    }

    /**
     * Create cart item row
     */
    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox();
        row.getStyleClass().add("cart-item-row");
        // spacing handled by CSS .cart-item-row
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getProductName());
        nameLabel.setMinWidth(60);
        nameLabel.setMaxWidth(130);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        // Unit Price removed to save space and avoid duplicate "Tk." display
        // User requested showing Tk only once (for total)

        // Quantity Label (Replacement for Spinner)
        Label qtyLabel = new Label("x " + item.getQuantity());
        qtyLabel.setMinWidth(30);
        qtyLabel.setPrefWidth(30);
        qtyLabel.setAlignment(Pos.CENTER);
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-text-fill: #a0aec0;");

        Label totalAmountLabel = new Label("Tk." + String.format("%.2f", item.getTotalPrice()));
        totalAmountLabel.setMinWidth(75);
        totalAmountLabel.setPrefWidth(75);
        totalAmountLabel.setAlignment(Pos.CENTER_RIGHT);
        totalAmountLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");

        Button removeBtn = new Button("❌");
        removeBtn.getStyleClass().add("cart-item-remove-btn");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-padding: 0; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            cartMap.remove(item.getProductId());
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
            showAlert("Cart Empty", "Please add products to the cart first.");
            return;
        }

        if (currentSelectedCustomer == null) {
            showAlert("Missing Customer", "Please add or select a customer for this sale.");
            return;
        }

        // Calculate totals
        double totalAmount = cartMap.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
        int totalQty = cartMap.values().stream().mapToInt(CartItem::getQuantity).sum();
        List<CartItem> purchasedItems = new ArrayList<>(cartMap.values());

        // Update Stock
        for (CartItem item : purchasedItems) {
            String productId = item.getProductId();
            int currentStock = StockManager.getStock(productId);
            int newStock = Math.max(0, currentStock - item.getQuantity());
            StockManager.updateStock(productId, newStock);
            NetworkManager.getInstance().broadcastStockUpdate(productId, newStock);
            
            // Record individually for dashboard tracker
            SalesTracker.addSale(item.getProductName(), item.getCategory(), item.getUnitPrice(), item.getQuantity());
        }

        // Record Sale Summary with customer details
        String customerName = currentSelectedCustomer != null ? currentSelectedCustomer.getName() : null;
        String customerPhone = currentSelectedCustomer != null ? currentSelectedCustomer.getPhone() : null;
        String customerEmail = currentSelectedCustomer != null ? currentSelectedCustomer.getEmail() : null;
        String customerAddress = currentSelectedCustomer != null ? currentSelectedCustomer.getAddress() : null;
        
        SaleRecord sale = NetworkManager.getInstance().buildSaleRecord(purchasedItems, totalQty, totalAmount, 
                                                                       customerName, customerPhone, customerEmail, customerAddress);
        SalesManager.recordSale(sale);
        NetworkManager.getInstance().broadcastSaleRecord(sale);

        // Success Feedback
        String customerInfo = currentSelectedCustomer.getName() + " (" + currentSelectedCustomer.getPhone() + ")";
        showAlert("Success", "Sale Completed! Amount: Tk." + String.format("%.2f", totalAmount) + "\nCustomer: " + customerInfo);
        
        // Clear Cart & Selection
        cartMap.clear();
        updateCartDisplay();
        resetSelectedCustomer();
        
        // Refresh dashboard stats
        HomeController.refreshDashboard();
    }
    
    @FXML
    private void onClearCart() {
        cartMap.clear();
        updateCartDisplay();
        resetSelectedCustomer();
    }

    // Modal Actions
    @FXML
    private void onOpenCustomerModal() {
        clearModalForm();
        if(modalOverlay != null) modalOverlay.setVisible(true);
    }

    @FXML
    private void onCancelModal() {
        if(modalOverlay != null) modalOverlay.setVisible(false);
    }

    @FXML
    private void onSaveCustomer() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressArea.getText().trim();
        String type = typeCombo.getValue();

        if (name.isEmpty()) {
            showAlert("Error", "Please enter customer name");
            return;
        }

        if (phone.isEmpty()) {
            showAlert("Error", "Please enter phone number");
            return;
        }

        boolean hasDue = dueBalanceToggle.isSelected();
        double due = 0.0;
        
        if (hasDue) {
             try {
                 due = Double.parseDouble(dueBalanceAmountField.getText());
             } catch (NumberFormatException e) {
                 due = 0.0;
             }
        }
        
        // Check if customer already exists
        Customer existing = null;
        for(Customer c : CustomerManager.getAllCustomers()) {
            if(c.getPhone().equals(phone)) {
                existing = c; 
                break;
            }
        }
        
        Customer customer;
        if (existing != null) {
            customer = existing;
            customer.setName(name);
            customer.setEmail(email);
            customer.setAddress(address);
            customer.setType(type);
        } else {
            customer = new Customer(name, phone, email, address, type, due);
        }

        System.out.println("[SalesController] Saving customer: " + customer.getName() + " | Phone: " + customer.getPhone());
        CustomerManager.saveCustomer(customer);
        NetworkManager.getInstance().broadcastCustomer(customer);
        
        System.out.println("[SalesController] Customer saved successfully!");

        // Set as selected
        currentSelectedCustomer = customer;
        selectedCustomerLabel.setText("Selected: " + customer.getName());
        updateAddCustomerButtonState();
        
        showAlert("Success", "Customer saved: " + customer.getName());
        onCancelModal(); // Close modal
    }
    
    @FXML
    private void onDueToggle() {
        if(dueBalanceAmountField != null && dueBalanceToggle != null) {
            dueBalanceAmountField.setDisable(!dueBalanceToggle.isSelected());
            if (!dueBalanceToggle.isSelected()) {
                dueBalanceAmountField.setText("0.0");
            }
        }
    }
    
    private void clearModalForm() {
        if(nameField != null) nameField.clear();
        if(phoneField != null) phoneField.clear();
        if(emailField != null) emailField.clear();
        if(addressArea != null) addressArea.clear();
        if(typeCombo != null) typeCombo.getSelectionModel().selectFirst();
        if(dueBalanceToggle != null) dueBalanceToggle.setSelected(false);
        if(dueBalanceAmountField != null) {
            dueBalanceAmountField.setText("0.0");
            dueBalanceAmountField.setDisable(true);
        }
    }

    private void resetSelectedCustomer() {
        currentSelectedCustomer = null;
        if (selectedCustomerLabel != null) {
            selectedCustomerLabel.setText("No Customer Selected");
        }
        updateAddCustomerButtonState();
    }

    private void updateAddCustomerButtonState() {
        if (addCustomerButton == null) {
            return;
        }
        boolean showAddCustomer = currentSelectedCustomer == null;
        addCustomerButton.setVisible(showAddCustomer);
        addCustomerButton.setManaged(showAddCustomer);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
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

        // Sort by product name alphabetically
        filtered.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

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

        // Sort by product name alphabetically
        searchResults.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

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
                item.getProductId(),
                item.getProductName(),
                item.getCategory(), 
                item.getPrice(),
                item.getQuantity(),
                item.getImagePath()
            ));
        }

        // Sort by product name alphabetically
        products.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        return products;
    }

    /**
     * Product class
     */
    public static class Product {
        private String id;
        private String name;
        private String category;
        private double price;
        private int stock;
        private String imagePath;

        public Product(String id, String name, String category, double price, int stock) {
            this(id, name, category, price, stock, null);
        }

        public Product(String id, String name, String category, double price, int stock, String imagePath) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.imagePath = imagePath;
        }

        public String getId() { return id; }

        public String getName() { return name; }

        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public String getImagePath() { return imagePath; }
    }
}
