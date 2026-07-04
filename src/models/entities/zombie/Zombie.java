package models.entities.zombie;

import models.entities.plant.Plant;
import models.map.Position;

public class Zombie {
    protected ZombieType type;
    protected Position position;
    protected int health;
    protected double speed;
    protected ZombieState state;

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