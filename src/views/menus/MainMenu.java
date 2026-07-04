package views.menus;

import controllers.menuControllers.MainController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.MainCommands;
import views.CommandRouter;

public class MainMenu implements AppMenu {
        private final CommandRouter router = new CommandRouter();

        public MainMenu(MainController controller) {
                router.add(MainCommands.ENTER_CHAPTER.pattern,
                                matcher -> controller.handleEnterChapter(matcher.group("chaptername")))
                                .add(MainCommands.ENTER_GREENHOUSE.pattern,
                                                matcher -> controller.handleMenuChange("greenhouse"))
                                .add(MainCommands.ENTER_TRAVEL_LOG.pattern,
                                                matcher -> controller.handleMenuChange("travel-log"))
                                .add(MainCommands.ENTER_LEADERBOARD.pattern,
                                                matcher -> controller.handleMenuChange("leaderboard"))
                                .add(MainCommands.ENTER_COIN_WALLET.pattern,
                                                matcher -> controller.handleShowCoinWallet())
                                .add(MainCommands.ENTER_GEM_WALLET.pattern,
                                                matcher -> controller.handleShowGemWallet())
                                .add(MainCommands.CHEAT_ADD.pattern, matcher -> controller.handleCheatAdd(
                                                Integer.parseInt(matcher.group("amount")), matcher.group("type")))
                                .add(MainCommands.SCORING_GAME.pattern, matcher -> controller.handleScoringGame())
                                .add(MainCommands.LOGOUT.pattern, matcher -> controller.handleLogout())
                                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Main menu"))
                                .add(GlobalCommands.CHANGE_MENU.pattern,
                                                matcher -> controller.handleMenuChange(matcher.group("menu")))
                                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
        }

        @Override
        public boolean processCommand(String cmd) {
                return router.dispatch(cmd.trim());
        }
}
