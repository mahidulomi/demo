package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Stock Management page - shows all products with their stock quantities
 */
public class StockController {

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label inStockLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label outOfStockLabel;

    @FXML
    private VBox stockTableBody;

    @FXML
    private Label statusLabel;

    @FXML
    private Button btnAll;

    @FXML
    private Button btnElectronics;

    @FXML
    private Button btnBeauty;

    @FXML
    private Button btnLowStock;

    @FXML
    private Button btnOutOfStock;

    // Store all stock data
    private List<StockItem> allStockItems = new ArrayList<>();

    @FXML
    private void initialize() {
        loadStockData();
        updateSummary();
        displayStockTable("All");
    }

    /**
     * Load stock data from StockManager
     */
    private void loadStockData() {
        allStockItems = StockManager.getAllStockItems();
    }

    /**
     * Update summary cards with counts
     */
    private void updateSummary() {
        int total = allStockItems.size();
        int inStock = 0;
        int lowStock = 0;
        int outOfStock = 0;

        for (StockItem item : allStockItems) {
            if (item.getQuantity() <= 0) {
                outOfStock++;
            } else if (item.getQuantity() <= 10) {
                lowStock++;
            } else {
                inStock++;
            }
        }

        totalProductsLabel.setText(String.valueOf(total));
        inStockLabel.setText(String.valueOf(inStock));
        lowStockLabel.setText(String.valueOf(lowStock));
        outOfStockLabel.setText(String.valueOf(outOfStock));
    }

    /**
     * Display stock table with filter
     */
    private void displayStockTable(String filter) {
        stockTableBody.getChildren().clear();

        int rowNum = 1;
        for (StockItem item : allStockItems) {
            boolean show = false;

            switch (filter) {
                case "All":
                    show = true;
                    break;
                case "Electronics":
                    show = item.getCategory().equals("Electronics");
                    break;
                case "Beauty":
                    show = item.getCategory().equals("Beauty");
                    break;
                case "LowStock":
                    show = item.getQuantity() > 0 && item.getQuantity() <= 10;
                    break;
                case "OutOfStock":
                    show = item.getQuantity() <= 0;
                    break;
            }

            if (show) {
                HBox row = createStockRow(rowNum, item);
                stockTableBody.getChildren().add(row);
                rowNum++;
            }
        }

        statusLabel.setText("📦 Showing " + (rowNum - 1) + " products");
    }

    /**
     * Create a single stock row
     */
    private HBox createStockRow(int rowNum, StockItem item) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(rowNum % 2 == 0 ? "stock-row-even" : "stock-row-odd");

        // Row number
        Label numLabel = new Label(String.valueOf(rowNum));
        numLabel.setPrefWidth(60);
        numLabel.getStyleClass().add("stock-cell");

        // Product name
        Label nameLabel = new Label(item.getProductName());
        nameLabel.setPrefWidth(300);
        nameLabel.getStyleClass().add("stock-cell");
        nameLabel.setWrapText(true);

        // Category
        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setPrefWidth(150);
        categoryLabel.getStyleClass().add("stock-cell");

        // Stock quantity with editable field
        HBox qtyBox = new HBox(5);
        qtyBox.setAlignment(Pos.CENTER_LEFT);
        qtyBox.setPrefWidth(150);
        qtyBox.getStyleClass().add("stock-cell");

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.getStyleClass().add("stock-qty-label");
        if (item.getQuantity() <= 0) {
            qtyLabel.getStyleClass().add("stock-qty-out");
        } else if (item.getQuantity() <= 10) {
            qtyLabel.getStyleClass().add("stock-qty-low");
        } else {
            qtyLabel.getStyleClass().add("stock-qty-ok");
        }
        qtyBox.getChildren().add(qtyLabel);

        // Status
        Label statusLbl = new Label(getStockStatus(item.getQuantity()));
        statusLbl.setPrefWidth(150);
        statusLbl.getStyleClass().add("stock-cell");
        statusLbl.getStyleClass().add(getStatusStyleClass(item.getQuantity()));

        row.getChildren().addAll(numLabel, nameLabel, categoryLabel, qtyBox, statusLbl);
        return row;
    }

    private String getStockStatus(int qty) {
        if (qty <= 0) return "❌ Out of Stock";
        if (qty <= 10) return "⚠️ Low Stock";
        return "✅ In Stock";
    }

    private String getStatusStyleClass(int qty) {
        if (qty <= 0) return "status-out";
        if (qty <= 10) return "status-low";
        return "status-ok";
    }

    private String currentFilter = "All";

    private String getCurrentFilter() {
        return currentFilter;
    }

    // ==================== FILTER METHODS ====================

    @FXML
    private void onFilterAll() {
        currentFilter = "All";
        updateFilterButtons("All");
        displayStockTable("All");
    }

    @FXML
    private void onFilterElectronics() {
        currentFilter = "Electronics";
        updateFilterButtons("Electronics");
        displayStockTable("Electronics");
    }

    @FXML
    private void onFilterBeauty() {
        currentFilter = "Beauty";
        updateFilterButtons("Beauty");
        displayStockTable("Beauty");
    }

    @FXML
    private void onFilterLowStock() {
        currentFilter = "LowStock";
        updateFilterButtons("LowStock");
        displayStockTable("LowStock");
    }

    @FXML
    private void onFilterOutOfStock() {
        currentFilter = "OutOfStock";
        updateFilterButtons("OutOfStock");
        displayStockTable("OutOfStock");
    }

    private void updateFilterButtons(String activeFilter) {
        btnAll.getStyleClass().removeAll("stock-filter-active");
        btnElectronics.getStyleClass().removeAll("stock-filter-active");
        btnBeauty.getStyleClass().removeAll("stock-filter-active");
        btnLowStock.getStyleClass().removeAll("stock-filter-active");
        btnOutOfStock.getStyleClass().removeAll("stock-filter-active");

        Button activeButton = switch (activeFilter) {
            case "Electronics" -> btnElectronics;
            case "Beauty" -> btnBeauty;
            case "LowStock" -> btnLowStock;
            case "OutOfStock" -> btnOutOfStock;
            default -> btnAll;
        };

        if (!activeButton.getStyleClass().contains("stock-filter-active")) {
            activeButton.getStyleClass().add("stock-filter-active");
        }
    }

    @FXML
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }
}

