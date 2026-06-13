package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum GlobalCommands {
    EXIT("^menu\\s+exit$"),
    SHOW_MENU("^menu\\s+show\\s+current$"),
    CHANGE_MENU("^menu\\s+enter\\s+(?<menu>.+?)$"),
    ;

    public final Pattern pattern;

    GlobalCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
