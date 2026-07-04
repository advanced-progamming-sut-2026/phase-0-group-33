package models.game;

import controllers.managers.CombatManager;
import controllers.managers.SunManager;
import controllers.managers.WaveManager;
import models.Result;
import models.entities.plant.PlantCategory;
import models.entities.plant.PlantTag;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.map.Grid;
import models.map.TerrainType;
import models.map.Tile;
import models.progress.level.Level;
import models.progress.level.special.SpecialLevelType;
import models.user.User;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * One run of a level: plant selection (preparation), then the
 * battle. 10 ticks equal one second. Implements waves, lawnmowers,
 * plant food, cheats, and the win/lose flow.
 */
public class GameSession {
    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int TICKS_PER_SECOND = 10;
    private static final int DEFAULT_STARTING_SUN = 50;

    private final User user;
    private final Level level;
    private final Grid grid = new Grid(ROWS, COLS);
    private final boolean[] lawnMowers = new boolean[ROWS + 1];
    private final PlantSelection selection;
    private final List<PlacedPlant> plants = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();
    private final Set<ZombieType> encounteredZombies = new HashSet<>();
    private final Random random = new Random();
    private final SunManager sunManager;
    private final WaveManager waveManager;
    private final CombatManager combatManager;
    private GamePhase phase = GamePhase.PREPARATION;
    private int tickCount;
    private int plantFoods;
    private boolean cooldownsDisabled;
    private int plantsLost;
    private int zombiesKilled;
    private int beltTicks;
    private int timerTicksLeft = -1;

    public GameSession(User user, Level level, List<String> unlockedPlantNames, int initialPlantFoods) {
        this.user = user;
        this.level = level;
        this.selection = new PlantSelection(new ArrayList<>(unlockedPlantNames), level.getSpecialType());
        this.plantFoods = Math.min(3, initialPlantFoods);
        int dl = user.getDifficultyLevel().getLevelNumber();
        boolean sky = !level.getChapter().isNight() && !isSpecial(SpecialLevelType.NIGHT_OPS)
                && !isSpecial(SpecialLevelType.PLANT_WHAT_YOU_GET);
        int startingSun = isSpecial(SpecialLevelType.PLANT_WHAT_YOU_GET) ? 800 : DEFAULT_STARTING_SUN;
        this.sunManager = new SunManager(startingSun, sky, dl / 3.0, random);
        this.waveManager = new WaveManager(this, level.getChapter().getZombiePool(),
                level.getWaveCount(), level.getBaseWaveBudget(), 3.0 / dl, random);
        this.combatManager = new CombatManager(this);
        initBoard();
        if (isSpecial(SpecialLevelType.TIMED_WAR)) {
            timerTicksLeft = 120 * TICKS_PER_SECOND;
        }
    }

    private void initBoard() {
        int waterColumns = level.getChapter().getWaterColumns();
        for (int row = 1; row <= ROWS; row++) {
            lawnMowers[row] = true;
            for (int col = 1; col <= COLS; col++) {
                TerrainType terrain = col > COLS - waterColumns ? TerrainType.WATER : TerrainType.NORMAL;
                grid.setTile(col - 1, row - 1, new Tile(new Point(col, row), terrain, col == 1));
            }
        }
        for (int i = 0; i < level.getChapter().getGraveCount(); i++) {
            int col = 3 + random.nextInt(COLS - 3);
            int row = 1 + random.nextInt(ROWS);
            Tile tile = grid.getTile(col - 1, row - 1);
            if (tile.getTerrain() == TerrainType.NORMAL) {
                grid.setTile(col - 1, row - 1, new Tile(new Point(col, row), TerrainType.GRAVE, false));
            }
        }
    }

    private boolean isSpecial(SpecialLevelType type) {
        return level.getSpecialType() == type;
    }

    // ---- Preparation ----

    public Result listAllPlants() {
        return selection.listAllPlants();
    }

    public Result listAvailablePlants() {
        return selection.listAvailablePlants();
    }

    public Result addPlantToSelection(String typeName) {
        return selection.add(typeName, phase == GamePhase.PREPARATION);
    }

    public Result removePlantFromSelection(String typeName) {
        return selection.remove(typeName, phase == GamePhase.PREPARATION);
    }

    public Result markBoosted(String typeName) {
        return selection.markBoosted(typeName);
    }

    public Result startGame() {
        if (phase != GamePhase.PREPARATION) {
            return Result.fail("The game has already started.");
        }
        if (selection.isEmpty() && !isSpecial(SpecialLevelType.CONVEYOR_BELT)) {
            return Result.fail("Pick at least one plant first.");
        }
        phase = GamePhase.BATTLE;
        if (isSpecial(SpecialLevelType.SAVE_OUR_SEEDS)) {
            placeProtectedSeeds();
        }
        return Result.ok("The battle begins! Use 'start zombie waves' to summon the horde.");
    }

    private void placeProtectedSeeds() {
        for (int row : new int[]{2, 4}) {
            PlacedPlant seed = new PlacedPlant(PlantType.WALL_NUT, 1, row, PlantType.WALL_NUT.getBaseHp());
            seed.setProtectedSeed(true);
            plants.add(seed);
            System.out.printf("Protect the plant at (1, %d) or you lose!%n", row);
        }
    }

    // ----- Battle commands -----

    public Result startZombieWaves() {
        if (phase != GamePhase.BATTLE) {
            return Result.fail("Start the game first.");
        }
        if (waveManager.isStarted()) {
            return Result.fail("The zombie waves have already started.");
        }
        waveManager.startWaves();
        return Result.ok();
    }

    public Result advanceTime(int ticks) {
        if (phase != GamePhase.BATTLE) {
            return Result.fail("There is no running game to advance.");
        }
        for (int i = 0; i < ticks && phase == GamePhase.BATTLE; i++) {
            tick();
        }
        return Result.ok();
    }

    private void tick() {
        tickCount++;
        sunManager.tick();
        for (PlantSlot slot : selection.getSlots()) {
            slot.tick();
        }
        if (isSpecial(SpecialLevelType.CONVEYOR_BELT)) {
            conveyorTick();
        }
        combatManager.plantsAct();
        combatManager.zombiesAct();
        if (phase != GamePhase.BATTLE) {
            return;
        }
        waveManager.tick();
        tickTimer();
        if (waveManager.allWavesCleared()) {
            winGame();
        }
    }

    /** conveyor belt level: every 12 seconds the belt delivers a random plant. */
    private void conveyorTick() {
        beltTicks++;
        if (beltTicks % (12 * TICKS_PER_SECOND) != 0 || selection.isFull()) {
            return;
        }
        List<String> names = selection.getUnlockedPlantNames();
        PlantType type = resolvePlantType(names.get(random.nextInt(names.size())));
        if (type != null) {
            PlantSlot slot = new PlantSlot(type);
            slot.setSingleUse(true);
            selection.getSlots().add(slot);
            System.out.println("The conveyor belt delivered a " + type.getName() + ".");
        }
    }

    private void tickTimer() {
        if (timerTicksLeft < 0) {
            return;
        }
        timerTicksLeft--;
        if (timerTicksLeft == 0) {
            if (zombiesKilled >= 12) {
                winGame();
            } else {
                loseGame("Time is up! You only killed " + zombiesKilled + " of 12 zombies.");
            }
        }
    }

    public Result plantAt(String typeName, int x, int y) {
        if (phase != GamePhase.BATTLE) {
            return Result.fail("Start the game before planting.");
        }
        PlantType type = resolvePlantType(typeName);
        PlantSlot slot = type == null ? null : selection.findSlot(type);
        if (slot == null) {
            return Result.fail("This plant is not among your chosen plants.");
        }
        Result placement = validatePlacement(type, x, y, slot);
        if (placement != null) {
            return placement;
        }
        boolean free = isSpecial(SpecialLevelType.CONVEYOR_BELT);
        if (!free && !sunManager.spendSun(type.getCost())) {
            return Result.fail("Not enough sun: " + type.getName() + " costs " + type.getCost()
                    + " and you have " + sunManager.getSunBalance() + ".");
        }
        return placePlant(type, x, y, slot);
    }

    private Result validatePlacement(PlantType type, int x, int y, PlantSlot slot) {
        if (x < 1 || x > COLS || y < 1 || y > ROWS) {
            return Result.fail("Coordinates are outside the lawn.");
        }
        if (!slot.isReady()) {
            return Result.fail("This plant is recharging; wait "
                    + (slot.getCooldownTicks() / TICKS_PER_SECOND + 1) + " more seconds.");
        }
        Tile tile = grid.getTile(x - 1, y - 1);
        if (tile.getTerrain() == TerrainType.GRAVE) {
            return Result.fail("You cannot plant on a gravestone.");
        }
        if (plantAt(x, y) != null) {
            return Result.fail("There is already a plant on this tile.");
        }
        boolean waterPlant = type.getTags().contains(PlantTag.WATER);
        if (tile.getTerrain() == TerrainType.WATER && !waterPlant && !tile.isHasLilyPad()) {
            return Result.fail("You need a Lily Pad to plant here.");
        }
        if (tile.getTerrain() != TerrainType.WATER && type == PlantType.LILY_PAD) {
            return Result.fail("Lily Pads can only be planted on water.");
        }
        return null;
    }

    private Result placePlant(PlantType type, int x, int y, PlantSlot slot) {
        Tile tile = grid.getTile(x - 1, y - 1);
        if (type == PlantType.LILY_PAD) {
            tile.setHasLilyPad(true);
        } else {
            PlacedPlant plant = new PlacedPlant(type, x, y, Math.max(1, type.getBaseHp()));
            if (type.getCategory() == PlantCategory.EXPLOSIVE
                    && !type.getTags().contains(PlantTag.TRAP)) {
                plant.setFuseTicks(15);
            }
            plants.add(plant);
            if (slot.isBoosted()) {
                combatManager.applyPlantFood(plant);
                slot.setBoosted(false);
            }
        }
        if (slot.isSingleUse()) {
            selection.getSlots().remove(slot);
        } else if (!cooldownsDisabled) {
            slot.setCooldownTicks(type.getRecharge() * TICKS_PER_SECOND);
        }
        return Result.ok(type.getName() + " planted at (" + x + ", " + y + ").");
    }

    public Result pluckPlant(int x, int y) {
        PlacedPlant plant = plantAt(x, y);
        if (plant == null) {
            return Result.fail("There is no plant on this tile.");
        }
        removePlant(plant, false);
        return Result.ok(plant.getType().getName() + " was removed from (" + x + ", " + y + ").");
    }

    public Result feedPlant(int x, int y) {
        if (plantFoods <= 0) {
            return Result.fail("You have no plant food.");
        }
        PlacedPlant plant = plantAt(x, y);
        if (plant == null) {
            return Result.fail("There is no plant on this tile.");
        }
        plantFoods--;
        combatManager.applyPlantFood(plant);
        return Result.ok("Plant food fed to " + plant.getType().getName()
                + "; you have " + plantFoods + " plant foods left.");
    }

    public Result collectSun(int x, int y) {
        Sun collected = sunManager.collectAt(x, y);
        if (collected == null) {
            return Result.fail("There is no sun to collect at (" + x + ", " + y + ").");
        }
        if (collected.isFalling()) {
            combatManager.applyRadioactiveExplosion(x, y);
            return Result.ok("The radioactive sun exploded!");
        }
        if (collected.isProducedByPlant()) {
            PlacedPlant producer = plantAt(x, y);
            if (producer != null) {
                producer.setSunPending(false);
            }
        }
        return Result.ok("Sun collected. You now have " + sunManager.getSunBalance() + " sun.");
    }

    public Result releaseNuke() {
        if (phase != GamePhase.BATTLE) {
            return Result.fail("There is no running game.");
        }
        for (Zombie zombie : new ArrayList<>(zombies)) {
            combatManager.damageZombie(zombie, Integer.MAX_VALUE / 2);
        }
        return Result.ok("The nuke wiped every zombie off the map.");
    }

    public Result cheatSpawnZombie(String typeName, int x, int y) {
        ZombieType type = resolveZombieType(typeName);
        if (type == null) {
            return Result.fail("No zombie with this name exists.");
        }
        if (x < 1 || x > COLS || y < 1 || y > ROWS) {
            return Result.fail("Coordinates are outside the lawn.");
        }
        spawnZombie(type, x, y, waveManager.getCurrentWave());
        return Result.ok(type.getName() + " spawned at (" + x + ", " + y + ").");
    }

    // Shared state used by the managers -----------------------------------

    public Zombie spawnZombie(ZombieType type, double x, int row, int wave) {
        Zombie zombie = ZombieFactory.create(type, x, row, getHealthFactor());
        zombie.setSpawnWave(wave);
        zombie.setGlowing(random.nextInt(100) < 5);
        zombies.add(zombie);
        encounteredZombies.add(type);
        return zombie;
    }

    public PlacedPlant plantAt(int x, int y) {
        for (PlacedPlant plant : plants) {
            if (plant.getX() == x && plant.getY() == y) {
                return plant;
            }
        }
        return null;
    }

    /** Removes a plant; when a zombie destroyed it, prints the doc message and checks specials. */
    public void removePlant(PlacedPlant plant, boolean killedByZombie) {
        plants.remove(plant);
        sunManager.clearProducedAt(plant.getX(), plant.getY());
        if (!killedByZombie) {
            return;
        }
        System.out.printf("Plant %s at (%d, %d) is destroyed.%n",
                plant.getType().getName(), plant.getX(), plant.getY());
        plantsLost++;
        if (plant.isProtectedSeed()) {
            loseGame("A protected plant was lost. You failed to save our seeds!");
        } else if (isSpecial(SpecialLevelType.LOVE_YOUR_PLANTS) && plantsLost >= 5) {
            loseGame("You lost 5 plants. The garden mourns; you lose!");
        }
    }

    /** Dead Line special: crossing the marked line loses the level instantly. */
    public void checkDeadline(Zombie zombie) {
        if (isSpecial(SpecialLevelType.DEAD_LINE) && zombie.getPosition().getX() < 4) {
            loseGame("A zombie crossed the dead line; you lose!");
        }
    }

    public void loseGame(String message) {
        if (phase == GamePhase.BATTLE) {
            phase = GamePhase.LOST;
            System.out.println(message);
        }
    }

    private void winGame() {
        phase = GamePhase.WON;
        System.out.println("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
    }

    public void countKill() {
        zombiesKilled++;
    }

    // Accessors ------------------------------------------------------------

    public boolean hasLawnMower(int row) {
        return row >= 1 && row <= ROWS && lawnMowers[row];
    }

    public void useLawnMower(int row) {
        lawnMowers[row] = false;
    }

    public boolean isOver() {
        return phase == GamePhase.WON || phase == GamePhase.LOST;
    }

    public static PlantType resolvePlantType(String name) {
        if (name == null) {
            return null;
        }
        for (PlantType type : PlantType.values()) {
            if (normalize(type.getName()).equals(normalize(name))) {
                return type;
            }
        }
        return null;
    }

    public static ZombieType resolveZombieType(String name) {
        if (name == null) {
            return null;
        }
        for (ZombieType type : ZombieType.values()) {
            if (normalize(type.getName()).equals(normalize(name))) {
                return type;
            }
        }
        return null;
    }

    private static String normalize(String text) {
        return text.replaceAll("[\\s_-]", "").toLowerCase();
    }

    public User getUser() {
        return user;
    }

    public Level getLevel() {
        return level;
    }

    public Grid getGrid() {
        return grid;
    }

    public List<PlantSlot> getSlots() {
        return selection.getSlots();
    }

    public List<PlacedPlant> getPlants() {
        return plants;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public Set<ZombieType> getEncounteredZombies() {
        return encounteredZombies;
    }

    public SunManager getSunManager() {
        return sunManager;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public int getTickCount() {
        return tickCount;
    }

    public int getPlantFoods() {
        return plantFoods;
    }

    public void setPlantFoods(int plantFoods) {
        this.plantFoods = Math.min(3, plantFoods);
    }

    public void disableCooldowns() {
        this.cooldownsDisabled = true;
        for (PlantSlot slot : selection.getSlots()) {
            slot.setCooldownTicks(0);
        }
    }

    public Random getRandom() {
        return random;
    }

    public double getHealthFactor() {
        return user.getDifficultyLevel().getLevelNumber() / 3.0;
    }

    public double getDamageFactor() {
        return user.getDifficultyLevel().getLevelNumber() / 3.0;
    }
}
