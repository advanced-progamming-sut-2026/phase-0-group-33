package models.entities.zombie;

import models.map.Position;

public class Zomboss extends Zombie {

    public enum BossKind {
        DRAGON("Dragon Zomboss", 9000),
        ROBOT("Egypt Zomboss", 9000),
        MAMMOTH("Mammoth Zomboss", 10000),
        SHARK("Shark Zomboss", 9000);

        private final String title;
        private final int hitpoints;

        BossKind(String title, int hitpoints) {
            this.title = title;
            this.hitpoints = hitpoints;
        }

        public String getTitle() {
            return title;
        }

        public int getHitpoints() {
            return hitpoints;
        }
    }

    public static final int SEGMENTS = 3;
    private static final int STUN_TICKS = 60;

    private final BossKind kind;
    private final int segmentHealth;

    private int segmentsCleared;
    private int stunTicks;
    private int abilityTicks;

    public Zomboss(BossKind kind, int row, double healthFactor) {
        super(ZombieType.GARGANTUAR, new Position(8, row),
                (int) Math.round(kind.getHitpoints() * healthFactor), 0);
        this.kind = kind;
        this.segmentHealth = Math.max(1, getHealth() / SEGMENTS);
    }

    public BossKind getKind() {
        return kind;
    }

    @Override
    public boolean occupiesRow(int row) {
        int top = (int) getPosition().getY();
        return row == top || row == top + 1;
    }

    public int getSegmentsCleared() {
        return segmentsCleared;
    }

    public int getSegmentHealth() {
        return segmentHealth;
    }

    public boolean isStunned() {
        return stunTicks > 0;
    }

    public int getStunTicks() {
        return stunTicks;
    }

    public void stun() {
        this.stunTicks = STUN_TICKS;
    }

    public int getAbilityTicks() {
        return abilityTicks;
    }

    public void setAbilityTicks(int abilityTicks) {
        this.abilityTicks = abilityTicks;
    }

    public void tickTimers() {
        if (stunTicks > 0) {
            stunTicks--;
        }
        if (abilityTicks > 0) {
            abilityTicks--;
        }
    }

    public boolean consumeSegmentIfCleared() {
        int cleared = SEGMENTS - (int) Math.ceil(Math.max(0, getHealth()) / (double) segmentHealth);
        if (cleared > segmentsCleared) {
            segmentsCleared = cleared;
            return true;
        }
        return false;
    }

    public float healthFraction() {
        return Math.max(0f, getHealth() / (float) (segmentHealth * SEGMENTS));
    }
}
