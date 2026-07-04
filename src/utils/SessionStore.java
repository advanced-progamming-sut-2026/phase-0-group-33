package utils;

import java.util.List;

/**
 * Persists the "stay logged in" session so the last user is restored
 * automatically when the program starts again.
 */
public final class SessionStore {
    private static final String SESSION_FILE = "session.txt";

    private SessionStore() {
    }

    /** Remembers the given user as permanently logged in. */
    public static void saveSession(String username) {
        FileStore.writeLines(SESSION_FILE, List.of(username));
    }

    /** Returns the remembered username, or null when no session is stored. */
    public static String loadSession() {
        List<String> lines = FileStore.readLines(SESSION_FILE);
        if (lines.isEmpty() || lines.get(0).isBlank()) {
            return null;
        }
        return lines.get(0).trim();
    }

    /** Forgets the stored session (logout or exit without stay-logged-in). */
    public static void clearSession() {
        FileStore.delete(SESSION_FILE);
    }
}
