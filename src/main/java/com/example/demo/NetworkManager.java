package com.example.demo;

import javafx.application.Platform;

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
    private NetworkManager() {}

    // ── Internal state ────────────────────────────────────────────────────────
    private StockServer server;
    private StockClient client;

    public enum Mode { OFFLINE, SERVER, CLIENT }
    private Mode mode = Mode.OFFLINE;

    private StockUpdateListener currentListener;
    private Runnable serverStatusCallback;

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

    public void onNewProductFromNetwork(String data) {
        // Format: productId:name:category:qty:price
        String[] parts = data.split(":");
        if (parts.length >= 5) {
            try {
                StockManager.addStock(parts[0], parts[1], parts[2],
                        Integer.parseInt(parts[3]), Double.parseDouble(parts[4]));
                System.out.println("[NetworkManager] New product from network: " + parts[1]);
            } catch (NumberFormatException e) {
                System.err.println("[NetworkManager] Bad NEW_PRODUCT data: " + data);
            }
        }
    }

    /** Serialise full stock for sending to a newly connected client. */
    public String getFullStockData() {
        StringBuilder sb = new StringBuilder();
        for (StockItem item : StockManager.getAllStockItems()) {
            if (sb.length() > 0) sb.append("|");
            sb.append(item.getProductId()).append("=").append(item.getQuantity());
        }
        return sb.toString();
    }

    public void shutdown() {
        if (server != null) { server.stop(); server = null; }
        if (client != null) { client.disconnect(); client = null; }
        mode = Mode.OFFLINE;
    }
}
