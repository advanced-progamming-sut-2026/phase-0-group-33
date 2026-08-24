package server;

import net.Connection;
import net.Packet;
import net.Protocol;

import java.io.IOException;

public final class ClientSession {

    private final GameServer server;
    private final Connection connection;
    private final long id;

    private volatile String username;
    private volatile Match match;
    private volatile String role = "";

    public ClientSession(GameServer server, Connection connection, long id) {
        this.server = server;
        this.connection = connection;
        this.id = id;
    }

    public long id() {
        return id;
    }

    public String username() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSignedIn() {
        return username != null;
    }

    public Match match() {
        return match;
    }

    public void setMatch(Match match, String role) {
        this.match = match;
        this.role = role == null ? "" : role;
    }

    public String role() {
        return role;
    }

    public String where() {
        return (username == null ? "guest#" + id : username) + "@" + connection.remoteName();
    }

    public void send(Packet packet) {
        try {
            connection.send(packet);
        } catch (IOException e) {
            connection.close();
        }
    }

    public void reply(Packet request, Packet answer) {
        long requestId = request.big(Protocol.REQ, 0);
        if (requestId > 0) {
            answer.put(Protocol.REQ, requestId);
        }
        send(answer);
    }

    public void ok(Packet request, Packet answer) {
        reply(request, answer.put(Protocol.OK, true));
    }

    public void ok(Packet request) {
        ok(request, Packet.of(request.type()));
    }

    public void deny(Packet request, String message) {
        reply(request, Packet.of(request.type())
                .put(Protocol.OK, false).put(Protocol.MESSAGE, message));
    }

    public void run() {
        try {
            while (true) {
                Packet packet = connection.receive();
                if (packet == null) {
                    break;
                }
                server.dispatch(this, packet);
            }
        } catch (IOException e) {
            Log.say(where() + " dropped the connection.");
        } finally {
            close();
        }
    }

    public void close() {
        connection.close();
        server.forget(this);
    }
}
