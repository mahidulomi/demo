package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small user store for demo.
 *
 * <p>Accounts are stored locally on disk so they persist between app runs.
 * NOTE: Passwords are stored as plain text for demo purposes (not secure).</p>
 */
public final class UserStore {

    private record User(String password, String personalData) {}

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    // Store under user's home so it works without admin rights.
    private static final Path STORE_PATH = Path.of(
            System.getProperty("user.home"),
            ".demo-javafx",
            "users.properties"
    );

    static {
        loadFromDisk();
    }

    private UserStore() {}

    public static boolean userExists(String username) {
        return username != null && USERS.containsKey(username.toLowerCase());
    }

    /** Backwards-compatible overload (older accounts won't have recovery data). */
    public static boolean createUser(String username, String password) {
        return createUser(username, password, "");
    }

    public static boolean createUser(String username, String password, String personalData) {
        if (isBlank(username) || isBlank(password)) return false;

        String key = username.toLowerCase();
        boolean created = USERS.putIfAbsent(key, new User(password, safe(personalData))) == null;
        if (created) {
            saveToDiskSafe();
        }
        return created;
    }

    public static boolean validateLogin(String username, String password) {
        if (isBlank(username) || isBlank(password)) return false;

        String key = username.toLowerCase();
        User stored = USERS.get(key);
        return stored != null && stored.password().equals(password);
    }

    public static boolean verifyRecoveryData(String username, String personalData) {
        if (isBlank(username) || isBlank(personalData)) return false;
        User user = USERS.get(username.toLowerCase());
        if (user == null) return false;

        // stored personal data may be blank for legacy accounts
        return !isBlank(user.personalData()) && user.personalData().equalsIgnoreCase(personalData.trim());
    }

    public static boolean resetPassword(String username, String personalData, String newPassword) {
        if (isBlank(newPassword)) return false;
        if (!verifyRecoveryData(username, personalData)) return false;

        String key = username.toLowerCase();
        User existing = USERS.get(key);
        if (existing == null) return false;

        USERS.put(key, new User(newPassword, existing.personalData()));
        saveToDiskSafe();
        return true;
    }

    /** Package-private for tests. */
    static void clearInMemoryForTestOnly() {
        USERS.clear();
    }

    /** Package-private for tests. */
    static void reloadFromDiskForTestOnly() {
        loadFromDisk();
    }

    private static void loadFromDisk() {
        USERS.clear();

        if (!Files.exists(STORE_PATH)) {
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(STORE_PATH)) {
            props.load(in);
        } catch (IOException e) {
            // Ignore corrupt/missing file; app should still run.
            return;
        }

        for (String name : props.stringPropertyNames()) {
            String value = props.getProperty(name);
            if (isBlank(name) || value == null) continue;

            String[] parts = value.split("\\|", 2);
            String password = parts.length > 0 ? parts[0] : "";
            String personalData = parts.length > 1 ? parts[1] : "";

            if (!isBlank(password)) {
                USERS.put(name.toLowerCase(), new User(password, safe(personalData)));
            }
        }
    }

    private static void saveToDiskSafe() {
        try {
            saveToDisk();
        } catch (IOException ignored) {
            // For demo: don't crash the app if disk write fails.
        }
    }

    private static void saveToDisk() throws IOException {
        Files.createDirectories(STORE_PATH.getParent());

        Properties props = new Properties();
        for (Map.Entry<String, User> e : USERS.entrySet()) {
            User u = e.getValue();
            props.setProperty(e.getKey(), u.password() + "|" + safe(u.personalData()));
        }

        // Atomic-ish write: write to temp file and then move.
        Path tmp = STORE_PATH.resolveSibling(STORE_PATH.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(tmp)) {
            props.store(out, "Demo JavaFX users (plain text passwords - not secure)");
        }
        Files.move(tmp, STORE_PATH, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
