package com.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Controller for Stock Management page - shows all products with their stock quantities
 */
public class StockController {

    // ── Stock table ──────────────────────────────────────────────────────────
    @FXML private Label totalProductsLabel;
    @FXML private Label inStockLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private VBox  stockTableBody;
    @FXML private Label statusLabel;
    @FXML private Button btnAll;
    @FXML private Button btnElectronics;
    @FXML private Button btnBeauty;
    @FXML private Button btnLowStock;
    @FXML private Button btnOutOfStock;

    // ── Network panel ────────────────────────────────────────────────────────
    @FXML private VBox      networkPanel;
    @FXML private VBox      networkPanelBody;
    @FXML private Label     networkToggleArrow;
    @FXML private Label     networkStatusDot;
    @FXML private Label     networkModeLabel;
    @FXML private Label     myIpLabel;
    @FXML private TextField serverPortField;
    @FXML private TextField clientIpField;
    @FXML private TextField clientPortField;
    @FXML private Button    startServerBtn;
    @FXML private Button    connectBtn;
    @FXML private Label     networkErrorLabel;

    // Stock data
    private List<StockItem> allStockItems = new ArrayList<>();
    private String currentFilter = "All";

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @FXML
    private void initialize() {
        loadStockData();
        updateSummary();
        displayStockTable("All");
        refreshNetworkStatus();

        // Show local IP in server row
        if (myIpLabel != null) myIpLabel.setText("Your IP: " + getLocalIp());

        // Server status callback: update dot + client count when clients connect/disconnect
        NetworkManager.getInstance().setServerStatusCallback(() ->
                Platform.runLater(this::refreshNetworkStatus));

        // Register as network listener — refresh table when stock, products, or sales sync changes
        NetworkManager.getInstance().setCurrentListener(new StockUpdateListener() {
            @Override
            public void onStockUpdated(String productId, int newQuantity) {
                refreshStockView();
            }

            @Override
            public void onProductCatalogChanged() {
                refreshStockView();
            }
        });
    }

    private void refreshStockView() {
        loadStockData();
        updateSummary();
        displayStockTable(currentFilter);
    }

    // ── Network Panel ────────────────────────────────────────────────────────

    @FXML
    private void onToggleNetworkPanel() {
        boolean open = networkPanelBody.isVisible();
        networkPanelBody.setVisible(!open);
        networkPanelBody.setManaged(!open);
        networkToggleArrow.setText(open ? "▶" : "▼");
    }

    @FXML
    private void onStartServer() {
        clearNetError();
        String portTxt = serverPortField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portTxt);
        } catch (NumberFormatException e) {
            showNetError("❌ Invalid port number.");
            return;
        }
        startServerBtn.setDisable(true);
        startServerBtn.setText("Starting…");
        int finalPort = port;
        new Thread(() -> {
            try {
                NetworkManager.getInstance().shutdown(); // stop any existing
                NetworkManager.getInstance().startAsServer(finalPort);
                Platform.runLater(() -> {
                    refreshNetworkStatus();
                    startServerBtn.setText("✅ Running");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showNetError("❌ " + ex.getMessage());
                    startServerBtn.setDisable(false);
                    startServerBtn.setText("▶ Start Server");
                });
            }
        }, "StockServer-Start").start();
    }

    @FXML
    private void onConnect() {
        clearNetError();
        String ip  = clientIpField.getText().trim();
        String portTxt = clientPortField.getText().trim();
        if (ip.isEmpty()) { showNetError("❌ Enter server IP address."); return; }
        int port;
        try {
            port = Integer.parseInt(portTxt);
        } catch (NumberFormatException e) {
            showNetError("❌ Invalid port.");
            return;
        }
        connectBtn.setDisable(true);
        connectBtn.setText("Connecting…");
        int finalPort = port;
        new Thread(() -> {
            try {
                NetworkManager.getInstance().shutdown(); // stop any existing
                NetworkManager.getInstance().connectToServer(ip, finalPort);
                Platform.runLater(() -> {
                    refreshNetworkStatus();
                    connectBtn.setText("✅ Connected");
                    // Immediately refresh stock table with synced data
                    loadStockData();
                    updateSummary();
                    displayStockTable(currentFilter);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showNetError("❌ " + ex.getMessage());
                    connectBtn.setDisable(false);
                    connectBtn.setText("🔗 Connect");
                });
            }
        }, "StockClient-Connect").start();
    }

    @FXML
    private void onSetOffline() {
        NetworkManager.getInstance().shutdown();
        connectBtn.setDisable(false);
        connectBtn.setText("🔗 Connect");
        startServerBtn.setDisable(false);
        startServerBtn.setText("▶ Start Server");
        refreshNetworkStatus();
        clearNetError();
    }

    private void refreshNetworkStatus() {
        NetworkManager.Mode mode = NetworkManager.getInstance().getMode();
        switch (mode) {
            case SERVER -> {
                networkStatusDot.setText("🟢");
                int clients = NetworkManager.getInstance().getClientCount();
                networkModeLabel.setText("SERVER  |  " + clients + " client(s) connected");
            }
            case CLIENT -> {
                networkStatusDot.setText("🔵");
                networkModeLabel.setText("CLIENT  |  Connected");
            }
            default -> {
                networkStatusDot.setText("⚫");
                networkModeLabel.setText("OFFLINE");
            }
        }
    }

    private void showNetError(String msg) {
        if (networkErrorLabel != null) networkErrorLabel.setText(msg);
    }

    private void clearNetError() {
        if (networkErrorLabel != null) networkErrorLabel.setText("");
    }

    private String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    // ── Stock Table ───────────────────────────────────────────────────────────

    private void loadStockData() {
        allStockItems = StockManager.getAllStockItems();
    }

    private void updateSummary() {
        int total = allStockItems.size();
        int inStock = 0, lowStock = 0, outOfStock = 0;
        for (StockItem item : allStockItems) {
            if      (item.getQuantity() <= 0)  outOfStock++;
            else if (item.getQuantity() <= 10) lowStock++;
            else                               inStock++;
        }
        totalProductsLabel.setText(String.valueOf(total));
        inStockLabel.setText(String.valueOf(inStock));
        lowStockLabel.setText(String.valueOf(lowStock));
        outOfStockLabel.setText(String.valueOf(outOfStock));
    }

    private void displayStockTable(String filter) {
        stockTableBody.getChildren().clear();
        int rowNum = 1;
        for (StockItem item : allStockItems) {
            boolean show = switch (filter) {
                case "Electronics" -> item.getCategory().equals("Electronics");
                case "Beauty"      -> item.getCategory().equals("Beauty");
                case "LowStock"    -> item.getQuantity() > 0 && item.getQuantity() <= 10;
                case "OutOfStock"  -> item.getQuantity() <= 0;
                default            -> true;
            };
            if (show) {
                stockTableBody.getChildren().add(createStockRow(rowNum, item));
                rowNum++;
            }
        }
        statusLabel.setText("📦 Showing " + (rowNum - 1) + " products");
    }

    private HBox createStockRow(int rowNum, StockItem item) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(rowNum % 2 == 0 ? "stock-row-even" : "stock-row-odd");

        Label numLabel = new Label(String.valueOf(rowNum));
        numLabel.setPrefWidth(60);
        numLabel.getStyleClass().add("stock-cell");

        Label nameLabel = new Label(item.getProductName());
        nameLabel.setPrefWidth(300);
        nameLabel.getStyleClass().add("stock-cell");
        nameLabel.setWrapText(true);

        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setPrefWidth(150);
        categoryLabel.getStyleClass().add("stock-cell");

        HBox qtyBox = new HBox(5);
        qtyBox.setAlignment(Pos.CENTER_LEFT);
        qtyBox.setPrefWidth(150);
        qtyBox.getStyleClass().add("stock-cell");

        Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
        qtyLabel.getStyleClass().add("stock-qty-label");
        if      (item.getQuantity() <= 0)  qtyLabel.getStyleClass().add("stock-qty-out");
        else if (item.getQuantity() <= 10) qtyLabel.getStyleClass().add("stock-qty-low");
        else                               qtyLabel.getStyleClass().add("stock-qty-ok");
        qtyBox.getChildren().add(qtyLabel);

        Label statusLbl = new Label(getStockStatus(item.getQuantity()));
        statusLbl.setPrefWidth(150);
        statusLbl.getStyleClass().add("stock-cell");
        statusLbl.getStyleClass().add(getStatusStyleClass(item.getQuantity()));

        row.getChildren().addAll(numLabel, nameLabel, categoryLabel, qtyBox, statusLbl);
        return row;
    }

    private String getStockStatus(int qty) {
        if (qty <= 0)  return "❌ Out of Stock";
        if (qty <= 10) return "⚠️ Low Stock";
        return "✅ In Stock";
    }

    private String getStatusStyleClass(int qty) {
        if (qty <= 0)  return "status-out";
        if (qty <= 10) return "status-low";
        return "status-ok";
    }

    // ── Filter buttons ────────────────────────────────────────────────────────

    @FXML private void onFilterAll()        { currentFilter = "All";        updateFilterButtons("All");        displayStockTable("All"); }
    @FXML private void onFilterElectronics(){ currentFilter = "Electronics"; updateFilterButtons("Electronics"); displayStockTable("Electronics"); }
    @FXML private void onFilterBeauty()     { currentFilter = "Beauty";      updateFilterButtons("Beauty");      displayStockTable("Beauty"); }
    @FXML private void onFilterLowStock()   { currentFilter = "LowStock";    updateFilterButtons("LowStock");    displayStockTable("LowStock"); }
    @FXML private void onFilterOutOfStock() { currentFilter = "OutOfStock";  updateFilterButtons("OutOfStock");  displayStockTable("OutOfStock"); }

    private void updateFilterButtons(String activeFilter) {
        btnAll.getStyleClass().removeAll("stock-filter-active");
        btnElectronics.getStyleClass().removeAll("stock-filter-active");
        btnBeauty.getStyleClass().removeAll("stock-filter-active");
        btnLowStock.getStyleClass().removeAll("stock-filter-active");
        btnOutOfStock.getStyleClass().removeAll("stock-filter-active");

        Button active = switch (activeFilter) {
            case "Electronics" -> btnElectronics;
            case "Beauty"      -> btnBeauty;
            case "LowStock"    -> btnLowStock;
            case "OutOfStock"  -> btnOutOfStock;
            default            -> btnAll;
        };
        if (!active.getStyleClass().contains("stock-filter-active")) {
            active.getStyleClass().add("stock-filter-active");
        }
    }

    @FXML
    private void onBackToHome() {
        Session.goToHome(statusLabel);
    }
}
