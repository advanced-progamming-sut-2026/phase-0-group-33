package views.menus;

import controllers.menuControllers.MainMenuController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.MainMenuCommands;

import java.util.regex.Matcher;

public class MainMenu implements AppMenu {
    private final MainMenuController controller;

    public MainMenu(MainMenuController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher enterChapterMatcher = MainMenuCommands.ENTER_CHAPTER.pattern.matcher(input);
        if (enterChapterMatcher.matches()) {
            String chaptername = enterChapterMatcher.group("chaptername");
            // TODO
            return true;
        }

        Matcher enterGreenhouseMatcher = MainMenuCommands.ENTER_GREENHOUSE.pattern.matcher(input);
        if (enterGreenhouseMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterTravelLogMatcher = MainMenuCommands.ENTER_TRAVEL_LOG.pattern.matcher(input);
        if (enterTravelLogMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterLeaderboardMatcher = MainMenuCommands.ENTER_LEADERBOARD.pattern.matcher(input);
        if (enterLeaderboardMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterCoinWalletMatcher = MainMenuCommands.ENTER_COIN_WALLET.pattern.matcher(input);
        if (enterCoinWalletMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher enterGemWalletMatcher = MainMenuCommands.ENTER_GEM_WALLET.pattern.matcher(input);
        if (enterGemWalletMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher cheatAddMatcher = MainMenuCommands.CHEAT_ADD.pattern.matcher(input);
        if (cheatAddMatcher.matches()) {
            String amount = cheatAddMatcher.group("amount");
            String type = cheatAddMatcher.group("type");
            // TODO
            return true;
        }

        Matcher logoutMatcher = MainMenuCommands.LOGOUT.pattern.matcher(input);
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