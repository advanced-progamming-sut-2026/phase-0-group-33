package models.game;

import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;

import java.util.HashSet;
import java.util.Set;

public class Projectile {

    public enum Motion {
        STRAIGHT,
        LOB,
        PIERCING,
        SCATTER
    }

    private static final double ARC_HEIGHT = 1.6;

    private final PlantType source;
    private final Motion motion;
    private double lane;
    private int laneStep;
    private final double originX;
    private final double targetX;
    private final int direction;
    private final Set<Zombie> hitAlready = new HashSet<>();

    private double x;
    private double flight;
    private boolean spent;
    private boolean fromZombie;
    private int pierceLeft = Integer.MAX_VALUE;

    public Projectile(PlantType source, Motion motion, int row, double originX,
                      double targetX, int direction) {
        this.source = source;
        this.motion = motion;
        this.lane = row;
        this.originX = originX;
        this.targetX = targetX;
        this.direction = direction;
        this.x = originX;
    }

    public PlantType getSource() {
        return source;
    }

    public boolean isFromZombie() {
        return fromZombie;
    }

    public void markFromZombie() {
        this.fromZombie = true;
    }

    public Motion getMotion() {
        return motion;
    }

    public int getRow() {
        return (int) Math.round(lane);
    }

    public double getLane() {
        return lane;
    }

    public int getLaneStep() {
        return laneStep;
    }

    public void setLaneStep(int laneStep) {
        this.laneStep = laneStep;
    }

    public void advanceLane(double amount) {
        this.lane += amount * laneStep;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getOriginX() {
        return originX;
    }

    public double getTargetX() {
        return targetX;
    }

    public int getDirection() {
        return direction;
    }

    public double getFlight() {
        return flight;
    }

    public void advanceFlight(double amount) {
        this.flight = Math.min(1.0, flight + amount);
        this.x = originX + (targetX - originX) * flight;
    }

    public double getHeight() {
        if (motion != Motion.LOB) {
            return 0;
        }
        return ARC_HEIGHT * 4 * flight * (1 - flight);
    }

    public boolean hasLanded() {
        return flight >= 1.0;
    }

    public boolean isSpent() {
        return spent;
    }

    public void markSpent() {
        this.spent = true;
    }

    public boolean recordHit(Zombie zombie) {
        return hitAlready.add(zombie);
    }

    public int getPierceLeft() {
        return pierceLeft;
    }

    public void setPierceLeft(int pierceLeft) {
        this.pierceLeft = pierceLeft;
    }

    public void spendPierce() {
        if (pierceLeft != Integer.MAX_VALUE) {
            pierceLeft--;
        }
    }
}
