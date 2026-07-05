package models.enums.regexes.commandHandlers;

import java.util.regex.Pattern;

public enum ShopCommands {
    SHOP_LIST("^shop\\s+list$"),
    SHOP_DAILY("^shop\\s+daily$"),
    SHOP_BUY("^shop\\s+buy\\s+-i\\s+(?<itemId>\\S+)\\s+-n\\s+(?<count>\\d+)(?:\\s+-t\\s+(?<plantType>.+?))?$");

    public final Pattern pattern;

    ShopCommands(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }
}
