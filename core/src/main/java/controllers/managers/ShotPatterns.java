package controllers.managers;

import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.game.GameSession;
import models.game.PlacedPlant;

import java.util.ArrayList;
import java.util.List;

public class ShotPatterns {
    private final GameSession session;
    private final CombatManager combat;

    public ShotPatterns(GameSession session, CombatManager combat) {
        this.session = session;
        this.combat = combat;
    }

    boolean specialShot(PlacedPlant plant, PlantType type) {
        switch (type) {
            case THREEPEATER:
                shootThreeLanes(plant);
                return true;
            case SPLIT_PEA:
                combat.shoot(plant, false);
                shootBackward(plant, 2);
                return true;
            case STARFRUIT:
                starShot(plant);
                return true;
            case ROTOBAGA:
                diagonalShot(plant);
                return true;
            case PUFF_SHROOM:
            case SEA_SHROOM:
                shortRangeShot(plant);
                return true;
            case KERNEL_PULT:
                kernelLob(plant);
                return true;
            case CAULIPOWER:
                hypnotizeShot();
                return true;
            case BOWLING_BULB:
                bouncingBulb(plant);
                return true;
            default:
                return false;
        }
    }

    private void shootThreeLanes(PlacedPlant plant) {
        for (int row = plant.getY() - 1; row <= plant.getY() + 1; row++) {
            if (row < 1 || row > GameSession.ROWS) {
                continue;
            }
            if (combat.firstZombieInRowAfter(row, plant.getX()) != null
                    || combat.graveAhead(row, plant.getX())) {
                combat.launchToward(plant, row, 1);
            }
        }
    }

    private void shootBackward(PlacedPlant plant, int shots) {
        Zombie target = lastZombieInRowBefore(plant.getY(), plant.getX());
        for (int i = 0; i < shots && target != null; i++) {
            combat.launchToward(plant, plant.getY(), -1, i * 0.45);
        }
    }

    private static final int[][] STAR_POINTS = {{1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}};

    private void starShot(PlacedPlant plant) {
        for (int[] point : STAR_POINTS) {
            session.getProjectileManager().launchStar(plant, point[0], point[1]);
        }
    }

    private void diagonalShot(PlacedPlant plant) {
        for (int row : new int[] { plant.getY() - 1, plant.getY(), plant.getY() + 1 }) {
            if (row < 1 || row > GameSession.ROWS) {
                continue;
            }
            if (combat.firstZombieInRowAfter(row, plant.getX()) != null
                    || combat.graveAhead(row, plant.getX())) {
                combat.launchToward(plant, row, 1);
            }
            if (lastZombieInRowBefore(row, plant.getX()) != null) {
                combat.launchToward(plant, row, -1);
            }
        }
    }

    private void shortRangeShot(PlacedPlant plant) {
        Zombie target = combat.firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target != null && target.getPosition().getX() - plant.getX() <= 3) {
            combat.launchToward(plant, plant.getY(), 1);
        }
    }

    private void kernelLob(PlacedPlant plant) {
        Zombie target = combat.firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target == null) {
            return;
        }
        session.getProjectileManager().launchLob(plant, target.getPosition().getX());
        if (session.getRandom().nextInt(100) < 25
                && session.getBehaviorManager().beforeHit(target, plant.getType())) {
            target.setFrozenTicks(3 * GameSession.TICKS_PER_SECOND);
            System.out.printf("Butter pinned the %s in place!%n", target.getType().getName());
        }
    }

    private void hypnotizeShot() {
        List<Zombie> candidates = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.getBattle().isHypnotized() && !zombie.getBattle().isSunProducer()
                    && !(zombie instanceof models.entities.zombie.Zomboss)) {
                candidates.add(zombie);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Zombie target = candidates.get(session.getRandom().nextInt(candidates.size()));
        target.getBattle().setHypnotized(true);
        System.out.printf("Caulipower's magic shot hypnotized the %s!%n",
                target.getType().getName());
    }

    private void bouncingBulb(PlacedPlant plant) {
        int row = plant.getY();
        int step = session.getRandom().nextBoolean() ? 1 : -1;
        for (int bounce = 0; bounce < 3; bounce++) {
            if (row < 1 || row > GameSession.ROWS) {
                step = -step;
                row += 2 * step;
            }
            if (row < 1 || row > GameSession.ROWS) {
                return;
            }
            combat.launchToward(plant, row, 1);
            if (bounce > 0) {
                System.out.printf("The bulb bounced into lane %d.%n", row);
            }
            row += step;
        }
    }

    private Zombie lastZombieInRowBefore(int row, double x) {
        Zombie last = null;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.occupiesRow(row) && zombie.getPosition().getX() < x
                    && (last == null || zombie.getPosition().getX() > last.getPosition().getX())) {
                last = zombie;
            }
        }
        return last;
    }
}
