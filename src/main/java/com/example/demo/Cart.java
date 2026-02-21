package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shopping Cart - stores cart items persistently across page navigation.
 * Uses static storage to maintain cart data throughout the session.
 */
public final class Cart {

    // Static map to store cart items - persists across all pages
    private static final Map<String, CartItem> cartItems = new HashMap<>();

    // Track the last visited page to return to after viewing cart
    private static String lastVisitedPage = "home-view.fxml";

    private Cart() {}

    /**
     * Set the last visited page (called before navigating to cart)
     */
    public static void setLastVisitedPage(String pageFxml) {
        if (pageFxml != null && !pageFxml.isEmpty()) {
            lastVisitedPage = pageFxml;
        }
    }

    /**
     * Get the last visited page
     */
    public static String getLastVisitedPage() {
        return lastVisitedPage;
    }

    /**
     * Add item to cart. If item already exists, increment quantity.
     */
    public static void addItem(CartItem item) {
        if (item == null || item.getProductId() == null) return;

        String productId = item.getProductId();
        if (cartItems.containsKey(productId)) {
            // Item exists, increment quantity
            CartItem existingItem = cartItems.get(productId);
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
        } else {
            // New item
            cartItems.put(productId, item);
        }
    }

    /**
     * Add item to cart with specific quantity
     */
    public static void addItem(String productId, String productName, String category,
                               double unitPrice, int quantity, String imagePath, int discountPercent) {
        CartItem item = new CartItem(productId, productName, category, unitPrice, quantity, imagePath, discountPercent);
        addItem(item);
    }

    /**
     * Remove item from cart completely
     */
    public static void removeItem(String productId) {
        cartItems.remove(productId);
    }

    /**
     * Update quantity for an item
     */
    public static void updateQuantity(String productId, int quantity) {
        if (cartItems.containsKey(productId)) {
            if (quantity <= 0) {
                removeItem(productId);
            } else {
                cartItems.get(productId).setQuantity(quantity);
            }
        }
    }

    /**
     * Increment item quantity by 1
     */
    public static void incrementQuantity(String productId) {
        if (cartItems.containsKey(productId)) {
            cartItems.get(productId).incrementQuantity();
        }
    }

    /**
     * Decrement item quantity by 1 (minimum 1)
     */
    public static void decrementQuantity(String productId) {
        if (cartItems.containsKey(productId)) {
            cartItems.get(productId).decrementQuantity();
        }
    }

    /**
     * Get item by product ID
     */
    public static CartItem getItem(String productId) {
        return cartItems.get(productId);
    }

    /**
     * Get all cart items as a list
     */
    public static List<CartItem> getAllItems() {
        return new ArrayList<>(cartItems.values());
    }

    /**
     * Get total number of unique items in cart
     */
    public static int getItemCount() {
        return cartItems.size();
    }

    /**
     * Get total quantity of all items (sum of all quantities)
     */
    public static int getTotalQuantity() {
        return cartItems.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Get total price of all items in cart
     */
    public static double getTotalPrice() {
        return cartItems.values().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    /**
     * Check if cart is empty
     */
    public static boolean isEmpty() {
        return cartItems.isEmpty();
    }

    /**
     * Check if a specific product is in cart
     */
    public static boolean containsItem(String productId) {
        return cartItems.containsKey(productId);
    }

    /**
     * Clear all items from cart
     */
    public static void clearCart() {
        cartItems.clear();
    }

    /**
     * Get formatted total price string in BDT
     */
    public static String getFormattedTotal() {
        return String.format("৳ %.2f BDT", getTotalPrice());
    }
}

