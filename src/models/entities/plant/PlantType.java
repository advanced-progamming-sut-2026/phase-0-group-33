package models.entities.plant;

import java.util.Set;

public enum PlantType {
    SUNFLOWER(1, "Sunflower", PlantCategory.SUN_PRODUCER, Set.of(PlantTag.DAY), 50, 300, 0, 24, 5),
    TWIN_SUNFLOWER(2, "Twin Sunflower", PlantCategory.SUN_PRODUCER, Set.of(PlantTag.DAY), 125, 300, 0, 24, 15),
    SUN_SHROOM(3, "Sun-shroom", PlantCategory.SUN_PRODUCER, Set.of(PlantTag.SHROOM, PlantTag.WRAMP_UP, PlantTag.NIGHT), 25, 300, 0, 24, 5),
    PRIMAL_SUNFLOWER(4, "Primal Sunflower", PlantCategory.SUN_PRODUCER, Set.of(), 75, 300, 0, 24, 5),
    GOLD_BLOOM(5, "Gold Bloom", PlantCategory.SUN_PRODUCER, Set.of(), 0, 0, 0, 0, 75),
    PEASHOOTER(6, "Peashooter", PlantCategory.SHOOTER, Set.of(PlantTag.PEA), 100, 300, 20, 1, 5),
    REPEATER(7, "Repeater", PlantCategory.SHOOTER, Set.of(PlantTag.PEA), 200, 300, 20 * 2, 1, 5),
    THREEPEATER(8, "Threepeater", PlantCategory.SHOOTER, Set.of(PlantTag.PEA), 300, 300, 20, 1, 5),
    SNOW_PEA(9, "Snow Pea", PlantCategory.SHOOTER, Set.of(PlantTag.ICE, PlantTag.PEA), 150, 300, 20, 1, 5),
    ROTOBAGA(10, "Rotobaga", PlantCategory.SHOOTER, Set.of(), 150, 300, 10 * 3, 1, 5),
    PEA_POD(11, "Pea Pod", PlantCategory.SHOOTER, Set.of(PlantTag.PEA, PlantTag.STACK), 125, 300, 20, 1, 5),
    SPLIT_PEA(12, "Split Pea", PlantCategory.SHOOTER, Set.of(PlantTag.PEA), 125, 300, 20, 1, 5),
    CITRON(13, "Citron", PlantCategory.SHOOTER, Set.of(PlantTag.CHARGE), 350, 300, 800, 9, 5),
    CAULIPOWER(14, "Caulipower", PlantCategory.HOMING, Set.of(PlantTag.MAGIC, PlantTag.CHARGE), 250, 300, -1, 12, 15),
    ELECTRIC_BLUEBERRY(15, "Electric Blueberry", PlantCategory.HOMING, Set.of(PlantTag.CHARGE), 150, 300, 5000, 12, 15),
    BOWLING_BULB(16, "Bowling Bulb", PlantCategory.SHOOTER, Set.of(PlantTag.CHARGE), 200, 300, 40, 2, 5),
    CACTUS(17, "Cactus", PlantCategory.STRIKE_THROUGH, Set.of(), 175, 300, 30, 1, 5),
    FIRE_PEASHOOTER(18, "Fire Peashooter", PlantCategory.SHOOTER, Set.of(PlantTag.FIRE, PlantTag.PEA), 175, 300, 40, 1, 5),
    STARFRUIT(19, "Starfruit", PlantCategory.SHOOTER, Set.of(), 150, 300, 20, 1, 5),
    GOO_PEASHOOTER(20, "Goo Peashooter", PlantCategory.SHOOTER, Set.of(PlantTag.POISON), 125, 300, 20, 1, 5),
    MEGA_GATLING_PEA(21, "Mega Gatling Pea", PlantCategory.SHOOTER, Set.of(PlantTag.PEA), 400, 300, 20 * 4, 1, 5),
    SEA_SHROOM(22, "Sea-shroom", PlantCategory.SHOOTER, Set.of(PlantTag.SHROOM, PlantTag.WATER), 0, 300, 20, 1, 15),
    PUFF_SHROOM(23, "Puff-shroom", PlantCategory.SHOOTER, Set.of(PlantTag.SHROOM), 0, 300, 20, 1, 5),
    FUME_SHROOM(24, "Fume-shroom", PlantCategory.STRIKE_THROUGH, Set.of(PlantTag.SHROOM), 125, 300, 20, 1, 5),
    CABBAGE_PULT(25, "Cabbage-pult", PlantCategory.LOBBER, Set.of(), 100, 300, 40, 2, 5),
    KERNEL_PULT(26, "Kernel-pult", PlantCategory.LOBBER, Set.of(), 100, 300, 20, 2, 5),
    MELON_PULT(27, "Melon-pult", PlantCategory.LOBBER, Set.of(PlantTag.AOE), 325, 300, 80, 2, 5),
    WINTER_MELON(28, "Winter Melon", PlantCategory.LOBBER, Set.of(PlantTag.ICE, PlantTag.AOE), 500, 300, 80, 2, 5),
    PEPPER_PULT(29, "Pepper-pult", PlantCategory.LOBBER, Set.of(PlantTag.FIRE, PlantTag.AOE), 200, 300, 50, 2, 5),
    POTATO_MINE(30, "Potato Mine", PlantCategory.EXPLOSIVE, Set.of(PlantTag.TRAP, PlantTag.CHARGE), 25, 300, 1800, -1, 25),
    PRIMAL_POTATO_MINE(31, "Primal Potato Mine", PlantCategory.EXPLOSIVE, Set.of(PlantTag.TRAP, PlantTag.CHARGE), 50, 300, 2400, -1, 5),
    CHERRY_BOMB(32, "Cherry Bomb", PlantCategory.EXPLOSIVE, Set.of(), 150, 0, 1800, -1, 35),
    SQUASH(33, "Squash", PlantCategory.EXPLOSIVE, Set.of(PlantTag.TRAP), 50, 300, 1800, -1, 20),
    GRAPESHOT(34, "Grapeshot", PlantCategory.EXPLOSIVE, Set.of(), 150, 0, 1800, -1, 35),
    JALAPENO(35, "Jalapeno", PlantCategory.EXPLOSIVE, Set.of(PlantTag.FIRE), 125, 0, 1800, -1, 35),
    DOOM_SHROOM(36, "Doom-shroom", PlantCategory.EXPLOSIVE, Set.of(PlantTag.SHROOM), 125, 0, 1800, -1, 15),
    TANGLE_KELP(37, "Tangle Kelp", PlantCategory.EXPLOSIVE, Set.of(PlantTag.TRAP, PlantTag.WATER), 25, 300, -1, -1, 15),
    ICEBERG_LETTUCE(38, "Iceberg Lettuce", PlantCategory.EXPLOSIVE, Set.of(PlantTag.TRAP, PlantTag.ICE), 0, 300, 0, -1, 20),
    BONK_CHOY(39, "Bonk Choy", PlantCategory.MELEE, Set.of(), 150, 300, 15, 0, 5),
    PHAT_BEET(40, "Phat Beet", PlantCategory.MELEE, Set.of(PlantTag.AOE), 150, 300, 15, 2, 5),
    CHOMPER(41, "Chomper", PlantCategory.MELEE, Set.of(), 150, 300, -1, 40, 5),
    WASABI_WHIP(42, "Wasabi Whip", PlantCategory.MELEE, Set.of(PlantTag.FIRE), 150, 300, 40, 2, 5),
    KIWIBEAST(43, "Kiwibeast", PlantCategory.MELEE, Set.of(PlantTag.AOE, PlantTag.WRAMP_UP), 175, 300, 15, 2, 5),
    WALL_NUT(44, "Wall-nut", PlantCategory.WALL_NUT, Set.of(), 50, 4000, 0, -1, 20),
    TALL_NUT(45, "Tall-nut", PlantCategory.WALL_NUT, Set.of(), 125, 8000, 0, -1, 20),
    ENDURIAN(46, "Endurian", PlantCategory.WALL_NUT, Set.of(), 100, 3000, 20, -1, 15),
    GARLIC(47, "Garlic", PlantCategory.WALL_NUT, Set.of(PlantTag.MOVE_ZOMBIES), 50, 300, 0, -1, 20),
    SWEET_POTATO(48, "Sweet Potato", PlantCategory.WALL_NUT, Set.of(PlantTag.MOVE_ZOMBIES), 150, 3000, 0, -1, 20),
    EXPLODE_O_NUT(49, "Explode-o-nut", PlantCategory.WALL_NUT, Set.of(PlantTag.EXPLOSIVE), 50, 4000, 1800, -1, 20),
    PUMPKIN(50, "Pumpkin", PlantCategory.WALL_NUT, Set.of(PlantTag.STACK), 150, 4000, 0, -1, 20),
    SUN_BEAN(51, "Sun Bean", PlantCategory.WALL_NUT, Set.of(PlantTag.SUN), 50, 1000, 0, -1, 20),
    TORCHWOOD(52, "Torchwood", PlantCategory.MODIFIER, Set.of(PlantTag.FIRE), 175, 300, 0, -1, 5),
    MAGNET_SHROOM(53, "Magnet-shroom", PlantCategory.HOMING, Set.of(PlantTag.SHROOM, PlantTag.MAGIC), 100, 300, 0, 10, 15),
    HYPNO_SHROOM(54, "Hypno-shroom", PlantCategory.MODIFIER, Set.of(PlantTag.SHROOM, PlantTag.MAGIC), 125, 300, 0, -1, 20),
    CAT_TAIL(55, "Cat-tail", PlantCategory.HOMING, Set.of(), 175, 300, 15, 1, 20),
    IMITATER(56, "Imitater", PlantCategory.MODIFIER, Set.of(), 0, 0, 0, -1, 0),
    ICE_SHROOM(57, "Ice-shroom", PlantCategory.EXPLOSIVE, Set.of(PlantTag.SHROOM, PlantTag.ICE), 75, 0, 0, -1, 50),
    LILY_PAD(58, "Lily Pad", PlantCategory.MODIFIER, Set.of(PlantTag.WATER, PlantTag.STACK), 25, 300, 0, -1, 5),
    HOT_POTATO(59, "Hot Potato", PlantCategory.EXPLOSIVE, Set.of(PlantTag.FIRE), 0, 0, 0, -1, 5),
    GRAVE_BUSTER(60, "Grave Buster", PlantCategory.EXPLOSIVE, Set.of(), 0, 0, -1, -1, 10),
    ENLIGHTEN_MINT(61, "Enlighten-mint", PlantCategory.SUN_PRODUCER, Set.of(), 0, 0, 0, -1, 85),
    APPEASE_MINT(62, "Appease-mint", PlantCategory.SHOOTER, Set.of(), 0, 0, 0, -1, 85),
    ARMA_MINT(63, "Arma-mint", PlantCategory.LOBBER, Set.of(), 0, 0, 0, -1, 85),
    BOMBARD_MINT(64, "Bombard-mint", PlantCategory.EXPLOSIVE, Set.of(), 0, 0, 0, -1, 85),
    ENFORCE_MINT(65, "Enforce-mint", PlantCategory.MELEE, Set.of(), 0, 0, 0, -1, 85),
    REINFORCE_MINT(66, "Reinforce-mint", PlantCategory.WALL_NUT, Set.of(), 0, 0, 0, -1, 85),
    ENCHANT_MINT(67, "Enchant-mint", PlantCategory.MODIFIER, Set.of(), 0, 0, 0, -1, 85),
    PIERCE_MINT(68, "Pierce-mint", PlantCategory.STRIKE_THROUGH, Set.of(), 0, 0, 0, -1, 85),
    CAT_TAIL_MINT(69, "catTail-mint", PlantCategory.HOMING, Set.of(), 0, 0, 0, -1, 85);

    private final int id;
    private final String name;
    private final PlantCategory category;
    private final Set<PlantTag> tags;
    private final int cost;
    private final int baseHp;
    private final int damage; // -1 means instant kill
    private final double actionInterval;
    private final int recharge;

    PlantType(int id, String name, PlantCategory category, Set<PlantTag> tags,
              int cost, int baseHp, int damage, double actionInterval, int recharge) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.cost = cost;
        this.baseHp = baseHp;
        this.damage = damage;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public PlantCategory getCategory() { return category; }
    public Set<PlantTag> getTags() { return tags; }
    public int getCost() { return cost; }
    public int getBaseHp() { return baseHp; }
    public int getDamage() { return damage; }
    public double getActionInterval() { return actionInterval; }
    public int getRecharge() { return recharge; }

    public boolean isInstantKill() { return damage == -1; }

    @Override
    public String toString() {
        return name + " (ID:" + id + ", Cost:" + cost + ")";
    }
}