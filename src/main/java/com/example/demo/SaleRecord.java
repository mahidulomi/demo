package com.example.demo;

/**
 * Immutable sale summary used for local persistence and network synchronisation.
 */
public class SaleRecord {
    private final String saleId;
    private final String timestamp;
    private final String soldBy;
    private final String sourceNode;
    private final int totalQuantity;
    private final double totalAmount;
    private final String lineItemsSummary;
    private final String itemsJson;
    private final String customerName;
    private final String customerPhone;
    private final String customerEmail;
    private final String customerAddress;

    public SaleRecord(String saleId, String timestamp, String soldBy, String sourceNode,
                      int totalQuantity, double totalAmount, String lineItemsSummary, String itemsJson,
                      String customerName, String customerPhone, String customerEmail, String customerAddress) {
        this.saleId = saleId;
        this.timestamp = timestamp;
        this.soldBy = soldBy;
        this.sourceNode = sourceNode;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.lineItemsSummary = lineItemsSummary;
        this.itemsJson = itemsJson;
        this.customerName = customerName == null ? "Walk-in Customer" : customerName;
        this.customerPhone = customerPhone == null ? "N/A" : customerPhone;
        this.customerEmail = customerEmail == null ? "" : customerEmail;
        this.customerAddress = customerAddress == null ? "" : customerAddress;
    }

    // Backward compatibility constructor without email
    public SaleRecord(String saleId, String timestamp, String soldBy, String sourceNode,
                      int totalQuantity, double totalAmount, String lineItemsSummary, String itemsJson,
                      String customerName, String customerPhone, String customerAddress) {
        this(saleId, timestamp, soldBy, sourceNode, totalQuantity, totalAmount, lineItemsSummary, itemsJson,
             customerName, customerPhone, "", customerAddress);
    }

    // Legacy constructor without customer details
    public SaleRecord(String saleId, String timestamp, String soldBy, String sourceNode,
                      int totalQuantity, double totalAmount, String lineItemsSummary, String itemsJson) {
        this(saleId, timestamp, soldBy, sourceNode, totalQuantity, totalAmount, lineItemsSummary, itemsJson, 
             "Walk-in Customer", "N/A", "", "");
    }

    public String getSaleId() {
        return saleId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSoldBy() {
        return soldBy;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getLineItemsSummary() {
        return lineItemsSummary;
    }
    
    public String getItemsJson() {
        return itemsJson;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }
}
