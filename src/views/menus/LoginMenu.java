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
    public void display() {
        // TODO
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();
        Matcher m;
        Result result = new Result();

        if ((m = LoginCommands.LOGIN.pattern.matcher(input)).matches()) {
            String username = m.group("username");
            String password = m.group("password");
            String stay = m.group("stay");
            result = controller.handleLogin(username, password, stay);
        } else if ((m = LoginCommands.FORGET_PASSWORD.pattern.matcher(input)).matches()) {
            String username = m.group("username");
            String email = m.group("email");
            result = controller.handleForgotPassword(username, email);
        } else if ((m = LoginCommands.ANSWER.pattern.matcher(input)).matches()) {
            String answer = m.group("answer");
            result = controller.handleSecurityAnswer(answer);
        } else if ((m = LoginCommands.NEW_PASSWORD.pattern.matcher(input)).matches()) {
            String password = m.group("password");
            String confirmation = m.group("password-confirm");
            result = controller.handleNewPassword(password, confirmation);
        } else if ((m = LoginCommands.QUIT_PASSWORD_RESET.pattern.matcher(input)).matches()) {
            result = controller.handleResetPasswordQuit();
        }  else if ((m = GlobalCommands.CHANGE_MENU.pattern.matcher(input)).matches()) {
            String menu = m.group("menu");
            result = controller.handleMenuChange(menu);
        } else if ((m = GlobalCommands.SHOW_MENU.pattern.matcher(input)).matches()) {
            result.setSuccess(true);
            result.addMessage("Login menu");
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
}