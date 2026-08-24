package net;

import java.util.Map;

public final class BoardRow {

    private final String username;
    private final String nickname;
    private final int levels;
    private final int minigames;
    private final int dailyQuests;
    private final int quests;
    private final int best;
    private final int point;

    private BoardRow(String username, String nickname, int levels, int minigames,
                     int dailyQuests, int quests, int best, int point) {
        this.username = username;
        this.nickname = nickname;
        this.levels = levels;
        this.minigames = minigames;
        this.dailyQuests = dailyQuests;
        this.quests = quests;
        this.best = best;
        this.point = point;
    }

    public static BoardRow from(Map<String, Object> raw) {
        return new BoardRow(text(raw, "username"), text(raw, "nickname"),
                number(raw, "levels"), number(raw, "minigames"), number(raw, "dailyQuests"),
                number(raw, "quests"), number(raw, "best"), number(raw, "point"));
    }

    private static String text(Map<String, Object> raw, String key) {
        Object value = raw.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static int number(Map<String, Object> raw, String key) {
        Object value = raw.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public int getLevels() {
        return levels;
    }

    public int getMinigames() {
        return minigames;
    }

    public int getDailyQuests() {
        return dailyQuests;
    }

    public int getQuests() {
        return quests;
    }

    public int getBest() {
        return best;
    }

    public boolean hasPoint() {
        return point >= 0;
    }

    public int getPoint() {
        return point;
    }
}
