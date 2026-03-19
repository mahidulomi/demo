package com.example.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Persists completed sales so server and clients can stay in sync.
 */
public final class SalesManager {
    private static final Path SALES_FILE =
            Paths.get(System.getProperty("user.home"), ".shopapp_sales.dat");

    private static final Map<String, SaleRecord> salesData = new LinkedHashMap<>();
    private static final List<Runnable> externalChangeListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static boolean initialized = false;

    private static long lastKnownFileModified = -1L;
    private static boolean fileWatcherStarted = false;

    private SalesManager() {}

    public static synchronized void initializeSales() {
        if (initialized) return;
        loadFromFile();
        initialized = true;
        System.out.println("[SalesManager] Ready — " + salesData.size() + " sale(s) | file: " + SALES_FILE);
        startFileWatcherIfNeeded();
    }
    
    public static void addExternalChangeListener(Runnable listener) {
        externalChangeListeners.add(listener);
    }

    public static synchronized void recordSale(SaleRecord sale) {
        initializeSales();
        if (sale == null || sale.getSaleId() == null || sale.getSaleId().isEmpty()) return;
        salesData.put(sale.getSaleId(), sale);
        saveToFile();
    }

    public static synchronized void replaceAllSales(List<SaleRecord> sales) {
        initializeSales();
        salesData.clear();
        if (sales != null) {
            for (SaleRecord sale : sales) {
                if (sale != null && sale.getSaleId() != null && !sale.getSaleId().isEmpty()) {
                    salesData.put(sale.getSaleId(), sale);
                }
            }
        }
        saveToFile();
    }

    public static synchronized List<SaleRecord> getAllSales() {
        initializeSales();
        return new ArrayList<>(salesData.values());
    }

    public static synchronized String getSerializedSalesData() {
        initializeSales();
        return NetworkCodec.joinRecords(salesData.values().stream()
                .map(NetworkCodec::encodeSaleRecord)
                .collect(Collectors.toList()));
    }

    private static void saveToFile() {
        try (BufferedWriter bw = Files.newBufferedWriter(SALES_FILE)) {
            for (SaleRecord sale : salesData.values()) {
                bw.write(NetworkCodec.encodeSaleRecord(sale));
                bw.newLine();
            }
            // Update timestamp so we don't trigger our own watcher unnecessarily
             lastKnownFileModified = getFileModifiedMillis();
        } catch (IOException e) {
            System.err.println("[SalesManager] Save failed: " + e.getMessage());
        }
    }

    private static void loadFromFile() {
        if (!Files.exists(SALES_FILE)) {
            System.out.println("[SalesManager] No saved file — starting with empty sales history.");
            return;
        }
        int count = 0;
        try (BufferedReader br = Files.newBufferedReader(SALES_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    SaleRecord sale = NetworkCodec.decodeSaleRecord(line);
                    salesData.put(sale.getSaleId(), sale);
                    count++;
                } catch (RuntimeException e) {
                    System.err.println("[SalesManager] Skipping bad sale line: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[SalesManager] Load failed: " + e.getMessage());
        }
        lastKnownFileModified = getFileModifiedMillis();
        System.out.println("[SalesManager] Loaded " + count + " sale(s) from " + SALES_FILE);
    }

    private static long getFileModifiedMillis() {
        try {
            if (!Files.exists(SALES_FILE)) return -1L;
            return Files.getLastModifiedTime(SALES_FILE).toMillis();
        } catch (IOException e) {
            return -1L;
        }
    }

    private static void startFileWatcherIfNeeded() {
        if (fileWatcherStarted) return;
        fileWatcherStarted = true;

        Thread watcher = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000); // Check every 2 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                boolean changedExternally = false;
                synchronized (SalesManager.class) {
                    if (!initialized) continue;

                    long currentModified = getFileModifiedMillis();
                    if (currentModified != lastKnownFileModified && currentModified != -1L) {
                        salesData.clear();
                        loadFromFile();
                        changedExternally = true;
                    }
                }

                if (changedExternally) {
                    System.out.println("[SalesManager] Detected external file change. Reloading sales...");
                    notifyExternalChangeListeners();
                }
            }
        }, "SalesManager-FileWatcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void notifyExternalChangeListeners() {
        for (Runnable listener : externalChangeListeners) {
            try {
                listener.run();
            } catch (RuntimeException ignored) {
            }
        }
    }

    /**
     * Generates the next sequential Bill ID (e.g., BILL-1001, BILL-1002).
     */
    public static synchronized String getNextBillId() {
        initializeSales();
        int maxId = 1000;
        for (String id : salesData.keySet()) {
            if (id != null && id.startsWith("BILL-")) {
                try {
                    String numPart = id.substring(5);
                    int num = Integer.parseInt(numPart);
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("BILL-%05d", maxId + 1);
    }

    /**
     * Clear all sales records (for demo or reset purposes).
     */
    public static synchronized void clearAllSales() {
        initializeSales();
        salesData.clear();
        saveToFile();
        System.out.println("[SalesManager] All sales records cleared.");
    }
}
