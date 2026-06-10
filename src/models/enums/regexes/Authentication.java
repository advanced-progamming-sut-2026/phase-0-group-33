package models.enums.regexes;

import java.util.regex.Pattern;

public enum Authentication {
    USERNAME("^[a-zA-Z0-9-]+$", "Username can only contain letters, digits, and hyphens."),
    PASSWORD("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[?><,'\";:\\\\|/\\[\\]{}()*&^%$#!@])[A-Za-z\\d?><,'\";:\\\\|/\\[\\]{}()*&^%$#!@]{8,}$", "Password must be at least 8 characters, with upper, lower, digit, and special char."),
    EMAIL("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", "Invalid email format.");

    private final Pattern pattern;
    private final String errorMessage;

    Authentication(String regex, String errorMessage) {
        this.pattern = Pattern.compile(regex);
        this.errorMessage = errorMessage;
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}