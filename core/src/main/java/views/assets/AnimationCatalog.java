package views.assets;

import com.badlogic.gdx.utils.ObjectMap;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;

public final class AnimationCatalog {

    private static final ObjectMap<String, String> PLANTS = new ObjectMap<>();
    private static final ObjectMap<String, String> ZOMBIES = new ObjectMap<>();
    private static final ObjectMap<String, String[]> ARMOR = new ObjectMap<>();

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
        PLANTS.put("Cat-tail", "HOMINGTHISTLE");

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

        ARMOR.put("Cone Head", new String[] {
            "zombie_armor_cone_norm", "zombie_armor_cone_damage_01", "zombie_armor_cone_damage_02"});
        ARMOR.put("Bucket Head", new String[] {
            "zombie_armor_bucket_norm", "zombie_armor_bucket_damage_01",
            "zombie_armor_bucket_damage_02"});
        ARMOR.put("Brick Head", new String[] {
            "zombie_armor_brick_norm", "zombie_armor_brick_damage_01",
            "zombie_armor_brick_damage_02"});
        ARMOR.put("Knight", new String[] {
            "zombie_shoulder_armor_norm", "zombie_shoulder_armor_damage_01",
            "zombie_shoulder_armor_damage_02"});
        ARMOR.put("Newspaper", new String[] {
            "_zombie_newspaper", "_zombie_newspaper_dmg1", "_zombie_newspaper_dmg2"});
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

    private static final ObjectMap<String, models.entities.plant.PlantType> ZOMBOTANY =
            new ObjectMap<>();
    private static final ObjectMap<String, String> RIDE = new ObjectMap<>();
    private static final ObjectMap<String, String> ABILITY = new ObjectMap<>();
    private static final ObjectMap<String, String> BITE = new ObjectMap<>();

    static {
        ABILITY.put("Ra", "power");
        ABILITY.put("Tomb Raiser", "power");
        ABILITY.put("Hunter", "throw");
        ABILITY.put("Octopus", "toss");
        ABILITY.put("Explorer", "power");
        ABILITY.put("Turquoise", "power");
        ABILITY.put("Fisherman", "cast");

        RIDE.put("Surfer", "walk_board");
        RIDE.put("Piano", "idle");

        ZOMBOTANY.put("Peashooter Zombie", models.entities.plant.PlantType.PEASHOOTER);
        ZOMBOTANY.put("Wall-nut Zombie", models.entities.plant.PlantType.WALL_NUT);
        ZOMBOTANY.put("Jalapeno Zombie", models.entities.plant.PlantType.JALAPENO);
        ZOMBOTANY.put("Squash Zombie", models.entities.plant.PlantType.SQUASH);

        BITE.put("Gargantuar", "smash_left");
        BITE.put("All Star", "kick");
        BITE.put("Piano", "smash_left");
    }

    public static models.entities.plant.PlantType zombotanyHead(ZombieType type) {
        return ZOMBOTANY.get(type.getName());
    }

    private static final ObjectMap<String, String[]> ATTACK_VARIANTS = new ObjectMap<>();

    static {
        ATTACK_VARIANTS.put("Bonk Choy",
                new String[] {"attack", "attack2", "attack3", "attack4", "attack5"});
        ATTACK_VARIANTS.put("Wasabi Whip",
                new String[] {"attack", "attack2", "attack3", "attack4", "attack5"});
        ATTACK_VARIANTS.put("Split Pea", new String[] {"attack", "attack2", "attack3"});
        ATTACK_VARIANTS.put("Kernel-pult", new String[] {"attack", "attack2"});
        ATTACK_VARIANTS.put("Grapeshot", new String[] {"attack", "attack_t2", "attack_t3"});
        ATTACK_VARIANTS.put("Bowling Bulb", new String[] {"attack", "special2", "special3"});
        ATTACK_VARIANTS.put("Chomper", new String[] {"bite", "attack"});
        ATTACK_VARIANTS.put("Magnet-shroom", new String[] {"catch", "busy"});
        ATTACK_VARIANTS.put("Tangle Kelp", new String[] {"attack_submerge", "attack"});
        ATTACK_VARIANTS.put("Pea Pod", new String[] {"attack", "attack 2", "attack 3"});
        ATTACK_VARIANTS.put("Cactus", new String[] {"attack", "attack_stretch"});
    }

    public static String[] attackVariants(models.entities.plant.PlantType type) {
        return ATTACK_VARIANTS.get(type.getName());
    }

    public static String breakClip(ZombieType type) {
        switch (type) {
            case NEWSPAPER:
                return "newspaper_defeat";
            case PHARAOH:
                return "break_power";
            case JUGGLER:
                return "spinup";
            case ALLSTAR:
                return "tackle";
            default:
                return null;
        }
    }

    public static String landClip(ZombieType type) {
        switch (type) {
            case IMP:
            case IMP_DRAGON:
                return "land";
            case PROSPECTOR:
                return "land";
            default:
                return null;
        }
    }

    public static String[] stateClips(ZombieType type, boolean eating, boolean armoured,
                                      boolean charging, boolean spinning, boolean hopping) {
        String action = eating ? "eat" : "walk";
        switch (type) {
            case NEWSPAPER:
                return armoured
                        ? new String[] {action + "_newspaper", action, "idle"}
                        : new String[] {action, "idle"};
            case PHARAOH:
                return armoured
                        ? new String[] {action, "idle"}
                        : new String[] {action + "_norm", action, "idle"};
            case WEASEL_HOARDER:
                return armoured || !eating
                        ? new String[] {"tree_" + action, action, "idle"}
                        : new String[] {"tree_eat", "eat", "idle"};
            case BARREL_ROLLER:
                return new String[] {action + "2", action, "idle"};
            case ALLSTAR:
                return charging
                        ? new String[] {"run", "walk", "idle"}
                        : new String[] {action, "idle"};
            case SURFER:
                return charging
                        ? new String[] {"surf_idle", "walk_board", "walk", "idle"}
                        : new String[] {action, "idle"};
            case JUGGLER:
                return spinning
                        ? new String[] {"spin_walk", "spin", action, "idle"}
                        : new String[] {action, "idle"};
            case DODO:
                return hopping
                        ? new String[] {"fly_loop", "fly_start", action, "idle"}
                        : new String[] {action, "idle"};
            case WEASEL:
                return eating
                        ? new String[] {"eat_loop", "eat_start", "eat", "idle"}
                        : new String[] {action, "idle"};
            case OCTOPUS:
                return new String[] {action, "idle"};
            default:
                return null;
        }
    }

    public static String rideClip(ZombieType type) {
        return RIDE.get(type.getName());
    }

    public static String abilityClip(ZombieType type) {
        return ABILITY.get(type.getName());
    }

    public static String biteClip(ZombieType type) {
        return BITE.get(type.getName());
    }

    public static String zomboss(models.entities.zombie.Zomboss.BossKind kind) {
        switch (kind) {
            case DRAGON:
                return "ZOMBIE_DARK_ZOMBOSS";
            case MAMMOTH:
                return "ZOMBIE_ICEAGE_ZOMBOSS";
            case SHARK:
                return "ZOMBIE_BEACH_ZOMBOSS";
            default:
                return "ZOMBIE_EGYPT_ZOMBOSS";
        }
    }

    public static String[] zombossClips(models.entities.zombie.Zomboss.BossKind kind,
                                        models.entities.zombie.Zomboss.Move move,
                                        boolean stunned) {
        if (stunned) {
            return new String[] {"stun_loop", "stun", "vulnerable_loop", "vulnerable", "idle"};
        }
        switch (kind) {
            case DRAGON:
                return dragonClips(move);
            case ROBOT:
                return robotClips(move);
            case MAMMOTH:
                return mammothClips(move);
            default:
                return sharkClips(move);
        }
    }

    private static String[] dragonClips(models.entities.zombie.Zomboss.Move move) {
        switch (move) {
            case BOMB:
                return new String[] {"fire_bomb_loop", "fire_bomb", "idle"};
            case BURN:
                return new String[] {"fire_attack_idle", "fire_attack", "idle"};
            case SUMMON:
                return new String[] {"summoning", "idle"};
            default:
                return new String[] {"idle"};
        }
    }

    private static String[] robotClips(models.entities.zombie.Zomboss.Move move) {
        switch (move) {
            case MISSILE:
                return new String[] {"rocket_launch", "missile_start", "idle"};
            case CHARGE:
                return new String[] {"walk_forward", "stomp", "idle"};
            case SUMMON:
                return new String[] {"zombie_portal_loop", "zombie_portal_start", "idle"};
            default:
                return new String[] {"idle"};
        }
    }

    private static String[] mammothClips(models.entities.zombie.Zomboss.Move move) {
        switch (move) {
            case SLINGSHOT:
                return new String[] {"slingshot", "idle"};
            case WIND:
                return new String[] {"wind_2", "wind_1", "idle"};
            case GLACIER:
                return new String[] {"glacier_column_3", "glacier_column_1", "idle"};
            default:
                return new String[] {"idle"};
        }
    }

    private static String[] sharkClips(models.entities.zombie.Zomboss.Move move) {
        switch (move) {
            case TURBINE:
                return new String[] {"suction_loop", "suction_on", "idle"};
            case SPAWN_SHARK:
            case SUMMON:
                return new String[] {"spawn", "emerge", "idle"};
            default:
                return new String[] {"idle"};
        }
    }

    public static String mount(ZombieType type) {
        return type == ZombieType.PIANO ? "PIANO" : null;
    }

    public static String[] mountClips(ZombieType type) {
        return new String[] {"idle", "play"};
    }

    public static String pushed(models.game.PushedObject.Kind kind) {
        switch (kind) {
            case BARREL:
                return "ZOMBIE_PIRATE_BARREL_PUSHER_BARREL";
            case ARCADE_MACHINE:
                return "80S_ARCADE_CABINET";
            default:
                return "ZOMBOSS_GLACIER_BLOCK";
        }
    }

    public static String pushedClip(models.game.PushedObject.Kind kind) {
        switch (kind) {
            case BARREL:
                return "roll";
            case ARCADE_MACHINE:
                return "idle";
            default:
                return "idle";
        }
    }

    public static String fireTile() {
        return "FIRETILE";
    }

    public static String sun() {
        return "SUN";
    }

    public static String mower(String chapterName) {
        String normalized = chapterName == null ? ""
                : chapterName.replaceAll("[^A-Za-z]", "").toLowerCase();
        switch (normalized) {
            case "egypt":
                return "MOWER_EGYPT";
            case "frostbite":
                return "MOWER_ICEAGE";
            case "waveybeach":
                return "MOWER_BEACH";
            case "darkages":
                return "MOWER_DARK";
            default:
                return "MOWER_TUTORIAL";
        }
    }

    public static String[] armorParts(ZombieType type) {
        return ARMOR.get(type.getName());
    }

    private static String normalize(String displayName) {
        return displayName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
