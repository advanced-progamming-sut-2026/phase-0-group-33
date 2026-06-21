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
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher registerMatcher = SignupCommands.REGISTER.pattern.matcher(input);
        if (registerMatcher.matches()) {
            String username = registerMatcher.group("username");
            String password = registerMatcher.group("password");
            String confirm = registerMatcher.group("password-confirm");
            String nickname = registerMatcher.group("nickname");
            String email = registerMatcher.group("email");
            String gender = registerMatcher.group("gender");
            Result result = controller.handleRegistry(username, password, confirm, nickname, email, gender);
            if (result != null) printResultMsg(result);
            return true;
        }

        Matcher selectQuestionMatcher = SignupCommands.SELECT_QUESTION.pattern.matcher(input);
        if (selectQuestionMatcher.matches()) {
            String number = selectQuestionMatcher.group("number");
            String answer = selectQuestionMatcher.group("answer");
            String confirm = selectQuestionMatcher.group("answer-confirm");
            Result result = controller.handleQuestionSelection(number, answer, confirm);
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
            result.addMessage("Signup menu");
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