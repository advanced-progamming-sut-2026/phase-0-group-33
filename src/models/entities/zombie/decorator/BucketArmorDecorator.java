package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

public class BucketArmorDecorator extends ZombieDecorator {
    private int bucketHealth;

    public BucketArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.bucketHealth = 1100;
    }

    @Override
    public void takeDamage(int damage) {
        if (bucketHealth > 0) {
            bucketHealth -= damage;
            if (bucketHealth < 0) {
                int remaining = -bucketHealth;
                bucketHealth = 0;
                super.takeDamage(remaining);
            }
        } else {
            super.takeDamage(damage);
        }
    }

    public int getBucketHealth() {
        return bucketHealth;
    }
}