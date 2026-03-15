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

    public SaleRecord(String saleId, String timestamp, String soldBy, String sourceNode,
                      int totalQuantity, double totalAmount, String lineItemsSummary) {
        this.saleId = saleId;
        this.timestamp = timestamp;
        this.soldBy = soldBy;
        this.sourceNode = sourceNode;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.lineItemsSummary = lineItemsSummary;
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
}

