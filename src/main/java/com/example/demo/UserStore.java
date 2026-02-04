package com.example.demo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small in-memory user store for demo.
 * NOTE: This is not secure and will reset on app restart.
 */
public final class UserStore {
    private static final Map<String, String> USERS = new ConcurrentHashMap<>();

    static {
        // No default users. Must sign up first.
    }

    private UserStore() {}

    public static boolean userExists(String username) {
        return username != null && USERS.containsKey(username.toLowerCase());
    }

    public static boolean createUser(String username, String password) {
        if (isBlank(username) || isBlank(password)) return false;

        String key = username.toLowerCase();
        return USERS.putIfAbsent(key, password) == null;
    }

    public static boolean validateLogin(String username, String password) {
        if (isBlank(username) || isBlank(password)) return false;

        String key = username.toLowerCase();
        String stored = USERS.get(key);
        return stored != null && stored.equals(password);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
