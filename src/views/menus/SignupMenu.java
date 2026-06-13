package views.menus;

import controllers.menuControllers.SignupController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.SignupCommands;

import java.util.regex.Matcher;

public class SignupMenu implements AppMenu {
    private final SignupController controller;

    public SignupMenu(SignupController controller) {
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

        if ((m = SignupCommands.REGISTER.pattern.matcher(input)).matches()) {
            String username = m.group("username");
            String password = m.group("password");
            String confirm = m.group("password-confirm");
            String nickname = m.group("nickname");
            String email = m.group("email");
            String gender = m.group("gender");
            result = controller.handleRegistry(username, password, confirm, nickname, email, gender);
        } else if ((m = SignupCommands.SELECT_QUESTION.pattern.matcher(input)).matches()) {
            String number = m.group("number");
            String answer = m.group("answer");
            String confirm = m.group("answer-confirm");
            result = controller.handleQuestionSelection(number, answer, confirm);
        } else if ((m = GlobalCommands.CHANGE_MENU.pattern.matcher(input)).matches()) {
            String menu = m.group("menu");
            result = controller.handleMenuChange(menu);
        } else if ((m = GlobalCommands.SHOW_MENU.pattern.matcher(input)).matches()) {
            result.setSuccess(true);
            result.addMessage("Signup menu");
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