package controllers.menuControllers;

import models.App;
import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.GameSetup;
import models.game.Names;
import models.game.PlacedPlant;
import models.map.Tile;
import models.map.TerrainType;
import models.progress.chapter.Chapter;
import models.progress.level.Level;
import utils.UserDataStore;

import java.util.ArrayList;
import java.util.List;

public class SandboxController extends BaseController {

    public static final String[] CHAPTERS = {"Egypt", "Wavey Beach", "Frost Bite", "Dark Ages"};

    public SandboxController(App app) {
        super(app);
    }

    public List<String> chapters() {
        List<String> names = new ArrayList<>();
        for (String name : CHAPTERS) {
            if (Chapter.getByName(name) != null) {
                names.add(name);
            }
        }
        return names;
    }

    public List<String> levelsOf(String chapterName) {
        List<String> names = new ArrayList<>();
        Chapter chapter = Chapter.getByName(chapterName);
        if (chapter == null) {
            return names;
        }
        for (Level level : chapter.getLevels()) {
            String special = level.getSpecialType() == null ? "Ordinary"
                    : level.getSpecialType().name().replace('_', ' ');
            names.add("Level " + level.getLevelNumber() + " - " + special);
        }
        return names;
    }

    public Result handleOpen(String chapterName, int levelNumber) {
        if (loggedOut()) {
            return notLoggedIn();
        }
        Chapter chapter = Chapter.getByName(chapterName);
        if (chapter == null) {
            return Result.fail("No chapter with this name.");
        }
        if (levelNumber < 1 || levelNumber > chapter.getLevels().size()) {
            return Result.fail("This chapter has " + chapter.getLevels().size() + " levels.");
        }
        Level level = chapter.getLevels().get(levelNumber - 1);
        chapter.setCurrentUnlockedLevel(level);
        UserDataStore store = UserDataStore.forUser(app.getCurrentUser().getUsername());
        List<String> plants = new ArrayList<>();
        for (PlantType type : PlantType.values()) {
            plants.add(type.getName());
        }
        GameSession session = new GameSession(GameSetup.sandbox(app.getCurrentUser(), level,
                plants, MainController.plantLevels(store, plants)));
        session.enterSandbox();
        app.setCurrentGameSession(session);
        session.startGame();
        return Result.ok("Sandbox open on " + chapterName + " level " + levelNumber + ".");
    }

    private GameSession session() {
        return app.getCurrentGameSession();
    }

    public Result handlePlaceZombie(String typeName, int x, int y) {
        GameSession session = session();
        if (session == null || !session.isSandbox()) {
            return Result.fail("The sandbox is not open.");
        }
        ZombieType type = Names.zombie(typeName);
        if (type == null) {
            return Result.fail("No zombie with this name.");
        }
        if (x < 1 || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            return Result.fail("That tile is off the lawn.");
        }
        Zombie zombie = session.spawnZombie(type, x, y, 1);
        return Result.ok(type.getName() + " dropped at (" + x + ", " + y + ")"
                + (zombie == null ? "." : " with " + zombie.getHealth() + " health."));
    }

    public Result handleClearZombies() {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        int count = session.getZombies().size();
        session.getZombies().clear();
        session.getPushedObjects().clear();
        return Result.ok("Swept " + count + " zombies off the lawn.");
    }

    public Result handleClearPlants() {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        int count = session.getPlants().size();
        session.getPlants().clear();
        return Result.ok("Dug up " + count + " plants.");
    }

    public Result handleFeedAll() {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        int fed = 0;
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            session.getCombatManager().applyPlantFood(plant);
            fed++;
        }
        return Result.ok("Fed " + fed + " plants.");
    }

    public Result handleRefill() {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        session.getSunManager().addSun(9990);
        session.setPlantFoods(9);
        return Result.ok("Sun and plant food topped up.");
    }

    public Result handleNextWave() {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        if (!session.getWaveManager().isStarted()) {
            return session.startZombieWaves();
        }
        session.getWaveManager().forceNextWave();
        return Result.ok("Wave " + session.getWaveManager().getCurrentWave() + " rolled in.");
    }

    public Result handleSpawnBoss() {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        if (session.getZombossManager().hasBoss()) {
            return Result.fail("A Zomboss is already on the lawn.");
        }
        models.entities.zombie.Zomboss.BossKind kind = bossOfChapter(session);
        session.getZombossManager().spawn(kind);
        return Result.ok(kind.getTitle() + " has arrived.");
    }

    private models.entities.zombie.Zomboss.BossKind bossOfChapter(GameSession session) {
        String name = session.getLevel() == null ? "" : session.getLevel().getChapter().getName();
        switch (name) {
            case "Frost Bite":
                return models.entities.zombie.Zomboss.BossKind.MAMMOTH;
            case "Wavey Beach":
                return models.entities.zombie.Zomboss.BossKind.SHARK;
            case "Dark Ages":
                return models.entities.zombie.Zomboss.BossKind.DRAGON;
            default:
                return models.entities.zombie.Zomboss.BossKind.ROBOT;
        }
    }

    public List<String> events() {
        GameSession session = session();
        return session == null ? new ArrayList<>()
                : session.getBehaviorManager().availableEvents();
    }

    public Result handleEvent(String event) {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        return Result.ok(session.getBehaviorManager().fireEvent(event));
    }

    public Result handleTerrain(String terrain, int x, int y) {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        Tile tile = session.getGrid().getTile(x - 1, y - 1);
        if (tile == null) {
            return Result.fail("That tile is off the lawn.");
        }
        TerrainType wanted;
        try {
            wanted = TerrainType.valueOf(terrain.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            return Result.fail("No terrain called " + terrain + ".");
        }
        PlacedPlant standing = session.plantAt(x, y);
        if (standing != null) {
            session.getPlants().remove(standing);
        }
        tile.setTerrain(wanted);
        tile.setHasLilyPad(false);
        return Result.ok("Tile (" + x + ", " + y + ") is now " + wanted + ".");
    }

    public Result handleMowers(boolean present) {
        GameSession session = session();
        if (session == null) {
            return Result.fail("The sandbox is not open.");
        }
        session.setLawnMowers(present);
        return Result.ok(present ? "Lawn mowers restored." : "Lawn mowers removed.");
    }

    public Result handleLeave() {
        app.setCurrentGameSession(null);
        return Result.ok("Sandbox closed.");
    }
}
