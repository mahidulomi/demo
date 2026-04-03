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
                javafx.application.Platform.runLater(() -> {
                    loadProducts(currentCategory, searchField.getText());
                });
            }

            @Override
            public void onProductCatalogChanged() {
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

        System.out.println("[DEBUG] Loading products: category=" + category + ", searchQuery=" + searchQuery + ", totalItems=" + items.size());

        // Sort by product name alphabetically
        items.sort((a, b) -> {
            String nameA = a.getProductName() != null ? a.getProductName() : "";
            String nameB = b.getProductName() != null ? b.getProductName() : "";
            return nameA.compareToIgnoreCase(nameB);
        });

        int displayedCount = 0;
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

            System.out.println("[DEBUG] Displaying product: " + name + " with image path: " + item.getImagePath());
            productsFlowPane.getChildren().add(createProductCard(item));
            displayedCount++;
        }

        System.out.println("[DEBUG] Total products displayed: " + displayedCount);

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

        // Image with white background
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
        System.out.println("[DEBUG-CARD] Creating card for: " + item.getProductName() + " (ID: " + item.getProductId() + ") with original image path: " + path);

        // If path is empty, try to get default path for this product
        if (path == null || path.isEmpty()) {
            path = StockManager.getDefaultImagePathForProduct(item.getProductName(), item.getCategory());
            System.out.println("[DEBUG-CARD] Got default path for " + item.getProductName() + ": " + path);
        }
        loadImageForProduct(imageView, path, item.getProductName());

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

        // Up/Down buttons
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

        // Add to Cart button
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

        // Bottom control box
        javafx.scene.layout.HBox controlBox = new javafx.scene.layout.HBox(8);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPrefHeight(40);
        controlBox.getChildren().addAll(addToCartBtn, downBtn, countLbl, upBtn);

        card.getChildren().addAll(imageBox, nameLbl, priceLbl, controlBox);

        // Hover effect
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

    // ─────────────────────────────────────────────────────────────────────────
    // Image loading — supports classpath resources AND absolute OS file paths
    // ─────────────────────────────────────────────────────────────────────────

    private void loadImageForProduct(ImageView imageView, String path, String productName) {
        if (path == null || path.isEmpty()) {
            System.err.println("[WARNING] No image path for product: " + productName);
            return;
        }

        try {
            // ── Step 1: file:// or file:/ URI ──────────────────────────────
            if (path.startsWith("file://") || path.startsWith("file:/")) {
                Image img = new Image(path);
                if (!img.isError()) {
                    imageView.setImage(img);
                    System.out.println("[INFO] ✓ Loaded '" + productName + "' via file URI: " + path);
                    return;
                }
            }

            // ── Step 2: Absolute OS path (C:\... or /home/...) ─────────────
            // This handles images added via FileChooser after a product
            // was deleted and re-added — the stored path is an absolute
            // filesystem path, not a classpath resource.
            java.io.File externalFile = new java.io.File(path);
            if (externalFile.isAbsolute() && externalFile.exists()) {
                Image img = new Image(externalFile.toURI().toString());
                if (!img.isError()) {
                    imageView.setImage(img);
                    System.out.println("[INFO] ✓ Loaded '" + productName + "' as absolute path: " + path);
                    return;
                }
            }

            // ── Step 3: Classpath resource (built-in images) ───────────────
            String resourcePath = path.startsWith("/") ? path : "/" + path;

            // Try getResource()
            var url = getClass().getResource(resourcePath);
            if (url != null) {
                Image img = new Image(url.toExternalForm());
                if (!img.isError()) {
                    imageView.setImage(img);
                    System.out.println("[INFO] ✓ Loaded '" + productName + "' via getResource(): " + resourcePath);
                    return;
                }
            }

            // Try getResourceAsStream()
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is != null) {
                Image img = new Image(is);
                if (!img.isError()) {
                    imageView.setImage(img);
                    System.out.println("[INFO] ✓ Loaded '" + productName + "' via getResourceAsStream(): " + resourcePath);
                    return;
                }
            }

            // ── Step 4: ClassLoader without leading slash ──────────────────
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            String noSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            is = cl.getResourceAsStream(noSlash);
            if (is != null) {
                Image img = new Image(is);
                if (!img.isError()) {
                    imageView.setImage(img);
                    System.out.println("[INFO] ✓ Loaded '" + productName + "' via ClassLoader: " + noSlash);
                    return;
                }
            }

            // ── Step 5: Path variations ────────────────────────────────────
            String[] pathVariations = {
                    resourcePath,
                    resourcePath.toLowerCase(),
                    "/com/example/demo" + resourcePath,
                    "com/example/demo" + resourcePath
            };

            for (String variation : pathVariations) {
                try {
                    is = getClass().getResourceAsStream(variation);
                    if (is != null) {
                        Image img = new Image(is);
                        if (!img.isError()) {
                            imageView.setImage(img);
                            System.out.println("[INFO] ✓ Loaded '" + productName + "' via path variation: " + variation);
                            return;
                        }
                    }
                } catch (Exception ignored) {}
            }

            System.err.println("[WARNING] Could not load image for '" + productName + "' at: " + path);

        } catch (Exception e) {
            System.err.println("[ERROR] Exception loading image for '" + productName + "': " + e.getMessage());
            e.printStackTrace();
        }
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