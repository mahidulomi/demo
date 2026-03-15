package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
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

    // Version marker for encoded on-disk format.
    private static final String ENCODED_PREFIX = "v2:";

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    // Legacy location used by older builds.
    private static final Path LEGACY_STORE_PATH = Path.of(
            System.getProperty("user.home"),
            ".demo-javafx",
            "users.properties"
    );

    // Active location (Windows APPDATA preferred, with property override support).
    private static final Path STORE_PATH = resolveStorePath();

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
        if (!created) {
            return false;
        }

        // Make sure "account created" means it is persisted too.
        if (!saveToDiskSafe()) {
            USERS.remove(key);
            return false;
        }
        return true;
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
        if (!saveToDiskSafe()) {
            USERS.put(key, existing);
            return false;
        }
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

    /** Package-private for tests. */
    static Path storePathForTestOnly() {
        return STORE_PATH;
    }

    private static void loadFromDisk() {
        USERS.clear();

        Path sourcePath = chooseReadableStorePath();
        if (sourcePath == null) {
            return;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(sourcePath)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("[UserStore] Failed to load users from " + sourcePath + ": " + e.getMessage());
            return;
        }

        for (String name : props.stringPropertyNames()) {
            String value = props.getProperty(name);
            if (isBlank(name) || value == null) continue;

            User parsed = parseStoredUser(value);
            if (parsed != null && !isBlank(parsed.password())) {
                USERS.put(name.toLowerCase(), parsed);
            }
        }

        // Migrate legacy file to active location once successfully loaded.
        if (!sourcePath.equals(STORE_PATH)) {
            saveToDiskSafe();
        }
    }

    private static boolean saveToDiskSafe() {
        try {
            saveToDisk();
            return true;
        } catch (IOException e) {
            System.err.println("[UserStore] Failed to save users to " + STORE_PATH + ": " + e.getMessage());
            return false;
        }
    }

    private static void saveToDisk() throws IOException {
        Files.createDirectories(STORE_PATH.getParent());

        Properties props = new Properties();
        for (Map.Entry<String, User> e : USERS.entrySet()) {
            User u = e.getValue();
            props.setProperty(e.getKey(), serializeUser(u));
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

    private static Path resolveStorePath() {
        String override = System.getProperty("demo.user.store");
        if (!isBlank(override)) {
            return Path.of(override).toAbsolutePath().normalize();
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        String appData = System.getenv("APPDATA");
        if (os.contains("win") && !isBlank(appData)) {
            return Path.of(appData, "DemoJavaFX", "users.properties");
        }

        return LEGACY_STORE_PATH;
    }

    private static Path chooseReadableStorePath() {
        if (Files.exists(STORE_PATH)) {
            return STORE_PATH;
        }

        if (!STORE_PATH.equals(LEGACY_STORE_PATH) && Files.exists(LEGACY_STORE_PATH)) {
            return LEGACY_STORE_PATH;
        }

        return null;
    }

    private static String serializeUser(User user) {
        return ENCODED_PREFIX + encode(user.password()) + "|" + encode(safe(user.personalData()));
    }

    private static User parseStoredUser(String value) {
        if (value.startsWith(ENCODED_PREFIX)) {
            String encoded = value.substring(ENCODED_PREFIX.length());
            String[] parts = encoded.split("\\|", 2);

            if (parts.length == 0 || isBlank(parts[0])) {
                return null;
            }

            String password = decode(parts[0]);
            if (isBlank(password)) {
                return null;
            }

            String personalData = parts.length > 1 ? decode(parts[1]) : "";
            if (personalData == null) {
                personalData = "";
            }
            return new User(password, safe(personalData));
        }

        // Legacy format: plain text "password|personalData".
        String[] parts = value.split("\\|", 2);
        String password = parts.length > 0 ? parts[0] : "";
        String personalData = parts.length > 1 ? parts[1] : "";
        if (isBlank(password)) {
            return null;
        }
        return new User(password, safe(personalData));
    }

    private static String encode(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
