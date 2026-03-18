package com.example.demo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tracks all sales/purchases made by customers
 */
public class SalesTracker {

    public static class SaleRecord {
        public String productName;
        public String category;
        public double price;
        public int quantity;
        public LocalDateTime saleTime;
        public double totalAmount;

        public SaleRecord(String productName, String category, double price, int quantity) {
            this.productName = productName;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.saleTime = LocalDateTime.now();
            this.totalAmount = price * quantity;
        }

        public String getFormattedDate() {
            return saleTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        public String getFormattedAmount() {
            return String.format("₹%.2f", totalAmount);
        }

        @Override
        public String toString() {
            return String.format("%s (%s) - Qty: %d @ ₹%.2f each = %s [%s]",
                    productName, category, quantity, price, getFormattedAmount(), getFormattedDate());
        }
    }

    private static final List<SaleRecord> SALES = new CopyOnWriteArrayList<>();

    static {
        // Load sales from persistent storage if available
        loadFromDisk();
    }

    private SalesTracker() {}

    /**
     * Add a new sale record
     */
    public static void addSale(String productName, String category, double price, int quantity) {
        SaleRecord record = new SaleRecord(productName, category, price, quantity);
        SALES.add(record);
        saveToDiskSafe();
    }

    /**
     * Get all sales records
     */
    public static List<SaleRecord> getAllSales() {
        return new ArrayList<>(SALES);
    }

    /**
     * Get sales for a specific category
     */
    public static List<SaleRecord> getSalesByCategory(String category) {
        List<SaleRecord> result = new ArrayList<>();
        for (SaleRecord record : SALES) {
            if (record.category.equalsIgnoreCase(category)) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get sales for a specific date
     */
    public static List<SaleRecord> getSalesByDate(String date) {
        List<SaleRecord> result = new ArrayList<>();
        for (SaleRecord record : SALES) {
            if (record.getFormattedDate().startsWith(date)) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * Get total sales count
     */
    public static int getTotalSalesCount() {
        return SALES.size();
    }

    /**
     * Get total revenue
     */
    public static double getTotalRevenue() {
        double total = 0;
        for (SaleRecord record : SALES) {
            total += record.totalAmount;
        }
        return total;
    }

    /**
     * Get top selling products
     */
    public static List<SaleRecord> getTopSellingProducts(int limit) {
        List<SaleRecord> sorted = new ArrayList<>(SALES);
        sorted.sort((a, b) -> Integer.compare(b.quantity, a.quantity));
        return sorted.size() > limit ? sorted.subList(0, limit) : sorted;
    }

    /**
     * Clear all sales (for demo purposes)
     */
    public static void clearAllSales() {
        SALES.clear();
        saveToDiskSafe();
    }

    private static void loadFromDisk() {
        // TODO: Implement persistence if needed
    }

    private static void saveToDiskSafe() {
        try {
            saveToDisk();
        } catch (Exception e) {
            System.err.println("[SalesTracker] Failed to save: " + e.getMessage());
        }
    }

    private static void saveToDisk() {
        // TODO: Implement persistence if needed
    }
}

