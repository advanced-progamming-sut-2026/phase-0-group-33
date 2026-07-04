package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

public class CrownArmorDecorator extends ZombieDecorator {
    private int crownHealth;
    private int shoulderArmorHealth;

    public CrownArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.crownHealth = 800;
        this.shoulderArmorHealth = 800;
    }

    @Override
    public void takeDamage(int damage) {
        int remainingDamage = damage;
        if (crownHealth > 0) {
            int absorbed = Math.min(crownHealth, remainingDamage);
            crownHealth -= absorbed;
            remainingDamage -= absorbed;
        }
        if (shoulderArmorHealth > 0 && remainingDamage > 0) {
            int absorbed = Math.min(shoulderArmorHealth, remainingDamage);
            shoulderArmorHealth -= absorbed;
            remainingDamage -= absorbed;
        }
        if (remainingDamage > 0) {
            super.takeDamage(remainingDamage);
        }
    }

    public int getCrownHealth() {
        return crownHealth;
    }

    public int getShoulderArmorHealth() {
        return shoulderArmorHealth;
    }
}