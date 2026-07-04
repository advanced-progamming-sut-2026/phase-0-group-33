package models.game;

/**
 * Miopoint scoring for the scoring game (doc: bonus scoring section).
 * Five patterns award points on top of a base amount per kill:
 * 1) multi-kill: several zombies dying in the same tick,
 * 2) speed kill: a zombie dies within 5 seconds of spawning,
 * 3) mass kill: 4+ zombies die within one second,
 * 4) streak: 5 kills without losing a single plant,
 * 5) untouched defense: finishing with unused lawnmowers.
 */
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

    /** Pattern hooks; called for every zombie death. */
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

    /** Streak pattern resets whenever the player loses a plant. */
    public void onPlantLost() {
        streak = 0;
    }

    /** End-of-game bonus for every lawnmower that was never triggered. */
    public void onGameWon(int unusedMowers) {
        score += MOWER_BONUS * unusedMowers;
    }

    public int getScore() {
        return score;
    }
}
