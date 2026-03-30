package com.example.demo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order class to represent a customer order.
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String customerId;
    private String customerName;
    private LocalDateTime orderDate;
    private List<OrderItem> items;
    private double totalPrice;
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String shippingAddress;
    private String paymentMethod;
    private String phoneNumber;

    public Order() {
        this.orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.totalPrice = 0.0;
    }

    public Order(String customerId, String customerName, String phoneNumber, String shippingAddress) {
        this();
        this.customerId = customerId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.shippingAddress = shippingAddress;
    }

    public void addItem(String productId, String productName, String category, double price, int quantity, String imagePath) {
        items.add(new OrderItem(productId, productName, category, price, quantity, imagePath));
        recalculateTotal();
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
    }

    public void recalculateTotal() {
        totalPrice = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getFormattedOrderDate() {
        return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { 
        this.items = items; 
        recalculateTotal();
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", orderDate=" + orderDate +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * Inner class for order items
     */
    public static class OrderItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productId;
        private String productName;
        private String category;
        private double price;
        private int quantity;
        private String imagePath;

        public OrderItem(String productId, String productName, String category, double price, int quantity, String imagePath) {
            this.productId = productId;
            this.productName = productName;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.imagePath = imagePath;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getImagePath() { return imagePath; }
        public double getSubtotal() { return price * quantity; }
    }
}

