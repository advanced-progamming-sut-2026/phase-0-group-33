package models.entities.zombie;

public enum ZombieType {
    NORMAL(1, "Normal", 100, 190, 0.185, 100, ArmorType.NONE, "ZombieDefault"),
    CONE_HEAD(2, "Cone Head", 100, 190, 0.185, 200, ArmorType.CONE, "ZombieArmor1"),
    BUCKET_HEAD(3, "Bucket Head", 100, 190, 0.185, 400, ArmorType.BUCKET, "ZombieArmor2"),
    BRICK_HEAD(4, "Brick Head", 100, 190, 0.185, 700, ArmorType.BRICK, "ZombieArmor4"),
    KNIGHT(5, "Knight", 100, 190, 0.185, 550, ArmorType.CROWN_SHOULDER, "ZombieDarkArmor3"),
    GARGANTUAR(6, "Gargantuar", 1500, 3600, 0.24, 1500, ArmorType.NONE, "ZombieGargantuar"),
    IMP(7, "Imp", 100, 190, 0.22, 100, ArmorType.NONE, "ZombieImp"),
    RA(8, "Ra", 100, 190, 0.2, 100, ArmorType.NONE, "ZombieRa"),
    EXPLORER(9, "Explorer", 100, 250, 0.25, 250, ArmorType.NONE, "ZombieExplorer"),
    TOMB_RAISER(10, "Tomb Raiser", 100, 380, 0.185, 300, ArmorType.NONE, "ZombieTombRaiser"),
    DODO(11, "Dodo", 100, 490, 0.3, 600, ArmorType.NONE, "ZombieIceAgeDodo"),
    HUNTER(12, "Hunter", 100, 700, 0.12, 500, ArmorType.NONE, "ZombieIceAgeHunter"),
    TROGLOBITE(13, "Troglobite", 100, 470, 0.185, 600, ArmorType.NONE, "ZombieIceAgeTroglobite"),
    FISHERMAN(14, "Fisherman", 100, 1000, 0.185, 700, ArmorType.NONE, "ZombieBeachFisherman"),
    OCTOPUS(15, "Octopus", 100, 910, 0.12, 900, ArmorType.NONE, "ZombieBeachOctopus"),
    SNORKEL(16, "Snorkel", 100, 350, 0.185, 200, ArmorType.NONE, "ZombieBeachSnorkel"),
    JUGGLER(17, "Juggler", 100, 420, 0.2, 450, ArmorType.NONE, "ZombieDarkJuggler"),
    WIZARD(18, "Wizard", 100, 490, 0.12, 800, ArmorType.NONE, "ZombieWizard"),
    KING(19, "King", 100, 1000, 0.0, 750, ArmorType.NONE, "ZombieDarkKing"),
    IMP_DRAGON(20, "Imp Dragon", 100, 190, 0.185, 150, ArmorType.NONE, "ZombieDarkImpDragon"),
    ALLSTAR(21, "All Star", 100, 1100, 0.16, 1000, ArmorType.NONE, "ZombieModernAllStar"),
    ARCAD(22, "Arcade", 100, 490, 0.19, 600, ArmorType.NONE, "ZombieArcade"),
    UMBRELLA(23, "Umbrella", 100, 350, 0.25, 200, ArmorType.NONE, "ZombieLostCityJane"),
    TURQUOISE(24, "Turquoise", 100, 250, 0.185, 500, ArmorType.NONE, "ZombieCrystalSkull"),
    PROSPECTOR(25, "Prospector", 100, 190, 0.16, 200, ArmorType.NONE, "ZombieProspector"),
    PIANO(26, "Piano", 4000, 840, 0.12, 450, ArmorType.NONE, "ZombiePiano"),
    NEWSPAPER(27, "Newspaper", 200, 460, 0.22, 700, ArmorType.NEWSPAPER, "ZombieNewspaper"),
    BARREL_ROLLER(32, "Barrel Roller", 100, 470, 0.185, 600, ArmorType.NONE, "ZombieBarrelRoller"),
    // Zombotany minigame zombies (doc: minigames chapter).
    PEASHOOTER_ZOMBIE(28, "Peashooter Zombie", 100, 190, 0.185, 200, ArmorType.NONE, "ZombotanyPea"),
    WALLNUT_ZOMBIE(29, "Wall-nut Zombie", 100, 1100, 0.12, 300, ArmorType.NONE, "ZombotanyWallnut"),
    JALAPENO_ZOMBIE(30, "Jalapeno Zombie", 100, 190, 0.185, 350, ArmorType.NONE, "ZombotanyJalapeno"),
    SQUASH_ZOMBIE(31, "Squash Zombie", 100, 190, 0.3, 300, ArmorType.NONE, "ZombotanySquash");

    private final int id;
    private final String name;
    private final int eatDps;
    private final int hitpoints;
    private final double speed;
    private final int waveCost;
    private final ArmorType armorType;
    private final String internalId;

    ZombieType(int id, String name, int eatDps, int hitpoints, double speed,
            int waveCost, ArmorType armorType, String internalId) {
        this.id = id;
        this.name = name;
        this.eatDps = eatDps;
        this.hitpoints = hitpoints;
        this.speed = speed;
        this.waveCost = waveCost;
        this.armorType = armorType;
        this.internalId = internalId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getEatDps() {
        return eatDps;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public double getSpeed() {
        return speed;
    }

    public int getWaveCost() {
        return waveCost;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public String getInternalId() {
        return internalId;
    }

    @Override
    public String toString() {
        return name + " (ID:" + id + ", HP:" + hitpoints + ")";
    }

    public enum ArmorType {
        NONE(0, false),
        CONE(370, false),
        BUCKET(1100, true),
        BRICK(2200, false),
        CROWN_SHOULDER(3200, true), // 1600+1600
        NEWSPAPER(800, false);

        private final int armorHitpoints;
        private final boolean metallic;

        ArmorType(int armorHitpoints, boolean metallic) {
            this.armorHitpoints = armorHitpoints;
            this.metallic = metallic;
        }

        public int getArmorHitpoints() {
            return armorHitpoints;
        }

        public boolean isMetallic() {
            return metallic;
        }
    }
}