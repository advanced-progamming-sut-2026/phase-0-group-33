package views.battle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import models.game.GameSession;

public final class Lawn {

    public static final float STAGE_WIDTH = 1280f;
    public static final float STAGE_HEIGHT = 720f;

    private static final float FX0 = 0.24707f;
    private static final float FX1 = 0.96777f;
    private static final float FY_TOP = 0.25781f;
    private static final float FY_BOTTOM = 0.90365f;

    private static float left = 395f;
    private static float bottom = 69f;
    private static float cellWidth = 76.9f;
    private static float cellHeight = 93f;

    private Lawn() {
    }

    public static void configure(TextureRegion background) {
        if (background == null) {
            return;
        }
        float imageWidth = background.getRegionWidth();
        float imageHeight = background.getRegionHeight();
        float scale = Math.min(STAGE_WIDTH / imageWidth, STAGE_HEIGHT / imageHeight);
        float offsetX = (STAGE_WIDTH - imageWidth * scale) / 2f;
        float offsetY = (STAGE_HEIGHT - imageHeight * scale) / 2f;

        left = offsetX + FX0 * imageWidth * scale;
        cellWidth = (FX1 - FX0) * imageWidth * scale / GameSession.COLS;
        bottom = offsetY + (1f - FY_BOTTOM) * imageHeight * scale;
        cellHeight = (FY_BOTTOM - FY_TOP) * imageHeight * scale / GameSession.ROWS;
    }

    public static float left() {
        return left;
    }

    public static float bottom() {
        return bottom;
    }

    public static float cellWidth() {
        return cellWidth;
    }

    public static float cellHeight() {
        return cellHeight;
    }

    public static float width() {
        return cellWidth * GameSession.COLS;
    }

    public static float height() {
        return cellHeight * GameSession.ROWS;
    }

    public static float top() {
        return bottom + height();
    }

    public static float columnLeft(double column) {
        return left + (float) (column - 1) * cellWidth;
    }

    public static float columnCenter(double column) {
        return columnLeft(column) + cellWidth / 2f;
    }

    public static float rowBottom(double row) {
        return bottom + (float) (GameSession.ROWS - row) * cellHeight;
    }

    public static float rowCenter(int row) {
        return rowBottom(row) + cellHeight / 2f;
    }

    public static int columnAt(float x) {
        if (x < left || x > left + width()) {
            return -1;
        }
        return 1 + (int) ((x - left) / cellWidth);
    }

    public static int rowAt(float y) {
        if (y < bottom || y > bottom + height()) {
            return -1;
        }
        return GameSession.ROWS - (int) ((y - bottom) / cellHeight);
    }
}
