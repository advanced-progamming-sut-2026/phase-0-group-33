package models.game;

public class PlantFoodDrop {

    private static final int FALL_TICKS = 12;
    private static final int LIFE_TICKS = 30 * GameSession.TICKS_PER_SECOND;

    private final int x;
    private final int y;
    private int ticksToLand = FALL_TICKS;
    private int ticksLeft = LIFE_TICKS;

    public PlantFoodDrop(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isFalling() {
        return ticksToLand > 0;
    }

    public float getFallProgress() {
        return ticksToLand <= 0 ? 1f : 1f - ticksToLand / (float) FALL_TICKS;
    }

    public boolean isExpiring() {
        return ticksLeft <= 5 * GameSession.TICKS_PER_SECOND;
    }

    public boolean tick() {
        if (ticksToLand > 0) {
            ticksToLand--;
        }
        ticksLeft--;
        return ticksLeft <= 0;
    }
}
