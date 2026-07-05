package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class FileStore {
    private static final Path DATA_DIR = Paths.get("data");

    private FileStore() {
    }

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

    public static void rename(String fromFile, String toFile) {
        try {
            Path from = DATA_DIR.resolve(fromFile);
            if (Files.exists(from)) {
                Files.move(from, DATA_DIR.resolve(toFile));
            }
        } catch (IOException e) {

        }
    }

    public static void delete(String fileName) {
        try {
            Files.deleteIfExists(DATA_DIR.resolve(fileName));
        } catch (IOException e) {

        }
    }

    public static boolean exists(String fileName) {
        return Files.exists(DATA_DIR.resolve(fileName));
    }
}
