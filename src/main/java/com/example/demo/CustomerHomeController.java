package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.List;

public class CustomerHomeController {

    @FXML private Label greetingLabel;
    @FXML private Button cartButton;
    @FXML private TextField searchField;
    @FXML private TilePane productsFlowPane;
    
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

        // Listen for external stock file changes (product deletions from another instance)
        StockManager.addExternalChangeListener(() -> {
            javafx.application.Platform.runLater(() -> {
                System.out.println("📦 Product catalog changed externally! Refreshing...");
                loadProducts(currentCategory, searchField.getText());
            });
        });

        // Register network listener for real-time product updates
        NetworkManager.getInstance().setCurrentListener(new StockUpdateListener() {
            @Override
            public void onStockUpdated(String productId, int newQuantity) {
                // When stock changes, reload products to reflect the change
                javafx.application.Platform.runLater(() -> {
                    loadProducts(currentCategory, searchField.getText());
                });
            }

            @Override
            public void onProductCatalogChanged() {
                // When product catalog changes (new product added/removed), reload all products
                javafx.application.Platform.runLater(() -> {
                    loadProducts(currentCategory, searchField.getText());
                });
            }
        });
    }


    private void updateCartCount() {
        cartButton.setText("🛒 Cart (" + Cart.getItemCount() + ")");
    }

    private void loadProducts(String category, String searchQuery) {
        productsFlowPane.getChildren().clear();
        List<StockItem> items = StockManager.getAllStockItems();

        // Sort by product name alphabetically
        items.sort((a, b) -> {
            String nameA = a.getProductName() != null ? a.getProductName() : "";
            String nameB = b.getProductName() != null ? b.getProductName() : "";
            return nameA.compareToIgnoreCase(nameB);
        });

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
            productsFlowPane.getChildren().add(noResult);
        }
    }

    private VBox createProductCard(StockItem item) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #000000; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 12;");

        // Image with white background - MUCH BIGGER
        javafx.scene.layout.VBox imageBox = new javafx.scene.layout.VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPrefHeight(220);
        imageBox.setMaxHeight(220);
        imageBox.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 8;");
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                // Try multiple path formats
                String[] pathFormats = {
                    path,                           // Original path
                    "/" + path,                     // With leading slash
                    path.startsWith("/") ? path.substring(1) : path  // Remove leading slash if exists
                };

                InputStream is = null;
                for (String tryPath : pathFormats) {
                    is = getClass().getResourceAsStream(tryPath);
                    if (is != null) {
                        imageView.setImage(new Image(is));
                        break;
                    }
                }

                if (is == null) {
                    System.err.println("[WARNING] Could not load image for product: " + item.getProductName() +
                                     " with path: " + path);
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Error loading image for " + item.getProductName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        imageBox.getChildren().add(imageView);

        // Product Name
        Label nameLbl = new Label(item.getProductName());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(270);
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setPrefHeight(30);

        // Price
        Label priceLbl = new Label(String.format("Tk.%.2f", item.getPrice()));
        priceLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        priceLbl.setAlignment(Pos.CENTER);
        priceLbl.setPrefHeight(20);

        // Count label
        Label countLbl = new Label("1");
        countLbl.setStyle("-fx-text-fill: white; -fx-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold;");
        countLbl.setPrefWidth(45);
        countLbl.setAlignment(Pos.CENTER);

        // Up/Down buttons - INCREASED FONT SIZE FOR VISIBILITY
        Button downBtn = new Button("▼");
        downBtn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
        downBtn.setPrefWidth(50);
        downBtn.setPrefHeight(40);
        downBtn.setCursor(Cursor.HAND);

        Button upBtn = new Button("▲");
        upBtn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 2 5; -fx-background-radius: 4; -fx-font-weight: bold;");
        upBtn.setPrefWidth(50);
        upBtn.setPrefHeight(40);
        upBtn.setCursor(Cursor.HAND);

        // Track count
        java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger(1);

        downBtn.setOnAction(e -> {
            int current = count.get();
            if (current > 1) {
                count.set(current - 1);
                countLbl.setText(String.valueOf(count.get()));
            }
        });

        upBtn.setOnAction(e -> {
            int current = count.get();
            if (current < 100) {
                count.set(current + 1);
                countLbl.setText(String.valueOf(count.get()));
            }
        });

        // Add button
        Button addToCartBtn = new Button("Add");
        addToCartBtn.setPrefWidth(160);
        addToCartBtn.setPrefHeight(38);
        addToCartBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");
        
        addToCartBtn.setOnAction(e -> {
            int qty = count.get();
            Cart.addItem(item.getProductId(), item.getProductName(), item.getCategory(), item.getPrice(), qty, item.getImagePath(), 0);
            updateCartCount();
            count.set(1);
            countLbl.setText("1");
            addToCartBtn.setText("✓ Added");
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                    javafx.application.Platform.runLater(() -> addToCartBtn.setText("Add"));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        // Bottom control box - Add button on left, Up/Down on right
        javafx.scene.layout.HBox controlBox = new javafx.scene.layout.HBox(8);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPrefHeight(40);
        controlBox.getChildren().addAll(addToCartBtn, downBtn, countLbl, upBtn);

        card.getChildren().addAll(imageBox, nameLbl, priceLbl, controlBox);
        
        // Add hover effect - ENHANCED
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #007bff; -fx-border-width: 3; -fx-border-radius: 10; -fx-padding: 12; -fx-spacing: 10; -fx-effect: dropshadow(gaussian, rgba(0,107,255,0.5), 8, 0, 0, 3);");
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
            priceLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
            addToCartBtn.setStyle("-fx-background-color: #0056cc; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,86,204,0.6), 6, 0, 0, 2);");
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #000000; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 12; -fx-spacing: 10;");
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
            priceLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
            addToCartBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");
        });
        
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
