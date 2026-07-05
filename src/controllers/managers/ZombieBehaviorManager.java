package controllers.managers;

import models.entities.plant.PlantCategory;
import models.entities.plant.PlantTag;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.game.PushedObject;
import models.game.Sun;
import models.map.TerrainType;
import models.map.Tile;
import models.progress.chapter.Chapter;
import models.progress.chapter.DarkAges;
import models.progress.chapter.Egypt;
import models.progress.chapter.FrostBite;
import models.progress.chapter.WaveyBeach;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ZombieBehaviorManager {
    private final GameSession session;
    private final CombatManager combatManager;
    private final Set<Zombie> pushersWithObject = new HashSet<>();

    public ZombieBehaviorManager(GameSession session, CombatManager combatManager) {
        this.session = session;
        this.combatManager = combatManager;
    }

    private Chapter chapter() {
        return session.getLevel() == null ? null : session.getLevel().getChapter();
    }

    public void spawnFrozenZombiesIfFrostbite() {
        if (!(chapter() instanceof FrostBite)) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
            Zombie zombie = session.spawnZombie(ZombieType.NORMAL, 5 + 2 * i, row, 0);
            zombie.getBattle().setIceHealth(600);
            System.out.printf("A frozen zombie stands at (%d, %d).%n", 5 + 2 * i, row);
        }
    }

    public boolean handleSpecial(Zombie zombie) {
        if (zombie.getBattle().getIceHealth() > 0) {
            return true;
        }
        if (zombie.getBattle().isHypnotized()) {
            hypnotizedAct(zombie);
            return true;
        }
        if (zombie.getBattle().isSunProducer()) {
            sunProducerAct(zombie);
            return true;
        }
        tickCooldown(zombie);
        switch (zombie.getType()) {
            case RA:
                raSteal(zombie);
                return false;
            case EXPLORER:
                torchBurn(zombie);
                return false;
            case TOMB_RAISER:
                raiseTombs(zombie);
                return false;
            case HUNTER:
                hunterThrow(zombie);
                return false;
            case TROGLOBITE:
                ensurePushedObject(zombie, PushedObject.Kind.ICE_BLOCK);
                return false;
            case ARCAD:
                ensurePushedObject(zombie, PushedObject.Kind.ARCADE_MACHINE);
                return false;
            case BARREL_ROLLER:
                ensurePushedObject(zombie, PushedObject.Kind.BARREL);
                return false;
            case DODO:
                return dodoFly(zombie);
            case FISHERMAN:
                fishermanHook(zombie);
                return true;
            case OCTOPUS:
                octopusThrow(zombie);
                return false;
            case JUGGLER:
                jugglerSpin(zombie);
                return false;
            case WIZARD:
                wizardCurse(zombie);
                return false;
            case ALLSTAR:
            case SURFER:
                return allStarCharge(zombie);
            case PHARAOH:
                pharaohRage(zombie);
                return false;
            case FAST_SWIMMER:
                fastSwim(zombie);
                return false;
            case TURQUOISE:
                turquoiseAct(zombie);
                return false;
            case PROSPECTOR:
                return prospectorAct(zombie);
            case PIANO:
                pianoShuffle(zombie);
                return false;
            case NEWSPAPER:
                newspaperRage(zombie);
                return false;
            case PEASHOOTER_ZOMBIE:
                zombotanyShoot(zombie);
                return false;
            case JALAPENO_ZOMBIE:
                return zombotanyJalapeno(zombie);
            case SQUASH_ZOMBIE:
                return zombotanySquash(zombie);
            default:
                return false;
        }
    }

    private void tickCooldown(Zombie zombie) {
        if (zombie.getBattle().getAbilityCooldown() > 0) {
            zombie.getBattle().setAbilityCooldown(zombie.getBattle().getAbilityCooldown() - 1);
        }
        zombie.getBattle().setTicksSinceShotAt(zombie.getBattle().getTicksSinceShotAt() + 1);
    }

    private boolean cooldownReady(Zombie zombie, int cooldownTicks) {
        if (zombie.getBattle().getAbilityCooldown() > 0) {
            return false;
        }
        zombie.getBattle().setAbilityCooldown(cooldownTicks);
        return true;
    }

    private void hypnotizedAct(Zombie zombie) {
        double step = zombie.getSpeed() / GameSession.TICKS_PER_SECOND;
        Zombie enemy = null;
        for (Zombie other : session.getZombies()) {
            if (other != zombie && !other.getBattle().isHypnotized()
                    && (int) other.getPosition().getY() == (int) zombie.getPosition().getY()
                    && Math.abs(other.getPosition().getX() - zombie.getPosition().getX()) <= 0.5) {
                enemy = other;
                break;
            }
        }
        if (enemy != null) {
            combatManager.damageZombie(enemy, zombie.getType().getEatDps()
                    / GameSession.TICKS_PER_SECOND + 1);
            return;
        }
        zombie.getPosition().setX(zombie.getPosition().getX() + step);
        if (zombie.getPosition().getX() > GameSession.COLS + 0.5) {
            session.getZombies().remove(zombie);
        }
    }

    private void sunProducerAct(Zombie zombie) {
        if (zombie.getBattle().getAbilityCooldown() > 0) {
            zombie.getBattle().setAbilityCooldown(zombie.getBattle().getAbilityCooldown() - 1);
            return;
        }
        int interval = Math.max(30, 120 - session.getTickCount() / 50);
        zombie.getBattle().setAbilityCooldown(interval);
        session.getSunManager().addSun(25);
        System.out.printf("Your sun-producer zombie in lane %d made 25 sun. Balance: %d%n",
                (int) zombie.getPosition().getY(), session.getSunManager().getSunBalance());
    }

    private void raSteal(Zombie zombie) {
        if (!cooldownReady(zombie, 30)) {
            return;
        }
        Sun stolen = session.getSunManager().stealLanded();
        if (stolen != null) {
            zombie.getBattle().addStolenSun(stolen.getValue());
            System.out.printf("Ra stole a sun worth %d from the ground!%n", stolen.getValue());
        }
    }

    private void torchBurn(Zombie zombie) {
        if (!zombie.getBattle().isTorchLit()) {
            return;
        }
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.getY() == (int) zombie.getPosition().getY()
                    && plant.getX() < zombie.getPosition().getX()
                    && zombie.getPosition().getX() - plant.getX() <= 1.5) {
                System.out.printf("The Explorer's torch burnt %s at (%d, %d)!%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
                session.removePlant(plant, true);
            }
        }
    }

    private void raiseTombs(Zombie zombie) {
        if (!cooldownReady(zombie, 100)) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            int col = 3 + session.getRandom().nextInt(GameSession.COLS - 3);
            int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
            Tile tile = session.getGrid().getTile(col - 1, row - 1);
            if (tile.getTerrain() == TerrainType.NORMAL && session.plantAt(col, row) == null) {
                tile.setTerrain(TerrainType.GRAVE);
                System.out.printf("The Tomb Raiser raised a grave at (%d, %d).%n", col, row);
            }
        }
    }

    private void hunterThrow(Zombie zombie) {
        if (!cooldownReady(zombie, 60)) {
            return;
        }
        PlacedPlant target = nearestPlantInRow(zombie);
        if (target == null || target.getIceHealth() > 0) {
            return;
        }
        target.setIceHits(target.getIceHits() + 1);
        if (target.getIceHits() >= 3) {
            target.setIceHits(0);
            target.setIceHealth(600);
            System.out.printf("%s at (%d, %d) is frozen solid by the Hunter!%n",
                    target.getType().getName(), target.getX(), target.getY());
        }
    }

    private void ensurePushedObject(Zombie zombie, PushedObject.Kind kind) {
        if (pushersWithObject.contains(zombie)) {
            return;
        }
        pushersWithObject.add(zombie);
        PushedObject pushed = new PushedObject(kind, 1100,
                zombie.getPosition().getX() - 0.5, (int) zombie.getPosition().getY());
        session.getPushedObjects().add(pushed);
    }

    private boolean dodoFly(Zombie zombie) {
        int aheadCol = (int) Math.floor(zombie.getPosition().getX());
        PlacedPlant blocker = session.plantAt(aheadCol, (int) zombie.getPosition().getY());
        if (blocker == null) {
            return false;
        }
        boolean flyable = (blocker.getType().getCategory() == PlantCategory.WALL_NUT
                && blocker.getType() != PlantType.TALL_NUT)
                || blocker.getType().getTags().contains(PlantTag.TRAP)
                || blocker.getType().getTags().contains(PlantTag.MOVE_ZOMBIES);
        if (!flyable) {
            return false;
        }
        zombie.getPosition().setX(zombie.getPosition().getX() - 1.2);
        return true;
    }

    private void fishermanHook(Zombie zombie) {
        zombie.getPosition().setX(GameSession.COLS);
        if (!cooldownReady(zombie, 70)) {
            return;
        }
        PlacedPlant target = nearestPlantInRow(zombie);
        if (target == null) {
            return;
        }
        if (target.getX() >= GameSession.COLS - 1) {
            System.out.printf("The Fisherman hurled %s at (%d, %d) away!%n",
                    target.getType().getName(), target.getX(), target.getY());
            session.removePlant(target, true);
            return;
        }
        if (session.plantAt(target.getX() + 1, target.getY()) == null) {
            target.setX(target.getX() + 1);
            System.out.printf("The Fisherman hooked %s one tile forward to (%d, %d).%n",
                    target.getType().getName(), target.getX(), target.getY());
        }
    }

    private void octopusThrow(Zombie zombie) {
        if (!cooldownReady(zombie, 80)) {
            return;
        }
        PlacedPlant target = nearestPlantInRow(zombie);
        if (target != null && target.getOctopusHealth() == 0) {
            target.setOctopusHealth(300);
            System.out.printf("An octopus landed on %s at (%d, %d)!%n",
                    target.getType().getName(), target.getX(), target.getY());
        }
    }

    private void jugglerSpin(Zombie zombie) {
        if (zombie.getBattle().isSpinning() && zombie.getBattle().getTicksSinceShotAt() > 30) {
            zombie.getBattle().setSpinning(false);
            zombie.setSpeed(zombie.getType().getSpeed());
        }
    }

    private void wizardCurse(Zombie zombie) {
        if (!cooldownReady(zombie, 80)) {
            return;
        }
        for (PlacedPlant plant : session.getPlants()) {
            if (!plant.isSheep()) {
                plant.setSheep(true);
                zombie.getBattle().getSheepPlants().add(plant);
                System.out.printf("The Wizard turned %s at (%d, %d) into a sheep!%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
                return;
            }
        }
    }

    private boolean allStarCharge(Zombie zombie) {
        if (!zombie.getBattle().isCharging()) {
            return false;
        }
        double step = 3 * zombie.getSpeed() / GameSession.TICKS_PER_SECOND;
        zombie.getPosition().setX(zombie.getPosition().getX() - step);
        session.checkDeadline(zombie);
        int col = (int) Math.round(zombie.getPosition().getX());
        PlacedPlant tackled = session.plantAt(col, (int) zombie.getPosition().getY());
        if (tackled != null) {
            System.out.printf("The All Star tackled %s at (%d, %d)!%n",
                    tackled.getType().getName(), tackled.getX(), tackled.getY());
            session.removePlant(tackled, true);
            zombie.getBattle().setCharging(false);
            zombie.setSpeed(zombie.getType().getSpeed() / 3);
        }
        if (zombie.getPosition().getX() < 0.5) {
            zombie.getBattle().setCharging(false);
        }
        return true;
    }

    private void turquoiseAct(Zombie zombie) {
        if (zombie.getBattle().getStealTicksLeft() > 0) {
            zombie.getBattle().setStealTicksLeft(zombie.getBattle().getStealTicksLeft() - 1);
            if (zombie.getBattle().getStealTicksLeft() % 10 == 0) {
                int stolen = Math.min(25, session.getSunManager().getSunBalance());
                session.getSunManager().addSun(-stolen);
                zombie.getBattle().addStolenSun(stolen);
            }
            if (zombie.getBattle().getStealTicksLeft() == 0) {
                turquoiseLaser(zombie);
            }
            return;
        }
        if (zombie.getBattle().isLaserFired()) {
            return;
        }
        for (PlacedPlant plant : session.getPlants()) {
            if (Math.abs(plant.getX() - zombie.getPosition().getX()) <= 4
                    && Math.abs(plant.getY() - zombie.getPosition().getY()) <= 4) {
                zombie.getBattle().setStealTicksLeft(50);
                System.out.println("The Turquoise zombie started draining your sun!");
                return;
            }
        }
    }

    private void turquoiseLaser(Zombie zombie) {
        zombie.getBattle().setLaserFired(true);
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.getY() == (int) zombie.getPosition().getY()
                    && plant.getX() < zombie.getPosition().getX()
                    && zombie.getPosition().getX() - plant.getX() <= 4) {
                System.out.printf("The Turquoise laser vaporized %s at (%d, %d)!%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
                session.removePlant(plant, true);
            }
        }
    }

    private boolean prospectorAct(Zombie zombie) {
        int dynamite = zombie.getBattle().getDynamiteTicks();
        if (dynamite > 0) {
            zombie.getBattle().setDynamiteTicks(dynamite - 1);
            if (dynamite - 1 == 0) {
                zombie.getBattle().setReversed(true);
                zombie.getPosition().setX(1);
                System.out.println("The Prospector's dynamite blasted him behind your defenses!");
            }
            return false;
        }
        if (!zombie.getBattle().isReversed()) {
            return false;
        }
        int col = (int) Math.round(zombie.getPosition().getX());
        PlacedPlant blocking = session.plantAt(col, (int) zombie.getPosition().getY());
        if (blocking != null) {
            blocking.setHealth(blocking.getHealth()
                    - zombie.getType().getEatDps() / GameSession.TICKS_PER_SECOND - 1);
            if (blocking.isDead()) {
                session.removePlant(blocking, true);
            }
            return true;
        }
        zombie.getPosition().setX(zombie.getPosition().getX()
                + zombie.getSpeed() / GameSession.TICKS_PER_SECOND);
        if (zombie.getPosition().getX() > GameSession.COLS + 0.5) {
            session.getZombies().remove(zombie);
        }
        return true;
    }

    private void pianoShuffle(Zombie zombie) {
        if (!cooldownReady(zombie, 50)) {
            return;
        }
        for (Zombie other : session.getZombies()) {
            if (other == zombie || other.getBattle().isSunProducer()) {
                continue;
            }
            int row = (int) other.getPosition().getY();
            int shifted = row + (session.getRandom().nextBoolean() ? 1 : -1);
            if (shifted >= 1 && shifted <= GameSession.ROWS) {
                other.getPosition().setY(shifted);
            }
        }
        System.out.println("The Pianist's tune shuffled the zombies between lanes!");
    }

    private void pharaohRage(Zombie zombie) {
        if (zombie.totalArmor() == 0 && !zombie.getBattle().isRaging()) {
            zombie.getBattle().setRaging(true);
            zombie.setSpeed(zombie.getType().getSpeed() * 3);
            System.out.println("The Pharaoh broke out of his sarcophagus and is running!");
        }
    }

    private void fastSwim(Zombie zombie) {
        if (isUnderwater(zombie)) {
            zombie.getPosition().setX(zombie.getPosition().getX()
                    - 2 * zombie.getSpeed() / GameSession.TICKS_PER_SECOND);
        }
    }

    private void releaseWeasel(Zombie hoarder) {
        if (session.getRandom().nextInt(100) < 20) {
            session.spawnZombie(ZombieType.WEASEL, hoarder.getPosition().getX(),
                    (int) hoarder.getPosition().getY(),
                    Math.max(1, hoarder.getSpawnWave()));
            System.out.println("A weasel burst out of the Weasel Hoarder's coat!");
        }
    }

    private void newspaperRage(Zombie zombie) {
        if (zombie.totalArmor() == 0 && !zombie.getBattle().isRaging()) {
            zombie.getBattle().setRaging(true);
            zombie.setSpeed(zombie.getType().getSpeed() * 3);
            System.out.println("The Newspaper zombie lost his paper and is furious!");
        }
    }

    private void zombotanyShoot(Zombie zombie) {
        if (!cooldownReady(zombie, 15)) {
            return;
        }
        PlacedPlant target = nearestPlantInRow(zombie);
        if (target == null) {
            return;
        }
        target.setHealth(target.getHealth() - 20);
        if (target.isDead()) {
            session.removePlant(target, true);
        }
    }

    private boolean zombotanyJalapeno(Zombie zombie) {
        if (session.getTickCount() - zombie.getBattle().getSpawnTick()
                < 10 * GameSession.TICKS_PER_SECOND) {
            return false;
        }
        int row = (int) zombie.getPosition().getY();
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.getY() == row) {
                session.removePlant(plant, true);
            }
        }
        System.out.printf("The Jalapeno zombie set lane %d on fire!%n", row);
        combatManager.damageZombie(zombie, 1_000_000);
        return true;
    }

    private boolean zombotanySquash(Zombie zombie) {
        int col = (int) Math.round(zombie.getPosition().getX());
        PlacedPlant blocking = session.plantAt(col, (int) zombie.getPosition().getY());
        if (blocking != null
                && Math.abs(zombie.getPosition().getX() - blocking.getX()) <= 0.4) {
            System.out.printf("The Squash zombie crushed %s and itself!%n",
                    blocking.getType().getName());
            session.removePlant(blocking, true);
            combatManager.damageZombie(zombie, 1_000_000);
            return true;
        }
        return false;
    }

    private PlacedPlant nearestPlantInRow(Zombie zombie) {
        PlacedPlant nearest = null;
        for (PlacedPlant plant : session.getPlants()) {
            if (plant.getY() == (int) zombie.getPosition().getY()
                    && plant.getX() <= zombie.getPosition().getX()
                    && (nearest == null || plant.getX() > nearest.getX())) {
                nearest = plant;
            }
        }
        return nearest;
    }

    public boolean beforeHit(Zombie zombie, PlantType source) {
        if (zombie.getBattle().getIceHealth() > 0) {
            int melt = source.getTags().contains(PlantTag.FIRE) ? 600
                    : Math.max(20, source.getDamage());
            zombie.getBattle().setIceHealth(Math.max(0, zombie.getBattle().getIceHealth() - melt));
            return false;
        }
        if (zombie.getType() == ZombieType.IMP_DRAGON && source.getTags().contains(PlantTag.FIRE)) {
            return false;
        }
        if (zombie.getType() == ZombieType.UMBRELLA
                && source.getCategory() == PlantCategory.LOBBER) {
            return false;
        }
        if (zombie.getType() == ZombieType.SNORKEL && isUnderwater(zombie)
                && source.getCategory() != PlantCategory.LOBBER) {
            return false;
        }
        if (zombie.getType() == ZombieType.EXPLORER) {
            if (source.getTags().contains(PlantTag.ICE)) {
                zombie.getBattle().setTorchLit(false);
            }
            if (source.getTags().contains(PlantTag.FIRE)) {
                zombie.getBattle().setTorchLit(true);
            }
        }
        if (zombie.getType() == ZombieType.PROSPECTOR && source.getTags().contains(PlantTag.ICE)) {
            zombie.getBattle().setDynamiteTicks(-1);
        }
        if (zombie.getType() == ZombieType.WEASEL_HOARDER) {
            releaseWeasel(zombie);
        }
        if (zombie.getType() == ZombieType.JUGGLER) {
            return jugglerDeflect(zombie, source);
        }
        return true;
    }

    private boolean jugglerDeflect(Zombie zombie, PlantType source) {
        zombie.getBattle().setTicksSinceShotAt(0);
        if (source.getCategory() != PlantCategory.SHOOTER) {
            return true;
        }
        if (!zombie.getBattle().isSpinning()) {
            zombie.getBattle().setSpinning(true);
            zombie.setSpeed(zombie.getType().getSpeed() * 2);
        }
        PlacedPlant victim = nearestPlantInRow(zombie);
        if (victim != null) {
            if (source.getTags().contains(PlantTag.ICE)) {
                victim.setFreezeLevel(victim.getFreezeLevel() + 1);
                if (victim.getFreezeLevel() >= 3 && victim.getIceHealth() == 0) {
                    victim.setIceHealth(600);
                }
            }
            victim.setHealth(victim.getHealth() - Math.max(0, source.getDamage()));
            if (victim.isDead()) {
                session.removePlant(victim, true);
            }
        }
        return false;
    }

    private boolean isUnderwater(Zombie zombie) {
        Tile tile = session.getGrid().getTile(
                (int) Math.round(zombie.getPosition().getX()) - 1,
                (int) zombie.getPosition().getY() - 1);
        return tile != null && tile.getTerrain() == TerrainType.WATER;
    }

    public void onZombieDeath(Zombie zombie) {
        int stolen = zombie.getBattle().getStolenSun();
        if (stolen > 0) {
            int returned = zombie.getType() == ZombieType.TURQUOISE ? stolen / 2 : stolen;
            session.getSunManager().addSun(returned);
            System.out.printf("The fallen %s dropped %d stolen sun.%n",
                    zombie.getType().getName(), returned);
        }
        for (PlacedPlant sheep : zombie.getBattle().getSheepPlants()) {
            sheep.setSheep(false);
        }
        if (zombie.getType() == ZombieType.BARREL_ROLLER) {
            for (PushedObject pushed : session.getPushedObjects()) {
                if (pushed.getKind() == PushedObject.Kind.BARREL
                        && pushed.getRow() == (int) zombie.getPosition().getY()) {
                    pushed.setMoving(false);
                }
            }
        }
        pushersWithObject.remove(zombie);
    }

    public void tickEnvironment() {
        movePushedObjects();
        applySliders();
    }

    private void movePushedObjects() {
        for (PushedObject pushed : new ArrayList<>(session.getPushedObjects())) {
            if (pushed.isDestroyed()) {
                session.getPushedObjects().remove(pushed);
                if (pushed.getKind() == PushedObject.Kind.BARREL) {
                    session.spawnZombie(ZombieType.IMP, pushed.getX(), pushed.getRow(),
                            Math.max(1, session.getWaveManager().getCurrentWave()));
                    session.spawnZombie(ZombieType.IMP, pushed.getX(), pushed.getRow(),
                            Math.max(1, session.getWaveManager().getCurrentWave()));
                    System.out.printf("The barrel broke and two Imps jumped out in lane %d!%n",
                            pushed.getRow());
                }
                continue;
            }
            if (!pushed.isMoving()) {
                continue;
            }
            pushed.setX(pushed.getX() - 0.0185);
            PlacedPlant crushed = session.plantAt((int) Math.round(pushed.getX()), pushed.getRow());
            if (crushed != null) {
                System.out.printf("The rolling %s crushed %s at (%d, %d)!%n",
                        pushed.getKind().name().toLowerCase().replace('_', ' '),
                        crushed.getType().getName(), crushed.getX(), crushed.getY());
                session.removePlant(crushed, true);
            }
            if (pushed.getX() < 0.5) {
                session.getPushedObjects().remove(pushed);
            }
        }
    }

    private void applySliders() {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getType() == ZombieType.DODO) {
                continue;
            }
            int col = (int) Math.round(zombie.getPosition().getX());
            int row = (int) zombie.getPosition().getY();
            Tile tile = session.getGrid().getTile(col - 1, row - 1);
            if (tile == null || zombie.getBattle().getLastSliderColumn() == col) {
                continue;
            }
            if (tile.getTerrain() == TerrainType.SLIDER_UP && row > 1) {
                zombie.getPosition().setY(row - 1);
                zombie.getBattle().setLastSliderColumn(col);
                System.out.printf("A slider pushed the %s up to lane %d.%n",
                        zombie.getType().getName(), row - 1);
            } else if (tile.getTerrain() == TerrainType.SLIDER_DOWN && row < GameSession.ROWS) {
                zombie.getPosition().setY(row + 1);
                zombie.getBattle().setLastSliderColumn(col);
                System.out.printf("A slider pushed the %s down to lane %d.%n",
                        zombie.getType().getName(), row + 1);
            }
        }
    }

    public void onWaveStart(int waveNumber) {
        Chapter chapter = chapter();
        if (chapter instanceof FrostBite) {
            iceWind();
        } else if (chapter instanceof WaveyBeach) {
            shiftTide();
        } else if (chapter instanceof DarkAges) {
            darkAgesGraves();
            necromancy();
        }
    }

    public void afterWaveSpawn(int waveNumber) {
        Chapter chapter = chapter();
        if (chapter instanceof Egypt && waveNumber == session.getWaveManager().getTotalWaves()) {
            for (Zombie zombie : session.getZombies()) {
                if (zombie.getSpawnWave() == waveNumber && session.getRandom().nextInt(100) < 40) {
                    int jump = 1 + session.getRandom().nextInt(4);
                    zombie.getPosition().setX(Math.max(2, zombie.getPosition().getX() - jump));
                    System.out.printf("A whirlwind carried the %s %d columns ahead!%n",
                            zombie.getType().getName(), jump);
                }
            }
        }
    }

    private void iceWind() {
        if (session.getRandom().nextInt(100) >= 40) {
            return;
        }
        int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
        System.out.printf("An icy wind sweeps through lane %d!%n", row);
        for (PlacedPlant plant : session.getPlants()) {
            if (plant.getY() != row || plant.getType().getTags().contains(PlantTag.FIRE)) {
                continue;
            }
            plant.setFreezeLevel(plant.getFreezeLevel() + 1);
            if (plant.getFreezeLevel() >= 3 && plant.getIceHealth() == 0) {
                plant.setIceHealth(600);
                System.out.printf("%s at (%d, %d) froze solid!%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
            }
        }
    }

    private void shiftTide() {
        int water = 2 + session.getRandom().nextInt(3);
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                Tile tile = session.getGrid().getTile(col - 1, row - 1);
                if (col > GameSession.COLS - water) {
                    if (tile.getTerrain() == TerrainType.NORMAL) {
                        tile.setTerrain(TerrainType.WATER);
                    }
                } else if (tile.getTerrain() == TerrainType.WATER) {
                    tile.setTerrain(TerrainType.NORMAL);
                    tile.setHasLilyPad(false);
                }
            }
        }
        System.out.printf("The tide shifted; the last %d columns are underwater.%n", water);
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            Tile tile = session.getGrid().getTile(plant.getX() - 1, plant.getY() - 1);
            if (tile.getTerrain() == TerrainType.WATER && !tile.isHasLilyPad()
                    && !plant.getType().getTags().contains(PlantTag.WATER)) {
                System.out.printf("%s at (%d, %d) was swept away by the tide!%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
                session.removePlant(plant, false);
            }
        }
    }

    private void darkAgesGraves() {
        int count = 1 + session.getRandom().nextInt(2);
        for (int i = 0; i < count; i++) {
            int col = 2 + session.getRandom().nextInt(GameSession.COLS - 2);
            int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
            Tile tile = session.getGrid().getTile(col - 1, row - 1);
            if (tile.getTerrain() != TerrainType.NORMAL || session.plantAt(col, row) != null) {
                continue;
            }
            tile.setTerrain(TerrainType.GRAVE);
            if (session.getRandom().nextInt(100) < 30) {
                if (session.getRandom().nextBoolean()) {
                    session.getSunManager().addSun(50);
                    System.out.printf("A grave rose at (%d, %d) carrying 50 sun!%n", col, row);
                } else {
                    session.setPlantFoods(session.getPlantFoods() + 1);
                    System.out.printf("A grave rose at (%d, %d) carrying a plant food!%n", col, row);
                }
            } else {
                System.out.printf("A grave rose at (%d, %d).%n", col, row);
            }
        }
    }

    private void necromancy() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                Tile tile = session.getGrid().getTile(col - 1, row - 1);
                if (tile.getTerrain() == TerrainType.GRAVE
                        && session.getRandom().nextInt(100) < 25) {
                    session.spawnZombie(ZombieType.NORMAL, col, row,
                            Math.max(1, session.getWaveManager().getCurrentWave()));
                    System.out.printf("Necromancy! A zombie crawled out from the grave at (%d, %d).%n",
                            col, row);
                }
            }
        }
    }
}
