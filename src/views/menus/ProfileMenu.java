package views.menus;

import controllers.menuControllers.ProfileController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.ProfileCommands;
import models.user.User;
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
                .add(ProfileCommands.CHANGE_PASSWORD.pattern, matcher -> controller.handleChangePassword(
                        matcher.group("oldPassword"), matcher.group("newPassword")))
                .add(ProfileCommands.SHOW_PROFILE.pattern, matcher -> showProfile(controller))
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Profile menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    private Result showProfile(ProfileController controller) {
        Result result = controller.handleShowProfile();
        if (result.isSuccessfull() && result.getData() instanceof User) {
            printProfileInfo((User) result.getData());
        }
        return result;
    }

    private void printProfileInfo(User user) {
        int levels = user.getCompletedLevels().size();
        System.out.println("Your Profile:");
        System.out.printf("%-20s | %-20s | %s%n", "Username", "Nickname", "Number of Games");
        System.out.printf("%-20s | %-20s | %d%n",
                user.getUsername(), user.getNickname(), user.getNumberOfGames());
        System.out.printf("%-10s | %-10s | %-10s | %s%n", "Coins", "Diamonds", "Levels", "Miopoints");
        System.out.printf("%-10d | %-10d | %-10d | %d%n",
                user.getCoins().getAmount(), user.getDiamonds().getAmount(),
                levels, user.getHighestScore());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
