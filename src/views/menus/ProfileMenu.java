package views.menus;

import controllers.menuControllers.ProfileController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.ProfileCommands;
import models.user.User;

import java.util.regex.Matcher;

public class ProfileMenu implements AppMenu {
    private final ProfileController controller;

    public ProfileMenu(ProfileController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        // TODO
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();
        Matcher m;
        Result result = getResult();

        if ((m = ProfileCommands.CHANGE_USERNAME.pattern.matcher(input)).matches()) {
            String username = m.group("username");
            result = controller.handleChangeUsername(username);
        } else if ((m = ProfileCommands.CHANGE_NICKNAME.pattern.matcher(input)).matches()) {
            String nickname = m.group("nickname");
            result = controller.handleChangeNickname(nickname);
        } else if ((m = ProfileCommands.CHANGE_EMAIL.pattern.matcher(input)).matches()) {
            String email = m.group("email");
            result = controller.handleChangeEmail(email);
        } else if ((m = ProfileCommands.CHANGE_PASSWORD.pattern.matcher(input)).matches()) {
            String password = m.group("new_password");
            String oldPassword = m.group("old_password");
            result = controller.handleChangePassword(oldPassword, password);
        } else if ((m = ProfileCommands.SHOW_PROFILE.pattern.matcher(input)).matches()) {
            result = controller.handleShowProfile();
            printProfileInfo((User) result.getData());
        }  else if ((m = GlobalCommands.CHANGE_MENU.pattern.matcher(input)).matches()) {
            String menu = m.group("menu");
            result = controller.handleMenuChange(menu);
        } else if ((m = GlobalCommands.SHOW_MENU.pattern.matcher(input)).matches()) {
            result.setSuccess(true);
            result.addMessage("Profile menu");
        } else if (((m = GlobalCommands.EXIT.pattern.matcher(input)).matches())) {
            result = controller.handleExit();
        }
        if (result != null) {
            printResultMsg(result);
            return true;
        }
        System.out.println("Invalid Command!");
        return false;
    }

    private static Result getResult() {
        return new Result();
    }


    //---* Helper Function *---
    public void printProfileInfo(User u) {
        int levels = u.getCompletedLevels().size();
        System.out.println("Your Profile:");
        System.out.printf("%-20s | %-20s | %s \n",
                "Username", "Nickname", "Number of Games");
        System.out.printf("%-20s | %-20s | %d \n",
                u.getUsername(), u.getNickname(), u.getNumberOfGames());
        System.out.printf("%-10s | %-10s | %-10s | %s \n",
                "Coins", "Diamonds", "Levels", "MewPoints");
        System.out.printf("%-20d | %-20d | %-10d | %d \n",
                u.getCoins().getAmount(), u.getDiamonds().getAmount(), levels, u.getHighestScore());
    }
}