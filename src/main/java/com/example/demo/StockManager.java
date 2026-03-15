package com.example.demo;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages stock quantities for all products.
 *
 * Persistence: every change is written to STOCK_FILE so data survives restarts.
 * File lives in the user's home dir: ~/.shopapp_stock.dat
 * Format per line:  productId|productName|category|quantity|price
 */
public final class StockManager {

    // ── Persistence ──────────────────────────────────────────────────────────
    private static final Path STOCK_FILE =
            Paths.get(System.getProperty("user.home"), ".shopapp_stock.dat");

    // ── In-memory store ──────────────────────────────────────────────────────
    private static final Map<String, StockItem> stockData = new LinkedHashMap<>();
    private static final List<Runnable> externalChangeListeners = new CopyOnWriteArrayList<>();
    private static boolean initialized = false;
    private static volatile boolean fileWatcherStarted = false;
    private static volatile long lastKnownFileModified = -1L;

    private StockManager() {}

    // ── Default product catalogue ─────────────────────────────────────────────

    private static void addDefault(String id, String name, String cat, int qty, double price) {
        // Only inserts if product is NOT already loaded from file
        if (!stockData.containsKey(id)) {
            stockData.put(id, new StockItem(id, name, cat, qty, price));
        }
    }

    private static void loadDefaults() {
        // Electronics
        addDefault("E_iPhone15",        "iPhone 15",                   "Electronics", 25, 99999);
        addDefault("E_iPhone16",        "iPhone 16",                   "Electronics", 25, 120000);
        addDefault("E_iPhone17",        "iPhone 17",                   "Electronics", 25, 139000);
        addDefault("E_SamsungS25",      "Samsung Galaxy S25",          "Electronics", 25, 120000);
        addDefault("E_VivoX200",        "Vivo X200 Ultra",             "Electronics", 25, 90000);
        addDefault("E_LenovoIdeaPad",   "Lenovo IdeaPad i5 8GB SSD",  "Electronics", 25, 144000);
        addDefault("E_WirelessEarbuds", "Wireless Earbuds Pro",        "Electronics", 25, 5000);
        addDefault("E_SmartWatch",      "Smart Watch Fitness",         "Electronics", 25, 10000);
        addDefault("E_PowerBank",       "Power Bank 20000mAh",        "Electronics", 25, 2500);
        addDefault("E_iPad",            "iPad",                        "Electronics", 25, 30000);
        addDefault("E_Mouse",           "Mouse",                       "Electronics", 25, 5000);
        addDefault("E_AsusVivoBook",    "Asus VivoBook Ryzen 5 16GB", "Electronics", 25, 116000);
        addDefault("E_AjazzK80",        "Ajazz K80 Redswitch",        "Electronics", 25, 4200);
        // Beauty
        addDefault("B_FaceCream",    "Vitamin C Face Cream",    "Beauty", 25, 1500);
        addDefault("B_Serum",        "Hyaluronic Acid Serum",   "Beauty", 25, 2000);
        addDefault("B_FoamCleanser", "Gentle Foam Cleanser",    "Beauty", 25, 800);
        addDefault("B_LipstickSet",  "Matte Lipstick Set",      "Beauty", 25, 1000);
        addDefault("B_Foundation",   "HD Foundation",            "Beauty", 25, 2000);
        addDefault("B_Eyeshadow",    "Eyeshadow Palette",       "Beauty", 25, 2000);
        addDefault("B_Shampoo",      "Keratin Repair Shampoo",  "Beauty", 25, 900);
        addDefault("B_Conditioner",  "Deep Conditioner",         "Beauty", 25, 1000);
        addDefault("B_HairOil",      "Argan Hair Oil",           "Beauty", 25, 1200);
        addDefault("B_Sunscreen",    "SPF 50+ Sunscreen",        "Beauty", 25, 1300);
        addDefault("B_Mascara",      "Volumizing Mascara",       "Beauty", 25, 800);
        addDefault("B_GentsFaceWash","Gents Face Wash",          "Beauty", 25, 1500);
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    public static synchronized void initializeStock() {
        if (initialized) return;
        loadFromFile();   // load saved data first
        loadDefaults();   // fill in any missing products with defaults
        saveToFile();     // persist combined state
        initialized = true;
        startFileWatcherIfNeeded();
        System.out.println("[StockManager] Ready — " + stockData.size()
                + " products | file: " + STOCK_FILE);
    }

    /**
     * Register a callback that is invoked when another running process changes the stock file.
     */
    public static void addExternalChangeListener(Runnable listener) {
        if (listener != null) externalChangeListeners.add(listener);
    }

    public static void removeExternalChangeListener(Runnable listener) {
        externalChangeListeners.remove(listener);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public static synchronized void addStock(String productId, String productName,
                                             String category, int quantity, double price) {
        addStock(productId, productName, category, category, quantity, price, "");
    }

    public static synchronized void addStock(String productId, String productName,
                                             String category, String subCategory,
                                             int quantity, double price, String imagePath) {
        initializeStock();
        stockData.put(productId, new StockItem(productId, productName, category, subCategory,
                quantity, price, imagePath));
        saveToFile();
    }

    public static synchronized void upsertStockItem(StockItem item) {
        initializeStock();
        if (item == null || item.getProductId() == null || item.getProductId().isBlank()) return;
        stockData.put(item.getProductId(), item);
        saveToFile();
    }

    public static synchronized void updateStock(String productId, int newQuantity) {
        initializeStock();
        StockItem item = stockData.get(productId);
        if (item != null) {
            item.setQuantity(Math.max(0, newQuantity));
            saveToFile();
        }
    }

    /**
     * Update many products at once, then save a single time.
     * Used when syncing a full stock snapshot received from the server.
     */
    public static synchronized void batchUpdateStock(Map<String, Integer> updates) {
        initializeStock();
        for (Map.Entry<String, Integer> e : updates.entrySet()) {
            StockItem item = stockData.get(e.getKey());
            if (item != null) {
                item.setQuantity(Math.max(0, e.getValue()));
            }
        }
        saveToFile();
    }

    public static synchronized void replaceAllStock(List<StockItem> items) {
        initializeStock();
        stockData.clear();
        if (items != null) {
            for (StockItem item : items) {
                if (item != null && item.getProductId() != null && !item.getProductId().isBlank()) {
                    stockData.put(item.getProductId(), item);
                }
            }
        }
        saveToFile();
    }

    public static synchronized boolean reduceStock(String productId, int quantity) {
        initializeStock();
        StockItem item = stockData.get(productId);
        if (item != null && item.getQuantity() >= quantity) {
            item.setQuantity(item.getQuantity() - quantity);
            saveToFile();
            return true;
        }
        return false;
    }

    public static int getStock(String productId) {
        initializeStock();
        StockItem item = stockData.get(productId);
        return item != null ? item.getQuantity() : 0;
    }

    public static StockItem getStockItem(String productId) {
        initializeStock();
        return stockData.get(productId);
    }

    public static List<StockItem> getAllStockItems() {
        initializeStock();
        return new ArrayList<>(stockData.values());
    }

    public static List<StockItem> getStockByCategory(String category) {
        initializeStock();
        List<StockItem> items = new ArrayList<>();
        for (StockItem item : stockData.values())
            if (item.getCategory().equals(category)) items.add(item);
        return items;
    }

    public static List<StockItem> getLowStockItems() {
        initializeStock();
        List<StockItem> items = new ArrayList<>();
        for (StockItem item : stockData.values())
            if (item.getQuantity() > 0 && item.getQuantity() <= 10) items.add(item);
        return items;
    }

    public static List<StockItem> getOutOfStockItems() {
        initializeStock();
        List<StockItem> items = new ArrayList<>();
        for (StockItem item : stockData.values())
            if (item.getQuantity() <= 0) items.add(item);
        return items;
    }

    public static String findProductIdByName(String productName) {
        initializeStock();
        if (productName == null) return null;
        for (Map.Entry<String, StockItem> e : stockData.entrySet())
            if (productName.equals(e.getValue().getProductName())) return e.getKey();
        return null;
    }

    public static boolean isInStock(String productId)  { return getStock(productId) > 0; }
    public static boolean isLowStock(String productId) {
        int q = getStock(productId); return q > 0 && q <= 10;
    }

    /**
     * Reset all quantities back to default (25) and delete the saved file.
     * Useful for an admin "Reset Stock" function.
     */
    public static synchronized void resetToDefaults() {
        stockData.clear();
        initialized = false;
        try { Files.deleteIfExists(STOCK_FILE); } catch (IOException ignored) {}
        initializeStock();
        System.out.println("[StockManager] Stock reset to defaults.");
    }

    // ── File I/O ──────────────────────────────────────────────────────────────

    private static void saveToFile() {
        try (BufferedWriter bw = Files.newBufferedWriter(STOCK_FILE)) {
            for (StockItem item : stockData.values()) {
                bw.write(item.getProductId()  + "|"
                       + item.getProductName() + "|"
                       + item.getCategory()    + "|"
                       + item.getSubCategory() + "|"
                       + item.getQuantity()    + "|"
                       + item.getPrice()       + "|"
                       + item.getImagePath());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[StockManager] Save failed: " + e.getMessage());
            return;
        }
        lastKnownFileModified = getFileModifiedMillis();
    }

    private static void loadFromFile() {
        if (!Files.exists(STOCK_FILE)) {
            lastKnownFileModified = -1L;
            System.out.println("[StockManager] No saved file — will use defaults.");
            return;
        }
        int count = 0;
        try (BufferedReader br = Files.newBufferedReader(STOCK_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 5) continue;
                try {
                    String id   = p[0];
                    String name = p[1];
                    String cat  = p[2];
                    String subCategory;
                    int qty;
                    double price;
                    String imagePath = "";

                    if (p.length >= 7) {
                        subCategory = p[3];
                        qty = Integer.parseInt(p[4].trim());
                        price = Double.parseDouble(p[5].trim());
                        imagePath = p[6];
                    } else {
                        subCategory = cat;
                        qty = Integer.parseInt(p[3].trim());
                        price = Double.parseDouble(p[4].trim());
                    }

                    stockData.put(id, new StockItem(id, name, cat, subCategory, qty, price, imagePath));
                    count++;
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[StockManager] Load failed: " + e.getMessage());
        }
        lastKnownFileModified = getFileModifiedMillis();
        System.out.println("[StockManager] Loaded " + count + " products from " + STOCK_FILE);
    }

    private static long getFileModifiedMillis() {
        try {
            if (!Files.exists(STOCK_FILE)) return -1L;
            return Files.getLastModifiedTime(STOCK_FILE).toMillis();
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
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                boolean changedExternally = false;
                synchronized (StockManager.class) {
                    if (!initialized) continue;

                    long currentModified = getFileModifiedMillis();
                    if (currentModified != lastKnownFileModified) {
                        stockData.clear();
                        loadFromFile();
                        loadDefaults();
                        changedExternally = true;
                    }
                }

                if (changedExternally) {
                    notifyExternalChangeListeners();
                }
            }
        }, "StockManager-FileWatcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    private static void notifyExternalChangeListeners() {
        for (Runnable listener : externalChangeListeners) {
            try {
                listener.run();
            } catch (RuntimeException ignored) {
                // Listener failures should not stop stock synchronization.
            }
        }
    }
}
