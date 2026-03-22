package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class HomeController {

    @FXML
    private Label userLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button productsBtn;

    @FXML
    private Label todaysSalesLabel;

    @FXML
    private Label totalProductsLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label totalCustomersLabel;

    @FXML
    private AreaChart<String, Number> salesChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private static HomeController currentInstance;

    @FXML
    private void initialize() {
        currentInstance = this;
        String user = Session.getCurrentUser();
        userLabel.setText("Welcome, " + (user == null ? "Admin" : user));
        statusLabel.setText("");
        updateDashboardStats();
        setupSalesChart();

        // Listen for external sales file changes (e.g. from other instances)
        SalesManager.addExternalChangeListener(() -> {
            System.out.println("[HomeController] Detected external sales update. Refreshing stats...");
            SalesTracker.reloadFromSalesManager();
            javafx.application.Platform.runLater(this::updateDashboardStats);
        });
    }

    /**
     * Refresh dashboard from other controllers (e.g., after purchase)
     */
    public static void refreshDashboard() {
        if (currentInstance != null) {
            currentInstance.updateDashboardStats();
        }
    }

    @FXML
    private void onNavHome() {
        statusLabel.setText("You are on the Home Dashboard");
        updateDashboardStats();
    }

    /**
     * Update all dashboard stat cards with real data
     */
    private void updateDashboardStats() {
        // Update Today's Sales
        java.util.List<SalesTracker.SaleRecord> todaysSales = SalesTracker.getAllSales();
        double totalSalesAmount = todaysSales.stream().mapToDouble(s -> s.totalAmount).sum();
        
        if (todaysSalesLabel != null) {
            todaysSalesLabel.setText(String.format("Tk.%.2f", totalSalesAmount));
        }

        // Update Total Product Count (Unique items/SKUs)
        int totalProductCount = StockManager.getAllStockItems().size();
        
        if (totalProductsLabel != null) {
            totalProductsLabel.setText(String.format("%d Products", totalProductCount));
        }

        // Update Low Stock Alert (Stock < 5)
        long lowStockCount = StockManager.getAllStockItems().stream()
                .filter(item -> item.getQuantity() < 5)
                .count();
        
        if (lowStockLabel != null) {
            lowStockLabel.setText(lowStockCount + " Items");
        }

        // Update Total Sales Transactions
        if (totalCustomersLabel != null) {
            totalCustomersLabel.setText(String.valueOf(todaysSales.size()));
        }
        
        // Refresh chart
        setupSalesChart();
    }

    /**
     * Shows a context menu with 4 product categories
     */
    @FXML
    private void onProductsClick() {
        if (productsBtn != null) {
            ContextMenu menu = new ContextMenu();
            
            MenuItem beautyItem = new MenuItem("💄 Beauty");
            beautyItem.setOnAction(e -> {
                statusLabel.setText("Opening Beauty products...");
                ProductListController.setCategoryToShow("Beauty");
                Session.goToProductList(statusLabel);
            });

            MenuItem electronicsItem = new MenuItem("📱 Electronics");
            electronicsItem.setOnAction(e -> {
                statusLabel.setText("Opening Electronics products...");
                ProductListController.setCategoryToShow("Electronics");
                Session.goToProductList(statusLabel);
            });

            MenuItem homeLivingItem = new MenuItem("🏠 Home & Living");
            homeLivingItem.setOnAction(e -> {
                statusLabel.setText("Opening Home & Living products...");
                ProductListController.setCategoryToShow("Home and Living");
                Session.goToProductList(statusLabel);
            });

            MenuItem fashionItem = new MenuItem("👗 Fashion");
            fashionItem.setOnAction(e -> {
                statusLabel.setText("Opening Fashion products...");
                ProductListController.setCategoryToShow("Fashion");
                Session.goToProductList(statusLabel);
            });

            menu.getItems().addAll(beautyItem, electronicsItem, homeLivingItem, fashionItem);
            
            Bounds bounds = productsBtn.localToScreen(productsBtn.getBoundsInLocal());
            menu.show(productsBtn, bounds.getCenterX(), bounds.getCenterY() + 30);
        }
    }

    /**
     * Shows Sales section - products that have been purchased
     */
    @FXML
    private void onSalesClick() {
        try {
            System.out.println("🔄 Opening Sales page...");
            statusLabel.setText("Opening Sales page...");
            Session.goToSales(statusLabel);
            statusLabel.setText("✅ Sales page opened successfully");
        } catch (Exception e) {
            String errorMsg = "Could not open Sales page: " + e.getMessage();
            statusLabel.setText(errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * Shows Reports section - Professional bill reports with IDs and dates
     */
    @FXML
    private void onReportsClick() {
        try {
            System.out.println("🔄 Opening Reports page...");
            statusLabel.setText("Opening Reports page...");
            Session.goToReports(statusLabel);
            statusLabel.setText("✅ Reports page opened successfully");
        } catch (Exception e) {
            String errorMsg = "Could not open Reports page: " + e.getMessage();
            statusLabel.setText(errorMsg);
            e.printStackTrace();
        }
    }

    @FXML
    private void onRestockClick() {
        try {
            System.out.println("🔄 Opening Restock page...");
            statusLabel.setText("Opening Restock page...");
            Session.goToRestock(statusLabel);
            statusLabel.setText("✅ Restock page opened successfully");
        } catch (Exception e) {
             String errorMsg = "Could not open Restock page: " + e.getMessage();
            statusLabel.setText(errorMsg);
            e.printStackTrace();
        }
    }

    @FXML
    private void onCustomersClick() {
        try {
            System.out.println("🔄 Opening Customers page...");
            statusLabel.setText("Opening Customers page...");
            Session.goToCustomers(statusLabel);
            statusLabel.setText("✅ Customers page opened successfully");
        } catch (Exception e) {
            String errorMsg = "Could not open Customers page: " + e.getMessage();
            statusLabel.setText(errorMsg);
            e.printStackTrace();
        }
    }

    @FXML
    private void onLogout() {
        Session.logoutToLogin(userLabel);
    }

    @FXML
    private void onProfileClick() {
        String user = Session.getCurrentUser();
        statusLabel.setText("Profile: " + (user == null ? "Guest" : user) + " (Profile page coming soon)");
    }

    @FXML
    private void openFashion() {
        Session.goToFashion(statusLabel);
    }

    @FXML
    private void openElectronics() {
        Session.goToElectronics(statusLabel);
    }

    @FXML
    private void openHomeLiving() {
        try {
            System.out.println("Opening Home & Living...");
            statusLabel.setText("Opening Home & Living...");
            Session.goToHomeLiving(statusLabel);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void openBeauty() {
        Session.goToBeauty(statusLabel);
    }

    @FXML
    private void openCart() {
        Session.goToCartFrom(statusLabel, "home-view.fxml");
    }

    @FXML
    private void openFreeDelivery() {
        Session.goToFreeDelivery(statusLabel);
    }

    @FXML
    private void openNewArrivals() {
        Session.goToNewArrivals(statusLabel);
    }

    @FXML
    private void openStock() {
        Session.goToStock(statusLabel);
    }


    private static String safe(String text) {
        return text == null ? "" : text.trim();
    }

    /**
     * Setup the sales chart with data
     */
    private void setupSalesChart() {
        if (salesChart == null) return;
        
        salesChart.getData().clear();
        
        // Use TreeMap to keep dates sorted chronologically
        Map<LocalDate, Double> salesByDate = new TreeMap<>();
        
        // Aggregate sales by day
        for (SalesTracker.SaleRecord record : SalesTracker.getAllSales()) {
            if (record.saleTime != null) {
                LocalDate date = record.saleTime.toLocalDate();
                salesByDate.put(date, salesByDate.getOrDefault(date, 0.0) + record.totalAmount);
            }
        }
        
        // Ensure at least today is present for aesthetics if empty
        if (salesByDate.isEmpty()) {
            salesByDate.put(LocalDate.now(), 0.0);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales Volume");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (Map.Entry<LocalDate, Double> entry : salesByDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().format(formatter), entry.getValue()));
        }

        salesChart.getData().add(series);
    }
}

