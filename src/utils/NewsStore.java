package utils;

import java.util.ArrayList;
import java.util.List;

public final class NewsStore {

    private NewsStore() {
    }

    private static String fileName(String username) {
        return "news_" + username + ".txt";
    }

    /** Appends a new unread news entry for the user. */
    public static void add(String username, String message) {
        List<String> lines = FileStore.readLines(fileName(username));
        lines.add("0|" + message);
        FileStore.writeLines(fileName(username), lines);
    }

    /** Returns unread news and marks all of it as read. */
    public static List<String> readUnread(String username) {
        List<String> lines = FileStore.readLines(fileName(username));
        List<String> unread = new ArrayList<>();
        List<String> updated = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("0|")) {
                unread.add(line.substring(2));
                updated.add("1|" + line.substring(2));
            } else {
                updated.add(line);
            }
        }
        FileStore.writeLines(fileName(username), updated);
        return unread;
    }

    /** Returns every news entry, oldest first. */
    public static List<String> readAll(String username) {
        List<String> all = new ArrayList<>();
        for (String line : FileStore.readLines(fileName(username))) {
            int sep = line.indexOf('|');
            all.add(sep >= 0 ? line.substring(sep + 1) : line);
        }
        return all;
    }
}
