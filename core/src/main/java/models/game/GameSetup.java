package models.game;

import models.entities.zombie.ZombieType;
import models.progress.level.Level;
import models.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSetup {
    private final User user;
    private final Level level;
    private final GameMode mode;
    private final List<String> unlockedPlants;
    private final int plantFoods;
    private final long seed;
    private final int difficultyTier;
    private final List<ZombieType> zombiePoolOverride;
    private final Map<String, Integer> plantLevels;

    private GameSetup(User user, Level level, GameMode mode, List<String> unlockedPlants,
                      int plantFoods, long seed, int difficultyTier,
                      List<ZombieType> zombiePoolOverride, Map<String, Integer> plantLevels) {
        this.user = user;
        this.level = level;
        this.mode = mode;
        this.unlockedPlants = new ArrayList<>(unlockedPlants);
        this.plantFoods = plantFoods;
        this.seed = seed;
        this.difficultyTier = difficultyTier;
        this.zombiePoolOverride = zombiePoolOverride;
        this.plantLevels = plantLevels == null ? new HashMap<>() : new HashMap<>(plantLevels);
    }

    public static GameSetup adventure(User user, Level level, List<String> unlockedPlants,
                                      int plantFoods, Map<String, Integer> plantLevels) {
        return new GameSetup(user, level, GameMode.ADVENTURE, unlockedPlants, plantFoods,
                -1, level.getLevelNumber(), null, plantLevels);
    }

    public static GameSetup scoring(User user, List<String> unlockedPlants,
                                    Map<String, Integer> plantLevels) {
        return new GameSetup(user, null, GameMode.SCORING, unlockedPlants, 0,
                LocalDate.now().toEpochDay(), 3, basicPool(), plantLevels);
    }

    public static GameSetup minigame(User user, GameMode mode, List<String> unlockedPlants,
                                     int difficultyTier, Map<String, Integer> plantLevels) {
        return new GameSetup(user, null, mode, unlockedPlants, 0, -1, difficultyTier,
                mode == GameMode.ZOMBOTANY ? zombotanyPool() : basicPool(), plantLevels);
    }

    private static List<ZombieType> basicPool() {
        return List.of(ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD,
                ZombieType.BRICK_HEAD, ZombieType.IMP, ZombieType.NEWSPAPER,
                ZombieType.ALLSTAR, ZombieType.GARGANTUAR);
    }

    private static List<ZombieType> zombotanyPool() {
        return List.of(ZombieType.NORMAL, ZombieType.PEASHOOTER_ZOMBIE,
                ZombieType.WALLNUT_ZOMBIE, ZombieType.JALAPENO_ZOMBIE,
                ZombieType.SQUASH_ZOMBIE);
    }

    public User getUser() {
        return user;
    }

    public Level getLevel() {
        return level;
    }

    public GameMode getMode() {
        return mode;
    }

    public List<String> getUnlockedPlants() {
        return unlockedPlants;
    }

    public int getPlantFoods() {
        return plantFoods;
    }

    public long getSeed() {
        return seed;
    }

    public int getDifficultyTier() {
        return difficultyTier;
    }

    public List<ZombieType> getZombiePoolOverride() {
        return zombiePoolOverride;
    }

    public Map<String, Integer> getPlantLevels() {
        return plantLevels;
    }
}
