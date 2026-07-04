package models.game;

import models.entities.plant.PlantType;

/** A plant standing on the lawn during battle. */
public class PlacedPlant {
    private final PlantType type;
    private final int x;
    private final int y;
    private int health;
    private final int maxHealth;
    private int actionCooldownTicks;
    private boolean sunPending;
    private int growthStage = 1;
    private int fuseTicks = -1;
    private boolean protectedSeed;

    public PlacedPlant(PlantType type, int x, int y, int maxHealth) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.health = maxHealth;
        this.maxHealth = maxHealth;
    }

    public PlantType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getActionCooldownTicks() {
        return actionCooldownTicks;
    }

    public void setActionCooldownTicks(int actionCooldownTicks) {
        this.actionCooldownTicks = actionCooldownTicks;
    }

    /** True while a produced sun sits uncollected on this plant (blocks production). */
    public boolean isSunPending() {
        return sunPending;
    }

    public void setSunPending(boolean sunPending) {
        this.sunPending = sunPending;
    }

    /** Growth stage for wrampup plants like Sun-shroom*/
    public int getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(int growthStage) {
        this.growthStage = growthStage;
    }

    /** Fuse for non-trap explosives; -1 means no fuse running. */
    public int getFuseTicks() {
        return fuseTicks;
    }

    public void setFuseTicks(int fuseTicks) {
        this.fuseTicks = fuseTicks;
    }

    /** Save Our Seeds: losing a protected plant loses the level. */
    public boolean isProtectedSeed() {
        return protectedSeed;
    }

    public void setProtectedSeed(boolean protectedSeed) {
        this.protectedSeed = protectedSeed;
    }
}
