package views.menus;

import controllers.menuControllers.MainController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.MainCommands;

import java.util.regex.Matcher;

public class MainMenu implements AppMenu {
    private final MainController controller;

    public MainMenu(MainController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher enterChapterMatcher = MainCommands.ENTER_CHAPTER.pattern.matcher(input);
        if (enterChapterMatcher.matches()) {
            String chaptername = enterChapterMatcher.group("chaptername");
            // TODO
            return true;
        }

        Matcher enterGreenhouseMatcher = MainCommands.ENTER_GREENHOUSE.pattern.matcher(input);
        if (enterGreenhouseMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterTravelLogMatcher = MainCommands.ENTER_TRAVEL_LOG.pattern.matcher(input);
        if (enterTravelLogMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterLeaderboardMatcher = MainCommands.ENTER_LEADERBOARD.pattern.matcher(input);
        if (enterLeaderboardMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterCoinWalletMatcher = MainCommands.ENTER_COIN_WALLET.pattern.matcher(input);
        if (enterCoinWalletMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterGemWalletMatcher = MainCommands.ENTER_GEM_WALLET.pattern.matcher(input);
        if (enterGemWalletMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher cheatAddMatcher = MainCommands.CHEAT_ADD.pattern.matcher(input);
        if (cheatAddMatcher.matches()) {
            String amount = cheatAddMatcher.group("amount");
            String type = cheatAddMatcher.group("type");
            // TODO
            return true;
        }

        Matcher logoutMatcher = MainCommands.LOGOUT.pattern.matcher(input);
        if (logoutMatcher.matches()) {
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
            result.addMessage("Main menu");
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