package views.menus;

import controllers.menuControllers.NewsController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.NewsCommands;

import java.util.regex.Matcher;

public class NewsMenu implements AppMenu {
    private final NewsController controller;

    public NewsMenu(NewsController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher showUnreadMatcher = NewsCommands.SHOW_UNREAD.pattern.matcher(input);
        if (showUnreadMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showAllMatcher = NewsCommands.SHOW_ALL.pattern.matcher(input);
        if (showAllMatcher.matches()) {
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
            result.addMessage("News menu");
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