package com.example.demo;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class RestockController {

    @FXML private ComboBox<String> itemComboBox;
    @FXML private TextField quantityField;
    @FXML private DatePicker restockDatePicker;
    @FXML private TextField purchasePriceField;
    @FXML private TextArea notesArea;
    @FXML private Label statusLabel;

    @FXML private TableView<RestockRecord> historyTable;
    @FXML private TableColumn<RestockRecord, String> colItemName;
    @FXML private TableColumn<RestockRecord, Integer> colQty;
    @FXML private TableColumn<RestockRecord, Double> colPrice;
    @FXML private TableColumn<RestockRecord, String> colDate;
    @FXML private TableColumn<RestockRecord, String> colAddedBy;

    // Stats
    @FXML private Label lowStockCountLabel;
    @FXML private Label outOfStockCountLabel;
    @FXML private Label totalItemsCountLabel; 
    @FXML private Label recentRestocksCountLabel;

    @FXML
    public void initialize() {
        refreshItemList();

        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colAddedBy.setCellValueFactory(new PropertyValueFactory<>("addedBy"));

        loadHistoryTable();
        updateStats();

        restockDatePicker.setValue(LocalDate.now());
    }

    private void refreshItemList() {
        List<String> items = StockManager.getAllStockItems().stream()
                .map(StockItem::getProductName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        itemComboBox.setItems(FXCollections.observableArrayList(items));
    }

    private void loadHistoryTable() {
        List<RestockRecord> history = RestockManager.getHistory();
        // Sort by newer first
        history.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        historyTable.setItems(FXCollections.observableArrayList(history));
    }
    
    private void updateStats() {
        long low = StockManager.getAllStockItems().stream().filter(i -> i.getQuantity() < 5 && i.getQuantity() > 0).count();
        long out = StockManager.getAllStockItems().stream().filter(i -> i.getQuantity() == 0).count();
        long total = StockManager.getAllStockItems().size();
        
        if (lowStockCountLabel != null) lowStockCountLabel.setText(String.valueOf(low));
        if (outOfStockCountLabel != null) outOfStockCountLabel.setText(String.valueOf(out));
        if (totalItemsCountLabel != null) totalItemsCountLabel.setText(String.valueOf(total));
        if (recentRestocksCountLabel != null) recentRestocksCountLabel.setText(String.valueOf(RestockManager.getHistory().size()));
    }

    @FXML
    private void onAddRestock() {
        String itemName = itemComboBox.getValue();
        if (itemName == null || itemName.isBlank()) {
            statusLabel.setText("Please select an item.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        String qtyStr = quantityField.getText();
        if (qtyStr == null || !qtyStr.matches("\\d+")) {
            statusLabel.setText("Invalid quantity.");
             statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        int qty = Integer.parseInt(qtyStr);
        if (qty <= 0) {
            statusLabel.setText("Quantity must be > 0.");
             statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String priceStr = purchasePriceField.getText();
        double price = 0.0;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
             statusLabel.setText("Invalid price.");
              statusLabel.setStyle("-fx-text-fill: red;");
             return;
        }

        LocalDate date = restockDatePicker.getValue();
        if (date == null) {
            statusLabel.setText("Select a date.");
             statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String notes = notesArea.getText();
        String user = Session.getCurrentUser() != null ? Session.getCurrentUser() : "Admin";

        // Create record
        RestockRecord record = new RestockRecord(
            date.toString(), itemName, qty, price, user, notes
        );
        RestockManager.addRecord(record);

        // Update StockManager
        StockItem stockItem = StockManager.getAllStockItems().stream()
            .filter(i -> i.getProductName().equals(itemName))
            .findFirst()
            .orElse(null);
            
        if (stockItem != null) {
            StockManager.updateStock(stockItem.getProductId(), stockItem.getQuantity() + qty);
            // Broadcast update to other networking clients
            NetworkManager.getInstance().broadcastStockUpdate(stockItem.getProductId(), stockItem.getQuantity() + qty);
        } else {
             statusLabel.setText("Error: Item not found in stock system.");
              statusLabel.setStyle("-fx-text-fill: red;");
             return;
        }

        statusLabel.setText("Restock added successfully!");
        statusLabel.setStyle("-fx-text-fill: green;");
        clearForm();
        loadHistoryTable();
        updateStats();
    }

    @FXML
    private void onCancel() {
        clearForm();
        statusLabel.setText("");
    }
    
    private void clearForm() {
        itemComboBox.getSelectionModel().clearSelection();
        quantityField.clear();
        purchasePriceField.clear();
        notesArea.clear();
        restockDatePicker.setValue(LocalDate.now());
    }
    
    @FXML
    private void onBackClick() {
        try {
             // Try loading home-view first, assuming standardized naming
             Parent root = FXMLLoader.load(getClass().getResource("home-view.fxml"));
             Stage stage = (Stage) itemComboBox.getScene().getWindow();
             stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Could not return to home.");
        }
    }
}
