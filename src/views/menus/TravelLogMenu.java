package views.menus;

import controllers.menuControllers.TravelLogController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.TravelLogCommands;

import java.util.regex.Matcher;

public class TravelLogMenu implements AppMenu {
    private final TravelLogController controller;

    public TravelLogMenu(TravelLogController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher travelLogPageMatcher = TravelLogCommands.TRAVEL_LOG_PAGE.pattern.matcher(input);
        if (travelLogPageMatcher.matches()) {
            String pageName = travelLogPageMatcher.group("pageName");
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
            result.addMessage("Travellog menu");
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