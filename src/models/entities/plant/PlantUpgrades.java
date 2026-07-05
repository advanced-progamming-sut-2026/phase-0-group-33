package models.entities.plant;

import java.util.EnumMap;
import java.util.Map;

public final class PlantUpgrades {
    private static final Map<PlantType, int[]> HP = new EnumMap<>(PlantType.class);
    private static final Map<PlantType, int[]> COST = new EnumMap<>(PlantType.class);
    private static final Map<PlantType, int[]> DAMAGE = new EnumMap<>(PlantType.class);
    private static final Map<PlantType, int[]> RECHARGE = new EnumMap<>(PlantType.class);

    private PlantUpgrades() {
    }

    static {
        initSunProducerAndPeaHp();
        initDefenderHp();
        initCost();
        initExtraCost();
        initDamage();
        initExplosiveDamage();
        initRecharge();
    }

    private static void initSunProducerAndPeaHp() {
        hp(PlantType.SUNFLOWER, 0, 150, 0);
        hp(PlantType.TWIN_SUNFLOWER, 0, 150, 0);
        hp(PlantType.SUN_SHROOM, 0, 150, 0);
        hp(PlantType.PRIMAL_SUNFLOWER, 0, 150, 0);
        hp(PlantType.PEASHOOTER, 0, 150, 0);
        hp(PlantType.REPEATER, 0, 200, 0);
        hp(PlantType.THREEPEATER, 0, 0, 200);
        hp(PlantType.ROTOBAGA, 0, 150, 0);
        hp(PlantType.PEA_POD, 0, 200, 0);
        hp(PlantType.SPLIT_PEA, 0, 200, 0);
        hp(PlantType.CAULIPOWER, 0, 150, 0);
        hp(PlantType.FIRE_PEASHOOTER, 0, 200, 0);
        hp(PlantType.GOO_PEASHOOTER, 0, 150, 0);
        hp(PlantType.CABBAGE_PULT, 0, 0, 150);
        hp(PlantType.KERNEL_PULT, 0, 0, 150);
        hp(PlantType.BONK_CHOY, 0, 0, 200);
        hp(PlantType.PHAT_BEET, 0, 0, 200);
        hp(PlantType.CHOMPER, 0, 200, 0);
        hp(PlantType.WASABI_WHIP, 0, 0, 200);
        hp(PlantType.KIWIBEAST, 200, 0, 0);
    }

    private static void initDefenderHp() {
        hp(PlantType.WALL_NUT, 1000, 0, 1500);
        hp(PlantType.TALL_NUT, 2000, 0, 3000);
        hp(PlantType.ENDURIAN, 0, 1000, 0);
        hp(PlantType.GARLIC, 150, 0, 250);
        hp(PlantType.SWEET_POTATO, 1000, 0, 1500);
        hp(PlantType.EXPLODE_O_NUT, 1000, 0, 0);
        hp(PlantType.PUMPKIN, 1000, 0, 1500);
        hp(PlantType.SUN_BEAN, 0, 150, 0);
        hp(PlantType.TORCHWOOD, 300, 0, 0);
        hp(PlantType.MAGNET_SHROOM, 0, 0, 200);
        hp(PlantType.CAT_TAIL, 0, 200, 0);
        hp(PlantType.LILY_PAD, 0, 200, 0);
    }

    private static void initCost() {
        cost(PlantType.TWIN_SUNFLOWER, 0, 0, 25);
        cost(PlantType.PRIMAL_SUNFLOWER, 0, 0, 25);
        cost(PlantType.GOLD_BLOOM, 0, 0, 25);
        cost(PlantType.PEASHOOTER, 0, 0, 25);
        cost(PlantType.REPEATER, 0, 0, 25);
        cost(PlantType.THREEPEATER, 25, 0, 0);
        cost(PlantType.SNOW_PEA, 0, 0, 25);
        cost(PlantType.ROTOBAGA, 0, 0, 25);
        cost(PlantType.PEA_POD, 0, 0, 25);
        cost(PlantType.SPLIT_PEA, 0, 0, 25);
        cost(PlantType.CITRON, 0, 0, 50);
        cost(PlantType.CAULIPOWER, 0, 0, 50);
        cost(PlantType.ELECTRIC_BLUEBERRY, 0, 0, 25);
        cost(PlantType.BOWLING_BULB, 0, 0, 25);
        cost(PlantType.CACTUS, 0, 0, 25);
        cost(PlantType.FIRE_PEASHOOTER, 0, 0, 25);
        cost(PlantType.STARFRUIT, 0, 0, 25);
        cost(PlantType.GOO_PEASHOOTER, 0, 0, 25);
        cost(PlantType.MEGA_GATLING_PEA, 0, 0, 50);
    }

    private static void initExtraCost() {
        cost(PlantType.FUME_SHROOM, 0, 0, 25);
        cost(PlantType.MELON_PULT, 25, 0, 0);
        cost(PlantType.WINTER_MELON, 50, 0, 25);
        cost(PlantType.PEPPER_PULT, 0, 0, 25);
        cost(PlantType.CHERRY_BOMB, 0, 0, 25);
        cost(PlantType.GRAPESHOT, 0, 0, 25);
        cost(PlantType.JALAPENO, 0, 0, 25);
        cost(PlantType.DOOM_SHROOM, 0, 0, 50);
        cost(PlantType.TANGLE_KELP, 0, 0, 25);
        cost(PlantType.ENDURIAN, 0, 0, 25);
        cost(PlantType.SUN_BEAN, 0, 0, 25);
        cost(PlantType.TORCHWOOD, 0, 0, 25);
        cost(PlantType.HYPNO_SHROOM, 25, 0, 0);
        cost(PlantType.CAT_TAIL, 0, 0, 25);
        cost(PlantType.IMITATER, 0, 25, 0);
        cost(PlantType.LILY_PAD, 25, 0, 0);
    }

    private static void initDamage() {
        damage(PlantType.PEASHOOTER, 10, 0, 0);
        damage(PlantType.REPEATER, 10, 0, 0);
        damage(PlantType.THREEPEATER, 0, 10, 0);
        damage(PlantType.SNOW_PEA, 10, 0, 0);
        damage(PlantType.ROTOBAGA, 10, 0, 0);
        damage(PlantType.PEA_POD, 10, 0, 0);
        damage(PlantType.SPLIT_PEA, 10, 0, 0);
        damage(PlantType.CITRON, 0, 150, 0);
        damage(PlantType.BOWLING_BULB, 0, 15, 0);
        damage(PlantType.CACTUS, 0, 10, 0);
        damage(PlantType.FIRE_PEASHOOTER, 10, 0, 0);
        damage(PlantType.STARFRUIT, 0, 10, 0);
        damage(PlantType.GOO_PEASHOOTER, 5, 0, 0);
        damage(PlantType.MEGA_GATLING_PEA, 10, 0, 0);
        damage(PlantType.SEA_SHROOM, 0, 5, 0);
        damage(PlantType.PUFF_SHROOM, 0, 10, 0);
        damage(PlantType.FUME_SHROOM, 0, 10, 0);
        damage(PlantType.CABBAGE_PULT, 10, 0, 0);
        damage(PlantType.KERNEL_PULT, 0, 10, 0);
        damage(PlantType.MELON_PULT, 0, 15, 30);
        damage(PlantType.WINTER_MELON, 0, 15, 0);
        damage(PlantType.PEPPER_PULT, 15, 0, 0);
        damage(PlantType.BONK_CHOY, 5, 0, 0);
        damage(PlantType.PHAT_BEET, 10, 0, 0);
        damage(PlantType.WASABI_WHIP, 10, 0, 0);
        damage(PlantType.KIWIBEAST, 0, 15, 0);
        damage(PlantType.ENDURIAN, 5, 0, 0);
        damage(PlantType.CAT_TAIL, 10, 0, 0);
    }

    private static void initExplosiveDamage() {
        damage(PlantType.POTATO_MINE, 0, 0, 600);
        damage(PlantType.PRIMAL_POTATO_MINE, 0, 0, 400);
        damage(PlantType.CHERRY_BOMB, 0, 600, 0);
        damage(PlantType.SQUASH, 0, 600, 0);
        damage(PlantType.GRAPESHOT, 600, 0, 0);
        damage(PlantType.JALAPENO, 0, 600, 0);
        damage(PlantType.DOOM_SHROOM, 0, 800, 0);
        damage(PlantType.EXPLODE_O_NUT, 0, 200, 0);
        damage(PlantType.ICE_SHROOM, 0, 0, 50);
    }

    private static void initRecharge() {
        recharge(PlantType.GOLD_BLOOM, 5, 0, 0);
        recharge(PlantType.POTATO_MINE, 0, 5, 0);
        recharge(PlantType.PRIMAL_POTATO_MINE, 0, 3, 0);
        recharge(PlantType.CHERRY_BOMB, 5, 0, 0);
        recharge(PlantType.SQUASH, 3, 0, 0);
        recharge(PlantType.JALAPENO, 5, 0, 0);
        recharge(PlantType.DOOM_SHROOM, 5, 0, 0);
        recharge(PlantType.TANGLE_KELP, 5, 0, 0);
        recharge(PlantType.ICEBERG_LETTUCE, 2, 0, 0);
        recharge(PlantType.WALL_NUT, 0, 5, 0);
        recharge(PlantType.TALL_NUT, 0, 5, 0);
        recharge(PlantType.SWEET_POTATO, 0, 5, 0);
        recharge(PlantType.PUMPKIN, 0, 5, 0);
        recharge(PlantType.MAGNET_SHROOM, 0, 5, 0);
        recharge(PlantType.ICE_SHROOM, 0, 5, 0);
        recharge(PlantType.CAULIPOWER, 2, 0, 0);
        recharge(PlantType.ELECTRIC_BLUEBERRY, 2, 0, 0);
        recharge(PlantType.GRAVE_BUSTER, 0, 2, 0);
        recharge(PlantType.HOT_POTATO, 2, 0, 0);
        recharge(PlantType.IMITATER, 2, 0, 0);
        recharge(PlantType.LILY_PAD, 0, 0, 2);
        recharge(PlantType.ENLIGHTEN_MINT, 0, 5, 0);
        recharge(PlantType.APPEASE_MINT, 0, 5, 0);
        recharge(PlantType.ARMA_MINT, 0, 5, 0);
        recharge(PlantType.BOMBARD_MINT, 0, 5, 0);
        recharge(PlantType.ENFORCE_MINT, 0, 5, 0);
        recharge(PlantType.REINFORCE_MINT, 0, 5, 0);
        recharge(PlantType.ENCHANT_MINT, 0, 5, 0);
        recharge(PlantType.PIERCE_MINT, 0, 5, 0);
        recharge(PlantType.CAT_TAIL_MINT, 0, 5, 0);
    }

    private static void hp(PlantType type, int l2, int l3, int l4) {
        HP.put(type, new int[] { l2, l3, l4 });
    }

    private static void cost(PlantType type, int l2, int l3, int l4) {
        COST.put(type, new int[] { l2, l3, l4 });
    }

    private static void damage(PlantType type, int l2, int l3, int l4) {
        DAMAGE.put(type, new int[] { l2, l3, l4 });
    }

    private static void recharge(PlantType type, int l2, int l3, int l4) {
        RECHARGE.put(type, new int[] { l2, l3, l4 });
    }

    private static int sum(int[] perLevel, int level) {
        if (perLevel == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < perLevel.length; i++) {
            if (level >= i + 2) {
                total += perLevel[i];
            }
        }
        return total;
    }

    public static int hpBonus(PlantType type, int level) {
        return sum(HP.get(type), level);
    }

    public static int costReduction(PlantType type, int level) {
        return sum(COST.get(type), level);
    }

    public static int damageBonus(PlantType type, int level) {
        return sum(DAMAGE.get(type), level);
    }

    public static int rechargeReduction(PlantType type, int level) {
        return sum(RECHARGE.get(type), level);
    }
}
