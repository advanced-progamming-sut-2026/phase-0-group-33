package models.entities.zombie.decorator;

import models.entities.zombie.Zombie;

import java.util.LinkedHashMap;
import java.util.Map;

public class CrownArmorDecorator extends ZombieDecorator {
    // Doc (zombies section): the knight's crown and shoulder armor each have 1600
    // HP.
    private int crownHealth;
    private int shoulderArmorHealth;

    public CrownArmorDecorator(Zombie decoratedZombie) {
        super(decoratedZombie);
        this.crownHealth = 1600;
        this.shoulderArmorHealth = 1600;
    }

    @Override
    public Map<String, Integer> getArmorInfo() {
        Map<String, Integer> info = new LinkedHashMap<>();
        if (crownHealth > 0) {
            info.put("crown", crownHealth);
        }
        if (shoulderArmorHealth > 0) {
            info.put("shoulderArmor", shoulderArmorHealth);
        }
        info.putAll(super.getArmorInfo());
        return info;
    }

    /** Helmet and shoulder armor are metal; the magnet-shroom can pull them off. */
    @Override
    public boolean stripMetallicArmor() {
        if (crownHealth > 0 || shoulderArmorHealth > 0) {
            crownHealth = 0;
            shoulderArmorHealth = 0;
            return true;
        }
        return super.stripMetallicArmor();
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