package com.example.demo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SharedStore manages synchronized data between customer and owner sides.
 *
 * Features:
 * - Unified customer store accessible by both sides
 * - Bi-directional sales synchronization
 * - All data sorted by timestamp
 */
public final class SharedStore {
    private SharedStore() {}

    /**
     * Get all customers sorted by creation time (ascending).
     * This represents the "sob store" - the shared customer database.
     */
    public static synchronized List<Customer> getAllCustomersSorted() {
        return CustomerManager.getAllCustomersSorted();
    }

    /**
     * Get all sales records sorted by timestamp (descending - newest first).
     * This includes sales from both customer and owner sides.
     */
    public static synchronized List<SaleRecord> getAllSalesSorted() {
        return SalesManager.getAllSalesSorted();
    }

    /**
     * Get all sales records sorted by timestamp (ascending - oldest first).
     */
    public static synchronized List<SaleRecord> getAllSalesSortedAscending() {
        return SalesManager.getAllSalesSortedAscending();
    }

    /**
     * Get sales for a specific customer, sorted by timestamp (newest first).
     */
    public static synchronized List<SaleRecord> getCustomerSalesSorted(String customerId) {
        Customer customer = getCustomerById(customerId);
        if (customer == null) return new ArrayList<>();
        return SalesManager.getSalesByCustomer(customer.getPhone());
    }

    /**
     * Get sales by a specific seller, sorted by timestamp (newest first).
     */
    public static synchronized List<SaleRecord> getSellerSalesSorted(String soldBy) {
        return SalesManager.getSalesBySeller(soldBy);
    }

    /**
     * Get sales by source node (owner or customer side), sorted by timestamp.
     */
    public static synchronized List<SaleRecord> getNodeSalesSorted(String sourceNode) {
        return SalesManager.getSalesBySourceNode(sourceNode);
    }

    /**
     * Get all unique source nodes (owner/customer machines).
     */
    public static synchronized Set<String> getAllSourceNodes() {
        return SalesManager.getAllSales().stream()
                .map(SaleRecord::getSourceNode)
                .collect(Collectors.toSet());
    }

    /**
     * Get customer by ID.
     */
    public static synchronized Customer getCustomerById(String customerId) {
        return CustomerManager.getCustomerById(customerId);
    }

    /**
     * Get customer by phone number.
     */
    public static synchronized Customer getCustomerByPhone(String phone) {
        return CustomerManager.getCustomerByPhone(phone);
    }

    /**
     * Add or update a customer in the shared store.
     * Will broadcast to other nodes if networking is active.
     */
    public static synchronized void addOrUpdateCustomer(Customer customer) {
        if (customer == null || customer.getId() == null) return;
        CustomerManager.saveCustomer(customer);
        // Broadcast to other nodes
        NetworkManager.getInstance().broadcastCustomer(customer);
    }

    /**
     * Record a sale in the shared store.
     * Will broadcast to other nodes if networking is active.
     */
    public static synchronized void recordSharedSale(SaleRecord sale) {
        if (sale == null || sale.getSaleId() == null) return;
        SalesManager.recordSale(sale);
        // Broadcast to other nodes
        NetworkManager.getInstance().broadcastSaleRecord(sale);
    }

    /**
     * Get summary statistics for all sales, sorted by date.
     */
    public static synchronized Map<String, Object> getSalesSummary() {
        List<SaleRecord> allSales = getAllSalesSorted();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalSales", allSales.size());
        summary.put("totalAmount", allSales.stream().mapToDouble(SaleRecord::getTotalAmount).sum());
        summary.put("totalQuantity", allSales.stream().mapToInt(SaleRecord::getTotalQuantity).sum());
        summary.put("totalCustomers", CustomerManager.getAllCustomers().size());

        // Group by source node
        Map<String, Long> byNode = allSales.stream()
                .collect(Collectors.groupingBy(SaleRecord::getSourceNode, Collectors.counting()));
        summary.put("salesByNode", byNode);

        // Group by seller
        Map<String, Long> bySeller = allSales.stream()
                .collect(Collectors.groupingBy(SaleRecord::getSoldBy, Collectors.counting()));
        summary.put("salesBySeller", bySeller);

        // Latest sale
        if (!allSales.isEmpty()) {
            summary.put("latestSale", allSales.get(0));
        }

        return summary;
    }

    /**
     * Get customer statistics sorted by total purchase amount (descending).
     */
    public static synchronized List<Map<String, Object>> getTopCustomers() {
        Map<String, Object[]> customerStats = new LinkedHashMap<>(); // [totalAmount, totalQuantity, count]

        for (SaleRecord sale : SalesManager.getAllSales()) {
            String phone = sale.getCustomerPhone();
            if (phone == null || phone.isEmpty() || phone.equals("N/A")) continue;

            Object[] stats = customerStats.computeIfAbsent(phone, k -> new Object[]{0.0, 0, 0, null});
            stats[0] = (Double) stats[0] + sale.getTotalAmount();
            stats[1] = (Integer) stats[1] + sale.getTotalQuantity();
            stats[2] = (Integer) stats[2] + 1;
        }

        // Add customer names
        for (String phone : customerStats.keySet()) {
            Customer c = getCustomerByPhone(phone);
            customerStats.get(phone)[3] = c != null ? c.getName() : "Unknown";
        }

        // Convert to list and sort by total amount descending
        return customerStats.entrySet().stream()
                .map(e -> {
                    Object[] stats = e.getValue();
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("phone", e.getKey());
                    m.put("name", stats[3]);
                    m.put("totalAmount", stats[0]);
                    m.put("totalQuantity", stats[1]);
                    m.put("purchaseCount", stats[2]);
                    return m;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("totalAmount"), (Double) a.get("totalAmount")))
                .collect(Collectors.toList());
    }

    /**
     * Clear all shared data (use with caution).
     */
    public static synchronized void clearAllData() {
        SalesManager.clearAllSales();
        // Note: Not clearing customers by default as they may be important
    }

    /**
     * Synchronize data between customer and owner sides.
     * Should be called on periodic intervals to ensure consistency.
     */
    public static synchronized void syncData() {
        // Ensure both CustomerManager and SalesManager are initialized
        CustomerManager.initializeCustomers();
        SalesManager.initializeSales();
    }

    /**
     * Get a customer purchase history with all details, sorted by timestamp.
     */
    public static synchronized Map<String, Object> getCustomerPurchaseHistory(String customerId) {
        Customer customer = getCustomerById(customerId);
        if (customer == null) return new LinkedHashMap<>();

        Map<String, Object> history = new LinkedHashMap<>();
        history.put("customerId", customer.getId());
        history.put("customerName", customer.getName());
        history.put("customerPhone", customer.getPhone());
        history.put("customerEmail", customer.getEmail());
        history.put("customerAddress", customer.getAddress());
        history.put("customerType", customer.getType());
        history.put("createdAt", customer.getCreatedAt());
        history.put("dueBalance", customer.getDueBalance());

        List<SaleRecord> purchases = getCustomerSalesSorted(customerId);
        history.put("purchases", purchases);
        history.put("totalPurchases", purchases.size());
        history.put("totalAmount", purchases.stream().mapToDouble(SaleRecord::getTotalAmount).sum());
        history.put("totalQuantity", purchases.stream().mapToInt(SaleRecord::getTotalQuantity).sum());

        return history;
    }
}

