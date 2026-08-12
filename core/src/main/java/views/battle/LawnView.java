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

    private static final int GRAVE_MAX_HEALTH = 700;

    private final String chapterName;
    private boolean showGrid;
    private int hoverColumn = -1;
    private int hoverRow = -1;

    public LawnView(GameSession session, Art art, Skin skin) {
        this.session = session;
        this.art = art;
        this.fill = skin.getDrawable("white");
        this.chapterName = session.getLevel() == null ? null
                : session.getLevel().getChapter().getName();
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
        drawEntities(batch);
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
        if (tile.getTerrain() == TerrainType.GRAVE) {
            drawGrave(batch, tile, column, row);
            return;
        }
        String icon = null;
        if (tile.getTerrain() == TerrainType.SLIDER_UP) {
            icon = "image_ui_almanac_stats_screen_nav_arrow_next";
        } else if (tile.getTerrain() == TerrainType.SLIDER_DOWN) {
            icon = "image_ui_almanac_stats_screen_nav_arrow_previous";
        } else if (tile.isHasLilyPad()) {
            icon = "image_ui_generic_leaf_backdrop";
        }
        if (icon == null) {
            return;
        }
        TextureRegion region = art.uiOptional(icon);
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float size = Lawn.CELL_WIDTH * 0.6f;
        batch.draw(region, Lawn.columnCenter(column) - size / 2f,
                Lawn.rowCenter(row) - size / 2f, size, size);
    }

    private void drawGrave(Batch batch, Tile tile, int column, int row) {
        float fraction = Math.max(0f, Math.min(1f, tile.getGraveHealth() / (float) GRAVE_MAX_HEALTH));
        TextureRegion region = art.grave(chapterName, fraction);
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float height = Lawn.CELL_HEIGHT * 0.82f;
        float width = height * region.getRegionWidth() / region.getRegionHeight();
        batch.draw(region, Lawn.columnCenter(column) - width / 2f,
                Lawn.rowBottom(row) + Lawn.CELL_HEIGHT * 0.1f, width, height);
        drawGraveContent(batch, tile, column, row);
    }

    private void drawGraveContent(Batch batch, Tile tile, int column, int row) {
        String icon = tile.getGraveSunContent() > 0 ? "image_ui_hud_ingame_sun"
                : tile.isGravePlantFood() ? "image_ui_hud_ingame_plantfood_button" : null;
        if (icon == null) {
            return;
        }
        TextureRegion region = art.uiOptional(icon);
        if (region == null) {
            return;
        }
        float size = Lawn.CELL_WIDTH * 0.32f;
        batch.draw(region, Lawn.columnCenter(column) - size / 2f,
                Lawn.rowBottom(row) + Lawn.CELL_HEIGHT * 0.72f, size, size);
    }

    private void drawMowers(Batch batch) {
        TextureRegion region = art.mower();
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float height = Lawn.CELL_HEIGHT * 0.72f;
        float width = height * region.getRegionWidth() / region.getRegionHeight();
        for (int row = 1; row <= GameSession.ROWS; row++) {
            if (!session.hasLawnMower(row)) {
                continue;
            }
            batch.draw(region, Lawn.LEFT - width - 4f,
                    Lawn.rowBottom(row) + Lawn.CELL_HEIGHT * 0.12f, width, height);
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

    private void drawEntities(Batch batch) {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            drawPlants(batch, row);
            drawPushed(batch, row);
            drawZombies(batch, row);
            drawProjectiles(batch, row);
        }
        drawSuns(batch);
    }

    private void drawProjectiles(Batch batch, int row) {
        TextureRegion region = art.pea();
        if (region == null) {
            return;
        }
        for (models.game.Projectile shot : session.getProjectileManager().getProjectiles()) {
            if (shot.getRow() != row) {
                continue;
            }
            float size = Lawn.CELL_WIDTH * projectileScale(shot.getSource());
            float x = Lawn.columnCenter(shot.getX()) - size / 2f;
            float y = Lawn.rowBottom(row) + Lawn.CELL_HEIGHT * 0.45f
                    + (float) shot.getHeight() * Lawn.CELL_HEIGHT * 0.5f;
            batch.setColor(projectileTint(shot.getSource()));
            batch.draw(region, x, y, size, size);
        }
    }

    private float projectileScale(models.entities.plant.PlantType source) {
        if (source.getTags().contains(models.entities.plant.PlantTag.AOE)) {
            return 0.42f;
        }
        return source.getCategory() == models.entities.plant.PlantCategory.STRIKE_THROUGH
                ? 0.38f : 0.26f;
    }

    private Color projectileTint(models.entities.plant.PlantType source) {
        java.util.Set<models.entities.plant.PlantTag> tags = source.getTags();
        if (tags.contains(models.entities.plant.PlantTag.FIRE)) {
            return new Color(1f, 0.78f, 0.22f, 1f);
        }
        if (tags.contains(models.entities.plant.PlantTag.ICE)) {
            return new Color(0.55f, 1f, 1f, 1f);
        }
        if (tags.contains(models.entities.plant.PlantTag.POISON)) {
            return new Color(0.7f, 0.35f, 0.9f, 1f);
        }
        if (source.getCategory() == models.entities.plant.PlantCategory.STRIKE_THROUGH) {
            return new Color(0.92f, 0.95f, 0.8f, 0.75f);
        }
        if (tags.contains(models.entities.plant.PlantTag.AOE)) {
            return new Color(0.55f, 0.95f, 0.4f, 1f);
        }
        return Color.WHITE;
    }

    private void drawPlants(Batch batch, int row) {
        for (models.game.PlacedPlant plant : session.getPlants()) {
            if (plant.getY() != row) {
                continue;
            }
            float width = Lawn.CELL_WIDTH * 0.74f;
            float height = Lawn.CELL_HEIGHT * 0.74f;
            float x = Lawn.columnCenter(plant.getX()) - width / 2f;
            float y = Lawn.rowBottom(row) + Lawn.CELL_HEIGHT * 0.12f;
            batch.setColor(plantTint(plant));
            batch.draw(art.plant(plant.getType()), x, y, width, height);
            drawPlantOverlays(batch, plant, x, y, width, height);
            drawHealthBar(batch, plant.getHealth(), plant.getMaxHealth(),
                    x, y + height + 3f, width);
        }
    }

    private Color plantTint(models.game.PlacedPlant plant) {
        if (plant.isSheep()) {
            return new Color(0.95f, 0.95f, 1f, 1f);
        }
        if (plant.getArmTicks() > 0) {
            return new Color(0.7f, 0.7f, 0.7f, 0.85f);
        }
        return Color.WHITE;
    }

    private void drawPlantOverlays(Batch batch, models.game.PlacedPlant plant,
                                   float x, float y, float width, float height) {
        if (plant.getIceHealth() > 0 || plant.getFreezeLevel() > 0) {
            float alpha = 0.2f + 0.2f * Math.min(3, Math.max(1, plant.getFreezeLevel()));
            batch.setColor(0.6f, 0.85f, 1f, plant.getIceHealth() > 0 ? 0.75f : alpha);
            fill.draw(batch, x, y, width, height);
        }
        if (plant.getOctopusHealth() > 0) {
            batch.setColor(Color.WHITE);
            TextureRegion octopus = art.zombie(models.entities.zombie.ZombieType.OCTOPUS);
            if (octopus != null) {
                batch.draw(octopus, x + width * 0.15f, y + height * 0.3f, width * 0.7f, height * 0.7f);
            }
        }
        if (plant.getPumpkinHealth() > 0) {
            batch.setColor(1f, 0.65f, 0.2f, 0.45f);
            fill.draw(batch, x, y, width, height);
        }
    }

    private void drawPushed(Batch batch, int row) {
        for (models.game.PushedObject pushed : session.getPushedObjects()) {
            if (pushed.getRow() != row || pushed.isDestroyed()) {
                continue;
            }
            batch.setColor(Color.WHITE);
            float size = Lawn.CELL_WIDTH * 0.6f;
            TextureRegion region = art.uiOptional("image_ui_generic_leaf_backdrop");
            if (region != null) {
                batch.draw(region, Lawn.columnCenter(pushed.getX()) - size / 2f,
                        Lawn.rowCenter(row) - size / 2f, size, size);
            }
        }
        for (models.game.RollingNut nut : session.getMinigameManager().getNuts()) {
            if (nut.getRow() != row) {
                continue;
            }
            batch.setColor(Color.WHITE);
            float size = Lawn.CELL_WIDTH * (nut.isGiant() ? 0.8f : 0.62f);
            batch.draw(art.plant(nut.getType()), Lawn.columnCenter(nut.getX()) - size / 2f,
                    Lawn.rowCenter(row) - size / 2f, size, size);
        }
    }

    private void drawZombies(Batch batch, int row) {
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() != row) {
                continue;
            }
            float width = Lawn.CELL_WIDTH * 0.66f;
            float height = Lawn.CELL_HEIGHT * 0.86f;
            float x = Lawn.columnCenter(zombie.getPosition().getX()) - width / 2f;
            float y = Lawn.rowBottom(row) + Lawn.CELL_HEIGHT * 0.08f;
            batch.setColor(zombieTint(zombie));
            batch.draw(art.zombie(zombie.getType()), x, y, width, height);
            if (zombie.getFrozenTicks() > 0) {
                batch.setColor(0.6f, 0.85f, 1f, 0.75f);
                fill.draw(batch, x, y, width, height);
            }
            drawArmor(batch, zombie, x, y, width, height);
            int max = Math.max(1, zombie.getType().getHitpoints());
            drawHealthBar(batch, zombie.getHealth(), max, x, y + height + 3f, width);
        }
    }

    private Color zombieTint(models.entities.zombie.Zombie zombie) {
        if (zombie.getBattle().isHypnotized()) {
            return new Color(0.75f, 0.5f, 1f, 1f);
        }
        if (zombie.getFrozenTicks() > 0) {
            return new Color(0.7f, 0.85f, 1f, 1f);
        }
        if (zombie.getChilledTicks() > 0) {
            return new Color(0.72f, 0.88f, 1f, 1f);
        }
        if (zombie.getBattle().getPoisonTicksLeft() > 0) {
            return new Color(0.75f, 1f, 0.6f, 1f);
        }
        if (zombie.isGlowing()) {
            return new Color(0.7f, 1f, 0.7f, 1f);
        }
        if (zombie.getPosition().getX() < 2.5) {
            return new Color(1f, 0.6f, 0.6f, 1f);
        }
        return Color.WHITE;
    }

    private void drawArmor(Batch batch, models.entities.zombie.Zombie zombie,
                           float x, float y, float width, float height) {
        if (zombie.totalArmor() <= 0) {
            return;
        }
        TextureRegion region = art.uiOptional("image_ui_lock_small_gold");
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float size = width * 0.4f;
        batch.draw(region, x + width - size * 0.7f, y + height - size * 0.6f, size, size);
    }

    private void drawSuns(Batch batch) {
        TextureRegion region = art.uiOptional("image_ui_hud_ingame_sun");
        if (region == null) {
            return;
        }
        for (models.game.Sun sun : session.getSunManager().getSuns()) {
            float size = sunSize(sun);
            float targetY = Lawn.rowCenter(sun.getY()) - size / 2f;
            float y = targetY;
            if (sun.isFalling()) {
                float startY = Lawn.BOTTOM + Lawn.HEIGHT + 60f;
                y = startY + (targetY - startY) * sun.getFallProgress();
            }
            batch.setColor(sunColor(sun));
            batch.draw(region, Lawn.columnCenter(sun.getX()) - size / 2f, y, size, size);
        }
    }

    private float sunSize(models.game.Sun sun) {
        return sun.getKind() == models.game.Sun.SunKind.SPECIAL
                ? Lawn.CELL_WIDTH * 0.58f : Lawn.CELL_WIDTH * 0.42f;
    }

    private Color sunColor(models.game.Sun sun) {
        if (sun.getKind() == models.game.Sun.SunKind.RADIOACTIVE) {
            return new Color(0.82f, 0.45f, 1f, 1f);
        }
        return Color.WHITE;
    }

    private void drawHealthBar(Batch batch, int health, int max, float x, float y, float width) {
        float fraction = Math.max(0f, Math.min(1f, health / (float) max));
        if (fraction >= 1f) {
            return;
        }
        batch.setColor(0f, 0f, 0f, 0.55f);
        fill.draw(batch, x, y, width, 6f);
        batch.setColor(fraction > 0.5f ? Color.LIME : fraction > 0.25f ? Color.ORANGE : Color.RED);
        fill.draw(batch, x, y, width * fraction, 6f);
    }

    protected void paint(Batch batch, Color color, double column, int row) {
        batch.setColor(color);
        fill.draw(batch, Lawn.columnLeft(column), Lawn.rowBottom(row),
                Lawn.CELL_WIDTH, Lawn.CELL_HEIGHT);
    }
}
