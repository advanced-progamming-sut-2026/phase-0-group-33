package controllers.managers;

import models.entities.plant.PlantTag;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.game.Projectile;

import java.util.ArrayList;
import java.util.List;

public class ProjectileManager {

    private static final double STRAIGHT_SPEED = 0.34;
    private static final double LOB_SPEED = 0.09;
    private static final double HIT_RANGE = 0.45;

    private final GameSession session;
    private final CombatManager combat;
    private final List<Projectile> projectiles = new ArrayList<>();

    public ProjectileManager(GameSession session, CombatManager combat) {
        this.session = session;
        this.combat = combat;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void launchStraight(PlacedPlant plant, int row, int direction) {
        projectiles.add(new Projectile(plant.getType(), Projectile.Motion.STRAIGHT,
                row, plant.getX(), 0, direction));
    }

    public void launchPiercing(PlacedPlant plant) {
        projectiles.add(new Projectile(plant.getType(), Projectile.Motion.PIERCING,
                plant.getY(), plant.getX(), 0, 1));
    }

    public void launchLob(PlacedPlant plant, double targetX) {
        projectiles.add(new Projectile(plant.getType(), Projectile.Motion.LOB,
                plant.getY(), plant.getX(), targetX, 1));
    }

    public void tick() {
        for (Projectile projectile : new ArrayList<>(projectiles)) {
            if (projectile.getMotion() == Projectile.Motion.LOB) {
                advanceLob(projectile);
            } else {
                advanceFlat(projectile);
            }
            if (projectile.isSpent()) {
                projectiles.remove(projectile);
            }
        }
    }

    private void advanceLob(Projectile projectile) {
        projectile.advanceFlight(LOB_SPEED);
        if (!projectile.hasLanded()) {
            return;
        }
        projectile.markSpent();
        PlantType source = projectile.getSource();
        if (source.getTags().contains(PlantTag.AOE)) {
            combat.damageArea(projectile.getX(), projectile.getRow(), 1, source);
            return;
        }
        Zombie target = zombieNear(projectile.getRow(), projectile.getX());
        if (target != null) {
            combat.hitZombie(target, source);
        }
    }

    private void advanceFlat(Projectile projectile) {
        projectile.setX(projectile.getX() + STRAIGHT_SPEED * projectile.getDirection());
        if (projectile.getX() < 0 || projectile.getX() > GameSession.COLS + 1) {
            projectile.markSpent();
            return;
        }
        Zombie target = zombieNear(projectile.getRow(), projectile.getX());
        if (target == null || !projectile.recordHit(target)) {
            return;
        }
        applyImpact(projectile, target);
        if (projectile.getMotion() == Projectile.Motion.STRAIGHT) {
            projectile.markSpent();
        }
    }

    private void applyImpact(Projectile projectile, Zombie target) {
        PlantType source = projectile.getSource();
        boolean torched = source.getTags().contains(PlantTag.PEA)
                && torchwoodBetween(projectile.getRow(), projectile.getOriginX(), projectile.getX());
        combat.hitZombie(target, source);
        if (torched && !target.isDead()) {
            target.setChilledTicks(0);
            combat.hitZombie(target, source);
        }
    }

    private boolean torchwoodBetween(int row, double fromX, double toX) {
        double low = Math.min(fromX, toX);
        double high = Math.max(fromX, toX);
        for (PlacedPlant other : session.getPlants()) {
            if (other.getType() == PlantType.TORCHWOOD && other.getY() == row
                    && other.getX() > low && other.getX() <= high && !other.isDisabled()) {
                return true;
            }
        }
        return false;
    }

    private Zombie zombieNear(int row, double x) {
        Zombie closest = null;
        double best = HIT_RANGE;
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() != row) {
                continue;
            }
            double distance = Math.abs(zombie.getPosition().getX() - x);
            if (distance <= best) {
                best = distance;
                closest = zombie;
            }
        }
        return closest;
    }
}
