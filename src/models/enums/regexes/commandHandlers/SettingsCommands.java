package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum SettingsCommands {
    CHANGE_DIFFICULTY("^menu\\s+settings\\s+change-difficulty\\s+-l\\s+(?<level>\\d+)$");

    public final Pattern pattern;

    SettingsCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}