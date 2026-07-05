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
    private static final int INSTANT_KILL_DAMAGE = 9999;
    private static final double MOWER_LINE = 0.5;

    private final GameSession session;
    private final Set<Zombie> impThrown = new HashSet<>();
    private int kingTicks;

    public CombatManager(GameSession session) {
        this.session = session;
    }

    public void plantsAct() {
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.isDead()) {
                continue;
            }
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
        double interval = Math.max(0.5, type.getActionInterval());
        if (type == PlantType.MAGNET_SHROOM) {
            session.getPlantActionManager().magnet(plant);
            plant.setActionCooldownTicks((int) Math.round(interval * GameSession.TICKS_PER_SECOND));
            return;
        }
        if (specialShot(plant, type)) {
            plant.setActionCooldownTicks((int) Math.round(interval * GameSession.TICKS_PER_SECOND));
            return;
        }
        switch (type.getCategory()) {
            case SUN_PRODUCER:
                produceSun(plant);
                break;
            case SHOOTER:
                shoot(plant, false);
                break;
            case STRIKE_THROUGH:
                strikeThrough(plant);
                break;
            case LOBBER:
                lob(plant);
                break;
            case HOMING:
                homingShot(plant);
                break;
            case MELEE:
                meleeHit(plant);
                break;
            case EXPLOSIVE:
                trapCheck(plant);
                return;
            default:
                return;
        }
        plant.setActionCooldownTicks((int) Math.round(interval * GameSession.TICKS_PER_SECOND));
    }

    private void produceSun(PlacedPlant plant) {
        if (plant.isSunPending()) {
            return;
        }
        int value = productionValue(plant);
        session.getSunManager().addProducedSun(plant.getX(), plant.getY(), value);
        plant.setSunPending(true);
        if (plant.getType().getTags().contains(PlantTag.WRAMP_UP) && plant.getGrowthStage() < 3) {
            plant.setGrowthStage(plant.getGrowthStage() + 1);
        }
        System.out.printf("plant %s produced a sun at (%d, %d)%n",
                plant.getType().getName(), plant.getX(), plant.getY());
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

    private void shoot(PlacedPlant plant, boolean pierceGraves) {
        Zombie target = firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target == null) {
            return;
        }
        if (!pierceGraves) {
            Tile grave = graveBetween(plant.getY(), plant.getX(), target.getPosition().getX());
            if (grave != null) {
                grave.damageGrave(plantDamage(plant.getType()));
                return;
            }
        }
        int shots = plant.getType() == PlantType.PEA_POD ? plant.getStackCount() : 1;
        boolean torched = plant.getType().getTags().contains(PlantTag.PEA)
                && torchwoodBetween(plant.getY(), plant.getX(), target.getPosition().getX());
        for (int i = 0; i < shots; i++) {
            hitZombie(target, plant.getType());
            if (torched) {
                target.setChilledTicks(0);
                hitZombie(target, plant.getType());
            }
        }
    }

    private boolean torchwoodBetween(int row, double fromX, double toX) {
        for (PlacedPlant other : session.getPlants()) {
            if (other.getType() == PlantType.TORCHWOOD && other.getY() == row
                    && other.getX() > fromX && other.getX() <= toX && !other.isDisabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean specialShot(PlacedPlant plant, PlantType type) {
        switch (type) {
            case THREEPEATER:
                shootThreeLanes(plant);
                return true;
            case SPLIT_PEA:
                shoot(plant, false);
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
                hypnotizeShot(plant);
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
            Zombie target = firstZombieInRowAfter(row, plant.getX());
            if (target != null) {
                hitZombie(target, plant.getType());
            }
        }
    }

    private void shootBackward(PlacedPlant plant, int shots) {
        Zombie target = lastZombieInRowBefore(plant.getY(), plant.getX());
        for (int i = 0; i < shots && target != null && !target.isDead(); i++) {
            hitZombie(target, plant.getType());
        }
    }

    private void starShot(PlacedPlant plant) {
        Zombie forward = firstZombieInRowAfter(plant.getY(), plant.getX());
        if (forward != null) {
            hitZombie(forward, plant.getType());
        }
        Zombie backward = lastZombieInRowBefore(plant.getY(), plant.getX());
        if (backward != null) {
            hitZombie(backward, plant.getType());
        }
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (Math.abs(zombie.getPosition().getX() - plant.getX()) <= 0.5
                    && (int) zombie.getPosition().getY() != plant.getY()) {
                hitZombie(zombie, plant.getType());
            }
        }
    }

    private void diagonalShot(PlacedPlant plant) {
        for (int row : new int[] { plant.getY() - 1, plant.getY() + 1 }) {
            if (row < 1 || row > GameSession.ROWS) {
                continue;
            }
            Zombie ahead = firstZombieInRowAfter(row, plant.getX());
            if (ahead != null) {
                hitZombie(ahead, plant.getType());
            }
            Zombie behind = lastZombieInRowBefore(row, plant.getX());
            if (behind != null) {
                hitZombie(behind, plant.getType());
            }
        }
    }

    private void shortRangeShot(PlacedPlant plant) {
        Zombie target = firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target != null && target.getPosition().getX() - plant.getX() <= 3) {
            hitZombie(target, plant.getType());
        }
    }

    private void kernelLob(PlacedPlant plant) {
        Zombie target = firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target == null) {
            return;
        }
        if (session.getRandom().nextInt(100) < 25) {
            target.setFrozenTicks(3 * GameSession.TICKS_PER_SECOND);
            damageZombie(target, plantDamage(plant.getType()) * 2, plant.getType());
            System.out.printf("Butter pinned the %s in place!%n", target.getType().getName());
        } else {
            hitZombie(target, plant.getType());
        }
    }

    private void hypnotizeShot(PlacedPlant plant) {
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

    private void strikeThrough(PlacedPlant plant) {
        for (Zombie zombie : zombiesInRowAfter(plant.getY(), plant.getX())) {
            hitZombie(zombie, plant.getType());
        }
    }

    private void lob(PlacedPlant plant) {
        Zombie target = firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target == null) {
            return;
        }
        if (plant.getType().getTags().contains(PlantTag.AOE)) {
            damageArea(target.getPosition().getX(), target.getPosition().getY(), 1, plant.getType());
        } else {
            hitZombie(target, plant.getType());
        }
    }

    private void homingShot(PlacedPlant plant) {
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
        if (target != null) {
            hitZombie(target, plant.getType());
        }
    }

    private void meleeHit(PlacedPlant plant) {
        for (Zombie zombie : zombiesInRowAfter(plant.getY(), plant.getX() - 0.5)) {
            if (zombie.getPosition().getX() <= plant.getX() + 1.5) {
                hitZombie(zombie, plant.getType());
                if (!plant.getType().getTags().contains(PlantTag.AOE)) {
                    return;
                }
            }
        }
    }

    private void trapCheck(PlacedPlant plant) {
        if (!plant.getType().getTags().contains(PlantTag.TRAP)) {
            return;
        }
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() == plant.getY()
                    && Math.abs(zombie.getPosition().getX() - plant.getX()) <= 0.5) {
                explode(plant);
                return;
            }
        }
    }

    public void explode(PlacedPlant plant) {
        if (plant.getType() == PlantType.JALAPENO) {
            for (Zombie zombie : zombiesInRowAfter(plant.getY(), 0)) {
                zombie.setChilledTicks(0);
                damageZombie(zombie, plantDamage(plant.getType()));
            }
        } else {
            damageArea(plant.getX(), plant.getY(), 1, plant.getType());
        }
        session.removePlant(plant, false);
    }

    private void damageArea(double centerX, double centerY, int radius, PlantType source) {
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (Math.abs(zombie.getPosition().getX() - centerX) <= radius + 0.5
                    && Math.abs(zombie.getPosition().getY() - centerY) <= radius) {
                hitZombie(zombie, source);
            }
        }
    }

    private void hitZombie(Zombie zombie, PlantType source) {
        if (!session.getBehaviorManager().beforeHit(zombie, source)) {
            return;
        }
        if (source.getTags().contains(PlantTag.ICE)) {
            zombie.setChilledTicks(3 * GameSession.TICKS_PER_SECOND);
        }
        if (source.getTags().contains(PlantTag.FIRE)) {
            zombie.setChilledTicks(0);
        }
        if (source.getTags().contains(PlantTag.POISON)) {
            zombie.damageHealthDirectly(plantDamage(source));
            if (zombie.isDead() && session.getZombies().remove(zombie)) {
                announceDeath(zombie);
                session.countKill(zombie);
                recordQuestKill(zombie, source);
                session.getBehaviorManager().onZombieDeath(zombie);
                handleDrops(zombie);
            }
            return;
        }
        damageZombie(zombie, plantDamage(source), source);
    }

    private int plantDamage(PlantType type) {
        int damage = session.effectiveDamage(type);
        return damage >= 9999 ? INSTANT_KILL_DAMAGE : damage;
    }

    public void applyRadioactiveExplosion(int x, int y) {
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (Math.abs(zombie.getPosition().getX() - x) <= 2.5
                    && Math.abs(zombie.getPosition().getY() - y) <= 2) {
                damageZombie(zombie, 150);
            }
        }
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (Math.abs(plant.getX() - x) <= 1 && Math.abs(plant.getY() - y) <= 1) {
                plant.setHealth(plant.getHealth() - 80);
                if (plant.isDead()) {
                    session.removePlant(plant, true);
                }
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
        if (session.getBehaviorManager().handleSpecial(zombie)) {
            return;
        }
        if (zombie.getType() == ZombieType.KING) {
            kingConvert();
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

    private void kingConvert() {
        if (kingTicks % (10 * GameSession.TICKS_PER_SECOND) != 0) {
            return;
        }
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (zombie.getType() == ZombieType.NORMAL) {
                session.getZombies().remove(zombie);
                session.spawnZombie(ZombieType.KNIGHT, zombie.getPosition().getX(),
                        (int) zombie.getPosition().getY(), zombie.getSpawnWave());
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
            session.spawnZombie(ZombieType.IMP, 3, (int) zombie.getPosition().getY(),
                    zombie.getSpawnWave());
        }
    }

    private PlacedPlant blockingPlant(Zombie zombie) {
        int column = (int) Math.round(zombie.getPosition().getX());
        PlacedPlant plant = session.plantAt(column, (int) zombie.getPosition().getY());
        if (plant != null && zombie.getPosition().getX() - column <= 0.4
                && zombie.getPosition().getX() >= column - 0.4) {
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
            if (plant.getPumpkinHealth() > 0) {
                plant.setPumpkinHealth(Math.max(0, plant.getPumpkinHealth() - bite));
            } else {
                plant.setHealth(plant.getHealth() - bite);
            }
            if (plant.getType() == PlantType.ENDURIAN) {
                damageZombie(zombie, plant.getType().getDamage() / GameSession.TICKS_PER_SECOND + 1);
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
                if ((int) inRow.getPosition().getY() == row) {
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

    public void onPlantEaten(PlacedPlant plant) {
        if (plant.getType() == PlantType.GARLIC) {
            moveZombiesOffLane(plant);
        } else if (plant.getType() == PlantType.HYPNO_SHROOM) {
            hypnotizeEater(plant);
        } else if (plant.getType() == PlantType.SUN_BEAN) {
            session.getSunManager().addSun(50);
            System.out.println("The digested Sun Bean released 50 sun!");
        } else if (plant.getType().getTags().contains(PlantTag.EXPLOSIVE)) {
            damageArea(plant.getX(), plant.getY(), 1, PlantType.EXPLODE_O_NUT);
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
            session.setPlantFoods(session.getPlantFoods() + 1);
            System.out.printf("The glowing zombie dropeed a plant food; you have %d plant foods now.%n",
                    session.getPlantFoods());
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
        if (applySpecificPlantFood(plant)) {
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
                explode(plant);
                break;
            default:
                powerAttack(plant);
                break;
        }
    }

    private boolean applySpecificPlantFood(PlacedPlant plant) {
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
            case CHOMPER:
                devour(plant, 3);
                return true;
            case SQUASH:
                crushRandom(plant, 2);
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
        Zombie target = firstZombieInRowAfter(plant.getY(), plant.getX());
        if (target != null) {
            damageZombie(target, plantDamage(plant.getType()) * 20, plant.getType());
        }
        powerAttack(plant);
    }

    private void fanBarrage(PlacedPlant plant) {
        int damage = plantDamage(plant.getType()) * 5;
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            damageZombie(zombie, damage, plant.getType());
        }
    }

    private void butterEveryZombie(PlacedPlant plant) {
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            zombie.setFrozenTicks(3 * GameSession.TICKS_PER_SECOND);
            damageZombie(zombie, plantDamage(plant.getType()) * 2, plant.getType());
        }
        System.out.println("Butter rained on every zombie's head!");
    }

    private void giantMelons(PlacedPlant plant) {
        for (int i = 0; i < 3 && !session.getZombies().isEmpty(); i++) {
            Zombie target = session.getZombies().get(
                    session.getRandom().nextInt(session.getZombies().size()));
            damageArea(target.getPosition().getX(), target.getPosition().getY(), 1,
                    plant.getType());
        }
    }

    private void freezeLane(PlacedPlant plant) {
        for (Zombie zombie : new ArrayList<>(zombiesInRowAfter(plant.getY(), 0))) {
            zombie.setFrozenTicks(4 * GameSession.TICKS_PER_SECOND);
            damageZombie(zombie, plantDamage(plant.getType()) * 5, plant.getType());
        }
    }

    private void devour(PlacedPlant plant, int count) {
        for (int i = 0; i < count && !session.getZombies().isEmpty(); i++) {
            Zombie target = session.getZombies().get(
                    session.getRandom().nextInt(session.getZombies().size()));
            damageZombie(target, INSTANT_KILL_DAMAGE, plant.getType());
        }
    }

    private void crushRandom(PlacedPlant plant, int count) {
        devour(plant, count);
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
                damageZombie(zombie, INSTANT_KILL_DAMAGE, plant.getType());
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
        int damage = plantDamage(plant.getType()) * 10;
        for (Zombie zombie : new ArrayList<>(zombiesInRowAfter(plant.getY(), 0))) {
            damageZombie(zombie, damage, plant.getType());
        }
    }

    private Zombie firstZombieInRowAfter(int row, double x) {
        Zombie first = null;
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() == row && zombie.getPosition().getX() >= x
                    && (first == null || zombie.getPosition().getX() < first.getPosition().getX())) {
                first = zombie;
            }
        }
        return first;
    }

    private List<Zombie> zombiesInRowAfter(int row, double x) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() == row && zombie.getPosition().getX() >= x) {
                result.add(zombie);
            }
        }
        return result;
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
}
