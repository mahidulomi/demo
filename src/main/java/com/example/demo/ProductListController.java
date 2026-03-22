package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductListController {

    private static String currentCategory = "All";

    public static void setCategoryToShow(String category) {
        currentCategory = category;
    }

    @FXML
    private Label categoryLabel;

    @FXML
    private VBox productsListContainer;

    @FXML
    private void initialize() {
        if (currentCategory == null || currentCategory.equals("All")) {
            categoryLabel.setText("All Products Inventory");
            loadAllProducts();
        } else {
            categoryLabel.setText(currentCategory + " Inventory");
            loadProductsByCategory(currentCategory);
        }
    }

    private void loadProductsByCategory(String category) {
        productsListContainer.getChildren().clear();

        List<StockItem> items = StockManager.getAllStockItems().stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(StockItem::getProductName))
                .collect(Collectors.toList());

        if (items.isEmpty()) {
             Label placeholder = new Label("No products found in " + category + ".");
             placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
             productsListContainer.getChildren().add(placeholder);
        } else {
             for (StockItem item : items) {
                 productsListContainer.getChildren().add(createProductRow(item));
             }
        }
    }

    private void loadAllProducts() {
        productsListContainer.getChildren().clear();

        // Get all items from StockManager
        List<StockItem> allItems = StockManager.getAllStockItems();

        // Group by Category
        Map<String, List<StockItem>> byCategory = allItems.stream()
                .collect(Collectors.groupingBy(StockItem::getCategory));

        // Display each category
        // Categories order preference: Electronics, Fashion, Beauty, Home & Living
        String[] preferredOrder = {"Electronics", "Fashion", "Beauty", "Home & Living"};

        for (String cat : preferredOrder) {
            if (byCategory.containsKey(cat)) {
                addCategorySection(cat, byCategory.get(cat));
                byCategory.remove(cat);
            }
        }

        // Add any remaining categories
        byCategory.forEach(this::addCategorySection);

        if (allItems.isEmpty()) {
            Label placeholder = new Label("No products found in inventory.");
            placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            productsListContainer.getChildren().add(placeholder);
        }
    }

    private void addCategorySection(String categoryName, List<StockItem> items) {
        // Section Header
        Label header = new Label(categoryName);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 10 0 5 0;");
        productsListContainer.getChildren().add(header);

        // Sort items by name
        items.sort(Comparator.comparing(StockItem::getProductName));

        for (StockItem item : items) {
            productsListContainer.getChildren().add(createProductRow(item));
        }

        // Separator after each section
        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));
        productsListContainer.getChildren().add(sep);
    }

    private HBox createProductRow(StockItem item) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(15);
        row.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        row.setPrefHeight(60);

        // Product Icon/Image Placeholder
        Label icon = new Label(getCategoryIcon(item.getCategory()));
        icon.setStyle("-fx-font-size: 24px;");
        icon.setMinWidth(40);
        icon.setAlignment(Pos.CENTER);

        // Name and ID
        VBox nameBox = new VBox(2);
        Label name = new Label(item.getProductName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label id = new Label("ID: " + item.getProductId());
        id.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        
        nameBox.getChildren().addAll(name, id);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Price
        Label price = new Label(String.format("৳%,.2f", item.getPrice()));
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        price.setMinWidth(100);
        price.setAlignment(Pos.CENTER_RIGHT);

        // Stock
        Label stock = new Label("Stock: " + item.getQuantity());
        stock.setMinWidth(100);
        stock.setAlignment(Pos.CENTER_RIGHT);
        if (item.getQuantity() < 5) {
            stock.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            stock.setStyle("-fx-text-fill: green;");
        }

        row.getChildren().addAll(icon, nameBox, spacer, price, stock);
        return row;
    }

    private String getCategoryIcon(String category) {
        return switch (category) {
            case "Electronics" -> "📱";
            case "Fashion" -> "👗";
            case "Beauty" -> "💄";
            case "Home & Living" -> "🏠";
            default -> "📦";
        };
    }

    @FXML
    private void onBackClick() {
        Session.goToHome(productsListContainer);
    }
}
