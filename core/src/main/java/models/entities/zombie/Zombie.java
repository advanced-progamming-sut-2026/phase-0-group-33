package models.entities.zombie;

import models.map.Position;

import java.util.LinkedHashMap;
import java.util.Map;

public class Zombie {
    protected ZombieType type;
    protected Position position;
    protected int health;
    protected double speed;

    private boolean glowing;
    private int chilledTicks;
    private int frozenTicks;
    private int spawnWave;
    private final ZombieBattleState battle = new ZombieBattleState();

    public Zombie(ZombieType type, Position position, int health, double speed) {
        this.type = type;
        this.position = position;
        this.health = health;
        this.speed = speed;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public void damageHealthDirectly(int damage) {
        health -= damage;
    }

    public boolean stripMetallicArmor() {
        return false;
    }

    public ZombieBattleState getBattle() {
        return battle;
    }

    public Map<String, Integer> getArmorInfo() {
        return new LinkedHashMap<>();
    }

    public boolean isDead() {
        return getHealth() <= 0;
    }

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

}
