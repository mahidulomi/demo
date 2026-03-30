package com.example.demo;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * OrderManager - manages all customer orders and their persistence.
 */
public final class OrderManager {

    private static final Path ORDERS_FILE = 
            Paths.get(System.getProperty("user.home"), ".shopapp_orders.dat");

    private static final List<Order> allOrders = new CopyOnWriteArrayList<>();
    private static boolean initialized = false;

    private OrderManager() {}

    public static synchronized void initializeOrders() {
        if (initialized) return;
        
        if (Files.exists(ORDERS_FILE)) {
            loadOrdersFromFile();
        }
        
        initialized = true;
        System.out.println("[OrderManager] Ready — " + allOrders.size() + " orders");
    }

    /**
     * Create a new order from cart items
     */
    public static synchronized String createOrder(String customerId, String customerName, 
                                                   String phoneNumber, String shippingAddress, 
                                                   String paymentMethod, List<CartItem> cartItems) {
        initializeOrders();
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot create order with empty cart");
        }

        Order order = new Order(customerId, customerName, phoneNumber, shippingAddress);
        order.setPaymentMethod(paymentMethod);

        // Add items to order
        for (CartItem cartItem : cartItems) {
            order.addItem(
                    cartItem.getProductId(),
                    cartItem.getProductName(),
                    cartItem.getCategory(),
                    cartItem.getUnitPrice(),
                    cartItem.getQuantity(),
                    cartItem.getImagePath()
            );
            
            // Reduce stock
            StockManager.updateStock(cartItem.getProductId(), 
                    StockManager.getStock(cartItem.getProductId()) - cartItem.getQuantity());
        }

        allOrders.add(order);
        saveOrdersToFile();
        
        System.out.println("[OrderManager] Order created: " + order.getOrderId() + " for customer: " + customerName);
        return order.getOrderId();
    }

    /**
     * Get all orders for a specific customer
     */
    public static List<Order> getCustomerOrders(String customerId) {
        initializeOrders();
        List<Order> customerOrders = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.getCustomerId().equals(customerId)) {
                customerOrders.add(order);
            }
        }
        return customerOrders;
    }

    /**
     * Get a specific order by ID
     */
    public static Order getOrderById(String orderId) {
        initializeOrders();
        for (Order order : allOrders) {
            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    /**
     * Get all orders (admin view)
     */
    public static List<Order> getAllOrders() {
        initializeOrders();
        return new ArrayList<>(allOrders);
    }

    /**
     * Update order status
     */
    public static synchronized void updateOrderStatus(String orderId, String newStatus) {
        initializeOrders();
        Order order = getOrderById(orderId);
        if (order != null) {
            order.setStatus(newStatus);
            saveOrdersToFile();
            System.out.println("[OrderManager] Order " + orderId + " status updated to: " + newStatus);
        }
    }

    /**
     * Cancel an order
     */
    public static synchronized boolean cancelOrder(String orderId) {
        initializeOrders();
        Order order = getOrderById(orderId);
        if (order != null && !order.getStatus().equals("DELIVERED") && !order.getStatus().equals("CANCELLED")) {
            // Restore stock
            for (Order.OrderItem item : order.getItems()) {
                int currentStock = StockManager.getStock(item.getProductId());
                StockManager.updateStock(item.getProductId(), currentStock + item.getQuantity());
            }
            
            order.setStatus("CANCELLED");
            saveOrdersToFile();
            System.out.println("[OrderManager] Order " + orderId + " cancelled");
            return true;
        }
        return false;
    }

    /**
     * Get order count for a customer
     */
    public static int getOrderCountForCustomer(String customerId) {
        return getCustomerOrders(customerId).size();
    }

    /**
     * File persistence
     */
    private static void saveOrdersToFile() {
        try {
            Files.createDirectories(ORDERS_FILE.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(ORDERS_FILE.toFile()))) {
                oos.writeObject(new ArrayList<>(allOrders));
            }
            System.out.println("[OrderManager] Orders persisted to: " + ORDERS_FILE);
        } catch (IOException e) {
            System.err.println("[OrderManager] Error saving orders: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadOrdersFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(ORDERS_FILE.toFile()))) {
            List<Order> loaded = (List<Order>) ois.readObject();
            allOrders.clear();
            allOrders.addAll(loaded);
            System.out.println("[OrderManager] Loaded " + allOrders.size() + " orders from file");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[OrderManager] Error loading orders: " + e.getMessage());
        }
    }
}

