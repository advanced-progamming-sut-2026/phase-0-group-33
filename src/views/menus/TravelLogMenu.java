package views.menus;

import controllers.menuControllers.TravelLogController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.TravelLogCommands;
import views.CommandRouter;

public class TravelLogMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public TravelLogMenu(TravelLogController controller) {
        router.add(TravelLogCommands.TRAVEL_LOG_PAGE.pattern,
                        matcher -> controller.handleShowPage(matcher.group("pageName")))
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Travel log menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
