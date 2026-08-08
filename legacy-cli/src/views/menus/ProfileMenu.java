package views.menus;

import controllers.menuControllers.ProfileController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.ProfileCommands;
import views.CommandRouter;

public class ProfileMenu implements AppMenu {
        private final CommandRouter router = new CommandRouter();

        public ProfileMenu(ProfileController controller) {
                router.add(ProfileCommands.CHANGE_USERNAME.pattern,
                                matcher -> controller.handleChangeUsername(matcher.group("username")))
                                .add(ProfileCommands.CHANGE_NICKNAME.pattern,
                                                matcher -> controller.handleChangeNickname(matcher.group("nickname")))
                                .add(ProfileCommands.CHANGE_EMAIL.pattern,
                                                matcher -> controller.handleChangeEmail(matcher.group("email")))
                                .add(ProfileCommands.CHANGE_PASSWORD.pattern,
                                                matcher -> controller.handleChangePassword(
                                                                matcher.group("oldPassword"),
                                                                matcher.group("newPassword")))
                                .add(ProfileCommands.SHOW_PROFILE.pattern, matcher -> controller.handleShowProfile())
                                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Profile menu"))
                                .add(GlobalCommands.CHANGE_MENU.pattern,
                                                matcher -> controller.handleMenuChange(matcher.group("menu")))
                                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
        }

        @Override
        public boolean processCommand(String cmd) {
                return router.dispatch(cmd.trim());
        }
}
