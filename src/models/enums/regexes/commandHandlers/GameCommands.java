package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum GameCommands {

    SHOW_ALL_PLANTS("^show\\s+all\\s+plants$"),
    SHOW_AVAILABLE_PLANTS("^show\\s+available\\s+plants$"),
    ADD_PLANT("^add\\s+plant\\s+-t\\s+(?<type>.+?)$"),
    REMOVE_PLANT("^remove\\s+plant\\s+-t\\s+(?<type>.+?)$"),
    BOOST_PLANT("^boost\\s+plant\\s+-t\\s+(?<type>.+?)$"),
    START_GAME("^start\\s+game$"),
    START_ZOMBIE_WAVES("^start\\s+zombie\\s+waves$"),

    ADVANCE_TIME("^advance\\s+time\\s+-t\\s+(?<count>\\d+)\\s+ticks$"),
    COLLECT_SUN("^collect\\s+sun\\s+-l\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    SHOW_SUN_AMOUNT("^show\\s+sun\\s+amount$"),
    CHEAT_ADD_SUN("^cheat\\s+add\\s+-n\\s+(?<count>\\d+)\\s+suns$"),
    RELEASE_NUKE("^release\\s+the\\s+nuke$"),
    PLANT_PLANT("^plant\\s+plant\\s+-t\\s+(?<type>.+?)\\s+-l\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    CHEAT_REMOVE_COOLDOWN("^cheat\\s+remove-cooldown$"),
    PLUCK_PLANT("^pluck\\s+plant\\s+-l\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    FEED_PLANT("^feed\\s+plant\\s+-l\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    CHEAT_ADD_PLANT_FOOD("^cheat\\s+add-plant-food$"),
    SHOW_MAP("^show\\s+map$"),
    SHOW_PLANTS_STATUS("^show\\s+plants\\s+status$"),
    SHOW_TILE_STATUS("^show\\s+tile\\s+status\\s+-l\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    ZOMBIES_INFO("^zombies\\s+info$"),
    CHEAT_SPAWN_ZOMBIE("^cheat\\s+spawn-zombie\\s+-t\\s+(?<type>.+?)\\s+-l\\s+(?<x>\\d+),\\s*(?<y>\\d+)$"),

    BREAK_VASE("^break\\s+vase\\s+-l\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    PLACE_ZOMBIE("^place\\s+zombie\\s+-t\\s+(?<type>.+?)\\s+-l\\s+\\((?<x>\\d+),\\s*(?<y>\\d+)\\)$"),
    SWAP_PLANTS("^swap\\s+-l\\s+\\((?<x1>\\d+),\\s*(?<y1>\\d+)\\)\\s+"
            + "\\((?<x2>\\d+),\\s*(?<y2>\\d+)\\)$"),
    BEGHOULED_UPGRADE("^upgrade\\s+-t\\s+(?<type>.+?)$");

    public final Pattern pattern;

    GameCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
