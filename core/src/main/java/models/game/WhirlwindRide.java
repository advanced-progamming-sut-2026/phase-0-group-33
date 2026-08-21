package models.game;

import models.entities.zombie.Zombie;

public class WhirlwindRide {

    private final Zombie zombie;
    private final double fromX;
    private final double toX;
    private final int row;

    public WhirlwindRide(Zombie zombie, double fromX, double toX, int row) {
        this.zombie = zombie;
        this.fromX = fromX;
        this.toX = toX;
        this.row = row;
    }

    public Zombie getZombie() {
        return zombie;
    }

    public double getFromX() {
        return fromX;
    }

    public double getToX() {
        return toX;
    }

    public int getRow() {
        return row;
    }
}
