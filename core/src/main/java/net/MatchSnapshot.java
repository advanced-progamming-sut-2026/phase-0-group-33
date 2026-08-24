package net;

import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.game.Projectile;
import models.game.ZombieFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MatchSnapshot {

    private static final PlantType[] PLANTS = PlantType.values();
    private static final ZombieType[] ZOMBIES = ZombieType.values();
    private static final Projectile.Motion[] MOTIONS = Projectile.Motion.values();

    private MatchSnapshot() {
    }

    public static Packet capture(GameSession session, long counter) {
        List<Object> plants = new ArrayList<>();
        for (PlacedPlant plant : session.getPlants()) {
            plants.add(writePlant(plant));
        }
        List<Object> zombies = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getNetId() == 0) {
                zombie.setNetId(++counter);
            }
            zombies.add(writeZombie(zombie));
        }
        List<Object> shots = new ArrayList<>();
        for (Projectile shot : session.getProjectileManager().getProjectiles()) {
            if (shot.getNetId() == 0) {
                shot.setNetId(++counter);
            }
            shots.add(writeShot(shot));
        }
        List<Object> suns = new ArrayList<>();
        for (models.game.Sun sun : session.getSunManager().getSuns()) {
            suns.add(Rows.writer().put(sun.getKind().ordinal()).put(sun.getX()).put(sun.getY())
                    .put(sun.getTicksToLand()).put(sun.getValue())
                    .put(sun.isProducedByPlant()).toString());
        }
        List<Object> brains = new ArrayList<>();
        for (int row = 1; row <= GameSession.ROWS; row++) {
            brains.add(session.hasBrain(row) ? 1 : 0);
        }
        return Packet.of(Protocol.MATCH_STATE).put("counter", counter)
                .put("plants", plants).put("zombies", zombies).put("shots", shots)
                .put("brains", brains).put("suns", suns)
                .put("sun", session.getSunManager().getSunBalance());
    }

    private static String writePlant(PlacedPlant plant) {
        return Rows.writer().put(plant.getX()).put(plant.getY())
                .put(plant.getType().ordinal()).put(plant.getHealth()).put(plant.getMaxHealth())
                .put(plant.getActionCooldownTicks()).put(plant.getGrowthStage())
                .put(plant.getPlantFoodTicks()).put(plant.getStackCount())
                .put(plant.getFreezeLevel()).put(plant.getIceHealth()).put(plant.getArmTicks())
                .put(plant.getOctopusHealth()).put(plant.getPumpkinHealth())
                .put(plant.isSheep()).put(plant.isSunPending()).put(plant.isProtectedSeed())
                .toString();
    }

    private static String writeZombie(Zombie zombie) {
        return Rows.writer().put(zombie.getNetId()).put(zombie.getType().ordinal())
                .put(zombie.getPosition().getX()).put((int) zombie.getPosition().getY())
                .put(zombie.getHealth()).put(zombie.totalArmor())
                .put(zombie.getChilledTicks()).put(zombie.getFrozenTicks())
                .put(zombie.isGlowing()).put(zombie.getBattle().getAbilityCooldown())
                .put(zombie.getBattle().getIceHealth()).put(zombie.getBattle().getPoisonTicksLeft())
                .put(zombie.getBattle().isCharging()).put(zombie.getBattle().isHypnotized())
                .put(zombie.getBattle().isReversed()).put(zombie.getBattle().isSpinning())
                .put(zombie.getBattle().isSunProducer()).toString();
    }

    private static String writeShot(Projectile shot) {
        return Rows.writer().put(shot.getNetId())
                .put(shot.getSource() == null ? -1 : shot.getSource().ordinal())
                .put(shot.getMotion().ordinal()).put(shot.getOriginX()).put(shot.getTargetX())
                .put(shot.getDirection()).put(shot.getFlight()).put(shot.getX())
                .put(shot.getLane()).put(shot.getLaneStep())
                .put(shot.isFromZombie()).put(shot.isLit()).toString();
    }

    public static void apply(GameSession session, Packet packet) {
        applyPlants(session, packet.list("plants"));
        applyZombies(session, packet.list("zombies"));
        applyShots(session, packet.list("shots"));
        applySuns(session, packet.list("suns"));
        applyBrains(session, packet.list("brains"));
        session.getSunManager().setSunBalance(packet.num("sun", 0));
    }

    private static void applySuns(GameSession session, List<String> rows) {
        List<models.game.Sun> suns = new ArrayList<>();
        models.game.Sun.SunKind[] kinds = models.game.Sun.SunKind.values();
        for (String row : rows) {
            Rows values = Rows.reader(row);
            suns.add(models.game.Sun.restored(kinds[values.nextInt()], values.nextInt(),
                    values.nextInt(), values.nextInt(), values.nextInt(), values.nextFlag()));
        }
        session.getSunManager().syncSuns(suns);
    }

    private static void applyBrains(GameSession session, List<String> rows) {
        for (int row = 1; row <= GameSession.ROWS && row <= rows.size(); row++) {
            if (!"1".equals(rows.get(row - 1)) && session.hasBrain(row)) {
                session.eatBrain(row);
            }
        }
    }

    private static void applyPlants(GameSession session, List<String> rows) {
        Map<Long, PlacedPlant> living = new LinkedHashMap<>();
        for (PlacedPlant plant : session.getPlants()) {
            living.put(tile(plant.getX(), plant.getY()), plant);
        }
        Set<Long> seen = new LinkedHashSet<>();
        for (String row : rows) {
            Rows values = Rows.reader(row);
            int x = values.nextInt();
            int y = values.nextInt();
            PlantType type = PLANTS[values.nextInt()];
            int health = values.nextInt();
            int maxHealth = values.nextInt();
            PlacedPlant plant = living.get(tile(x, y));
            if (plant == null || plant.getType() != type) {
                plant = new PlacedPlant(type, x, y, maxHealth);
                session.getPlants().add(plant);
            }
            seen.add(tile(x, y));
            plant.setHealth(health);
            readPlantExtras(plant, values);
        }
        session.getPlants().removeIf(plant -> !seen.contains(tile(plant.getX(), plant.getY())));
    }

    private static void readPlantExtras(PlacedPlant plant, Rows values) {
        plant.setActionCooldownTicks(values.nextInt());
        plant.setGrowthStage(values.nextInt());
        plant.setPlantFoodTicks(values.nextInt());
        plant.setStackCount(values.nextInt());
        plant.setFreezeLevel(values.nextInt());
        plant.setIceHealth(values.nextInt());
        plant.setArmTicks(values.nextInt());
        plant.setOctopusHealth(values.nextInt());
        plant.setPumpkinHealth(values.nextInt());
        plant.setSheep(values.nextFlag());
        plant.setSunPending(values.nextFlag());
        plant.setProtectedSeed(values.nextFlag());
    }

    private static long tile(int x, int y) {
        return (long) y * 100 + x;
    }

    private static void applyZombies(GameSession session, List<String> rows) {
        Map<Long, Zombie> living = new LinkedHashMap<>();
        for (Zombie zombie : session.getZombies()) {
            living.put(zombie.getNetId(), zombie);
        }
        Set<Long> seen = new LinkedHashSet<>();
        for (String row : rows) {
            Rows values = Rows.reader(row);
            long id = values.nextLong();
            ZombieType type = ZOMBIES[values.nextInt()];
            double x = values.nextDouble();
            int lane = values.nextInt();
            Zombie zombie = living.get(id);
            if (zombie == null) {
                zombie = ZombieFactory.create(type, x, lane, session.getHealthFactor());
                zombie.setNetId(id);
                session.getZombies().add(zombie);
            }
            seen.add(id);
            zombie.getPosition().setX(x);
            zombie.getPosition().setY(lane);
            readZombieExtras(zombie, values);
        }
        session.getZombies().removeIf(zombie -> !seen.contains(zombie.getNetId()));
    }

    private static void readZombieExtras(Zombie zombie, Rows values) {
        int health = values.nextInt();
        int armour = values.nextInt();
        int shortfall = zombie.getHealth() + zombie.totalArmor() - health - armour;
        if (shortfall > 0) {
            zombie.takeDamage(shortfall);
        }
        zombie.setHealth(health);
        zombie.setChilledTicks(values.nextInt());
        zombie.setFrozenTicks(values.nextInt());
        zombie.setGlowing(values.nextFlag());
        zombie.getBattle().setAbilityCooldown(values.nextInt());
        zombie.getBattle().setIceHealth(values.nextInt());
        zombie.getBattle().setPoisonTicksLeft(values.nextInt());
        zombie.getBattle().setCharging(values.nextFlag());
        zombie.getBattle().setHypnotized(values.nextFlag());
        zombie.getBattle().setReversed(values.nextFlag());
        zombie.getBattle().setSpinning(values.nextFlag());
        zombie.getBattle().setSunProducer(values.nextFlag());
    }

    private static void applyShots(GameSession session, List<String> rows) {
        List<Projectile> shots = session.getProjectileManager().getProjectiles();
        Map<Long, Projectile> living = new LinkedHashMap<>();
        for (Projectile shot : shots) {
            living.put(shot.getNetId(), shot);
        }
        Set<Long> seen = new LinkedHashSet<>();
        for (String row : rows) {
            Rows values = Rows.reader(row);
            long id = values.nextLong();
            int source = values.nextInt();
            Projectile.Motion motion = MOTIONS[values.nextInt()];
            double originX = values.nextDouble();
            double targetX = values.nextDouble();
            int direction = values.nextInt();
            Projectile shot = living.get(id);
            if (shot == null) {
                shot = new Projectile(source < 0 ? null : PLANTS[source], motion,
                        0, originX, targetX, direction);
                shot.setNetId(id);
                shots.add(shot);
            }
            seen.add(id);
            shot.syncFlight(values.nextDouble(), values.nextDouble(), values.nextDouble());
            shot.setLaneStep(values.nextInt());
            if (values.nextFlag()) {
                shot.markFromZombie();
            }
            shot.syncLit(values.nextFlag());
        }
        shots.removeIf(shot -> !seen.contains(shot.getNetId()));
    }
}
