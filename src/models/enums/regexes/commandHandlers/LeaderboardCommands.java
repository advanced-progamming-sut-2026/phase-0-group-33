package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum LeaderboardCommands {
    SHOW_LEADERBOARD("^show\\s+leaderboard(?:\\s+-s\\s+(?<column>\\S+))?$");

    public final Pattern pattern;

    LeaderboardCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
