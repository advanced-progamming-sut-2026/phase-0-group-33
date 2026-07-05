package models.game;

public class PushedObject {

    public enum Kind {
        BARREL,
        ICE_BLOCK,
        ARCADE_MACHINE
    }

    private final Kind kind;
    private int health;
    private double x;
    private final int row;
    private boolean moving = true;

    public PushedObject(Kind kind, int health, double x, int row) {
        this.kind = kind;
        this.health = health;
        this.x = x;
        this.row = row;
    }

    public Kind getKind() {
        return kind;
    }

    public int getHealth() {
        return health;
    }

    public void damage(int amount) {
        health -= amount;
    }

    public boolean isDestroyed() {
        return health <= 0;
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

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }
}
