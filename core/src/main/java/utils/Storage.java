package utils;

import java.util.List;

public interface Storage {

    List<String> readLines(String fileName);

    boolean writeLines(String fileName, List<String> lines);

    List<String> listFiles(String directory);

    void rename(String fromFile, String toFile);

    void delete(String fileName);

    boolean exists(String fileName);
}
