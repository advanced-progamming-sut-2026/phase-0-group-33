package models.game;

import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.entities.zombie.decorator.BrickArmorDecorator;
import models.entities.zombie.decorator.BucketArmorDecorator;
import models.entities.zombie.decorator.ConeArmorDecorator;
import models.entities.zombie.decorator.CrownArmorDecorator;
import models.map.Position;

/**
 * Builds battle-ready zombies: applies difficulty scaling to health
 * and wraps armored types in their armor decorators.
 */
public final class ZombieFactory {

    private ZombieFactory() {
    }

    public static Zombie create(ZombieType type, double x, int row, double difficultyFactor) {
        int hp = (int) Math.round(type.getHitpoints() * difficultyFactor);
        Zombie zombie = new Zombie(type, new Position(x, row), hp, type.getSpeed());
        switch (type) {
            case CONE_HEAD:
                return new ConeArmorDecorator(zombie);
            case BUCKET_HEAD:
                return new BucketArmorDecorator(zombie);
            case KNIGHT:
                return new CrownArmorDecorator(zombie);
            case BRICK_HEAD:
                return new BrickArmorDecorator(zombie);
            default:
                return zombie;
        }
    }
}
