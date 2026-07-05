package controllers.menuControllers;

import database.UserDAO;
import models.App;
import models.Result;
import models.enums.Menus;
import models.user.User;
import utils.UserDataStore;

import java.util.Comparator;
import java.util.List;

public class LeaderboardController extends BaseController {
    private final UserDAO userDAO = new UserDAO();

    public LeaderboardController(App app) {
        super(app);
    }

    private static final String[] CHAPTERS = {"Egypt", "Frost Bite", "Wavey Beach", "Dark Ages"};

    public Result handleShowLeaderboard(String sortColumn, String order) {
        List<User> users = userDAO.getAllUsers();
        if (users.isEmpty()) {
            return Result.fail("Could not load users (is the database reachable?).");
        }
        sort(users, sortColumn == null ? "miopoint" : sortColumn.toLowerCase(),
                !"asc".equalsIgnoreCase(order));
        Result result = Result.ok(String.format("%-20s | %-16s | %-9s | %-7s | %-7s | %s",
                "Username", "Last Level", "Minigames", "Daily Q", "Other Q", "Miopoint"));
        for (User user : users) {
            UserDataStore store = UserDataStore.forUser(user.getUsername());
            int daily = store.getInt("dailyQuestsDone", 0);
            int other = Math.max(0, store.getInt("questsDone", 0) - daily);
            result.addMessage(String.format("%-20s | %-16s | %-9d | %-7d | %-7d | %d",
                    user.getUsername(), lastLevel(store),
                    store.getInt("minigamesWon", 0), daily, other, user.getHighestScore()));
        }
        result.addMessage("Sort with: show leaderboard -s "
                + "<levels|minigames|quests|dailyquests|miopoint> -o <asc|desc>");
        return result;
    }

    private void sort(List<User> users, String column, boolean descending) {
        Comparator<User> comparator;
        switch (column) {
            case "levels":
                comparator = Comparator.comparingInt(
                        u -> completedLevels(UserDataStore.forUser(u.getUsername())));
                break;
            case "minigames":
                comparator = Comparator.comparingInt(
                        u -> UserDataStore.forUser(u.getUsername()).getInt("minigamesWon", 0));
                break;
            case "quests":
                comparator = Comparator.comparingInt(
                        u -> UserDataStore.forUser(u.getUsername()).getInt("questsDone", 0));
                break;
            case "dailyquests":
                comparator = Comparator.comparingInt(
                        u -> UserDataStore.forUser(u.getUsername()).getInt("dailyQuestsDone", 0));
                break;
            default:
                comparator = Comparator.comparingInt(User::getHighestScore);
                break;
        }
        users.sort(descending ? comparator.reversed() : comparator);
    }

    private String lastLevel(UserDataStore store) {
        String best = "-";
        int bestLevel = 0;
        for (String chapter : CHAPTERS) {
            int completed = store.getInt("progress." + chapter, 1) - 1;
            if (completed > bestLevel) {
                bestLevel = completed;
                best = chapter + " L" + completed;
            }
        }
        return best;
    }

    private int completedLevels(UserDataStore store) {
        int total = 0;
        for (String chapter : CHAPTERS) {
            total += store.getInt("progress." + chapter, 1) - 1;
        }
        return total;
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == Menus.MAIN) {
            app.navigateTo(menu);
            return Result.ok("Redirected to main menu");
        }
        return Result.fail(menu == null ? "No menu with the given name"
                : "You can't move to " + menuName + " from the leaderboard");
    }

    public Result handleExit() {
        app.navigateTo(Menus.MAIN);
        return Result.ok("Redirected to Main menu");
    }
}
