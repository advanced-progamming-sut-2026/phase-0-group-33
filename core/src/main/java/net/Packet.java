package net;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Packet {

    private final String type;
    private final Map<String, Object> body;

    private Packet(String type, Map<String, Object> body) {
        this.type = type;
        this.body = body;
    }

    public static Packet of(String type) {
        return new Packet(type, new LinkedHashMap<>());
    }

    public Packet put(String key, Object value) {
        body.put(key, value);
        return this;
    }

    public String type() {
        return type;
    }

    public String str(String key, String fallback) {
        Object value = body.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    public String str(String key) {
        return str(key, "");
    }

    public int num(String key, int fallback) {
        Object value = body.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public long big(String key, long fallback) {
        Object value = body.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean flag(String key, boolean fallback) {
        Object value = body.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    public boolean has(String key) {
        return body.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public List<String> list(String key) {
        Object value = body.get(key);
        List<String> items = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<Object>) value) {
                items.add(item == null ? "" : String.valueOf(item));
            }
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> maps(String key) {
        Object value = body.get(key);
        List<Map<String, Object>> items = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<Object>) value) {
                if (item instanceof Map) {
                    items.add((Map<String, Object>) item);
                }
            }
        }
        return items;
    }

    public String encode() {
        StringBuilder text = new StringBuilder();
        text.append('{');
        Json.writeString(text, "t");
        text.append(':');
        Json.writeString(text, type);
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            text.append(',');
            Json.writeString(text, entry.getKey());
            text.append(':');
            Json.writeValue(text, entry.getValue());
        }
        text.append('}');
        return text.toString();
    }

    public static Packet decode(String line) {
        JsonValue root = new JsonReader().parse(line);
        if (root == null || !root.isObject()) {
            return null;
        }
        Map<String, Object> body = Json.readObject(root);
        Object type = body.remove("t");
        return new Packet(type == null ? "" : String.valueOf(type), body);
    }
}
