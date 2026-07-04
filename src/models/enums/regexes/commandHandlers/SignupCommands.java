package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum SignupCommands {
    NUMBER_PATTERN("^-?\\d+$"),
    REGISTER("^register\\s+-u\\s+(?<username>.+?)\\s+-p\\s+(?<password>.+?)"
            + "\\s+(?<passwordConfirm>.+?)\\s+-n\\s+(?<nickname>.+?)"
            + "\\s+-e\\s+(?<email>.+?)\\s+-g\\s+(?<gender>.+?)$"),
    SELECT_QUESTION("^pick\\s+question\\s+-q\\s+(?<number>.+?)\\s+-a\\s+(?<answer>.+?)"
            + "\\s+-c\\s+(?<answerConfirm>.+?)$");

    public final Pattern pattern;

    SignupCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}