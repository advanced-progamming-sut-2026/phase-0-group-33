package models.game;

import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;

/** Name lookup for plants and zombies, tolerant of case, spaces and dashes. */
public final class Names {

    private Names() {
    }

    public static PlantType plant(String name) {
        if (name == null) {
            return null;
        }
        for (PlantType type : PlantType.values()) {
            if (normalize(type.getName()).equals(normalize(name))) {
                return type;
            }
        }
        return null;
    }

    public static ZombieType zombie(String name) {
        if (name == null) {
            return null;
        }
        for (ZombieType type : ZombieType.values()) {
            if (normalize(type.getName()).equals(normalize(name))) {
                return type;
            }
        }
        return null;
    }

    public static String normalize(String text) {
        return text.replaceAll("[\\s_-]", "").toLowerCase();
    }
}
