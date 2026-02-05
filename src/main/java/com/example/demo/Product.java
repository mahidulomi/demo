package com.example.demo;

/**
 * Simple product model for demo shop.
 */
public record Product(
        String id,
        String name,
        String category,      // "Fashion", "Electronics", etc.
        String subcategory,   // "Baby", "Male", "Female", etc. (for Fashion)
        double price,         // in BDT
        int discountPercent   // 0 means no discount
) {
    public double getDiscountedPrice() {
        if (discountPercent <= 0) return price;
        return price * (1.0 - discountPercent / 100.0);
    }

    public boolean hasDiscount() {
        return discountPercent > 0;
    }
}
