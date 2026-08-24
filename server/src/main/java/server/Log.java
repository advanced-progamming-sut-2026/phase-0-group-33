package server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Log {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Log() {
    }

    public static void say(String message) {
        System.out.println(LocalTime.now().format(CLOCK) + "  " + message);
    }

    public static void warn(String message) {
        System.out.println(LocalTime.now().format(CLOCK) + "  ! " + message);
    }
}
