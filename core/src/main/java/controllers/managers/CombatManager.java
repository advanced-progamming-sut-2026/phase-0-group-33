package controllers.managers;

import models.entities.plant.PlantTag;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.map.TerrainType;
import models.map.Tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombatManager {
    private static final int SUN_BEAN_SUN_PER_BITE = 5;
    private static final int SWEET_POTATO_RANGE = 3;
    static final int INSTANT_KILL_DAMAGE = 9999;
    private static final int POISON_TICKS = 5 * GameSession.TICKS_PER_SECOND;
    private static final int POISON_DAMAGE_PER_SECOND = 5;
    private static final double MOWER_LINE = 0.5;

    private final GameSession session;
    private final Set<Zombie> impThrown = new HashSet<>();
    private final PlantFoodEffects plantFoodEffects;
    private final ShotPatterns shotPatterns;
    private int kingTicks;

    public CombatManager(GameSession session) {
        this.session = session;
        this.plantFoodEffects = new PlantFoodEffects(session, this);
        this.shotPatterns = new ShotPatterns(session, this);
    }

    public void plantsAct() {
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.isDead()) {
                continue;
            }
            plant.tickPlantFood();
            if (tickFuse(plant)) {
                continue;
            }
            if (plant.isDisabled() || plant.getArmTicks() > 0) {
                continue;
            }
            if (plant.getActionCooldownTicks() > 0) {
                plant.setActionCooldownTicks(plant.getActionCooldownTicks() - 1);
                continue;
            }
            actPlant(plant);
        }
    }

    private boolean tickFuse(PlacedPlant plant) {
        if (plant.getFuseTicks() < 0) {
            return false;
        }
        plant.setFuseTicks(plant.getFuseTicks() - 1);
        if (plant.getFuseTicks() == 0) {
            explode(plant);
        }
        return true;
    }

    private void actPlant(PlacedPlant plant) {
        PlantType type = plant.getType();
        double interval = Math.max(0.25, type.getActionInterval());
        if (type == PlantType.MAGNET_SHROOM) {
            session.getPlantActionManager().magnet(plant);
            plant.setActionCooldownTicks((int) Math.round(interval * GameSession.TICKS_PER_SECOND));
            return;
        }
        if (type == PlantType.SWEET_POTATO) {
            sweetPotatoPull(plant);
            return;
        }
        if (shotPatterns.specialShot(plant, type)) {
            plant.setActionCooldownTicks((int) Math.round(interval * GameSession.TICKS_PER_SECOND));
            return;
        }
        boolean acted;
        switch (type.getCategory()) {
            case SUN_PRODUCER:
                acted = produceSun(plant);
                break;
            case SHOOTER:
                acted = shoot(plant, false);
                break;
            case STRIKE_THROUGH:
                acted = strikeThrough(plant);
                break;
            case LOBBER:
                acted = lob(plant);
                break;
            case HOMING:
                acted = homingShot(plant);
                break;
            case MELEE:
                acted = meleeHit(plant);
                break;
            case EXPLOSIVE:
                trapCheck(plant);
                return;
            default:
                return;
        }
        if (acted) {
            plant.setActionCooldownTicks(
                    (int) Math.round(interval * GameSession.TICKS_PER_SECOND));
        }
    }

    private boolean produceSun(PlacedPlant plant) {
        if (plant.isSunPending()) {
            return false;
        }
        int value = productionValue(plant);
        session.getSunManager().addProducedSun(plant.getX(), plant.getY(), value);
        plant.setSunPending(true);
        System.out.printf("plant %s produced a sun at (%d, %d)%n",
                plant.getType().getName(), plant.getX(), plant.getY());
        return true;
    }

    private int productionValue(PlacedPlant plant) {
        switch (plant.getType()) {
            case TWIN_SUNFLOWER:
                return 100;
            case PRIMAL_SUNFLOWER:
            case GOLD_BLOOM:
                return 75;
            case SUN_SHROOM:
                return 25 * plant.getGrowthStage();
            default:
                return 50;
        }
    }

    boolean shoot(PlacedPlant plant, boolean pierceGraves) {
        if (firstZombieInRowAfter(plant.getY(), plant.getX()) == null
                && !graveAhead(plant.getY(), plant.getX())) {
            return false;
        }
        int shots = plant.getType() == PlantType.PEA_POD ? plant.getStackCount() : 1;
        for (int i = 0; i < shots; i++) {
            session.getProjectileManager().launchStraight(plant, plant.getY(), 1);
        }
        return true;
    }

    void launchToward(PlacedPlant plant, int row, int direction) {
        session.getProjectileManager().launchStraight(plant, row, direction);
    }

    void launchToward(PlacedPlant plant, int row, int direction, double offset) {
        session.getProjectileManager().launchStraight(plant, row, direction, offset);
    }


    private boolean strikeThrough(PlacedPlant plant) {
        if (firstZombieInRowAfter(plant.getY(), plant.getX()) == null) {
            return false;
        }
        session.getProjectileManager().launchPiercing(plant);
        return true;
    }

    private boolean lob(PlacedPlant plant) {
        Zombie target = firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target == null) {
            return false;
        }
        session.getProjectileManager().launchLob(plant, target.getPosition().getX());
        return true;
    }

    private boolean homingShot(PlacedPlant plant) {
        if (plant.getType() == PlantType.ELECTRIC_BLUEBERRY) {
            return randomLightning(plant);
        }
        Zombie target = null;
        double best = Double.MAX_VALUE;
        for (Zombie zombie : session.getZombies()) {
            double distance = Math.abs(zombie.getPosition().getX() - plant.getX())
                    + Math.abs(zombie.getPosition().getY() - plant.getY());
            if (distance < best) {
                best = distance;
                target = zombie;
            }
        }
        if (target == null) {
            return false;
        }
        int row = (int) target.getPosition().getY();
        int direction = target.getPosition().getX() >= plant.getX() ? 1 : -1;
        session.getProjectileManager().launchStraight(plant, row, direction);
        return true;
    }

    private boolean meleeHit(PlacedPlant plant) {
        boolean hit = false;
        for (Zombie zombie : zombiesInRowAfter(plant.getY(), plant.getX() - 0.5)) {
            if (zombie.getPosition().getX() <= plant.getX() + 1.5) {
                hitZombie(zombie, plant);
                hit = true;
                if (!plant.getType().getTags().contains(PlantTag.AOE)) {
                    return true;
                }
            }
        }
        return hit;
    }

    private void trapCheck(PlacedPlant plant) {
        if (!plant.getType().getTags().contains(PlantTag.TRAP)) {
            return;
        }
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (zombie.occupiesRow(plant.getY())
                    && Math.abs(zombie.getPosition().getX() - plant.getX()) <= 0.5) {
                explode(plant);
                return;
            }
        }
    }

    public void explode(PlacedPlant plant) {
        PlantType type = plant.getType();
        session.recordDetonation(type, plant.getX(), plant.getY());
        if (type == PlantType.JALAPENO) {
            for (Zombie zombie : new ArrayList<>(zombiesInRowAfter(plant.getY(), 0))) {
                hitZombie(zombie, type);
            }
        } else if (type == PlantType.ICEBERG_LETTUCE) {
            for (Zombie zombie : new ArrayList<>(session.getZombies())) {
                if (zombie.occupiesRow(plant.getY())
                        && Math.abs(zombie.getPosition().getX() - plant.getX()) <= 0.5) {
                    zombie.setFrozenTicks(5 * GameSession.TICKS_PER_SECOND);
                    System.out.printf("The Iceberg Lettuce froze the %s solid!%n",
                            zombie.getType().getName());
                    break;
                }
            }
        } else if (type == PlantType.POTATO_MINE || type == PlantType.SQUASH
                || type == PlantType.TANGLE_KELP) {
            damageArea(plant.getX(), plant.getY(), 0, type);
        } else {
            damageArea(plant.getX(), plant.getY(), 1, type);
            if (type == PlantType.GRAPESHOT) {
                scatterGrapes(plant);
            }
        }
        session.removePlant(plant, false);
    }

    private void scatterGrapes(PlacedPlant plant) {
        session.getProjectileManager().launchGrapes(plant.getX(), plant.getY());
        System.out.println("The Grapeshot flung bouncing grapes across the lawn!");
    }

    public void damageArea(double centerX, double centerY, int radius, PlantType source) {
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (Math.abs(zombie.getPosition().getX() - centerX) <= radius + 0.5
                    && Math.abs(zombie.getPosition().getY() - centerY) <= radius) {
                hitZombie(zombie, source);
            }
        }
    }

    public void hitZombie(Zombie zombie, PlantType source) {
        hitZombie(zombie, source, plantDamage(source));
    }

    public void hitZombie(Zombie zombie, PlacedPlant plant) {
        hitZombie(zombie, plant.getType(), plantDamage(plant));
    }

    private void hitZombie(Zombie zombie, PlantType source, int damage) {
        if (!session.getBehaviorManager().beforeHit(zombie, source)) {
            return;
        }
        if (source.getTags().contains(PlantTag.ICE) && !isFrostbiteLevel()) {
            zombie.setChilledTicks(3 * GameSession.TICKS_PER_SECOND);
        }
        if (source.getTags().contains(PlantTag.FIRE)) {
            zombie.setChilledTicks(0);
        }
        if (source.getTags().contains(PlantTag.POISON)) {
            zombie.getBattle().setPoisonTicksLeft(POISON_TICKS);
            zombie.damageHealthDirectly(damage);
            if (zombie.isDead() && session.getZombies().remove(zombie)) {
                announceDeath(zombie);
                session.countKill(zombie);
                recordQuestKill(zombie, source);
                session.getBehaviorManager().onZombieDeath(zombie);
                handleDrops(zombie);
            }
            return;
        }
        damageZombie(zombie, damage, source);
    }

    private boolean randomLightning(PlacedPlant plant) {
        List<Zombie> candidates = new ArrayList<>(session.getZombies());
        if (candidates.isEmpty()) {
            return false;
        }
        Zombie target = candidates.get(session.getRandom().nextInt(candidates.size()));
        System.out.printf("Electric Blueberry struck the %s with lightning!%n",
                target.getType().getName());
        damageZombie(target, INSTANT_KILL_DAMAGE, plant.getType());
        return true;
    }

    private void tickPoison(Zombie zombie) {
        int left = zombie.getBattle().getPoisonTicksLeft();
        if (left <= 0) {
            return;
        }
        zombie.getBattle().setPoisonTicksLeft(left - 1);
        if (left % GameSession.TICKS_PER_SECOND != 0) {
            return;
        }
        zombie.damageHealthDirectly(POISON_DAMAGE_PER_SECOND);
        if (zombie.isDead() && session.getZombies().remove(zombie)) {
            announceDeath(zombie);
            session.countKill(zombie);
            session.getBehaviorManager().onZombieDeath(zombie);
            handleDrops(zombie);
        }
    }

    int plantDamage(PlacedPlant plant) {
        int damage = plantDamage(plant.getType());
        if (damage < INSTANT_KILL_DAMAGE
                && plant.getType().getTags().contains(PlantTag.WRAMP_UP)) {
            damage *= plant.getGrowthStage();
        }
        return damage;
    }

    int plantDamage(PlantType type) {
        int damage = session.effectiveDamage(type);
        return damage >= 9999 ? INSTANT_KILL_DAMAGE : damage;
    }

    private boolean isFrostbiteLevel() {
        return session.getLevel() != null
                && session.getLevel().getChapter() instanceof models.progress.chapter.FrostBite;
    }

    public void applyRadioactiveExplosion(int x, int y) {
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (Math.abs(zombie.getPosition().getX() - x) <= 2.5
                    && Math.abs(zombie.getPosition().getY() - y) <= 2) {
                damageZombie(zombie, 150);
            }
        }
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (Math.abs(plant.getX() - x) <= 1 && Math.abs(plant.getY() - y) <= 1
                    && !damagePlant(plant, 80) && plant.isDead()) {
                session.removePlant(plant, true);
            }
        }
    }

    public void zombiesAct() {
        kingTicks++;
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (!session.getZombies().contains(zombie) || session.isOver()) {
                continue;
            }
            if (zombie.getFrozenTicks() > 0) {
                zombie.setFrozenTicks(zombie.getFrozenTicks() - 1);
                continue;
            }
            if (zombie.getChilledTicks() > 0) {
                zombie.setChilledTicks(zombie.getChilledTicks() - 1);
            }
            actZombie(zombie);
        }
    }

    private void actZombie(Zombie zombie) {
        tickPoison(zombie);
        if (session.getBehaviorManager().handleSpecial(zombie)) {
            return;
        }
        if (zombie.getType() == ZombieType.KING) {
            kingConvert(zombie);
            return;
        }
        gargantuarImpThrow(zombie);
        PlacedPlant blocking = blockingPlant(zombie);
        if (blocking != null) {
            eatPlant(zombie, blocking);
            return;
        }
        double speed = zombie.getSpeed() * session.getSpeedFactor() / GameSession.TICKS_PER_SECOND;
        if (zombie.getChilledTicks() > 0) {
            speed /= 2;
        }
        zombie.getPosition().setX(zombie.getPosition().getX() - speed);
        session.checkDeadline(zombie);
        if (zombie.getPosition().getX() < MOWER_LINE) {
            reachHouse(zombie);
        }
    }

    private void kingConvert(Zombie king) {
        if (kingTicks % (10 * GameSession.TICKS_PER_SECOND) != 0) {
            return;
        }
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (zombie.getType() == ZombieType.NORMAL
                    && Math.abs(zombie.getPosition().getX() - king.getPosition().getX()) <= 2
                    && Math.abs(zombie.getPosition().getY() - king.getPosition().getY()) <= 1) {
                session.getZombies().remove(zombie);
                session.spawnZombie(ZombieType.KNIGHT, zombie.getPosition().getX(),
                        (int) zombie.getPosition().getY(), zombie.getSpawnWave());
                System.out.printf("The King knighted a zombie in lane %d!%n",
                        (int) zombie.getPosition().getY());
                return;
            }
        }
    }

    private void gargantuarImpThrow(Zombie zombie) {
        if (zombie.getType() != ZombieType.GARGANTUAR || impThrown.contains(zombie)) {
            return;
        }
        int scaledMax = (int) Math.round(ZombieType.GARGANTUAR.getHitpoints()
                * session.getHealthFactor());
        if (zombie.getHealth() <= scaledMax / 2) {
            impThrown.add(zombie);
            double landing = Math.max(1, zombie.getPosition().getX() - 3);
            session.spawnZombie(ZombieType.IMP, landing, (int) zombie.getPosition().getY(),
                    zombie.getSpawnWave());
        }
    }

    private PlacedPlant blockingPlant(Zombie zombie) {
        int column = (int) Math.round(zombie.getPosition().getX());
        PlacedPlant plant = session.plantAt(column, (int) zombie.getPosition().getY());
        if (plant != null && zombie.getPosition().getX() - column <= 0.4
                && zombie.getPosition().getX() - column >= 0) {
            return plant;
        }
        return null;
    }

    private void eatPlant(Zombie zombie, PlacedPlant plant) {
        if (plant.isSheep()) {
            return;
        }
        if (zombie.getType() == ZombieType.WIZARD) {
            if (!plant.isSheep()) {
                plant.setSheep(true);
                zombie.getBattle().getSheepPlants().add(plant);
            }
            return;
        }
        if (zombie.getType() == ZombieType.GARGANTUAR || zombie.getType() == ZombieType.PIANO) {
            plant.setPumpkinHealth(0);
            plant.setHealth(0);
        } else {
            double dps = zombie.getType().getEatDps() * session.getDamageFactor();
            int bite = (int) Math.ceil(dps / GameSession.TICKS_PER_SECOND);
            if (damagePlant(plant, bite)) {
                return;
            }
            if (plant.getType() == PlantType.ENDURIAN) {
                damageZombie(zombie, plant.getType().getDamage() / GameSession.TICKS_PER_SECOND + 1);
            }
            if (plant.getType() == PlantType.SUN_BEAN
                    && session.getTickCount() % GameSession.TICKS_PER_SECOND == 0) {
                session.getSunManager().addSun(SUN_BEAN_SUN_PER_BITE);
                System.out.printf("The Sun Bean at (%d, %d) released %d sun!%n",
                        plant.getX(), plant.getY(), SUN_BEAN_SUN_PER_BITE);
            }
        }
        if (plant.isDead()) {
            session.removePlant(plant, true);
        }
    }

    private void reachHouse(Zombie zombie) {
        int row = (int) zombie.getPosition().getY();
        if (session.getMode() == models.game.GameMode.I_ZOMBIE) {
            if (session.hasBrain(row)) {
                session.eatBrain(row);
                System.out.printf("Your zombie ate the brain in row %d!%n", row);
            }
            session.getZombies().remove(zombie);
            for (int r = 1; r <= GameSession.ROWS; r++) {
                if (session.hasBrain(r)) {
                    return;
                }
            }
            session.winGame();
            return;
        }
        if (session.hasLawnMower(row)) {
            session.useLawnMower(row);
            List<String> names = new ArrayList<>();
            for (Zombie inRow : new ArrayList<>(session.getZombies())) {
                if (inRow instanceof models.entities.zombie.Zomboss) {
                    continue;
                }
                if (inRow.occupiesRow(row)) {
                    names.add(inRow.getType().getName());
                    session.getZombies().remove(inRow);
                    session.countKill(inRow);
                }
            }
            System.out.printf("The lawn mower in the row %d is triggered and killed these zombies:%n", row);
            for (String name : names) {
                System.out.println("- " + name);
            }
        } else {
            session.loseGame("The zombie ate your brain; LOSER!!!");
        }
    }

    public void damageZombie(Zombie zombie, int damage) {
        damageZombie(zombie, damage, null);
    }

    public void damageZombie(Zombie zombie, int damage, PlantType source) {
        zombie.takeDamage(damage);
        if (zombie.isDead() && session.getZombies().remove(zombie)) {
            announceDeath(zombie);
            session.countKill(zombie);
            recordQuestKill(zombie, source);
            session.getBehaviorManager().onZombieDeath(zombie);
            handleDrops(zombie);
        }
    }

    private void recordQuestKill(Zombie zombie, PlantType source) {
        session.getQuestStats().onKill(source, session.getTickCount(),
                zombie.getPosition().getX(),
                !session.hasLawnMower((int) zombie.getPosition().getY()));
    }

    private void sweetPotatoPull(PlacedPlant potato) {
        if (session.getTickCount() % GameSession.TICKS_PER_SECOND != 0) {
            return;
        }
        for (Zombie zombie : session.getZombies()) {
            int row = (int) zombie.getPosition().getY();
            if (Math.abs(row - potato.getY()) != 1
                    || zombie.getPosition().getX() < potato.getX()
                    || zombie.getPosition().getX() - potato.getX() > SWEET_POTATO_RANGE) {
                continue;
            }
            zombie.getPosition().setY(potato.getY());
            System.out.printf("The Sweet Potato pulled the %s into lane %d!%n",
                    zombie.getType().getName(), potato.getY());
        }
    }

    public boolean damagePlant(PlacedPlant plant, int damage) {
        if (plant.getIceHealth() > 0) {
            plant.setIceHealth(Math.max(0, plant.getIceHealth() - damage));
            if (plant.getIceHealth() == 0) {
                plant.setFreezeLevel(0);
                System.out.printf("The ice around %s at (%d, %d) broke apart.%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
            }
            return true;
        }
        if (plant.getOctopusHealth() > 0) {
            plant.setOctopusHealth(Math.max(0, plant.getOctopusHealth() - damage));
            return true;
        }
        if (plant.getPumpkinHealth() > 0) {
            plant.setPumpkinHealth(Math.max(0, plant.getPumpkinHealth() - damage));
            return true;
        }
        plant.setHealth(plant.getHealth() - damage);
        return false;
    }

    public void onPlantEaten(PlacedPlant plant) {
        if (plant.getType() == PlantType.GARLIC) {
            moveZombiesOffLane(plant);
        } else if (plant.getType() == PlantType.HYPNO_SHROOM) {
            hypnotizeEater(plant);
        } else if (plant.getType().getTags().contains(PlantTag.EXPLOSIVE)) {
            damageArea(plant.getX(), plant.getY(), 1, plant.getType());
            System.out.printf("%s exploded as it died!%n", plant.getType().getName());
        }
    }

    private void moveZombiesOffLane(PlacedPlant garlic) {
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() != garlic.getY()
                    || Math.abs(zombie.getPosition().getX() - garlic.getX()) > 1) {
                continue;
            }
            int row = garlic.getY() + (session.getRandom().nextBoolean() ? 1 : -1);
            if (row < 1) {
                row = garlic.getY() + 1;
            }
            if (row > GameSession.ROWS) {
                row = garlic.getY() - 1;
            }
            zombie.getPosition().setY(row);
            System.out.printf("The garlic pushed the %s to lane %d!%n",
                    zombie.getType().getName(), row);
        }
    }

    private void hypnotizeEater(PlacedPlant shroom) {
        for (Zombie zombie : session.getZombies()) {
            if (zombie instanceof models.entities.zombie.Zomboss) {
                continue;
            }
            if ((int) zombie.getPosition().getY() == shroom.getY()
                    && Math.abs(zombie.getPosition().getX() - shroom.getX()) <= 1) {
                zombie.getBattle().setHypnotized(true);
                System.out.printf("The %s is hypnotized and fights for you now!%n",
                        zombie.getType().getName());
                return;
            }
        }
    }

    private void announceDeath(Zombie zombie) {
        double x = zombie.getPosition().getX();
        String xText = x == Math.floor(x) ? String.valueOf((int) x) : String.format("%.1f", x);
        System.out.printf("Zombie of type %s is dead at (%s, %d)%n",
                zombie.getType().getName(), xText, (int) zombie.getPosition().getY());
    }

    private void handleDrops(Zombie zombie) {
        if (zombie.isGlowing() && session.getPlantFoods() < 3) {
            session.dropPlantFood((int) Math.round(zombie.getPosition().getX()),
                    (int) zombie.getPosition().getY());
            System.out.println("The glowing zombie dropped a plant food; pick it up!");
        }
        if (session.getRandom().nextInt(100) < 10) {
            dropTreasure();
        }
    }

    private void dropTreasure() {
        UserManager userManager = UserManager.getInstance();
        int pick = session.getRandom().nextInt(3);
        if (pick == 0) {
            userManager.addDiamonds(1);
            System.out.printf("A zombie dropeed a diamond; you have %d diamonds now.%n",
                    userManager.getCurrentUser().getDiamonds().getAmount());
        } else if (pick == 1) {
            userManager.addCoins(50);
            System.out.printf("A zombie dropeed a coin; you have %d coins now.%n",
                    userManager.getCurrentUser().getCoins().getAmount());
        } else {
            userManager.addPots(1);
            System.out.printf("A zombie dropeed a pot; you have %d pots now.%n",
                    userManager.getCurrentUser().getPots().getAmount());
        }
    }

    public void applyPlantFood(PlacedPlant plant) {
        plant.triggerPlantFood();
        plantFoodEffects.apply(plant);
    }

    boolean graveAhead(int row, double x) {
        for (int column = (int) Math.ceil(x); column <= GameSession.COLS; column++) {
            Tile tile = session.getGrid().getTile(column - 1, row - 1);
            if (tile != null && tile.getTerrain() == TerrainType.GRAVE) {
                return true;
            }
        }
        return false;
    }

    Zombie firstZombieInRowAfter(int row, double x) {
        Zombie first = null;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.occupiesRow(row) && zombie.getPosition().getX() >= x
                    && (first == null || zombie.getPosition().getX() < first.getPosition().getX())) {
                first = zombie;
            }
        }
        return first;
    }

    List<Zombie> zombiesInRowAfter(int row, double x) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if (zombie.occupiesRow(row) && zombie.getPosition().getX() >= x) {
                result.add(zombie);
            }
        }
        return result;
    }

    public boolean blockedAt(int row, double x, PlantType source) {
        Tile grave = graveBetween(row, x - 0.5, x + 0.1);
        if (grave != null) {
            grave.damageGrave(plantDamage(source));
            if (grave.getTerrain() != TerrainType.GRAVE) {
                grantGraveContent(grave);
            }
            return true;
        }
        models.game.PushedObject pushed = pushedObjectBetween(row, x - 0.5, x + 0.1);
        if (pushed != null) {
            pushed.damage(plantDamage(source));
            return true;
        }
        PlacedPlant blocked = disabledPlantBetween(row, x - 0.5, x + 0.5);
        if (blocked != null) {
            damageDisablingLayer(blocked, source);
            return true;
        }
        return false;
    }

    private Tile graveBetween(int row, double fromX, double toX) {
        for (int column = (int) Math.floor(fromX) + 1; column <= Math.min(GameSession.COLS, toX); column++) {
            Tile tile = session.getGrid().getTile(column - 1, row - 1);
            if (tile != null && tile.getTerrain() == TerrainType.GRAVE) {
                return tile;
            }
        }
        return null;
    }

    public void grantGraveContent(Tile tile) {
        if (tile.getGraveSunContent() > 0) {
            session.getSunManager().addSun(tile.getGraveSunContent());
            System.out.printf("The broken grave released %d sun!%n", tile.getGraveSunContent());
        }
        if (tile.isGravePlantFood()) {
            session.setPlantFoods(session.getPlantFoods() + 1);
            System.out.printf("The broken grave held a plant food; you have %d plant foods now.%n",
                    session.getPlantFoods());
        }
        tile.clearGraveContent();
    }

    private models.game.PushedObject pushedObjectBetween(int row, double fromX, double toX) {
        for (models.game.PushedObject pushed : session.getPushedObjects()) {
            if (pushed.getRow() == row && !pushed.isDestroyed()
                    && pushed.getX() > fromX && pushed.getX() <= toX) {
                return pushed;
            }
        }
        return null;
    }

    private PlacedPlant disabledPlantBetween(int row, double fromX, double toX) {
        for (PlacedPlant other : session.getPlants()) {
            if (other.getY() == row && other.getX() > fromX && other.getX() < toX
                    && (other.getIceHealth() > 0 || other.getOctopusHealth() > 0)) {
                return other;
            }
        }
        return null;
    }

    private void damageDisablingLayer(PlacedPlant blocked, PlantType source) {
        int damage = source.getTags().contains(PlantTag.FIRE) ? 600 : plantDamage(source);
        if (blocked.getIceHealth() > 0) {
            blocked.setIceHealth(Math.max(0, blocked.getIceHealth() - damage));
            if (blocked.getIceHealth() == 0) {
                blocked.setFreezeLevel(0);
                System.out.printf("The ice around %s at (%d, %d) shattered.%n",
                        blocked.getType().getName(), blocked.getX(), blocked.getY());
            }
        } else if (blocked.getOctopusHealth() > 0) {
            blocked.setOctopusHealth(Math.max(0, blocked.getOctopusHealth() - damage));
            if (blocked.getOctopusHealth() == 0) {
                System.out.printf("The octopus on %s at (%d, %d) was destroyed.%n",
                        blocked.getType().getName(), blocked.getX(), blocked.getY());
            }
        }
    }
}
