package server;

import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.GameSetup;
import models.game.Names;
import models.game.PlacedPlant;
import models.user.User;
import net.MatchSnapshot;
import net.Packet;
import net.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Match {

    public static final int ROUND_SECONDS = 120;
    private static final int TICK_MILLIS = 1000 / GameSession.TICKS_PER_SECOND;
    private static final int ZOMBIE_SUN_EVERY = 45;
    private static final int ZOMBIE_SUN_AMOUNT = 50;
    private static final int ZOMBIE_START_SUN = 175;

    private final long id;
    private final ClientSession planter;
    private final ClientSession raiser;
    private final GameSession session;
    private final Map<ZombieType, Integer> cooldowns = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final List<ZombieType> roster;
    private final Runnable onFinish;

    private int zombieSun = ZOMBIE_START_SUN;
    private int ticksLeft = ROUND_SECONDS * GameSession.TICKS_PER_SECOND;
    private long counter;
    private Thread clock;

    public Match(long id, ClientSession planter, ClientSession raiser, Runnable onFinish) {
        this.id = id;
        this.planter = planter;
        this.raiser = raiser;
        this.onFinish = onFinish;
        this.session = build(planter.username());
        this.roster = new ArrayList<>(session.getMinigameManager().getIzombieTypes());
    }

    private GameSession build(String owner) {
        User user = new User();
        user.setUsername(owner);
        List<String> plants = new ArrayList<>();
        for (PlantType type : PlantType.values()) {
            plants.add(type.getName());
        }
        GameSession fresh = new GameSession(GameSetup.duel(user, plants, id));
        fresh.startGame();
        return fresh;
    }

    public void begin() {
        planter.setMatch(this, Protocol.ROLE_PLANTS);
        raiser.setMatch(this, Protocol.ROLE_ZOMBIES);
        planter.send(opening(Protocol.ROLE_PLANTS, raiser.username()));
        raiser.send(opening(Protocol.ROLE_ZOMBIES, planter.username()));
        clock = new Thread(this::spin, "match-" + id);
        clock.setDaemon(true);
        clock.start();
        Log.say("Match " + id + ": " + planter.username() + " (plants) vs "
                + raiser.username() + " (zombies).");
    }

    private Packet opening(String role, String opponent) {
        List<Object> names = new ArrayList<>();
        List<Object> costs = new ArrayList<>();
        for (ZombieType type : roster) {
            names.add(type.getName());
            costs.add(type.getWaveCost());
        }
        return Packet.of(Protocol.MATCH_START).put("match", id).put("role", role)
                .put("opponent", opponent).put("seconds", ROUND_SECONDS)
                .put("roster", names).put("costs", costs);
    }

    private void spin() {
        long due = System.currentTimeMillis();
        while (running.get()) {
            due += TICK_MILLIS;
            step();
            long wait = due - System.currentTimeMillis();
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } else {
                due = System.currentTimeMillis();
            }
        }
    }

    private synchronized void step() {
        if (!running.get()) {
            return;
        }
        session.advanceTime(1);
        tickCooldowns();
        payZombiePlayer();
        broadcastState();
        if (session.isOver()) {
            finish(Protocol.ROLE_ZOMBIES, "Every brain was eaten.");
            return;
        }
        if (--ticksLeft <= 0) {
            finish(Protocol.ROLE_PLANTS, "The plants held the line for the full round.");
        }
    }

    private void tickCooldowns() {
        for (Map.Entry<ZombieType, Integer> entry : cooldowns.entrySet()) {
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            }
        }
    }

    private void payZombiePlayer() {
        if (ticksLeft % ZOMBIE_SUN_EVERY == 0) {
            zombieSun += ZOMBIE_SUN_AMOUNT;
        }
    }

    private void broadcastState() {
        Packet state = MatchSnapshot.capture(session, counter);
        counter = state.big("counter", counter);
        state.put("left", ticksLeft / GameSession.TICKS_PER_SECOND);
        planter.send(state.put("mine", session.getSunManager().getSunBalance()));
        raiser.send(state.put("mine", zombieSun).put("cool", coolRow()));
    }

    private List<Object> coolRow() {
        List<Object> values = new ArrayList<>();
        for (ZombieType type : roster) {
            values.add(cooldowns.getOrDefault(type, 0) / GameSession.TICKS_PER_SECOND);
        }
        return values;
    }

    public synchronized void intent(ClientSession from, Packet packet) {
        if (!running.get()) {
            return;
        }
        String what = packet.str("what");
        int x = packet.num("x", 0);
        int y = packet.num("y", 0);
        if (from == planter) {
            plantSide(from, packet, what, x, y);
        } else if (from == raiser && Protocol.INTENT_ZOMBIE.equals(what)) {
            raiseZombie(from, packet.str("type"), x, y);
        }
    }

    private void plantSide(ClientSession from, Packet packet, String what, int x, int y) {
        if (Protocol.INTENT_PLANT.equals(what)) {
            note(from, session.plantAt(packet.str("type"), x, y).getMessages());
        } else if (Protocol.INTENT_SUN.equals(what)) {
            session.collectSun(x, y);
        } else if (Protocol.INTENT_SHOVEL.equals(what)) {
            note(from, session.pluckPlant(x, y).getMessages());
        }
    }

    private void raiseZombie(ClientSession from, String typeName, int x, int y) {
        ZombieType type = Names.zombie(typeName);
        if (type == null || !roster.contains(type)) {
            note(from, List.of("That zombie is not in your roster."));
            return;
        }
        if (x < 6 || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            note(from, List.of("Zombies drop right of the red line (columns 6-9)."));
            return;
        }
        PlacedPlant standing = session.plantAt(x, y);
        if (standing != null) {
            note(from, List.of("A plant already stands on that tile."));
            return;
        }
        if (cooldowns.getOrDefault(type, 0) > 0) {
            note(from, List.of(type.getName() + " is still recharging."));
            return;
        }
        if (zombieSun < type.getWaveCost()) {
            note(from, List.of(type.getName() + " costs " + type.getWaveCost() + " sun."));
            return;
        }
        zombieSun -= type.getWaveCost();
        cooldowns.put(type, session.getMinigameManager().zombieRechargeTicks(type));
        Zombie dropped = session.spawnZombie(type, x, y, 1);
        dropped.getBattle().setSpawnTick(0);
    }

    private void note(ClientSession target, List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        target.send(Packet.of(Protocol.MESSAGE).put(Protocol.MESSAGE, messages.get(0)));
    }

    public void react(ClientSession from, Packet packet) {
        ClientSession other = from == planter ? raiser : planter;
        other.send(Packet.of(Protocol.REACTION_IN)
                .put("kind", packet.str("kind")).put("index", packet.num("index", 0))
                .put("from", from.username()));
    }

    public synchronized void forfeit(ClientSession who) {
        if (!running.get()) {
            return;
        }
        boolean plantsLeft = who == planter;
        finish(plantsLeft ? Protocol.ROLE_ZOMBIES : Protocol.ROLE_PLANTS,
                who.username() + " left the match.");
    }

    private void finish(String winner, String reason) {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        Packet over = Packet.of(Protocol.MATCH_OVER).put("winner", winner).put("reason", reason);
        planter.send(over);
        raiser.send(over);
        planter.setMatch(null, "");
        raiser.setMatch(null, "");
        Log.say("Match " + id + " ended: " + winner + " won. " + reason);
        onFinish.run();
    }

    public boolean holds(ClientSession session) {
        return planter == session || raiser == session;
    }
}
