package controllers.menuControllers;

import controllers.managers.QuestManager;
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
        quests.add(flagQuest(store, "Defense master: finish a level with exactly 0 sun"
                        + " | reward: 200 gems",
                QuestPriority.CRITICAL, QuestType.EPIC, "q.done.epic.defense"));
        return quests;
    }

    private List<Quest> highQuests(UserDataStore store) {
        String day = QuestManager.today();
        List<Quest> quests = new ArrayList<>();
        quests.add(counterQuest(store, "Only Cactus: kill 10 zombies with Cactus today"
                        + " | reward: 20 gems",
                QuestType.DAILY, "q.kills.Cactus." + day, 10));
        String specialist = QuestManager.dailySpecialistPlant(store);
        quests.add(counterQuest(store, "Plant specialist: kill 10 zombies with " + specialist
                        + " today | reward: a new plant",
                QuestType.DAILY, "q.kills." + specialist + "." + day, 10));
        quests.add(flagQuest(store, "Economical gardener: win losing at most 2 plants"
                        + " | reward: 18 seed packets",
                QuestPriority.HIGH, QuestType.DAILY, "q.done.economical." + day));
        quests.add(flagQuest(store, "Blooming in limits: win without the "
                        + QuestManager.dailyRestrictedFamily() + " family | reward: 100 gems",
                QuestPriority.HIGH, QuestType.DAILY, "q.done.restriction." + day));
        quests.add(flagQuest(store, "Night or morning: win a day level with shrooms only"
                        + " | reward: 20 gems",
                QuestPriority.HIGH, QuestType.EPIC, "q.done.epic.nightshroom"));
        quests.add(flagQuest(store, "Cloudy day: win with at most 3 sun producers"
                        + " | reward: 10 gems",
                QuestPriority.HIGH, QuestType.DAILY, "q.done.cloudy." + day));
        quests.add(flagQuest(store, "One column less: win with column "
                        + QuestManager.dailyEmptyColumn() + " empty | reward: 10 gems",
                QuestPriority.HIGH, QuestType.DAILY, "q.done.emptycolumn." + day));
        quests.add(flagQuest(store, "Defenseless row: win with row "
                        + QuestManager.dailyEmptyRow() + " empty | reward: 20 gems",
                QuestPriority.HIGH, QuestType.DAILY, "q.done.emptyrow." + day));
        quests.add(flagQuest(store, "Defenseless cross: win with row "
                        + QuestManager.dailyEmptyRow() + " and column "
                        + QuestManager.dailyEmptyColumn() + " empty | reward: 25 gems",
                QuestPriority.HIGH, QuestType.DAILY, "q.done.cross." + day));
        for (String chapter : new String[]{"Egypt", "Frost Bite", "Wavey Beach", "Dark Ages"}) {
            quests.add(counterQuest(store, "Hunter of " + chapter + ": defeat 50 of its zombies"
                            + " | reward: 10 seed packets",
                    QuestType.STORY, "q.kills.chapter." + chapter, 50));
        }
        return quests;
    }

    private List<Quest> dailyQuests(UserDataStore store) {
        String day = QuestManager.today();
        List<Quest> quests = new ArrayList<>();
        quests.add(counterQuest(store, "Daily sun collector: gather "
                        + QuestManager.dailySunGoal() + " sun today | reward: "
                        + QuestManager.dailySunGoal() / 100 + " coins",
                QuestType.DAILY, "q.sun." + day, QuestManager.dailySunGoal()));
        quests.add(flagQuest(store, "Quick trigger: 10 kills within 30s of the first wave"
                        + " | reward: 500 coins",
                QuestPriority.MEDIUM, QuestType.STORY, "q.done.speed"));
        quests.add(flagQuest(store, "Demolition expert: use 3 explosive plants in one level"
                        + " | reward: 100 coins",
                QuestPriority.LOW, QuestType.DAILY, "q.done.demolition." + day));
        quests.add(flagQuest(store, "Symmetry: win with a symmetric garden | reward: 500 coins",
                QuestPriority.MEDIUM, QuestType.DAILY, "q.done.symmetry." + day));
        quests.add(flagQuest(store, "No OCD: win with no symmetry outside the middle row"
                        + " | reward: 800 coins",
                QuestPriority.MEDIUM, QuestType.DAILY, "q.done.ocd." + day));
        quests.add(flagQuest(store, "Family killer: every kill from the "
                        + QuestManager.dailyKillerFamily() + " family | reward: 1000 coins",
                QuestPriority.MEDIUM, QuestType.DAILY, "q.done.familykiller." + day));
        quests.add(counterQuest(store, "Almost winner: kill 10 zombies at the last column of"
                        + " mowerless rows | reward: 300 coins",
                QuestType.DAILY, "q.kills.firstcolumn." + day, 10));
        quests.add(counterQuest(store, "Back-to-back: win 5 levels in a row at max difficulty"
                        + " | reward: 5000 coins",
                QuestType.REPEATABLE, "q.streak", 5));
        Quest daily = buildQuest("Buy the shop's daily offer",
                QuestPriority.MEDIUM, QuestType.DAILY, 1);
        daily.setProgress(store.get("daily.lastBuy", "").equals(QuestManager.today()) ? 1 : 0);
        quests.add(daily);
        return quests;
    }

    private Quest counterQuest(UserDataStore store, String description,
                               QuestType type, String key, int goal) {
        Quest quest = buildQuest(description, QuestPriority.HIGH, type, goal);
        quest.setProgress(Math.min(goal, store.getInt(key, 0)));
        return quest;
    }

    private Quest flagQuest(UserDataStore store, String description, QuestPriority priority,
                            QuestType type, String doneKey) {
        Quest quest = buildQuest(description, priority, type, 1);
        quest.setProgress(store.getInt(doneKey, 0));
        return quest;
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
