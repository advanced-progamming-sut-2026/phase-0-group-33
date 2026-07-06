package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.enums.Menus;
import models.game.GameSession;
import models.game.GameSetup;
import models.progress.chapter.Chapter;
import models.progress.level.Level;
import models.user.User;
import utils.SessionStore;
import utils.UserDataStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController extends BaseController {

    public static final List<String> STARTER_PLANTS =
            Arrays.asList("Sunflower", "Peashooter", "Wall-nut", "Potato Mine");

    public MainController(App app) {
        super(app);
    }

    private static final List<String> CHAPTER_ORDER =
            Arrays.asList("Egypt", "Frost Bite", "Wavey Beach", "Dark Ages");

    public Result handleEnterChapter(String chapterName) {
        Chapter chapter = Chapter.getByName(chapterName);
        if (chapter == null) {
            return Result.fail("No chapter with the given name.");
        }
        User user = app.getCurrentUser();
        UserDataStore store = UserDataStore.forUser(user.getUsername());
        int order = CHAPTER_ORDER.indexOf(chapter.getName());
        if (order > 0) {
            String previous = CHAPTER_ORDER.get(order - 1);
            Chapter previousChapter = Chapter.getByName(previous);
            if (store.getInt("progress." + previous, 1) < previousChapter.getLevels().size()) {
                return Result.fail("This chapter is locked; finish the levels of "
                        + previous + " first.");
            }
        }
        int unlockedLevel = store.getInt("progress." + chapter.getName(), 1);
        Level level = chapter.getLevels().get(
                Math.min(unlockedLevel, chapter.getLevels().size()) - 1);
        chapter.setCurrentUnlockedLevel(level);

        List<String> unlockedPlants = unlockedPlants(store);
        int plantFoods = store.getInt("plantFoods", 0);
        store.setInt("plantFoods", 0);
        store.save();

        app.setCurrentGameSession(new GameSession(
                GameSetup.adventure(user, level, unlockedPlants, plantFoods,
                        plantLevels(store, unlockedPlants))));
        app.navigateTo(Menus.GAME);
        String special = level.getSpecialType() == null ? ""
                : " [special: " + level.getSpecialType() + "]";
        return Result.ok("Entered " + chapter.getName() + ", level " + level.getLevelNumber() + special,
                "Pick your plants, then use 'start game'.");
    }

    public static Map<String, Integer> plantLevels(UserDataStore store, List<String> plants) {
        Map<String, Integer> levels = new HashMap<>();
        for (String name : plants) {
            levels.put(name, store.getInt("level." + name, 1));
        }
        return levels;
    }

    public Result handleScoringGame() {
        User user = app.getCurrentUser();
        UserDataStore store = UserDataStore.forUser(user.getUsername());
        List<String> unlockedPlants = unlockedPlants(store);
        app.setCurrentGameSession(new GameSession(
                GameSetup.scoring(user, unlockedPlants, plantLevels(store, unlockedPlants))));
        app.navigateTo(Menus.GAME);
        return Result.ok("Scoring game started! Today's zombies are the same for everyone.",
                "Pick your plants, then use 'start game'. Earn miopoints with stylish kills.");
    }

    public static List<String> unlockedPlants(UserDataStore store) {
        String stored = store.get("plants", null);
        if (stored == null || stored.isBlank()) {
            store.set("plants", String.join(",", STARTER_PLANTS));
            store.save();
            return STARTER_PLANTS;
        }
        return Arrays.asList(stored.split(","));
    }

    public Result handleShowCoinWallet() {
        return Result.ok("Coins: " + app.getCurrentUser().getCoins().getAmount());
    }

    public Result handleShowGemWallet() {
        return Result.ok("Diamonds: " + app.getCurrentUser().getDiamonds().getAmount());
    }

    public Result handleCheatAdd(int amount, String type) {
        if (type.equalsIgnoreCase("coin")) {
            return UserManager.getInstance().addCoins(amount);
        }
        return UserManager.getInstance().addDiamonds(amount);
    }

    public Result handleLogout() {
        SessionStore.clearSession();
        app.clearCurrentUser();
        UserManager.getInstance().logout();
        app.navigateTo(Menus.SIGNUP);
        return Result.ok("Logged out. Redirected to signup menu");
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == null) {
            return Result.fail("No menu with the given name");
        }
        switch (menu) {
            case SETTINGS:
            case NEWS:
            case PROFILE:
            case GREENHOUSE:
            case TRAVELLOG:
            case LEADERBOARD:
            case COLLECTION:
                app.navigateTo(menu);
                return Result.ok("Redirected to " + menuName + " menu");
            case GAME:
                return Result.fail("Enter a chapter to play: menu enter chapter -c <chaptername>");
            default:
                return Result.fail("You can't move to " + menuName + " from the main menu");
        }
    }

    public Result handleExit() {
        return Result.fail("You must log out to leave the main menu: menu logout");
    }
}
