package com.example.demo;

import java.io.IOException;

/**
 * Standalone Client Launcher
 * Run this on client machines to connect to the stock server.
 *
 * Usage: java ClientLauncher [host] [port]
 * Example: java ClientLauncher localhost 5555
 * Example: java ClientLauncher 192.168.1.100 5555 (connect to server on another machine)
 *
 * Default: localhost:5555
 */
public class ClientLauncher {

    public static void main(String[] args) throws InterruptedException {
        String host = "localhost"; // Default host
        int port = 5555;           // Default port

        // Parse command line arguments
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + port);
            }
        }

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║       STOCK MANAGEMENT SYSTEM - CLIENT MODE                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("🔌 Connecting to server at " + host + ":" + port + "...");
        System.out.println();

        // Initialize StockManager
        StockManager.initializeStock();

        // Create NetworkManager (client mode)
        NetworkManager networkManager = NetworkManager.getInstance();

        try {
            networkManager.connectToServer(host, port);

            System.out.println("✅ Connected to server successfully!");
            System.out.println();
            System.out.println("This client is now synchronized with the server.");
            System.out.println("Any changes made here will be sent to the server");
            System.out.println("and broadcast to all other clients.");
            System.out.println();
            System.out.println("Press Ctrl+C to disconnect from the server.");
            System.out.println();

            // Keep client running
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to server!");
            System.err.println("   Make sure the server is running at " + host + ":" + port);
            System.err.println("   Error: " + e.getMessage());
            System.exit(1);
        }

    }
}

