package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.map.Position;

import java.util.Map;

public abstract class ZombieDecorator extends Zombie {
    protected Zombie decoratedZombie;

    public ZombieDecorator(Zombie decoratedZombie) {
        super(decoratedZombie.getType(), decoratedZombie.getPosition(),
                decoratedZombie.getHealth(), decoratedZombie.getSpeed());
        this.decoratedZombie = decoratedZombie;
    }

    @Override
    public void move() {
        decoratedZombie.move();
    }

    @Override
    public void attack(Plant plant) {
        decoratedZombie.attack(plant);
    }

    @Override
    public void takeDamage(int damage) {
        decoratedZombie.takeDamage(damage);
    }

    @Override
    public ZombieType getType() {
        return decoratedZombie.getType();
    }

    @Override
    public Position getPosition() {
        return decoratedZombie.getPosition();
    }

    @Override
    public void setPosition(Position position) {
        decoratedZombie.setPosition(position);
    }

    @Override
    public int getHealth() {
        return decoratedZombie.getHealth();
    }

    @Override
    public void setHealth(int health) {
        decoratedZombie.setHealth(health);
    }

    @Override
    public double getSpeed() {
        return decoratedZombie.getSpeed();
    }

    @Override
    public void setSpeed(double speed) {
        decoratedZombie.setSpeed(speed);
    }

    @Override
    public ZombieState getState() {
        return decoratedZombie.getState();
    }

    @Override
    public void setState(ZombieState state) {
        decoratedZombie.setState(state);
    }

    @Override
    public Map<String, Integer> getArmorInfo() {
        return decoratedZombie.getArmorInfo();
    }
}