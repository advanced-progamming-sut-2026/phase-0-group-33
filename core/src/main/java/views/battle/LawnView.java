package views.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import models.game.GameSession;
import models.map.TerrainType;
import models.map.Tile;
import views.assets.Art;

public class LawnView extends Actor {

    private static final Color GRID_LINE = new Color(1f, 0.25f, 0.25f, 0.55f);
    private static final Color WATER = new Color(0.24f, 0.5f, 0.86f, 0.42f);
    private static final Color LOW_TIDE = new Color(0.55f, 0.75f, 0.95f, 0.35f);
    private static final Color SLIDER = new Color(0.65f, 0.9f, 1f, 0.35f);
    private static final Color NECROMANCY = new Color(0.55f, 0.2f, 0.7f, 0.4f);
    private static final Color HOVER = new Color(1f, 1f, 1f, 0.28f);
    private static final Color DANGER = new Color(1f, 0.2f, 0.2f, 0.75f);
    private static final Color PROTECTED = new Color(0.4f, 1f, 0.5f, 0.3f);

    protected final GameSession session;
    protected final Art art;
    protected final Drawable fill;

    private boolean showGrid;
    private int hoverColumn = -1;
    private int hoverRow = -1;

    public LawnView(GameSession session, Art art, Skin skin) {
        this.session = session;
        this.art = art;
        this.fill = skin.getDrawable("white");
        setBounds(Lawn.LEFT, Lawn.BOTTOM, Lawn.WIDTH, Lawn.HEIGHT);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void setHover(int column, int row) {
        this.hoverColumn = column;
        this.hoverRow = row;
    }

    public void clearHover() {
        this.hoverColumn = -1;
        this.hoverRow = -1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color previous = batch.getColor().cpy();
        drawTerrain(batch);
        drawMowers(batch);
        drawHover(batch);
        if (showGrid) {
            drawGrid(batch);
        }
        drawSpecialMarkers(batch);
        batch.setColor(previous);
    }

    private void drawTerrain(Batch batch) {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int column = 1; column <= GameSession.COLS; column++) {
                Tile tile = session.getGrid().getTile(column - 1, row - 1);
                if (tile == null) {
                    continue;
                }
                Color tint = terrainTint(tile);
                if (tint != null) {
                    paint(batch, tint, column, row);
                }
                if (tile.isNecromancy()) {
                    paint(batch, NECROMANCY, column, row);
                }
                drawTerrainIcon(batch, tile, column, row);
            }
        }
    }

    private Color terrainTint(Tile tile) {
        if (tile.getTerrain() == TerrainType.WATER) {
            return tile.isLowTide() ? LOW_TIDE : WATER;
        }
        if (tile.getTerrain() == TerrainType.SLIDER_UP || tile.getTerrain() == TerrainType.SLIDER_DOWN) {
            return SLIDER;
        }
        return null;
    }

    private void drawTerrainIcon(Batch batch, Tile tile, int column, int row) {
        String icon = null;
        if (tile.getTerrain() == TerrainType.GRAVE) {
            icon = tile.getGraveSunContent() > 0 ? "image_ui_hud_ingame_sun"
                    : tile.isGravePlantFood() ? "image_ui_hud_ingame_plantfood_button"
                    : "image_ui_generic_tombstone_icon";
        } else if (tile.getTerrain() == TerrainType.SLIDER_UP) {
            icon = "image_ui_almanac_stats_screen_nav_arrow_next";
        } else if (tile.getTerrain() == TerrainType.SLIDER_DOWN) {
            icon = "image_ui_almanac_stats_screen_nav_arrow_previous";
        } else if (tile.isHasLilyPad()) {
            icon = "image_ui_generic_leaf_backdrop";
        }
        if (icon == null) {
            return;
        }
        TextureRegion region = art.ui(icon);
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float size = Lawn.CELL_WIDTH * 0.6f;
        batch.draw(region, Lawn.columnCenter(column) - size / 2f,
                Lawn.rowCenter(row) - size / 2f, size, size);
    }

    private void drawMowers(Batch batch) {
        TextureRegion region = art.ui("image_ui_generic_button_hud_minigames_normal");
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        for (int row = 1; row <= GameSession.ROWS; row++) {
            if (!session.hasLawnMower(row)) {
                continue;
            }
            float size = Lawn.CELL_HEIGHT * 0.62f;
            batch.draw(region, Lawn.LEFT - size - 6f, Lawn.rowCenter(row) - size / 2f, size, size);
        }
    }

    private void drawHover(Batch batch) {
        if (hoverColumn < 1 || hoverRow < 1) {
            return;
        }
        paint(batch, HOVER, hoverColumn, hoverRow);
    }

    private void drawGrid(Batch batch) {
        batch.setColor(GRID_LINE);
        for (int column = 0; column <= GameSession.COLS; column++) {
            fill.draw(batch, Lawn.LEFT + column * Lawn.CELL_WIDTH, Lawn.BOTTOM, 2f, Lawn.HEIGHT);
        }
        for (int row = 0; row <= GameSession.ROWS; row++) {
            fill.draw(batch, Lawn.LEFT, Lawn.BOTTOM + row * Lawn.CELL_HEIGHT, Lawn.WIDTH, 2f);
        }
    }

    private void drawSpecialMarkers(Batch batch) {
        if (session.isSpecial(models.progress.level.special.SpecialLevelType.DEAD_LINE)) {
            batch.setColor(DANGER);
            fill.draw(batch, Lawn.columnLeft(4), Lawn.BOTTOM, 4f, Lawn.HEIGHT);
        }
        for (models.game.PlacedPlant plant : session.getPlants()) {
            if (plant.isProtectedSeed()) {
                paint(batch, PROTECTED, plant.getX(), plant.getY());
            }
        }
    }

    protected void paint(Batch batch, Color color, double column, int row) {
        batch.setColor(color);
        fill.draw(batch, Lawn.columnLeft(column), Lawn.rowBottom(row),
                Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
    }
}
