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
import views.assets.Animations;
import views.assets.Art;
import pvz.libpvz.pam.ClipRef;

public class LawnView extends Actor {

    private static final Color GRID_LINE = new Color(1f, 0.25f, 0.25f, 0.55f);
    private static final Color WATER = new Color(0.24f, 0.5f, 0.86f, 0.42f);
    private static final Color LOW_TIDE = new Color(0.55f, 0.75f, 0.95f, 0.35f);
    private static final Color SLIDER = new Color(0.65f, 0.9f, 1f, 0.35f);
    private static final Color NECROMANCY = new Color(0.55f, 0.2f, 0.7f, 0.4f);
    private static final Color HOVER = new Color(1f, 1f, 1f, 0.28f);
    private static final Color DANGER = new Color(1f, 0.2f, 0.2f, 0.75f);
    private static final Color PROTECTED = new Color(0.4f, 1f, 0.5f, 0.3f);
    private static final Color INVALID = new Color(1f, 0.35f, 0.3f, 0.32f);

    protected final GameSession session;
    protected final Art art;
    protected final Drawable fill;

    private static final int GRAVE_MAX_HEALTH = 700;

    private final String chapterName;
    private boolean showGrid;
    private int hoverColumn = -1;
    private int hoverRow = -1;
    private int selectColumn = -1;
    private int selectRow = -1;
    private float time;
    private boolean frozen;
    private boolean hoverValid = true;
    private models.entities.plant.PlantType ghost;
    private final EntityAnimator animator;
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Float> firing =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Integer> lastCooldown =
            new com.badlogic.gdx.utils.ObjectMap<>();

    public LawnView(GameSession session, Art art, Skin skin, Animations animations) {
        this.session = session;
        this.art = art;
        this.animator = new EntityAnimator(animations);
        this.fill = skin.getDrawable("white");
        this.chapterName = session.getLevel() == null ? null
                : session.getLevel().getChapter().getName();
        setBounds(Lawn.left(), Lawn.bottom(), Lawn.width(), Lawn.height());
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void setHover(int column, int row) {
        this.hoverColumn = column;
        this.hoverRow = row;
    }

    public void setGhost(models.entities.plant.PlantType ghost) {
        this.ghost = ghost;
    }

    public void setHoverValid(boolean hoverValid) {
        this.hoverValid = hoverValid;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (frozen) {
            return;
        }
        time += delta;
        trackFiring();
    }

    private void trackFiring() {
        for (models.game.PlacedPlant plant : session.getPlants()) {
            int cooldown = plant.getActionCooldownTicks();
            Integer previous = lastCooldown.get(plant);
            lastCooldown.put(plant, cooldown);
            if (previous == null || previous != 0 || cooldown <= 0) {
                continue;
            }
            String clip = animator.plantClipName(plant.getType(), "attack", "special");
            if (clip == null || "idle".equals(clip)) {
                continue;
            }
            float duration = animator.plantClipDuration(plant.getType(), clip);
            if (duration > 0f) {
                firing.put(plant, time + duration);
            }
        }
        com.badlogic.gdx.utils.Array<models.game.PlacedPlant> stale =
                new com.badlogic.gdx.utils.Array<>();
        for (models.game.PlacedPlant plant : lastCooldown.keys()) {
            if (!session.getPlants().contains(plant)) {
                stale.add(plant);
            }
        }
        for (models.game.PlacedPlant plant : stale) {
            lastCooldown.remove(plant);
            firing.remove(plant);
        }
    }

    private float idlePulse(int seed, float speed, float amount) {
        return 1f + amount * (float) Math.sin(time * speed + seed * 0.7f);
    }

    public void setSelection(int column, int row) {
        this.selectColumn = column;
        this.selectRow = row;
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
        float size = Lawn.cellWidth() * 0.6f;
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
        float height = Lawn.cellHeight() * 0.82f;
        float width = height * region.getRegionWidth() / region.getRegionHeight();
        batch.draw(region, Lawn.columnCenter(column) - width / 2f,
                Lawn.rowBottom(row) + Lawn.cellHeight() * 0.1f, width, height);
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
        float size = Lawn.cellWidth() * 0.32f;
        batch.draw(region, Lawn.columnCenter(column) - size / 2f,
                Lawn.rowBottom(row) + Lawn.cellHeight() * 0.72f, size, size);
    }

    private void drawMowers(Batch batch) {
        TextureRegion region = art.mower();
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float height = Lawn.cellHeight() * 0.72f;
        float width = height * region.getRegionWidth() / region.getRegionHeight();
        for (int row = 1; row <= GameSession.ROWS; row++) {
            if (!session.hasLawnMower(row)) {
                continue;
            }
            batch.draw(region, Lawn.left() - width - 4f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 0.12f, width, height);
        }
    }

    private void drawHover(Batch batch) {
        if (selectColumn >= 1 && selectRow >= 1) {
            paint(batch, PROTECTED, selectColumn, selectRow);
        }
        if (hoverColumn < 1 || hoverRow < 1) {
            return;
        }
        paint(batch, hoverValid ? HOVER : INVALID, hoverColumn, hoverRow);
        if (ghost == null) {
            return;
        }
        float width = Lawn.cellWidth() * 0.78f;
        float height = Lawn.cellHeight() * 0.8f;
        batch.setColor(1f, 1f, 1f, hoverValid ? 0.62f : 0.3f);
        batch.draw(art.plant(ghost), Lawn.columnCenter(hoverColumn) - width / 2f,
                Lawn.rowBottom(hoverRow) + Lawn.cellHeight() * 0.1f, width, height);
    }

    private void drawGrid(Batch batch) {
        batch.setColor(GRID_LINE);
        for (int column = 0; column <= GameSession.COLS; column++) {
            fill.draw(batch, Lawn.left() + column * Lawn.cellWidth(), Lawn.bottom(), 2f, Lawn.height());
        }
        for (int row = 0; row <= GameSession.ROWS; row++) {
            fill.draw(batch, Lawn.left(), Lawn.bottom() + row * Lawn.cellHeight(), Lawn.width(), 2f);
        }
    }

    private void drawSpecialMarkers(Batch batch) {
        if (session.isSpecial(models.progress.level.special.SpecialLevelType.DEAD_LINE)) {
            batch.setColor(DANGER);
            fill.draw(batch, Lawn.columnLeft(4), Lawn.bottom(), 4f, Lawn.height());
        }
        if (session.getMode() == models.game.GameMode.WALLNUT_BOWLING) {
            batch.setColor(DANGER);
            fill.draw(batch, Lawn.columnLeft(4), Lawn.bottom(), 4f, Lawn.height());
        }
        if (session.getMode() == models.game.GameMode.I_ZOMBIE) {
            batch.setColor(DANGER);
            fill.draw(batch, Lawn.columnLeft(6), Lawn.bottom(), 4f, Lawn.height());
        }
        for (models.game.PlacedPlant plant : session.getPlants()) {
            if (plant.isProtectedSeed()) {
                paint(batch, PROTECTED, plant.getX(), plant.getY());
            }
        }
    }

    private void drawBrains(Batch batch) {
        TextureRegion region = art.brain();
        if (region == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float size = Lawn.cellHeight() * 0.6f;
        for (int row = 1; row <= GameSession.ROWS; row++) {
            if (!session.hasBrain(row)) {
                continue;
            }
            batch.draw(region, Lawn.left() - size - 6f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 0.2f, size, size);
        }
    }

    private void drawVases(Batch batch) {
        for (models.game.Vase vase : session.getMinigameManager().getVases()) {
            TextureRegion region = art.vase(vase.getKind());
            if (region == null) {
                continue;
            }
            batch.setColor(Color.WHITE);
            float height = Lawn.cellHeight() * 0.8f;
            float width = height * region.getRegionWidth() / region.getRegionHeight();
            batch.draw(region, Lawn.columnCenter(vase.getX()) - width / 2f,
                    Lawn.rowBottom(vase.getY()) + Lawn.cellHeight() * 0.1f, width, height);
        }
    }

    private void drawEntities(Batch batch) {
        drawBrains(batch);
        drawVases(batch);
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
            float size = Lawn.cellWidth() * projectileScale(shot.getSource());
            float x = Lawn.columnCenter(shot.getX()) - size / 2f;
            float y = Lawn.rowBottom(row) + Lawn.cellHeight() * 0.45f
                    + (float) shot.getHeight() * Lawn.cellHeight() * 0.5f;
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
            float centerX = Lawn.columnCenter(plant.getX());
            float feet = Lawn.rowBottom(row) + Lawn.cellHeight() * 0.16f;
            float width = Lawn.cellWidth() * 0.78f;
            float height = Lawn.cellHeight() * 0.8f;
            batch.setColor(plantTint(plant));
            if (!drawPlantAnimation(batch, plant, centerX, feet)) {
                batch.draw(art.plant(plant.getType()), centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.1f, width, height);
            }
            drawPlantOverlays(batch, plant, centerX - width / 2f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 0.1f, width, height);
            drawHealthBar(batch, plant.getHealth(), plant.getMaxHealth(),
                    centerX - width / 2f, Lawn.rowBottom(row) + Lawn.cellHeight() * 0.94f, width);
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
            float size = Lawn.cellWidth() * 0.6f;
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
            float size = Lawn.cellWidth() * (nut.isGiant() ? 0.8f : 0.62f);
            batch.draw(art.plant(nut.getType()), Lawn.columnCenter(nut.getX()) - size / 2f,
                    Lawn.rowCenter(row) - size / 2f, size, size);
        }
    }

    private void drawZombies(Batch batch, int row) {
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() != row) {
                continue;
            }
            int seed = System.identityHashCode(zombie) & 0xff;
            float centerX = Lawn.columnCenter(zombie.getPosition().getX());
            float feet = Lawn.rowBottom(row) + Lawn.cellHeight() * 0.12f;
            float width = Lawn.cellWidth() * 0.7f;
            float height = Lawn.cellHeight() * 0.92f;
            batch.setColor(zombieTint(zombie));
            if (!drawZombieAnimation(batch, zombie, seed, centerX, feet)) {
                batch.draw(art.zombie(zombie.getType()), centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.06f, width, height);
                drawArmor(batch, zombie, centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.06f, width, height);
            }
            int max = Math.max(1, zombie.getType().getHitpoints());
            drawHealthBar(batch, zombie.getHealth(), max, centerX - width / 2f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 1.02f, width);
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
            float size = sunSize(sun) * idlePulse(sun.getX() * 5 + sun.getY(), 3.1f, 0.07f);
            float targetY = Lawn.rowCenter(sun.getY()) - size / 2f;
            float y = targetY;
            if (sun.isFalling()) {
                float startY = Lawn.bottom() + Lawn.height() + 60f;
                y = startY + (targetY - startY) * sun.getFallProgress();
            }
            batch.setColor(sunColor(sun));
            batch.draw(region, Lawn.columnCenter(sun.getX()) - size / 2f, y, size, size);
        }
    }

    private float sunSize(models.game.Sun sun) {
        return sun.getKind() == models.game.Sun.SunKind.SPECIAL
                ? Lawn.cellWidth() * 0.58f : Lawn.cellWidth() * 0.42f;
    }

    private Color sunColor(models.game.Sun sun) {
        if (sun.getKind() == models.game.Sun.SunKind.RADIOACTIVE) {
            return new Color(0.82f, 0.45f, 1f, 1f);
        }
        return Color.WHITE;
    }

    private boolean drawPlantAnimation(Batch batch, models.game.PlacedPlant plant,
                                       float centerX, float feet) {
        if (!animator.isReady()) {
            return false;
        }
        String[] wanted = plantClipNames(plant);
        ClipRef clip = animator.plantClip(plant.getType(), wanted);
        if (clip == null) {
            return false;
        }
        Float until = firing.get(plant);
        boolean attacking = until != null && time < until;
        float clock = attacking
                ? time - (until - animator.plantClipDuration(plant.getType(), wanted[0]))
                : time + plant.getX() * 0.37f + plant.getY() * 0.19f;
        animator.draw(batch, clip, clock, centerX, feet + animator.plantLift(),
                animator.plantScale(), !attacking, null);
        return true;
    }

    private String[] plantClipNames(models.game.PlacedPlant plant) {
        Float until = firing.get(plant);
        if (until != null && time < until) {
            return new String[] {"attack", "special", "idle"};
        }
        float fraction = plant.getHealth() / (float) Math.max(1, plant.getMaxHealth());
        if (fraction <= 0.34f) {
            return new String[] {"damage3", "damage2", "damage", "idle3", "idle2", "idle"};
        }
        if (fraction <= 0.67f) {
            return new String[] {"damage2", "damage", "idle2", "idle"};
        }
        if (plant.getArmTicks() > 0) {
            return new String[] {"plant_idle", "idle2", "idle"};
        }
        return new String[] {"idle"};
    }

    private boolean drawZombieAnimation(Batch batch, models.entities.zombie.Zombie zombie,
                                        int seed, float centerX, float feet) {
        if (!animator.isReady()) {
            return false;
        }
        boolean eating = isEating(zombie);
        ClipRef clip = eating
                ? animator.zombieClip(zombie.getType(), "eat", "attack", "walk", "idle")
                : animator.zombieClip(zombie.getType(), "walk", "walk1", "idle");
        if (clip == null) {
            return false;
        }
        float clock = zombie.getFrozenTicks() > 0 ? seed * 0.11f : time + seed * 0.11f;
        animator.draw(batch, clip, clock, centerX, feet + animator.zombieLift(),
                animator.zombieScale(), true,
                animator.armourFor(zombie.getType(), armourFraction(zombie)));
        return true;
    }

    private float armourFraction(models.entities.zombie.Zombie zombie) {
        int max = zombie.getType().getArmorType().getArmorHitpoints();
        if (max <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, zombie.totalArmor() / (float) max));
    }

    private boolean isEating(models.entities.zombie.Zombie zombie) {
        int column = (int) Math.round(zombie.getPosition().getX());
        models.game.PlacedPlant blocking = session.plantAt(column, (int) zombie.getPosition().getY());
        return blocking != null && zombie.getPosition().getX() - column <= 0.4
                && zombie.getPosition().getX() - column >= 0;
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
                Lawn.cellWidth(), Lawn.cellHeight());
    }
}
