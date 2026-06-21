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
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher changeUsernameMatcher = ProfileCommands.CHANGE_USERNAME.pattern.matcher(input);
        if (changeUsernameMatcher.matches()) {
            String username = changeUsernameMatcher.group("username");
            Result result = controller.handleChangeUsername(username);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher changeNicknameMatcher = ProfileCommands.CHANGE_NICKNAME.pattern.matcher(input);
        if (changeNicknameMatcher.matches()) {
            String nickname = changeNicknameMatcher.group("nickname");
            Result result = controller.handleChangeNickname(nickname);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher changeEmailMatcher = ProfileCommands.CHANGE_EMAIL.pattern.matcher(input);
        if (changeEmailMatcher.matches()) {
            String email = changeEmailMatcher.group("email");
            Result result = controller.handleChangeEmail(email);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher changePasswordMatcher = ProfileCommands.CHANGE_PASSWORD.pattern.matcher(input);
        if (changePasswordMatcher.matches()) {
            String password = changePasswordMatcher.group("new_password");
            String oldPassword = changePasswordMatcher.group("old_password");
            Result result = controller.handleChangePassword(oldPassword, password);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher showProfileMatcher = ProfileCommands.SHOW_PROFILE.pattern.matcher(input);
        if (showProfileMatcher.matches()) {
            Result result = controller.handleShowProfile();
            printProfileInfo((User) result.getData());
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher changeMenuMatcher = GlobalCommands.CHANGE_MENU.pattern.matcher(input);
        if (changeMenuMatcher.matches()) {
            String menu = changeMenuMatcher.group("menu");
            Result result = controller.handleMenuChange(menu);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher showMenuMatcher = GlobalCommands.SHOW_MENU.pattern.matcher(input);
        if (showMenuMatcher.matches()) {
            Result result = new Result();
            result.setSuccess(true);
            result.addMessage("Profile menu");
            printResultMsg(result);
            return true;
        }

        Matcher exitMatcher = GlobalCommands.EXIT.pattern.matcher(input);
        if (exitMatcher.matches()) {
            Result result = controller.handleExit();
            if (result != null) printResultMsg(result);
            return true;
        }

        return false;
    }

    private void printProfileInfo(User u) {
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