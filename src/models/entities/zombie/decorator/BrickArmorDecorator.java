package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

import java.util.LinkedHashMap;
import java.util.Map;

public class BrickArmorDecorator extends ZombieDecorator {
    private int blockHealth;

    public BrickArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.blockHealth = 2200;
    }

    @Override
    public void takeDamage(int damage) {
        if (blockHealth > 0) {
            blockHealth -= damage;
            if (blockHealth < 0) {
                int remaining = -blockHealth;
                blockHealth = 0;
                super.takeDamage(remaining);
            }
        } else {
            super.takeDamage(damage);
        }
    }

    @Override
    public Map<String, Integer> getArmorInfo() {
        Map<String, Integer> info = new LinkedHashMap<>();
        if (blockHealth > 0) {
            info.put("block", blockHealth);
        }
        info.putAll(super.getArmorInfo());
        return info;
    }

    public int getBlockHealth() {
        return blockHealth;
    }
}
