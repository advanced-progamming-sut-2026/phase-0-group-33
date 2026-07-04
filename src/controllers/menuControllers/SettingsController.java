package controllers.menuControllers;

import models.App;
import models.Result;

public class SettingsController extends BaseController {
    public SettingsController(App app) {
        super(app);
    }

    public Result handleChangeDifficulty(String level) {
        return null;
    }

    public Result handleMenuChange(String menuName) {
        return null;
    }

    public Result handleExit() {
        return null;
    }
}