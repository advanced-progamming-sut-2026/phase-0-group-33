package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum GreenhouseCommands {
    SHOW_GREENHOUSE("^show\\s+greenhouse$"),
    PLANT_POT("^plant\\s+pot\\s+at\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    COLLECT("^collect\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    GROW("^grow\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    ENTER_SHOP("^enter\\s+shop$");

    public final Pattern pattern;

    GreenhouseCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
