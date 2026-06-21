package views.menus;

import controllers.menuControllers.SettingsController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.SettingsCommands;

import java.util.regex.Matcher;

public class SettingsMenu implements AppMenu {
    private final SettingsController controller;

    public SettingsMenu(SettingsController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher changeDifficultyMatcher = SettingsCommands.CHANGE_DIFFICULTY.pattern.matcher(input);
        if (changeDifficultyMatcher.matches()) {
            String level = changeDifficultyMatcher.group("level");
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
            result.addMessage("Settings menu");
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