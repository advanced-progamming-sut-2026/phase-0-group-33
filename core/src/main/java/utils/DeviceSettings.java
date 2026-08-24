package utils;

import net.Protocol;

import java.util.ArrayList;
import java.util.List;

public final class DeviceSettings {

    private static final String FILE = "device.properties";
    private static final Storage DEVICE = new LocalStorage();

    private DeviceSettings() {
    }

    private static String read(String key, String fallback) {
        for (String line : DEVICE.readLines(FILE)) {
            int sep = line.indexOf('=');
            if (sep > 0 && line.substring(0, sep).equals(key)) {
                return line.substring(sep + 1);
            }
        }
        return fallback;
    }

    private static void write(String key, String value) {
        List<String> lines = new ArrayList<>();
        boolean replaced = false;
        for (String line : DEVICE.readLines(FILE)) {
            int sep = line.indexOf('=');
            if (sep > 0 && line.substring(0, sep).equals(key)) {
                lines.add(key + "=" + value);
                replaced = true;
            } else {
                lines.add(line);
            }
        }
        if (!replaced) {
            lines.add(key + "=" + value);
        }
        DEVICE.writeLines(FILE, lines);
    }

    public static String serverHost() {
        return read("serverHost", Protocol.DEFAULT_HOST);
    }

    public static int serverPort() {
        try {
            return Integer.parseInt(read("serverPort", String.valueOf(Protocol.DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            return Protocol.DEFAULT_PORT;
        }
    }

    public static void setServer(String host, int port) {
        write("serverHost", host == null || host.isBlank() ? Protocol.DEFAULT_HOST : host.trim());
        write("serverPort", String.valueOf(port));
    }

    public static void setResume(String username, String token) {
        write("resumeUser", username == null ? "" : username);
        write("resumeToken", token == null ? "" : token);
    }

    public static void clearResume() {
        write("resumeUser", "");
        write("resumeToken", "");
    }

    public static String resumeUser() {
        return read("resumeUser", "");
    }

    public static String resumeToken() {
        return read("resumeToken", "");
    }
}
