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
    private static final String PLANT_FOOD_GLOW = "PLANTFOOD_FX";
    private static final String ICE_PLANT = "FROSTBITE_ICE_BLOCK_PLANT";
    private static final String ICE_ZOMBIE = "FROSTBITE_ICE_BLOCK_ZOMBIE";
    private static final String TIDE_LINE = "WATER_TIDE_LINE";
    private static final String WATER_TILE = "WATER_SQUARE";
    private static final float HURT_FLASH = 0.14f;
    private static final float IMP_FLIGHT = 0.85f;
    private static final float IMP_ARC = 1.9f;
    private static final String SANDSTORM = "SANDSTORM_TOP";
    private static final String SNOWSTORM = "SNOWSTORM_TOP";
    private static final float STORM_LIFE = 1.4f;
    private static final String BOOM = "CHERRYBOMB_EXPLOSION_TOP";

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
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Integer> lastAbility =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Float> ability =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Boolean> lastSunPending =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<Object, int[]> lastHealth =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<Object, Float> hurtUntil =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.Array<Corpse> blasts = new com.badlogic.gdx.utils.Array<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, int[]> plantSpots =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private boolean shakePending;
    private final com.badlogic.gdx.utils.Array<float[]> storms = new com.badlogic.gdx.utils.Array<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Integer> lastFreeze =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, float[]> flights =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.Array<Corpse> corpses = new com.badlogic.gdx.utils.Array<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, float[]> seen =
            new com.badlogic.gdx.utils.ObjectMap<>();

    private static final class Corpse {
        private final models.entities.zombie.ZombieType type;
        private final float x;
        private final int row;
        private final float start;
        private final float end;
        private final boolean parts;

        private Corpse(models.entities.zombie.ZombieType type, float x, int row,
                       float start, float end) {
            this(type, x, row, start, end, false);
        }

        private Corpse(models.entities.zombie.ZombieType type, float x, int row,
                       float start, float end, boolean parts) {
            this.type = type;
            this.x = x;
            this.row = row;
            this.start = start;
            this.end = end;
            this.parts = parts;
        }
    }

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

    public EntityAnimator animator() {
        return animator;
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
        trackAbilities();
        trackDamage();
        trackDeaths();
        trackBlasts();
        trackStorms();
    }

    private void trackStorms() {
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, float[]> entry
                : seen.entries()) {
            models.entities.zombie.Zombie zombie = entry.key;
            if (!session.getZombies().contains(zombie)) {
                continue;
            }
            float previousX = entry.value[0];
            float nowX = (float) zombie.getPosition().getX();
            if (previousX - nowX > 0.9f) {
                storms.add(new float[] {nowX, (int) zombie.getPosition().getY(), time, 0f});
            }
        }
        for (models.game.PlacedPlant plant : session.getPlants()) {
            Integer previous = lastFreeze.get(plant);
            int level = plant.getFreezeLevel();
            lastFreeze.put(plant, level);
            if (previous != null && level > previous) {
                storms.add(new float[] {0f, plant.getY(), time, 1f});
            }
        }
        com.badlogic.gdx.utils.Array<models.game.PlacedPlant> gone =
                new com.badlogic.gdx.utils.Array<>();
        for (models.game.PlacedPlant plant : lastFreeze.keys()) {
            if (!session.getPlants().contains(plant)) {
                gone.add(plant);
            }
        }
        for (models.game.PlacedPlant plant : gone) {
            lastFreeze.remove(plant);
        }
        for (int i = storms.size - 1; i >= 0; i--) {
            if (time - storms.get(i)[2] >= STORM_LIFE) {
                storms.removeIndex(i);
            }
        }
    }

    private void drawStorms(Batch batch) {
        if (!animator.isReady()) {
            return;
        }
        for (float[] storm : storms) {
            boolean wind = storm[3] > 0.5f;
            String animation = wind ? SNOWSTORM : SANDSTORM;
            String clipName = animator.clipName(animation, "animation", "idle");
            ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
            if (clip == null) {
                continue;
            }
            int row = (int) storm[1];
            float age = time - storm[2];
            float fade = Math.min(0.7f, 1.6f * (1f - age / STORM_LIFE));
            batch.setColor(1f, 1f, 1f, Math.max(0f, fade));
            if (wind) {
                float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 1.4f);
                float lift = animator.centreOffset(animation, clipName, scale);
                for (int column = 2; column <= GameSession.COLS; column += 3) {
                    animator.draw(batch, clip, age + column * 0.09f, Lawn.columnCenter(column),
                            Lawn.rowCenter(row) + lift, scale, true, null);
                }
            } else {
                float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 1.6f);
                float lift = animator.centreOffset(animation, clipName, scale);
                animator.draw(batch, clip, age, Lawn.columnCenter(storm[0]),
                        Lawn.rowCenter(row) + lift, scale, true, null);
            }
            batch.setColor(Color.WHITE);
        }
    }

    public boolean consumeShake() {
        boolean value = shakePending;
        shakePending = false;
        return value;
    }

    private void trackBlasts() {
        for (models.game.PlacedPlant plant : session.getPlants()) {
            plantSpots.put(plant, new int[] {plant.getX(), plant.getY()});
        }
        com.badlogic.gdx.utils.Array<models.game.PlacedPlant> gone =
                new com.badlogic.gdx.utils.Array<>();
        for (models.game.PlacedPlant plant : plantSpots.keys()) {
            if (!session.getPlants().contains(plant)) {
                gone.add(plant);
            }
        }
        for (models.game.PlacedPlant plant : gone) {
            int[] spot = plantSpots.remove(plant);
            if (!isExplosive(plant)) {
                continue;
            }
            float duration = animator.namedClipDuration(BOOM, "explosion");
            if (duration <= 0f) {
                continue;
            }
            blasts.add(new Corpse(null, spot[0], spot[1], time, time + duration));
            shakePending = true;
        }
        for (int i = blasts.size - 1; i >= 0; i--) {
            if (time >= blasts.get(i).end) {
                blasts.removeIndex(i);
            }
        }
    }

    private boolean isExplosive(models.game.PlacedPlant plant) {
        return plant.getType().getCategory() == models.entities.plant.PlantCategory.EXPLOSIVE
                || plant.getType().getTags().contains(models.entities.plant.PlantTag.EXPLOSIVE);
    }

    private void trackDamage() {
        for (models.game.PlacedPlant plant : session.getPlants()) {
            noteHealth(plant, plant.getHealth() + plant.getPumpkinHealth() + plant.getIceHealth());
        }
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            noteHealth(zombie, zombie.getHealth() + zombie.totalArmor());
        }
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int column = 1; column <= GameSession.COLS; column++) {
                Tile tile = session.getGrid().getTile(column - 1, row - 1);
                if (tile != null && tile.getTerrain() == TerrainType.GRAVE) {
                    noteHealth(tile, tile.getGraveHealth());
                }
            }
        }
    }

    private void noteHealth(Object key, int health) {
        int[] previous = lastHealth.get(key);
        if (previous == null) {
            lastHealth.put(key, new int[] {health});
            return;
        }
        if (health < previous[0]) {
            hurtUntil.put(key, time + HURT_FLASH);
        }
        previous[0] = health;
    }

    private boolean isHurt(Object key) {
        Float until = hurtUntil.get(key);
        return until != null && time < until;
    }

    private void flash(Batch batch, Runnable draw) {
        int srcFunc = batch.getBlendSrcFunc();
        int dstFunc = batch.getBlendDstFunc();
        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE);
        batch.setColor(0.55f, 0.55f, 0.55f, 1f);
        draw.run();
        batch.setBlendFunction(srcFunc, dstFunc);
        batch.setColor(Color.WHITE);
    }

    private void trackAbilities() {
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            String clip = views.assets.AnimationCatalog.abilityClip(zombie.getType());
            if (clip == null || !animator.hasZombieClip(zombie.getType(), clip)) {
                continue;
            }
            int cooldown = zombie.getBattle().getAbilityCooldown();
            Integer previous = lastAbility.get(zombie);
            lastAbility.put(zombie, cooldown);
            if (previous != null && cooldown > previous) {
                float duration = animator.zombieClipDuration(zombie.getType(), clip);
                if (duration > 0f) {
                    ability.put(zombie, time + duration);
                }
            }
        }
        for (models.entities.zombie.Zombie zombie : zombieKeys(lastAbility)) {
            if (!session.getZombies().contains(zombie)) {
                lastAbility.remove(zombie);
                ability.remove(zombie);
            }
        }
    }

    private com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> zombieKeys(
            com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Integer> map) {
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> keys =
                new com.badlogic.gdx.utils.Array<>();
        for (models.entities.zombie.Zombie zombie : map.keys()) {
            keys.add(zombie);
        }
        return keys;
    }

    private void trackDeaths() {
        com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, float[]> current =
                new com.badlogic.gdx.utils.ObjectMap<>();
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            current.put(zombie, new float[] {
                (float) zombie.getPosition().getX(), (int) zombie.getPosition().getY()});
        }
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, float[]> entry
                : seen.entries()) {
            if (current.containsKey(entry.key)) {
                continue;
            }
            String clip = animator.zombieClipName(entry.key.getType(), "die");
            if (clip == null || !"die".equals(clip)) {
                continue;
            }
            float duration = animator.zombieClipDuration(entry.key.getType(), clip);
            if (duration <= 0f) {
                continue;
            }
            corpses.add(new Corpse(entry.key.getType(), entry.value[0], (int) entry.value[1],
                    time, time + duration));
            if (animator.hasZombieClip(entry.key.getType(), "particles")) {
                float parts = animator.zombieClipDuration(entry.key.getType(), "particles");
                if (parts > 0f) {
                    corpses.add(new Corpse(entry.key.getType(), entry.value[0],
                            (int) entry.value[1], time, time + parts, true));
                }
            }
        }
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, float[]> entry
                : current.entries()) {
            if (!seen.containsKey(entry.key)) {
                noteArrival(entry.key);
            }
        }
        seen.clear();
        seen.putAll(current);
        for (models.entities.zombie.Zombie zombie : zombieFlightKeys()) {
            float[] flight = flights.get(zombie);
            if (!session.getZombies().contains(zombie) || time >= flight[2]) {
                flights.remove(zombie);
            }
        }
        for (int i = corpses.size - 1; i >= 0; i--) {
            if (time >= corpses.get(i).end) {
                corpses.removeIndex(i);
            }
        }
    }

    private void noteArrival(models.entities.zombie.Zombie zombie) {
        if (zombie.getType() != models.entities.zombie.ZombieType.IMP) {
            return;
        }
        models.entities.zombie.Zombie thrower = null;
        for (models.entities.zombie.Zombie other : session.getZombies()) {
            if (other.getType() == models.entities.zombie.ZombieType.GARGANTUAR
                    && (int) other.getPosition().getY() == (int) zombie.getPosition().getY()) {
                thrower = other;
                break;
            }
        }
        if (thrower == null) {
            return;
        }
        flights.put(zombie, new float[] {
            (float) thrower.getPosition().getX(), time, time + IMP_FLIGHT});
    }

    private com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> zombieFlightKeys() {
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> keys =
                new com.badlogic.gdx.utils.Array<>();
        for (models.entities.zombie.Zombie zombie : flights.keys()) {
            keys.add(zombie);
        }
        return keys;
    }

    private void trackFiring() {
        for (models.game.PlacedPlant plant : session.getPlants()) {
            int cooldown = plant.getActionCooldownTicks();
            Integer previous = lastCooldown.get(plant);
            lastCooldown.put(plant, cooldown);
            if (previous == null || previous != 0 || cooldown <= 0) {
                continue;
            }
            trackSunProduction(plant);
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
            lastSunPending.remove(plant);
        }
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
        drawTideLine(batch);
        drawSpecialMarkers(batch);
        drawEntities(batch);
        drawStorms(batch);
        batch.setColor(previous);
    }

    private void paintNothing() {
        return;
    }

    private boolean drawWaterTile(Batch batch, int column, int row) {
        if (!animator.isReady()) {
            return false;
        }
        ClipRef clip = animator.namedClip(WATER_TILE, "idle", "animation");
        if (clip == null) {
            return false;
        }
        String clipName = animator.clipName(WATER_TILE, "idle", "animation");
        float scale = animator.fitScale(WATER_TILE, clipName, Lawn.cellHeight());
        float lift = animator.centreOffset(WATER_TILE, clipName, scale);
        batch.setColor(1f, 1f, 1f, 0.85f);
        animator.draw(batch, clip, time + column * 0.13f, Lawn.columnCenter(column),
                Lawn.rowCenter(row) + lift, scale, true, null);
        batch.setColor(Color.WHITE);
        return true;
    }

    private void drawTideLine(Batch batch) {
        if (!animator.isReady() || session.getLevel() == null) {
            return;
        }
        int firstWater = -1;
        for (int column = 1; column <= GameSession.COLS && firstWater < 0; column++) {
            for (int row = 1; row <= GameSession.ROWS; row++) {
                Tile tile = session.getGrid().getTile(column - 1, row - 1);
                if (tile != null && tile.getTerrain() == TerrainType.WATER) {
                    firstWater = column;
                    break;
                }
            }
        }
        if (firstWater < 1) {
            return;
        }
        ClipRef clip = animator.namedClip(TIDE_LINE, "idle");
        if (clip == null) {
            return;
        }
        String clipName = animator.clipName(TIDE_LINE, "idle");
        float scale = animator.fitScale(TIDE_LINE, clipName, Lawn.height());
        float lift = animator.centreOffset(TIDE_LINE, clipName, scale);
        batch.setColor(Color.WHITE);
        animator.draw(batch, clip, time, Lawn.columnLeft(firstWater),
                Lawn.bottom() + Lawn.height() / 2f + lift, scale, true, null);
    }

    private void drawTerrain(Batch batch) {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int column = 1; column <= GameSession.COLS; column++) {
                Tile tile = session.getGrid().getTile(column - 1, row - 1);
                if (tile == null) {
                    continue;
                }
                if (tile.getTerrain() == TerrainType.WATER && !tile.isLowTide()
                        && drawWaterTile(batch, column, row)) {
                    paintNothing();
                } else {
                    Color tint = terrainTint(tile);
                    if (tint != null) {
                        paint(batch, tint, column, row);
                    } else if (tile.getTerrain() == TerrainType.WATER) {
                        paint(batch, WATER, column, row);
                    }
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
            return tile.isLowTide() ? LOW_TIDE : null;
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
        float gx = Lawn.columnCenter(column) - width / 2f;
        float gy = Lawn.rowBottom(row) + Lawn.cellHeight() * 0.1f;
        batch.draw(region, gx, gy, width, height);
        if (isHurt(tile)) {
            final TextureRegion flashRegion = region;
            final float fw = width;
            final float fh = height;
            flash(batch, () -> batch.draw(flashRegion, gx, gy, fw, fh));
        }
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
        batch.setColor(Color.WHITE);
        if (drawAnimatedMowers(batch)) {
            return;
        }
        TextureRegion region = art.mower();
        if (region == null) {
            return;
        }
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

    private boolean drawAnimatedMowers(Batch batch) {
        if (!animator.isReady()) {
            return false;
        }
        String animation = views.assets.AnimationCatalog.mower(chapterName);
        String clipName = animator.clipName(animation, "idle");
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            return false;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 0.7f);
        float lift = animator.centreOffset(animation, clipName, scale);
        for (int row = 1; row <= GameSession.ROWS; row++) {
            if (!session.hasLawnMower(row)) {
                continue;
            }
            animator.draw(batch, clip, time + row * 0.21f, Lawn.left() - Lawn.cellWidth() * 0.45f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 0.46f + lift, scale, true, null);
        }
        return true;
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
            drawCorpses(batch, row);
            drawBlasts(batch, row);
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
            final models.game.PlacedPlant target = plant;
            final float px = centerX;
            final float py = feet;
            if (!drawPlantAnimation(batch, plant, centerX, feet)) {
                batch.draw(art.plant(plant.getType()), centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.1f, width, height);
            }
            if (isHurt(target)) {
                flash(batch, () -> drawPlantAnimation(batch, target, px, py));
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
            if (!drawIceBlock(batch, ICE_PLANT, x + width / 2f, y, plant.getIceHealth() > 0
                    ? 1f : 0.3f + 0.2f * Math.min(3, plant.getFreezeLevel()))) {
                float alpha = 0.2f + 0.2f * Math.min(3, Math.max(1, plant.getFreezeLevel()));
                batch.setColor(0.6f, 0.85f, 1f, plant.getIceHealth() > 0 ? 0.75f : alpha);
                fill.draw(batch, x, y, width, height);
            }
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
            float[] flight = flights.get(zombie);
            float column = (float) zombie.getPosition().getX();
            float arc = 0f;
            if (flight != null) {
                float progress = Math.min(1f, (time - flight[1]) / IMP_FLIGHT);
                column = flight[0] + (column - flight[0]) * progress;
                arc = IMP_ARC * 4f * progress * (1f - progress) * Lawn.cellHeight();
            }
            float centerX = Lawn.columnCenter(column);
            float feet = Lawn.rowBottom(row) + Lawn.cellHeight() * 0.12f + arc;
            float width = Lawn.cellWidth() * 0.7f;
            float height = Lawn.cellHeight() * 0.92f;
            batch.setColor(zombieTint(zombie));
            final models.entities.zombie.Zombie target = zombie;
            final int zseed = seed;
            final float zx = centerX;
            final float zy = feet;
            if (!drawZombieAnimation(batch, zombie, seed, centerX, feet)) {
                batch.draw(art.zombie(zombie.getType()), centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.06f, width, height);
                drawArmor(batch, zombie, centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.06f, width, height);
            }
            if (isHurt(target)) {
                flash(batch, () -> drawZombieAnimation(batch, target, zseed, zx, zy));
            }
            if (zombie.getFrozenTicks() > 0 || zombie.getBattle().getIceHealth() > 0) {
                drawIceBlock(batch, ICE_ZOMBIE, centerX, Lawn.rowBottom(row), 0.9f);
            }
            int max = Math.max(1, zombie.getType().getHitpoints());
            drawHealthBar(batch, zombie.getHealth(), max, centerX - width / 2f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 1.02f, width);
        }
    }

    private void drawCorpses(Batch batch, int row) {
        if (!animator.isReady()) {
            return;
        }
        for (Corpse corpse : corpses) {
            if (corpse.row != row) {
                continue;
            }
            ClipRef clip = animator.zombieClip(corpse.type, corpse.parts ? "particles" : "die");
            if (clip == null) {
                continue;
            }
            batch.setColor(Color.WHITE);
            animator.draw(batch, clip, time - corpse.start, Lawn.columnCenter(corpse.x),
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 0.12f + animator.zombieLift(),
                    animator.zombieScale(), false, null);
        }
    }

    private void drawBlasts(Batch batch, int row) {
        if (!animator.isReady()) {
            return;
        }
        ClipRef clip = animator.namedClip(BOOM, "explosion");
        if (clip == null) {
            return;
        }
        for (Corpse blast : blasts) {
            if (blast.row != row) {
                continue;
            }
            float scale = animator.fitScale(BOOM, "explosion", Lawn.cellHeight() * 2.4f);
            batch.setColor(Color.WHITE);
            animator.draw(batch, clip, time - blast.start, Lawn.columnCenter(blast.x),
                    Lawn.rowCenter(row), scale, false, null);
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
        String animation = views.assets.AnimationCatalog.sun();
        String clipName = animator.isReady() ? animator.clipName(animation, "animation", "idle") : null;
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        TextureRegion region = art.uiOptional("image_ui_hud_ingame_sun");
        for (models.game.Sun sun : session.getSunManager().getSuns()) {
            float size = sunSize(sun);
            float targetY = Lawn.rowCenter(sun.getY());
            float y = targetY;
            if (sun.isFalling()) {
                float startY = Lawn.bottom() + Lawn.height() + 60f;
                y = startY + (targetY - startY) * sun.getFallProgress();
            }
            float x = Lawn.columnCenter(sun.getX());
            batch.setColor(sunColor(sun));
            if (clip != null) {
                float scale = animator.fitScale(animation, clipName, size);
                float lift = animator.centreOffset(animation, clipName, scale);
                animator.draw(batch, clip, time + sun.getX() * 0.31f, x, y + lift, scale, true, null);
            } else if (region != null) {
                batch.draw(region, x - size / 2f, y - size / 2f, size, size);
            }
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
        boolean fed = plant.getPlantFoodTicks() > 0;
        boolean attacking = fed || (until != null && time < until);
        float clock;
        if (fed) {
            clock = (30 - plant.getPlantFoodTicks()) / 10f;
        } else if (attacking) {
            clock = time - (until - animator.plantClipDuration(plant.getType(), wanted[0]));
        } else {
            clock = time + plant.getX() * 0.37f + plant.getY() * 0.19f;
        }
        if (fed) {
            drawPlantFoodGlow(batch, centerX, feet);
        }
        animator.draw(batch, clip, clock, centerX, feet + animator.plantLift(),
                animator.plantScale(), !attacking, null);
        return true;
    }

    private void drawPlantFoodGlow(Batch batch, float centerX, float feet) {
        ClipRef glow = animator.namedClip(PLANT_FOOD_GLOW, "plantfood", "plantfood_on");
        if (glow == null) {
            return;
        }
        String clipName = animator.clipName(PLANT_FOOD_GLOW, "plantfood", "plantfood_on");
        float scale = animator.fitScale(PLANT_FOOD_GLOW, clipName, Lawn.cellHeight() * 1.5f);
        float lift = animator.centreOffset(PLANT_FOOD_GLOW, clipName, scale);
        batch.setColor(Color.WHITE);
        animator.draw(batch, glow, time, centerX,
                feet + Lawn.cellHeight() * 0.35f + lift, scale, true, null);
    }

    private void trackSunProduction(models.game.PlacedPlant plant) {
        Boolean previous = lastSunPending.get(plant);
        boolean pending = plant.isSunPending();
        lastSunPending.put(plant, pending);
        if (previous != null && !previous && pending) {
            String clip = animator.plantClipName(plant.getType(), "special");
            if ("special".equals(clip)) {
                float duration = animator.plantClipDuration(plant.getType(), clip);
                if (duration > 0f) {
                    firing.put(plant, time + duration);
                }
            }
        }
    }

    private String[] plantClipNames(models.game.PlacedPlant plant) {
        if (plant.getPlantFoodTicks() > 0) {
            return new String[] {"plantfood", "plantfood_on", "special", "attack", "idle"};
        }
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
        Float busy = ability.get(zombie);
        ClipRef clip;
        if (busy != null && time < busy) {
            clip = animator.zombieClip(zombie.getType(),
                    views.assets.AnimationCatalog.abilityClip(zombie.getType()), "walk", "idle");
        } else if (flights.containsKey(zombie)) {
            clip = animator.zombieClip(zombie.getType(), "fly", "walk", "idle");
        } else if (eating) {
            String bite = views.assets.AnimationCatalog.biteClip(zombie.getType());
            clip = bite != null && animator.hasZombieClip(zombie.getType(), bite)
                    ? animator.zombieClip(zombie.getType(), bite, "eat", "idle")
                    : animator.zombieClip(zombie.getType(), "eat", "attack", "walk", "idle");
        } else {
            clip = animator.zombieClip(zombie.getType(), "walk", "walk1", "idle");
        }
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

    private boolean drawIceBlock(Batch batch, String animation, float centreX, float bottom,
                                 float alpha) {
        if (!animator.isReady()) {
            return false;
        }
        String clipName = animator.clipName(animation, "freeze_idle", "idle");
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            return false;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 0.95f);
        float lift = animator.centreOffset(animation, clipName, scale);
        batch.setColor(1f, 1f, 1f, alpha);
        animator.draw(batch, clip, time, centreX, bottom + Lawn.cellHeight() * 0.42f + lift,
                scale, true, null);
        batch.setColor(Color.WHITE);
        return true;
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
