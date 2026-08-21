package models.game;

public class RunningMower {

    private final int row;
    private double x;

    public RunningMower(int row, double x) {
        this.row = row;
        this.x = x;
    }

    public int getRow() {
        return row;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }
}
