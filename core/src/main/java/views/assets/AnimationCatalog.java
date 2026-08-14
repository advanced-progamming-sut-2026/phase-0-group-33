package views.assets;

import com.badlogic.gdx.utils.ObjectMap;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;

public final class AnimationCatalog {

    private static final ObjectMap<String, String> PLANTS = new ObjectMap<>();
    private static final ObjectMap<String, String> ZOMBIES = new ObjectMap<>();
    private static final ObjectMap<String, String> ARMOR = new ObjectMap<>();

    static {
        PLANTS.put("Twin Sunflower", "SUNFLOWER_TWIN");
        PLANTS.put("Rotobaga", "ROTORUTABAGA");
        PLANTS.put("Mega Gatling Pea", "MEGAGATLING");
        PLANTS.put("Kernel-pult", "KERNALPULT");
        PLANTS.put("Iceberg Lettuce", "ICEBURG");
        PLANTS.put("Phat Beet", "PHATBEETS");
        PLANTS.put("Pierce-mint", "SPEARMINT");
        PLANTS.put("catTail-mint", "AILMINT");
        PLANTS.put("Goo Peashooter", "GOOPEASHOOTER");
        PLANTS.put("Primal Sunflower", "PRIMAL_SUNFLOWER");
        PLANTS.put("Primal Potato Mine", "PRIMAL_POTATOMINE");

        ZOMBIES.put("Normal", "ZOMBIE_TUTORIAL");
        ZOMBIES.put("Cone Head", "ZOMBIE_TUTORIAL");
        ZOMBIES.put("Bucket Head", "ZOMBIE_TUTORIAL");
        ZOMBIES.put("Brick Head", "ZOMBIE_TUTORIAL");
        ZOMBIES.put("Knight", "ZOMBIE_DARK_BASIC");
        ZOMBIES.put("Gargantuar", "GARGANTUAR");
        ZOMBIES.put("Imp", "ZOMBIE_TUTORIAL_IMP");
        ZOMBIES.put("Ra", "ZOMBIE_EGYPT_RA");
        ZOMBIES.put("Explorer", "ZOMBIE_EXPLORER");
        ZOMBIES.put("Tomb Raiser", "ZOMBIE_EGYPT_TOMBRAISER");
        ZOMBIES.put("Dodo", "ZOMBIE_ICEAGE_DODORIDER");
        ZOMBIES.put("Hunter", "ZOMBIE_ICEAGE_HUNTER");
        ZOMBIES.put("Troglobite", "ZOMBIE_ICEAGE_TROGLOBITE");
        ZOMBIES.put("Fisherman", "ZOMBIE_BEACH_FISHERMAN");
        ZOMBIES.put("Octopus", "ZOMBIE_BEACH_OCTOPUS");
        ZOMBIES.put("Snorkel", "ZOMBIE_BEACH_SNORKELER");
        ZOMBIES.put("Juggler", "ZOMBIE_DARK_JESTER");
        ZOMBIES.put("Wizard", "ZOMBIE_DARK_WIZARD");
        ZOMBIES.put("King", "ZOMBIE_DARK_KING");
        ZOMBIES.put("Imp Dragon", "ZOMBIE_DARK_IMP_DRAGON");
        ZOMBIES.put("All Star", "ZOMBIE_MODERN_ALLSTAR");
        ZOMBIES.put("Arcade", "ZOMBIE_80S_ARCADE");
        ZOMBIES.put("Umbrella", "ZOMBIE_LOSTCITY_JANE");
        ZOMBIES.put("Turquoise", "ZOMBIE_LOSTCITY_CRYSTALSKULL");
        ZOMBIES.put("Prospector", "ZOMBIE_PROSPECTOR");
        ZOMBIES.put("Piano", "ZOMBIE_PIANO");
        ZOMBIES.put("Newspaper", "ZOMBIE_MODERN_NEWSPAPER");
        ZOMBIES.put("Barrel Roller", "ZOMBIE_PIRATE_BARREL_PUSHER");
        ZOMBIES.put("Pharaoh", "ZOMBIE_EGYPT_SARCOPHAGUS");
        ZOMBIES.put("Camel", "ZOMBIE_EGYPT_CAMEL");
        ZOMBIES.put("Weasel Hoarder", "ZOMBIE_ICEAGE_WEASELHOARDER");
        ZOMBIES.put("Weasel", "ZOMBIE_ICEAGE_WEASEL");
        ZOMBIES.put("Surfer", "ZOMBIE_BEACH_SURFER");
        ZOMBIES.put("Fast Swimmer", "ZOMBIE_DUCKYTUBE_BASIC");
        ZOMBIES.put("Peashooter Zombie", "ZOMBIE_TUTORIAL");
        ZOMBIES.put("Wall-nut Zombie", "ZOMBIE_TUTORIAL");
        ZOMBIES.put("Jalapeno Zombie", "ZOMBIE_TUTORIAL");
        ZOMBIES.put("Squash Zombie", "ZOMBIE_TUTORIAL");

        ARMOR.put("Cone Head", "zombie_cone");
        ARMOR.put("Bucket Head", "zombie_bucket");
        ARMOR.put("Brick Head", "zombie_screendoor");
        ARMOR.put("Newspaper", "zombie_newspaper");
    }

    private AnimationCatalog() {
    }

    public static String plant(PlantType type) {
        String override = PLANTS.get(type.getName());
        return override == null ? normalize(type.getName()) : override;
    }

    public static String zombie(ZombieType type) {
        String mapped = ZOMBIES.get(type.getName());
        return mapped == null ? "ZOMBIE_TUTORIAL" : mapped;
    }

    public static String armorPart(ZombieType type) {
        return ARMOR.get(type.getName());
    }

    private static String normalize(String displayName) {
        return displayName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
