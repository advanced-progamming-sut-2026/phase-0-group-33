package net;

public final class Rows {

    private static final double SCALE = 1000.0;

    private final StringBuilder text = new StringBuilder();
    private final String[] parts;
    private int cursor;

    private Rows(String[] parts) {
        this.parts = parts;
    }

    public static Rows writer() {
        return new Rows(null);
    }

    public static Rows reader(String row) {
        return new Rows(row.split(";", -1));
    }

    public Rows put(int value) {
        if (text.length() > 0) {
            text.append(';');
        }
        text.append(value);
        return this;
    }

    public Rows put(long value) {
        if (text.length() > 0) {
            text.append(';');
        }
        text.append(value);
        return this;
    }

    public Rows put(boolean value) {
        return put(value ? 1 : 0);
    }

    public Rows put(double value) {
        return put(Math.round(value * SCALE));
    }

    public int nextInt() {
        String part = next();
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public long nextLong() {
        String part = next();
        try {
            return Long.parseLong(part);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public double nextDouble() {
        return nextLong() / SCALE;
    }

    public boolean nextFlag() {
        return nextInt() != 0;
    }

    private String next() {
        if (parts == null || cursor >= parts.length) {
            return "0";
        }
        String part = parts[cursor];
        cursor++;
        return part.isEmpty() ? "0" : part;
    }

    @Override
    public String toString() {
        return text.toString();
    }
}
