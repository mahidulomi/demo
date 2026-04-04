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
 * Manages Customer data and persistence.
 */
public final class CustomerManager {
    private static final Path CUSTOMERS_FILE =
            Paths.get(System.getProperty("user.home"), ".shopapp_customers.dat");

    private static final Map<String, Customer> customerData = new LinkedHashMap<>();
    private static final List<Runnable> externalChangeListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static boolean initialized = false;

    private static long lastKnownFileModified = -1L;
    private static boolean fileWatcherStarted = false;

    private CustomerManager() {}

    public static synchronized void initializeCustomers() {
        if (initialized) return;
        loadFromFile();
        initialized = true;
        System.out.println("[CustomerManager] Ready — " + customerData.size() + " customer(s) | file: " + CUSTOMERS_FILE);
        startFileWatcherIfNeeded();
    }
    
    public static void addExternalChangeListener(Runnable listener) {
        externalChangeListeners.add(listener);
    }

    public static synchronized void saveCustomer(Customer customer) {
        initializeCustomers();
        if (customer == null || customer.getId() == null) return;
        customerData.put(customer.getId(), customer);
        saveToFile();
    }

    public static synchronized void replaceAllCustomers(List<Customer> customers) {
        initializeCustomers();
        customerData.clear();
        if (customers != null) {
            for (Customer c : customers) {
                if (c != null && c.getId() != null) {
                    customerData.put(c.getId(), c);
                }
            }
        }
        saveToFile();
    }
    
    public static synchronized void deleteCustomer(String customerId) {
        initializeCustomers();
        if (customerData.remove(customerId) != null) {
            saveToFile();
        }
    }

    public static synchronized List<Customer> getAllCustomers() {
        initializeCustomers();
        return new ArrayList<>(customerData.values());
    }

    public static synchronized Customer getCustomerById(String customerId) {
        initializeCustomers();
        return customerData.get(customerId);
    }

    public static synchronized Customer getCustomerByPhone(String phone) {
        initializeCustomers();
        return customerData.values().stream()
                .filter(c -> c.getPhone().equals(phone))
                .findFirst()
                .orElse(null);
    }

    public static synchronized List<Customer> getAllCustomersSorted() {
        initializeCustomers();
        return customerData.values().stream()
                .sorted((c1, c2) -> Long.compare(c1.getCreatedAt(), c2.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public static synchronized String getSerializedCustomerData() {
        initializeCustomers();
        return NetworkCodec.joinRecords(customerData.values().stream()
                .map(NetworkCodec::encodeCustomer)
                .collect(Collectors.toList()));
    }

    private static void saveToFile() {
        try (BufferedWriter bw = Files.newBufferedWriter(CUSTOMERS_FILE)) {
            for (Customer c : customerData.values()) {
                bw.write(NetworkCodec.encodeCustomer(c));
                bw.newLine();
            }
             lastKnownFileModified = getFileModifiedMillis();
        } catch (IOException e) {
            System.err.println("[CustomerManager] Save failed: " + e.getMessage());
        }
    }

    private static void loadFromFile() {
        if (!Files.exists(CUSTOMERS_FILE)) {
            System.out.println("[CustomerManager] No saved file — starting with empty customer list.");
            return;
        }
        int count = 0;
        try (BufferedReader br = Files.newBufferedReader(CUSTOMERS_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    Customer c = NetworkCodec.decodeCustomer(line);
                    customerData.put(c.getId(), c);
                    count++;
                } catch (RuntimeException e) {
                    System.err.println("[CustomerManager] Skipping bad customer line: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[CustomerManager] Load failed: " + e.getMessage());
        }
        lastKnownFileModified = getFileModifiedMillis();
        System.out.println("[CustomerManager] Loaded " + count + " customer(s) from " + CUSTOMERS_FILE);
    }

    private static long getFileModifiedMillis() {
        try {
            if (!Files.exists(CUSTOMERS_FILE)) return -1L;
            return Files.getLastModifiedTime(CUSTOMERS_FILE).toMillis();
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
                synchronized (CustomerManager.class) {
                    if (!initialized) continue;

                    long currentModified = getFileModifiedMillis();
                    if (currentModified != lastKnownFileModified && currentModified != -1L) {
                        customerData.clear();
                        loadFromFile();
                        changedExternally = true;
                    }
                }

                if (changedExternally) {
                    System.out.println("[CustomerManager] Detected external file change. Reloading customers...");
                    notifyExternalChangeListeners();
                }
            }
        }, "CustomerManager-FileWatcher");
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
}

