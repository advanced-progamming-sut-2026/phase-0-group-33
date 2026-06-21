package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum ProfileCommands {
    CHANGE_USERNAME("^menu\\s+profile\\s+change-username\\s+-u\\s+(?<username>.+?)$"),
    CHANGE_NICKNAME("^menu\\s+profile\\s+change-nickname\\s+-u\\s+(?<nickname>.+?)$"),
    CHANGE_EMAIL("^menu\\s+profile\\s+change-email\\s+-u\\s+(?<email>.+?)$"),
    CHANGE_PASSWORD("^menu\\s+profile\\s+change-password\\s+-p\\s+(?<new_password>.+?)\\s+-o\\s+(?<old_password>.+?)$"),
    SHOW_PROFILE("^menu\\s+profile\\s+show-info$");

    public final Pattern pattern;

    ProfileCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
