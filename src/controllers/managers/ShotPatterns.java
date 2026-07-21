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
            default:
                return false;
        }
    }

    private void shootThreeLanes(PlacedPlant plant) {
        for (int row = plant.getY() - 1; row <= plant.getY() + 1; row++) {
            if (row < 1 || row > GameSession.ROWS) {
                continue;
            }
            Zombie target = combat.firstZombieInRowAfter(row, plant.getX());
            if (target != null) {
                combat.hitZombie(target, plant.getType());
            }
        }
    }

    private void shootBackward(PlacedPlant plant, int shots) {
        Zombie target = lastZombieInRowBefore(plant.getY(), plant.getX());
        for (int i = 0; i < shots && target != null && !target.isDead(); i++) {
            combat.hitZombie(target, plant.getType());
        }
    }

    private void starShot(PlacedPlant plant) {
        Zombie forward = combat.firstZombieInRowAfter(plant.getY(), plant.getX());
        if (forward != null) {
            combat.hitZombie(forward, plant.getType());
        }
        Zombie backward = lastZombieInRowBefore(plant.getY(), plant.getX());
        if (backward != null) {
            combat.hitZombie(backward, plant.getType());
        }
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (Math.abs(zombie.getPosition().getX() - plant.getX()) <= 0.5
                    && (int) zombie.getPosition().getY() != plant.getY()) {
                combat.hitZombie(zombie, plant.getType());
            }
        }
    }

    private void diagonalShot(PlacedPlant plant) {
        for (int row : new int[] { plant.getY() - 1, plant.getY() + 1 }) {
            if (row < 1 || row > GameSession.ROWS) {
                continue;
            }
            Zombie ahead = combat.firstZombieInRowAfter(row, plant.getX());
            if (ahead != null) {
                combat.hitZombie(ahead, plant.getType());
            }
            Zombie behind = lastZombieInRowBefore(row, plant.getX());
            if (behind != null) {
                combat.hitZombie(behind, plant.getType());
            }
        }
    }

    private void shortRangeShot(PlacedPlant plant) {
        Zombie target = combat.firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target != null && target.getPosition().getX() - plant.getX() <= 3) {
            combat.hitZombie(target, plant.getType());
        }
    }

    private void kernelLob(PlacedPlant plant) {
        Zombie target = combat.firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target == null) {
            return;
        }
        if (session.getRandom().nextInt(100) < 25) {
            target.setFrozenTicks(3 * GameSession.TICKS_PER_SECOND);
            combat.damageZombie(target, combat.plantDamage(plant.getType()) * 2, plant.getType());
            System.out.printf("Butter pinned the %s in place!%n", target.getType().getName());
        } else {
            combat.hitZombie(target, plant.getType());
        }
    }

    private void hypnotizeShot() {
        List<Zombie> candidates = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (!zombie.getBattle().isHypnotized() && !zombie.getBattle().isSunProducer()) {
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

    private Zombie lastZombieInRowBefore(int row, double x) {
        Zombie last = null;
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() == row && zombie.getPosition().getX() < x
                    && (last == null || zombie.getPosition().getX() > last.getPosition().getX())) {
                last = zombie;
            }
        }
        return last;
    }
}
