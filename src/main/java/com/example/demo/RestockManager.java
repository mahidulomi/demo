package com.example.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RestockManager {
    private static final Path RESTOCK_FILE = Paths.get(System.getProperty("user.home"), ".shopapp_restock.dat");
    private static final List<RestockRecord> restockHistory = new ArrayList<>();
    private static boolean initialized = false;

    public static synchronized void loadHistory() {
        if (initialized) return;
        if (!Files.exists(RESTOCK_FILE)) {
            initialized = true;
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(RESTOCK_FILE)) {
            restockHistory.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                // simple pipe delimited
                // timestamp|itemName|qty|price|addedBy|notes
                String[] parts = line.split("\\|", 6);
                if (parts.length >= 6) {
                    restockHistory.add(new RestockRecord(
                        parts[0], parts[1], Integer.parseInt(parts[2]), Double.parseDouble(parts[3]), parts[4], parts[5]
                    ));
                }
            }
            initialized = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void addRecord(RestockRecord record) {
        if (!initialized) loadHistory();
        restockHistory.add(record);
        saveToFile();
    }
    
    public static synchronized List<RestockRecord> getHistory() {
        if (!initialized) loadHistory();
        return new ArrayList<>(restockHistory);
    }

    private static void saveToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(RESTOCK_FILE)) {
            for (RestockRecord r : restockHistory) {
                String notes = r.getNotes() == null ? "" : r.getNotes().replace("\n", " ").replace("|", "");
                writer.write(String.format("%s|%s|%d|%.2f|%s|%s",
                     r.getTimestamp(), r.getItemName(), r.getQuantity(), r.getPurchasePrice(), r.getAddedBy(), notes
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

