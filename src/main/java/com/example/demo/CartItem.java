package com.example.demo;

/**
 * Represents an item in the shopping cart
 */
public class CartItem {
    private String productId;
    private String productName;
    private String category;
    private double unitPrice;
    private int quantity;
    private String imagePath;
    private int discountPercent;

    public CartItem(String productId, String productName, String category, double unitPrice, int quantity, String imagePath, int discountPercent) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imagePath = imagePath;
        this.discountPercent = discountPercent;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public void incrementQuantity() {
        this.quantity++;
    }

    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public double getTotalPrice() {
        double discountedPrice = unitPrice;
        if (discountPercent > 0) {
            discountedPrice = unitPrice * (1 - discountPercent / 100.0);
        }
        return discountedPrice * quantity;
    }

    public double getDiscountedUnitPrice() {
        if (discountPercent > 0) {
            return unitPrice * (1 - discountPercent / 100.0);
        }
        return unitPrice;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CartItem cartItem = (CartItem) obj;
        return productId != null && productId.equals(cartItem.productId);
    }

    @Override
    public int hashCode() {
        return productId != null ? productId.hashCode() : 0;
    }
}

