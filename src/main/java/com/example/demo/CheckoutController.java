package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * CheckoutController - handles the checkout process for orders
 */
public class CheckoutController {

    @FXML private Label cartTotalLabel;
    @FXML private TextField customerNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextArea shippingAddressArea;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Button placeOrderButton;
    @FXML private Button cancelButton;
    @FXML private VBox itemsVBox;
    @FXML private Label statusLabel;

    private List<CartItem> cartItems;
    private String customerId;

    @FXML
    public void initialize() {
        paymentMethodCombo.getItems().addAll("Credit Card", "Debit Card", "Mobile Banking", "Cash on Delivery");
        paymentMethodCombo.setValue("Cash on Delivery");
    }

    public void setCheckoutData(String customerId, String customerName, List<CartItem> cartItems) {
        this.customerId = customerId;
        this.cartItems = cartItems;

        customerNameField.setText(customerName);
        
        // Display cart items
        double total = 0;
        itemsVBox.getChildren().clear();
        for (CartItem item : cartItems) {
            Label itemLabel = new Label(item.getQuantity() + "x " + item.getProductName() 
                    + " - ৳" + String.format("%.2f", item.getUnitPrice() * item.getQuantity()));
            itemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            itemsVBox.getChildren().add(itemLabel);
            total += item.getUnitPrice() * item.getQuantity();
        }

        cartTotalLabel.setText("৳" + String.format("%.2f", total));
    }

    @FXML
    private void onPlaceOrder() {
        String customerName = customerNameField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String shippingAddress = shippingAddressArea.getText().trim();
        String paymentMethod = paymentMethodCombo.getValue();

        if (customerName.isEmpty()) {
            statusLabel.setText("❌ Please enter customer name");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (phoneNumber.isEmpty()) {
            statusLabel.setText("❌ Please enter phone number");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        if (shippingAddress.isEmpty()) {
            statusLabel.setText("❌ Please enter shipping address");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        try {
            String orderId = OrderManager.createOrder(
                    customerId,
                    customerName,
                    phoneNumber,
                    shippingAddress,
                    paymentMethod,
                    cartItems
            );

            // Clear cart
            Cart.clearCart();

            statusLabel.setText("✓ Order placed successfully! Order ID: " + orderId);
            statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

            // Close dialog after 2 seconds
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(2000);
                    Stage stage = (Stage) placeOrderButton.getScene().getWindow();
                    stage.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}

