package utils;

import java.util.List;

public final class SessionStore {
    private static final String SESSION_FILE = "session.txt";
    private static final Storage DEVICE = new LocalStorage();

    private SessionStore() {
    }

    public static void saveSession(String username) {
        DEVICE.writeLines(SESSION_FILE, List.of(username));
    }

    public static String loadSession() {
        List<String> lines = DEVICE.readLines(SESSION_FILE);
        if (lines.isEmpty() || lines.get(0).isBlank()) {
            return null;
        }
        return lines.get(0).trim();
    }

    public static void clearSession() {
        DEVICE.delete(SESSION_FILE);
    }
}
