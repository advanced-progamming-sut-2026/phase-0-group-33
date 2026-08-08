package models.game;

import models.entities.plant.PlantType;

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

    public boolean isSunPending() {
        return sunPending;
    }

    public void setSunPending(boolean sunPending) {
        this.sunPending = sunPending;
    }

    public int getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(int growthStage) {
        this.growthStage = growthStage;
    }

    public int getFuseTicks() {
        return fuseTicks;
    }

    public void setFuseTicks(int fuseTicks) {
        this.fuseTicks = fuseTicks;
    }

    public boolean isProtectedSeed() {
        return protectedSeed;
    }

    public void setProtectedSeed(boolean protectedSeed) {
        this.protectedSeed = protectedSeed;
    }

    public int getIceHits() {
        return iceHits;
    }

    public void setIceHits(int iceHits) {
        this.iceHits = iceHits;
    }

    public int getIceHealth() {
        return iceHealth;
    }

    public void setIceHealth(int iceHealth) {
        this.iceHealth = iceHealth;
    }

    public int getFreezeLevel() {
        return freezeLevel;
    }

    public void setFreezeLevel(int freezeLevel) {
        this.freezeLevel = freezeLevel;
    }

    public int getOctopusHealth() {
        return octopusHealth;
    }

    public void setOctopusHealth(int octopusHealth) {
        this.octopusHealth = octopusHealth;
    }

    public boolean isSheep() {
        return sheep;
    }

    public void setSheep(boolean sheep) {
        this.sheep = sheep;
    }

    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int stackCount) {
        this.stackCount = stackCount;
    }

    public int getPumpkinHealth() {
        return pumpkinHealth;
    }

    public void setPumpkinHealth(int pumpkinHealth) {
        this.pumpkinHealth = pumpkinHealth;
    }

    public int getArmTicks() {
        return armTicks;
    }

    public void setArmTicks(int armTicks) {
        this.armTicks = armTicks;
    }

    public boolean isDisabled() {
        return iceHealth > 0 || octopusHealth > 0 || sheep;
    }
}
