package views.menus;

import controllers.menuControllers.CollectionController;
import models.Result;
import models.enums.regexes.commandHandlers.CollectionCommands;
import models.enums.regexes.commandHandlers.GlobalCommands;

import java.util.regex.Matcher;

public class CollectionMenu implements AppMenu {
    private final CollectionController controller;

    public CollectionMenu(CollectionController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher showPlantsMatcher = CollectionCommands.SHOW_PLANTS.pattern.matcher(input);
        if (showPlantsMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showAllPlantsMatcher = CollectionCommands.SHOW_ALL_PLANTS.pattern.matcher(input);
        if (showAllPlantsMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showZombiesMatcher = CollectionCommands.SHOW_ZOMBIES.pattern.matcher(input);
        if (showZombiesMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showAllZombiesMatcher = CollectionCommands.SHOW_ALL_ZOMBIES.pattern.matcher(input);
        if (showAllZombiesMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showPlantMatcher = CollectionCommands.SHOW_PLANT.pattern.matcher(input);
        if (showPlantMatcher.matches()) {
            String plantName = showPlantMatcher.group("plantName");
            // TODO
            return true;
        }

        Matcher showZombieMatcher = CollectionCommands.SHOW_ZOMBIE.pattern.matcher(input);
        if (showZombieMatcher.matches()) {
            String zombieName = showZombieMatcher.group("zombieName");
            // TODO
            return true;
        }

        Matcher upgradePlantMatcher = CollectionCommands.UPGRADE_PLANT.pattern.matcher(input);
        if (upgradePlantMatcher.matches()) {
            String plantName = upgradePlantMatcher.group("plantName");
            // TODO
            return true;
        }

        Matcher purchasePlantMatcher = CollectionCommands.PURCHASE_PLANT.pattern.matcher(input);
        if (purchasePlantMatcher.matches()) {
            String plantName = purchasePlantMatcher.group("plantName");
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
            result.addMessage("Collection menu");
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