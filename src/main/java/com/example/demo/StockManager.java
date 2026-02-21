package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages stock quantities for all products across the application.
 * Uses static storage to maintain stock data throughout the session.
 */
public final class StockManager {

    // Static map to store stock data - productId -> StockItem
    private static final Map<String, StockItem> stockData = new HashMap<>();

    // Flag to check if initial data is loaded
    private static boolean initialized = false;

    private StockManager() {}

    /**
     * Initialize with default stock data (call once at app start)
     */
    public static void initializeStock() {
        if (initialized) return;

        // Electronics Products - All start with 25 stock
        addStock("E_iPhone15", "iPhone 15", "Electronics", 25, 99999);
        addStock("E_iPhone16", "iPhone 16", "Electronics", 25, 120000);
        addStock("E_iPhone17", "iPhone 17", "Electronics", 25, 139000);
        addStock("E_SamsungS25", "Samsung Galaxy S25", "Electronics", 25, 120000);
        addStock("E_VivoX200", "Vivo X200 Ultra", "Electronics", 25, 90000);
        addStock("E_LenovoIdeaPad", "Lenovo IdeaPad i5 8GB SSD", "Electronics", 25, 144000);
        addStock("E_WirelessEarbuds", "Wireless Earbuds Pro", "Electronics", 25, 5000);
        addStock("E_SmartWatch", "Smart Watch Fitness", "Electronics", 25, 10000);
        addStock("E_PowerBank", "Power Bank 20000mAh", "Electronics", 25, 2500);
        addStock("E_iPad", "iPad", "Electronics", 25, 30000);
        addStock("E_Mouse", "Mouse", "Electronics", 25, 5000);
        addStock("E_AsusVivoBook", "Asus VivoBook Ryzen 5 16GB", "Electronics", 25, 116000);
        addStock("E_AjazzK80", "Ajazz K80 Redswitch", "Electronics", 25, 4200);

        // Beauty Products - All start with 25 stock
        addStock("B_FaceCream", "Vitamin C Face Cream", "Beauty", 25, 1500);
        addStock("B_Serum", "Hyaluronic Acid Serum", "Beauty", 25, 2000);
        addStock("B_FoamCleanser", "Gentle Foam Cleanser", "Beauty", 25, 800);
        addStock("B_LipstickSet", "Matte Lipstick Set", "Beauty", 25, 1000);
        addStock("B_Foundation", "HD Foundation", "Beauty", 25, 2000);
        addStock("B_Eyeshadow", "Eyeshadow Palette", "Beauty", 25, 2000);
        addStock("B_Shampoo", "Keratin Repair Shampoo", "Beauty", 25, 900);
        addStock("B_Conditioner", "Deep Conditioner", "Beauty", 25, 1000);
        addStock("B_HairOil", "Argan Hair Oil", "Beauty", 25, 1200);
        addStock("B_Sunscreen", "SPF 50+ Sunscreen", "Beauty", 25, 1300);
        addStock("B_Mascara", "Volumizing Mascara", "Beauty", 25, 800);
        addStock("B_GentsFaceWash", "Gents Face Wash", "Beauty", 25, 1500);

        initialized = true;
    }

    /**
     * Add a new stock item
     */
    public static void addStock(String productId, String productName, String category, int quantity, double price) {
        StockItem item = new StockItem(productId, productName, category, quantity, price);
        stockData.put(productId, item);
    }

    /**
     * Update stock quantity for a product
     */
    public static void updateStock(String productId, int newQuantity) {
        if (stockData.containsKey(productId)) {
            stockData.get(productId).setQuantity(Math.max(0, newQuantity));
        }
    }

    /**
     * Reduce stock when product is sold/added to cart
     */
    public static boolean reduceStock(String productId, int quantity) {
        if (stockData.containsKey(productId)) {
            StockItem item = stockData.get(productId);
            if (item.getQuantity() >= quantity) {
                item.setQuantity(item.getQuantity() - quantity);
                return true;
            }
        }
        return false;
    }

    /**
     * Get stock quantity for a product
     */
    public static int getStock(String productId) {
        if (stockData.containsKey(productId)) {
            return stockData.get(productId).getQuantity();
        }
        return 0;
    }

    /**
     * Get stock item by product ID
     */
    public static StockItem getStockItem(String productId) {
        return stockData.get(productId);
    }

    /**
     * Get all stock items as a list
     */
    public static List<StockItem> getAllStockItems() {
        initializeStock(); // Ensure data is loaded
        return new ArrayList<>(stockData.values());
    }

    /**
     * Get stock items by category
     */
    public static List<StockItem> getStockByCategory(String category) {
        List<StockItem> items = new ArrayList<>();
        for (StockItem item : stockData.values()) {
            if (item.getCategory().equals(category)) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Get low stock items (quantity <= 10)
     */
    public static List<StockItem> getLowStockItems() {
        List<StockItem> items = new ArrayList<>();
        for (StockItem item : stockData.values()) {
            if (item.getQuantity() > 0 && item.getQuantity() <= 10) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Get out of stock items (quantity = 0)
     */
    public static List<StockItem> getOutOfStockItems() {
        List<StockItem> items = new ArrayList<>();
        for (StockItem item : stockData.values()) {
            if (item.getQuantity() <= 0) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Check if product is in stock
     */
    public static boolean isInStock(String productId) {
        return getStock(productId) > 0;
    }

    /**
     * Check if product has low stock
     */
    public static boolean isLowStock(String productId) {
        int qty = getStock(productId);
        return qty > 0 && qty <= 10;
    }
}

