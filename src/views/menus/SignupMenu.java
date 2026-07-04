package views.menus;

import controllers.menuControllers.SignupController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.SignupCommands;
import views.CommandRouter;

public class SignupMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public SignupMenu(SignupController controller) {
        router.add(SignupCommands.REGISTER.pattern, matcher -> controller.handleRegistry(
                        matcher.group("username"), matcher.group("password"),
                        matcher.group("passwordConfirm"), matcher.group("nickname"),
                        matcher.group("email"), matcher.group("gender")))
                .add(SignupCommands.SELECT_QUESTION.pattern, matcher -> controller.handleQuestionSelection(
                        matcher.group("number"), matcher.group("answer"),
                        matcher.group("answerConfirm")))
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Signup menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
