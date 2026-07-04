package models.game;

import models.entities.plant.PlantType;

/** A plant standing on the lawn during battle. */
public class PlacedPlant {
    private final PlantType type;
    private int x;
    private int y;
    private int health;
    private final int maxHealth;
    private int actionCooldownTicks;
    private boolean sunPending;
    private int growthStage = 1;
    private int fuseTicks = -1;
    private boolean protectedSeed;
    private int iceHits;
    private int iceHealth;
    private int freezeLevel;
    private int octopusHealth;
    private boolean sheep;
    private int stackCount = 1;
    private int pumpkinHealth;
    private int armTicks;

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

    /** Plants can be dragged around (Fisherman's hook, Beghouled swaps). */
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    /**
     * True while a produced sun sits uncollected on this plant (blocks production).
     */
    public boolean isSunPending() {
        return sunPending;
    }

    public void setSunPending(boolean sunPending) {
        this.sunPending = sunPending;
    }

    /** Growth stage for wramp-up plants such as Sun-shroom (1..3). */
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

    /** Hunter zombie ice hits; the third hit freezes the plant (doc). */
    public int getIceHits() {
        return iceHits;
    }

    public void setIceHits(int iceHits) {
        this.iceHits = iceHits;
    }

    /** Remaining HP of the ice encasing this plant; 0 means not frozen. */
    public int getIceHealth() {
        return iceHealth;
    }

    public void setIceHealth(int iceHealth) {
        this.iceHealth = iceHealth;
    }

    /** Frostbite ice-wind freeze level (doc: the third level freezes the plant). */
    public int getFreezeLevel() {
        return freezeLevel;
    }

    public void setFreezeLevel(int freezeLevel) {
        this.freezeLevel = freezeLevel;
    }

    /** Remaining HP of an octopus stuck on this plant; 0 means none. */
    public int getOctopusHealth() {
        return octopusHealth;
    }

    public void setOctopusHealth(int octopusHealth) {
        this.octopusHealth = octopusHealth;
    }

    /** Wizard zombie transformation; sheep plants do nothing and are not eaten. */
    public boolean isSheep() {
        return sheep;
    }

    public void setSheep(boolean sheep) {
        this.sheep = sheep;
    }

    /** Pea Pod stacking count (doc: stack tag, up to 5 heads). */
    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int stackCount) {
        this.stackCount = stackCount;
    }

    /** Pumpkin shell HP protecting the plant underneath (doc: stack tag). */
    public int getPumpkinHealth() {
        return pumpkinHealth;
    }

    public void setPumpkinHealth(int pumpkinHealth) {
        this.pumpkinHealth = pumpkinHealth;
    }

    /** Charge-tag warm-up ticks before the plant may act (doc: charge tag). */
    public int getArmTicks() {
        return armTicks;
    }

    public void setArmTicks(int armTicks) {
        this.armTicks = armTicks;
    }

    /** True when the plant cannot act (frozen, under an octopus, or a sheep). */
    public boolean isDisabled() {
        return iceHealth > 0 || octopusHealth > 0 || sheep;
    }
}
