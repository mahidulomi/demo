package com.example.demo;

import java.io.IOException;

/**
 * Standalone Server Launcher
 * Run this on one machine to start the authoritative stock server.
 * Other machines will connect as clients.
 *
 * Usage: java ServerLauncher [port]
 * Default port: 5555
 */
public class ServerLauncher {

    public static void main(String[] args) throws InterruptedException {
        int port = 5555; // Default port

        // Allow port override via command line
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + port);
            }
        }

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║       STOCK MANAGEMENT SYSTEM - SERVER MODE                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📡 Starting Server on port " + port + "...");
        System.out.println("   Waiting for clients to connect...");
        System.out.println();

        // Initialize StockManager first
        StockManager.initializeStock();

        // Create NetworkManager (server mode)
        NetworkManager networkManager = NetworkManager.getInstance();

        try {
            networkManager.startAsServer(port);

            System.out.println("✅ Server started successfully!");
            System.out.println("   Other machines can connect to this server.");
            System.out.println();
            System.out.println("Press Ctrl+C to stop the server.");
            System.out.println();

            // Keep server running
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }
}

