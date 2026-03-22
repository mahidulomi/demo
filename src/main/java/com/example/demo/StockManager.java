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

    private static void addDefault(String id, String name, String cat, int qty, double price, String imagePath) {
        // Only inserts if product is NOT already loaded from file
        if (!stockData.containsKey(id)) {
            stockData.put(id, new StockItem(id, name, cat, cat, qty, price, imagePath));
        } else {
            // If item exists but has no image (or has old /home/ or broken /extra/ path), update it
            StockItem existing = stockData.get(id);
            if ((existing.getImagePath() == null || existing.getImagePath().isBlank() 
                    || existing.getImagePath().startsWith("/home/") 
                    || existing.getImagePath().startsWith("/extra/"))
                    && imagePath != null && !imagePath.isBlank()) {
                existing.setImagePath(imagePath);
            }
        }
    }

    private static void loadDefaults() {
        // --- BEAUTY ---
        addDefault("B_AcidSerum",       "Acid Serum",             "Beauty", 25, 1200, "/beautyimages/acidserum.png");
        addDefault("B_DeepConditioner", "Deep Conditioner",       "Beauty", 25, 950, "/beautyimages/deepconditioner.png");
        addDefault("B_Eyeshadow",       "Eyeshadow Palette",      "Beauty", 25, 1800, "/beautyimages/eyeshadow.png");
        addDefault("B_FaceCream",       "Face Cream",             "Beauty", 25, 850, "/beautyimages/facecream.png");
        addDefault("B_FoamCleanser",    "Foam Cleanser",          "Beauty", 25, 600, "/beautyimages/foamcleanser.png");
        addDefault("B_Foundation",      "Foundation",             "Beauty", 25, 1500, "/beautyimages/foundation_cropped.png");
        addDefault("B_GarnierMen",      "Garnier Men Facewash",   "Beauty", 25, 250, "/beautyimages/gernierman_1_cropped.png");
        addDefault("B_HairOil",         "Hair Oil",               "Beauty", 25, 350, "/beautyimages/hairoil_cropped.png");
        addDefault("B_LipstickSet",     "Lipstick Set",           "Beauty", 25, 2200, "/beautyimages/lipstickset_cropped.png");
        addDefault("B_Mascara",         "Mascara",                "Beauty", 25, 450, "/beautyimages/mashkara_cropped.png");
        addDefault("B_Shampoo",         "Shampoo",                "Beauty", 25, 500, "/beautyimages/shampp_1_cropped.png");
        addDefault("B_Sunscreen",       "Sunscreen",              "Beauty", 25, 750, "/beautyimages/sunscreen_1_cropped.png");

        // --- ELECTRONICS ---
        addDefault("E_AirPods",         "AirPods",               "Electronics", 25, 18000, "/images/airpods.png");
        addDefault("E_AsusLaptop",      "Asus Laptop",           "Electronics", 25, 65000, "/images/asus.png");
        addDefault("E_iPad",            "iPad",                  "Electronics", 25, 45000, "/images/ipad.png");
        addDefault("E_iPhone15",        "iPhone 15",             "Electronics", 25, 75000, "/images/iphone15.png");
        addDefault("E_iPhone16",        "iPhone 16",             "Electronics", 25, 85000, "/images/iphone16.png");
        addDefault("E_iPhone17",        "iPhone 17",             "Electronics", 25, 95000, "/images/iphone17.png");
        addDefault("E_LenovoLaptop",    "Lenovo Laptop",         "Electronics", 25, 55000, "/images/loglenevo.png");
        addDefault("E_WirelessMouse",   "Wireless Mouse",        "Electronics", 25, 1200, "/images/mouise.png");
        addDefault("E_PowerBank",       "Power Bank",            "Electronics", 25, 2500, "/images/powerbank.png");
        addDefault("E_SamsungS25",      "Samsung S25",           "Electronics", 25, 80000, "/images/samsungs25.png");
        addDefault("E_VivoX200",        "Vivo X200 Ultra",       "Electronics", 25, 60000, "/images/vivox200ultra.png");

        // --- FASHION ---
        addDefault("F_TitanWatch",      "Titan Watch",           "Fashion", 25, 4500, "/images/titan.png");
        addDefault("E_Keyboard",        "Keyboard",              "Electronics", 25, 4500, "/images/ajaj.png");
        
        // New Fashion Items with Images
        addDefault("F_TShirt",          "T-shirt",               "Fashion", 25, 800, "/fashion/T-shirt.jpg");
        addDefault("F_Pant",            "Pant",                  "Fashion", 25, 1200, "/fashion/pant.jpg");
        addDefault("F_Sneakers",        "Sneakers",              "Fashion", 25, 2500, "/fashion/sneakers.jpg");
        addDefault("F_Jacket",          "Jacket",                "Fashion", 25, 2500, "/fashion/jacket.jpg");
        addDefault("F_Saree",           "Saree",                 "Fashion", 25, 3500, "/fashion/saree.jpg");
        addDefault("F_Shirt",           "Shirt",                 "Fashion", 25, 1500, "/fashion/Shirt.jpg");

        // --- HOME & LIVING ---
        addDefault("H_Sofa",            "Luxury Sofa",           "Home and Living", 5, 25000, "/extra/luxurysofa.jpg");
        addDefault("H_HomeDecor",       "Home Decor Set",        "Home and Living", 15, 4500, "/extra/decorset.jpg");
        addDefault("H_Swing",           "Indoor Swing",          "Home and Living", 10, 8500, "/extra/indorswing.jpg");
        addDefault("H_DiningTable",     "Dining Table",          "Home and Living", 5, 15000, "/extra/dinningtable.jpg");
        addDefault("H_CornerTable",     "Corner Table",          "Home and Living", 20, 3500, "/extra/cornertable.jpg");
        addDefault("H_BedSheet",        "Cotton Bed Sheet",      "Home and Living", 30, 2200, "/extra/bedsit.jpg");
        // REMOVED "H_Cushion" (Pillow/Soft Fur Cushion) as per request
        addDefault("H_WallClock",       "Lunar Wall Clock",      "Home and Living", 15, 3200, "/extra/wallclock.jpg");
        addDefault("H_TableLamp",       "Table Lamp",            "Home and Living", 25, 1500, "/extra/tablelamp.jpg");
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    public static synchronized void initializeStock() {
        if (initialized) return;

        // Remove old Fashion broken items to replace with new imaged ones
        if (stockData.containsKey("F_MensTShirt")) stockData.remove("F_MensTShirt");
        if (stockData.containsKey("F_Jeans")) stockData.remove("F_Jeans");
        // Remove old Ajaj Watch so it doesn't appear in Fashion
        if (stockData.containsKey("F_AjajWatch")) stockData.remove("F_AjajWatch");
        // Also remove any intermediate renamed versions if they exist
        if (stockData.containsKey("E_AjajKeyboard")) stockData.remove("E_AjajKeyboard");
        if (stockData.containsKey("E_RedSwitchKeyboard")) stockData.remove("E_RedSwitchKeyboard");

        // Remove any items that have no image path (as per user request: "jei box gulay image nei ogula remove kore dao")
        stockData.values().removeIf(item -> item.getImagePath() == null || item.getImagePath().trim().isEmpty());

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
