package views.menus;

import controllers.menuControllers.NewsController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.NewsCommands;
import views.CommandRouter;

public class NewsMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public NewsMenu(NewsController controller) {
        router.add(NewsCommands.SHOW_UNREAD.pattern, matcher -> controller.handleShowUnread())
                .add(NewsCommands.SHOW_ALL.pattern, matcher -> controller.handleShowAll())
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("News menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
