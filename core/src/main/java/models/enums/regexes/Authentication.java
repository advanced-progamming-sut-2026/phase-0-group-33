package models.enums.regexes;

import java.util.regex.Pattern;

public enum Authentication {
    USERNAME("^[a-zA-Z0-9-]+$",
            "Username can only contain letters, digits, and hyphens."),
    PASSWORD("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
            + "(?=.*[!#$%^&*()=+}{\\[\\]|\\\\/:;'\",<>?])"
            + "[A-Za-z\\d!#$%^&*()=+}{\\[\\]|\\\\/:;'\",<>?]{8,}$",
            "Weak password: it must be at least 8 characters and contain a lowercase letter, "
                    + "an uppercase letter, a digit, and a special character."),
    EMAIL("^(?!.*\\.\\.)[A-Za-z0-9][A-Za-z0-9._-]*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$",
            "Invalid email format.");

    private final Pattern pattern;
    private final String errorMessage;

    Authentication(String regex, String errorMessage) {
        this.pattern = Pattern.compile(regex);
        this.errorMessage = errorMessage;
    }

    public boolean matches(String input) {
        if (this == EMAIL) {
            return pattern.matcher(input).matches() && localPartEndsWithAlphanumeric(input);
        }
        return pattern.matcher(input).matches();
    }

    private static boolean localPartEndsWithAlphanumeric(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return false;
        }
        char last = email.charAt(at - 1);
        return Character.isLetterOrDigit(last);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
