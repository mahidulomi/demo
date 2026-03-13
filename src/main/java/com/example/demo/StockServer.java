package com.example.demo;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * TCP Server - one machine runs this to be the authoritative stock host.
 * All other clients connect to this server.
 * When any client buys a product, the server broadcasts the new stock to ALL clients.
 */
public class StockServer {

    private final int port;
    private final NetworkManager networkManager;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private Thread acceptThread;
    private volatile boolean running = false;

    public StockServer(int port, NetworkManager networkManager) {
        this.port = port;
        this.networkManager = networkManager;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        acceptThread = new Thread(this::acceptClients, "StockServer-Accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        System.out.println("[SERVER] Started on port " + port);
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) { /* ignore */ }
        synchronized (clients) {
            for (ClientHandler ch : new ArrayList<>(clients)) ch.close();
            clients.clear();
        }
    }

    public int getClientCount() {
        return clients.size();
    }

    private void acceptClients() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
                System.out.println("[SERVER] New client connected: " + socket.getInetAddress().getHostAddress()
                        + " | Total clients: " + clients.size());
                // Notify UI
                networkManager.onServerStatusChanged();
            } catch (IOException e) {
                if (running) System.err.println("[SERVER] Accept error: " + e.getMessage());
            }
        }
    }

    /**
     * Broadcast stock update to ALL connected clients.
     */
    public void broadcastToAllClients(String productId, int qty) {
        String message = "STOCK_UPDATE:" + productId + ":" + qty;
        synchronized (clients) {
            for (ClientHandler ch : new ArrayList<>(clients)) {
                ch.sendMessage(message);
            }
        }
    }

    /**
     * Broadcast stock update to all clients EXCEPT the sender.
     */
    private void broadcastToOthers(ClientHandler sender, String productId, int qty) {
        String message = "STOCK_UPDATE:" + productId + ":" + qty;
        synchronized (clients) {
            for (ClientHandler ch : new ArrayList<>(clients)) {
                if (ch != sender) {
                    ch.sendMessage(message);
                }
            }
        }
    }

    // ========================== INNER CLASS ==========================

    class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private volatile boolean active = true;

        ClientHandler(Socket socket) {
            this.socket = socket;
            setDaemon(true);
            setName("StockServer-Client-" + socket.getInetAddress().getHostAddress());
        }

        public void sendMessage(String msg) {
            if (out != null && active) {
                out.println(msg);
            }
        }

        public void close() {
            active = false;
            try {
                socket.close();
            } catch (IOException e) { /* ignore */ }
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                // Send full current stock to the newly connected client
                String stockData = networkManager.getFullStockData();
                out.println("STOCK_ALL:" + stockData);
                System.out.println("[SERVER] Sent full stock to new client");

                String line;
                while (active && (line = in.readLine()) != null) {
                    handleMessage(line);
                }
            } catch (IOException e) {
                if (active) System.out.println("[SERVER] Client disconnected: " + socket.getInetAddress().getHostAddress());
            } finally {
                clients.remove(this);
                active = false;
                networkManager.onServerStatusChanged();
            }
        }

        private void handleMessage(String line) {
            if (line.startsWith("STOCK_UPDATE:")) {
                // Client bought something - update server stock and broadcast to others
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String productId = parts[1];
                    try {
                        int qty = Integer.parseInt(parts[2]);
                        // Update server-side StockManager
                        networkManager.onNetworkUpdate(productId, qty);
                        // Broadcast to other clients
                        broadcastToOthers(this, productId, qty);
                        System.out.println("[SERVER] Stock update from client: " + productId + " -> " + qty);
                    } catch (NumberFormatException e) {
                        System.err.println("[SERVER] Invalid stock update: " + line);
                    }
                }
            } else if (line.equals("GET_ALL")) {
                // Client is requesting full stock
                out.println("STOCK_ALL:" + networkManager.getFullStockData());
            } else if (line.startsWith("NEW_PRODUCT:")) {
                // Client added a new product - update server StockManager and relay to others
                networkManager.onNewProductFromNetwork(line.substring("NEW_PRODUCT:".length()));
                broadcastRawToOthers(this, line);
            }
        }

        private void broadcastRawToOthers(ClientHandler sender, String fullMessage) {
            synchronized (clients) {
                for (ClientHandler ch : new ArrayList<>(clients)) {
                    if (ch != sender) {
                        ch.sendMessage(fullMessage);
                    }
                }
            }
        }
    }
}

