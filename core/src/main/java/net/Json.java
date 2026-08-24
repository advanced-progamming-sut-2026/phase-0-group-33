package net;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {

    private Json() {
    }

    static void writeString(StringBuilder text, String value) {
        text.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    text.append("\\\"");
                    break;
                case '\\':
                    text.append("\\\\");
                    break;
                case '\n':
                    text.append("\\n");
                    break;
                case '\r':
                    text.append("\\r");
                    break;
                case '\t':
                    text.append("\\t");
                    break;
                default:
                    appendPlain(text, c);
                    break;
            }
        }
        text.append('"');
    }

    private static void appendPlain(StringBuilder text, char c) {
        if (c < 0x20) {
            text.append(String.format("\\u%04x", (int) c));
        } else {
            text.append(c);
        }
    }

    @SuppressWarnings("unchecked")
    static void writeValue(StringBuilder text, Object value) {
        if (value == null) {
            text.append("null");
        } else if (value instanceof String) {
            writeString(text, (String) value);
        } else if (value instanceof Boolean) {
            text.append(value.toString());
        } else if (value instanceof Number) {
            writeNumber(text, (Number) value);
        } else if (value instanceof Map) {
            writeMap(text, (Map<String, Object>) value);
        } else if (value instanceof Iterable) {
            writeList(text, (Iterable<Object>) value);
        } else {
            writeString(text, String.valueOf(value));
        }
    }

    private static void writeNumber(StringBuilder text, Number value) {
        double raw = value.doubleValue();
        if (Double.isNaN(raw) || Double.isInfinite(raw)) {
            text.append('0');
        } else {
            text.append(value);
        }
    }

    private static void writeMap(StringBuilder text, Map<String, Object> value) {
        text.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            if (!first) {
                text.append(',');
            }
            first = false;
            writeString(text, entry.getKey());
            text.append(':');
            writeValue(text, entry.getValue());
        }
        text.append('}');
    }

    private static void writeList(StringBuilder text, Iterable<Object> value) {
        text.append('[');
        boolean first = true;
        for (Object item : value) {
            if (!first) {
                text.append(',');
            }
            first = false;
            writeValue(text, item);
        }
        text.append(']');
    }

    static Map<String, Object> readObject(JsonValue node) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (JsonValue child = node.child; child != null; child = child.next) {
            map.put(child.name, readValue(child));
        }
        return map;
    }

    private static List<Object> readArray(JsonValue node) {
        List<Object> items = new ArrayList<>();
        for (JsonValue child = node.child; child != null; child = child.next) {
            items.add(readValue(child));
        }
        return items;
    }

    private static Object readValue(JsonValue node) {
        if (node.isObject()) {
            return readObject(node);
        }
        if (node.isArray()) {
            return readArray(node);
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isNull()) {
            return null;
        }
        if (node.isDouble() || node.isLong()) {
            return readNumber(node);
        }
        return node.asString();
    }

    private static Object readNumber(JsonValue node) {
        double raw = node.asDouble();
        if (raw == Math.floor(raw) && !Double.isInfinite(raw) && Math.abs(raw) < Long.MAX_VALUE) {
            return node.asLong();
        }
        return raw;
    }
}
