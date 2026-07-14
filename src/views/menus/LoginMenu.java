package views.menus;

import controllers.menuControllers.LoginController;
import models.Result;
import models.enums.regexes.commandHandlers.GlobalCommands;
import models.enums.regexes.commandHandlers.LoginCommands;
import views.CommandRouter;

public class LoginMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public LoginMenu(LoginController controller) {
        router.add(LoginCommands.LOGIN.pattern, matcher -> controller.handleLogin(
                        matcher.group("username"), matcher.group("password"), matcher.group("stay")))
                .add(LoginCommands.FORGET_PASSWORD.pattern, matcher -> controller.handleForgotPassword(
                        matcher.group("username"), matcher.group("email")))
                .add(LoginCommands.ANSWER.pattern,
                        matcher -> controller.handleSecurityAnswer(matcher.group("answer")))
                .add(LoginCommands.NEW_PASSWORD.pattern, matcher -> controller.handleNewPassword(
                        matcher.group("password"), matcher.group("confirm")))
                .add(LoginCommands.QUIT_PASSWORD_RESET.pattern,
                        matcher -> controller.handleResetPasswordQuit())
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Login menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}