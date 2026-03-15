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
    private static boolean initialized = false;

    private SalesManager() {}

    public static synchronized void initializeSales() {
        if (initialized) return;
        loadFromFile();
        initialized = true;
        System.out.println("[SalesManager] Ready — " + salesData.size() + " sale(s) | file: " + SALES_FILE);
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
        System.out.println("[SalesManager] Loaded " + count + " sale(s) from " + SALES_FILE);
    }
}

