package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal line-based file storage under the local {@code data/} directory.
 * Used for state that must survive a restart but does not belong in the
 * shared MySQL schema (session, per-user greenhouse, news, progress).
 */
public final class FileStore {
    private static final Path DATA_DIR = Paths.get("data");

    private FileStore() {
    }

    /** Reads all lines of the given data file, or an empty list if it does not exist. */
    public static List<String> readLines(String fileName) {
        Path path = DATA_DIR.resolve(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** Writes all lines to the given data file, creating the data directory if needed. */
    public static boolean writeLines(String fileName, List<String> lines) {
        try {
            Files.createDirectories(DATA_DIR);
            Files.write(DATA_DIR.resolve(fileName), lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Deletes the given data file if it exists. */
    public static void delete(String fileName) {
        try {
            Files.deleteIfExists(DATA_DIR.resolve(fileName));
        } catch (IOException e) {
            // Nothing sensible to do; the file will be overwritten next save.
        }
    }

    /** Returns whether the given data file exists. */
    public static boolean exists(String fileName) {
        return Files.exists(DATA_DIR.resolve(fileName));
    }
}
