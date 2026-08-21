package models.game;

import controllers.managers.BattleCommands;
import controllers.managers.CombatManager;
import controllers.managers.MinigameManager;
import controllers.managers.PlantActionManager;
import controllers.managers.PlantingManager;
import controllers.managers.ProjectileManager;
import controllers.managers.ZombossManager;
import controllers.managers.SunManager;
import controllers.managers.WaveManager;
import controllers.managers.ZombieBehaviorManager;
import models.Result;
import models.entities.plant.PlantType;
import models.entities.plant.PlantUpgrades;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.map.Grid;
import models.progress.level.Level;
import models.progress.level.special.SpecialLevelType;
import models.user.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameSession {
    public static final int ROWS = 5;
    private static final int SANDBOX_SUN = 9990;
    private static final int SANDBOX_FOOD = 9;
    public static final int COLS = 9;
    public static final int TICKS_PER_SECOND = 10;
    private static final int DEFAULT_STARTING_SUN = 50;

    private final GameSetup setup;
    private final Grid grid = new Grid(ROWS, COLS);
    private final boolean[] lawnMowers = new boolean[ROWS + 1];
    private final boolean[] brains = new boolean[ROWS + 1];
    private final PlantSelection selection;
    private final List<PlacedPlant> plants = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<PushedObject> pushedObjects = new ArrayList<>();
    private final Set<ZombieType> encounteredZombies = new HashSet<>();
    private final Random random;
    private final SunManager sunManager;
    private final WaveManager waveManager;
    private final CombatManager combatManager;
    private final PlantActionManager plantActionManager;
    private final ZombieBehaviorManager behaviorManager;
    private final PlantingManager plantingManager;
    private final BattleCommands battleCommands;
    private final MinigameManager minigameManager;
    private final ProjectileManager projectileManager;
    private final ZombossManager zombossManager;
    private final ScoreTracker scoreTracker = new ScoreTracker();
    private final QuestStats questStats = new QuestStats();
    private GamePhase phase = GamePhase.PREPARATION;
    private int tickCount;
    private int plantFoods;
    private final java.util.List<PlantFoodDrop> plantFoodDrops = new java.util.ArrayList<>();
    private boolean cooldownsDisabled;
    private boolean sandbox;
    private int plantsLost;
    private int zombiesKilled;
    private int timerTicksLeft = -1;
    private String farewell;
    private final java.util.List<int[]> detonations = new java.util.ArrayList<>();
    private final java.util.List<PlantType> detonationTypes = new java.util.ArrayList<>();

    public GameSession(GameSetup setup) {
        this.setup = setup;
        this.random = setup.getSeed() >= 0 ? new Random(setup.getSeed()) : new Random();
        this.selection = new PlantSelection(new ArrayList<>(setup.getUnlockedPlants()),
                setup.getLevel());
        this.plantFoods = Math.min(3, setup.getPlantFoods());
        this.sunManager = new SunManager(startingSun(), skyEnabled(), difficulty() / 3.0, random);
        this.waveManager = new WaveManager(this, zombiePool(), waveCount(), waveBudget(),
                3.0 / difficulty(), random);
        this.waveManager.setEndless(getMode() == GameMode.SCORING);
        this.combatManager = new CombatManager(this);
        this.plantActionManager = new PlantActionManager(this, combatManager);
        this.behaviorManager = new ZombieBehaviorManager(this, combatManager);
        this.plantingManager = new PlantingManager(this);
        this.battleCommands = new BattleCommands(this);
        this.minigameManager = new MinigameManager(this, setup.getDifficultyTier());
        this.projectileManager = new ProjectileManager(this, combatManager);
        this.zombossManager = new ZombossManager(this, combatManager);
        BoardBuilder.build(grid, setup.getLevel() == null ? null : setup.getLevel().getChapter(), random);
        initModeState();
    }

    private void initModeState() {
        for (int row = 1; row <= ROWS; row++) {
            lawnMowers[row] = getMode() != GameMode.I_ZOMBIE;
            brains[row] = getMode() == GameMode.I_ZOMBIE;
        }
        if (isSpecial(SpecialLevelType.TIMED_WAR)) {
            timerTicksLeft = 120 * TICKS_PER_SECOND;
        }
        minigameManager.setUpBoard();
        if (minigameManager.startsImmediately()) {
            phase = GamePhase.BATTLE;
            spawnBossIfNeeded();
        }
    }

    private int startingSun() {
        if (getMode() == GameMode.I_ZOMBIE) {
            return 100 + 50 * setup.getDifficultyTier();
        }
        return isSpecial(SpecialLevelType.PLANT_WHAT_YOU_GET) ? 800 : DEFAULT_STARTING_SUN;
    }

    private boolean skyEnabled() {
        if (getMode() != GameMode.ADVENTURE && getMode() != GameMode.SCORING
                && getMode() != GameMode.ZOMBOTANY) {
            return false;
        }
        boolean night = setup.getLevel() != null && setup.getLevel().getChapter().isNight();
        return !night && !isSpecial(SpecialLevelType.NIGHT_OPS)
                && !isSpecial(SpecialLevelType.PLANT_WHAT_YOU_GET);
    }

    private List<ZombieType> zombiePool() {
        if (setup.getZombiePoolOverride() != null) {
            return setup.getZombiePoolOverride();
        }
        return setup.getLevel().getChapter().getZombiePool();
    }

    private int waveCount() {
        return setup.getLevel() == null ? 2 + setup.getDifficultyTier()
                : setup.getLevel().getWaveCount();
    }

    private int waveBudget() {
        return setup.getLevel() == null ? 150 + 100 * setup.getDifficultyTier()
                : setup.getLevel().getBaseWaveBudget();
    }

    public boolean isSpecial(SpecialLevelType type) {
        return setup.getLevel() != null && setup.getLevel().getSpecialType() == type;
    }

    public Result listAllPlants() {
        return selection.listAllPlants();
    }

    public Result listAvailablePlants() {
        return selection.listAvailablePlants();
    }

    public Result addPlantToSelection(String typeName) {
        Result result = selection.add(typeName, phase == GamePhase.PREPARATION);
        if (result.isSuccessfull()) {
            applyStoredBoost(Names.plant(typeName));
        }
        return result;
    }

    private void applyStoredBoost(models.entities.plant.PlantType type) {
        PlantSlot slot = type == null ? null : selection.findSlot(type);
        if (slot == null || slot.isBoosted()) {
            return;
        }
        utils.UserDataStore store = utils.UserDataStore.forUser(getUser().getUsername());
        if (store.getInt("boost." + type.getName(), 0) > 0) {
            slot.setBoosted(true);
        }
    }

    public boolean isGreenhouseBoost(models.entities.plant.PlantType type) {
        if (type == null) {
            return false;
        }
        utils.UserDataStore store = utils.UserDataStore.forUser(getUser().getUsername());
        return store.getInt("boost." + type.getName(), 0) > 0;
    }

    private void spendStoredBoosts() {
        utils.UserDataStore store = utils.UserDataStore.forUser(getUser().getUsername());
        boolean dirty = false;
        for (PlantSlot slot : selection.getSlots()) {
            String key = "boost." + slot.getType().getName();
            if (slot.isBoosted() && store.getInt(key, 0) > 0) {
                store.setInt(key, 0);
                dirty = true;
            }
        }
        if (dirty) {
            store.save();
        }
    }

    public Result removePlantFromSelection(String typeName) {
        return selection.remove(typeName, phase == GamePhase.PREPARATION);
    }

    public Result markBoosted(String typeName) {
        return selection.markBoosted(typeName);
    }

    public Result canBoost(String typeName) {
        return selection.canBoost(typeName);
    }

    public Result startGame() {
        if (phase != GamePhase.PREPARATION) {
            return Result.fail("The game has already started.");
        }
        if (selection.isEmpty() && !sandbox
                && !isSpecial(SpecialLevelType.CONVEYOR_BELT)) {
            return Result.fail("Pick at least one plant first.");
        }
        phase = GamePhase.BATTLE;
        spendStoredBoosts();
        if (isSpecial(SpecialLevelType.SAVE_OUR_SEEDS)) {
            placeProtectedSeeds();
        }
        behaviorManager.spawnFrozenZombiesIfFrostbite();
        spawnBossIfNeeded();
        return Result.ok("The battle begins! Use 'start zombie waves' to summon the horde.");
    }

    private void spawnBossIfNeeded() {
        if (isBossLevel()) {
            zombossManager.spawn(((models.progress.level.BossLevel) setup.getLevel()).getBossKind());
        }
    }

    private void placeProtectedSeeds() {
        for (int row : new int[] { 2, 4 }) {
            PlacedPlant seed = new PlacedPlant(PlantType.WALL_NUT, 1, row, PlantType.WALL_NUT.getBaseHp());
            seed.setProtectedSeed(true);
            plants.add(seed);
            System.out.printf("Protect the plant at (1, %d) or you lose!%n", row);
        }
    }

    public Result advanceTime(int ticks) {
        return battleCommands.advanceTime(ticks);
    }

    public Result collectSun(int x, int y) {
        return battleCommands.collectSun(x, y);
    }

    public Result startZombieWaves() {
        return battleCommands.startZombieWaves();
    }

    public Result plantAt(String typeName, int x, int y) {
        return plantingManager.plant(typeName, x, y);
    }

    public Result pluckPlant(int x, int y) {
        return plantingManager.pluck(x, y);
    }

    public Result feedPlant(int x, int y) {
        return battleCommands.feedPlant(x, y);
    }

    public Result cheatSpawnZombie(String typeName, int x, int y) {
        return battleCommands.cheatSpawnZombie(typeName, x, y);
    }

    public Result releaseNuke() {
        return battleCommands.releaseNuke();
    }

    public static PlantType resolvePlantType(String name) {
        return Names.plant(name);
    }

    public static ZombieType resolveZombieType(String name) {
        return Names.zombie(name);
    }

    public Zombie spawnZombie(ZombieType type, double x, int row, int wave) {
        Zombie zombie = ZombieFactory.create(type, x, row, getHealthFactor());
        zombie.setSpawnWave(wave);
        zombie.getBattle().setSpawnTick(tickCount);
        if (getMode() != GameMode.I_ZOMBIE) {
            zombie.setGlowing(random.nextInt(100) < 5);
        }
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

    public void removePlant(PlacedPlant plant, boolean killedByZombie) {
        plants.remove(plant);
        sunManager.clearProducedAt(plant.getX(), plant.getY());
        if (!killedByZombie) {
            return;
        }
        System.out.printf("Plant %s at (%d, %d) is destroyed.%n",
                plant.getType().getName(), plant.getX(), plant.getY());
        plantsLost++;
        scoreTracker.onPlantLost();
        combatManager.onPlantEaten(plant);
        minigameManager.onPlantEaten(plant);
        if (plant.isProtectedSeed()) {
            loseGame("A protected plant was lost. You failed to save our seeds!");
        } else if (isSpecial(SpecialLevelType.LOVE_YOUR_PLANTS) && plantsLost >= 5) {
            loseGame("You lost 5 plants. The garden mourns; you lose!");
        }
    }

    public void checkDeadline(Zombie zombie) {
        if (isSpecial(SpecialLevelType.DEAD_LINE) && zombie.getPosition().getX() < 4) {
            loseGame("A zombie crossed the dead line; you lose!");
        }
    }

    public void loseGame(String message) {
        if (phase == GamePhase.BATTLE) {
            phase = GamePhase.LOST;
            if (getMode() == GameMode.SCORING) {
                scoreTracker.onGameWon(unusedMowerCount());
                System.out.println("Your miopoint score: " + scoreTracker.getScore());
            }
            System.out.println(message);
        }
    }

    public void winGame() {
        if (phase != GamePhase.BATTLE) {
            return;
        }
        phase = GamePhase.WON;
        scoreTracker.onGameWon(unusedMowerCount());
        if (getMode() == GameMode.ADVENTURE || getMode() == GameMode.ZOMBOTANY
                || getMode() == GameMode.SCORING) {
            farewell = "Dear humanz, zis is not done yet;"
                    + " we will come back to eat your brainz, humanz.";
        } else {
            farewell = "You won the minigame!";
        }
        System.out.println(farewell);
        if (getMode() == GameMode.SCORING) {
            System.out.println("Your miopoint score: " + scoreTracker.getScore());
        }
    }

    public void countKill(Zombie zombie) {
        zombiesKilled++;
        scoreTracker.onZombieKilled(tickCount, zombie.getBattle().getSpawnTick());
    }

    public QuestStats getQuestStats() {
        return questStats;
    }

    public int getPlantsLost() {
        return plantsLost;
    }

    public double getSpeedFactor() {
        return difficulty() / 3.0;
    }

    public int unusedMowerCount() {
        int count = 0;
        for (int row = 1; row <= ROWS; row++) {
            if (lawnMowers[row]) {
                count++;
            }
        }
        return count;
    }

    private final java.util.List<RunningMower> runningMowers = new java.util.ArrayList<>();

    public java.util.List<RunningMower> getRunningMowers() {
        return runningMowers;
    }

    public void startLawnMower(int row) {
        if (!hasLawnMower(row)) {
            return;
        }
        lawnMowers[row] = false;
        runningMowers.add(new RunningMower(row, 0.4));
    }

    public void setLawnMowers(boolean present) {
        for (int row = 1; row <= ROWS; row++) {
            lawnMowers[row] = present;
        }
        if (!present) {
            runningMowers.clear();
        }
    }

    public boolean hasLawnMower(int row) {
        return row >= 1 && row <= ROWS && lawnMowers[row];
    }

    public void useLawnMower(int row) {
        lawnMowers[row] = false;
    }

    public boolean hasBrain(int row) {
        return row >= 1 && row <= ROWS && brains[row];
    }

    public void eatBrain(int row) {
        brains[row] = false;
    }

    public boolean isOver() {
        return phase == GamePhase.WON || phase == GamePhase.LOST;
    }

    public PlantSlot findSlot(PlantType type) {
        return selection.findSlot(type);
    }

    public PlantSlot addSandboxSlot(PlantType type) {
        if (!sandbox) {
            return null;
        }
        return selection.addFreely(type);
    }

    public int effectiveCost(PlantType type) {
        if (sandbox) {
            return 0;
        }
        return Math.max(0, type.getCost() - PlantUpgrades.costReduction(type, plantLevel(type)));
    }

    public int effectiveHp(PlantType type) {
        return Math.max(1, type.getBaseHp() + PlantUpgrades.hpBonus(type, plantLevel(type)));
    }

    public int effectiveDamage(PlantType type) {
        int base = type.getDamage() < 0 ? 9999 : type.getDamage();
        return base + PlantUpgrades.damageBonus(type, plantLevel(type));
    }

    public int effectiveRecharge(PlantType type) {
        return Math.max(0,
                type.getRecharge() - PlantUpgrades.rechargeReduction(type, plantLevel(type)));
    }

    public int plantUpgradeLevel(PlantType type) {
        return plantLevel(type);
    }

    private int plantLevel(PlantType type) {
        return Math.max(1, setup.getPlantLevels().getOrDefault(type.getName(), 1));
    }

    public boolean usesWaves() {
        GameMode mode = getMode();
        return mode == GameMode.ADVENTURE || mode == GameMode.SCORING
                || mode == GameMode.ZOMBOTANY || mode == GameMode.WALLNUT_BOWLING;
    }

    public GameMode getMode() {
        return setup.getMode();
    }

    public int difficulty() {
        return setup.getUser().getDifficultyLevel().getLevelNumber();
    }

    public User getUser() {
        return setup.getUser();
    }

    public Level getLevel() {
        return setup.getLevel();
    }

    public Grid getGrid() {
        return grid;
    }

    public List<PlantSlot> getSlots() {
        return selection.getSlots();
    }

    public PlantSelection getSelection() {
        return selection;
    }

    public List<PlacedPlant> getPlants() {
        return plants;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public List<PushedObject> getPushedObjects() {
        return pushedObjects;
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

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public PlantActionManager getPlantActionManager() {
        return plantActionManager;
    }

    public ZombieBehaviorManager getBehaviorManager() {
        return behaviorManager;
    }

    public PlantingManager getPlantingManager() {
        return plantingManager;
    }

    public BattleCommands getBattleCommands() {
        return battleCommands;
    }

    public MinigameManager getMinigameManager() {
        return minigameManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public ZombossManager getZombossManager() {
        return zombossManager;
    }

    public boolean isBossLevel() {
        return setup.getLevel() instanceof models.progress.level.BossLevel;
    }

    public int getDifficultyTier() {
        return setup.getDifficultyTier();
    }

    public ScoreTracker getScoreTracker() {
        return scoreTracker;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void incrementTick() {
        tickCount++;
    }

    public int getPlantFoods() {
        return plantFoods;
    }

    public void setPlantFoods(int plantFoods) {
        this.plantFoods = Math.min(sandbox ? SANDBOX_FOOD : 3, plantFoods);
    }

    public java.util.List<PlantFoodDrop> getPlantFoodDrops() {
        return plantFoodDrops;
    }

    public void dropPlantFood(int x, int y) {
        plantFoodDrops.add(new PlantFoodDrop(
                Math.max(1, Math.min(COLS, x)), Math.max(1, Math.min(ROWS, y))));
    }

    public boolean collectPlantFoodAt(int x, int y) {
        for (PlantFoodDrop drop : plantFoodDrops) {
            if (drop.getX() == x && drop.getY() == y && !drop.isFalling()) {
                plantFoodDrops.remove(drop);
                setPlantFoods(plantFoods + 1);
                return true;
            }
        }
        return false;
    }

    public void tickPlantFoodDrops() {
        for (int i = plantFoodDrops.size() - 1; i >= 0; i--) {
            if (plantFoodDrops.get(i).tick()) {
                plantFoodDrops.remove(i);
            }
        }
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void enterSandbox() {
        this.sandbox = true;
        this.cooldownsDisabled = true;
        sunManager.addSun(SANDBOX_SUN);
        plantFoods = SANDBOX_FOOD;
    }

    public boolean isCooldownsDisabled() {
        return cooldownsDisabled || sandbox;
    }

    public void disableCooldowns() {
        this.cooldownsDisabled = true;
        for (PlantSlot slot : selection.getSlots()) {
            slot.setCooldownTicks(0);
        }
    }

    private final java.util.List<WhirlwindRide> whirlwinds = new java.util.ArrayList<>();

    private final java.util.List<Object[]> mintBursts = new java.util.ArrayList<>();

    public void recordMint(PlantType type, int x, int y, java.util.List<int[]> touched) {
        if (mintBursts.size() > 16) {
            return;
        }
        mintBursts.add(new Object[] {type, x, y, touched});
    }

    public java.util.List<Object[]> drainMints() {
        java.util.List<Object[]> copy = new java.util.ArrayList<>(mintBursts);
        mintBursts.clear();
        return copy;
    }

    private final java.util.List<double[]> emergences = new java.util.ArrayList<>();

    public void recordEmergence(double x, int row, boolean fromWater) {
        if (emergences.size() > 32) {
            return;
        }
        emergences.add(new double[] {x, row, fromWater ? 1 : 0});
    }

    public java.util.List<double[]> drainEmergences() {
        java.util.List<double[]> copy = new java.util.ArrayList<>(emergences);
        emergences.clear();
        return copy;
    }

    public void recordWhirlwind(models.entities.zombie.Zombie zombie,
                                double fromX, double toX, int row) {
        addRide(zombie, fromX, toX, row, true);
    }

    public void recordBlastRide(models.entities.zombie.Zombie zombie,
                                double fromX, double toX, int row) {
        addRide(zombie, fromX, toX, row, false);
    }

    private void addRide(models.entities.zombie.Zombie zombie, double fromX,
                         double toX, int row, boolean storm) {
        if (whirlwinds.size() > 32) {
            return;
        }
        whirlwinds.add(new WhirlwindRide(zombie, fromX, toX, row, storm));
    }

    public java.util.List<WhirlwindRide> drainWhirlwinds() {
        java.util.List<WhirlwindRide> copy = new java.util.ArrayList<>(whirlwinds);
        whirlwinds.clear();
        return copy;
    }

    public void recordDetonation(PlantType type, int x, int y) {
        if (detonations.size() > 32) {
            return;
        }
        detonations.add(new int[] {x, y});
        detonationTypes.add(type);
    }

    public java.util.List<int[]> drainDetonations(java.util.List<PlantType> types) {
        java.util.List<int[]> copy = new java.util.ArrayList<>(detonations);
        types.clear();
        types.addAll(detonationTypes);
        detonations.clear();
        detonationTypes.clear();
        return copy;
    }

    public String getFarewell() {
        return farewell;
    }

    public int getTimerTicksLeft() {
        return timerTicksLeft;
    }

    public void setTimerTicksLeft(int timerTicksLeft) {
        this.timerTicksLeft = timerTicksLeft;
    }

    public int getZombiesKilled() {
        return zombiesKilled;
    }

    public Random getRandom() {
        return random;
    }

    public double getHealthFactor() {
        return difficulty() / 3.0;
    }

    public double getDamageFactor() {
        return difficulty() / 3.0;
    }
}
