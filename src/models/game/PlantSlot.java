package models.game;

import models.entities.plant.PlantType;


public class PlantSlot {
    private final PlantType type;
    private int cooldownTicks;
    private boolean boosted;
    private boolean singleUse;

    public PlantSlot(PlantType type) {
        this.type = type;
    }

    public PlantType getType() {
        return type;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
    }

    public boolean isReady() {
        return cooldownTicks <= 0;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
    }

    /** Conveyor-belt slots disappear after one use. */
    public boolean isSingleUse() {
        return singleUse;
    }

    public void setSingleUse(boolean singleUse) {
        this.singleUse = singleUse;
    }
}
