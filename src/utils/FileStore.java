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
 * All persistent state lives here: user accounts, the stay-logged-in
 * session, per-user progress, greenhouse pots and the news feed.
 */
public final class FileStore {
    private static final Path DATA_DIR = Paths.get("data");

    private FileStore() {
    }

    /**
     * Reads all lines of the given data file, or an empty list if it does not
     * exist.
     */
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

    /**
     * Writes all lines to the given data file, creating parent directories if
     * needed.
     */
    public static boolean writeLines(String fileName, List<String> lines) {
        try {
            Path target = DATA_DIR.resolve(fileName);
            Files.createDirectories(target.getParent());
            Files.write(target, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Lists the plain file names inside a sub-directory of the data folder. */
    public static List<String> listFiles(String directory) {
        List<String> names = new ArrayList<>();
        Path dir = DATA_DIR.resolve(directory);
        if (!Files.isDirectory(dir)) {
            return names;
        }
        try (var stream = Files.list(dir)) {
            stream.forEach(path -> names.add(path.getFileName().toString()));
        } catch (IOException e) {
            return names;
        }
        return names;
    }

    /** Renames a data file (used when a username changes). */
    public static void rename(String fromFile, String toFile) {
        try {
            Path from = DATA_DIR.resolve(fromFile);
            if (Files.exists(from)) {
                Files.move(from, DATA_DIR.resolve(toFile));
            }
        } catch (IOException e) {
            // Best effort; the old file simply stays behind.
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
