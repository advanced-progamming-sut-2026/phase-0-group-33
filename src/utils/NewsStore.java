package utils;

import java.util.ArrayList;
import java.util.List;

public final class NewsStore {

    private NewsStore() {
    }

    private static String fileName(String username) {
        return "news_" + username + ".txt";
    }

    public static void add(String username, String message) {
        List<String> lines = FileStore.readLines(fileName(username));
        lines.add("0|" + message);
        FileStore.writeLines(fileName(username), lines);
    }

    public static int countUnread(String username) {
        int count = 0;
        for (String line : FileStore.readLines(fileName(username))) {
            if (line.startsWith("0|")) {
                count++;
            }
        }
        return count;
    }

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

    public static List<String> readAll(String username) {
        List<String> all = new ArrayList<>();
        List<String> updated = new ArrayList<>();
        boolean changed = false;
        for (String line : FileStore.readLines(fileName(username))) {
            int sep = line.indexOf('|');
            String message = sep >= 0 ? line.substring(sep + 1) : line;
            all.add(message);
            if (line.startsWith("0|")) {
                updated.add("1|" + message);
                changed = true;
            } else {
                updated.add(line);
            }
        }
        if (changed) {
            FileStore.writeLines(fileName(username), updated);
        }
        return all;
    }
}
