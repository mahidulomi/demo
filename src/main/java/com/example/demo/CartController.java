package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Shopping Cart page.
 *
 * Stock flow:
 *   • Adding an item to cart does NOT reduce stock.
 *   • Removing/cancelling an item from cart does NOT change stock.
 *   • "Buy Now" (checkout) reduces stock for every purchased item, then clears the cart.
 */
public class CartController {
    private static final int DEFAULT_NEW_PRODUCT_STOCK = 25;

    @FXML private Label statusLabel;
    @FXML private VBox cartItemsContainer;
    @FXML private VBox billItemsContainer;
    @FXML private Label totalPriceLabel;

    // Customer Input Fields
    @FXML private javafx.scene.control.TextField customerNameField;
    @FXML private javafx.scene.control.TextField customerPhoneField;
    @FXML private javafx.scene.control.TextField customerEmailField;
    @FXML private javafx.scene.control.TextField customerAddressField;

    @FXML
    private void initialize() {
        // Auto-fill username
        String currentUser = Session.getCurrentUser();
        if (currentUser != null && !currentUser.isEmpty()) {
            customerNameField.setText(currentUser);
        } else {
            customerNameField.setText("Customer");
        }
        customerNameField.setEditable(false);
        
        refreshCart();
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    private void refreshCart() {
        List<CartItem> items = Cart.getAllItems();
        statusLabel.setText(items.isEmpty()
                ? "🛒 Your cart is empty. Start shopping!"
                : "🛒 " + items.size() + " item(s) in your cart");
        updateSummary();
    }

    private void updateSummary() {
        totalPriceLabel.setText(Cart.getFormattedTotal());

        if (billItemsContainer != null) {
            billItemsContainer.getChildren().clear();
            List<CartItem> items = Cart.getAllItems();
            if (items.isEmpty()) {
                Label empty = new Label("(No items)");
                empty.setStyle("-fx-font-size:11px; -fx-text-fill:#999; -fx-font-style:italic;");
                billItemsContainer.getChildren().add(empty);
            } else {
                for (CartItem item : items) {
                    billItemsContainer.getChildren().add(createBillItemRow(item));
                }
            }
        }

        // Populate cartItemsContainer
        if (cartItemsContainer != null) {
            cartItemsContainer.getChildren().clear();
            List<CartItem> items = Cart.getAllItems();
            if (items.isEmpty()) {
                Label empty = new Label("(No items added yet)");
                empty.setStyle("-fx-font-size:12px; -fx-text-fill:#999; -fx-font-style:italic;");
                cartItemsContainer.getChildren().add(empty);
            } else {
                for (CartItem item : items) {
                    cartItemsContainer.getChildren().add(createCartItemRow(item));
                }
            }
        }
    }

    // ── Cart Item Row (with image and quantity control) ──────────────────────

    private VBox createCartItemRow(CartItem item) {
        VBox itemBox = new VBox(10);
        itemBox.setStyle("-fx-border-color: #333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-padding: 15;");
        itemBox.setPrefWidth(Double.MAX_VALUE);

        // Header: Image + Info
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Product Image - Larger
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-color: #444; -fx-border-radius: 8; -fx-background-color: #0d0d0d;");

        String imagePath = item.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }
                InputStream is = getClass().getResourceAsStream(imagePath);
                if (is != null) {
                    imageView.setImage(new Image(is));
                } else {
                    String altPath = imagePath.substring(1);
                    is = getClass().getResourceAsStream(altPath);
                    if (is != null) {
                        imageView.setImage(new Image(is));
                    }
                }
            } catch (Exception e) {
                System.err.println("[DEBUG] Could not load image: " + e.getMessage());
            }
        }

        // Product Info - Better layout
        VBox infoBox = new VBox(6);

        Label nameLabel = new Label(item.getProductName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(250);

        Label categoryLabel = new Label("📦 " + item.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

        Label priceLabel = new Label("৳ " + String.format("%.2f", item.getUnitPrice()) + " per unit");
        priceLabel.setStyle("-fx-text-fill: #11998e; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label totalLabel = new Label("Total: ৳ " + String.format("%.2f", item.getTotalPrice()));
        totalLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 13px;");

        infoBox.getChildren().addAll(nameLabel, categoryLabel, priceLabel, totalLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        headerBox.getChildren().addAll(imageView, infoBox);

        // Quantity Control Row - Improved
        HBox qtyControlBox = new HBox(12);
        qtyControlBox.setAlignment(Pos.CENTER);
        qtyControlBox.setStyle("-fx-padding: 12; -fx-background-color: #0d0d0d; -fx-border-radius: 6; -fx-border-color: #333;");

        Label qtyTitleLabel = new Label("Quantity:");
        qtyTitleLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px; -fx-font-weight: bold;");

        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, item.getQuantity());
        qtySpinner.setPrefWidth(90);
        qtySpinner.setStyle("-fx-font-size: 13px; -fx-padding: 8;");

        Button decrementBtn = new Button("▼");
        decrementBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 14; -fx-font-size: 14px; -fx-background-radius: 5;");
        decrementBtn.setOnAction(e -> {
            int current = qtySpinner.getValue();
            if (current > 1) {
                qtySpinner.decrement();
            }
        });

        Button incrementBtn = new Button("▲");
        incrementBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 14; -fx-font-size: 14px; -fx-background-radius: 5;");
        incrementBtn.setOnAction(e -> {
            int current = qtySpinner.getValue();
            if (current < 100) {
                qtySpinner.increment();
            }
        });

        Button removeBtn = new Button("🗑️ Remove");
        removeBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15; -fx-font-size: 12px; -fx-background-radius: 5;");
        removeBtn.setOnAction(e -> removeItemFromCart(item));

        HBox.setHgrow(removeBtn, Priority.ALWAYS);

        qtyControlBox.getChildren().addAll(qtyTitleLabel, decrementBtn, qtySpinner, incrementBtn, removeBtn);

        // Update quantity and total when spinner changes
        qtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal > 0) {
                Cart.updateQuantity(item.getProductId(), newVal);
                totalLabel.setText("Total: ৳ " + String.format("%.2f", item.getTotalPrice()));
                updateSummary();
            }
        });

        itemBox.getChildren().addAll(headerBox, qtyControlBox);
        return itemBox;
    }

    // ── Bill row ──────────────────────────────────────────────────────────────

    private HBox createBillItemRow(CartItem item) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("bill-item-row");

        String name = item.getProductName();
        if (name.length() > 22) name = name.substring(0, 19) + "...";

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("bill-item-name");
        nameLabel.setMinWidth(160);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label qtyLabel = new Label("×" + item.getQuantity());
        qtyLabel.getStyleClass().add("bill-item-qty");
        qtyLabel.setMinWidth(35);
        qtyLabel.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(String.format("৳%.0f", item.getTotalPrice()));
        priceLabel.getStyleClass().add("bill-item-price");
        priceLabel.setMinWidth(75);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        // ── Cancel / Remove button — only removes from cart, stock unchanged ──
        Button removeBtn = new Button("✕ Cancel");
        removeBtn.getStyleClass().add("bill-item-remove-btn");
        removeBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; "
                + "-fx-font-size:11px; -fx-background-radius:5; -fx-cursor:hand; -fx-padding:3 8;");
        removeBtn.setOnAction(e -> removeItemFromCart(item));

        row.getChildren().addAll(nameLabel, qtyLabel, priceLabel, removeBtn);
        return row;
    }

    /**
     * Remove one item from the cart.
     * Stock is NOT changed here — stock is only reduced at checkout/purchase time.
     */
    private void removeItemFromCart(CartItem item) {
        Cart.removeItem(item.getProductId());
        refreshCart();
        statusLabel.setText("🗑️ Removed \"" + item.getProductName() + "\" from cart.");
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML
    private void onBackToHome() {
        Session.goBackFromCart(statusLabel);
    }

    @FXML
    private void onClearCart() {
        if (Cart.isEmpty()) {
            statusLabel.setText("⚠️ Cart is already empty.");
            return;
        }
        // Just clear cart — stock is NOT changed (stock only reduces at checkout)
        Cart.clearCart();
        refreshCart();
        statusLabel.setText("🗑️ Cart cleared.");
    }

    @FXML
    private void onCheckout() {
        if (Cart.isEmpty()) {
            statusLabel.setText("⚠️ Your cart is empty! Add some products first.");
            return;
        }

        // Extract customer info at the beginning
        String custName = customerNameField.getText().trim();
        String custPhone = customerPhoneField.getText().trim();
        String custEmail = customerEmailField.getText().trim();
        String custAddress = customerAddressField.getText().trim();

        // Validate required fields
        if (custName.isEmpty()) {
            statusLabel.setText("⚠️ Please enter Customer Name.");
            customerNameField.requestFocus();
            return;
        }

        if (custPhone.isEmpty()) {
            statusLabel.setText("⚠️ Please enter Customer Phone Number.");
            customerPhoneField.requestFocus();
            return;
        }
        
        if (custPhone.length() != 11 || !custPhone.matches("\\d+")) {
            statusLabel.setText("⚠️ Phone number must be exactly 11 digits.");
            customerPhoneField.requestFocus();
            return;
        }

        List<CartItem> purchasedItems = Cart.getAllItems();
        int totalQty = Cart.getTotalQuantity();
        double totalAmount = Cart.getTotalPrice();

        // 1. Save or Update Customer (with ALL info: name, phone, email, address)
        Customer customerToSave = null;
        for (Customer existing : CustomerManager.getAllCustomers()) {
            if (existing.getPhone().equals(custPhone)) {
                customerToSave = existing;
                break;
            }
        }
        
        if (customerToSave == null) {
            // Create new customer
            customerToSave = new Customer(custName, custPhone, custEmail, custAddress, "Retail", 0.0);
            System.out.println("✓ New Customer Created: " + custName + " | " + custPhone);
        } else {
            // Update existing customer
            customerToSave.setName(custName);
            customerToSave.setPhone(custPhone);
            customerToSave.setEmail(custEmail);
            customerToSave.setAddress(custAddress);
            System.out.println("✓ Existing Customer Updated: " + custName + " | " + custPhone);
        }
        
        CustomerManager.saveCustomer(customerToSave);
        NetworkManager.getInstance().broadcastCustomer(customerToSave);
        System.out.println("  Email: " + custEmail + " | Address: " + custAddress);

        // 2. Process Sale & Stock
        for (CartItem item : purchasedItems) {
            String canonicalProductId = ensureCanonicalStockProduct(item);
            int currentStock = StockManager.getStock(canonicalProductId);
            int newStock = Math.max(0, currentStock - item.getQuantity());
            StockManager.updateStock(canonicalProductId, newStock);
            NetworkManager.getInstance().broadcastStockUpdate(canonicalProductId, newStock);
            SalesTracker.addSale(item.getProductName(), item.getCategory(), item.getUnitPrice(), item.getQuantity());
        }

        SaleRecord sale = NetworkManager.getInstance().buildSaleRecord(purchasedItems, totalQty, totalAmount,
                                                                       custName.isEmpty() ? null : custName,
                                                                       custPhone.isEmpty() ? null : custPhone,
                                                                       custEmail.isEmpty() ? null : custEmail,
                                                                       custAddress.isEmpty() ? null : custAddress);
        SalesManager.recordSale(sale);
        NetworkManager.getInstance().broadcastSaleRecord(sale);

        // Show alert with all customer details
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("✅ Order Confirmed");
        successAlert.setHeaderText("Purchase Successful!");
        successAlert.setContentText("Order Details:\n" +
                "Customer: " + custName + "\n" +
                "Phone: " + custPhone + "\n" +
                "Email: " + (custEmail.isEmpty() ? "N/A" : custEmail) + "\n" +
                "Address: " + (custAddress.isEmpty() ? "N/A" : custAddress) + "\n" +
                "Total Amount: ৳ " + String.format("%.2f", totalAmount) + "\n" +
                "Total Items: " + totalQty + "\n\n" +
                "Payment Method: Cash on Delivery\n" +
                "Status: Confirmed\n\n" +
                "Thank you for shopping with us!");
        successAlert.showAndWait();

        Cart.clearCart();
        refreshCart();
        customerNameField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        customerAddressField.clear();
        
        HomeController.refreshDashboard();
    }

    @FXML
    private void onContinueShopping() {
        Session.goBackFromCart(statusLabel);
    }

    private String ensureCanonicalStockProduct(CartItem item) {
        String productId = item.getProductId();
        if (StockManager.getStockItem(productId) != null) {
            return productId;
        }

        String existingByName = StockManager.findProductIdByName(item.getProductName());
        if (existingByName != null) {
            return existingByName;
        }

        String category = normalizeCategory(item.getCategory());
        String prefix = "Beauty".equals(category) ? "B" : "E";
        String slug = item.getProductName() == null ? "item" : item.getProductName().replaceAll("[^A-Za-z0-9]+", "_");
        if (slug.isBlank()) slug = "item";
        String newId = prefix + "_auto_" + slug;

        int suffix = 2;
        while (StockManager.getStockItem(newId) != null &&
                !item.getProductName().equalsIgnoreCase(StockManager.getStockItem(newId).getProductName())) {
            newId = prefix + "_auto_" + slug + "_" + suffix;
            suffix++;
        }

        int initialStock = Math.max(DEFAULT_NEW_PRODUCT_STOCK, item.getQuantity());
        StockItem newItem = new StockItem(newId, item.getProductName(), category, item.getCategory(),
                initialStock, item.getUnitPrice(), item.getImagePath());
        StockManager.upsertStockItem(newItem);
        NetworkManager.getInstance().broadcastNewProduct(newItem);
        return newId;
    }

    private String normalizeCategory(String category) {
        if (category == null) return "Electronics";
        String c = category.trim();
        if (c.equalsIgnoreCase("Beauty")) return "Beauty";
        if (c.equalsIgnoreCase("Skincare") || c.equalsIgnoreCase("Makeup") || c.equalsIgnoreCase("Haircare")) {
            return "Beauty";
        }
        return "Electronics";
    }
}
