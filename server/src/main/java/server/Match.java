package server;

import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.game.DuelRules;
import models.game.GameSession;
import models.user.User;
import net.MatchSnapshot;
import net.Packet;
import net.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Match {

    private static final int TICK_MILLIS = 1000 / GameSession.TICKS_PER_SECOND;

    private final long id;
    private final ClientSession planter;
    private final ClientSession raiser;
    private final GameSession session;
    private final DuelRules rules;
    private static final int PICK_SECONDS = 45;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final List<ZombieType> roster = new ArrayList<>();
    private final List<PlantType> seeds = new ArrayList<>();
    private final Runnable onFinish;

    private long counter;
    private Thread clock;

    public Match(long id, ClientSession planter, ClientSession raiser, Runnable onFinish) {
        this.id = id;
        this.planter = planter;
        this.raiser = raiser;
        this.onFinish = onFinish;
        User user = new User();
        user.setUsername(planter.username());
        this.session = DuelRules.openSession(user, id);
        this.rules = new DuelRules(session);
    }

    public void begin() {
        planter.setMatch(this, Protocol.ROLE_PLANTS);
        raiser.setMatch(this, Protocol.ROLE_ZOMBIES);
        planter.send(picking(Protocol.ROLE_PLANTS, raiser.username()));
        raiser.send(picking(Protocol.ROLE_ZOMBIES, planter.username()));
        Thread watchdog = new Thread(this::waitForPicks, "picks-" + id);
        watchdog.setDaemon(true);
        watchdog.start();
        Log.say("Match " + id + ": " + planter.username() + " (plants) vs "
                + raiser.username() + " (zombies) - picking teams.");
    }

    private Packet picking(String role, String opponent) {
        List<Object> pool = new ArrayList<>();
        if (Protocol.ROLE_ZOMBIES.equals(role)) {
            for (ZombieType type : DuelRules.ZOMBIE_POOL) {
                pool.add(type.getName());
            }
        } else {
            for (PlantType type : DuelRules.PLANT_POOL) {
                pool.add(type.getName());
            }
        }
        return Packet.of(Protocol.MATCH_START).put("match", id).put("role", role)
                .put("opponent", opponent).put("seconds", DuelRules.ROUND_SECONDS)
                .put("phase", "picking").put("pool", pool)
                .put("slots", Protocol.ROLE_ZOMBIES.equals(role)
                        ? DuelRules.ZOMBIE_SLOTS : DuelRules.PLANT_SLOTS)
                .put("picking", PICK_SECONDS);
    }

    private void waitForPicks() {
        long deadline = System.currentTimeMillis() + PICK_SECONDS * 1000L;
        while (running.get() && !playing.get() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        startPlay();
    }

    public synchronized void picks(ClientSession from, Packet packet) {
        if (playing.get()) {
            return;
        }
        if (from == raiser) {
            roster.clear();
            for (String name : packet.list("picks")) {
                ZombieType type = models.game.Names.zombie(name);
                if (type != null && roster.size() < DuelRules.ZOMBIE_SLOTS
                        && !roster.contains(type)) {
                    roster.add(type);
                }
            }
        } else if (from == planter) {
            seeds.clear();
            for (String name : packet.list("picks")) {
                PlantType type = models.game.Names.plant(name);
                if (type != null && seeds.size() < DuelRules.PLANT_SLOTS
                        && !seeds.contains(type)) {
                    seeds.add(type);
                }
            }
        }
        if (!roster.isEmpty() && !seeds.isEmpty()) {
            startPlay();
        }
    }

    private synchronized void startPlay() {
        if (!running.get() || !playing.compareAndSet(false, true)) {
            return;
        }
        if (seeds.isEmpty()) {
            java.util.Collections.addAll(seeds, DuelRules.PLANTS);
        }
        for (PlantType type : seeds) {
            session.addPlantToSelection(type.getName());
        }
        session.startGame();
        if (roster.isEmpty()) {
            roster.addAll(rules.roster());
        }
        session.getMinigameManager().setDuelRoster(roster);
        planter.send(opening(Protocol.ROLE_PLANTS, raiser.username()));
        raiser.send(opening(Protocol.ROLE_ZOMBIES, planter.username()));
        clock = new Thread(this::spin, "match-" + id);
        clock.setDaemon(true);
        clock.start();
        Log.say("Match " + id + " begins: " + seeds.size() + " seeds vs "
                + roster.size() + " zombie types.");
    }

    private Packet opening(String role, String opponent) {
        List<Object> names = new ArrayList<>();
        List<Object> costs = new ArrayList<>();
        for (ZombieType type : roster) {
            names.add(type.getName());
            costs.add(type.getWaveCost());
        }
        List<Object> picked = new ArrayList<>();
        for (PlantType type : seeds) {
            picked.add(type.getName());
        }
        return Packet.of(Protocol.MATCH_START).put("match", id).put("role", role)
                .put("opponent", opponent).put("seconds", DuelRules.ROUND_SECONDS)
                .put("phase", "playing")
                .put("roster", names).put("costs", costs).put("seeds", picked);
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
        rules.tick();
        broadcastState();
        if (session.isOver() || rules.brainsGone()) {
            finish(Protocol.ROLE_ZOMBIES, "Every brain was eaten.");
            return;
        }
        if (rules.timeUp()) {
            finish(Protocol.ROLE_PLANTS, "The plants held the line for the full round.");
        }
    }

    private void broadcastState() {
        Packet state = MatchSnapshot.capture(session, counter);
        counter = state.big("counter", counter);
        state.put("left", rules.secondsLeft());
        planter.send(state.put("mine", session.getSunManager().getSunBalance()));
        raiser.send(state.put("mine", rules.getSun()).put("cool", coolRow()));
    }

    private List<Object> coolRow() {
        List<Object> values = new ArrayList<>();
        for (ZombieType type : roster) {
            values.add(rules.cooldownSeconds(type));
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
        models.Result placed = rules.placeZombie(typeName, x, y);
        if (!placed.isSuccessfull()) {
            note(from, placed.getMessages());
        }
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
