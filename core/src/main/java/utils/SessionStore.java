package utils;

import java.util.List;

public final class SessionStore {
    private static final String SESSION_FILE = "session.txt";

    private SessionStore() {
    }

    public static void saveSession(String username) {
        FileStore.writeLines(SESSION_FILE, List.of(username));
    }

    public static String loadSession() {
        List<String> lines = FileStore.readLines(SESSION_FILE);
        if (lines.isEmpty() || lines.get(0).isBlank()) {
            return null;
        }
        return lines.get(0).trim();
    }

    public static void clearSession() {
        FileStore.delete(SESSION_FILE);
    }
}
