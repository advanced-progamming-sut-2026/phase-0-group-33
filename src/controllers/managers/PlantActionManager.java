package controllers.managers;

import models.entities.plant.PlantTag;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.game.GameSession;
import models.game.PlacedPlant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlantActionManager {
    private static final int SHROOM_LIFESPAN_TICKS = 60 * GameSession.TICKS_PER_SECOND;
    private static final int MELT_PER_TICK = 6;

    private final GameSession session;
    private final CombatManager combatManager;
    private final Map<PlacedPlant, Integer> ages = new HashMap<>();

    public PlantActionManager(GameSession session, CombatManager combatManager) {
        this.session = session;
        this.combatManager = combatManager;
    }

    public void tick() {
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.getArmTicks() > 0) {
                plant.setArmTicks(plant.getArmTicks() - 1);
            }
            tickLifespan(plant);
            if (plant.getType().getTags().contains(PlantTag.FIRE) && !plant.isDisabled()) {
                radiateWarmth(plant);
            }
        }
        ages.keySet().removeIf(plant -> !session.getPlants().contains(plant));
    }

    private void tickLifespan(PlacedPlant plant) {
        if (plant.getType() != PlantType.SEA_SHROOM && plant.getType() != PlantType.PUFF_SHROOM) {
            return;
        }
        int age = ages.merge(plant, 1, Integer::sum);
        if (age >= SHROOM_LIFESPAN_TICKS) {
            session.removePlant(plant, false);
            System.out.printf("%s at (%d, %d) withered away.%n",
                    plant.getType().getName(), plant.getX(), plant.getY());
        }
    }

    private void radiateWarmth(PlacedPlant firePlant) {
        for (PlacedPlant other : session.getPlants()) {
            if (other != firePlant && other.getIceHealth() > 0 && isNeighbor(firePlant, other)) {
                other.setIceHealth(Math.max(0, other.getIceHealth() - MELT_PER_TICK));
                if (other.getIceHealth() == 0) {
                    other.setFreezeLevel(0);
                }
            }
        }
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getBattle().getIceHealth() > 0
                    && Math.abs(zombie.getPosition().getX() - firePlant.getX()) <= 1.5
                    && Math.abs(zombie.getPosition().getY() - firePlant.getY()) <= 1) {
                zombie.getBattle().setIceHealth(
                        Math.max(0, zombie.getBattle().getIceHealth() - MELT_PER_TICK));
            }
        }
    }

    private boolean isNeighbor(PlacedPlant a, PlacedPlant b) {
        return Math.abs(a.getX() - b.getX()) <= 1 && Math.abs(a.getY() - b.getY()) <= 1;
    }

    public void magnet(PlacedPlant magnetShroom) {
        Zombie target = null;
        double best = Double.MAX_VALUE;
        for (Zombie zombie : session.getZombies()) {
            double distance = Math.abs(zombie.getPosition().getX() - magnetShroom.getX())
                    + Math.abs(zombie.getPosition().getY() - magnetShroom.getY());
            if (distance <= 4 && distance < best && zombie.totalArmor() > 0) {
                best = distance;
                target = zombie;
            }
        }
        if (target != null && target.stripMetallicArmor()) {
            System.out.printf("Magnet-shroom pulled the metal armor off the %s!%n",
                    target.getType().getName());
        }
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }
}
