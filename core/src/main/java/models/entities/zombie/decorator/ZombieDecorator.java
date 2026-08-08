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
    public void takeDamage(int damage) {
        decoratedZombie.takeDamage(damage);
    }

    @Override
    public void damageHealthDirectly(int damage) {
        decoratedZombie.damageHealthDirectly(damage);
    }

    @Override
    public boolean stripMetallicArmor() {
        return decoratedZombie.stripMetallicArmor();
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
    public Map<String, Integer> getArmorInfo() {
        return decoratedZombie.getArmorInfo();
    }
}
