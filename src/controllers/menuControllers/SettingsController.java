package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.enums.DifficultyLevel;
import models.enums.Menus;

/** Settings menu: difficulty level 1..5 (default is 3). */
public class SettingsController extends BaseController {

    public SettingsController(App app) {
        super(app);
    }

    public Result handleChangeDifficulty(String levelText) {
        int levelNumber;
        try {
            levelNumber = Integer.parseInt(levelText);
        } catch (NumberFormatException e) {
            return Result.fail("Difficulty level must be a number between 1 and 5.");
        }
        if (levelNumber < 1 || levelNumber > 5) {
            return Result.fail("Difficulty level must be between 1 and 5.");
        }
        return UserManager.getInstance()
                .changeDifficulty(DifficultyLevel.getDifficultyByLevel(levelNumber));
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == Menus.MAIN) {
            app.navigateTo(menu);
            return Result.ok("Redirected to main menu");
        }
        return Result.fail(menu == null ? "No menu with the given name"
                : "You can't move to " + menuName + " from the settings menu");
    }

    public Result handleExit() {
        app.navigateTo(Menus.MAIN);
        return Result.ok("Redirected to Main menu");
    }
}
