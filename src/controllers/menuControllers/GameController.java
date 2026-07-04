package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.entities.zombie.ZombieType;
import models.enums.Menus;
import models.game.GamePhase;
import models.game.GameSession;
import models.progress.chapter.Chapter;
import utils.NewsStore;
import utils.UserDataStore;
import views.GameBoardPrinter;

/**
 * Game menu: pre-game plant selection and every in-battle command.
 * Delegates the mechanics to the {@link GameSession}; this class handles
 * payments, persistence of the outcome and navigation.
 */
public class GameController extends BaseController {

    public GameController(App app) {
        super(app);
    }

    private GameSession session() {
        return app.getCurrentGameSession();
    }

    private Result noSession() {
        return Result.fail("There is no active game. Enter a chapter from the main menu.");
    }

    public Result handleShowAllPlants() {
        return session() == null ? noSession() : session().listAllPlants();
    }

    public Result handleShowAvailablePlants() {
        return session() == null ? noSession() : session().listAvailablePlants();
    }

    public Result handleAddPlant(String type) {
        return session() == null ? noSession() : session().addPlantToSelection(type);
    }

    public Result handleRemovePlant(String type) {
        return session() == null ? noSession() : session().removePlantFromSelection(type);
    }

    /** Doc: boosting costs 2 diamonds, or consumes a boost stored in the greenhouse. */
    public Result handleBoostPlant(String type) {
        if (session() == null) {
            return noSession();
        }
        UserDataStore store = new UserDataStore(app.getCurrentUser().getUsername());
        String boostKey = "boost." + type;
        if (store.getInt(boostKey, 0) > 0) {
            Result result = session().markBoosted(type);
            if (result.isSuccessfull()) {
                store.setInt(boostKey, 0);
                store.save();
                result.addMessage("A stored greenhouse boost was used.");
            }
            return result;
        }
        if (app.getCurrentUser().getDiamonds().getAmount() < 2) {
            return Result.fail("Boosting costs 2 diamonds and you have "
                    + app.getCurrentUser().getDiamonds().getAmount() + ".");
        }
        Result result = session().markBoosted(type);
        if (result.isSuccessfull()) {
            UserManager.getInstance().spendDiamonds(2);
        }
        return result;
    }

    public Result handleStartGame() {
        return session() == null ? noSession() : session().startGame();
    }

    public Result handleStartZombieWaves() {
        return session() == null ? noSession() : session().startZombieWaves();
    }

    public Result handleAdvanceTime(int ticks) {
        if (session() == null) {
            return noSession();
        }
        Result result = session().advanceTime(ticks);
        finalizeIfOver();
        return result;
    }

    public Result handleCollectSun(int x, int y) {
        if (session() == null) {
            return noSession();
        }
        Result result = session().collectSun(x, y);
        finalizeIfOver();
        return result;
    }

    public Result handleShowSunAmount() {
        return session() == null ? noSession()
                : Result.ok("Sun: " + session().getSunManager().getSunBalance());
    }

    public Result handleCheatAddSun(int count) {
        if (session() == null) {
            return noSession();
        }
        session().getSunManager().addSun(count);
        return Result.ok("Sun: " + session().getSunManager().getSunBalance());
    }

    public Result handlePlant(String type, int x, int y) {
        return session() == null ? noSession() : session().plantAt(type, x, y);
    }

    public Result handlePluck(int x, int y) {
        return session() == null ? noSession() : session().pluckPlant(x, y);
    }

    public Result handleFeedPlant(int x, int y) {
        if (session() == null) {
            return noSession();
        }
        Result result = session().feedPlant(x, y);
        finalizeIfOver();
        return result;
    }

    public Result handleCheatAddPlantFood() {
        if (session() == null) {
            return noSession();
        }
        session().setPlantFoods(session().getPlantFoods() + 1);
        return Result.ok("Plant foods: " + session().getPlantFoods());
    }

    public Result handleCheatRemoveCooldown() {
        if (session() == null) {
            return noSession();
        }
        session().disableCooldowns();
        return Result.ok("All cooldown limits removed.");
    }

    public Result handleCheatSpawnZombie(String type, int x, int y) {
        return session() == null ? noSession() : session().cheatSpawnZombie(type, x, y);
    }

    public Result handleReleaseNuke() {
        if (session() == null) {
            return noSession();
        }
        Result result = session().releaseNuke();
        finalizeIfOver();
        return result;
    }

    public Result handleShowMap() {
        return session() == null ? noSession() : GameBoardPrinter.showMap(session());
    }

    public Result handleShowPlantsStatus() {
        return session() == null ? noSession() : GameBoardPrinter.showPlantsStatus(session());
    }

    public Result handleShowTileStatus(int x, int y) {
        return session() == null ? noSession() : GameBoardPrinter.showTileStatus(session(), x, y);
    }

    public Result handleZombiesInfo() {
        return session() == null ? noSession() : GameBoardPrinter.zombiesInfo(session());
    }

    /** Persists the outcome once the battle ends and returns to the main menu. */
    private void finalizeIfOver() {
        GameSession session = session();
        if (session == null || !session.isOver()) {
            return;
        }
        UserDataStore store = new UserDataStore(app.getCurrentUser().getUsername());
        recordSeenZombies(session, store);
        if (session.getPhase() == GamePhase.WON) {
            recordVictory(session, store);
        }
        store.save();
        app.setCurrentGameSession(null);
        app.navigateTo(Menus.MAIN);
        System.out.println("Returned to the main menu.");
    }

    private void recordSeenZombies(GameSession session, UserDataStore store) {
        String seen = store.get("zombies", "");
        for (ZombieType type : session.getEncounteredZombies()) {
            if (!seen.contains(type.getName())) {
                NewsStore.add(app.getCurrentUser().getUsername(),
                        "New zombie encountered: " + type.getName());
                seen = seen.isEmpty() ? type.getName() : seen + "," + type.getName();
            }
        }
        store.set("zombies", seen);
    }

    private void recordVictory(GameSession session, UserDataStore store) {
        Chapter chapter = session.getLevel().getChapter();
        int levelNumber = session.getLevel().getLevelNumber();
        String progressKey = "progress." + chapter.getName();
        if (store.getInt(progressKey, 1) == levelNumber && levelNumber < chapter.getLevels().size()) {
            store.setInt(progressKey, levelNumber + 1);
            NewsStore.add(app.getCurrentUser().getUsername(),
                    "New level unlocked: " + chapter.getName() + " level " + (levelNumber + 1));
        }
        store.addInt("completed." + chapter.getName(), 1);
        int reward = 100 + 50 * levelNumber;
        UserManager.getInstance().addCoins(reward);
        System.out.println("Victory reward: " + reward + " coins.");
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == null) {
            return Result.fail("No menu with the given name");
        }
        if (menu == Menus.COLLECTION || menu == Menus.MAIN) {
            app.navigateTo(menu);
            return Result.ok("Redirected to " + menuName + " menu");
        }
        return Result.fail("You can't move to " + menuName + " from the game menu");
    }

    /** Doc: exiting the game menu returns the player to the main menu. */
    public Result handleExit() {
        app.navigateTo(Menus.MAIN);
        return Result.ok("Redirected to Main menu");
    }
}
