package views.menus;

import controllers.menuControllers.GreenhouseController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.GreenhouseCommands;
import views.CommandRouter;

public class GreenhouseMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public GreenhouseMenu(GreenhouseController controller) {
        router.add(GreenhouseCommands.SHOW_GREENHOUSE.pattern,
                        matcher -> controller.handleShowGreenhouse())
                .add(GreenhouseCommands.PLANT_POT.pattern, matcher -> controller.handlePlantPot(
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GreenhouseCommands.COLLECT.pattern, matcher -> controller.handleCollect(
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GreenhouseCommands.GROW.pattern, matcher -> controller.handleGrow(
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GreenhouseCommands.ENTER_SHOP.pattern, matcher -> controller.handleEnterShop())
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Greenhouse menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
