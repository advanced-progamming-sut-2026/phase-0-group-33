package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewspaperArmorDecorator extends ZombieDecorator {
    private int newspaperHealth = 190;

    public NewspaperArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
    }

    @Override
    public void takeDamage(int damage) {
        if (newspaperHealth > 0) {
            newspaperHealth -= damage;
            if (newspaperHealth < 0) {
                int remaining = -newspaperHealth;
                newspaperHealth = 0;
                super.takeDamage(remaining);
            }
        } else {
            super.takeDamage(damage);
        }
    }

    @Override
    public Map<String, Integer> getArmorInfo() {
        Map<String, Integer> info = new LinkedHashMap<>();
        if (newspaperHealth > 0) {
            info.put("newspaper", newspaperHealth);
        }
        info.putAll(super.getArmorInfo());
        return info;
    }

    public boolean isDestroyed() {
        return newspaperHealth <= 0;
    }
}
