package com.example.demo;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Label totalSalesLabel;

    @FXML
    private AreaChart<String, Number> salesChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private AnchorPane addProductOverlay;

    @FXML
    private TextField productIdField;

    @FXML
    private TextField productNameField;

    @FXML
    private ComboBox<String> productCategoryCombo;

    @FXML
    private TextField productStockField;

    @FXML
    private TextField productPriceField;

    @FXML
    private ImageView productPreviewImage;

    @FXML
    private Label selectedImageLabel;

    private String selectedImagePath = "";

    private static HomeController currentInstance;

    @FXML
    private void initialize() {
        currentInstance = this;
        String user = Session.getCurrentUser();
        userLabel.setText("Welcome, " + (user == null ? "Admin" : user));
        statusLabel.setText("");
        updateDashboardStats();
        setupSalesChart();

        if (productCategoryCombo != null) {
            productCategoryCombo.getItems().setAll("Beauty", "Electronics", "Fashion", "Home and Living");
            productCategoryCombo.getSelectionModel().selectFirst();
        }
        clearProductForm();

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
        // Fetch all sales records
        java.util.List<SalesTracker.SaleRecord> allSales = SalesTracker.getAllSales();
        
        // Calculate Today's Sales
        LocalDate today = LocalDate.now();
        double todaysAmount = allSales.stream()
                .filter(s -> s.saleTime != null && s.saleTime.toLocalDate().isEqual(today))
                .mapToDouble(s -> s.totalAmount)
                .sum();
        
        if (todaysSalesLabel != null) {
            todaysSalesLabel.setText(String.format("Tk.%.2f", todaysAmount));
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

        // Update Total Sales (Revenue)
        double totalSalesAmount = allSales.stream().mapToDouble(s -> s.totalAmount).sum();

        if (totalSalesLabel != null) {
            totalSalesLabel.setText(String.format("Tk.%.2f", totalSalesAmount));
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

    @FXML
    private void onOpenAddProductModal() {
        clearProductForm();
        if (productIdField != null) {
            productIdField.setText(generateNextProductId());
        }
        if (addProductOverlay != null) {
            addProductOverlay.toFront();
            addProductOverlay.setVisible(true);
        }
    }

    @FXML
    private void onCloseAddProductModal() {
        if (addProductOverlay != null) {
            addProductOverlay.setVisible(false);
        }
    }

    @FXML
    private void onProductOverlayBackgroundClick() {
        onCloseAddProductModal();
    }

    @FXML
    private void onProductPanelClick(MouseEvent event) {
        if (event != null) event.consume();
    }

    @FXML
    private void onImportProductImage() {
        if (productPreviewImage == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Product Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        Stage stage = productPreviewImage.getScene() == null ? null : (Stage) productPreviewImage.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        selectedImagePath = file.toURI().toString();
        productPreviewImage.setImage(new Image(selectedImagePath, true));
        if (selectedImageLabel != null) {
            selectedImageLabel.setText(file.getName());
        }
    }

    @FXML
    private void onAddProduct() {
        if (saveProduct(false)) {
            onCloseAddProductModal();
            clearProductForm();
            statusLabel.setText("Product added");
        }
    }

    @FXML
    private void onUpdateProduct() {
        if (saveProduct(true)) {
            statusLabel.setText("Product updated");
        }
    }

    @FXML
    private void onDeleteProduct() {
        String productId = safe(productIdField == null ? null : productIdField.getText());
        if (productId.isEmpty()) {
            showInfo("Missing Product ID", "Please enter Product ID to delete.");
            return;
        }

        boolean deleted = StockManager.removeStockItem(productId);
        if (!deleted) {
            showInfo("Not Found", "No product found for ID: " + productId);
            return;
        }

        updateDashboardStats();
        statusLabel.setText("Product deleted: " + productId);
        clearProductForm();
    }

    @FXML
    private void onClearProductForm() {
        clearProductForm();
    }

    private boolean saveProduct(boolean updateMode) {
        String productId = safe(productIdField == null ? null : productIdField.getText());
        if (productId.isEmpty()) {
            productId = generateNextProductId();
        }

        String productName = safe(productNameField == null ? null : productNameField.getText());
        String category = normalizeCategory(productCategoryCombo == null ? null : productCategoryCombo.getValue());

        if (productName.isEmpty()) {
            showInfo("Missing Product Name", "Please enter product name.");
            return false;
        }

        if (category.isEmpty()) {
            showInfo("Missing Category", "Please select a category.");
            return false;
        }

        int stock;
        double price;
        try {
            stock = Integer.parseInt(safe(productStockField == null ? null : productStockField.getText()));
            price = Double.parseDouble(safe(productPriceField == null ? null : productPriceField.getText()));
        } catch (NumberFormatException ex) {
            showInfo("Invalid Number", "Stock must be integer and price must be numeric.");
            return false;
        }

        if (stock < 0 || price < 0) {
            showInfo("Invalid Value", "Stock/Price cannot be negative.");
            return false;
        }

        StockItem existing = StockManager.getStockItem(productId);
        if (!updateMode && existing != null) {
            showInfo("Duplicate Product ID", "This Product ID already exists. Use Update or change ID.");
            return false;
        }

        String imagePath = selectedImagePath;
        if (imagePath.isEmpty() && existing != null) {
            imagePath = existing.getImagePath();
        }

        StockManager.upsertStockItem(new StockItem(productId, productName, category, category, stock, price, imagePath));
        updateDashboardStats();

        if (productIdField != null) {
            productIdField.setText(productId);
        }
        return true;
    }

    private String normalizeCategory(String category) {
        String c = safe(category);
        if (c.equalsIgnoreCase("Home & Living")) return "Home and Living";
        return c;
    }

    private String generateNextProductId() {
        Pattern pattern = Pattern.compile("PROD_(\\d+)");
        int max = 0;
        for (StockItem item : StockManager.getAllStockItems()) {
            Matcher matcher = pattern.matcher(safe(item.getProductId()));
            if (matcher.matches()) {
                int n = Integer.parseInt(matcher.group(1));
                if (n > max) max = n;
            }
        }
        return String.format("PROD_%03d", max + 1);
    }

    private void clearProductForm() {
        selectedImagePath = "";
        if (productNameField != null) productNameField.clear();
        if (productStockField != null) productStockField.clear();
        if (productPriceField != null) productPriceField.clear();
        if (productPreviewImage != null) productPreviewImage.setImage(null);
        if (selectedImageLabel != null) selectedImageLabel.setText("No image selected");
        if (productCategoryCombo != null && productCategoryCombo.getItems().size() > 0) {
            productCategoryCombo.getSelectionModel().selectFirst();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
