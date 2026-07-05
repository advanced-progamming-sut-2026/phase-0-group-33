package controllers.managers;

import models.game.Sun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SunManager {
    private static final int TICKS_PER_SECOND = 10;

    private int sunBalance;
    private final List<Sun> suns = new ArrayList<>();
    private final Random random;
    private final boolean skyEnabled;
    private final double difficultyFactor;
    private int elapsedTicks;
    private int ticksUntilNextDrop;

    public SunManager(int startingSun, boolean skyEnabled, double difficultyFactor, Random random) {
        this.sunBalance = startingSun;
        this.skyEnabled = skyEnabled;
        this.difficultyFactor = difficultyFactor;
        this.random = random;
        this.ticksUntilNextDrop = dropIntervalTicks();
    }

    private int dropIntervalTicks() {
        double seconds = Math.max(6 + 0.05 * (elapsedTicks / (double) TICKS_PER_SECOND), 12);
        return (int) Math.round(seconds * TICKS_PER_SECOND * difficultyFactor);
    }

    public void tick() {
        elapsedTicks++;
        for (Sun sun : suns) {
            if (sun.tickFall()) {
                System.out.printf("Sun reached the ground at position (%d, %d)%n", sun.getX(), sun.getY());
            }
        }
        if (skyEnabled && --ticksUntilNextDrop <= 0) {
            dropSunFromSky();
            ticksUntilNextDrop = dropIntervalTicks();
        }
    }

    private void dropSunFromSky() {
        int roll = random.nextInt(100);
        Sun.SunKind kind;
        if (roll < 80) {
            kind = Sun.SunKind.NORMAL;
        } else if (roll < 95) {
            kind = Sun.SunKind.SPECIAL;
        } else {
            kind = Sun.SunKind.RADIOACTIVE;
        }
        int x = 1 + random.nextInt(9);
        int y = 1 + random.nextInt(5);
        suns.add(Sun.falling(kind, x, y));
        System.out.printf("New %s sun is dropping at position (%d, %d)%n", kind.getLabel(), x, y);
    }

    public void addProducedSun(int x, int y, int value) {
        suns.add(Sun.produced(x, y, value));
    }

    public Sun collectAt(int x, int y) {
        Sun landedMatch = null;
        for (Sun sun : suns) {
            if (sun.getX() != x || sun.getY() != y) {
                continue;
            }
            if (sun.isFalling()) {
                if (sun.getKind() == Sun.SunKind.RADIOACTIVE) {
                    suns.remove(sun);
                    return sun;
                }
            } else if (landedMatch == null) {
                landedMatch = sun;
            }
        }
        if (landedMatch != null) {
            suns.remove(landedMatch);
            sunBalance += landedMatch.getValue();
        }
        return landedMatch;
    }

    public void clearProducedAt(int x, int y) {
        Iterator<Sun> iterator = suns.iterator();
        while (iterator.hasNext()) {
            Sun sun = iterator.next();
            if (sun.isProducedByPlant() && sun.getX() == x && sun.getY() == y) {
                iterator.remove();
            }
        }
    }

    public Sun stealLanded() {
        for (Sun sun : suns) {
            if (!sun.isFalling() && !sun.isProducedByPlant()) {
                suns.remove(sun);
                return sun;
            }
        }
        return null;
    }

    public boolean hasSunAt(int x, int y) {
        for (Sun sun : suns) {
            if (!sun.isFalling() && sun.getX() == x && sun.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public int getSunBalance() {
        return sunBalance;
    }

    public void addSun(int amount) {
        sunBalance += amount;
    }

    public boolean spendSun(int amount) {
        if (sunBalance < amount) {
            return false;
        }
        sunBalance -= amount;
        return true;
    }
}
