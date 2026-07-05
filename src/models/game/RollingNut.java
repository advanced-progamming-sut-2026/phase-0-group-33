package models.game;

import models.entities.plant.PlantType;

public class RollingNut {
    private final PlantType type;
    private double x;
    private int row;

    public RollingNut(PlantType type, double x, int row) {
        this.type = type;
        this.x = x;
        this.row = row;
    }

    public PlantType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean isGiant() {
        return type == PlantType.TALL_NUT;
    }

    public boolean isExplosive() {
        return type == PlantType.EXPLODE_O_NUT;
    }
}
