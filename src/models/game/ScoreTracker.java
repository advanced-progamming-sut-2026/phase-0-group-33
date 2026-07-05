package models.game;

public class ScoreTracker {
    private static final int BASE_KILL = 10;
    private static final int MULTI_KILL_BONUS = 50;
    private static final int SPEED_KILL_BONUS = 20;
    private static final int MASS_KILL_BONUS = 100;
    private static final int STREAK_BONUS = 75;
    private static final int MOWER_BONUS = 150;

    private int score;
    private int lastKillTick = -1;
    private int killsInLastTick;
    private int killsInLastSecondWindowStart = -1;
    private int killsInLastSecond;
    private int streak;

    public void onZombieKilled(int tick, int spawnTick) {
        score += BASE_KILL;
        if (tick == lastKillTick) {
            killsInLastTick++;
            score += MULTI_KILL_BONUS;
        } else {
            lastKillTick = tick;
            killsInLastTick = 1;
        }
        if (tick - spawnTick <= 50) {
            score += SPEED_KILL_BONUS;
        }
        trackMassKill(tick);
        streak++;
        if (streak == 5) {
            score += STREAK_BONUS;
            streak = 0;
        }
    }

    private void trackMassKill(int tick) {
        if (killsInLastSecondWindowStart < 0 || tick - killsInLastSecondWindowStart > 10) {
            killsInLastSecondWindowStart = tick;
            killsInLastSecond = 1;
            return;
        }
        killsInLastSecond++;
        if (killsInLastSecond == 4) {
            score += MASS_KILL_BONUS;
        }
    }

    public void onPlantLost() {
        streak = 0;
    }

    public void onGameWon(int unusedMowers) {
        score += MOWER_BONUS * unusedMowers;
    }

    public int getScore() {
        return score;
    }
}
