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

    @FXML
    public void initialize() {
        String user = Session.getCurrentUser();
        greetingLabel.setText("Hello, " + (user != null ? user : "Customer") + "!");
        updateCartCount();
        loadProducts("All", "");
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
            productsFlowPane.getChildren().add(new Label("No products found."));
        }
    }

    private VBox createProductCard(StockItem item) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        card.setPrefWidth(200);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);

        String path = item.getImagePath();
        if (path != null && !path.isEmpty()) {
            try {
                if (!path.startsWith("/")) path = "/" + path;
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    imageView.setImage(new Image(is));
                } else {
                    System.err.println("[CustomerHome] Image not found: " + path);
                }
            } catch (Exception e) {
                System.err.println("[CustomerHome] Error loading image " + path + ": " + e.getMessage());
            }
        }

        Label nameLbl = new Label(item.getProductName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");

        Label priceLbl = new Label(String.format("৳ %.2f", item.getPrice()));
        priceLbl.setStyle("-fx-text-fill: #11998e; -fx-font-weight: bold;");

        Button buyBtn = new Button("Buy Now");
        buyBtn.setStyle("-fx-background-color: #11998e; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
        buyBtn.setOnAction(e -> {
            Cart.addItem(item.getProductId(), item.getProductName(), item.getCategory(), item.getPrice(), 1, item.getImagePath(), 0);
            updateCartCount();
        });

        card.getChildren().addAll(imageView, nameLbl, priceLbl, buyBtn);
        return card;
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText();
        loadProducts("All", query);
    }

    @FXML
    private void onCategoryAll() { loadProducts("All", ""); }

    @FXML
    private void onCategoryElectronics() { loadProducts("Electronics", ""); }

    @FXML
    private void onCategoryBeauty() { loadProducts("Beauty", ""); }

    @FXML
    private void onCategoryFashion() { loadProducts("Fashion", ""); }

    @FXML
    private void onViewCart() {
        Session.goToCartFrom(greetingLabel, "customer-home-view.fxml");
    }

    @FXML
    private void onLogout() {
        Session.logoutToLogin(greetingLabel);
    }
}
