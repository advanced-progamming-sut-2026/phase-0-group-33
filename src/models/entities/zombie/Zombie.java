package models.entities.zombie;

import models.entities.plant.Plant;
import models.map.Position;

import java.util.LinkedHashMap;
import java.util.Map;

public class Zombie {
    protected ZombieType type;
    protected Position position;
    protected int health;
    protected double speed;
    protected ZombieState state;

    // Battle-time attributes (owned by the outermost decorator instance).
    private boolean glowing;
    private int chilledTicks;
    private int frozenTicks;
    private int spawnWave;

    public Zombie(ZombieType type, Position position, int health, double speed) {
        this.type = type;
        this.position = position;
        this.health = health;
        this.speed = speed;
    }

    public void move() {
    }

    public void attack(Plant plant) {
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    /** Armor pieces still intact, in outermost-first order (for the zombies info output). */
    public Map<String, Integer> getArmorInfo() {
        return new LinkedHashMap<>();
    }

    public boolean isDead() {
        return getHealth() <= 0 && totalArmor() <= 0;
    }

    /** Total remaining armor hit points across all decorators. */
    public int totalArmor() {
        int total = 0;
        for (int armorHp : getArmorInfo().values()) {
            total += armorHp;
        }
        return total;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public int getChilledTicks() {
        return chilledTicks;
    }

    public void setChilledTicks(int chilledTicks) {
        this.chilledTicks = chilledTicks;
    }

    public int getFrozenTicks() {
        return frozenTicks;
    }

    public void setFrozenTicks(int frozenTicks) {
        this.frozenTicks = frozenTicks;
    }

    public int getSpawnWave() {
        return spawnWave;
    }

    public void setSpawnWave(int spawnWave) {
        this.spawnWave = spawnWave;
    }

    public ZombieType getType() {
        return type;
    }

    public void setType(ZombieType type) {
        this.type = type;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public ZombieState getState() {
        return state;
    }

    public void setState(ZombieState state) {
        this.state = state;
    }
}