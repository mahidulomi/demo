package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.List;

public class CustomerHomeController {

    @FXML private Label greetingLabel;
    @FXML private Button cartButton;
    @FXML private TextField searchField;
    @FXML private FlowPane productsFlowPane;
    
    private String currentCategory = "All";

    @FXML
    public void initialize() {
        StockManager.initializeStock();
        String user = Session.getCurrentUser();
        greetingLabel.setText("Hello, " + (user != null ? user : "Customer") + "!");
        updateCartCount();
        loadProducts("All", "");
        
        // Add real-time search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            loadProducts(currentCategory, newVal);
        });
    }

    private void updateCartCount() {
        cartButton.setText("🛒 Cart (" + Cart.getItemCount() + ")");
    }

    private void loadProducts(String category, String searchQuery) {
        productsFlowPane.getChildren().clear();
        List<StockItem> items = StockManager.getAllStockItems();

        for (StockItem item : items) {
            String cat = item.getCategory();
            String name = item.getProductName() != null ? item.getProductName() : "";

            if (!"All".equalsIgnoreCase(category)) {
                if (cat == null || !cat.equalsIgnoreCase(category)) {
                    continue;
                }
            }

            if (searchQuery != null && !searchQuery.isBlank()) {
                if (!name.toLowerCase().contains(searchQuery.toLowerCase())) {
                    continue;
                }
            }

            productsFlowPane.getChildren().add(createProductCard(item));
        }

        if (productsFlowPane.getChildren().isEmpty()) {
            Label noResult = new Label("No products found.");
            noResult.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            productsFlowPane.getChildren().add(noResult);
        }
    }

    private VBox createProductCard(StockItem item) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #444; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        card.setPrefWidth(240);

        // Image with white background
        javafx.scene.layout.VBox imageBox = new javafx.scene.layout.VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setStyle("-fx-background-color: white; -fx-padding: 8; -fx-background-radius: 6;");
        imageBox.setPrefHeight(130);
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(110);
        imageView.setFitWidth(110);
        imageView.setPreserveRatio(true);

        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    imageView.setImage(new Image(is));
                } else {
                    String altPath = path.substring(1);
                    is = getClass().getResourceAsStream(altPath);
                    if (is != null) {
                        imageView.setImage(new Image(is));
                    }
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Error loading image: " + e.getMessage());
            }
        }
        
        imageBox.getChildren().add(imageView);

        // Product Name
        Label nameLbl = new Label(item.getProductName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(210);

        // Price
        Label priceLbl = new Label(String.format("Tk.%.2f", item.getPrice()));
        priceLbl.setStyle("-fx-text-fill: #11998e; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Stock Info
        Label stockLbl = new Label("Stock: " + item.getQuantity());
        stockLbl.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic; -fx-font-size: 11px;");

        // Store quantity in an AtomicInteger wrapper
        java.util.concurrent.atomic.AtomicInteger quantity = new java.util.concurrent.atomic.AtomicInteger(1);
        java.util.concurrent.atomic.AtomicBoolean isUpdatingField = new java.util.concurrent.atomic.AtomicBoolean(false);

        // Quantity TextField
        javafx.scene.control.TextField qtyField = new javafx.scene.control.TextField();
        qtyField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: #444; -fx-border-width: 1; -fx-border-radius: 4; -fx-font-size: 12px; -fx-alignment: center; -fx-padding: 5;");
        qtyField.setPrefWidth(50);
        qtyField.setEditable(true);
        qtyField.setText("1");
        
        // Update quantity when user types
        qtyField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingField.get()) {
                return;  // Ignore programmatic updates
            }
            if (newVal == null || newVal.isEmpty()) {
                return;
            }
            try {
                int val = Integer.parseInt(newVal);
                if (val > 0 && val <= 100) {
                    quantity.set(val);
                } else {
                    isUpdatingField.set(true);
                    qtyField.setText(String.valueOf(quantity.get()));
                    isUpdatingField.set(false);
                }
            } catch (NumberFormatException e) {
                isUpdatingField.set(true);
                qtyField.setText(String.valueOf(quantity.get()));
                isUpdatingField.set(false);
            }
        });

        // Up/Down buttons
        Button downBtn = new Button("▼");
        downBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 10; -fx-font-size: 12px; -fx-background-radius: 4;");
        downBtn.setPrefWidth(40);

        Button upBtn = new Button("▲");
        upBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 10; -fx-font-size: 12px; -fx-background-radius: 4;");
        upBtn.setPrefWidth(40);

        downBtn.setOnAction(e -> {
            int current = quantity.get();
            if (current > 1) {
                quantity.set(current - 1);
                isUpdatingField.set(true);
                qtyField.setText(String.valueOf(quantity.get()));
                isUpdatingField.set(false);
            }
        });

        upBtn.setOnAction(e -> {
            int current = quantity.get();
            if (current < 100) {
                quantity.set(current + 1);
                isUpdatingField.set(true);
                qtyField.setText(String.valueOf(quantity.get()));
                isUpdatingField.set(false);
            }
        });

        // Control box with quantity and buttons
        javafx.scene.layout.HBox controlBox = new javafx.scene.layout.HBox(6);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPrefHeight(40);
        controlBox.getChildren().addAll(qtyField, downBtn, upBtn);

        // Add button
        Button addToCartBtn = new Button("Add");
        addToCartBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-font-weight: bold; -fx-padding: 8 20; -fx-font-size: 13px;");
        addToCartBtn.setPrefWidth(170);
        addToCartBtn.setOnAction(e -> {
            int qty = quantity.get();
            Cart.addItem(item.getProductId(), item.getProductName(), item.getCategory(), item.getPrice(), qty, item.getImagePath(), 0);
            updateCartCount();
            quantity.set(1);
            qtyField.setText("1");
            addToCartBtn.setText("✓ Added!");
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                    javafx.application.Platform.runLater(() -> addToCartBtn.setText("Add"));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        card.getChildren().addAll(imageBox, nameLbl, priceLbl, stockLbl, controlBox, addToCartBtn);
        return card;
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText();
        loadProducts("All", query);
    }

    @FXML
    private void onCategoryAll() { 
        currentCategory = "All";
        searchField.clear();
        loadProducts("All", ""); 
    }

    @FXML
    private void onCategoryElectronics() { 
        currentCategory = "Electronics";
        searchField.clear();
        loadProducts("Electronics", ""); 
    }

    @FXML
    private void onCategoryBeauty() { 
        currentCategory = "Beauty";
        searchField.clear();
        loadProducts("Beauty", ""); 
    }

    @FXML
    private void onCategoryFashion() { 
        currentCategory = "Fashion";
        searchField.clear();
        loadProducts("Fashion", ""); 
    }

    @FXML
    private void onCategoryHomeAndLiving() { 
        currentCategory = "Home and Living";
        searchField.clear();
        loadProducts("Home and Living", ""); 
    }

    @FXML
    private void onViewCart() {
        Session.goToCartFrom(greetingLabel, "customer-home-view.fxml");
    }

    @FXML
    private void onLogout() {
        Session.logoutToLogin(greetingLabel);
    }
}
