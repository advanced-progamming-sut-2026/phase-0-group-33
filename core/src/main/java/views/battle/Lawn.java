package views.battle;

import models.game.GameSession;

public final class Lawn {

    public static final float LEFT = 262f;
    public static final float BOTTOM = 96f;
    public static final float CELL_WIDTH = 106f;
    public static final float CELL_HEIGHT = 100f;
    public static final float WIDTH = CELL_WIDTH * GameSession.COLS;
    public static final float HEIGHT = CELL_HEIGHT * GameSession.ROWS;

    private Lawn() {
    }

    public static float columnLeft(double column) {
        return LEFT + (float) (column - 1) * CELL_WIDTH;
    }

    public static float columnCenter(double column) {
        return columnLeft(column) + CELL_WIDTH / 2f;
    }

    public static float rowBottom(int row) {
        return BOTTOM + (GameSession.ROWS - row) * CELL_HEIGHT;
    }

    public static float rowCenter(int row) {
        return rowBottom(row) + CELL_HEIGHT / 2f;
    }

    public static int columnAt(float x) {
        if (x < LEFT || x > LEFT + WIDTH) {
            return -1;
        }
        return 1 + (int) ((x - LEFT) / CELL_WIDTH);
    }

    public static int rowAt(float y) {
        if (y < BOTTOM || y > BOTTOM + HEIGHT) {
            return -1;
        }
        return GameSession.ROWS - (int) ((y - BOTTOM) / CELL_HEIGHT);
    }

    public static boolean contains(float x, float y) {
        return columnAt(x) >= 1 && rowAt(y) >= 1;
    }
}
