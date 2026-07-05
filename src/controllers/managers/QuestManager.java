package controllers.managers;

import controllers.menuControllers.MainController;
import models.entities.plant.PlantCategory;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.game.QuestStats;
import models.user.User;
import utils.NewsStore;
import utils.UserDataStore;

import java.time.LocalDate;
import java.util.List;

public final class QuestManager {
    private static QuestManager instance;

    private QuestManager() {
    }

    public static synchronized QuestManager getInstance() {
        if (instance == null) {
            instance = new QuestManager();
        }
        return instance;
    }

    public static String today() {
        return LocalDate.now().toString();
    }

    private static long epochDay() {
        return LocalDate.now().toEpochDay();
    }

    public static int dailySunGoal() {
        return 3000 + 1000 * (int) (epochDay() % 3);
    }

    public static int dailyEmptyColumn() {
        return 1 + (int) (epochDay() % GameSession.COLS);
    }

    public static int dailyEmptyRow() {
        return 1 + (int) (epochDay() % GameSession.ROWS);
    }

    public static PlantCategory dailyRestrictedFamily() {
        return PlantCategory.values()[(int) (epochDay() % PlantCategory.values().length)];
    }

    public static PlantCategory dailyKillerFamily() {
        return PlantCategory.values()[(int) ((epochDay() + 3) % PlantCategory.values().length)];
    }

    public static String dailySpecialistPlant(UserDataStore store) {
        List<String> unlocked = MainController.unlockedPlants(store);
        return unlocked.get((int) (epochDay() % unlocked.size()));
    }

    public void onSunCollected(String username, int amount) {
        UserDataStore store = UserDataStore.forUser(username);
        int total = store.addInt("q.sun." + today(), amount);
        if (total >= dailySunGoal() && notDoneDaily(store, "suncollector")) {
            markDaily(store, "suncollector");
            grantCoins(username, dailySunGoal() / 100,
                    "Daily sun collector quest complete: " + total + " sun gathered today");
        }
        store.save();
    }

    public void onGameFinished(User user, GameSession session, boolean won) {
        UserDataStore store = UserDataStore.forUser(user.getUsername());
        QuestStats stats = session.getQuestStats();
        String username = user.getUsername();
        accumulateKillQuests(username, store, session, stats);
        if (won) {
            evaluateWinQuests(username, store, session, stats);
            updateStreak(username, store, session);
        } else if (session.difficulty() == 5) {
            store.setInt("q.streak", 0);
        }
        store.save();
    }

    private void accumulateKillQuests(String username, UserDataStore store,
                                      GameSession session, QuestStats stats) {
        if (session.getLevel() != null) {
            String chapter = session.getLevel().getChapter().getName();
            int total = store.addInt("q.kills.chapter." + chapter, stats.getTotalKills());
            if (total >= 50 && notDoneOnce(store, "hunter." + chapter)) {
                markOnce(store, "hunter." + chapter);
                grantPackets(username, store, 10,
                        "Chapter hunter quest complete: 50 zombies of " + chapter + " defeated");
            }
        }
        int cactus = store.addInt("q.kills.Cactus." + today(), stats.killsOf("Cactus"));
        if (cactus >= 10 && notDoneDaily(store, "onlycactus")) {
            markDaily(store, "onlycactus");
            grantDiamonds(username, 20, "Only Cactus quest complete: 10 zombies killed by Cactus");
        }
        String specialist = dailySpecialistPlant(store);
        int specialistKills = store.addInt("q.kills." + specialist + "." + today(),
                stats.killsOf(specialist));
        if (specialistKills >= 10 && notDoneDaily(store, "specialist")) {
            markDaily(store, "specialist");
            grantRandomPlant(username, store,
                    "Plant specialist quest complete: 10 kills with " + specialist);
        }
        int firstColumn = store.addInt("q.kills.firstcolumn." + today(),
                stats.getFirstColumnKills());
        if (firstColumn >= 10 && notDoneDaily(store, "almostwinner")) {
            markDaily(store, "almostwinner");
            grantCoins(username, 300,
                    "Almost winner quest complete: 10 zombies stopped at the last column");
        }
    }

    private void evaluateWinQuests(String username, UserDataStore store,
                                   GameSession session, QuestStats stats) {
        if (session.getPlantsLost() <= 2 && notDoneDaily(store, "economical")) {
            markDaily(store, "economical");
            grantPackets(username, store, 18,
                    "Economical gardener quest complete: won losing at most 2 plants");
        }
        if (session.getSunManager().getSunBalance() == 0 && notDoneOnce(store, "epic.defense")) {
            markOnce(store, "epic.defense");
            grantDiamonds(username, 200, "Defense master quest complete: won with exactly 0 sun");
        }
        if (stats.getEarlyKills() >= 10 && notDoneOnce(store, "speed")) {
            markOnce(store, "speed");
            grantCoins(username, 500,
                    "Quick trigger quest complete: 10 kills within 30s of the first wave");
        }
        if (stats.getExplosivesPlanted() >= 3 && notDoneDaily(store, "demolition")) {
            markDaily(store, "demolition");
            grantCoins(username, 100,
                    "Demolition expert quest complete: 3 explosive plants used in one level");
        }
        evaluateLayoutQuests(username, store, session, stats);
        evaluateFamilyQuests(username, store, session, stats);
    }

    private void evaluateLayoutQuests(String username, UserDataStore store,
                                      GameSession session, QuestStats stats) {
        if (!stats.isAnythingPlanted()) {
            return;
        }
        if (isGardenSymmetric(session) && notDoneDaily(store, "symmetry")) {
            markDaily(store, "symmetry");
            grantCoins(username, 500, "Symmetry quest complete: the garden ended symmetric");
        }
        if (isGardenAsymmetric(session) && notDoneDaily(store, "ocd")) {
            markDaily(store, "ocd");
            grantCoins(username, 800,
                    "No-OCD quest complete: no symmetry outside the middle row");
        }
        if (stats.getSunProducersPlanted() <= 3 && notDoneDaily(store, "cloudy")) {
            markDaily(store, "cloudy");
            grantDiamonds(username, 10,
                    "Cloudy day quest complete: won with at most 3 sun producers");
        }
        boolean emptyColumn = !stats.getPlantedColumns().contains(dailyEmptyColumn());
        boolean emptyRow = !stats.getPlantedRows().contains(dailyEmptyRow());
        if (emptyColumn && notDoneDaily(store, "emptycolumn")) {
            markDaily(store, "emptycolumn");
            grantDiamonds(username, 10, "One column less quest complete: column "
                    + dailyEmptyColumn() + " stayed empty");
        }
        if (emptyRow && notDoneDaily(store, "emptyrow")) {
            markDaily(store, "emptyrow");
            grantDiamonds(username, 20, "Defenseless row quest complete: row "
                    + dailyEmptyRow() + " stayed empty");
        }
        if (emptyColumn && emptyRow && notDoneDaily(store, "cross")) {
            markDaily(store, "cross");
            grantDiamonds(username, 25, "Defenseless cross quest complete: row "
                    + dailyEmptyRow() + " and column " + dailyEmptyColumn() + " stayed empty");
        }
    }

    private void evaluateFamilyQuests(String username, UserDataStore store,
                                      GameSession session, QuestStats stats) {
        if (stats.onlyOneFamilyKilled()
                && stats.getPlantedCategories().contains(dailyKillerFamily())
                && notDoneDaily(store, "familykiller")) {
            markDaily(store, "familykiller");
            grantCoins(username, 1000, "Family killer quest complete: every kill came from the "
                    + dailyKillerFamily() + " family");
        }
        if (stats.isAnythingPlanted()
                && !stats.getPlantedCategories().contains(dailyRestrictedFamily())
                && notDoneDaily(store, "restriction")) {
            markDaily(store, "restriction");
            grantDiamonds(username, 100, "Blooming in limits quest complete: won without the "
                    + dailyRestrictedFamily() + " family");
        }
        if (session.getLevel() != null && !session.getLevel().getChapter().isNight()
                && stats.isAllPlantedShroom() && notDoneOnce(store, "epic.nightshroom")) {
            markOnce(store, "epic.nightshroom");
            grantDiamonds(username, 20,
                    "Night or morning quest complete: a day level won with shrooms only");
        }
    }

    private void updateStreak(String username, UserDataStore store, GameSession session) {
        if (session.difficulty() < 5) {
            return;
        }
        int streak = store.addInt("q.streak", 1);
        if (streak >= 5) {
            store.setInt("q.streak", 0);
            grantCoins(username, 5000,
                    "Winning streak quest complete: 5 wins in a row at maximum difficulty");
        }
    }

    private boolean isGardenSymmetric(GameSession session) {
        for (PlacedPlant plant : session.getPlants()) {
            PlacedPlant mirror = session.plantAt(plant.getX(),
                    GameSession.ROWS + 1 - plant.getY());
            if (mirror == null || mirror.getType() != plant.getType()) {
                return false;
            }
        }
        return !session.getPlants().isEmpty();
    }

    private boolean isGardenAsymmetric(GameSession session) {
        int middle = GameSession.ROWS / 2 + 1;
        for (PlacedPlant plant : session.getPlants()) {
            if (plant.getY() == middle) {
                continue;
            }
            PlacedPlant mirror = session.plantAt(plant.getX(),
                    GameSession.ROWS + 1 - plant.getY());
            if (mirror != null && mirror.getType() == plant.getType()) {
                return false;
            }
        }
        return !session.getPlants().isEmpty();
    }

    private boolean notDoneDaily(UserDataStore store, String id) {
        return store.getInt("q.done." + id + "." + today(), 0) == 0;
    }

    private void markDaily(UserDataStore store, String id) {
        store.setInt("q.done." + id + "." + today(), 1);
        store.addInt("questsDone", 1);
        store.addInt("dailyQuestsDone", 1);
    }

    private boolean notDoneOnce(UserDataStore store, String id) {
        return store.getInt("q.done." + id, 0) == 0;
    }

    private void markOnce(UserDataStore store, String id) {
        store.setInt("q.done." + id, 1);
        store.addInt("questsDone", 1);
    }

    public boolean isDoneDaily(UserDataStore store, String id) {
        return !notDoneDaily(store, id);
    }

    public boolean isDoneOnce(UserDataStore store, String id) {
        return !notDoneOnce(store, id);
    }

    private void grantCoins(String username, int amount, String message) {
        UserManager.getInstance().addCoins(amount);
        announce(username, message + " | reward: " + amount + " coins");
    }

    private void grantDiamonds(String username, int amount, String message) {
        UserManager.getInstance().addDiamonds(amount);
        announce(username, message + " | reward: " + amount + " diamonds");
    }

    private void grantPackets(String username, UserDataStore store, int amount, String message) {
        List<String> unlocked = MainController.unlockedPlants(store);
        String plant = unlocked.get((int) (epochDay() % unlocked.size()));
        store.addInt("packets." + plant, amount);
        announce(username, message + " | reward: " + amount + " seed packets of " + plant);
    }

    private void grantRandomPlant(String username, UserDataStore store, String message) {
        List<String> unlocked = MainController.unlockedPlants(store);
        for (models.entities.plant.PlantType type : models.entities.plant.PlantType.values()) {
            if (!unlocked.contains(type.getName())) {
                store.set("plants", String.join(",", unlocked) + "," + type.getName());
                announce(username, message + " | reward: new plant unlocked - " + type.getName());
                return;
            }
        }
        grantCoins(username, 500, message);
    }

    private void announce(String username, String message) {
        System.out.println("[QUEST] " + message);
        NewsStore.add(username, message);
    }
}
