package views.menus;

import controllers.menuControllers.LeaderboardController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.LeaderboardCommands;
import views.CommandRouter;

public class LeaderboardMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public LeaderboardMenu(LeaderboardController controller) {
        router.add(LeaderboardCommands.SHOW_LEADERBOARD.pattern,
                        matcher -> controller.handleShowLeaderboard(
                                matcher.group("column"), matcher.group("order")))
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Leaderboard menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
