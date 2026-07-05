package models.game;

import controllers.managers.BattleCommands;
import controllers.managers.CombatManager;
import controllers.managers.MinigameManager;
import controllers.managers.PlantActionManager;
import controllers.managers.PlantingManager;
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
    private final ScoreTracker scoreTracker = new ScoreTracker();
    private final QuestStats questStats = new QuestStats();
    private GamePhase phase = GamePhase.PREPARATION;
    private int tickCount;
    private int plantFoods;
    private boolean cooldownsDisabled;
    private int plantsLost;
    private int zombiesKilled;
    private int timerTicksLeft = -1;

    public GameSession(GameSetup setup) {
        this.setup = setup;
        this.random = setup.getSeed() >= 0 ? new Random(setup.getSeed()) : new Random();
        this.selection = new PlantSelection(new ArrayList<>(setup.getUnlockedPlants()),
                setup.getLevel() == null ? null : setup.getLevel().getSpecialType());
        this.plantFoods = Math.min(3, setup.getPlantFoods());
        this.sunManager = new SunManager(startingSun(), skyEnabled(), difficulty() / 3.0, random);
        this.waveManager = new WaveManager(this, zombiePool(), waveCount(), waveBudget(),
                3.0 / difficulty(), random);
        this.combatManager = new CombatManager(this);
        this.plantActionManager = new PlantActionManager(this, combatManager);
        this.behaviorManager = new ZombieBehaviorManager(this, combatManager);
        this.plantingManager = new PlantingManager(this);
        this.battleCommands = new BattleCommands(this);
        this.minigameManager = new MinigameManager(this, setup.getDifficultyTier());
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
        behaviorManager.spawnFrozenZombiesIfFrostbite();
        return Result.ok("The battle begins! Use 'start zombie waves' to summon the horde.");
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
            System.out.println(
                    "Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
        } else {
            System.out.println("You won the minigame!");
        }
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

    public int effectiveCost(PlantType type) {
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

    private int plantLevel(PlantType type) {
        return Math.max(1, setup.getPlantLevels().getOrDefault(type.getName(), 1));
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
        this.plantFoods = Math.min(3, plantFoods);
    }

    public boolean isCooldownsDisabled() {
        return cooldownsDisabled;
    }

    public void disableCooldowns() {
        this.cooldownsDisabled = true;
        for (PlantSlot slot : selection.getSlots()) {
            slot.setCooldownTicks(0);
        }
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
