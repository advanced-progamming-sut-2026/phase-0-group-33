package controllers.menuControllers;

import models.App;
import models.Result;
import models.enums.Menus;
import models.game.GameMode;
import models.game.GameSession;
import models.game.GameSetup;
import models.quest.Quest;
import models.quest.QuestPriority;
import models.quest.QuestType;
import models.user.User;
import utils.UserDataStore;

import java.util.ArrayList;
import java.util.List;

public class TravelLogController extends BaseController {

    public TravelLogController(App app) {
        super(app);
    }

    public Result handleShowPage(String pageName) {
        if (pageName.equalsIgnoreCase("minigame") || pageName.equalsIgnoreCase("minigames")) {
            return minigamePage();
        }
        List<Quest> quests = questsForPage(pageName.toLowerCase());
        if (quests == null) {
            return Result.fail(
                    "No travel log page with this name. Pages: critical, high, daily, minigame");
        }
        Result result = Result.ok("Travel log - " + pageName + " quests:");
        for (Quest quest : quests) {
            String status = quest.isCompleted() ? "DONE" : quest.getProgress() + "/" + quest.getGoal();
            result.addMessage("- " + quest.getDescription() + " [" + status + "]");
        }
        return result;
    }

    private List<Quest> questsForPage(String pageName) {
        UserDataStore store = UserDataStore.forUser(app.getCurrentUser().getUsername());
        switch (pageName) {
            case "critical":
                return criticalQuests(store);
            case "high":
                return highQuests(store);
            case "daily":
                return dailyQuests(store);
            default:
                return null;
        }
    }

    private List<Quest> criticalQuests(UserDataStore store) {
        List<Quest> quests = new ArrayList<>();
        for (String chapter : new String[]{"Egypt", "Frost Bite", "Wavey Beach", "Dark Ages"}) {
            Quest quest = buildQuest("Complete the first level of " + chapter,
                    QuestPriority.CRITICAL, QuestType.STORY, 1);
            quest.setProgress(Math.min(1, store.getInt("progress." + chapter, 1) - 1));
            quests.add(quest);
        }
        return quests;
    }

    private List<Quest> highQuests(UserDataStore store) {
        List<Quest> quests = new ArrayList<>();
        Quest special = buildQuest("Win 2 special levels (reward: diamonds)",
                QuestPriority.HIGH, QuestType.EPIC, 2);
        int specials = Math.max(0, store.getInt("progress.Egypt", 1) - 2)
                + Math.max(0, store.getInt("progress.Dark Ages", 1) - 2);
        special.setProgress(Math.min(2, specials));
        quests.add(special);
        return quests;
    }

    private List<Quest> dailyQuests(UserDataStore store) {
        List<Quest> quests = new ArrayList<>();
        Quest daily = buildQuest("Buy the shop's daily offer",
                QuestPriority.MEDIUM, QuestType.DAILY, 1);
        daily.setProgress(store.get("daily.lastBuy", "").isEmpty() ? 0 : 1);
        quests.add(daily);
        return quests;
    }

    private Result minigamePage() {
        UserDataStore store = UserDataStore.forUser(app.getCurrentUser().getUsername());
        return Result.ok("Travel log - minigames (won so far: " + store.getInt("minigamesWon", 0) + "):",
                "- Vasebreaker | break every vase, survive what hides inside",
                "- Wallnut Bowling | bowl nuts from behind the red line",
                "- I Zombie | command the zombies and eat all the brains",
                "- Beghouled | match 3 plants, upgrade, survive the endless horde",
                "- Zombotany | zombies with plant powers",
                "Each minigame has 3 stages; start one with:",
                "play minigame -n <name> -d <1|2|3>");
    }

    public Result handlePlayMinigame(String name, int difficulty) {
        GameMode mode = resolveMinigame(name);
        if (mode == null) {
            return Result.fail("No minigame with this name. "
                    + "Options: vasebreaker, wallnut-bowling, i-zombie, beghouled, zombotany");
        }
        User user = app.getCurrentUser();
        UserDataStore store = UserDataStore.forUser(user.getUsername());
        List<String> unlockedPlants = MainController.unlockedPlants(store);
        app.setCurrentGameSession(new GameSession(GameSetup.minigame(user, mode, unlockedPlants,
                difficulty, MainController.plantLevels(store, unlockedPlants))));
        app.navigateTo(Menus.GAME);
        return Result.ok(mode + " (stage " + difficulty + ") started!",
                "Use 'show map' to look around and 'advance time -t <n> ticks' to play.");
    }

    private GameMode resolveMinigame(String name) {
        String normalized = name.replaceAll("[\\s_-]", "").toLowerCase();
        switch (normalized) {
            case "vasebreaker":
                return GameMode.VASEBREAKER;
            case "wallnutbowling":
            case "bowling":
                return GameMode.WALLNUT_BOWLING;
            case "izombie":
                return GameMode.I_ZOMBIE;
            case "beghouled":
                return GameMode.BEGHOULED;
            case "zombotany":
                return GameMode.ZOMBOTANY;
            default:
                return null;
        }
    }

    private Quest buildQuest(String description, QuestPriority priority, QuestType type, int goal) {
        Quest quest = new Quest();
        quest.setDescription(description);
        quest.setPriority(priority);
        quest.setType(type);
        quest.setGoal(goal);
        return quest;
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == Menus.MAIN) {
            app.navigateTo(menu);
            return Result.ok("Redirected to main menu");
        }
        return Result.fail(menu == null ? "No menu with the given name"
                : "You can't move to " + menuName + " from the travel log");
    }

    public Result handleExit() {
        app.navigateTo(Menus.MAIN);
        return Result.ok("Redirected to Main menu");
    }
}
