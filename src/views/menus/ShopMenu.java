package views.menus;

import controllers.menuControllers.ShopController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.ShopCommands;

import java.util.regex.Matcher;

public class ShopMenu implements AppMenu {
    private final ShopController controller;

    public ShopMenu(ShopController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher shopListMatcher = ShopCommands.SHOP_LIST.pattern.matcher(input);
        if (shopListMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher shopDailyMatcher = ShopCommands.SHOP_DAILY.pattern.matcher(input);
        if (shopDailyMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher shopBuyMatcher = ShopCommands.SHOP_BUY.pattern.matcher(input);
        if (shopBuyMatcher.matches()) {
            String itemId = shopBuyMatcher.group("itemId");
            String count = shopBuyMatcher.group("count");
            String plantType = shopBuyMatcher.group("plantType");
            // TODO
            return true;
        }

        Matcher changeMenuMatcher = GlobalCommands.CHANGE_MENU.pattern.matcher(input);
        if (changeMenuMatcher.matches()) {
            String menu = changeMenuMatcher.group("menu");
            // TODO
            return true;
        }

        Matcher showMenuMatcher = GlobalCommands.SHOW_MENU.pattern.matcher(input);
        if (showMenuMatcher.matches()) {
            Result result = new Result();
            result.setSuccess(true);
            result.addMessage("Shop menu");
            printResultMsg(result);
            return true;
        }

        Matcher exitMatcher = GlobalCommands.EXIT.pattern.matcher(input);
        if (exitMatcher.matches()) {
            // TODO
            return true;
        }

        return false;
    }
}