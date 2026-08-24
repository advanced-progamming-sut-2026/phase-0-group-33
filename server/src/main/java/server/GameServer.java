package server;

import net.Connection;
import net.Packet;
import net.Protocol;
import utils.FileStore;
import utils.LocalStorage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public final class GameServer {

    private final int port;
    private final Map<String, BiConsumer<ClientSession, Packet>> routes = new ConcurrentHashMap<>();
    private final Map<Long, ClientSession> everyone = new ConcurrentHashMap<>();
    private final Map<String, ClientSession> signedIn = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    private final AccountService accounts = new AccountService(this);
    private final StorageService files = new StorageService();
    private final LeaderboardService board = new LeaderboardService();
    private final Matchmaker matchmaker = new Matchmaker(this);

    private volatile ServerSocket socket;
    private volatile boolean running;

    public GameServer(int port) {
        this.port = port;
        FileStore.useBackend(new LocalStorage(Paths.get("data")));
        accounts.register(routes);
        files.register(routes);
        board.register(routes);
        matchmaker.register(routes);
    }

    public Matchmaker matchmaker() {
        return matchmaker;
    }

    public void start() {
        running = true;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            Log.warn("Could not open port " + port + ": " + e.getMessage());
            return;
        }
        Log.say("Plants vs. Zombies server listening on port " + port + ".");
        Log.say("Data lives in " + Paths.get("data").toAbsolutePath() + ".");
        accept();
    }

    private void accept() {
        while (running) {
            try {
                Socket incoming = socket.accept();
                ClientSession session = new ClientSession(this, new Connection(incoming),
                        nextId.getAndIncrement());
                everyone.put(session.id(), session);
                Thread thread = new Thread(session::run, "client-" + session.id());
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
                if (running) {
                    Log.warn("Rejected a connection: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        for (ClientSession session : new ArrayList<>(everyone.values())) {
            session.close();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            return;
        }
        Log.say("Server stopped.");
    }

    public void dispatch(ClientSession session, Packet packet) {
        BiConsumer<ClientSession, Packet> route = routes.get(packet.type());
        if (route == null) {
            session.deny(packet, "The server does not understand '" + packet.type() + "'.");
            return;
        }
        if (!session.isSignedIn() && !accounts.isOpenToGuests(packet.type())) {
            session.deny(packet, "You must sign in first.");
            return;
        }
        try {
            route.accept(session, packet);
        } catch (RuntimeException e) {
            Log.warn("Failed to handle '" + packet.type() + "' from " + session.where() + ": " + e);
            session.deny(packet, "The server hit an unexpected problem.");
        }
    }

    public void bind(String username, ClientSession session) {
        ClientSession previous = signedIn.put(username, session);
        if (previous != null && previous != session) {
            previous.send(Packet.of(Protocol.LOGOUT).put("reason", "signed-in-elsewhere"));
            previous.close();
        }
    }

    public void unbind(ClientSession session) {
        String username = session.username();
        if (username == null) {
            return;
        }
        matchmaker.dropped(session);
        if (signedIn.get(username) == session) {
            signedIn.remove(username);
        }
        session.setUsername(null);
        Log.say(username + " signed out.");
    }

    public ClientSession findOnline(String username) {
        return username == null ? null : signedIn.get(username);
    }

    public List<String> onlineNames() {
        return new ArrayList<>(signedIn.keySet());
    }

    public void forget(ClientSession session) {
        everyone.remove(session.id());
        String username = session.username();
        if (username != null && signedIn.get(username) == session) {
            signedIn.remove(username);
            Log.say(username + " went offline.");
        }
        matchmaker.dropped(session);
    }
}
