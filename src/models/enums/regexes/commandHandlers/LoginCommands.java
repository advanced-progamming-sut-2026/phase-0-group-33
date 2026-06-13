package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum LoginCommands {
    LOGIN("^login\\s+-u\\s+(?<username>.+?)\\s+-p\\s+(?<password>.+?)\\s+-(?<stay>.+?)?$"),
    FORGET_PASSWORD("^forget\\s+password\\s+-u\\s+(?<username>.+?)\\s+-e\\s+(?<email>.+?)$"),
    ANSWER("^answer\\s+-a\\s+(?<answer>.+?)$"),
    NEW_PASSWORD("^new\\s+password\\s+-p(?<password>.+?)\\s+(?<password-confirm>.+?)$"),
    QUIT_PASSWORD_RESET("^quit\\s+password\\s+reset$")
    ;
    public final Pattern pattern;

    LoginCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
