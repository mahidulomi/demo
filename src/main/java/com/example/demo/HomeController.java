package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.stage.Stage;

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

    private static HomeController currentInstance;

    @FXML
    private void initialize() {
        currentInstance = this;
        String user = Session.getCurrentUser();
        userLabel.setText("Welcome, " + (user == null ? "Admin" : user));
        statusLabel.setText("");
        updateDashboardStats();
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
        todaysSalesLabel.setText(String.format("Tk.%.2f", totalSalesAmount));

        // Update Total Products (estimated from sales)
        java.util.Set<String> uniqueProducts = new java.util.HashSet<>();
        for (SalesTracker.SaleRecord sale : todaysSales) {
            uniqueProducts.add(sale.productName);
        }
        int totalProducts = Math.max(uniqueProducts.size(), 0);
        totalProductsLabel.setText(String.valueOf(totalProducts));

        // Update Low Stock Alert (simplified - show 0 for now unless low stock detected)
        int lowStockCount = 0;
        // This can be enhanced if you have a way to check all products' stock
        lowStockLabel.setText(lowStockCount + " Items");

        // Update Total Customers (count transactions)
        totalCustomersLabel.setText(String.valueOf(Math.max(1, todaysSales.size())));
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
                Session.goToBeauty(statusLabel);
            });

            MenuItem electronicsItem = new MenuItem("📱 Electronics");
            electronicsItem.setOnAction(e -> {
                statusLabel.setText("Opening Electronics products...");
                Session.goToElectronics(statusLabel);
            });

            MenuItem homeLivingItem = new MenuItem("🏠 Home & Living");
            homeLivingItem.setOnAction(e -> {
                statusLabel.setText("Opening Home & Living products...");
                Session.goToHomeLiving(statusLabel);
            });

            MenuItem fashionItem = new MenuItem("👗 Fashion");
            fashionItem.setOnAction(e -> {
                statusLabel.setText("Opening Fashion products...");
                Session.goToFashion(statusLabel);
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
            
            java.net.URL salesViewUrl = HelloApplication.class.getResource("sales-view.fxml");
            if (salesViewUrl == null) {
                salesViewUrl = getClass().getResource("/com/example/demo/sales-view.fxml");
            }
            
            if (salesViewUrl == null) {
                String error = "CRITICAL: sales-view.fxml not found in resources!";
                statusLabel.setText(error);
                System.err.println("❌ " + error);
                
                // Construct a helpful tip
                System.err.println("  Expected path: /com/example/demo/sales-view.fxml");
                return;
            }

            System.out.println("✓ Found sales-view.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(salesViewUrl);
            javafx.scene.layout.AnchorPane page = loader.load();
            
            System.out.println("✓ Loaded FXML successfully");
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("💰 Sales - POS");
            stage.setScene(new javafx.scene.Scene(page, 1250, 750));
            stage.show();
            
            statusLabel.setText("✅ Sales page opened successfully");
            System.out.println("✅ Sales page opened successfully");
        } catch (Exception e) {
            String errorMsg = "Could not open Sales page: " + e.getMessage();
            statusLabel.setText(errorMsg);
            System.err.println("❌ " + errorMsg);
            System.err.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
        }
    }

    /**
     * Shows Reports section - Professional bill reports with IDs and dates
     */
    @FXML
    private void onReportsClick() {
        java.util.List<SalesTracker.SaleRecord> sales = SalesTracker.getAllSales();
        
        if (sales.isEmpty()) {
            statusLabel.setText("📈 Reports: No sales data available yet.");
            return;
        }
        
        StringBuilder report = new StringBuilder();
        report.append("╔═════════════════════════════════════════════════════════════╗\n");
        report.append("║         📊 SHOP MANAGEMENT REPORT SYSTEM                    ║\n");
        report.append("╚═════════════════════════════════════════════════════════════╝\n\n");
        
        // 1. SUMMARY STATISTICS
        report.append("┌─ 📈 SUMMARY STATISTICS ─────────────────────────────────────┐\n");
        double totalRevenue = SalesTracker.getTotalRevenue();
        int totalItems = sales.stream().mapToInt(s -> s.quantity).sum();
        
        report.append(String.format("│ Total Bills Generated:       %35d │\n", sales.size()));
        report.append(String.format("│ Total Items Sold:            %35d │\n", totalItems));
        report.append(String.format("│ Total Revenue:               Tk.%33.2f │\n", totalRevenue));
        report.append("└──────────────────────────────────────────────────────────────┘\n\n");
        
        // 2. BILLS ORGANIZED BY DATE (MAIN REPORT)
        report.append("┌─ 🧾 BILL REPORTS (Organized by Date) ────────────────────────┐\n\n");
        
        java.util.Map<String, java.util.List<SalesTracker.SaleRecord>> dateMap = new java.util.LinkedHashMap<>();
        for (SalesTracker.SaleRecord record : sales) {
            String dateKey = record.getFormattedDate().substring(0, 10); // YYYY-MM-DD
            dateMap.computeIfAbsent(dateKey, k -> new java.util.ArrayList<>()).add(record);
        }
        
        int billNumber = 1;
        for (String date : dateMap.keySet()) {
            java.util.List<SalesTracker.SaleRecord> dayRecords = dateMap.get(date);
            double dayTotal = dayRecords.stream().mapToDouble(r -> r.totalAmount).sum();
            int dayItems = dayRecords.stream().mapToInt(r -> r.quantity).sum();
            
            String billId = String.format("BILL-%05d", billNumber);
            
            report.append("╔════════════════════════════════════════════════════════════╗\n");
            report.append(String.format("║ %s  │  Date: %s                       ║\n", billId, date));
            report.append("╠════════════════════════════════════════════════════════════╣\n");
            report.append("║ ITEMS:                                                     ║\n");
            report.append("╠═══╦════════════════════════╦═════╦═════════╦════════════╣\n");
            report.append("║No.║ Product Name           ║ Qty ║ Price   ║ Amount     ║\n");
            report.append("╠═══╬════════════════════════╬═════╬═════════╬════════════╣\n");
            
            for (int i = 0; i < dayRecords.size(); i++) {
                SalesTracker.SaleRecord r = dayRecords.get(i);
                report.append(String.format("║%3d║ %-22s ║%5d║ Tk.%7.2f║ Tk.%10.2f║\n",
                        i + 1,
                        r.productName.substring(0, Math.min(22, r.productName.length())),
                        r.quantity,
                        r.price,
                        r.totalAmount));
            }
            
            report.append("╠═══╩════════════════════════╩═════╩═════════╩════════════╣\n");
            report.append(String.format("║ Bill Details:                                             ║\n"));
            report.append(String.format("║ • Bill ID: %s                                           ║\n", billId));
            report.append(String.format("║ • Date: %s                                       ║\n", date));
            report.append(String.format("║ • Total Items: %d                                        ║\n", dayItems));
            report.append(String.format("║ • Bill Amount: Tk.%.2f                                  ║\n", dayTotal));
            report.append(String.format("║ • Time Range: %s to %s                    ║\n",
                    dayRecords.get(0).getFormattedDate().substring(11, 19),
                    dayRecords.get(dayRecords.size() - 1).getFormattedDate().substring(11, 19)));
            report.append("╚════════════════════════════════════════════════════════════╝\n\n");
            
            billNumber++;
        }
        
        // 3. SALES BY CATEGORY
        report.append("┌─ 📁 SALES BY CATEGORY ──────────────────────────────────────┐\n");
        java.util.Map<String, Integer> categoryQty = new java.util.HashMap<>();
        java.util.Map<String, Double> categoryRev = new java.util.HashMap<>();
        for (SalesTracker.SaleRecord r : sales) {
            categoryQty.put(r.category, categoryQty.getOrDefault(r.category, 0) + r.quantity);
            categoryRev.put(r.category, categoryRev.getOrDefault(r.category, 0.0) + r.totalAmount);
        }
        for (String cat : categoryQty.keySet()) {
            report.append(String.format("│ %-20s │ %3d units │ Tk.%14.2f     │\n", 
                    cat, categoryQty.get(cat), categoryRev.get(cat)));
        }
        report.append("└──────────────────────────────────────────────────────────────┘\n\n");
        
        // 4. PRODUCT-WISE HISTORY
        report.append("┌─ 📦 PRODUCT-WISE HISTORY ───────────────────────────────────┐\n");
        java.util.Map<String, java.util.List<SalesTracker.SaleRecord>> productMap = new java.util.LinkedHashMap<>();
        for (SalesTracker.SaleRecord record : sales) {
            productMap.computeIfAbsent(record.productName, k -> new java.util.ArrayList<>()).add(record);
        }
        
        for (String product : productMap.keySet()) {
            java.util.List<SalesTracker.SaleRecord> records = productMap.get(product);
            int totalQty = records.stream().mapToInt(r -> r.quantity).sum();
            double totalRev = records.stream().mapToDouble(r -> r.totalAmount).sum();
            
            report.append(String.format("│ 📌 %s\n", product));
            report.append(String.format("│    Total: %d units | Tk.%.2f | Transactions: %d\n", 
                    totalQty, totalRev, records.size()));
            report.append("│\n");
        }
        report.append("└──────────────────────────────────────────────────────────────┘\n");
        
        statusLabel.setText(report.toString());
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
}






