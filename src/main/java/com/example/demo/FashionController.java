package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FashionController {

    @FXML
    private Button btnAll;

    @FXML
    private Button btnBaby;

    @FXML
    private Button btnMale;

    @FXML
    private Button btnFemale;

    @FXML
    private GridPane productGrid;

    @FXML
    private Label statusLabel;

    private String currentFilter = "All";

    @FXML
    private void initialize() {
        loadProducts();
    }

    @FXML
    private void onFilterAll() {
        currentFilter = "All";
        updateFilterButtons();
        loadProducts();
    }

    @FXML
    private void onFilterBaby() {
        currentFilter = "Baby";
        updateFilterButtons();
        loadProducts();
    }

    @FXML
    private void onFilterMale() {
        currentFilter = "Male";
        updateFilterButtons();
        loadProducts();
    }

    @FXML
    private void onFilterFemale() {
        currentFilter = "Female";
        updateFilterButtons();
        loadProducts();
    }

    @FXML
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }

    private void updateFilterButtons() {
        btnAll.getStyleClass().removeAll("fashion-filter-active");
        btnBaby.getStyleClass().removeAll("fashion-filter-active");
        btnMale.getStyleClass().removeAll("fashion-filter-active");
        btnFemale.getStyleClass().removeAll("fashion-filter-active");

        Button active = switch (currentFilter) {
            case "Baby" -> btnBaby;
            case "Male" -> btnMale;
            case "Female" -> btnFemale;
            default -> btnAll;
        };

        if (!active.getStyleClass().contains("fashion-filter-active")) {
            active.getStyleClass().add("fashion-filter-active");
        }
    }

    private void loadProducts() {
        productGrid.getChildren().clear();

        List<Product> all = ProductCatalog.getFashionProducts();

        // Filter by subcategory
        List<Product> filtered = all.stream()
                .filter(p -> currentFilter.equals("All") || p.subcategory().equals(currentFilter))
                .collect(Collectors.toList());

        // Sort: discounted first, then by name
        filtered.sort(Comparator
                .comparing(Product::hasDiscount).reversed()
                .thenComparing(Product::name)
        );

        int col = 0, row = 0;
        for (Product p : filtered) {
            VBox card = buildProductCard(p);
            productGrid.add(card, col, row);

            col++;
            if (col >= 3) { // 3 products per row
                col = 0;
                row++;
            }
        }

        statusLabel.setText(filtered.size() + " product(s) found.");
    }

    private VBox buildProductCard(Product p) {
        VBox card = new VBox(10);
        card.setPrefSize(280, 320);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(12));
        card.getStyleClass().add("product-card");

        // Product Image Placeholder
        javafx.scene.layout.StackPane imagePlaceholder = new javafx.scene.layout.StackPane();
        imagePlaceholder.setPrefSize(250, 180);
        imagePlaceholder.setMaxSize(250, 180);
        imagePlaceholder.getStyleClass().add("product-image");

        // Image icon/text
        Label imageLabel = new Label("📷");
        imageLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #9ca3af;");
        imagePlaceholder.getChildren().add(imageLabel);

        // Discount badge overlay on image
        if (p.hasDiscount()) {
            Label discountBadge = new Label(p.discountPercent() + "% OFF");
            discountBadge.getStyleClass().add("product-discount-badge");
            javafx.scene.layout.StackPane.setAlignment(discountBadge, javafx.geometry.Pos.TOP_RIGHT);
            javafx.scene.layout.StackPane.setMargin(discountBadge, new Insets(8, 8, 0, 0));
            imagePlaceholder.getChildren().add(discountBadge);
        }

        // Product Name
        Label name = new Label(p.name());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setMaxWidth(250);

        // Price Label
        Label priceLabel = new Label();
        if (p.hasDiscount()) {
            double oldPrice = p.price();
            double newPrice = p.getDiscountedPrice();
            priceLabel.setText(String.format("BDT %.0f  (was %.0f)", newPrice, oldPrice));
            priceLabel.getStyleClass().add("product-price-discount");
        } else {
            priceLabel.setText(String.format("BDT %.0f", p.price()));
            priceLabel.getStyleClass().add("product-price");
        }

        // Add to Cart Button
        Button addBtn = new Button("Add to Cart");
        addBtn.getStyleClass().add("product-add-btn");
        addBtn.setPrefWidth(220);
        addBtn.setOnAction(e -> statusLabel.setText("Added: " + p.name() + " (demo)"));

        card.getChildren().addAll(imagePlaceholder, name, priceLabel, addBtn);

        return card;
    }
}
