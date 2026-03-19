package com.example.demo;

import java.util.HashMap;
import java.util.Map;

/**
 * Product model for shop with detailed specifications.
 */
public class Product {
    // Basic Info
    private String id;
    private String name;
    private String category;
    private String subcategory;
    private double priceValue;
    private int discountPercent;
    private String price;
    private String originalPrice;
    private String discount;
    private String imagePath;
    private String description;
    private String[] colors;
    private int stockQuantity;

    // Dynamic specifications - flexible for any product type
    private Map<String, String> specifications;

    public Product() {
        this.specifications = new HashMap<>();
    }

    public Product(String id, String name, String category, String subcategory, double price, int discountPercent) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.priceValue = price;
        this.discountPercent = discountPercent;
        this.specifications = new HashMap<>();
    }

    public double getDiscountedPrice() {
        if (discountPercent <= 0) return priceValue;
        return priceValue * (1.0 - discountPercent / 100.0);
    }

    public boolean hasDiscount() {
        return discountPercent > 0;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // Add specification method
    public void addSpecification(String key, String value) {
        specifications.put(key, value);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public double getPriceValue() { return priceValue; }
    public void setPriceValue(double priceValue) { this.priceValue = priceValue; }

    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }

    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String[] getColors() { return colors; }
    public void setColors(String[] colors) { this.colors = colors; }

    public Map<String, String> getSpecifications() { return specifications; }
    public void setSpecifications(Map<String, String> specifications) { this.specifications = specifications; }
}
