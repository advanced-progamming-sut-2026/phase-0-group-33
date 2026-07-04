package models.entities.zombie;

import models.game.PlacedPlant;

import java.util.ArrayList;
import java.util.List;


public class ZombieBattleState {
    private int stolenSun;
    private int stealTicksLeft;
    private boolean laserFired;
    private int dynamiteTicks = -1;
    private boolean reversed;
    private boolean charging;
    private boolean raging;
    private boolean spinning;
    private int ticksSinceShotAt = 999;
    private boolean torchLit = true;
    private boolean hypnotized;
    private int iceHealth;
    private int abilityCooldown;
    private boolean impThrown;
    private boolean sunProducer;
    private int spawnTick;
    private int lastSliderColumn = -1;
    private final List<PlacedPlant> sheepPlants = new ArrayList<>();

    public int getStolenSun() {
        return stolenSun;
    }

    public void addStolenSun(int amount) {
        this.stolenSun += amount;
    }

    public void setStolenSun(int stolenSun) {
        this.stolenSun = stolenSun;
    }

    public int getStealTicksLeft() {
        return stealTicksLeft;
    }

    public void setStealTicksLeft(int stealTicksLeft) {
        this.stealTicksLeft = stealTicksLeft;
    }

    public boolean isLaserFired() {
        return laserFired;
    }

    public void setLaserFired(boolean laserFired) {
        this.laserFired = laserFired;
    }

    public int getDynamiteTicks() {
        return dynamiteTicks;
    }

    public void setDynamiteTicks(int dynamiteTicks) {
        this.dynamiteTicks = dynamiteTicks;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public boolean isCharging() {
        return charging;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
    }

    public boolean isRaging() {
        return raging;
    }

    public void setRaging(boolean raging) {
        this.raging = raging;
    }

    public boolean isSpinning() {
        return spinning;
    }

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
    }

    public int getTicksSinceShotAt() {
        return ticksSinceShotAt;
    }

    public void setTicksSinceShotAt(int ticksSinceShotAt) {
        this.ticksSinceShotAt = ticksSinceShotAt;
    }

    public boolean isTorchLit() {
        return torchLit;
    }

    public void setTorchLit(boolean torchLit) {
        this.torchLit = torchLit;
    }

    public boolean isHypnotized() {
        return hypnotized;
    }

    public void setHypnotized(boolean hypnotized) {
        this.hypnotized = hypnotized;
    }

    public int getIceHealth() {
        return iceHealth;
    }

    public void setIceHealth(int iceHealth) {
        this.iceHealth = iceHealth;
    }

    public int getAbilityCooldown() {
        return abilityCooldown;
    }

    public void setAbilityCooldown(int abilityCooldown) {
        this.abilityCooldown = abilityCooldown;
    }

    public boolean isImpThrown() {
        return impThrown;
    }

    public void setImpThrown(boolean impThrown) {
        this.impThrown = impThrown;
    }

    public boolean isSunProducer() {
        return sunProducer;
    }

    public void setSunProducer(boolean sunProducer) {
        this.sunProducer = sunProducer;
    }

    public List<PlacedPlant> getSheepPlants() {
        return sheepPlants;
    }

    public int getSpawnTick() {
        return spawnTick;
    }

    public void setSpawnTick(int spawnTick) {
        this.spawnTick = spawnTick;
    }

    /** Last slider column this zombie was pushed from (prevents slide loops). */
    public int getLastSliderColumn() {
        return lastSliderColumn;
    }

    public void setLastSliderColumn(int lastSliderColumn) {
        this.lastSliderColumn = lastSliderColumn;
    }
}
