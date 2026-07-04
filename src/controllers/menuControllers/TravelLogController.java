package controllers.menuControllers;

import models.App;
import models.Result;
import models.enums.Menus;
import models.quest.Quest;
import models.quest.QuestPriority;
import models.quest.QuestType;
import utils.UserDataStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Travel log (Quest section). Quests are grouped into pages by their
 * priority; page names: critical, high, daily. Progress is based on the
 * user's stored chapter progress.
 */
public class TravelLogController extends BaseController {

    public TravelLogController(App app) {
        super(app);
    }

    public Result handleShowPage(String pageName) {
        List<Quest> quests = questsForPage(pageName.toLowerCase());
        if (quests == null) {
            return Result.fail("No travel log page with this name. Pages: critical, high, daily");
        }
        Result result = Result.ok("Travel log - " + pageName + " quests:");
        for (Quest quest : quests) {
            String status = quest.isCompleted() ? "DONE" : quest.getProgress() + "/" + quest.getGoal();
            result.addMessage("- " + quest.getDescription() + " [" + status + "]");
        }
        return result;
    }

    private List<Quest> questsForPage(String pageName) {
        UserDataStore store = new UserDataStore(app.getCurrentUser().getUsername());
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

    /** Critical: story quests that unlock new plants and always top the list. */
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

    /** High: epic challenges rewarded with diamonds. */
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

    /** Daily/medium: repeatable engagement quests. */
    private List<Quest> dailyQuests(UserDataStore store) {
        List<Quest> quests = new ArrayList<>();
        Quest daily = buildQuest("Buy the shop's daily offer",
                QuestPriority.MEDIUM, QuestType.DAILY, 1);
        daily.setProgress(store.get("daily.lastBuy", "").isEmpty() ? 0 : 1);
        quests.add(daily);
        return quests;
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
