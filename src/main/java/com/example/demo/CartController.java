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

    @FXML private Label statusLabel;
    @FXML private VBox  billItemsContainer;
    @FXML private Label billDateLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label totalQuantityLabel;
    @FXML private Label totalPriceLabel;

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
        // NOW reduce stock for every purchased item
        for (CartItem item : Cart.getAllItems()) {
            int currentStock = StockManager.getStock(item.getProductId());
            int newStock = Math.max(0, currentStock - item.getQuantity());
            StockManager.updateStock(item.getProductId(), newStock);
            NetworkManager.getInstance().broadcastStockUpdate(item.getProductId(), newStock);
        }
        int    totalQty = Cart.getTotalQuantity();
        String total    = Cart.getFormattedTotal();
        Cart.clearCart();
        refreshCart();
        statusLabel.setText("✅ Purchase successful!  " + totalQty
                + " item(s) bought — Total: " + total + "  Thank you! 🎉");
    }

    @FXML
    private void onContinueShopping() {
        Session.goBackFromCart(statusLabel);
    }
}
