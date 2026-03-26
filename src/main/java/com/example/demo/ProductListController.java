package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

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
             placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #a0aec0;");
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
            placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #a0aec0;");
            productsListContainer.getChildren().add(placeholder);
        }
    }

    private void addCategorySection(String categoryName, List<StockItem> items) {
        // Section Header
        Label header = new Label(categoryName);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-padding: 10 0 5 0;");
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
        row.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 5, 0, 0, 2);");
        row.setPrefHeight(80); // Increased height for image and more details

        // Product Image or Icon
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(60, 60);

        String imagePath = item.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
                
                // Try to load image
                String url = getClass().getResource(imagePath) != null ? 
                             getClass().getResource(imagePath).toExternalForm() : null;
                
                if (url != null) {
                    imageView.setImage(new Image(url));
                    // Clip to rounded square
                    Rectangle clip = new Rectangle(60, 60);
                    clip.setArcWidth(10);
                    clip.setArcHeight(10);
                    imageView.setClip(clip);
                    imageContainer.getChildren().add(imageView);
                } else {
                    // Fallback to icon
                    Label icon = new Label(getCategoryIcon(item.getCategory()));
                    icon.setStyle("-fx-font-size: 30px; -fx-text-fill: #ffffff;");
                    imageContainer.getChildren().add(icon);
                }
            } catch (Exception e) {
                // Fallback
                Label icon = new Label(getCategoryIcon(item.getCategory()));
                icon.setStyle("-fx-font-size: 30px; -fx-text-fill: #ffffff;");
                imageContainer.getChildren().add(icon);
            }
        } else {
            Label icon = new Label(getCategoryIcon(item.getCategory()));
            icon.setStyle("-fx-font-size: 30px; -fx-text-fill: #ffffff;");
            imageContainer.getChildren().add(icon);
        }

        // Name, ID, Category
        VBox nameBox = new VBox(4);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        
        Label name = new Label(item.getProductName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #ffffff;");
        
        Label details = new Label("ID: " + item.getProductId() + "  |  " + item.getCategory() + 
                                  (item.getSubCategory() != null ? " (" + item.getSubCategory() + ")" : ""));
        details.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 12px;");
        
        nameBox.getChildren().addAll(name, details);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Price Section
        VBox priceBox = new VBox(2);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        priceBox.setMinWidth(120);

        Label priceLabel = new Label("Price");
        priceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        
        Label price = new Label(String.format("৳%,.2f", item.getPrice()));
        price.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-font-size: 16px;");
        
        priceBox.getChildren().addAll(priceLabel, price);

        // Stock Section
        VBox stockBox = new VBox(2);
        stockBox.setAlignment(Pos.CENTER_RIGHT);
        stockBox.setMinWidth(100);

        Label stockLabel = new Label("Stock");
        stockLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        Label stock = new Label(item.getQuantity() + " units");
        if (item.getQuantity() < 5) {
            stock.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
            stockLabel.setText("Low Stock");
            stockLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        } else {
            stock.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
        
        stockBox.getChildren().addAll(stockLabel, stock);

        // Add to row
        row.getChildren().addAll(imageContainer, nameBox, spacer, priceBox, stockBox);
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
