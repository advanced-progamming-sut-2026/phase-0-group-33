package models.game;

import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.entities.zombie.decorator.BrickArmorDecorator;
import models.entities.zombie.decorator.BucketArmorDecorator;
import models.entities.zombie.decorator.ConeArmorDecorator;
import models.entities.zombie.decorator.CrownArmorDecorator;
import models.entities.zombie.decorator.NewspaperArmorDecorator;
import models.map.Position;


public final class ZombieFactory {

    private ZombieFactory() {
    }

    public static Zombie create(ZombieType type, double x, int row, double difficultyFactor) {
        int hp = (int) Math.round(type.getHitpoints() * difficultyFactor);
        Zombie zombie = new Zombie(type, new Position(x, row), hp, type.getSpeed());
        Zombie decorated = applyArmor(type, zombie);
        if (type == ZombieType.ALLSTAR) {
            decorated.getBattle().setCharging(true);
        }
        if (type == ZombieType.PROSPECTOR) {
            decorated.getBattle().setDynamiteTicks(100);
        }
        return decorated;
    }

    private static Zombie applyArmor(ZombieType type, Zombie zombie) {
        switch (type) {
            case CONE_HEAD:
                return new ConeArmorDecorator(zombie);
            case BUCKET_HEAD:
                return new BucketArmorDecorator(zombie);
            case KNIGHT:
                return new CrownArmorDecorator(zombie);
            case BRICK_HEAD:
                return new BrickArmorDecorator(zombie);
            case NEWSPAPER:
                return new NewspaperArmorDecorator(zombie);
            default:
                return zombie;
        }
    }
}
