package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

import java.util.LinkedHashMap;
import java.util.Map;

public class BucketArmorDecorator extends ZombieDecorator {
    private int bucketHealth;

    public BucketArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.bucketHealth = 1100;
    }

    @Override
    public Map<String, Integer> getArmorInfo() {
        Map<String, Integer> info = new LinkedHashMap<>();
        if (bucketHealth > 0) {
            info.put("bucket", bucketHealth);
        }
        info.putAll(super.getArmorInfo());
        return info;
    }

    /**
     * The bucket is metal; the magnet-shroom can rip it off (doc: zombies chapter).
     */
    @Override
    public boolean stripMetallicArmor() {
        if (bucketHealth > 0) {
            bucketHealth = 0;
            return true;
        }
        return super.stripMetallicArmor();
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