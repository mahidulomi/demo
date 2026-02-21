package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller for Free Delivery page - products with free delivery from all categories
 */
public class FreeDeliveryController {

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane productGrid;

    @FXML
    private void initialize() {
        statusLabel.setText("🚚 12 Products with Free Delivery - From All Categories!");
        // Setup arrow button handlers for each product card
        if (productGrid != null) {
            for (javafx.scene.Node node : productGrid.getChildren()) {
                if (node instanceof VBox) {
                    VBox productCard = (VBox) node;
                    setupArrowButtons(productCard);
                }
            }
        }
    }

    /**
     * Setup arrow button click handlers for quantity increase/decrease
     */
    private void setupArrowButtons(VBox productCard) {
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
                                    e.consume();
                                    int currentQty = Integer.parseInt(finalQtyLabel.getText());
                                    finalQtyLabel.setText(String.valueOf(currentQty + 1));
                                });
                            }

                            if (downBtn != null && qtyLabel != null) {
                                final Label finalQtyLabel = qtyLabel;
                                downBtn.setOnAction(e -> {
                                    e.consume();
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
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }

    @FXML
    private void onOpenCart() {
        Session.goToCartFrom(statusLabel, "free-delivery-view.fxml");
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
        String productId = "";

        if (card != null) {
            for (javafx.scene.Node node : card.getChildren()) {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    if (label.getStyleClass().contains("product-name")) {
                        productName = label.getText();
                        productId = "FD_" + productName.hashCode();
                    } else if (label.getStyleClass().contains("product-price") ||
                               label.getStyleClass().contains("product-price-discount")) {
                        String priceText = label.getText();
                        // Parse price: "৳900 - Free Delivery!" or "BDT 1125  (was BDT 1500)"
                        if (priceText.contains("(was")) {
                            String[] parts = priceText.split("\\(was");
                            productPrice = parts[0].replace("BDT", "").replace("৳", "").replace(",", "").trim();
                            String originalPrice = parts[1].replace("BDT", "").replace("৳", "").replace(")", "").replace(",", "").trim();
                            try {
                                double discounted = Double.parseDouble(productPrice);
                                double original = Double.parseDouble(originalPrice);
                                discountPercent = (int) Math.round((1 - discounted / original) * 100);
                                productPrice = originalPrice;
                            } catch (NumberFormatException e) {
                                productPrice = "0";
                            }
                        } else {
                            // Format: "৳900 - Free Delivery!"
                            productPrice = priceText.replace("BDT", "")
                                                   .replace("৳", "")
                                                   .replace(",", "")
                                                   .replace("- Free Delivery!", "")
                                                   .trim();
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

        // Add to cart
        try {
            double price = Double.parseDouble(productPrice);
            Cart.addItem(productId, productName, "Free Delivery", price, quantity, "", discountPercent);
        } catch (NumberFormatException e) {
            Cart.addItem(productId, productName, "Free Delivery", 0, quantity, "", 0);
        }

        // Update button and status
        btn.setText("✓ Added!");
        statusLabel.setText("✓ Added to cart: " + productName + " (Qty: " + quantity + ") | Cart Total: " + Cart.getTotalQuantity() + " items");

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
}

