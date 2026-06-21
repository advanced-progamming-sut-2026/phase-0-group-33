package views.menus;

import controllers.menuControllers.GreenhouseController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.GreenhouseCommands;

import java.util.regex.Matcher;

public class GreenhouseMenu implements AppMenu {
    private final GreenhouseController controller;

    public GreenhouseMenu(GreenhouseController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher showGreenhouseMatcher = GreenhouseCommands.SHOW_GREENHOUSE.pattern.matcher(input);
        if (showGreenhouseMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher plantPotMatcher = GreenhouseCommands.PLANT_POT.pattern.matcher(input);
        if (plantPotMatcher.matches()) {
            String x = plantPotMatcher.group("x");
            String y = plantPotMatcher.group("y");
            // TODO
            return true;
        }

        Matcher collectMatcher = GreenhouseCommands.COLLECT.pattern.matcher(input);
        if (collectMatcher.matches()) {
            String x = collectMatcher.group("x");
            String y = collectMatcher.group("y");
            // TODO
            return true;
        }

        Matcher growMatcher = GreenhouseCommands.GROW.pattern.matcher(input);
        if (growMatcher.matches()) {
            String x = growMatcher.group("x");
            String y = growMatcher.group("y");
            // TODO
            return true;
        }

        Matcher enterShopMatcher = GreenhouseCommands.ENTER_SHOP.pattern.matcher(input);
        if (enterShopMatcher.matches()) {
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
            result.addMessage("Greenhouse menu");
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