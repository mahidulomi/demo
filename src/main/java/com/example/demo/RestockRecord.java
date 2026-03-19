package com.example.demo;

public class RestockRecord {
    private final String timestamp;
    private final String itemName;
    private final int quantity;
    private final double purchasePrice; // Per unit or total? The UI says "Purchase Price (per unit)"
    private final String addedBy;
    private final String notes;

    public RestockRecord(String timestamp, String itemName, int quantity, double purchasePrice, String addedBy, String notes) {
        this.timestamp = timestamp;
        this.itemName = itemName;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.addedBy = addedBy;
        this.notes = notes;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public String getAddedBy() {
        return addedBy;
    }
    
    public String getNotes() {
        return notes;
    }
}

