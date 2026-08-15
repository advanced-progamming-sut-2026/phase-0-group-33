package controllers.managers;

import models.entities.zombie.Zomboss;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.map.TerrainType;
import models.map.Tile;

import java.util.ArrayList;
import java.util.List;

public class ZombossManager {

    private static final int ABILITY_INTERVAL = 45;
    private static final int FIRE_TICKS = 4 * GameSession.TICKS_PER_SECOND;
    private static final int SUMMON_CHANCE = 45;
    private static final int MOVE_CHANCE = 30;

    private static final List<ZombieType> SUMMONS = List.of(
            ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD,
            ZombieType.IMP, ZombieType.NEWSPAPER, ZombieType.KNIGHT);

    private final GameSession session;
    private final CombatManager combat;

    private Zomboss boss;

    public ZombossManager(GameSession session, CombatManager combat) {
        this.session = session;
        this.combat = combat;
    }

    public Zomboss getBoss() {
        return boss;
    }

    public boolean hasBoss() {
        return boss != null;
    }

    public void spawn(Zomboss.BossKind kind) {
        if (boss != null) {
            return;
        }
        int row = 2;
        boss = new Zomboss(kind, row, session.getHealthFactor());
        boss.setAbilityTicks(ABILITY_INTERVAL);
        session.getZombies().add(boss);
        session.getEncounteredZombies().add(ZombieType.GARGANTUAR);
        System.out.println("The " + kind.getTitle() + " has arrived!");
    }

    public void tick() {
        if (boss == null) {
            return;
        }
        if (!session.getZombies().contains(boss)) {
            boss = null;
            return;
        }
        tickFires();
        boss.tickTimers();
        if (boss.consumeSegmentIfCleared()) {
            boss.stun();
            System.out.println("The " + boss.getKind().getTitle()
                    + " reels back, dazed! Segments cleared: " + boss.getSegmentsCleared());
        }
        if (boss.isStunned()) {
            return;
        }
        if (boss.getAbilityTicks() > 0) {
            return;
        }
        boss.setAbilityTicks(ABILITY_INTERVAL);
        useAbility();
        maybeSummon();
        maybeMove();
    }

    private void tickFires() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int column = 1; column <= GameSession.COLS; column++) {
                Tile tile = session.getGrid().getTile(column - 1, row - 1);
                if (tile != null) {
                    tile.tickFire();
                }
            }
        }
    }

    private void useAbility() {
        switch (boss.getKind()) {
            case DRAGON:
                if (session.getRandom().nextBoolean()) {
                    fireballs();
                } else {
                    burnRows();
                }
                break;
            case ROBOT:
                if (session.getRandom().nextBoolean()) {
                    missile();
                } else {
                    chargeForward();
                }
                break;
            case MAMMOTH:
                int pick = session.getRandom().nextInt(3);
                if (pick == 0) {
                    iceMissile();
                } else if (pick == 1) {
                    icyWind();
                } else {
                    freezeColumn();
                }
                break;
            default:
                if (session.getRandom().nextBoolean()) {
                    babySharks();
                } else {
                    turbine();
                }
                break;
        }
    }

    private void fireballs() {
        boss.startMove(Zomboss.Move.BOMB);
        int count = 2 + session.getRandom().nextInt(2);
        for (int i = 0; i < count; i++) {
            int column = 1 + session.getRandom().nextInt(GameSession.COLS);
            int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
            Tile tile = session.getGrid().getTile(column - 1, row - 1);
            if (tile == null) {
                continue;
            }
            destroyPlantAt(column, row);
            tile.ignite(FIRE_TICKS);
            session.spawnZombie(ZombieType.IMP_DRAGON, column, row, currentWave());
            System.out.printf("A fireball scorched (%d, %d) and left a Dragon Imp behind!%n",
                    column, row);
        }
    }

    private void burnRows() {
        boss.startMove(Zomboss.Move.BURN);
        int top = (int) boss.getPosition().getY();
        for (int row = top; row <= Math.min(GameSession.ROWS, top + 1); row++) {
            for (int column = 1; column <= GameSession.COLS; column++) {
                destroyPlantAt(column, row);
                Tile tile = session.getGrid().getTile(column - 1, row - 1);
                if (tile != null) {
                    tile.ignite(FIRE_TICKS);
                }
            }
            System.out.printf("The dragon set row %d ablaze!%n", row);
        }
    }

    private void missile() {
        boss.startMove(Zomboss.Move.MISSILE);
        int column = 1 + session.getRandom().nextInt(GameSession.COLS);
        int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
        destroyPlantAt(column, row);
        System.out.printf("A rocket slammed into (%d, %d)!%n", column, row);
        for (int i = 0; i < 2; i++) {
            int graveColumn = 2 + session.getRandom().nextInt(GameSession.COLS - 2);
            int graveRow = 1 + session.getRandom().nextInt(GameSession.ROWS);
            Tile tile = session.getGrid().getTile(graveColumn - 1, graveRow - 1);
            if (tile != null && tile.getTerrain() == TerrainType.NORMAL
                    && session.plantAt(graveColumn, graveRow) == null) {
                tile.setTerrain(TerrainType.GRAVE);
                System.out.printf("The blast threw up a grave at (%d, %d).%n",
                        graveColumn, graveRow);
            }
        }
    }

    private void chargeForward() {
        boss.startMove(Zomboss.Move.CHARGE);
        int top = (int) boss.getPosition().getY();
        for (int row = top; row <= Math.min(GameSession.ROWS, top + 1); row++) {
            for (int column = 1; column <= GameSession.COLS; column++) {
                destroyPlantAt(column, row);
            }
        }
        System.out.println("The robot charged forward and flattened everything in its path!");
    }

    private void iceMissile() {
        boss.startMove(Zomboss.Move.SLINGSHOT);
        int column = 1 + session.getRandom().nextInt(GameSession.COLS);
        int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
        destroyPlantAt(column, row);
        System.out.printf("An ice rocket shattered the plant at (%d, %d)!%n", column, row);
    }

    private void icyWind() {
        boss.startMove(Zomboss.Move.WIND);
        for (int i = 0; i < 2; i++) {
            int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
            for (PlacedPlant plant : session.getPlants()) {
                if (plant.getY() != row) {
                    continue;
                }
                plant.setFreezeLevel(plant.getFreezeLevel() + 1);
                if (plant.getFreezeLevel() >= 3 && plant.getIceHealth() == 0) {
                    plant.setIceHealth(600);
                }
            }
            System.out.printf("The mammoth blew an icy wind down row %d!%n", row);
        }
    }

    private void freezeColumn() {
        boss.startMove(Zomboss.Move.GLACIER);
        int column = 3 + session.getRandom().nextInt(GameSession.COLS - 3);
        for (int row = 1; row <= GameSession.ROWS; row++) {
            Zombie frozen = session.spawnZombie(ZombieType.TROGLOBITE, column, row, currentWave());
            frozen.setFrozenTicks(5 * GameSession.TICKS_PER_SECOND);
        }
        System.out.printf("The mammoth froze column %d solid!%n", column);
    }

    private void babySharks() {
        boss.startMove(Zomboss.Move.SPAWN_SHARK);
        int count = 1 + session.getRandom().nextInt(2);
        for (int i = 0; i < count; i++) {
            PlacedPlant target = plantOnWater();
            if (target == null) {
                return;
            }
            System.out.printf("A baby shark swallowed the %s at (%d, %d)!%n",
                    target.getType().getName(), target.getX(), target.getY());
            session.removePlant(target, true);
        }
    }

    private PlacedPlant plantOnWater() {
        for (PlacedPlant plant : session.getPlants()) {
            Tile tile = session.getGrid().getTile(plant.getX() - 1, plant.getY() - 1);
            if (tile != null && tile.getTerrain() == TerrainType.WATER) {
                return plant;
            }
        }
        return session.getPlants().isEmpty() ? null : session.getPlants().get(0);
    }

    private void turbine() {
        boss.startMove(Zomboss.Move.TURBINE);
        int top = (int) boss.getPosition().getY();
        for (int row = top; row <= Math.min(GameSession.ROWS, top + 1); row++) {
            for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
                if (plant.getY() == row) {
                    session.removePlant(plant, true);
                }
            }
            for (Zombie zombie : new ArrayList<>(session.getZombies())) {
                if (zombie != boss && zombie.occupiesRow(row)) {
                    combat.damageZombie(zombie, CombatManager.INSTANT_KILL_DAMAGE);
                }
            }
        }
        System.out.println("The shark's turbine dragged everything in front of it into its mouth!");
    }

    private void maybeSummon() {
        if (boss.getKind() == Zomboss.BossKind.MAMMOTH) {
            return;
        }
        if (session.getRandom().nextInt(100) >= SUMMON_CHANCE) {
            return;
        }
        int count = 1 + session.getRandom().nextInt(3);
        boss.startMove(Zomboss.Move.SUMMON);
        for (int i = 0; i < count; i++) {
            ZombieType type = SUMMONS.get(session.getRandom().nextInt(SUMMONS.size()));
            int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
            session.spawnZombie(type, GameSession.COLS, row, currentWave());
        }
        System.out.printf("The %s called in %d more zombies!%n", boss.getKind().getTitle(), count);
    }

    private void maybeMove() {
        if (boss.getKind() == Zomboss.BossKind.MAMMOTH) {
            return;
        }
        if (session.getRandom().nextInt(100) >= MOVE_CHANCE) {
            return;
        }
        int row = 1 + session.getRandom().nextInt(GameSession.ROWS - 1);
        boss.moveToRow(row);
        System.out.printf("The %s moved to rows %d and %d.%n",
                boss.getKind().getTitle(), row, row + 1);
    }

    private void destroyPlantAt(int column, int row) {
        PlacedPlant plant = session.plantAt(column, row);
        if (plant != null) {
            session.removePlant(plant, true);
        }
    }

    private int currentWave() {
        return Math.max(1, session.getWaveManager().getCurrentWave());
    }
}
