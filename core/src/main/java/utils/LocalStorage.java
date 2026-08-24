package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class LocalStorage implements Storage {

    private final Path root;

    public LocalStorage() {
        this(Paths.get("data"));
    }

    public LocalStorage(Path root) {
        this.root = root;
    }

    @Override
    public List<String> readLines(String fileName) {
        Path path = root.resolve(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean writeLines(String fileName, List<String> lines) {
        try {
            Path target = root.resolve(fileName);
            Files.createDirectories(target.getParent());
            Files.write(target, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<String> listFiles(String directory) {
        List<String> names = new ArrayList<>();
        Path dir = root.resolve(directory);
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

    @Override
    public void rename(String fromFile, String toFile) {
        try {
            Path from = root.resolve(fromFile);
            if (Files.exists(from)) {
                Files.move(from, root.resolve(toFile));
            }
        } catch (IOException e) {
            return;
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            Files.deleteIfExists(root.resolve(fileName));
        } catch (IOException e) {
            return;
        }
    }

    @Override
    public boolean exists(String fileName) {
        return Files.exists(root.resolve(fileName));
    }
}
