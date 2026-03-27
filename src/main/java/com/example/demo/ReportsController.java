package com.example.demo;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController implements StockUpdateListener {

    @FXML private TableView<SaleRecord> salesTable;
    @FXML private TableColumn<SaleRecord, String> colDate;
    @FXML private TableColumn<SaleRecord, String> colBillId;
    @FXML private TableColumn<SaleRecord, Integer> colQty;
    @FXML private TableColumn<SaleRecord, Double> colAmount;
    @FXML private TableColumn<SaleRecord, String> colSoldBy;

    @FXML private Label totalSalesLabel;
    @FXML private Label grandTotalLabel;
    @FXML private Label statusLabel;
    
    // Slide-up Details Panel
    @FXML private javafx.scene.layout.VBox billDetailsPanel;
    @FXML private Label detailBillId;
    @FXML private Label detailDate;
    @FXML private Label detailSoldBy;
    @FXML private Label detailCustomerName;
    @FXML private Label detailCustomerPhone;
    @FXML private Label detailCustomerEmail;
    @FXML private Label detailCustomerAddress;
    @FXML private javafx.scene.layout.VBox detailItemsContainer;
    @FXML private Label detailTotalAmount;

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
        
        // Listen for new sales via network or local updates
        NetworkManager.getInstance().setCurrentListener(this);
        
        // Listen for file changes from other instances
        SalesManager.addExternalChangeListener(() -> {
             javafx.application.Platform.runLater(this::loadData);
        });
        
        // Listen for row selection
        salesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showBillDetails(newVal);
            }
        });
        
        // Close panel when clicking outside? (Optional, maybe later)
    }

    private void setupColumns() {
        colDate.setCellValueFactory(cell -> {
            String ts = cell.getValue().getTimestamp();
            try {
                // Try to parse standard LocalDateTime.toString() format
                LocalDateTime dt = LocalDateTime.parse(ts);
                return new SimpleStringProperty(dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } catch (Exception e) {
                return new SimpleStringProperty(ts);
            }
        });
        
        colBillId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSaleId()));
        colQty.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getTotalQuantity()).asObject());
        colAmount.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getTotalAmount()).asObject());
        colSoldBy.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoldBy()));
        
        // Format amount
        colAmount.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("Tk.%.2f", amount));
                }
            }
        });
    }

    private void showBillDetails(SaleRecord record) {
        if (billDetailsPanel == null) return;
        
        detailBillId.setText(record.getSaleId());
        detailDate.setText(record.getTimestamp().replace("T", " "));
        detailSoldBy.setText("Sold By: " + record.getSoldBy());
        
        // Display customer details
        if (detailCustomerName != null) {
            detailCustomerName.setText("Name: " + record.getCustomerName());
        }
        if (detailCustomerPhone != null) {
            detailCustomerPhone.setText("Phone: " + record.getCustomerPhone());
        }
        if (detailCustomerEmail != null) {
            String email = record.getCustomerEmail() != null && !record.getCustomerEmail().isEmpty()
                ? record.getCustomerEmail()
                : "N/A";
            detailCustomerEmail.setText("Email: " + email);
        }
        if (detailCustomerAddress != null) {
            String address = record.getCustomerAddress() != null && !record.getCustomerAddress().isEmpty() 
                ? record.getCustomerAddress() 
                : "N/A";
            detailCustomerAddress.setText("Address: " + address);
        }
        
        detailTotalAmount.setText(String.format("Tk.%.2f", record.getTotalAmount()));
        
        parseAndDisplayItems(record.getItemsJson());

        billDetailsPanel.setVisible(true);
        // Ensure starting position is off-screen (bottom)
        billDetailsPanel.setTranslateY(400); 
        
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300), billDetailsPanel);
        tt.setToY(0);
        tt.play();
    }
    
    @FXML
    private void closeDetailsPanel() {
        if (billDetailsPanel == null) return;
        
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(300), billDetailsPanel);
        tt.setToY(400); 
        tt.setOnFinished(e -> {
            billDetailsPanel.setVisible(false);
            salesTable.getSelectionModel().clearSelection();
        });
        tt.play();
    }

    private void parseAndDisplayItems(String json) {
        if (detailItemsContainer == null) return;
        detailItemsContainer.getChildren().clear();
        
        if (json == null || json.length() < 5) {
            Label placeholder = new Label("No detailed item data available.");
            placeholder.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            detailItemsContainer.getChildren().add(placeholder);
            return;
        }

        String patternStr = "\\{\"name\":\"((?:[^\"\\\\]|\\\\.)*)\",\"price\":([0-9.]+),\"quantity\":([0-9]+),\"category\":\"((?:[^\"\\\\]|\\\\.)*)\"\\}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternStr);
        java.util.regex.Matcher matcher = pattern.matcher(json);

        boolean found = false;
        while (matcher.find()) {
            found = true;
            String nameRaw = matcher.group(1);
            String name = nameRaw.replace("\\\"", "\"").replace("\\\\", "\\"); 
            
            double price = Double.parseDouble(matcher.group(2));
            int quantity = Integer.parseInt(matcher.group(3));

            addItemRow(name, quantity, price);
        }
        
        if (!found) {
             Label placeholder = new Label("Could not parse item details.");
             placeholder.setStyle("-fx-text-fill: #999;");
             detailItemsContainer.getChildren().add(placeholder);
        }
    }

    private void addItemRow(String name, int qty, double price) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        row.setStyle("-fx-border-color: #334155; -fx-border-width: 0 0 1 0; -fx-padding: 10 0 10 0; -fx-background-color: #000000;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;");
        javafx.scene.layout.HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        
        Label qtyLabel = new Label("x" + qty);
        qtyLabel.setMinWidth(45);
        qtyLabel.setAlignment(javafx.geometry.Pos.CENTER);
        qtyLabel.setStyle("-fx-background-color: #334155; -fx-background-radius: 4; -fx-padding: 4 8; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label priceLabel = new Label(String.format("Tk.%.2f", price * qty));
        priceLabel.setMinWidth(100);
        priceLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        priceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 16px;");

        row.getChildren().addAll(nameLabel, qtyLabel, priceLabel);
        detailItemsContainer.getChildren().add(row);
    }

    private void loadData() {
        List<SaleRecord> sales = SalesManager.getAllSales();
        // Sort by date descending (newest first)
        sales.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        salesTable.setItems(FXCollections.observableArrayList(sales));
        
        totalSalesLabel.setText("(Total: " + sales.size() + ")");
        
        double grandTotal = sales.stream().mapToDouble(SaleRecord::getTotalAmount).sum();
        grandTotalLabel.setText(String.format("Tk.%.2f", grandTotal));
        
        statusLabel.setText("Loaded " + sales.size() + " records.");
    }

    @Override
    public void onStockUpdated(String productId, int newQuantity) {
        // Reports might not care about stock, but we must implement the method
    }

    @Override
    public void onSalesDataChanged() {
        javafx.application.Platform.runLater(this::loadData);
    }
    
    @FXML
    private void onBackClick() {
        try {
            // Unregister listener when leaving
            NetworkManager.getInstance().clearCurrentListener(this);
            // Navigate to Home instead of closing
            Session.goToHome(salesTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClearHistory() {
        SalesManager.clearAllSales();
        SalesTracker.clearAllSales(); // Also clear in-memory dashboard stats
        HomeController.refreshDashboard(); // Update dashboard UI if open
        loadData();
        statusLabel.setText("✅ History cleared successfully.");
    }
}
