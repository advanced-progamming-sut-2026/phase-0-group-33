package utils;

import java.util.List;

public final class FileStore {

    private static volatile Storage backend = new LocalStorage();

    private FileStore() {
    }

    public static void useBackend(Storage storage) {
        backend = storage == null ? new LocalStorage() : storage;
    }

    public static Storage backend() {
        return backend;
    }

    public static List<String> readLines(String fileName) {
        return backend.readLines(fileName);
    }

    public static boolean writeLines(String fileName, List<String> lines) {
        return backend.writeLines(fileName, lines);
    }

    public static List<String> listFiles(String directory) {
        return backend.listFiles(directory);
    }

    public static void rename(String fromFile, String toFile) {
        backend.rename(fromFile, toFile);
    }

    public static void delete(String fileName) {
        backend.delete(fileName);
    }

    public static boolean exists(String fileName) {
        return backend.exists(fileName);
    }
}
