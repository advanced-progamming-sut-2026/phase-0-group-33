package views.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;

public final class Art {

    private static final String[] DARK_GRAVES = {
        "IMAGE_GRAVESTONES_DARK_NOOP_DARK_NOOP_132X160",
        "IMAGE_GRAVESTONES_DARK_NOOP_DARK_NOOP_132X156",
        "IMAGE_GRAVESTONES_DARK_NOOP_DARK_NOOP_93X89",
    };

    private static final String[] EGYPT_GRAVES = {
        "IMAGE_GRAVESTONES_EGYPT_HIEROGLYPH_EGYPT_HIEROGLYPH_118X148",
        "IMAGE_GRAVESTONES_EGYPT_HIEROGLYPH_EGYPT_HIEROGLYPH_113X145",
        "IMAGE_GRAVESTONES_EGYPT_HIEROGLYPH_EGYPT_HIEROGLYPH_110X145",
    };

    private static final String PLANT_PREFIX = "IMAGE_UI_PACKETS_";
    private static final String ZOMBIE_PREFIX = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_";
    private static final String FALLBACK_ZOMBIE = "TUTORIAL";

    private static final ObjectMap<String, String> PLANT_OVERRIDES = new ObjectMap<>();
    private static final ObjectMap<String, String> ZOMBIE_KEYS = new ObjectMap<>();

    static {
        PLANT_OVERRIDES.put("Goo Peashooter", "POISONPEASHOOTER");
        PLANT_OVERRIDES.put("Mega Gatling Pea", "MEGAGATLING");
        PLANT_OVERRIDES.put("Cherry Bomb", "CHERRY_BOMB");
        PLANT_OVERRIDES.put("Iceberg Lettuce", "ICEBURG");
        PLANT_OVERRIDES.put("Pierce-mint", "SPEARMINT");
        PLANT_OVERRIDES.put("catTail-mint", "AILMINT");

        ZOMBIE_KEYS.put("Normal", "TUTORIAL");
        ZOMBIE_KEYS.put("Cone Head", "TUTORIAL_ARMOR1");
        ZOMBIE_KEYS.put("Bucket Head", "TUTORIAL_ARMOR2");
        ZOMBIE_KEYS.put("Brick Head", "TUTORIAL_ARMOR4");
        ZOMBIE_KEYS.put("Knight", "DARK_ARMOR3");
        ZOMBIE_KEYS.put("Gargantuar", "TUTORIAL_GARGANTUAR");
        ZOMBIE_KEYS.put("Imp", "TUTORIAL_IMP");
        ZOMBIE_KEYS.put("Ra", "RA");
        ZOMBIE_KEYS.put("Explorer", "EXPLORER");
        ZOMBIE_KEYS.put("Tomb Raiser", "TOMB_RAISER");
        ZOMBIE_KEYS.put("Dodo", "ICEAGE_DODO");
        ZOMBIE_KEYS.put("Hunter", "ICEAGE_HUNTER");
        ZOMBIE_KEYS.put("Troglobite", "ICEAGE_TROGLOBITE");
        ZOMBIE_KEYS.put("Fisherman", "BEACH_FISHERMAN");
        ZOMBIE_KEYS.put("Octopus", "BEACH_OCTOPUS");
        ZOMBIE_KEYS.put("Snorkel", "BEACH_SNORKEL");
        ZOMBIE_KEYS.put("Juggler", "DARK_JUGGLER");
        ZOMBIE_KEYS.put("Wizard", "DARK_WIZARD");
        ZOMBIE_KEYS.put("King", "DARK_KING");
        ZOMBIE_KEYS.put("Imp Dragon", "DARK_IMP_DRAGON");
        ZOMBIE_KEYS.put("All Star", "MODERN_ALLSTAR");
        ZOMBIE_KEYS.put("Arcade", "EIGHTIES_ARCADE");
        ZOMBIE_KEYS.put("Umbrella", "LOSTCITY_JANE");
        ZOMBIE_KEYS.put("Turquoise", "LOSTCITY_CRYSTALSKULL");
        ZOMBIE_KEYS.put("Prospector", "PROSPECTOR");
        ZOMBIE_KEYS.put("Piano", "PIANO");
        ZOMBIE_KEYS.put("Newspaper", "MODERN_NEWSPAPER");
        ZOMBIE_KEYS.put("Barrel Roller", "BARRELROLLER");
        ZOMBIE_KEYS.put("Pharaoh", "PHARAOH");
        ZOMBIE_KEYS.put("Camel", "CAMEL_ALMANAC");
        ZOMBIE_KEYS.put("Weasel Hoarder", "ICEAGE_WEASELHOARDER");
        ZOMBIE_KEYS.put("Weasel", "ICEAGE_WEASEL");
        ZOMBIE_KEYS.put("Surfer", "BEACH_SURFER");
        ZOMBIE_KEYS.put("Fast Swimmer", "DUCKYTUBE");
        ZOMBIE_KEYS.put("Peashooter Zombie", "TUTORIAL");
        ZOMBIE_KEYS.put("Wall-nut Zombie", "TUTORIAL_ARMOR2");
        ZOMBIE_KEYS.put("Jalapeno Zombie", "TUTORIAL_ARMOR1");
        ZOMBIE_KEYS.put("Squash Zombie", "TUTORIAL_ARMOR4");
    }

    private final GameAssets assets;

    public Art(GameAssets assets) {
        this.assets = assets;
    }

    public TextureRegion plant(PlantType type) {
        String key = PLANT_OVERRIDES.get(type.getName());
        if (key == null) {
            key = normalize(type.getName());
        }
        TextureRegion region = assets.region(GameAssets.PLANTS, PLANT_PREFIX + key);
        return region == null ? placeholder() : region;
    }

    public TextureRegion placeholder() {
        return ui("image_ui_generic_leaf_backdrop");
    }

    public TextureRegion zombie(ZombieType type) {
        String key = ZOMBIE_KEYS.get(type.getName());
        if (key == null) {
            key = FALLBACK_ZOMBIE;
        }
        TextureRegion region = assets.region(GameAssets.ZOMBIES, ZOMBIE_PREFIX + key);
        if (region == null) {
            region = assets.region(GameAssets.ZOMBIES, ZOMBIE_PREFIX + FALLBACK_ZOMBIE);
        }
        return region == null ? placeholder() : region;
    }

    public TextureRegion ui(String name) {
        TextureRegion region = assets.region(GameAssets.UI, name);
        return region == null ? assets.region(GameAssets.UI, "white-pixel") : region;
    }

    public TextureRegion uiOptional(String name) {
        return assets.region(GameAssets.UI, name);
    }

    public TextureRegion vase(models.game.Vase.VaseKind kind) {
        String key = kind == models.game.Vase.VaseKind.PLANT ? "GREEN"
                : kind == models.game.Vase.VaseKind.GHOUL ? "GARGANTUAR" : "BROWN";
        return assets.region(GameAssets.VASES,
                "IMAGE_VASEBREAKER_VASE_" + key + "_VASE_" + key + "_115X150");
    }

    public TextureRegion brain() {
        return assets.region(GameAssets.BRAINS,
                "IMAGE_ZOMBIE_POWER_BRAIN_PROJECTILE_POWER_BRAIN_PROJECTILE_112X82");
    }

    public TextureRegion lawnBackground() {
        return assets.region("ATLASES/DELAYLOAD_BACKGROUND_FRONTLAWN_768_00.atlas",
                "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE");
    }

    public TextureRegion conveyorBelt() {
        return assets.region(GameAssets.ALWAYS_LOADED, "IMAGE_UI_CONVEYOR_CONVEYOR_BELT");
    }

    public TextureRegion conveyorTop() {
        return assets.region(GameAssets.ALWAYS_LOADED, "IMAGE_UI_CONVEYOR_CONVEYOR_TOP");
    }

    public TextureRegion pea() {
        return assets.region(GameAssets.PROJECTILES,
                "IMAGE_EFFECTS_T_PEA_PROJECTILE_T_PEA_PROJECTILE_39X36");
    }

    public TextureRegion mower() {
        return assets.region(GameAssets.MOWERS, "IMAGE_MOWERS_MOWER_BEACH_MOWER_BEACH_166X175");
    }

    public TextureRegion grave(String chapterName, float healthFraction) {
        boolean egypt = chapterName != null
                && chapterName.replaceAll("[^A-Za-z]", "").equalsIgnoreCase("egypt");
        String[] stages = egypt ? EGYPT_GRAVES : DARK_GRAVES;
        int index = healthFraction > 0.66f ? 0 : healthFraction > 0.33f ? 1 : 2;
        return assets.region(egypt ? GameAssets.GRAVES_EGYPT : GameAssets.GRAVES_DARK,
                stages[index]);
    }

    public TextureRegion logo() {
        return assets.region(GameAssets.LOGO, "IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
    }

    public TextureRegion statIcon(String name) {
        TextureRegion region = assets.region(GameAssets.STAT_ICONS,
                "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_" + name);
        return region == null ? placeholder() : region;
    }

    public TextureRegion trophy(String chapterName) {
        String normalized = chapterName == null ? "" : chapterName.replaceAll("[^A-Za-z]", "").toLowerCase();
        String key;
        switch (normalized) {
            case "egypt":
                key = "EGYPT";
                break;
            case "frostbite":
                key = "ICEAGE";
                break;
            case "waveybeach":
                key = "BEACH";
                break;
            case "darkages":
                key = "DARK";
                break;
            default:
                key = "MODERN";
                break;
        }
        return assets.region(GameAssets.TROPHIES, "IMAGE_WORLDMAP_TROPHY_" + key);
    }

    public TextureRegion menuBackground() {
        return assets.region(GameAssets.MENU_BACKGROUND, "IMAGE_MAINMENU_BACKGROUND");
    }

    public TextureRegion gardenBackground() {
        return assets.region(GameAssets.GARDEN_BACKGROUND, "IMAGE_BACKGROUNDS_ZEN_GARDEN");
    }

    public TextureRegion chapterBackground(String chapterName) {
        String normalized = chapterName == null ? "" : chapterName.replaceAll("[^A-Za-z]", "").toLowerCase();
        switch (normalized) {
            case "egypt":
                return assets.region("ATLASES/DELAYLOAD_BACKGROUND_EGYPT_COMPRESSED_768_00.atlas",
                        "IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
            case "frostbite":
                return assets.region("ATLASES/DELAYLOAD_BACKGROUND_ICEAGE_COMPRESSED_768_00.atlas",
                        "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE");
            case "waveybeach":
                return assets.region("ATLASES/DELAYLOAD_BACKGROUND_BEACH_COMPRESSED_768_00.atlas",
                        "IMAGE_BACKGROUNDS_BEACH_TEXTURE");
            case "darkages":
                return assets.region("ATLASES/DELAYLOAD_BACKGROUND_DARK_COMPRESSED_768_00.atlas",
                        "IMAGE_BACKGROUNDS_DARK_TEXTURE");
            default:
                TextureRegion lawn = lawnBackground();
                return lawn == null ? menuBackground() : lawn;
        }
    }

    private static String normalize(String displayName) {
        return displayName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
