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
        if (item == null || item.getProductId() == null) {
            System.err.println("⚠️ Cart: Cannot add null item or item with null ID");
            return;
        }

        String productId = item.getProductId();
        if (cartItems.containsKey(productId)) {
            // Item exists, increment quantity
            CartItem existingItem = cartItems.get(productId);
            int oldQty = existingItem.getQuantity();
            existingItem.setQuantity(oldQty + item.getQuantity());
            System.out.println("✓ Cart: Updated " + item.getProductName() + " quantity from " + oldQty + " to " + existingItem.getQuantity());
        } else {
            // New item
            cartItems.put(productId, item);
            System.out.println("✓ Cart: Added new item - " + item.getProductName() + " (Qty: " + item.getQuantity() + ", Price: ৳" + item.getUnitPrice() + ")");
        }
        System.out.println("📊 Cart Status: " + getItemCount() + " unique items, Total Qty: " + getTotalQuantity() + ", Total Price: ৳" + getTotalPrice());
    }

    /**
     * Add item to cart with specific quantity
     */
    public static void addItem(String productId, String productName, String category,
                               double unitPrice, int quantity, String imagePath, int discountPercent) {
        if (productId == null || productId.isEmpty()) {
            System.err.println("⚠️ Cart: Cannot add item with empty product ID");
            return;
        }
        
        CartItem item = new CartItem(productId, productName, category, unitPrice, quantity, imagePath, discountPercent);
        addItem(item);
    }

    /**
     * Remove item from cart completely
     */
    public static void removeItem(String productId) {
        cartItems.remove(productId);
        System.out.println("✓ Cart: Removed item with ID " + productId);
        System.out.println("📊 Cart Status: " + getItemCount() + " unique items, Total Qty: " + getTotalQuantity() + ", Total Price: ৳" + getTotalPrice());
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
                System.out.println("✓ Cart: Updated " + productId + " quantity to " + quantity);
            }
        }
    }

    /**
     * Increment item quantity by 1
     */
    public static void incrementQuantity(String productId) {
        if (cartItems.containsKey(productId)) {
            cartItems.get(productId).incrementQuantity();
            System.out.println("✓ Cart: Incremented " + productId + " quantity");
        }
    }

    /**
     * Decrement item quantity by 1 (minimum 1)
     */
    public static void decrementQuantity(String productId) {
        if (cartItems.containsKey(productId)) {
            cartItems.get(productId).decrementQuantity();
            System.out.println("✓ Cart: Decremented " + productId + " quantity");
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
        System.out.println("✓ Cart: Cleared all items");
    }

    /**
     * Get formatted total price string in BDT
     */
    public static String getFormattedTotal() {
        return String.format("৳ %.2f BDT", getTotalPrice());
    }
}

