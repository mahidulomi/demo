package com.example.demo;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * TCP Client - connects to a StockServer on another machine.
 * Listens for STOCK_UPDATE broadcasts and notifies NetworkManager.
 * Sends local stock changes to the server.
 */
public class StockClient {

    private final String host;
    private final int port;
    private final NetworkManager networkManager;

    private Socket socket;
    private PrintWriter out;
    private Thread listenerThread;
    private volatile boolean connected = false;

    public StockClient(String host, int port, NetworkManager networkManager) {
        this.host = host;
        this.port = port;
        this.networkManager = networkManager;
    }

    /**
     * Connect to the server. Blocks until connected or throws an exception.
     */
    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000); // 5s timeout
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        connected = true;

        listenerThread = new Thread(this::listen, "StockClient-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        System.out.println("[CLIENT] Connected to server " + host + ":" + port);
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) { /* ignore */ }
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Send a stock update to the server (called when this machine buys a product).
     */
    public void sendStockUpdate(String productId, int qty) {
        if (out != null && connected) {
            out.println("STOCK_UPDATE:" + productId + ":" + qty);
        }
    }

    /**
     * Request full stock snapshot from server.
     */
    public void requestAllStock() {
        if (out != null && connected) {
            out.println("GET_ALL");
        }
    }

    /**
     * Notify server that a new product was added.
     */
    public void sendNewProduct(String productId, String name, String category, int qty, double price) {
        if (out != null && connected) {
            out.println("NEW_PRODUCT:" + productId + ":" + name + ":" + category + ":" + qty + ":" + price);
        }
    }

    private void listen() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while (connected && (line = in.readLine()) != null) {
                handleMessage(line);
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("[CLIENT] Connection lost: " + e.getMessage());
            }
        }
        connected = false;
        networkManager.onClientDisconnected();
    }

    private void handleMessage(String line) {
        if (line.startsWith("STOCK_UPDATE:")) {
            // Another client bought something - update local stock and refresh UI
            String[] parts = line.split(":");
            if (parts.length == 3) {
                String productId = parts[1];
                try {
                    int qty = Integer.parseInt(parts[2]);
                    networkManager.onNetworkUpdate(productId, qty);
                    System.out.println("[CLIENT] Received stock update: " + productId + " -> " + qty);
                } catch (NumberFormatException e) {
                    System.err.println("[CLIENT] Invalid update: " + line);
                }
            }
        } else if (line.startsWith("STOCK_ALL:")) {
            // Full stock snapshot from server — sync everything in one batch (single file save)
            String data = line.substring("STOCK_ALL:".length());
            if (data.isEmpty()) return;
            Map<String, Integer> updates = new HashMap<>();
            for (String item : data.split("\\|")) {
                String[] kv = item.split("=");
                if (kv.length == 2) {
                    try { updates.put(kv[0], Integer.parseInt(kv[1])); }
                    catch (NumberFormatException ignored) {}
                }
            }
            networkManager.onBatchNetworkUpdate(updates);
            System.out.println("[CLIENT] Full stock synced from server (" + updates.size() + " products)");
        } else if (line.startsWith("NEW_PRODUCT:")) {
            // Another client added a new product
            networkManager.onNewProductFromNetwork(line.substring("NEW_PRODUCT:".length()));
        }
    }
}

