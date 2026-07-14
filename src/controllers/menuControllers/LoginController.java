package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.enums.Menus;
import models.user.SecurityQuestion;
import utils.NewsStore;
import utils.SessionStore;

public class LoginController extends BaseController {
    private String pendingUsername;
    private String pendingEmail;

    public LoginController(App app) {
        super(app);
    }

    public Result handleLogin(String username, String password, String stayLoggedInString) {
        boolean stayLoggedIn = stayLoggedInString != null;

        Result result = UserManager.getInstance().login(username, password);
        if (result.isSuccessfull()) {
            app.setStayLoggedIn(stayLoggedIn);
            if (stayLoggedIn) {
                SessionStore.saveSession(username);
            }
            app.navigateTo(Menus.MAIN);
            result.addMessage("Redirected to main menu");
            int unread = NewsStore.countUnread(username);
            if (unread > 0) {
                result.addMessage("[!] You have " + unread
                        + " unread news; check the news menu.");
            }
        }
        return result;
    }

    public Result handleForgotPassword(String username, String email) {
        Result result = new Result();
        if (pendingUsername != null) {
            result.setSuccess(false);
            result.addMessage("Finish or abort the current password-reset session");
            return result;
        }
        Result questionResult = UserManager.getInstance()
                .getSecurityQuestionForUser(username, email);
        if (!questionResult.isSuccessfull()) return questionResult;

        pendingUsername = username;
        pendingEmail = email;

        SecurityQuestion question = (SecurityQuestion) questionResult.getData();
        result.addMessage(question.getQuestion());
        result.setSuccess(true);
        return result;
    }

    public Result handleSecurityAnswer(String answer) {
        Result result = new Result();
        if (pendingUsername == null) {
            result.setSuccess(false);
            result.addMessage("No pending password-reset sessions");
            return result;
        }
        result = UserManager.getInstance().verifySecurityAnswer(pendingUsername, answer);

        if (!result.isSuccessfull()) {
            pendingUsername = null;
            pendingEmail = null;
            result.addMessage("Returned to login menu");
            return result;
        }

        result.setSuccess(true);
        result.addMessage("Choose your new password");
        return result;
    }

    public Result handleNewPassword(String password, String confirmation) {
        Result result = new Result();
        if (pendingUsername == null) {
            result.setSuccess(false);
            result.addMessage("You can't change password right now");
            return result;
        }
        if (!password.equals(confirmation)) {
            result.setSuccess(false);
            result.addMessage("Confirmation does not match");
            return result;
        }
        result = UserManager.getInstance().resetPassword(pendingUsername, password);
        if (result.isSuccessfull()) {
            result.addMessage(handleResetPasswordQuit().getMessages().get(0));
        }
        return result;
    }

    public Result handleResetPasswordQuit() {
        Result result = new Result();
        pendingUsername = null;
        pendingEmail = null;

        result.setSuccess(true);
        result.addMessage("Returned to login menu");
        return result;
    }

    public Result handleExit() {
        Result result = new Result();
        result.setSuccess(true);
        result.addMessage("Redirected to Signup menu");
        app.navigateTo(Menus.SIGNUP);
        return result;
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        Result result = new Result();

        if (menu == null) {
            result.setSuccess(false);
            result.addMessage("No menu with the given name");
            return result;
        } else if (menu != Menus.MAIN && menu != Menus.SIGNUP) {
            result.setSuccess(false);
            result.addMessage("You must first login/signup");
            return result;
        } else if (menu == Menus.MAIN && app.getCurrentUser() == null) {
            result.setSuccess(false);
            result.addMessage("You haven't logged in yet");
            return result;
        }

        app.navigateTo(menu);
        result.setSuccess(true);
        result.addMessage("Redirected to " + menuName);
        return result;
    }
}
