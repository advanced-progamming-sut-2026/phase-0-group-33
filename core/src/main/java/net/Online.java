package net;

import models.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Online {

    private static final Online INSTANCE = new Online();

    private final NetClient client = NetClient.get();
    private final RemoteStorage storage = new RemoteStorage(client);
    private final Queue<Packet> events = new ConcurrentLinkedQueue<>();

    private String username;

    private Online() {
        client.addListener(events::add);
    }

    public static Online get() {
        return INSTANCE;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public boolean isSignedIn() {
        return username != null && client.isConnected();
    }

    public String username() {
        return username;
    }

    public String address() {
        return client.address();
    }

    public Packet nextEvent() {
        return events.poll();
    }

    public void clearEvents() {
        events.clear();
    }

    public Result connect(String host, int port) {
        if (client.connect(host, port)) {
            return Result.ok("Connected to " + client.address() + ".");
        }
        return Result.fail(client.lastFailure());
    }

    public void disconnect() {
        username = null;
        storage.forget();
        utils.FileStore.useBackend(null);
        client.disconnect();
        events.clear();
    }

    private Result answer(Packet reply) {
        if (reply.flag(Protocol.OK, false)) {
            String note = reply.str(Protocol.MESSAGE, "");
            return note.isEmpty() ? Result.ok("Done.") : Result.ok(note);
        }
        return Result.fail(reply.str(Protocol.MESSAGE, "The server refused that request."));
    }

    public Result signupDetails(String user, String password, String confirm,
                                String nickname, String email, String gender) {
        return answer(client.request(Packet.of(Protocol.SIGNUP)
                .put("username", user).put("password", password).put("confirm", confirm)
                .put("nickname", nickname).put("email", email).put("gender", gender)));
    }

    public Result signupFinish(String question, String answer, String confirm) {
        return answer(client.request(Packet.of(Protocol.SIGNUP).put("step", "finish")
                .put("question", question).put("answer", answer).put("confirm", confirm)));
    }

    public Result login(String user, String password) {
        Packet reply = client.request(Packet.of(Protocol.LOGIN)
                .put("username", user).put("password", password));
        if (!reply.flag(Protocol.OK, false)) {
            return Result.fail(reply.str(Protocol.MESSAGE, "Could not sign in."));
        }
        username = reply.str("username", user);
        storage.forget();
        utils.FileStore.useBackend(storage);
        return Result.ok("Welcome back, " + username + ".");
    }

    public Result securityQuestion(String user, String email) {
        Packet packet = Packet.of(Protocol.SECURITY_QUESTION).put("username", user);
        if (email != null && !email.isBlank()) {
            packet.put("email", email);
        }
        Packet reply = client.request(packet);
        if (!reply.flag(Protocol.OK, false)) {
            return Result.fail(reply.str(Protocol.MESSAGE, "Could not fetch the question."));
        }
        return Result.ok(reply.str("question"));
    }

    public Result verifyAnswer(String user, String reply) {
        return answer(client.request(Packet.of(Protocol.RESET_PASSWORD)
                .put("username", user).put("answer", reply)));
    }

    public Result resetPassword(String user, String reply, String password) {
        return answer(client.request(Packet.of(Protocol.RESET_PASSWORD).put("username", user)
                .put("answer", reply).put("password", password)));
    }

    public Result signOut() {
        Packet reply = client.request(Packet.of(Protocol.LOGOUT));
        username = null;
        storage.forget();
        utils.FileStore.useBackend(null);
        events.clear();
        return answer(reply);
    }

    public List<String> whoIsOnline() {
        Packet reply = client.request(Packet.of(Protocol.WHO_IS_ONLINE));
        return reply.flag(Protocol.OK, false) ? reply.list("names") : new ArrayList<>();
    }

    public Result invite(String target) {
        return answer(client.request(Packet.of(Protocol.INVITE).put("target", target)));
    }

    public Result answerInvite(boolean accept) {
        return answer(client.request(Packet.of(Protocol.INVITE_ANSWER).put("accept", accept)));
    }

    public Result joinQueue() {
        Packet reply = client.request(Packet.of(Protocol.QUEUE_JOIN));
        if (!reply.flag(Protocol.OK, false)) {
            return Result.fail(reply.str(Protocol.MESSAGE, "Could not join the queue."));
        }
        return Result.ok(reply.flag("waiting", true)
                ? "Waiting for another player to join the queue."
                : "Found an opponent.");
    }

    public Result leaveQueue() {
        return answer(client.request(Packet.of(Protocol.QUEUE_LEAVE)));
    }

    public void intent(Packet packet) {
        client.tell(packet);
    }

    public void react(String kind, int index) {
        client.tell(Packet.of(Protocol.REACTION).put("kind", kind).put("index", index));
    }

    public void leaveMatch() {
        client.tell(Packet.of(Protocol.MATCH_LEAVE));
    }

    public Result submitScore(int score) {
        Packet reply = client.request(Packet.of(Protocol.SUBMIT_SCORE).put("score", score));
        if (!reply.flag(Protocol.OK, false)) {
            return Result.fail(reply.str(Protocol.MESSAGE, "Could not send your score."));
        }
        return Result.ok(reply.flag("record", false)
                ? "New personal record: " + reply.num("best", score) + " points!"
                : "Your best online score is still " + reply.num("best", score) + ".");
    }

    public List<BoardRow> leaderboard() {
        Packet reply = client.request(Packet.of(Protocol.LEADERBOARD));
        List<BoardRow> rows = new ArrayList<>();
        if (!reply.flag(Protocol.OK, false)) {
            return rows;
        }
        for (java.util.Map<String, Object> raw : reply.maps("rows")) {
            rows.add(BoardRow.from(raw));
        }
        return rows;
    }
}
