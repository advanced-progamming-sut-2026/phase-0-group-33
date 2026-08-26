package models.game;

import models.entities.plant.PlantType;

import java.util.HashSet;
import java.util.Set;

public class RollingNut {
    private final PlantType type;
    private final Set<Object> struck = new HashSet<>();
    private double x;
    private double lane;
    private double spin;

    public RollingNut(PlantType type, double x, int row) {
        this.type = type;
        this.x = x;
        this.lane = row;
    }

    public PlantType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.spin -= (x - this.x) * 360.0 / (Math.PI * (isGiant() ? 0.8 : 0.62));
        this.x = x;
    }

    public float getSpin() {
        return (float) spin;
    }

    public int getRow() {
        return (int) Math.round(lane);
    }

    public void setRow(int row) {
        this.lane = row;
    }

    public double getLane() {
        return lane;
    }

    public void setLane(double lane) {
        this.lane = lane;
    }

    public boolean recordHit(Object zombie) {
        return struck.add(zombie);
    }

    public boolean isGiant() {
        return type == PlantType.TALL_NUT;
    }

    public boolean isExplosive() {
        return type == PlantType.EXPLODE_O_NUT;
    }
}
