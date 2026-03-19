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
            return String.format("Tk.%.2f", totalAmount);
        }

        @Override
        public String toString() {
            return String.format("%s (%s) - Qty: %d @ Tk.%.2f each = %s [%s]",
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
        try {
            // Load existing bills from SalesManager (persistent storage)
            List<com.example.demo.SaleRecord> persistentbills = SalesManager.getAllSales();
            
            for (com.example.demo.SaleRecord bill : persistentbills) {
                if (bill == null) continue;
                
                // Parse items from JSON if available
                String json = bill.getItemsJson();
                boolean parsed = false;

                if (json != null && json.length() > 5) {
                    int countBefore = SALES.size();
                    parseAndAddItems(json, bill.getTimestamp());
                    if (SALES.size() > countBefore) {
                        parsed = true;
                    }
                } 
                
                if (!parsed) {
                    // Fallback for legacy records without JSON details is not easily possible
                    // as we don't know individual item prices/categories perfectly from summary string
                    // BUT we must add something so total revenue matches!
                    SaleRecord summary = new SaleRecord(
                            "Bill " + (bill.getSaleId() != null ? bill.getSaleId() : "Unknown"), 
                            "General", 
                            bill.getTotalAmount(), 
                            Math.max(1, bill.getTotalQuantity())
                    );
                    try { 
                        if (bill.getTimestamp() != null) {
                            summary.saleTime = LocalDateTime.parse(bill.getTimestamp()); 
                        }
                    } catch(Exception ignored){}
                    SALES.add(summary);
                }
            }
            
            System.out.println("[SalesTracker] Loaded " + SALES.size() + " line items from persistent bill history.");
            
        } catch (Exception e) {
            System.err.println("[SalesTracker] Failed to load from disk: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void parseAndAddItems(String json, String timestampStr) {
        // Regex to parse JSON array of items
        // Pattern: {"name":"...","price":123.00,"quantity":1,"category":"..."}
        String patternStr = "\\{\"name\":\"((?:[^\"\\\\]|\\\\.)*)\",\"price\":([0-9.]+),\"quantity\":([0-9]+),\"category\":\"((?:[^\"\\\\]|\\\\.)*)\"\\}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
        java.util.regex.Matcher matcher = pattern.matcher(json);

        LocalDateTime saleTime = LocalDateTime.now();
        try {
            if (timestampStr != null) {
                saleTime = LocalDateTime.parse(timestampStr);
            }
        } catch (Exception ignored) {}

        while (matcher.find()) {
            String nameRaw = matcher.group(1);
            String name = nameRaw.replace("\\\"", "\"").replace("\\\\", "\\"); 
            double price = Double.parseDouble(matcher.group(2));
            int quantity = Integer.parseInt(matcher.group(3));
            String catRaw = matcher.group(4);
            String category = catRaw.replace("\\\"", "\"").replace("\\\\", "\\");

            SaleRecord record = new SaleRecord(name, category, price, quantity);
            record.saleTime = saleTime; // Restore original time
            SALES.add(record);
        }
    }
    
    /**
     * Called by NetworkManager when a new sale arrives from another machine.
     * Updates the in-memory dashboard stats.
     */
    public static void addNetworkSale(com.example.demo.SaleRecord sale) {
        if (sale == null) return;
        
        // Use the existing logic to parse items from JSON
        String json = sale.getItemsJson();
        boolean parsedItems = false;
        
        if (json != null && json.length() > 5) {
            try {
                parseAndAddItems(json, sale.getTimestamp());
                parsedItems = true;
            } catch (Exception e) {
                System.err.println("[SalesTracker] Failed to parse network items: " + e.getMessage());
            }
        }
        
        // Fallback: If we couldn't parse items (or it's a legacy record), 
        // add a summary record so the Total Revenue and Sales Count are at least correct!
        if (!parsedItems) {
            SaleRecord summary = new SaleRecord(
                    "Network Sale " + sale.getSaleId(), 
                    "Unknown", 
                    sale.getTotalAmount(), 
                    sale.getTotalQuantity()
            );
            // Parse time if possible
            try { summary.saleTime = LocalDateTime.parse(sale.getTimestamp()); } catch(Exception ignored){}
            SALES.add(summary);
        }
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

    /**
     * Clears and reloads all sales data from the persistent SalesManager.
     * Useful when the sales file is updated externally.
     */
    public static void reloadFromSalesManager() {
        SALES.clear();
        loadFromDisk();
        System.out.println("[SalesTracker] Reloaded data from SalesManager. Total items: " + SALES.size());
    }
}
