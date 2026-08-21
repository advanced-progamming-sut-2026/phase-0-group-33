package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum ZenGardenCommands {
    SHOW_GARDEN("^show\\s+garden$"),
    PLACE_PLANT("^place\\s+plant\\s+-t\\s+(?<type>.+?)\\s+-s\\s+(?<slot>\\d+)$"),
    WATER("^water\\s+plant\\s+-s\\s+(?<slot>\\d+)$"),
    TAKE_BACK("^take\\s+plant\\s+-s\\s+(?<slot>\\d+)$"),
    ENTER_GREENHOUSE("^enter\\s+greenhouse$");

    public final Pattern pattern;

    ZenGardenCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return input != null && pattern.matcher(input).matches();
    }
}
