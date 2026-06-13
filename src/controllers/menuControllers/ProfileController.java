package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.enums.Menus;
import models.user.User;

public class ProfileController extends BaseController {
    public ProfileController(App app) {
        super(app);
    }

    public Result handleChangeUsername(String username) {
        return UserManager.getInstance().changeUsername(username);
    }

    public Result handleChangeNickname(String nickname) {
        return UserManager.getInstance().changeNickname(nickname);
    }

    public Result handleChangeEmail(String email) {
        return UserManager.getInstance().changeEmail(email);
    }

    public Result handleChangePassword(String oldPassword, String newPassword) {
        return UserManager.getInstance().changePassword(oldPassword, newPassword);
    }

    public Result handleShowProfile() {
        User user = app.getCurrentUser();
        Result result = new Result();
        if (user == null) {
            result.setSuccess(false);
            result.addMessage("How aren't you logged in?!");
            return result;
        }

        result.setSuccess(true);
        result.setData(user);
        return result;
    }

    public Result handleExit() {
        Result result = new Result();
        result.setSuccess(true);
        result.addMessage("Redirected to Main menu");
        app.navigateTo(Menus.MAIN);
        return result;
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        Result result = new Result();

        if (menu == null) {
            result.setSuccess(false);
            result.addMessage("No menu with the given name");
            return result;
        } else if (menu != Menus.MAIN) {
            result.setSuccess(false);
            result.addMessage("You can't move to " + menuName);
            return result;
        }

        app.navigateTo(menu);
        result.setSuccess(true);
        result.addMessage("Redirected to " + menuName);
        return result;
    }
}
