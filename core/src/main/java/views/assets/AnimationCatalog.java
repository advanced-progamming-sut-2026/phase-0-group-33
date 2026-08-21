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
        ABILITY.put("Wizard", "sheep");

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
        ATTACK_VARIANTS.put("Bowling Bulb", new String[] {"special", "special2", "special3"});
        ATTACK_VARIANTS.put("Chomper", new String[] {"bite", "special"});
        ATTACK_VARIANTS.put("Magnet-shroom", new String[] {"catch", "busy"});
        ATTACK_VARIANTS.put("Tangle Kelp", new String[] {"attack_submerge", "attack"});
        ATTACK_VARIANTS.put("Pea Pod", new String[] {"attack", "attack 2", "attack 3"});
        ATTACK_VARIANTS.put("Cactus", new String[] {"attack", "attack_stretch"});
    }

    public static String[] attackVariants(models.entities.plant.PlantType type) {
        return ATTACK_VARIANTS.get(type.getName());
    }

    private static final ObjectMap<String, String[]> PROJECTILE = new ObjectMap<>();

    static {
        PROJECTILE.put("Peashooter", new String[] {"T_PEA_PROJECTILE", "animation"});
        PROJECTILE.put("Repeater", new String[] {"T_PEA_PROJECTILE", "animation"});
        PROJECTILE.put("Threepeater", new String[] {"T_PEA_PROJECTILE", "animation"});
        PROJECTILE.put("Split Pea", new String[] {"T_PEA_PROJECTILE", "animation"});
        PROJECTILE.put("Pea Pod", new String[] {"T_PEA_PROJECTILE", "animation"});
        PROJECTILE.put("Mega Gatling Pea", new String[] {"T_PEA_PROJECTILE", "animation"});
        PROJECTILE.put("Snow Pea", new String[] {"T_SNOW_PEA", "animation"});
        PROJECTILE.put("Fire Peashooter", new String[] {"T_FIRE_PEA", "animation"});
        PROJECTILE.put("Goo Peashooter",
                new String[] {"GOOPEASHOOTER_PROJECTILES", "projectile_t1"});
        PROJECTILE.put("Kernel-pult", new String[] {"T_KERNALPULT_PROJECTILE", "animation"});
        PROJECTILE.put("Cabbage-pult", new String[] {"T_CABBAGEPULT_PROJECTILE", "animation"});
        PROJECTILE.put("Melon-pult", new String[] {"T_MELON_PROJECTILE", "animation"});
        PROJECTILE.put("Winter Melon", new String[] {"T_WINTERMELON_PROJECTILE", "animation"});
        PROJECTILE.put("Pepper-pult", new String[] {"T_PEPPERPULT_PROJECTILE", "animation"});
        PROJECTILE.put("Arma-mint", new String[] {"ARMAMINT_PROJECTILE", "animation"});
        PROJECTILE.put("Cactus", new String[] {"T_CACTUS_PROJECTILE", "idle"});
        PROJECTILE.put("Citron", new String[] {"T_CITRON_CITRUS_ORB", "Citron_Citrus_Orb"});
        PROJECTILE.put("Puff-shroom", new String[] {"T_PUFFSHROOM_PROJECTILE", "animation"});
        PROJECTILE.put("Sea-shroom", new String[] {"SEASHROOM_PROJECTILE", "animation"});
        PROJECTILE.put("Starfruit", new String[] {"T_STARFRUIT_PROJECTILE", "animation"});
        PROJECTILE.put("Bowling Bulb", new String[] {"BOWLINGBULB_PROJECTILE1", "animation"});
        PROJECTILE.put("Rotobaga", new String[] {"T_ROTORUTABAGA_PROJECTILE1", "animation"});
        PROJECTILE.put("Cat-tail", new String[] {"T_SPORESHROOM_PROJECTILE", "animation"});
        PROJECTILE.put("Fume-shroom", new String[] {"T_SPORESHROOM_PROJECTILE", "animation"});
        PROJECTILE.put("Sun-shroom", new String[] {"T_PUFFSHROOM_PROJECTILE", "animation"});
    }

    public static String[] projectile(models.entities.plant.PlantType type) {
        return PROJECTILE.get(type.getName());
    }

    private static final ObjectMap<String, String[]> IMPACT = new ObjectMap<>();

    static {
        IMPACT.put("Peashooter", new String[] {"T_SPLAT_PEA", "animation"});
        IMPACT.put("Repeater", new String[] {"T_SPLAT_PEA", "animation"});
        IMPACT.put("Threepeater", new String[] {"T_SPLAT_PEA", "animation"});
        IMPACT.put("Split Pea", new String[] {"T_SPLAT_PEA", "animation"});
        IMPACT.put("Pea Pod", new String[] {"T_SPLAT_PEA", "animation"});
        IMPACT.put("Mega Gatling Pea", new String[] {"T_SPLAT_PEA", "animation"});
        IMPACT.put("Snow Pea", new String[] {"T_SPLAT_SNOW_PEA", "animation"});
        IMPACT.put("Fire Peashooter", new String[] {"T_SPLAT_FIRE_PEA", "animation"});
        IMPACT.put("Goo Peashooter", new String[] {"GOOPEASHOOTER_PROJECTILES", "hit_t1"});
        IMPACT.put("Kernel-pult", new String[] {"SPLAT_KERNALPULT_KERNAL", "animation"});
        IMPACT.put("Cabbage-pult", new String[] {"SPLAT_CABBAGEPULT", "animation"});
        IMPACT.put("Melon-pult", new String[] {"T_SPLAT_MELONPULT", "animation"});
        IMPACT.put("Winter Melon", new String[] {"T_SPLAT_WINTERMELON", "animation"});
        IMPACT.put("Pepper-pult", new String[] {"PEPPERPULT_PROJECTILE_SPLAT", "animation"});
        IMPACT.put("Cactus", new String[] {"CACTUS_PROJECTILE_HIT", "animation"});
        IMPACT.put("Citron", new String[] {"CITRON_CITRUS_ORB_HIT", "animation"});
        IMPACT.put("Rotobaga", new String[] {"ROTORUTABAGA_PROJECTILE_HIT", "animation"});
        IMPACT.put("Arma-mint", new String[] {"ARMAMINT_EXPLOSION", "animation"});
        IMPACT.put("Grapeshot", new String[] {"GRAPESHOT_HIT", "animation"});
        IMPACT.put("Rotobaga", new String[] {"T_ROTORUTABAGA_PROJECTILE_HIT", "animation"});
        IMPACT.put("Starfruit", new String[] {"T_STARFRUIT_PROJECTILE_HIT", "idle"});
        IMPACT.put("Puff-shroom", new String[] {"T_PUFFSHROOM_HIT", "animation"});
        IMPACT.put("Fume-shroom", new String[] {"FUMESHROOM_BUBBLES_HIT", "animation"});
        IMPACT.put("Sea-shroom", new String[] {"SEASHROOM_PROJECTILE", "animation2"});
    }

    public static String[] impact(models.entities.plant.PlantType type) {
        return IMPACT.get(type.getName());
    }

    public static String torchwoodHit() {
        return "TORCHWOOD_HIT_EFFECTS";
    }

    public static String graveDirt() {
        return "GRAVEBUSTER_DIRT";
    }

    private static final ObjectMap<String, String[]> DETONATION = new ObjectMap<>();

    static {
        DETONATION.put("Cherry Bomb", new String[] {"CHERRYBOMB", "attack"});
        DETONATION.put("Grapeshot", new String[] {"GRAPESHOT", "attack"});
        DETONATION.put("Jalapeno", new String[] {"JALAPENO", "attack"});
        DETONATION.put("Doom-shroom", new String[] {"DOOMSHROOM", "stage3_explode"});
        DETONATION.put("Ice-shroom", new String[] {"ICESHROOM", "attack"});
        DETONATION.put("Potato Mine", new String[] {"POTATOMINE_EXPLOSION", "animation"});
        DETONATION.put("Primal Potato Mine",
                new String[] {"PRIMAL_POTATOMINE_EXPLOSION", "animation"});
        DETONATION.put("Squash", new String[] {"SQUASH", "jump_down_left"});
        DETONATION.put("Iceberg Lettuce", new String[] {"ICEBURG", "attack"});
        DETONATION.put("Explode-o-nut", new String[] {"EXPLODEONUT", "plantfood"});
        DETONATION.put("Tangle Kelp", new String[] {"TANGLEKELP", "attack_submerge"});
    }

    public static String[] detonation(models.entities.plant.PlantType type) {
        return DETONATION.get(type.getName());
    }

    public static String blastRear() {
        return "CHERRYBOMB_EXPLOSION_REAR";
    }

    public static String stormTop(boolean icy) {
        return icy ? "SNOWSTORM_TOP" : "SANDSTORM_TOP";
    }

    public static String stormRear(boolean icy) {
        return icy ? "SNOWSTORM_REAR" : "SANDSTORM_REAR";
    }

    public static String chillWind() {
        return "FROSTBITE_CHILL_WIND";
    }

    public static String laneFire() {
        return "JALAPENO_FIRE";
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

    public static String[] abilityClips(ZombieType type) {
        switch (type) {
            case RA:
            case TURQUOISE:
                return new String[] {"power_up", "power", "walk", "idle"};
            case WIZARD:
                return new String[] {"sheep", "walk", "idle"};
            case FISHERMAN:
                return new String[] {"cast", "cast_loop", "reel", "idle"};
            case OCTOPUS:
                return new String[] {"toss", "idle4", "idle5", "walk", "idle"};
            default:
                String single = ABILITY.get(type.getName());
                return single == null ? null : new String[] {single, "walk", "idle"};
        }
    }

    public static String[] biteClips(ZombieType type, boolean alternate) {
        switch (type) {
            case GARGANTUAR:
                return alternate
                        ? new String[] {"smash_right", "smash_left", "eat", "idle"}
                        : new String[] {"smash_left", "smash_right", "eat", "idle"};
            case PIANO:
                return alternate
                        ? new String[] {"smash_right", "smash_left", "eat", "idle"}
                        : new String[] {"smash_left", "eat", "idle"};
            case ALLSTAR:
                return new String[] {"kick", "tackle", "eat", "idle"};
            default:
                String single = BITE.get(type.getName());
                return single == null ? null : new String[] {single, "eat", "idle"};
        }
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
