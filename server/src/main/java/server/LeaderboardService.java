package server;

import database.UserDAO;
import models.user.User;
import net.Packet;
import net.Protocol;
import utils.UserDataStore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class LeaderboardService {

    public static final String NET_POINT = "netPoint";

    private static final String[] CHAPTERS = {"Egypt", "Frost Bite", "Wavey Beach", "Dark Ages"};

    private final UserDAO users = new UserDAO();

    public void register(Map<String, BiConsumer<ClientSession, Packet>> routes) {
        routes.put(Protocol.LEADERBOARD, this::rows);
        routes.put(Protocol.SUBMIT_SCORE, this::submit);
    }

    private void rows(ClientSession session, Packet request) {
        List<Object> entries = new ArrayList<>();
        for (User user : users.getAllUsers()) {
            entries.add(row(user));
        }
        session.ok(request, Packet.of(request.type()).put("rows", entries));
    }

    private Map<String, Object> row(User user) {
        UserDataStore store = UserDataStore.forUser(user.getUsername());
        store.reload();
        Map<String, Object> entry = new LinkedHashMap<>();
        int daily = store.getInt("dailyQuestsDone", 0);
        entry.put("username", user.getUsername());
        entry.put("nickname", user.getNickname() == null ? user.getUsername() : user.getNickname());
        entry.put("levels", completedLevels(store));
        entry.put("minigames", store.getInt("minigamesWon", 0));
        entry.put("dailyQuests", daily);
        entry.put("quests", Math.max(0, store.getInt("questsDone", 0) - daily));
        entry.put("best", user.getHighestScore());
        entry.put("point", store.getInt(NET_POINT, -1));
        return entry;
    }

    private int completedLevels(UserDataStore store) {
        int total = 0;
        for (String name : CHAPTERS) {
            models.progress.chapter.Chapter chapter =
                    models.progress.chapter.Chapter.getByName(name);
            if (chapter != null) {
                total += Math.min(chapter.getLevels().size(),
                        Math.max(0, store.getInt("progress." + name, 1) - 1));
            }
        }
        return total;
    }

    private void submit(ClientSession session, Packet request) {
        String username = session.username();
        int score = request.num("score", 0);
        if (score < 0) {
            session.deny(request, "A score cannot be negative.");
            return;
        }
        UserDataStore store = UserDataStore.forUser(username);
        store.reload();
        int best = store.getInt(NET_POINT, -1);
        boolean record = score > best;
        if (record) {
            store.setInt(NET_POINT, score);
            store.save();
            Log.say(username + " set a new online record of " + score + " points.");
        }
        session.ok(request, Packet.of(request.type())
                .put("record", record).put("best", Math.max(best, score)));
    }
}
