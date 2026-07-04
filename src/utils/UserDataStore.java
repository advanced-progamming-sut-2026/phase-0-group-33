package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-user key/value storage 
 * Holds local state the shared database schema has no columns for:
 * chapter progress, seed packets, plant boosts, plant foods and greenhouse pots.
 * Keys and values must not contain the '=' separator or line breaks.
 */
public class UserDataStore {
    private final String fileName;
    private final Map<String, String> values = new LinkedHashMap<>();

    public UserDataStore(String username) {
        this.fileName = "user_" + username + ".properties";
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

    /** Persists the current state to disk. */
    public void save() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            lines.add(entry.getKey() + "=" + entry.getValue());
        }
        FileStore.writeLines(fileName, lines);
    }

    public String get(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(values.getOrDefault(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(values.getOrDefault(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void set(String key, String value) {
        values.put(key, value);
    }

    public void setInt(String key, int value) {
        values.put(key, String.valueOf(value));
    }

    public void setLong(String key, long value) {
        values.put(key, String.valueOf(value));
    }

    public void remove(String key) {
        values.remove(key);
    }

    /** Adds a delta to an integer value, clamping at zero, and returns the new value. */
    public int addInt(String key, int delta) {
        int updated = Math.max(0, getInt(key, 0) + delta);
        setInt(key, updated);
        return updated;
    }

    public Map<String, String> getAll() {
        return new LinkedHashMap<>(values);
    }
}
