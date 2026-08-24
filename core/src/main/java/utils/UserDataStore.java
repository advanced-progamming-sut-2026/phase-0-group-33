package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserDataStore {
    private static final Map<String, UserDataStore> CACHE = new LinkedHashMap<>();

    private final String fileName;
    private final Map<String, String> values = new LinkedHashMap<>();

    private UserDataStore(String username) {
        this.fileName = "user_" + username + ".properties";
        load();
    }

    public static synchronized UserDataStore forUser(String username) {
        return CACHE.computeIfAbsent(username, UserDataStore::new);
    }

    public static synchronized void evict(String username) {
        CACHE.remove(username);
    }

    public synchronized void reload() {
        values.clear();
        load();
    }

    private void load() {
        for (String line : FileStore.readLines(fileName)) {
            int sep = line.indexOf('=');
            if (sep > 0) {
                values.put(line.substring(0, sep), line.substring(sep + 1));
            }
        }
    }

    public synchronized void save() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            lines.add(entry.getKey() + "=" + entry.getValue());
        }
        FileStore.writeLines(fileName, lines);
    }

    public synchronized String get(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public synchronized int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(values.getOrDefault(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public synchronized long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(values.getOrDefault(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public synchronized void set(String key, String value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }

    public synchronized void setInt(String key, int value) {
        values.put(key, String.valueOf(value));
    }

    public synchronized void setLong(String key, long value) {
        values.put(key, String.valueOf(value));
    }

    public synchronized void remove(String key) {
        values.remove(key);
    }

    public synchronized int addInt(String key, int delta) {
        int updated = Math.max(0, getInt(key, 0) + delta);
        setInt(key, updated);
        return updated;
    }

    public synchronized Map<String, String> getAll() {
        return new LinkedHashMap<>(values);
    }
}
