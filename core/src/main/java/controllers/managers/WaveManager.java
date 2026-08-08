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
    private int currentWaveSpawnedHp;

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
            budgets[i] = budgets[i - 1] * 1.25;
        }
        if (waves > 1) {
            budgets[waves - 1] = budgets[waves - 2] * 2;
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
        if (!started || currentWave >= totalWaves) {
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
        if (waveNumber == totalWaves) {
            System.out.println("The final wave has come.");
        } else {
            System.out.printf("Wave %d started.%n", waveNumber);
        }
        session.getBehaviorManager().onWaveStart(waveNumber);
        if (waveNumber == 1) {
            session.getQuestStats().onWaveOneStarted(session.getTickCount());
        }
        currentWaveSpawnedHp = 0;
        double budget = waveBudgets[waveNumber - 1];
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
        session.getBehaviorManager().afterWaveSpawn(waveNumber);
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
        return started && currentWave >= totalWaves && session.getZombies().isEmpty();
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        return totalWaves;
    }
}
