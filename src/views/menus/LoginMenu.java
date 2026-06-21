package views.menus;

import controllers.menuControllers.LoginController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.LoginCommands;

import java.util.regex.Matcher;

public class LoginMenu implements AppMenu {
    private final LoginController controller;

    public LoginMenu(LoginController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher loginMatcher = LoginCommands.LOGIN.pattern.matcher(input);
        if (loginMatcher.matches()) {
            String username = loginMatcher.group("username");
            String password = loginMatcher.group("password");
            String stay = loginMatcher.group("stay");
            Result result = controller.handleLogin(username, password, stay);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher forgetPasswordMatcher = LoginCommands.FORGET_PASSWORD.pattern.matcher(input);
        if (forgetPasswordMatcher.matches()) {
            String username = forgetPasswordMatcher.group("username");
            String email = forgetPasswordMatcher.group("email");
            Result result = controller.handleForgotPassword(username, email);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher answerMatcher = LoginCommands.ANSWER.pattern.matcher(input);
        if (answerMatcher.matches()) {
            String answer = answerMatcher.group("answer");
            Result result = controller.handleSecurityAnswer(answer);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher newPasswordMatcher = LoginCommands.NEW_PASSWORD.pattern.matcher(input);
        if (newPasswordMatcher.matches()) {
            String password = newPasswordMatcher.group("password");
            String confirmation = newPasswordMatcher.group("password-confirm");
            Result result = controller.handleNewPassword(password, confirmation);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher quitPasswordResetMatcher = LoginCommands.QUIT_PASSWORD_RESET.pattern.matcher(input);
        if (quitPasswordResetMatcher.matches()) {
            Result result = controller.handleResetPasswordQuit();
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
            result.addMessage("Login menu");
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
}