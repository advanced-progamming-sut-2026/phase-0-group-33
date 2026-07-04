package views.menus;

import controllers.menuControllers.SettingsController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.SettingsCommands;
import views.CommandRouter;

public class SettingsMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public SettingsMenu(SettingsController controller) {
        router.add(SettingsCommands.CHANGE_DIFFICULTY.pattern,
                        matcher -> controller.handleChangeDifficulty(matcher.group("level")))
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Settings menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
