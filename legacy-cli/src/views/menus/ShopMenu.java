package views.menus;

import controllers.menuControllers.ShopController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.ShopCommands;
import views.CommandRouter;

public class ShopMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public ShopMenu(ShopController controller) {
        router.add(ShopCommands.SHOP_LIST.pattern, matcher -> controller.handleShopList())
                .add(ShopCommands.SHOP_DAILY.pattern, matcher -> controller.handleShopDaily())
                .add(ShopCommands.SHOP_BUY.pattern, matcher -> controller.handleShopBuy(
                        matcher.group("itemId"), Integer.parseInt(matcher.group("count")),
                        matcher.group("plantType")))
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Shop menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
