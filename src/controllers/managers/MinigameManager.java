package controllers.managers;

import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameMode;
import models.game.GamePhase;
import models.game.GameSession;
import models.game.Names;
import models.game.PlacedPlant;
import models.game.PlantSlot;
import models.game.RollingNut;
import models.game.Vase;
import models.progress.level.special.SpecialLevelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MinigameManager {
    private static final int BELT_INTERVAL_TICKS = 12 * GameSession.TICKS_PER_SECOND;
    private static final int PACKET_LIFETIME_TICKS = 30 * GameSession.TICKS_PER_SECOND;
    private static final List<PlantType> BOWLING_NUTS = List.of(
            PlantType.WALL_NUT, PlantType.EXPLODE_O_NUT, PlantType.TALL_NUT);
    private static final List<PlantType> BEGHOULED_TYPES = List.of(
            PlantType.PEASHOOTER, PlantType.SNOW_PEA, PlantType.WALL_NUT,
            PlantType.PUFF_SHROOM, PlantType.CABBAGE_PULT);
    private static final List<ZombieType> I_ZOMBIE_ROSTER = List.of(
            ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD,
            ZombieType.IMP, ZombieType.NEWSPAPER, ZombieType.ALLSTAR,
            ZombieType.BRICK_HEAD, ZombieType.PROSPECTOR, ZombieType.DODO,
            ZombieType.KNIGHT);

    private final GameSession session;
    private final int tier;
    private final List<Vase> vases = new ArrayList<>();
    private final List<RollingNut> nuts = new ArrayList<>();
    private final Map<RollingNut, Integer> nutDirections = new HashMap<>();
    private final Map<PlantSlot, Integer> packetExpiry = new HashMap<>();
    private final List<ZombieType> izombieTypes = new ArrayList<>();
    private final Set<Long> craters = new HashSet<>();
    private int combosMade;
    private int combosNeeded;
    private int beltTicks;
    private int spawnTicks;

    public MinigameManager(GameSession session, int tier) {
        this.session = session;
        this.tier = Math.max(1, tier);
    }

    public boolean startsImmediately() {
        GameMode mode = session.getMode();
        return mode == GameMode.VASEBREAKER || mode == GameMode.WALLNUT_BOWLING
                || mode == GameMode.I_ZOMBIE || mode == GameMode.BEGHOULED
                || session.isSpecial(SpecialLevelType.CONVEYOR_BELT);
    }

    public void setUpBoard() {
        switch (session.getMode()) {
            case VASEBREAKER:
                buildVases();
                break;
            case WALLNUT_BOWLING:
                addBeltSlot(randomNutType());
                break;
            case I_ZOMBIE:
                setUpIZombie();
                break;
            case BEGHOULED:
                setUpBeghouled();
                break;
            default:
                if (session.isSpecial(SpecialLevelType.CONVEYOR_BELT)) {
                    addBeltSlot(randomUnlockedPlant());
                }
                break;
        }
    }

    private void buildVases() {
        List<int[]> spots = new ArrayList<>();
        for (int col = 5; col <= GameSession.COLS; col++) {
            for (int row = 1; row <= GameSession.ROWS; row++) {
                spots.add(new int[] { col, row });
            }
        }
        Collections.shuffle(spots, session.getRandom());
        int count = Math.min(spots.size(), 10 + 2 * tier);
        for (int i = 0; i < count; i++) {
            int[] spot = spots.get(i);
            vases.add(createVase(i, spot[0], spot[1]));
        }
    }

    private Vase createVase(int index, int x, int y) {
        if (index == 0) {
            return new Vase(x, y, Vase.VaseKind.GHOUL, ZombieType.GARGANTUAR, null);
        }
        if (index == 1) {
            return new Vase(x, y, Vase.VaseKind.PLANT, null, randomUnlockedPlant());
        }
        int roll = session.getRandom().nextInt(100);
        if (roll < 55) {
            ZombieType[] pool = { ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD };
            return new Vase(x, y, Vase.VaseKind.ORDINARY,
                    pool[session.getRandom().nextInt(pool.length)], null);
        }
        if (roll < 80) {
            return new Vase(x, y, Vase.VaseKind.ORDINARY, null, randomUnlockedPlant());
        }
        return new Vase(x, y, Vase.VaseKind.ORDINARY, null, null);
    }

    private void setUpIZombie() {
        int start = ((tier - 1) * 3) % I_ZOMBIE_ROSTER.size();
        for (int i = 0; i < 5; i++) {
            izombieTypes.add(I_ZOMBIE_ROSTER.get((start + i) % I_ZOMBIE_ROSTER.size()));
        }
        List<PlantType> defenders = List.of(PlantType.PEASHOOTER, PlantType.SNOW_PEA,
                PlantType.WALL_NUT, PlantType.CABBAGE_PULT, PlantType.BONK_CHOY,
                PlantType.POTATO_MINE);
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= 3; col++) {
                if (session.getRandom().nextInt(100) < 60) {
                    PlantType type = defenders.get(session.getRandom().nextInt(defenders.size()));
                    session.getPlants().add(new PlacedPlant(type, col, row, type.getBaseHp()));
                }
            }
            Zombie producer = session.spawnZombie(ZombieType.NORMAL, GameSession.COLS, row, 0);
            producer.setHealth(1100);
            producer.getBattle().setSunProducer(true);
        }
    }

    private void setUpBeghouled() {
        combosNeeded = 6 + 2 * tier;
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                PlantType type = BEGHOULED_TYPES.get(
                        session.getRandom().nextInt(BEGHOULED_TYPES.size()));
                session.getPlants().add(new PlacedPlant(type, col, row, type.getBaseHp()));
            }
        }
    }

    public void tick() {
        tickBelt();
        tickPackets();
        switch (session.getMode()) {
            case VASEBREAKER:
                if (session.getPhase() == GamePhase.BATTLE && vases.isEmpty()
                        && session.getZombies().isEmpty()) {
                    session.winGame();
                }
                break;
            case WALLNUT_BOWLING:
                moveNuts();
                break;
            case I_ZOMBIE:
                checkIZombieDefeat();
                break;
            case BEGHOULED:
                spawnBeghouledZombie();
                break;
            default:
                break;
        }
    }

    private void tickBelt() {
        boolean hasBelt = session.getMode() == GameMode.WALLNUT_BOWLING
                || session.isSpecial(SpecialLevelType.CONVEYOR_BELT);
        if (!hasBelt || session.getPhase() != GamePhase.BATTLE) {
            return;
        }
        beltTicks++;
        if (beltTicks >= BELT_INTERVAL_TICKS && session.getSlots().size() < 8) {
            beltTicks = 0;
            addBeltSlot(session.getMode() == GameMode.WALLNUT_BOWLING
                    ? randomNutType() : randomUnlockedPlant());
        }
    }

    private void addBeltSlot(PlantType type) {
        if (type == null) {
            return;
        }
        PlantSlot slot = new PlantSlot(type);
        slot.setSingleUse(true);
        session.getSlots().add(slot);
        System.out.printf("The conveyor belt delivered a %s.%n", type.getName());
    }

    private PlantType randomNutType() {
        return BOWLING_NUTS.get(session.getRandom().nextInt(BOWLING_NUTS.size()));
    }

    private PlantType randomUnlockedPlant() {
        List<String> unlocked = session.getSelection().getUnlockedPlantNames();
        if (unlocked.isEmpty()) {
            return PlantType.PEASHOOTER;
        }
        PlantType type = Names.plant(unlocked.get(session.getRandom().nextInt(unlocked.size())));
        return type == null ? PlantType.PEASHOOTER : type;
    }

    private void tickPackets() {
        for (Map.Entry<PlantSlot, Integer> entry : new HashMap<>(packetExpiry).entrySet()) {
            int left = entry.getValue() - 1;
            if (!session.getSlots().contains(entry.getKey())) {
                packetExpiry.remove(entry.getKey());
            } else if (left <= 0) {
                session.getSlots().remove(entry.getKey());
                packetExpiry.remove(entry.getKey());
                System.out.printf("The %s seed packet on the ground faded away.%n",
                        entry.getKey().getType().getName());
            } else {
                packetExpiry.put(entry.getKey(), left);
            }
        }
    }

    private void moveNuts() {
        for (RollingNut nut : new ArrayList<>(nuts)) {
            nut.setX(nut.getX() + 0.3);
            if (nut.getX() > GameSession.COLS + 0.5) {
                nuts.remove(nut);
                nutDirections.remove(nut);
                continue;
            }
            Zombie hit = firstZombieNear(nut);
            if (hit == null) {
                continue;
            }
            if (nut.isExplosive()) {
                explodeNut(nut, hit);
            } else if (nut.isGiant()) {
                session.getCombatManager().damageZombie(hit, 1_000_000);
            } else {
                session.getCombatManager().damageZombie(hit, 190);
                bounce(nut);
            }
        }
    }

    private Zombie firstZombieNear(RollingNut nut) {
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() == nut.getRow()
                    && Math.abs(zombie.getPosition().getX() - nut.getX()) <= 0.5) {
                return zombie;
            }
        }
        return null;
    }

    private void explodeNut(RollingNut nut, Zombie center) {
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            if (Math.abs(zombie.getPosition().getX() - center.getPosition().getX()) <= 1.5
                    && Math.abs(zombie.getPosition().getY() - center.getPosition().getY()) <= 1) {
                session.getCombatManager().damageZombie(zombie, 1800);
            }
        }
        nuts.remove(nut);
        nutDirections.remove(nut);
        System.out.printf("The explosive nut blew up in lane %d!%n", nut.getRow());
    }

    private void bounce(RollingNut nut) {
        int direction = nutDirections.getOrDefault(nut,
                session.getRandom().nextBoolean() ? 1 : -1);
        int next = nut.getRow() + direction;
        if (next < 1 || next > GameSession.ROWS) {
            direction = -direction;
            next = nut.getRow() + direction;
        }
        nut.setRow(next);
        nutDirections.put(nut, -direction);
    }

    public Result placeBowlingNut(PlantType type, int x, int y) {
        if (!BOWLING_NUTS.contains(type)) {
            return Result.fail("Only bowling nuts can be planted in this minigame.");
        }
        if (x > 3) {
            return Result.fail("You may only bowl from behind the red line (columns 1-3).");
        }
        PlantSlot slot = session.findSlot(type);
        if (slot == null) {
            return Result.fail("The belt has not delivered a " + type.getName() + " yet.");
        }
        session.getSlots().remove(slot);
        RollingNut nut = new RollingNut(type, x, y);
        nuts.add(nut);
        return Result.ok(type.getName() + " is rolling down lane " + y + "!");
    }

    private void checkIZombieDefeat() {
        if (session.getPhase() != GamePhase.BATTLE) {
            return;
        }
        boolean attackers = false;
        boolean producers = false;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getBattle().isSunProducer()) {
                producers = true;
            } else {
                attackers = true;
            }
        }
        int cheapest = Integer.MAX_VALUE;
        for (ZombieType type : izombieTypes) {
            cheapest = Math.min(cheapest, type.getWaveCost());
        }
        if (!attackers && !producers && session.getSunManager().getSunBalance() < cheapest) {
            session.loseGame("No zombies left and not enough sun for more. The plants win!");
        }
    }

    private void spawnBeghouledZombie() {
        spawnTicks++;
        if (spawnTicks < 150) {
            return;
        }
        spawnTicks = 0;
        ZombieType[] pool = { ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD };
        int lane = 1 + session.getRandom().nextInt(GameSession.ROWS);
        ZombieType type = pool[session.getRandom().nextInt(pool.length)];
        session.spawnZombie(type, GameSession.COLS, lane, 1);
        System.out.printf("Zombie %s shambles into lane %d.%n", type.getName(), lane);
    }

    public Result breakVase(int x, int y) {
        if (session.getMode() != GameMode.VASEBREAKER) {
            return Result.fail("There are no vases in this mode.");
        }
        Vase vase = null;
        for (Vase candidate : vases) {
            if (candidate.getX() == x && candidate.getY() == y) {
                vase = candidate;
                break;
            }
        }
        if (vase == null) {
            return Result.fail("There is no vase at (" + x + ", " + y + ").");
        }
        vases.remove(vase);
        if (vase.getZombie() != null) {
            session.spawnZombie(vase.getZombie(), x, y, 1);
            return Result.ok("A " + vase.getZombie().getName()
                    + " was hiding in the vase at (" + x + ", " + y + ")!");
        }
        if (vase.getPacket() != null) {
            PlantSlot slot = new PlantSlot(vase.getPacket());
            slot.setSingleUse(true);
            session.getSlots().add(slot);
            packetExpiry.put(slot, PACKET_LIFETIME_TICKS);
            return Result.ok("The vase dropped a " + vase.getPacket().getName()
                    + " seed packet; plant it before it fades!");
        }
        return Result.ok("The vase at (" + x + ", " + y + ") was empty.");
    }

    public Result placeZombie(String typeName, int x, int y) {
        if (session.getMode() != GameMode.I_ZOMBIE) {
            return Result.fail("You can only place zombies in the I, Zombie minigame.");
        }
        ZombieType type = Names.zombie(typeName);
        if (type == null || !izombieTypes.contains(type)) {
            return Result.fail("Available zombies: " + rosterNames());
        }
        if (x < 6 || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            return Result.fail("Zombies must be placed right of the red line (columns 6-9).");
        }
        if (!session.getSunManager().spendSun(type.getWaveCost())) {
            return Result.fail(type.getName() + " costs " + type.getWaveCost()
                    + " sun and you have " + session.getSunManager().getSunBalance() + ".");
        }
        session.spawnZombie(type, x, y, 1);
        return Result.ok(type.getName() + " placed at (" + x + ", " + y + ")."
                + " Sun left: " + session.getSunManager().getSunBalance());
    }

    private String rosterNames() {
        List<String> names = new ArrayList<>();
        for (ZombieType type : izombieTypes) {
            names.add(type.getName() + " (" + type.getWaveCost() + " sun)");
        }
        return String.join(", ", names);
    }

    public Result swap(int x1, int y1, int x2, int y2) {
        if (session.getMode() != GameMode.BEGHOULED) {
            return Result.fail("Swapping plants only works in Beghouled.");
        }
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) {
            return Result.fail("You can only swap two adjacent plants.");
        }
        PlacedPlant first = session.plantAt(x1, y1);
        PlacedPlant second = session.plantAt(x2, y2);
        if (first == null || second == null) {
            return Result.fail("Both tiles must hold a plant (craters cannot be swapped).");
        }
        swapPositions(first, second);
        if (!hasMatch()) {
            swapPositions(first, second);
            return Result.fail("That swap would not create a 3-in-a-row match.");
        }
        processMatches();
        Result result = Result.ok("Match! Combos so far: " + combosMade + "/" + combosNeeded
                + " | Sun: " + session.getSunManager().getSunBalance());
        if (combosMade >= combosNeeded) {
            for (Zombie zombie : new ArrayList<>(session.getZombies())) {
                session.getCombatManager().damageZombie(zombie, 1_000_000);
            }
            session.winGame();
        }
        return result;
    }

    private void swapPositions(PlacedPlant a, PlacedPlant b) {
        int x = a.getX();
        int y = a.getY();
        a.setX(b.getX());
        a.setY(b.getY());
        b.setX(x);
        b.setY(y);
    }

    private boolean hasMatch() {
        return !findMatchedPlants().isEmpty();
    }

    private Set<PlacedPlant> findMatchedPlants() {
        Set<PlacedPlant> matched = new HashSet<>();
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS - 2; col++) {
                collectRun(matched, col, row, 1, 0);
            }
        }
        for (int col = 1; col <= GameSession.COLS; col++) {
            for (int row = 1; row <= GameSession.ROWS - 2; row++) {
                collectRun(matched, col, row, 0, 1);
            }
        }
        return matched;
    }

    private void collectRun(Set<PlacedPlant> matched, int x, int y, int dx, int dy) {
        PlacedPlant a = session.plantAt(x, y);
        PlacedPlant b = session.plantAt(x + dx, y + dy);
        PlacedPlant c = session.plantAt(x + 2 * dx, y + 2 * dy);
        if (a != null && b != null && c != null
                && a.getType() == b.getType() && b.getType() == c.getType()) {
            matched.add(a);
            matched.add(b);
            matched.add(c);
        }
    }

    private void processMatches() {
        int cascade = 0;
        while (cascade < 10) {
            Set<PlacedPlant> matched = findMatchedPlants();
            if (matched.isEmpty()) {
                break;
            }
            combosMade++;
            int suns = 50 * Math.max(1, matched.size() - 2) + 50 * cascade;
            session.getSunManager().addSun(suns);
            for (PlacedPlant plant : matched) {
                session.removePlant(plant, false);
            }
            refillBoard();
            cascade++;
        }
    }

    private void refillBoard() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                if (session.plantAt(col, row) == null && !craters.contains(key(col, row))) {
                    PlantType type = BEGHOULED_TYPES.get(
                            session.getRandom().nextInt(BEGHOULED_TYPES.size()));
                    session.getPlants().add(new PlacedPlant(type, col, row, type.getBaseHp()));
                }
            }
        }
    }

    public Result beghouledUpgrade(String typeName) {
        if (session.getMode() != GameMode.BEGHOULED) {
            return Result.fail("Upgrades only work in Beghouled.");
        }
        PlantType source = Names.plant(typeName);
        PlantType target = upgradeTarget(source);
        if (source == null || target == null) {
            return Result.fail("No upgrade exists for this plant.");
        }
        int cost = upgradeCost(source);
        if (!session.getSunManager().spendSun(cost)) {
            return Result.fail("This upgrade costs " + cost + " sun and you have "
                    + session.getSunManager().getSunBalance() + ".");
        }
        int upgraded = 0;
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.getType() == source) {
                int x = plant.getX();
                int y = plant.getY();
                session.removePlant(plant, false);
                session.getPlants().add(new PlacedPlant(target, x, y, target.getBaseHp()));
                upgraded++;
            }
        }
        return Result.ok("Upgraded " + upgraded + " " + source.getName()
                + " into " + target.getName() + ".");
    }

    private PlantType upgradeTarget(PlantType source) {
        if (source == null) {
            return null;
        }
        switch (source) {
            case PEASHOOTER:
                return PlantType.REPEATER;
            case REPEATER:
                return PlantType.MEGA_GATLING_PEA;
            case WALL_NUT:
                return PlantType.TALL_NUT;
            case PUFF_SHROOM:
                return PlantType.FUME_SHROOM;
            case CABBAGE_PULT:
                return PlantType.MELON_PULT;
            case MELON_PULT:
                return PlantType.WINTER_MELON;
            default:
                return null;
        }
    }

    private int upgradeCost(PlantType source) {
        switch (source) {
            case PEASHOOTER:
                return 500;
            case REPEATER:
                return 1500;
            case WALL_NUT:
                return 500;
            case PUFF_SHROOM:
                return 250;
            case CABBAGE_PULT:
                return 1000;
            case MELON_PULT:
                return 750;
            default:
                return 0;
        }
    }

    public void onPlantEaten(PlacedPlant plant) {
        if (session.getMode() == GameMode.BEGHOULED) {
            craters.add(key(plant.getX(), plant.getY()));
            System.out.printf("A crater opened at (%d, %d); nothing grows there anymore.%n",
                    plant.getX(), plant.getY());
        }
    }

    private long key(int x, int y) {
        return x * 100L + y;
    }

    public List<Vase> getVases() {
        return vases;
    }
}
