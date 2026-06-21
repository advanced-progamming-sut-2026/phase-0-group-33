package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum CollectionCommands {
    SHOW_PLANTS("^menu\\s+collection\\s+show-plants$"),
    SHOW_ALL_PLANTS("^menu\\s+collection\\s+show-all-plants$"),
    SHOW_ZOMBIES("^menu\\s+collection\\s+show-zombies$"),
    SHOW_ALL_ZOMBIES("^menu\\s+collection\\s+show-all-zombies$"),
    SHOW_PLANT("^menu\\s+collection\\s+show-plant\\s+-p\\s+(?<plantName>.+?)$"),
    SHOW_ZOMBIE("^menu\\s+collection\\s+show-zombie\\s+-z\\s+(?<zombieName>.+?)$"),
    UPGRADE_PLANT("^menu\\s+collection\\s+upgrade-plant\\s+-p\\s+(?<plantName>.+?)$"),
    PURCHASE_PLANT("^menu\\s+collection\\s+purchase-plant\\s+-p\\s+(?<plantName>.+?)$");

    public final Pattern pattern;

    CollectionCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}