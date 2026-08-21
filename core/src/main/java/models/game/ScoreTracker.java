package models.game;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScoreTracker {

    public enum Pattern {
        KILL("Zombie down", 10),
        MULTI_KILL("Multi-kill!", 50),
        SPEED_KILL("Speed kill!", 20),
        MASS_KILL("Mass kill!", 100),
        STREAK("Five in a row!", 75),
        UNTOUCHED("Untouched mower", 150);

        private final String title;
        private final int points;

        Pattern(String title, int points) {
            this.title = title;
            this.points = points;
        }

        public String getTitle() {
            return title;
        }

        public int getPoints() {
            return points;
        }
    }

    private static final int SPEED_KILL_TICKS = 50;
    private static final int MASS_KILL_WINDOW = 10;
    private static final int MASS_KILL_COUNT = 4;
    private static final int STREAK_LENGTH = 5;

    private final Map<Pattern, Integer> tally = new LinkedHashMap<>();
    private final List<Pattern> pending = new ArrayList<>();

    private int score;
    private int lastKillTick = -1;
    private int killsInLastSecondWindowStart = -1;
    private int killsInLastSecond;
    private int streak;
    private int bestStreak;

    private void award(Pattern pattern) {
        score += pattern.getPoints();
        tally.merge(pattern, 1, Integer::sum);
        if (pattern != Pattern.KILL && pending.size() < 16) {
            pending.add(pattern);
        }
    }

    public void onZombieKilled(int tick, int spawnTick) {
        award(Pattern.KILL);
        if (tick == lastKillTick) {
            award(Pattern.MULTI_KILL);
        } else {
            lastKillTick = tick;
        }
        if (tick - spawnTick <= SPEED_KILL_TICKS) {
            award(Pattern.SPEED_KILL);
        }
        trackMassKill(tick);
        streak++;
        bestStreak = Math.max(bestStreak, streak);
        if (streak == STREAK_LENGTH) {
            award(Pattern.STREAK);
            streak = 0;
        }
    }

    private void trackMassKill(int tick) {
        if (killsInLastSecondWindowStart < 0
                || tick - killsInLastSecondWindowStart > MASS_KILL_WINDOW) {
            killsInLastSecondWindowStart = tick;
            killsInLastSecond = 1;
            return;
        }
        killsInLastSecond++;
        if (killsInLastSecond == MASS_KILL_COUNT) {
            award(Pattern.MASS_KILL);
        }
    }

    public void onPlantLost() {
        streak = 0;
    }

    public void onGameWon(int unusedMowers) {
        for (int i = 0; i < unusedMowers; i++) {
            award(Pattern.UNTOUCHED);
        }
    }

    public List<Pattern> drainBonuses() {
        List<Pattern> copy = new ArrayList<>(pending);
        pending.clear();
        return copy;
    }

    public Map<Pattern, Integer> getTally() {
        return new LinkedHashMap<>(tally);
    }

    public int countOf(Pattern pattern) {
        return tally.getOrDefault(pattern, 0);
    }

    public int getStreak() {
        return streak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public int getScore() {
        return score;
    }
}
