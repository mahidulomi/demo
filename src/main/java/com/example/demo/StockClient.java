package com.example.demo;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

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
    public void sendNewProduct(StockItem item) {
        if (out != null && connected) {
            out.println("PRODUCT_UPSERT:" + NetworkCodec.encodeStockItem(item));
        }
    }

    public void sendDeleteProduct(String productId) {
        if (out != null && connected) {
            out.println("PRODUCT_DELETE:" + productId);
        }
    }

    public void sendSaleRecord(SaleRecord sale) {
        if (out != null && connected) {
            out.println("SALE_RECORD:" + NetworkCodec.encodeSaleRecord(sale));
        }
    }

    public void sendCustomerUpdate(Customer customer) {
        if (out != null && connected) {
            out.println("CUSTOMER_UPSERT:" + NetworkCodec.encodeCustomer(customer));
        }
    }

    public void sendUserUpdate(String userData) {
        if (out != null && connected) {
            out.println("USER_UPSERT:" + userData);
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
        } else if (line.startsWith("PRODUCT_ALL:")) {
            String data = line.substring("PRODUCT_ALL:".length());
            List<StockItem> products = new ArrayList<>();
            for (String record : NetworkCodec.splitRecords(data)) {
                try {
                    products.add(NetworkCodec.decodeStockItem(record));
                } catch (RuntimeException e) {
                    System.err.println("[CLIENT] Skipping bad product record: " + e.getMessage());
                }
            }
            networkManager.onFullProductSync(products);
            System.out.println("[CLIENT] Full product catalog synced from server (" + products.size() + " products)");
        } else if (line.startsWith("SALES_ALL:")) {
            String data = line.substring("SALES_ALL:".length());
            List<SaleRecord> sales = new ArrayList<>();
            for (String record : NetworkCodec.splitRecords(data)) {
                try {
                    sales.add(NetworkCodec.decodeSaleRecord(record));
                } catch (RuntimeException e) {
                    System.err.println("[CLIENT] Skipping bad sale record: " + e.getMessage());
                }
            }
            networkManager.onFullSalesSync(sales);
            System.out.println("[CLIENT] Full sales history synced from server (" + sales.size() + " sales)");
        } else if (line.startsWith("CUSTOMERS_ALL:")) {
            String data = line.substring("CUSTOMERS_ALL:".length());
            List<Customer> customers = new ArrayList<>();
            for (String record : NetworkCodec.splitRecords(data)) {
                try {
                    customers.add(NetworkCodec.decodeCustomer(record));
                } catch (RuntimeException e) {
                    System.err.println("[CLIENT] Skipping bad customer record: " + e.getMessage());
                }
            }
            networkManager.onFullCustomerSync(customers);
            System.out.println("[CLIENT] Full customer list synced from server (" + customers.size() + " customers)");
        } else if (line.startsWith("USERS_ALL:")) {
            String data = line.substring("USERS_ALL:".length());
            List<String> users = NetworkCodec.splitRecords(data);
            networkManager.onFullUserSync(users);
            System.out.println("[CLIENT] Full user list synced from server.");
        } else if (line.startsWith("PRODUCT_UPSERT:")) {
            networkManager.onNewProductFromNetwork(line.substring("PRODUCT_UPSERT:".length()));
        } else if (line.startsWith("NEW_PRODUCT:")) {
            networkManager.onNewProductFromNetwork(line.substring("NEW_PRODUCT:".length()));
        } else if (line.startsWith("PRODUCT_DELETE:")) {
            networkManager.onDeleteProductFromNetwork(line.substring("PRODUCT_DELETE:".length()));
        } else if (line.startsWith("SALE_RECORD:")) {
            networkManager.onSaleRecordFromNetwork(line.substring("SALE_RECORD:".length()));
        } else if (line.startsWith("CUSTOMER_UPSERT:")) {
            networkManager.onCustomerUpdateFromNetwork(line.substring("CUSTOMER_UPSERT:".length()));
        } else if (line.startsWith("USER_UPSERT:")) {
            networkManager.onUserUpdateFromNetwork(line.substring("USER_UPSERT:".length()));
        }
    }
}
