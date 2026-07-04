package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.enums.Menus;
import models.enums.SecurityQuestionData;
import models.enums.regexes.commandHandlers.SignupCommands;

public class SignupController extends BaseController {
    public SignupController(App app) {
        super(app);
    }

    public Result handleRegistry(String username, String password, String confirm,
                                 String nickname, String email, String gender) {

        Result result = UserManager.getInstance().registerUser(username, password, confirm, nickname, email, gender);
        if (!result.isSuccessfull()) return result;

        int count = 1;
        for (SecurityQuestionData q : SecurityQuestionData.values()) {
            result.addMessage(count + ". " + q);
            count++;
        }
        return result;
    }

    public Result handleQuestionSelection(String questionNum, String answer, String confirmation) {
        Result result = new Result();

        if (!SignupCommands.NUMBER_PATTERN.matches(questionNum)) {
            result.setSuccess(false);
            result.addMessage("Invalid question ID");
            return result;
        }

        int number = Integer.parseInt(questionNum);
        if (number < 1 || number > SecurityQuestionData.values().length) {
            result.setSuccess(false);
            result.addMessage("There is no question with the given ID");
            return result;
        } else if (!answer.equals(confirmation)) {
            result.setSuccess(false);
            result.addMessage("Answers do not match");
            return result;
        }

        String question = SecurityQuestionData.values()[number - 1].getQuestion();
        result = UserManager.getInstance().completeRegistration(question, answer);
        if (!result.isSuccessfull()) return result;

        app.navigateTo(Menus.LOGIN);
        result.addMessage("Redirected to login menu");
        return result;
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        Result result = new Result();

        if (menu == null) {
            result.setSuccess(false);
            result.addMessage("No menu with the given name");
            return result;
        } else if (menu != Menus.LOGIN) {
            result.setSuccess(false);
            result.addMessage("You must first login/signup");
            return result;
        }

        app.navigateTo(Menus.LOGIN);
        result.setSuccess(true);
        result.addMessage("Redirected to login menu");
        return result;
    }

    public Result handleExit() {
        //TODO(kamyar): Can't print a message. have to change the main loop "while(currentMenu != null)"
        //TODO(kamyar): Save the current user before exit

        Result result = new Result();
        if (!App.getInstance().isStayLoggedIn()) {
            result = UserManager.getInstance().logout();
        }
        System.exit(0);
        return result;
    }
}
