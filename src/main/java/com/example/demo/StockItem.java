package com.example.demo;

/**
 * Represents a single stock item with product info and quantity
 */
public class StockItem {
    private String productId;
    private String productName;
    private String category;
    private String subCategory;
    private int quantity;
    private double price;
    private String imagePath;

    public StockItem(String productId, String productName, String category, int quantity, double price) {
        this(productId, productName, category, category, quantity, price, "");
    }

    public StockItem(String productId, String productName, String category, String subCategory,
                     int quantity, double price, String imagePath) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.subCategory = (subCategory == null || subCategory.isBlank()) ? category : subCategory;
        this.quantity = quantity;
        this.price = price;
        this.imagePath = imagePath == null ? "" : imagePath;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = (subCategory == null || subCategory.isBlank()) ? category : subCategory;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath == null ? "" : imagePath;
    }
}

