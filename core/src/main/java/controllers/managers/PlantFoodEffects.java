package controllers.managers;

import models.entities.zombie.Zombie;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.map.TerrainType;
import models.map.Tile;

import java.util.ArrayList;

public class PlantFoodEffects {
    private final GameSession session;
    private final CombatManager combat;

    public PlantFoodEffects(GameSession session, CombatManager combat) {
        this.session = session;
        this.combat = combat;
    }

    public void apply(PlacedPlant plant) {
        if (applySpecific(plant)) {
            return;
        }
        switch (plant.getType().getCategory()) {
            case SUN_PRODUCER:
                session.getSunManager().addSun(150);
                break;
            case WALL_NUT:
                plant.setHealth(plant.getMaxHealth() + plant.getType().getBaseHp());
                break;
            case EXPLOSIVE:
                combat.explode(plant);
                break;
            default:
                powerAttack(plant);
                break;
        }
    }

    private boolean applySpecific(PlacedPlant plant) {
        switch (plant.getType()) {
            case TWIN_SUNFLOWER:
                session.getSunManager().addSun(250);
                return true;
            case PRIMAL_SUNFLOWER:
            case SUN_SHROOM:
                plant.setGrowthStage(3);
                session.getSunManager().addSun(225);
                return true;
            case REPEATER:
            case PEA_POD:
            case MEGA_GATLING_PEA:
                giantPea(plant);
                return true;
            case THREEPEATER:
                fanBarrage(plant);
                return true;
            case KERNEL_PULT:
                butterEveryZombie(plant);
                return true;
            case MELON_PULT:
            case WINTER_MELON:
            case PEPPER_PULT:
                giantMelons(plant);
                return true;
            case SNOW_PEA:
                freezeLane(plant);
                return true;
            default:
                return applyUtility(plant);
        }
    }

    private boolean applyUtility(PlacedPlant plant) {
        switch (plant.getType()) {
            case CHOMPER:
            case SQUASH:
                devour(plant, plant.getType() == models.entities.plant.PlantType.CHOMPER ? 3 : 2);
                return true;
            case ICEBERG_LETTUCE:
                freezeEveryZombie();
                return true;
            case TANGLE_KELP:
                drownRandom(plant, 3);
                return true;
            case CAULIPOWER:
                hypnotizeRandom(3);
                return true;
            case MAGNET_SHROOM:
                magnetEveryZombie();
                return true;
            default:
                return false;
        }
    }

    private void giantPea(PlacedPlant plant) {
        Zombie target = combat.firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target != null) {
            combat.damageZombie(target, combat.plantDamage(plant.getType()) * 20, plant.getType());
        }
        powerAttack(plant);
    }

    private void fanBarrage(PlacedPlant plant) {
        int damage = combat.plantDamage(plant.getType()) * 5;
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            combat.damageZombie(zombie, damage, plant.getType());
        }
    }

    private void butterEveryZombie(PlacedPlant plant) {
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            zombie.setFrozenTicks(3 * GameSession.TICKS_PER_SECOND);
            combat.damageZombie(zombie, combat.plantDamage(plant.getType()) * 2, plant.getType());
        }
        System.out.println("Butter rained on every zombie's head!");
    }

    private void giantMelons(PlacedPlant plant) {
        for (int i = 0; i < 3 && !session.getZombies().isEmpty(); i++) {
            Zombie target = session.getZombies().get(
                    session.getRandom().nextInt(session.getZombies().size()));
            combat.damageArea(target.getPosition().getX(), target.getPosition().getY(), 1,
                    plant.getType());
        }
    }

    private void freezeLane(PlacedPlant plant) {
        for (Zombie zombie : new ArrayList<>(combat.zombiesInRowAfter(plant.getY(), 0))) {
            zombie.setFrozenTicks(4 * GameSession.TICKS_PER_SECOND);
            combat.damageZombie(zombie, combat.plantDamage(plant.getType()) * 5, plant.getType());
        }
    }

    private void devour(PlacedPlant plant, int count) {
        for (int i = 0; i < count && !session.getZombies().isEmpty(); i++) {
            Zombie target = session.getZombies().get(
                    session.getRandom().nextInt(session.getZombies().size()));
            combat.damageZombie(target, CombatManager.INSTANT_KILL_DAMAGE, plant.getType());
        }
    }

    private void freezeEveryZombie() {
        for (Zombie zombie : session.getZombies()) {
            zombie.setFrozenTicks(5 * GameSession.TICKS_PER_SECOND);
        }
        System.out.println("Every zombie on the lawn is frozen!");
    }

    private void drownRandom(PlacedPlant plant, int count) {
        int drowned = 0;
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (drowned >= count) {
                return;
            }
            Tile tile = session.getGrid().getTile(
                    (int) Math.round(zombie.getPosition().getX()) - 1,
                    (int) zombie.getPosition().getY() - 1);
            if (tile != null && tile.getTerrain() == TerrainType.WATER) {
                combat.damageZombie(zombie, CombatManager.INSTANT_KILL_DAMAGE, plant.getType());
                drowned++;
            }
        }
    }

    private void hypnotizeRandom(int count) {
        int done = 0;
        for (Zombie zombie : session.getZombies()) {
            if (done >= count) {
                return;
            }
            if (!zombie.getBattle().isHypnotized() && !zombie.getBattle().isSunProducer()) {
                zombie.getBattle().setHypnotized(true);
                System.out.printf("The %s is hypnotized and fights for you now!%n",
                        zombie.getType().getName());
                done++;
            }
        }
    }

    private void magnetEveryZombie() {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.totalArmor() > 0 && zombie.stripMetallicArmor()) {
                System.out.printf("Magnet-shroom devoured the %s's metal armor!%n",
                        zombie.getType().getName());
            }
        }
    }

    private void powerAttack(PlacedPlant plant) {
        int damage = combat.plantDamage(plant.getType()) * 10;
        for (Zombie zombie : new ArrayList<>(combat.zombiesInRowAfter(plant.getY(), 0))) {
            combat.damageZombie(zombie, damage, plant.getType());
        }
    }
}
