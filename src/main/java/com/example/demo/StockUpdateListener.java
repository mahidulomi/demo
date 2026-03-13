package com.example.demo;

/**
 * Listener interface for real-time stock updates from the network.
 * Controllers implement this to refresh their UI when another machine buys a product.
 */
public interface StockUpdateListener {
    void onStockUpdated(String productId, int newQuantity);
}

