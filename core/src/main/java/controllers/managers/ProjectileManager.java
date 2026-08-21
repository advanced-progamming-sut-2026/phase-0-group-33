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
    private static final int ZOMBIE_SHOT_DAMAGE = 20;
    private static final double SCATTER_SPEED = 0.26;

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
        launchStraight(plant, row, direction, 0);
    }

    public void launchStraight(PlacedPlant plant, int row, int direction, double offset) {
        projectiles.add(new Projectile(plant.getType(), Projectile.Motion.STRAIGHT,
                row, plant.getX() + offset, 0, direction));
    }

    public void launchPiercing(PlacedPlant plant, int pierce) {
        Projectile shot = new Projectile(plant.getType(), Projectile.Motion.PIERCING,
                plant.getY(), plant.getX(), 0, 1);
        if (pierce > 0) {
            shot.setPierceLeft(pierce);
        }
        projectiles.add(shot);
    }

    public void launchZombieShot(int row, double originX) {
        Projectile shot = new Projectile(PlantType.PEASHOOTER, Projectile.Motion.STRAIGHT,
                row, originX, 0, -1);
        shot.markFromZombie();
        projectiles.add(shot);
    }

    public void launchStar(PlacedPlant plant, int forward, int lane) {
        Projectile star = new Projectile(plant.getType(), Projectile.Motion.SCATTER,
                plant.getY(), plant.getX(), 0, forward);
        star.setLaneStep(lane);
        projectiles.add(star);
    }

    public void launchGrapes(int column, int row) {
        int[][] ways = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}};
        for (int[] way : ways) {
            Projectile grape = new Projectile(PlantType.GRAPESHOT, Projectile.Motion.SCATTER,
                    row, column, 0, way[0]);
            grape.setLaneStep(way[1]);
            projectiles.add(grape);
        }
    }

    public void launchLob(PlacedPlant plant, double targetX) {
        projectiles.add(new Projectile(plant.getType(), Projectile.Motion.LOB,
                plant.getY(), plant.getX(), targetX, 1));
    }

    public void tick() {
        for (Projectile projectile : new ArrayList<>(projectiles)) {
            if (projectile.getMotion() == Projectile.Motion.LOB) {
                advanceLob(projectile);
            } else if (projectile.getMotion() == Projectile.Motion.SCATTER) {
                advanceScatter(projectile);
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

    private void advanceScatter(Projectile projectile) {
        projectile.setX(projectile.getX() + SCATTER_SPEED * projectile.getDirection());
        projectile.advanceLane(SCATTER_SPEED);
        if (projectile.getX() < 0.4 || projectile.getX() > GameSession.COLS + 0.6
                || projectile.getLane() < 0.4 || projectile.getLane() > GameSession.ROWS + 0.6) {
            projectile.markSpent();
            return;
        }
        Zombie target = zombieNear(projectile.getRow(), projectile.getX());
        if (target == null || !projectile.recordHit(target)) {
            return;
        }
        projectile.markSpent();
        PlantType source = projectile.getSource();
        if (source.getTags().contains(PlantTag.AOE) || source == PlantType.GRAPESHOT) {
            combat.damageArea(projectile.getX(), projectile.getRow(), 0, source);
        } else {
            combat.hitZombie(target, source);
        }
    }

    private void advanceZombieShot(Projectile projectile) {
        projectile.setX(projectile.getX() - STRAIGHT_SPEED);
        if (projectile.getX() < 0.5) {
            projectile.markSpent();
            return;
        }
        PlacedPlant target = session.plantAt((int) Math.round(projectile.getX()),
                projectile.getRow());
        if (target == null) {
            return;
        }
        projectile.markSpent();
        if (!combat.damagePlant(target, ZOMBIE_SHOT_DAMAGE) && target.isDead()) {
            session.removePlant(target, true);
        }
    }

    private void advanceFlat(Projectile projectile) {
        if (projectile.isFromZombie()) {
            advanceZombieShot(projectile);
            return;
        }
        projectile.setX(projectile.getX() + STRAIGHT_SPEED * projectile.getDirection());
        if (projectile.getX() < 0 || projectile.getX() > GameSession.COLS + 1) {
            projectile.markSpent();
            return;
        }
        if (projectile.getMotion() == Projectile.Motion.STRAIGHT
                && combat.blockedAt(projectile.getRow(), projectile.getX(),
                        projectile.getSource())) {
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
            return;
        }
        projectile.spendPierce();
        if (projectile.getPierceLeft() <= 0) {
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
            if (!zombie.occupiesRow(row)) {
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
