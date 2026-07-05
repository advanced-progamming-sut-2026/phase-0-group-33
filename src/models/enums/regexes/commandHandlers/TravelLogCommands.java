package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum TravelLogCommands {
    TRAVEL_LOG_PAGE("^travel\\s+log\\s+page\\s+(?<pageName>.+?)$"),
    PLAY_MINIGAME("^play\\s+minigame\\s+-n\\s+(?<name>.+?)\\s+-d\\s+(?<difficulty>[1-3])$");

    public final Pattern pattern;

    TravelLogCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
