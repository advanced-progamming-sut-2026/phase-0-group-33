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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MinigameManager {
    private static final int BELT_INTERVAL_TICKS = 12 * GameSession.TICKS_PER_SECOND;
    private static final int BOSS_BELT_INTERVAL_TICKS = 4 * GameSession.TICKS_PER_SECOND;
    private static final int BOWLING_BELT_INTERVAL_TICKS = 7 * GameSession.TICKS_PER_SECOND;
    private static final int BOWLING_BELT_SLOTS = 5;
    private static final int PACKET_LIFETIME_TICKS = 30 * GameSession.TICKS_PER_SECOND;
    private static final double NUT_SPEED = 0.18;
    private static final List<PlantType> BOWLING_NUTS = List.of(
            PlantType.WALL_NUT, PlantType.EXPLODE_O_NUT, PlantType.TALL_NUT);
    private static final int VASE_COOLDOWN_TICKS = 8;
    private static final List<PlantType> BEGHOULED_TYPES = List.of(
            PlantType.PEASHOOTER, PlantType.SNOW_PEA, PlantType.WALL_NUT,
            PlantType.PUFF_SHROOM, PlantType.CABBAGE_PULT);
    private static final List<ZombieType> I_ZOMBIE_ROSTER = List.of(
            ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD,
            ZombieType.IMP, ZombieType.NEWSPAPER, ZombieType.ALLSTAR,
            ZombieType.BRICK_HEAD, ZombieType.PROSPECTOR, ZombieType.DODO,
            ZombieType.KNIGHT);

    private static final List<ZombieType> DUEL_ROSTER = List.of(
            ZombieType.NORMAL, ZombieType.IMP, ZombieType.CONE_HEAD,
            ZombieType.PROSPECTOR, ZombieType.BUCKET_HEAD);

    private final GameSession session;
    private final int tier;
    private final List<Vase> vases = new ArrayList<>();
    private final List<RollingNut> nuts = new ArrayList<>();
    private final Map<RollingNut, Integer> nutDirections = new HashMap<>();
    private final Map<PlantSlot, Integer> packetExpiry = new HashMap<>();
    private final List<ZombieType> izombieTypes = new ArrayList<>();
    private final Map<ZombieType, Integer> zombieCooldowns = new HashMap<>();
    private final Set<Long> craters = new HashSet<>();
    private int combosMade;
    private int combosNeeded;
    private final VaseDeck vaseDeck;
    private int beltTicks;
    private int spawnTicks;
    private int vaseCooldown;

    public MinigameManager(GameSession session, int tier) {
        this.session = session;
        this.tier = Math.max(1, tier);
        this.vaseDeck = new VaseDeck(session);
    }

    public boolean startsImmediately() {
        if (session.isDuel()) {
            return false;
        }
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
                for (int i = 0; i < BOWLING_BELT_SLOTS; i++) {
                    addBeltSlot(randomNutType());
                }
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
        vases.addAll(vaseDeck.board());
    }

    private void setUpIZombie() {
        if (session.isDuel()) {
            izombieTypes.addAll(DUEL_ROSTER);
            return;
        }
        int start = ((tier - 1) * 3) % I_ZOMBIE_ROSTER.size();
        for (int i = 0; i < 5; i++) {
            izombieTypes.add(I_ZOMBIE_ROSTER.get((start + i) % I_ZOMBIE_ROSTER.size()));
        }
        List<PlantType> defenders = List.of(PlantType.PEASHOOTER, PlantType.SNOW_PEA,
                PlantType.WALL_NUT, PlantType.CABBAGE_PULT, PlantType.BONK_CHOY,
                PlantType.POTATO_MINE);
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= 5; col++) {
                PlantType type = defenders.get(session.getRandom().nextInt(defenders.size()));
                session.getPlants().add(new PlacedPlant(type, col, row, type.getBaseHp()));
            }
            Zombie producer = session.spawnZombie(ZombieType.BUCKET_HEAD, GameSession.COLS, row, 0);
            producer.getBattle().setSunProducer(true);
        }
    }

    private void setUpBeghouled() {
        combosNeeded = 20 + 10 * tier;
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                PlantType type = BEGHOULED_TYPES.get(
                        session.getRandom().nextInt(BEGHOULED_TYPES.size()));
                session.getPlants().add(new PlacedPlant(type, col, row, type.getBaseHp()));
            }
        }
    }

    public void tick() {
        if (vaseCooldown > 0) {
            vaseCooldown--;
        }
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
                tickZombieCooldowns();
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
        boolean bowling = session.getMode() == GameMode.WALLNUT_BOWLING;
        int interval = BELT_INTERVAL_TICKS;
        if (session.isBossLevel()) {
            interval = BOSS_BELT_INTERVAL_TICKS;
        } else if (bowling) {
            interval = BOWLING_BELT_INTERVAL_TICKS;
        }
        if (beltTicks >= interval && session.getSlots().size() < 8) {
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
            nut.setX(nut.getX() + NUT_SPEED);
            driftNut(nut);
            if (nut.getX() > GameSession.COLS + 0.5) {
                nuts.remove(nut);
                nutDirections.remove(nut);
                continue;
            }
            Zombie hit = firstZombieNear(nut);
            if (hit == null || !nut.recordHit(hit)) {
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
        int direction = nutDirections.getOrDefault(nut, 0);
        if (direction == 0) {
            direction = session.getRandom().nextBoolean() ? 1 : -1;
        } else {
            direction = -direction;
        }
        int next = nut.getRow() + direction;
        if (next < 1 || next > GameSession.ROWS) {
            direction = -direction;
        }
        nutDirections.put(nut, direction);
        System.out.printf("The nut ricocheted towards lane %d.%n",
                Math.max(1, Math.min(GameSession.ROWS, nut.getRow() + direction)));
    }

    private void driftNut(RollingNut nut) {
        int direction = nutDirections.getOrDefault(nut, 0);
        if (direction == 0) {
            return;
        }
        double lane = nut.getLane() + direction * NUT_SPEED;
        if (lane < 1 || lane > GameSession.ROWS) {
            direction = -direction;
            nutDirections.put(nut, direction);
            lane = Math.max(1, Math.min(GameSession.ROWS, lane));
            System.out.printf("The nut hit the edge of the lawn and turned into lane %d.%n",
                    (int) Math.round(lane) + direction);
        }
        nut.setLane(lane);
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
        boolean brains = false;
        for (int row = 1; row <= GameSession.ROWS; row++) {
            brains |= session.hasBrain(row);
        }
        if (!brains) {
            session.winGame();
            return;
        }
        if (session.isDuel()) {
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
        if (vaseCooldown > 0) {
            return Result.fail("Steady on - let the dust settle before the next vase.");
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
        vaseCooldown = VASE_COOLDOWN_TICKS;
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

    private void tickZombieCooldowns() {
        for (Map.Entry<ZombieType, Integer> entry : zombieCooldowns.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }
    }

    public int zombieRechargeTicks(ZombieType type) {
        if (session.isDuel()) {
            return Math.max(3, type.getWaveCost() / 40) * GameSession.TICKS_PER_SECOND;
        }
        return Math.max(5, type.getWaveCost() / 4) * GameSession.TICKS_PER_SECOND;
    }

    public int zombieCooldown(ZombieType type) {
        return zombieCooldowns.getOrDefault(type, 0);
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
        if (session.plantAt(x, y) != null) {
            return Result.fail("A plant already stands on that tile.");
        }
        if (zombieCooldown(type) > 0) {
            return Result.fail(type.getName() + " is still recharging ("
                    + (zombieCooldown(type) / GameSession.TICKS_PER_SECOND + 1) + "s left).");
        }
        if (!session.getSunManager().spendSun(type.getWaveCost())) {
            return Result.fail(type.getName() + " costs " + type.getWaveCost()
                    + " sun and you have " + session.getSunManager().getSunBalance() + ".");
        }
        zombieCooldowns.put(type, zombieRechargeTicks(type));
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
            return result;
        }
        if (!possibleMoveExists()) {
            resetBoard();
            result.addMessage("No more possible matches; the garden was reshuffled!");
        }
        return result;
    }

    private boolean possibleMoveExists() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                if (trySwapCreatesMatch(col, row, col + 1, row)
                        || trySwapCreatesMatch(col, row, col, row + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean trySwapCreatesMatch(int x1, int y1, int x2, int y2) {
        PlacedPlant first = session.plantAt(x1, y1);
        PlacedPlant second = session.plantAt(x2, y2);
        if (first == null || second == null) {
            return false;
        }
        swapPositions(first, second);
        boolean match = hasMatch();
        swapPositions(first, second);
        return match;
    }

    private void resetBoard() {
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            int x = plant.getX();
            int y = plant.getY();
            session.removePlant(plant, false);
            PlantType type = BEGHOULED_TYPES.get(
                    session.getRandom().nextInt(BEGHOULED_TYPES.size()));
            session.getPlants().add(new PlacedPlant(type, x, y, type.getBaseHp()));
        }
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
        return !findMatchedLines().isEmpty();
    }

    private List<List<PlacedPlant>> findMatchedLines() {
        List<List<PlacedPlant>> lines = new ArrayList<>();
        for (int row = 1; row <= GameSession.ROWS; row++) {
            collectLines(lines, 1, row, 1, 0, GameSession.COLS);
        }
        for (int col = 1; col <= GameSession.COLS; col++) {
            collectLines(lines, col, 1, 0, 1, GameSession.ROWS);
        }
        return lines;
    }

    private void collectLines(List<List<PlacedPlant>> lines, int x, int y,
                              int dx, int dy, int length) {
        int index = 0;
        while (index < length) {
            PlacedPlant first = session.plantAt(x + index * dx, y + index * dy);
            int run = 1;
            if (first != null) {
                while (index + run < length) {
                    PlacedPlant next = session.plantAt(
                            x + (index + run) * dx, y + (index + run) * dy);
                    if (next == null || next.getType() != first.getType()) {
                        break;
                    }
                    run++;
                }
            }
            if (first != null && run >= 3) {
                List<PlacedPlant> line = new ArrayList<>();
                for (int step = 0; step < run; step++) {
                    line.add(session.plantAt(
                            x + (index + step) * dx, y + (index + step) * dy));
                }
                lines.add(line);
            }
            index += Math.max(1, run);
        }
    }

    private void processMatches() {
        int cascade = 0;
        while (cascade < 10) {
            List<List<PlacedPlant>> lines = findMatchedLines();
            if (lines.isEmpty()) {
                break;
            }
            Set<PlacedPlant> cleared = new HashSet<>();
            for (List<PlacedPlant> line : lines) {
                combosMade++;
                int suns = 50 * (line.size() - 2) + 50 * cascade;
                session.getSunManager().addSun(suns);
                System.out.printf("A line of %d %s scored %d sun.%n",
                        line.size(), line.get(0).getType().getName(), suns);
                cleared.addAll(line);
            }
            for (PlacedPlant plant : cleared) {
                session.removePlant(plant, false);
            }
            refillBoard();
            cascade++;
        }
    }

    private void refillBoard() {
        for (int col = 1; col <= GameSession.COLS; col++) {
            collapseColumn(col);
        }
    }

    private void collapseColumn(int col) {
        List<Integer> openRows = new ArrayList<>();
        List<PlacedPlant> survivors = new ArrayList<>();
        for (int row = GameSession.ROWS; row >= 1; row--) {
            if (!craters.contains(key(col, row))) {
                openRows.add(row);
            }
            PlacedPlant plant = session.plantAt(col, row);
            if (plant != null) {
                survivors.add(plant);
            }
        }
        int taken = 0;
        for (int row : openRows) {
            if (taken < survivors.size()) {
                survivors.get(taken).setY(row);
                taken++;
            } else {
                PlantType type = BEGHOULED_TYPES.get(
                        session.getRandom().nextInt(BEGHOULED_TYPES.size()));
                session.getPlants().add(new PlacedPlant(type, col, row, type.getBaseHp()));
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
        List<PlacedPlant> targets = new ArrayList<>();
        for (PlacedPlant plant : session.getPlants()) {
            if (plant.getType() == source) {
                targets.add(plant);
            }
        }
        if (targets.isEmpty()) {
            return Result.fail("There is no " + source.getName() + " on the board to upgrade.");
        }
        int cost = upgradeCost(source);
        if (!session.getSunManager().spendSun(cost)) {
            return Result.fail("This upgrade costs " + cost + " sun and you have "
                    + session.getSunManager().getSunBalance() + ".");
        }
        int upgraded = 0;
        for (PlacedPlant plant : targets) {
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

    public int upgradeCostOf(PlantType source) {
        return upgradeCost(source);
    }

    public PlantType upgradeTargetOf(PlantType source) {
        return upgradeTarget(source);
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

    public List<RollingNut> getNuts() {
        return nuts;
    }

    public int getCombosMade() {
        return combosMade;
    }

    public int getCombosNeeded() {
        return combosNeeded;
    }

    public List<ZombieType> getIzombieTypes() {
        return izombieTypes;
    }

    public List<Vase> getVases() {
        return vases;
    }
}
