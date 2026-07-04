package controllers.menuControllers;

import models.App;
import models.Result;
import models.enums.Menus;
import utils.NewsStore;

import java.util.List;

/** News menu: unlock events; unread items are marked read once shown. */
public class NewsController extends BaseController {

    public NewsController(App app) {
        super(app);
    }

    public Result handleShowUnread() {
        List<String> unread = NewsStore.readUnread(app.getCurrentUser().getUsername());
        if (unread.isEmpty()) {
            return Result.ok("No unread news.");
        }
        Result result = Result.ok("Unread news:");
        for (String entry : unread) {
            result.addMessage("- " + entry);
        }
        return result;
    }

    public Result handleShowAll() {
        List<String> all = NewsStore.readAll(app.getCurrentUser().getUsername());
        if (all.isEmpty()) {
            return Result.ok("No news yet.");
        }
        Result result = Result.ok("All news:");
        for (String entry : all) {
            result.addMessage("- " + entry);
        }
        return result;
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == Menus.MAIN) {
            app.navigateTo(menu);
            return Result.ok("Redirected to main menu");
        }
        return Result.fail(menu == null ? "No menu with the given name"
                : "You can't move to " + menuName + " from the news menu");
    }

    public Result handleExit() {
        app.navigateTo(Menus.MAIN);
        return Result.ok("Redirected to Main menu");
    }
}
