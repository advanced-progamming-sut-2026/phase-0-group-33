package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConeArmorDecorator extends ZombieDecorator {
    private int coneHealth;

    public ConeArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.coneHealth = 370;
    }

    @Override
    public Map<String, Integer> getArmorInfo() {
        Map<String, Integer> info = new LinkedHashMap<>();
        if (coneHealth > 0) {
            info.put("cone", coneHealth);
        }
        info.putAll(super.getArmorInfo());
        return info;
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
