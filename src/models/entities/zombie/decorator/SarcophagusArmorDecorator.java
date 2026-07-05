package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

import java.util.LinkedHashMap;
import java.util.Map;

public class SarcophagusArmorDecorator extends ZombieDecorator {
    private int sarcophagusHealth;

    public SarcophagusArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.sarcophagusHealth = 1000;
    }

    @Override
    public void takeDamage(int damage) {
        if (sarcophagusHealth > 0) {
            sarcophagusHealth -= damage;
            if (sarcophagusHealth < 0) {
                int remaining = -sarcophagusHealth;
                sarcophagusHealth = 0;
                super.takeDamage(remaining);
            }
        } else {
            super.takeDamage(damage);
        }
    }

    @Override
    public Map<String, Integer> getArmorInfo() {
        Map<String, Integer> info = new LinkedHashMap<>();
        if (sarcophagusHealth > 0) {
            info.put("sarcophagus", sarcophagusHealth);
        }
        info.putAll(super.getArmorInfo());
        return info;
    }

    public int getSarcophagusHealth() {
        return sarcophagusHealth;
    }
}
