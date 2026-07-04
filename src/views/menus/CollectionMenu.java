package views.menus;

import controllers.menuControllers.CollectionController;
import models.Result;
import models.enums.regexes.commandHandlers.CollectionCommands;
import models.enums.regexes.commandHandlers.GlobalCommands;
import views.CommandRouter;

public class CollectionMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public CollectionMenu(CollectionController controller) {
        router.add(CollectionCommands.SHOW_PLANTS.pattern, matcher -> controller.handleShowPlants())
                .add(CollectionCommands.SHOW_ALL_PLANTS.pattern,
                        matcher -> controller.handleShowAllPlants())
                .add(CollectionCommands.SHOW_ZOMBIES.pattern, matcher -> controller.handleShowZombies())
                .add(CollectionCommands.SHOW_ALL_ZOMBIES.pattern,
                        matcher -> controller.handleShowAllZombies())
                .add(CollectionCommands.SHOW_PLANT.pattern,
                        matcher -> controller.handleShowPlant(matcher.group("plantName")))
                .add(CollectionCommands.SHOW_ZOMBIE.pattern,
                        matcher -> controller.handleShowZombie(matcher.group("zombieName")))
                .add(CollectionCommands.UPGRADE_PLANT.pattern,
                        matcher -> controller.handleUpgradePlant(matcher.group("plantName")))
                .add(CollectionCommands.PURCHASE_PLANT.pattern,
                        matcher -> controller.handlePurchasePlant(matcher.group("plantName")))
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Collection menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
