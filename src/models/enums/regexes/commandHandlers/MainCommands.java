package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum MainCommands {
    ENTER_CHAPTER("^menu\\s+enter\\s+chapter\\s+-c\\s+(?<chaptername>.+?)$"),
    ENTER_GREENHOUSE("^menu\\s+greenhouse$"),
    ENTER_TRAVEL_LOG("^menu\\s+travel-log$"),
    ENTER_LEADERBOARD("^menu\\s+leaderboard$"),
    ENTER_COIN_WALLET("^menu\\s+coin-wallet$"),
    ENTER_GEM_WALLET("^menu\\s+gem-wallet$"),
    CHEAT_ADD("^menu\\s+cheat\\s+add\\s+(?<amount>\\d+)\\s+(?<type>coin|diamond)$"),
    LOGOUT("^menu\\s+logout$");

    public final Pattern pattern;

    MainCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}