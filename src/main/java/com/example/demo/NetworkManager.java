package com.example.demo;

import javafx.application.Platform;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Singleton NetworkManager — central hub for all networking.
 *
 * Modes:
 *  • SERVER  – this machine runs StockServer + the app.
 *  • CLIENT  – connects to a server machine.
 *  • OFFLINE – standalone, no network.
 */
public class NetworkManager {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static final NetworkManager INSTANCE = new NetworkManager();
    public static NetworkManager getInstance() { return INSTANCE; }
    private NetworkManager() {
        // Same-machine multi-instance mode: refresh active UI when stock file changes externally.
        StockManager.addExternalChangeListener(this::onLocalStockFileChanged);
    }

    // ── Internal state ────────────────────────────────────────────────────────
    private StockServer server;
    private StockClient client;

    public enum Mode { OFFLINE, SERVER, CLIENT }
    private Mode mode = Mode.OFFLINE;

    private StockUpdateListener currentListener;
    private Runnable serverStatusCallback;
    private Runnable userSyncCallback;

    // ── Public API ────────────────────────────────────────────────────────────

    public void startAsServer(int port) throws Exception {
        server = new StockServer(port, this);
        server.start();
        mode = Mode.SERVER;
        System.out.println("[NetworkManager] Running as SERVER on port " + port);
    }

    public void connectToServer(String host, int port) throws Exception {
        client = new StockClient(host, port, this);
        client.connect();
        mode = Mode.CLIENT;
        System.out.println("[NetworkManager] Running as CLIENT → " + host + ":" + port);
    }

    public void setOffline() {
        mode = Mode.OFFLINE;
        System.out.println("[NetworkManager] OFFLINE");
    }

    public Mode   getMode()        { return mode; }
    public boolean isActive()      { return mode != Mode.OFFLINE; }
    public boolean isServer()      { return mode == Mode.SERVER; }
    public int    getClientCount() { return server != null ? server.getClientCount() : 0; }

    // ── Listener wiring ───────────────────────────────────────────────────────

    public void setCurrentListener(StockUpdateListener listener) {
        this.currentListener = listener;
    }

    public void clearCurrentListener(StockUpdateListener listener) {
        if (this.currentListener == listener) this.currentListener = null;
    }

    public StockUpdateListener getCurrentListener() { return currentListener; }

    public void setServerStatusCallback(Runnable callback) {
        this.serverStatusCallback = callback;
    }

    public void setUserSyncCallback(Runnable callback) {
        this.userSyncCallback = callback;
    }

    // ── Called by controllers when THIS machine changes stock ─────────────────

    /**
     * Propagate a single stock change to all other machines.
     * Local StockManager must already be updated before calling this.
     */
    public void broadcastStockUpdate(String productId, int newQty) {
        if (mode == Mode.OFFLINE) return;
        if (mode == Mode.SERVER && server != null)
            server.broadcastToAllClients(productId, newQty);
        else if (mode == Mode.CLIENT && client != null)
            client.sendStockUpdate(productId, newQty);
    }

    public void broadcastNewProduct(StockItem item) {
        if (item == null || mode == Mode.OFFLINE) return;
        if (mode == Mode.SERVER && server != null)
            server.broadcastProductToAllClients(item);
        else if (mode == Mode.CLIENT && client != null)
            client.sendNewProduct(item);
    }

    public void broadcastSaleRecord(SaleRecord sale) {
        if (sale == null || mode == Mode.OFFLINE) return;
        if (mode == Mode.SERVER && server != null)
            server.broadcastSaleToAllClients(sale);
        else if (mode == Mode.CLIENT && client != null)
            client.sendSaleRecord(sale);
    }

    public void broadcastCustomer(Customer customer) {
        if (customer == null || mode == Mode.OFFLINE) return;
        if (mode == Mode.SERVER && server != null)
            server.broadcastCustomerToAllClients(customer);
        else if (mode == Mode.CLIENT && client != null)
            client.sendCustomerUpdate(customer);
    }

    public void broadcastUserUpdate(String username) {
        for (String u : UserStore.getAllSerializedUsers()) {
            if (u.startsWith(username.toLowerCase() + "=")) {
                String safePayload = NetworkCodec.encodeText(u);
                
                if (mode == Mode.SERVER && server != null)
                    server.broadcastUserToAllClients(safePayload);
                else if (mode == Mode.CLIENT && client != null)
                    client.sendUserUpdate(safePayload);
                return;
            }
        }
    }

    // ── Called by StockServer / StockClient when network messages arrive ──────

    /**
     * A single STOCK_UPDATE arrived from the network.
     * Updates StockManager (which persists to file) and refreshes current UI page.
     */
    public void onNetworkUpdate(String productId, int newQty) {
        int safeQty = Math.max(0, newQty);
        StockManager.updateStock(productId, safeQty);   // persists to file
        Platform.runLater(() -> {
            if (currentListener != null)
                currentListener.onStockUpdated(productId, safeQty);
        });
    }

    /**
     * Full STOCK_ALL snapshot from server received (on client connect).
     * Updates all quantities in one go (single file save) then refreshes UI.
     */
    public void onBatchNetworkUpdate(Map<String, Integer> updates) {
        StockManager.batchUpdateStock(updates);         // single file save
        Platform.runLater(() -> {
            if (currentListener != null) {
                for (Map.Entry<String, Integer> e : updates.entrySet())
                    currentListener.onStockUpdated(e.getKey(), e.getValue());
            }
        });
    }

    public void onFullProductSync(List<StockItem> products) {
        StockManager.replaceAllStock(products);
        Platform.runLater(() -> {
            if (currentListener != null) {
                currentListener.onProductCatalogChanged();
            }
        });
    }

    public void onFullSalesSync(List<SaleRecord> sales) {
        SalesManager.replaceAllSales(sales);
        Platform.runLater(() -> {
            if (currentListener != null) {
                currentListener.onSalesDataChanged();
            }
        });
    }

    public void onFullCustomerSync(List<Customer> customers) {
        CustomerManager.replaceAllCustomers(customers);
        Platform.runLater(() -> {
            if (currentListener != null) {
                // You might need a specific listener method for customers or just refresh UI
            }
        });
        System.out.println("[NetworkManager] Full customer list synced.");
    }
    
    public void onCustomerUpdateFromNetwork(String encodedCustomer) {
        try {
            Customer c = NetworkCodec.decodeCustomer(encodedCustomer);
            CustomerManager.saveCustomer(c);
            System.out.println("[NetworkManager] Customer synced: " + c.getName());
        } catch (Exception e) {
            System.err.println("[NetworkManager] Bad customer update: " + e.getMessage());
        }
    }

    public void onFullUserSync(List<String> users) {
        for (String u : users) {
             // Each user record is Base64 encoded text
             UserStore.importUserFromNetwork(NetworkCodec.decodeText(u));
        }
        System.out.println("[NetworkManager] Full user list synced.");
        Platform.runLater(() -> {
            if (userSyncCallback != null) userSyncCallback.run();
        });
    }

    public void onUserUpdateFromNetwork(String data) {
        UserStore.importUserFromNetwork(NetworkCodec.decodeText(data));
        System.out.println("[NetworkManager] User synced from network.");
        Platform.runLater(() -> {
            if (userSyncCallback != null) userSyncCallback.run();
        });
    }

    public void onClientDisconnected() {
        System.out.println("[NetworkManager] Disconnected from server.");
        mode = Mode.OFFLINE;
        Platform.runLater(() -> {
            if (serverStatusCallback != null) serverStatusCallback.run();
        });
    }

    public void onServerStatusChanged() {
        Platform.runLater(() -> {
            if (serverStatusCallback != null) serverStatusCallback.run();
        });
    }

    private void onLocalStockFileChanged() {
        Platform.runLater(() -> {
            if (currentListener != null) {
                currentListener.onProductCatalogChanged();
            }
        });
    }

    public void onNewProductFromNetwork(String data) {
        try {
            StockItem item = NetworkCodec.decodeStockItem(data);
            StockManager.upsertStockItem(item);
            System.out.println("[NetworkManager] Product sync from network: " + item.getProductName());
            Platform.runLater(() -> {
                if (currentListener != null) {
                    currentListener.onProductCatalogChanged();
                }
            });
        } catch (RuntimeException e) {
            System.err.println("[NetworkManager] Bad PRODUCT_UPSERT data: " + data);
        }
    }

    public void onSaleRecordFromNetwork(String data) {
        try {
            SaleRecord sale = NetworkCodec.decodeSaleRecord(data);
            SalesManager.recordSale(sale);
            // Sync with local dashboard Tracker
            SalesTracker.addNetworkSale(sale);
            
            System.out.println("[NetworkManager] Sale synced from network: " + sale.getSaleId());
            Platform.runLater(() -> {
                HomeController.refreshDashboard(); // Ensure dashboard updates
                if (currentListener != null) {
                    currentListener.onSalesDataChanged();
                }
            });
        } catch (RuntimeException e) {
            System.err.println("[NetworkManager] Bad SALE_RECORD data: " + data);
        }
    }

    /** Serialise full product data for sending to a newly connected client. */
    public String getFullProductData() {
        List<String> encodedProducts = new ArrayList<>();
        for (StockItem item : StockManager.getAllStockItems()) {
            encodedProducts.add(NetworkCodec.encodeStockItem(item));
        }
        return NetworkCodec.joinRecords(encodedProducts);
    }

    public String getFullSalesData() {
        return SalesManager.getSerializedSalesData();
    }

    public String getFullCustomerData() {
        return CustomerManager.getSerializedCustomerData();
    }

    public String getFullUserData() {
        List<String> encodedUsers = new ArrayList<>();
        for (String u : UserStore.getAllSerializedUsers()) {
            encodedUsers.add(NetworkCodec.encodeText(u));
        }
        return NetworkCodec.joinRecords(encodedUsers);
    }

    public SaleRecord buildSaleRecord(List<CartItem> items, int totalQty, double totalAmount) {
        return buildSaleRecord(items, totalQty, totalAmount, null, null, null, null);
    }

    public SaleRecord buildSaleRecord(List<CartItem> items, int totalQty, double totalAmount, 
                                     String customerName, String customerPhone, String customerAddress) {
        return buildSaleRecord(items, totalQty, totalAmount, customerName, customerPhone, null, customerAddress);
    }

    public SaleRecord buildSaleRecord(List<CartItem> items, int totalQty, double totalAmount, 
                                     String customerName, String customerPhone, String customerEmail, String customerAddress) {
        StringBuilder summary = new StringBuilder();
        StringBuilder jsonBuilder = new StringBuilder("[");
        boolean first = true;
        
        for (CartItem item : items) {
            // Summary
            if (summary.length() > 0) summary.append(" | ");
            summary.append(item.getProductName()).append(" x").append(item.getQuantity())
                    .append(" @ ").append(String.format("%.2f", item.getDiscountedUnitPrice()));
            
            // JSON
            if (!first) jsonBuilder.append(",");
            jsonBuilder.append(String.format("{\"name\":\"%s\",\"price\":%.2f,\"quantity\":%d,\"category\":\"%s\"}",
                    item.getProductName().replace("\"", "\\\""),
                    item.getDiscountedUnitPrice(),
                    item.getQuantity(),
                    safe(item.getCategory()).replace("\"", "\\\"")));
            first = false;
        }
        jsonBuilder.append("]");
        
        String user = Session.getCurrentUser();
        String soldBy = (user == null || user.isBlank()) ? "Guest" : user;
        String sourceNode = getMachineName() + "-" + mode.name();
        
        return new SaleRecord(
                "BILL-" + System.currentTimeMillis() % 1000000,
                java.time.LocalDateTime.now().toString(),
                soldBy,
                sourceNode,
                totalQty,
                totalAmount,
                summary.toString(),
                jsonBuilder.toString(),
                customerName,
                customerPhone,
                customerEmail,
                customerAddress
        );
    }
    
    private String safe(String s) { return s == null ? "" : s; }

    private String getMachineName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }

    public void shutdown() {
        if (server != null) { server.stop(); server = null; }
        if (client != null) { client.disconnect(); client = null; }
        mode = Mode.OFFLINE;
    }
}
