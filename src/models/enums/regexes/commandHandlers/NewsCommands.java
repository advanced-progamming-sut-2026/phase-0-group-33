package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum NewsCommands {
    SHOW_UNREAD("^menu\\s+news\\s+show-unread$"),
    SHOW_ALL("^menu\\s+news\\s+show-all$");

    public final Pattern pattern;

    NewsCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
