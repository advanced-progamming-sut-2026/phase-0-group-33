package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

public class ConeArmorDecorator extends ZombieDecorator {
    private int coneHealth;

    public ConeArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.coneHealth = 370;
    }

    @Override
    public void takeDamage(int damage) {
        if (coneHealth > 0) {
            coneHealth -= damage;
            if (coneHealth < 0) {
                int remaining = -coneHealth;
                coneHealth = 0;
                super.takeDamage(remaining);
            }
        } else {
            super.takeDamage(damage);
        }
    }

    public int getConeHealth() {
        return coneHealth;
    }
}