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
 * Controller for Shopping Cart page
 */
public class CartController {

    @FXML
    private Label statusLabel;


    @FXML
    private VBox billItemsContainer;

    @FXML
    private Label billDateLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label totalQuantityLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private void initialize() {
        refreshCart();
    }

    /**
     * Refresh the cart display
     */
    private void refreshCart() {
        List<CartItem> items = Cart.getAllItems();

        if (items.isEmpty()) {
            statusLabel.setText("🛒 Your cart is empty. Start shopping!");
        } else {
            statusLabel.setText("🛒 " + items.size() + " item(s) in your cart");
        }

        // Update bill summary
        updateSummary();
    }


    /**
     * Update cart summary labels and bill receipt
     */
    private void updateSummary() {
        totalItemsLabel.setText(String.valueOf(Cart.getItemCount()));
        totalQuantityLabel.setText(String.valueOf(Cart.getTotalQuantity()));
        totalPriceLabel.setText(Cart.getFormattedTotal());

        // Update bill date
        if (billDateLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            billDateLabel.setText("Date: " + now.format(formatter));
        }

        // Update subtotal
        if (subtotalLabel != null) {
            subtotalLabel.setText(String.format("৳ %.2f", Cart.getTotalPrice()));
        }

        // Populate bill items
        if (billItemsContainer != null) {
            billItemsContainer.getChildren().clear();

            List<CartItem> items = Cart.getAllItems();
            for (CartItem item : items) {
                HBox itemRow = createBillItemRow(item);
                billItemsContainer.getChildren().add(itemRow);
            }

            // If cart is empty, show a message
            if (items.isEmpty()) {
                Label emptyLabel = new Label("(No items)");
                emptyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999; -fx-font-style: italic;");
                billItemsContainer.getChildren().add(emptyLabel);
            }
        }
    }

    /**
     * Create a row for the bill receipt with remove button
     */
    private HBox createBillItemRow(CartItem item) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("bill-item-row");

        // Truncate product name if too long
        String productName = item.getProductName();
        if (productName.length() > 20) {
            productName = productName.substring(0, 17) + "...";
        }

        Label nameLabel = new Label(productName);
        nameLabel.getStyleClass().add("bill-item-name");
        nameLabel.setMinWidth(180);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label qtyLabel = new Label("x" + item.getQuantity());
        qtyLabel.getStyleClass().add("bill-item-qty");
        qtyLabel.setMinWidth(40);
        qtyLabel.setAlignment(Pos.CENTER);

        Label priceLabel = new Label(String.format("৳%.0f", item.getTotalPrice()));
        priceLabel.getStyleClass().add("bill-item-price");
        priceLabel.setMinWidth(80);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        // Remove button
        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("bill-item-remove-btn");
        removeBtn.setMinWidth(30);
        removeBtn.setOnAction(e -> {
            Cart.removeItem(item.getProductId());
            refreshCart();
        });

        row.getChildren().addAll(nameLabel, qtyLabel, priceLabel, removeBtn);
        return row;
    }

    @FXML
    private void onBackToHome() {
        Session.goBackFromCart(statusLabel);
    }

    @FXML
    private void onClearCart() {
        Cart.clearCart();
        refreshCart();
        statusLabel.setText("🗑️ Cart has been cleared!");
    }

    @FXML
    private void onCheckout() {
        if (Cart.isEmpty()) {
            statusLabel.setText("⚠️ Your cart is empty! Add some products first.");
        } else {
            statusLabel.setText("✓ Proceeding to checkout... (Demo - Total: " + Cart.getFormattedTotal() + ")");
        }
    }

    @FXML
    private void onContinueShopping() {
        Session.goBackFromCart(statusLabel);
    }
}

