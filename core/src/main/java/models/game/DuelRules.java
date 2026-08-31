package models.game;

import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.user.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DuelRules {

    public static final int ROUND_SECONDS = 120;
    public static final int ZOMBIE_START_SUN = 200;
    public static final int ZOMBIE_SUN_EVERY = 20;
    public static final int ZOMBIE_SUN_AMOUNT = 25;
    public static final int FIRST_ZOMBIE_COLUMN = 6;

    public static final int PLANT_SLOTS = 8;
    public static final int ZOMBIE_SLOTS = 5;

    public static final PlantType[] PLANTS = {
        PlantType.SUNFLOWER, PlantType.PEASHOOTER, PlantType.WALL_NUT, PlantType.SNOW_PEA,
        PlantType.REPEATER, PlantType.CABBAGE_PULT, PlantType.POTATO_MINE, PlantType.CHOMPER,
    };

    public static final PlantType[] PLANT_POOL = {
        PlantType.SUNFLOWER, PlantType.TWIN_SUNFLOWER, PlantType.PEASHOOTER,
        PlantType.REPEATER, PlantType.SNOW_PEA, PlantType.THREEPEATER,
        PlantType.WALL_NUT, PlantType.TALL_NUT, PlantType.CABBAGE_PULT,
        PlantType.MELON_PULT, PlantType.POTATO_MINE, PlantType.CHOMPER,
        PlantType.BONK_CHOY, PlantType.CHERRY_BOMB, PlantType.TORCHWOOD,
        PlantType.SPLIT_PEA,
    };

    public static final models.entities.zombie.ZombieType[] ZOMBIE_POOL = {
        models.entities.zombie.ZombieType.NORMAL, models.entities.zombie.ZombieType.IMP,
        models.entities.zombie.ZombieType.CONE_HEAD,
        models.entities.zombie.ZombieType.PROSPECTOR,
        models.entities.zombie.ZombieType.BUCKET_HEAD,
        models.entities.zombie.ZombieType.NEWSPAPER,
        models.entities.zombie.ZombieType.BRICK_HEAD,
        models.entities.zombie.ZombieType.DODO,
        models.entities.zombie.ZombieType.KNIGHT,
    };

    private final GameSession session;
    private final Map<ZombieType, Integer> cooldowns = new HashMap<>();

    private int sun = ZOMBIE_START_SUN;
    private int ticksLeft = ROUND_SECONDS * GameSession.TICKS_PER_SECOND;

    public DuelRules(GameSession session) {
        this.session = session;
    }

    public static GameSession newSession(User user, long seed) {
        GameSession session = openSession(user, seed);
        for (PlantType type : PLANTS) {
            session.addPlantToSelection(type.getName());
        }
        session.startGame();
        return session;
    }

    public static GameSession openSession(User user, long seed) {
        List<String> plants = new ArrayList<>();
        for (PlantType type : PlantType.values()) {
            plants.add(type.getName());
        }
        return new GameSession(GameSetup.duel(user, plants, seed));
    }

    public List<ZombieType> roster() {
        return session.getMinigameManager().getIzombieTypes();
    }

    public int getSun() {
        return sun;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    public int secondsLeft() {
        return Math.max(0, ticksLeft / GameSession.TICKS_PER_SECOND);
    }

    public int cooldownSeconds(ZombieType type) {
        return cooldowns.getOrDefault(type, 0) / GameSession.TICKS_PER_SECOND;
    }

    public boolean isReady(ZombieType type) {
        return cooldowns.getOrDefault(type, 0) <= 0 && sun >= type.getWaveCost();
    }

    public void tick() {
        session.advanceTime(1);
        for (Map.Entry<ZombieType, Integer> entry : cooldowns.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }
        if (ticksLeft % ZOMBIE_SUN_EVERY == 0) {
            sun += ZOMBIE_SUN_AMOUNT;
        }
        ticksLeft--;
    }

    public boolean brainsGone() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            if (session.hasBrain(row)) {
                return false;
            }
        }
        return true;
    }

    public boolean timeUp() {
        return ticksLeft <= 0;
    }

    public Result placeZombie(String typeName, int x, int y) {
        ZombieType type = Names.zombie(typeName);
        if (type == null || !roster().contains(type)) {
            return Result.fail("That zombie is not in your roster.");
        }
        if (x < FIRST_ZOMBIE_COLUMN || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            return Result.fail("Zombies drop right of the red line (columns "
                    + FIRST_ZOMBIE_COLUMN + "-" + GameSession.COLS + ").");
        }
        if (session.plantAt(x, y) != null) {
            return Result.fail("A plant already stands on that tile.");
        }
        if (cooldowns.getOrDefault(type, 0) > 0) {
            return Result.fail(type.getName() + " is still recharging.");
        }
        if (sun < type.getWaveCost()) {
            return Result.fail(type.getName() + " costs " + type.getWaveCost() + " sun.");
        }
        sun -= type.getWaveCost();
        cooldowns.put(type, session.getMinigameManager().zombieRechargeTicks(type));
        Zombie dropped = session.spawnZombie(type, x, y, 1);
        dropped.getBattle().setSpawnTick(0);
        return Result.ok(type.getName() + " drops into lane " + y + ".");
    }
}
