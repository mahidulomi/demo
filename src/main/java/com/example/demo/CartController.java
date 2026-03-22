package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
    @FXML private VBox  billItemsContainer;
    @FXML private Label billDateLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label totalQuantityLabel;
    @FXML private Label totalPriceLabel;

    // Customer Input Fields
    @FXML private javafx.scene.control.TextField customerNameField;
    @FXML private javafx.scene.control.TextField customerPhoneField;

    @FXML
    private void initialize() {
        refreshCart();
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    private void refreshCart() {
        List<CartItem> items = Cart.getAllItems();
        statusLabel.setText(items.isEmpty()
                ? "🛒 Your cart is empty. Start shopping!"
                : "🛒 " + items.size() + " item(s) in your cart");
        updateSummary();
        
        // Reset customer fields if cart is cleared, but not on refresh unless empty
        if(items.isEmpty() && customerNameField != null) {
             customerNameField.clear();
             customerPhoneField.clear();
        }
    }

    private void updateSummary() {
        totalItemsLabel.setText(String.valueOf(Cart.getItemCount()));
        totalQuantityLabel.setText(String.valueOf(Cart.getTotalQuantity()));
        totalPriceLabel.setText(Cart.getFormattedTotal());

        if (billDateLabel != null) {
            billDateLabel.setText("Date: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        }
        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("৳ %.2f", Cart.getTotalPrice()));
        }

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

        // Validate Customer Info (Required as per request)
        String customerName = customerNameField.getText().trim();
        String customerPhone = customerPhoneField.getText().trim();

        if (customerName.isEmpty()) {
            statusLabel.setText("⚠️ Please enter Customer Name.");
            customerNameField.requestFocus();
            return;
        }

        if (customerPhone.isEmpty()) {
            statusLabel.setText("⚠️ Please enter Customer Phone Number.");
            customerPhoneField.requestFocus();
            return;
        }
        
        if (customerPhone.length() != 11 || !customerPhone.matches("\\d+")) {
            statusLabel.setText("⚠️ Phone number must be exactly 11 digits.");
            customerPhoneField.requestFocus();
            return;
        }

        List<CartItem> purchasedItems = Cart.getAllItems();
        int totalQty = Cart.getTotalQuantity();
        double totalAmount = Cart.getTotalPrice();

        // 1. Save or Update Customer
        // Check if customer exists by phone
        Customer customerToSave = null;
        for (Customer existing : CustomerManager.getAllCustomers()) {
            if (existing.getPhone().equals(customerPhone)) {
                customerToSave = existing;
                // Update name if changed? Let's just update name to current input
                customerToSave.setName(customerName); 
                break;
            }
        }
        
        if (customerToSave == null) {
            // New Customer
            customerToSave = new Customer(customerName, customerPhone, "", "", "Retail", 0.0);
        }
        
        // Update Due Balance if needed (assuming fully paid here, so no due balance change unless we add credit sales)
        // For now, simple retail sale
        CustomerManager.saveCustomer(customerToSave);
        NetworkManager.getInstance().broadcastCustomer(customerToSave);

        // 2. Process Sale & Stock
        // NOW reduce stock for every purchased item
        for (CartItem item : purchasedItems) {
            String canonicalProductId = ensureCanonicalStockProduct(item);
            int currentStock = StockManager.getStock(canonicalProductId);
            int newStock = Math.max(0, currentStock - item.getQuantity());
            StockManager.updateStock(canonicalProductId, newStock);
            NetworkManager.getInstance().broadcastStockUpdate(canonicalProductId, newStock);
            
            // Record sale in SalesTracker for dashboard
            // linking customerName to sale is tricky without changing SaleRecord structure heavily
            // but we can append it to category or product name in tracking if needed, 
            // or just rely on CustomerManager having the customer.
            SalesTracker.addSale(item.getProductName(), item.getCategory(), item.getUnitPrice(), item.getQuantity());
        }

        SaleRecord sale = NetworkManager.getInstance().buildSaleRecord(purchasedItems, totalQty, totalAmount);
        SalesManager.recordSale(sale);
        NetworkManager.getInstance().broadcastSaleRecord(sale);

        String total = String.format("৳ %.2f BDT", totalAmount);
        Cart.clearCart();
        refreshCart();
        statusLabel.setText("✅ Purchase successful! Customer Add/Updated. Total: " + total);
        
        // Clear inputs
        customerNameField.clear();
        customerPhoneField.clear();
        
        // Refresh dashboard stats
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
