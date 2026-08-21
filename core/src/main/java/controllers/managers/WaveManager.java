package controllers.managers;

import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveManager {
    private final GameSession session;
    private final List<ZombieType> pool;
    private final Random random;
    private final int totalWaves;
    private final double[] waveBudgets;
    private final double costFactor;
    private int currentWave;
    private boolean started;
    private static final double GROWTH = 1.18;
    private static final double FINAL_SURGE = 1.8;
    private static final int MIN_WAVE_GAP = 14 * GameSession.TICKS_PER_SECOND;

    private int currentWaveSpawnedHp;
    private int ticksSinceWave;
    private double bestProgress;

    private boolean endless;

    public void setEndless(boolean endless) {
        this.endless = endless;
    }

    public boolean isEndless() {
        return endless;
    }

    public WaveManager(GameSession session, List<ZombieType> pool, int totalWaves,
                       int baseBudget, double costFactor, Random random) {
        this.session = session;
        this.pool = new ArrayList<>(pool);
        this.random = random;
        this.totalWaves = Math.max(1, totalWaves);
        this.costFactor = costFactor;
        this.waveBudgets = computeBudgets(this.totalWaves, baseBudget);
    }

    private static double[] computeBudgets(int waves, int baseBudget) {
        double[] budgets = new double[waves];
        budgets[0] = baseBudget;
        for (int i = 1; i < waves; i++) {
            budgets[i] = budgets[i - 1] * GROWTH;
        }
        if (waves > 1) {
            budgets[waves - 1] = budgets[waves - 2] * FINAL_SURGE;
        }
        return budgets;
    }

    public void startWaves() {
        if (!started) {
            started = true;
            beginWave(1);
        }
    }

    public void tick() {
        if (!started || (currentWave >= totalWaves && !endless)) {
            return;
        }
        ticksSinceWave++;
        if (ticksSinceWave < MIN_WAVE_GAP) {
            return;
        }
        if (remainingWaveHpFraction() <= 0.25) {
            beginWave(currentWave + 1);
        }
    }

    private double remainingWaveHpFraction() {
        if (currentWaveSpawnedHp <= 0) {
            return 0;
        }
        int remaining = 0;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getSpawnWave() == currentWave) {
                remaining += Math.max(0, zombie.getHealth()) + zombie.totalArmor();
            }
        }
        return remaining / (double) currentWaveSpawnedHp;
    }

    private void beginWave(int waveNumber) {
        currentWave = waveNumber;
        ticksSinceWave = 0;
        if (endless) {
            System.out.printf("Wave %d rolls in.%n", waveNumber);
        } else if (waveNumber == totalWaves) {
            System.out.println("The final wave has come.");
        } else {
            System.out.printf("Wave %d started.%n", waveNumber);
        }
        session.getBehaviorManager().onWaveStart(waveNumber);
        if (waveNumber == 1) {
            session.getQuestStats().onWaveOneStarted(session.getTickCount());
        }
        currentWaveSpawnedHp = 0;
        double budget = budgetFor(waveNumber);
        while (true) {
            ZombieType type = pool.get(random.nextInt(pool.size()));
            int cost = Math.max(1, (int) Math.round(type.getWaveCost() * costFactor));
            if (cost > budget) {
                if (!anyAffordable(budget)) {
                    break;
                }
                continue;
            }
            budget -= cost;
            int lane = 1 + random.nextInt(GameSession.ROWS);
            Zombie zombie = session.spawnZombie(type, GameSession.COLS, lane, waveNumber);
            currentWaveSpawnedHp += Math.max(0, zombie.getHealth()) + zombie.totalArmor();
            System.out.printf("Zombie %s spawned at wave %d in lane %d which costed %d.%n",
                    type.getName(), waveNumber, lane, cost);
        }
        fillOutWave(waveNumber);
        session.getBehaviorManager().afterWaveSpawn(waveNumber);
    }

    private double budgetFor(int waveNumber) {
        if (waveNumber <= waveBudgets.length) {
            return waveBudgets[waveNumber - 1];
        }
        double budget = waveBudgets[waveBudgets.length - 1];
        for (int extra = waveBudgets.length; extra < waveNumber; extra++) {
            budget *= GROWTH;
        }
        return budget;
    }

    private int leastZombies(int waveNumber) {
        int floor = 2 + waveNumber;
        return waveNumber == totalWaves ? floor * 2 : floor;
    }

    private void fillOutWave(int waveNumber) {
        int spawned = 0;
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getSpawnWave() == waveNumber) {
                spawned++;
            }
        }
        for (int i = spawned; i < leastZombies(waveNumber); i++) {
            ZombieType type = pool.get(random.nextInt(pool.size()));
            int lane = 1 + random.nextInt(GameSession.ROWS);
            Zombie zombie = session.spawnZombie(type, GameSession.COLS, lane, waveNumber);
            currentWaveSpawnedHp += Math.max(0, zombie.getHealth()) + zombie.totalArmor();
            System.out.printf("Zombie %s joined wave %d in lane %d.%n",
                    type.getName(), waveNumber, lane);
        }
    }

    private boolean anyAffordable(double budget) {
        for (ZombieType type : pool) {
            int cost = Math.max(1, (int) Math.round(type.getWaveCost() * costFactor));
            if (cost <= budget) {
                return true;
            }
        }
        return false;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean allWavesCleared() {
        return !endless && started && currentWave >= totalWaves
                && session.getZombies().isEmpty();
    }

    public double getWaveClearedFraction() {
        if (!started) {
            return 0;
        }
        return Math.max(0, Math.min(1, 1 - remainingWaveHpFraction()));
    }

    public double getProgress() {
        if (!started) {
            return 0;
        }
        if (endless) {
            return Math.max(0, Math.min(1, getWaveClearedFraction()));
        }
        double now = Math.max(0, Math.min(1,
                (currentWave - 1 + getWaveClearedFraction()) / totalWaves));
        bestProgress = Math.max(bestProgress, now);
        return bestProgress;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        return totalWaves;
    }
}
