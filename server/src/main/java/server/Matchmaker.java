package server;

import database.UserDAO;
import net.Packet;
import net.Protocol;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public final class Matchmaker {

    private final GameServer server;
    private final UserDAO users = new UserDAO();
    private final Deque<ClientSession> queue = new ArrayDeque<>();
    private final Map<String, String> offers = new ConcurrentHashMap<>();
    private final Map<Long, Match> live = new ConcurrentHashMap<>();
    private final AtomicLong nextMatch = new AtomicLong(1);

    public Matchmaker(GameServer server) {
        this.server = server;
    }

    public void register(Map<String, BiConsumer<ClientSession, Packet>> routes) {
        routes.put(Protocol.INVITE, this::invite);
        routes.put(Protocol.INVITE_ANSWER, this::answer);
        routes.put(Protocol.QUEUE_JOIN, this::joinQueue);
        routes.put(Protocol.QUEUE_LEAVE, this::leaveQueue);
        routes.put(Protocol.MATCH_INTENT, this::intent);
        routes.put(Protocol.MATCH_LEAVE, this::leave);
        routes.put(Protocol.REACTION, this::react);
    }

    private void invite(ClientSession from, Packet request) {
        String target = request.str("target").trim();
        if (target.isEmpty() || target.equals(from.username())) {
            from.deny(request, "Type the username of somebody else.");
            return;
        }
        if (!users.existsByUsername(target)) {
            from.deny(request, "No player called " + target + ".");
            return;
        }
        ClientSession other = server.findOnline(target);
        if (other == null) {
            from.deny(request, target + " is offline right now.");
            return;
        }
        if (other.match() != null || from.match() != null) {
            from.deny(request, target + " is already in a match.");
            return;
        }
        if (offers.putIfAbsent(target, from.username()) != null) {
            from.deny(request, target + " is already deciding on another invite.");
            return;
        }
        other.send(Packet.of(Protocol.INVITE_OFFER).put("from", from.username()));
        from.ok(request, Packet.of(request.type()).put(Protocol.MESSAGE,
                "Waiting for " + target + " to answer."));
    }

    private void answer(ClientSession from, Packet request) {
        String inviter = offers.remove(from.username());
        if (inviter == null) {
            from.deny(request, "That invite is no longer open.");
            return;
        }
        ClientSession host = server.findOnline(inviter);
        if (host == null) {
            from.deny(request, inviter + " went offline.");
            return;
        }
        if (!request.flag("accept", false)) {
            host.send(Packet.of(Protocol.INVITE_CANCELLED)
                    .put(Protocol.MESSAGE, from.username() + " turned down your invite."));
            from.ok(request);
            return;
        }
        from.ok(request);
        start(host, from);
    }

    private void joinQueue(ClientSession from, Packet request) {
        ClientSession partner = null;
        synchronized (queue) {
            queue.remove(from);
            while (!queue.isEmpty()) {
                ClientSession candidate = queue.poll();
                if (candidate.match() == null && server.findOnline(candidate.username()) != null) {
                    partner = candidate;
                    break;
                }
            }
            if (partner == null) {
                queue.add(from);
            }
        }
        from.ok(request, Packet.of(request.type()).put("waiting", partner == null));
        if (partner != null) {
            start(partner, from);
        }
    }

    private void leaveQueue(ClientSession from, Packet request) {
        synchronized (queue) {
            queue.remove(from);
        }
        from.ok(request);
    }

    private void start(ClientSession host, ClientSession guest) {
        synchronized (queue) {
            queue.remove(host);
            queue.remove(guest);
        }
        long id = nextMatch.getAndIncrement();
        Match match = new Match(id, host, guest, () -> live.remove(id));
        live.put(id, match);
        match.begin();
    }

    private void intent(ClientSession from, Packet request) {
        Match match = from.match();
        if (match != null) {
            match.intent(from, request);
        }
    }

    private void react(ClientSession from, Packet request) {
        Match match = from.match();
        if (match != null) {
            match.react(from, request);
        }
    }

    private void leave(ClientSession from, Packet request) {
        Match match = from.match();
        if (match != null) {
            match.forfeit(from);
        }
        from.ok(request);
    }

    public void dropped(ClientSession session) {
        synchronized (queue) {
            queue.remove(session);
        }
        String username = session.username();
        if (username != null) {
            offers.remove(username);
            offers.values().removeIf(username::equals);
        }
        Match match = session.match();
        if (match != null) {
            match.forfeit(session);
        }
    }
}
