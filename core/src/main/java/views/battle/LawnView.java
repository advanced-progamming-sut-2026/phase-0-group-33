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
    private static final Color GUARD = new Color(1f, 0.3f, 0.28f, 1f);
    private static final float GUARD_EDGE = 3f;
    private static final Color INVALID = new Color(1f, 0.35f, 0.3f, 0.32f);

    protected final GameSession session;
    protected final Art art;
    protected final Drawable fill;

    private static final String PLANT_FOOD_GLOW = "PLANTFOOD_FX";
    private static final String GRAPE_SHARD = "GRAPESHOT_PROJECTILE";
    private static final String ICE_PLANT = "FROSTBITE_ICE_BLOCK_PLANT";
    private static final String ICE_ZOMBIE = "FROSTBITE_ICE_BLOCK_ZOMBIE";
    private static final String TIDE_LINE = "WATER_TIDE_LINE";
    private static final String BIG_WAVE = "WAVE_BIG";
    private static final String MINT_FX = "MINT_FX";
    private static final float MINT_LIFE = 1.6f;
    private static final float TIDE_WAVE_LIFE = 1.6f;
    private static final String WATER_TILE = "WATER_SQUARE";
    private static final float EFFECT_SHIFT_X = 10f;
    private static final float ATTACK_RELEASE = 0.45f;
    private static final Color[] FROST = {
        new Color(0.72f, 0.9f, 1f, 0.26f),
        new Color(0.6f, 0.86f, 1f, 0.44f),
        new Color(0.5f, 0.8f, 1f, 0.62f)};
    private static final float FOOD_GLOW_SHIFT = 20f;
    private static final float HURT_FLASH = 0.14f;
    private static final float IMP_FLIGHT = 0.85f;
    private static final float IMP_ARC = 1.9f;
    private static final float STORM_LIFE = 1.4f;
    private static final float STORM_RIDE = 1.1f;
    private static final float DODO_HOP = 0.55f;
    private static final float MOWER_START = 0.35f;
    private static final String FIRE_TILE = "FIRETILE";
    private static final String BOOM = "CHERRYBOMB_EXPLOSION_TOP";
    private static final float BLAST_LIFE = 3.5f;
    private static final String POOF = "PLANT_POOF";
    private static final String DIRT = "DIRT_SPAWN_GRASS";
    private static final String ASH = "ZOMBIE_BIGHEAD_ASH";
    private static final String ASH_GARGANTUAR = "ZOMBIE_BIGHEAD_GARGANTUAR_ASH";
    private static final String ASH_IMP = "ZOMBIE_BIGHEAD_IMP_ASH";
    private static final float ASH_LIFE = 1.3f;
    private static final float POP_TIME = 0.42f;
    private static final String ARMOUR_BREAK = "ARMOR_BREAK_EFFECT";
    private static final float SLIDE_TIME = 0.22f;
    private static final float DROP_TIME = 0.36f;
    private static final float BOSS_SLIDE = 0.45f;
    private static final int CHARGE_TICKS = 6;
    private static final String RIPPLE = "WATER_ZOMBIE_RIPPLE";
    private static final String RIPPLE_BIG = "WATER_GARGANTUAR_RIPPLE";
    private static final String RIPPLE_SMALL = "WATER_IMP_RIPPLE";
    private static final String SPLASH = "WATER_SPLASH";
    private static final float SURFACE_TIME = 0.55f;

    private final String chapterName;
    private boolean showGrid;
    private int hoverColumn = -1;
    private int hoverRow = -1;
    private models.entities.zombie.ZombieType zombieGhost;
    private int tideColumn = -1;
    private float tideWave;
    private int selectColumn = -1;
    private int selectRow = -1;
    private float time;
    private boolean frozen;
    private boolean hoverValid = true;
    private models.entities.plant.PlantType ghost;
    private final EntityAnimator animator;
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Float> firing =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Integer> attacks =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final float[] burning = new float[GameSession.ROWS + 1];
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Float> foodStart =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Boolean> biting =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Integer> lastFood =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, String> firingClip =
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
    private boolean seeded;
    private final boolean[] mowerSeen = new boolean[GameSession.ROWS + 1];
    private final float[] mowerRun = new float[GameSession.ROWS + 1];
    private final com.badlogic.gdx.utils.Array<float[]> storms = new com.badlogic.gdx.utils.Array<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, Integer> lastFreeze =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, float[]> rides =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, float[]> flights =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Float> hopping =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Float> breaking =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.Array<Corpse> corpses = new com.badlogic.gdx.utils.Array<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, float[]> seen =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.Array<Fx> effects = new com.badlogic.gdx.utils.Array<>();
    private final com.badlogic.gdx.utils.ObjectMap<Object, Float> births =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.Projectile, float[]> shots =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Integer> lastArmour =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.game.PlacedPlant, float[]> slides =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<String, Float> bossMoveStart =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<Long, Boolean> graveSeen =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Boolean> diving =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private final com.badlogic.gdx.utils.ObjectMap<models.entities.zombie.Zombie, Float> surfaced =
            new com.badlogic.gdx.utils.ObjectMap<>();
    private float bossLane = -1f;
    private float bossFrom = -1f;
    private float bossAt = -1f;

    private static final class Fx {
        private final String animation;
        private final String clip;
        private final float x;
        private final int row;
        private final float start;
        private final float end;
        private final float height;
        private final float lift;

        private Fx(String animation, String clip, float x, int row,
                   float start, float end, float height, float lift) {
            this.animation = animation;
            this.clip = clip;
            this.x = x;
            this.row = row;
            this.start = start;
            this.end = end;
            this.height = height;
            this.lift = lift;
        }
    }

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

    public void setZombieGhost(models.entities.zombie.ZombieType ghost) {
        this.zombieGhost = ghost;
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
        trackFeeding();
        trackFiring();
        trackAbilities();
        trackDamage();
        trackBlasts();
        trackStorms();
        trackDeaths();
        trackMowers();
        expireBreakClips();
        trackDiving();
        trackBirths();
        trackShots();
        trackArmour();
        trackBoss();
        trackGraves();
        for (int i = effects.size - 1; i >= 0; i--) {
            if (time >= effects.get(i).end) {
                effects.removeIndex(i);
            }
        }
    }

    private void addFx(String animation, String clip, float x, int row,
                       float height, float lift, float life) {
        if (!animator.isReady() || animator.namedClip(animation, clip) == null) {
            return;
        }
        float duration = animator.namedClipDuration(animation, clip);
        if (duration <= 0f) {
            return;
        }
        effects.add(new Fx(animation, clip, x, row, time,
                time + Math.min(duration, life), height, lift));
    }

    private void noteBreakClip(models.entities.zombie.Zombie zombie) {
        String clip = views.assets.AnimationCatalog.breakClip(zombie.getType());
        if (clip == null || !animator.hasZombieClip(zombie.getType(), clip)) {
            return;
        }
        float duration = animator.zombieClipDuration(zombie.getType(), clip);
        if (duration > 0f) {
            breaking.put(zombie, time + duration);
            cue(views.assets.Audio.ARMOUR_BREAKS);
        }
    }

    private void expireBreakClips() {
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> settled =
                new com.badlogic.gdx.utils.Array<>();
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, Float> entry
                : breaking.entries()) {
            if (time >= entry.value || !session.getZombies().contains(entry.key)) {
                settled.add(entry.key);
            }
        }
        for (models.entities.zombie.Zombie zombie : settled) {
            breaking.remove(zombie);
        }
    }

    private void trackDiving() {
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            boolean under = session.getBehaviorManager().isSubmerged(zombie);
            Boolean previous = diving.get(zombie);
            diving.put(zombie, under);
            if (previous == null || previous == under) {
                continue;
            }
            surfaced.put(zombie, time);
            addFx(SPLASH, "water_splash_01", (float) zombie.getPosition().getX(),
                    (int) zombie.getPosition().getY(), 1.1f, 0.1f, SURFACE_TIME);
        }
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> stale =
                new com.badlogic.gdx.utils.Array<>();
        for (models.entities.zombie.Zombie zombie : diving.keys()) {
            if (!session.getZombies().contains(zombie)) {
                stale.add(zombie);
            }
        }
        for (models.entities.zombie.Zombie zombie : stale) {
            diving.remove(zombie);
            surfaced.remove(zombie);
        }
    }

    private boolean isDiving(models.entities.zombie.Zombie zombie) {
        Boolean under = diving.get(zombie);
        if (under == null || !under) {
            return false;
        }
        Float changed = surfaced.get(zombie);
        return changed == null || time - changed >= SURFACE_TIME * 0.5f;
    }

    private boolean drawRipple(Batch batch, models.entities.zombie.Zombie zombie,
                               float centerX, float row) {
        if (!animator.isReady()) {
            return false;
        }
        String animation = zombie.getType() == models.entities.zombie.ZombieType.GARGANTUAR
                ? RIPPLE_BIG
                : zombie.getType() == models.entities.zombie.ZombieType.IMP
                        ? RIPPLE_SMALL : RIPPLE;
        Float changed = surfaced.get(zombie);
        boolean leaving = changed != null && time - changed < SURFACE_TIME;
        String clipName = animator.clipName(animation,
                leaving ? "ripple_exit" : "ripple", "ripple");
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            return false;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 0.85f);
        float lift = animator.centreOffset(animation, clipName, scale);
        batch.setColor(Color.WHITE);
        animator.draw(batch, clip, leaving ? time - changed : time, centerX,
                Lawn.rowBottom((int) row) + Lawn.cellHeight() * 0.4f + lift, scale, true, null);
        return true;
    }

    private void drawStormLayer(Batch batch, String animation, float column, int row,
                                float age, float height) {
        String clipName = animator.clipName(animation, "loop", "intro", "animation", "idle");
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            return;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * height);
        float lift = animator.centreOffset(animation, clipName, scale);
        animator.draw(batch, clip, age, Lawn.columnCenter(column),
                Lawn.rowCenter(row) + lift, scale, true, null);
    }

    private void drawGust(Batch batch, String animation, int row, float age) {
        String clipName = animator.clipName(animation, "animation", "loop", "idle");
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            drawStormLayer(batch, views.assets.AnimationCatalog.stormTop(true),
                    5f, row, age, 1.4f);
            return;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 1.3f);
        float lift = animator.centreOffset(animation, clipName, scale);
        for (int column = 2; column <= GameSession.COLS; column += 3) {
            animator.draw(batch, clip, age + column * 0.09f, Lawn.columnCenter(column),
                    Lawn.rowCenter(row) + lift, scale, true, null);
        }
    }

    private void trackBirths() {
        boolean settled = seeded;
        for (models.game.PlacedPlant plant : session.getPlants()) {
            if (births.containsKey(plant)) {
                continue;
            }
            births.put(plant, time);
            if (settled) {
                cue(views.assets.Audio.PLANT);
            }
            if (matching()) {
                slides.put(plant, new float[] {plant.getX(), -0.6f, time});
                plantSpots.put(plant, new int[] {plant.getX(), plant.getY()});
            } else if (settled) {
                addFx(POOF, "animation", plant.getX(), plant.getY(), 1.15f, 0.3f, 1f);
            }
        }
        boolean planted = session.getMode() == models.game.GameMode.I_ZOMBIE
                || session.getMode() == models.game.GameMode.VASEBREAKER;
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            if (births.containsKey(zombie)) {
                continue;
            }
            births.put(zombie, time);
            if (settled && planted) {
                addFx(DIRT, "tomb_dirt_anim", (float) zombie.getPosition().getX(),
                        (int) zombie.getPosition().getY(), 1f, 0.1f, 1f);
            }
        }
        com.badlogic.gdx.utils.Array<Object> stale = new com.badlogic.gdx.utils.Array<>();
        for (Object key : births.keys()) {
            boolean alive = key instanceof models.game.PlacedPlant
                    ? session.getPlants().contains(key)
                    : session.getZombies().contains(key);
            if (!alive) {
                stale.add(key);
            }
        }
        for (Object key : stale) {
            births.remove(key);
        }
        seeded = true;
    }

    private float popScale(Object key) {
        Float born = births.get(key);
        if (born == null) {
            return 1f;
        }
        float age = time - born;
        if (age < 0f || age >= POP_TIME) {
            return 1f;
        }
        float progress = age / POP_TIME;
        return 1f + 0.26f * (1f - progress)
                * (float) Math.sin(progress * Math.PI * 3f);
    }

    private void trackBoss() {
        models.entities.zombie.Zomboss boss = session.getZombossManager().getBoss();
        if (boss == null) {
            bossLane = -1f;
            return;
        }
        float lane = (float) boss.getPosition().getY();
        if (bossLane < 0f) {
            bossLane = lane;
            return;
        }
        if (lane != bossLane) {
            bossFrom = bossLane;
            bossAt = time;
            bossLane = lane;
        }
    }

    private void trackArmour() {
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            int armour = zombie.totalArmor();
            Integer previous = lastArmour.get(zombie);
            lastArmour.put(zombie, armour);
            if (previous != null && previous > 0 && armour <= 0) {
                addFx(ARMOUR_BREAK, "animation", (float) zombie.getPosition().getX(),
                        (int) zombie.getPosition().getY(), 1.2f, 0.7f, 1.25f);
                noteBreakClip(zombie);
            }
        }
        for (models.entities.zombie.Zombie zombie : zombieKeys(lastArmour)) {
            if (!session.getZombies().contains(zombie)) {
                lastArmour.remove(zombie);
            }
        }
    }

    private void trackShots() {
        com.badlogic.gdx.utils.Array<models.game.Projectile> gone =
                new com.badlogic.gdx.utils.Array<>();
        for (models.game.Projectile shot : shots.keys()) {
            if (!session.getProjectileManager().getProjectiles().contains(shot)) {
                gone.add(shot);
            }
        }
        for (models.game.Projectile shot : gone) {
            float[] last = shots.remove(shot);
            if (last[0] >= GameSession.COLS + 0.4f || last[0] <= 0.6f
                    || last[1] < 1f || last[1] > GameSession.ROWS) {
                continue;
            }
            cue(views.assets.Audio.SPLAT);
            String[] impact = views.assets.AnimationCatalog.impact(shot.isLit()
                    ? models.entities.plant.PlantType.FIRE_PEASHOOTER : shot.getSource());
            if (impact != null) {
                addFx(impact[0], impact[1], last[0], (int) last[1], 0.9f, 0.42f, 1.1f);
            } else {
                addFx(splatFor(shot.getSource()), "animation", last[0], (int) last[1],
                        0.9f, 0.42f, 0.9f);
            }
        }
        for (models.game.Projectile shot : session.getProjectileManager().getProjectiles()) {
            float[] seenBefore = shots.get(shot);
            if (seenBefore == null) {
                cue(shot.getMotion() == models.game.Projectile.Motion.LOB
                        ? views.assets.Audio.LOB : views.assets.Audio.SHOOT);
            }
            if (shot.isLit() && (seenBefore == null || seenBefore[2] < 0.5f)) {
                noteTorchwoodBurn((float) shot.getX(), shot.getRow());
            }
            shots.put(shot, new float[] {(float) shot.getX(), (float) shot.getLane(),
                shot.isLit() ? 1f : 0f});
        }
    }

    private String splatFor(models.entities.plant.PlantType source) {
        java.util.Set<models.entities.plant.PlantTag> tags = source.getTags();
        if (tags.contains(models.entities.plant.PlantTag.ICE)) {
            return "ZOMBIE_HUNTER_SNOWBALL_SPLAT";
        }
        if (tags.contains(models.entities.plant.PlantTag.FIRE)) {
            return "SPLAT_FIRE_PEA_BLUE";
        }
        if (tags.contains(models.entities.plant.PlantTag.AOE)) {
            return "SPLAT_CABBAGEPULT";
        }
        return "SPLAT_GIANTPEA";
    }

    private String ashFor(models.entities.zombie.ZombieType type) {
        if (type == models.entities.zombie.ZombieType.GARGANTUAR) {
            return ASH_GARGANTUAR;
        }
        if (type == models.entities.zombie.ZombieType.IMP
                || type == models.entities.zombie.ZombieType.IMP_DRAGON) {
            return ASH_IMP;
        }
        return ASH;
    }

    private boolean blastNear(float x, int row) {
        if (row >= 1 && row <= GameSession.ROWS && time < burning[row]) {
            return true;
        }
        for (Corpse blast : blasts) {
            if (Math.abs(blast.x - x) <= 1.6f && Math.abs(blast.row - row) <= 1) {
                return true;
            }
        }
        return false;
    }

    private void drawEffects(Batch batch, int row) {
        if (!animator.isReady()) {
            return;
        }
        for (Fx fx : effects) {
            if (fx.row != row) {
                continue;
            }
            ClipRef clip = animator.namedClip(fx.animation, fx.clip);
            if (clip == null) {
                continue;
            }
            float scale = animator.fitScale(fx.animation, fx.clip,
                    Lawn.cellHeight() * fx.height);
            float lift = animator.centreOffset(fx.animation, fx.clip, scale);
            batch.setColor(Color.WHITE);
            animator.draw(batch, clip, time - fx.start, Lawn.columnCenter(fx.x),
                    Lawn.rowBottom(row) + Lawn.cellHeight() * fx.lift + lift, scale, false, null);
        }
    }

    private void trackMowers() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            boolean present = session.hasLawnMower(row);
            if (mowerSeen[row] && !present) {
                mowerRun[row] = Math.max(time, 0.0001f);
                shakePending = true;
                cue(views.assets.Audio.MOWER);
            }
            mowerSeen[row] = present;
        }
    }

    private boolean blasted(models.entities.zombie.Zombie zombie) {
        return zombie.getType() == models.entities.zombie.ZombieType.PROSPECTOR
                && zombie.getBattle().isReversed();
    }

    private boolean matching() {
        return session.getMode() == models.game.GameMode.BEGHOULED;
    }

    private void trackMints() {
        for (Object[] burst : session.drainMints()) {
            cue(views.assets.Audio.MINT);
            models.entities.plant.PlantType type = (models.entities.plant.PlantType) burst[0];
            int column = (Integer) burst[1];
            int row = (Integer) burst[2];
            String animation = views.assets.AnimationCatalog.plant(type);
            addFx(animation, "intro", column, row, 2f, 0.45f, MINT_LIFE);
            addFx(MINT_FX, "intro", column, row, 2.4f, 0.5f, MINT_LIFE);
            shakePending = true;
            @SuppressWarnings("unchecked")
            java.util.List<int[]> touched = (java.util.List<int[]>) burst[3];
            for (int[] spot : touched) {
                addFx(MINT_FX, "loop", spot[0], spot[1], 1.2f, 0.35f, 0.9f);
            }
        }
    }

    private void trackEmergences() {
        trackMints();
        for (double[] spot : session.drainEmergences()) {
            cue(views.assets.Audio.RISE);
            int row = (int) spot[1];
            if (spot[2] > 0.5) {
                addFx(SPLASH, "water_splash_01", (float) spot[0], row, 1.3f, 0.1f, 0.9f);
            } else {
                addFx(views.assets.AnimationCatalog.graveDirt(), "gravebuster_dirt_anim",
                        (float) spot[0], row, 1.3f, 0.25f, 1.1f);
            }
            addFx(POOF, "animation", (float) spot[0], row, 1.15f, 0.3f, 0.9f);
        }
    }

    private void trackStorms() {
        trackEmergences();
        for (models.game.WhirlwindRide ride : session.drainWhirlwinds()) {
            flights.put(ride.getZombie(), new float[] {
                (float) ride.getFromX(), time, time + STORM_RIDE,
                ride.isStorm() ? 0.3f : 1.1f});
            if (ride.isStorm()) {
                cue(views.assets.Audio.STORM);
                rides.put(ride.getZombie(), new float[] {
                    (float) ride.getFromX(), (float) ride.getToX(), time, ride.getRow()});
            } else {
                noteBlastRide(ride);
            }
        }
        expireRides();
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, float[]> entry
                : seen.entries()) {
            models.entities.zombie.Zombie zombie = entry.key;
            if (!session.getZombies().contains(zombie)) {
                continue;
            }
            float nowX = (float) zombie.getPosition().getX();
            if (entry.value[0] - nowX > 0.9f && !blasted(zombie)) {
                if (zombie.getType() == models.entities.zombie.ZombieType.DODO) {
                    hopping.put(zombie, time + DODO_HOP);
                } else {
                    storms.add(new float[] {nowX, (int) zombie.getPosition().getY(), time, 0f});
                }
            }
        }
        trackFreezes();
        for (int i = storms.size - 1; i >= 0; i--) {
            if (time - storms.get(i)[2] >= STORM_LIFE) {
                storms.removeIndex(i);
            }
        }
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> landed =
                new com.badlogic.gdx.utils.Array<>();
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, Float> hop
                : hopping.entries()) {
            if (time >= hop.value || !session.getZombies().contains(hop.key)) {
                landed.add(hop.key);
            }
        }
        for (models.entities.zombie.Zombie zombie : landed) {
            hopping.remove(zombie);
        }
    }

    private void trackFreezes() {
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
    }

    private void drawStorms(Batch batch) {
        if (!animator.isReady()) {
            return;
        }
        drawRides(batch);
        for (float[] storm : storms) {
            boolean icy = storm[3] > 0.5f;
            int row = (int) storm[1];
            float age = time - storm[2];
            float fade = Math.min(0.85f, 1.8f * (1f - age / STORM_LIFE));
            batch.setColor(1f, 1f, 1f, Math.max(0f, fade));
            if (icy) {
                drawGust(batch, views.assets.AnimationCatalog.chillWind(), row, age);
            } else {
                drawStormLayer(batch, views.assets.AnimationCatalog.stormRear(false),
                        storm[0], row, age, 1.9f);
                drawStormLayer(batch, views.assets.AnimationCatalog.stormTop(false),
                        storm[0], row, age, 1.7f);
            }
            batch.setColor(Color.WHITE);
        }
    }

    private void noteBlastRide(models.game.WhirlwindRide ride) {
        cue(views.assets.Audio.EXPLODE);
        addFx(views.assets.AnimationCatalog.blastRear(), "explosion",
                (float) ride.getFromX(), ride.getRow(), 1.7f, 0.4f, 1f);
        addFx(BOOM, "explosion", (float) ride.getFromX(), ride.getRow(), 1.4f, 0.35f, 1f);
        shakePending = true;
    }

    private void drawRides(Batch batch) {
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, float[]> entry
                : rides.entries()) {
            float[] ride = entry.value;
            float age = time - ride[2];
            float progress = Math.max(0f, Math.min(1f, age / STORM_RIDE));
            float column = ride[0] + (ride[1] - ride[0]) * progress;
            int row = (int) ride[3];
            float fade = Math.min(1f, 3f * Math.min(progress, 1f - progress) + 0.35f);
            batch.setColor(1f, 1f, 1f, fade);
            drawStormLayer(batch, views.assets.AnimationCatalog.stormRear(false),
                    column, row, age, 1.9f);
            drawStormLayer(batch, views.assets.AnimationCatalog.stormTop(false),
                    column, row, age, 1.7f);
            batch.setColor(Color.WHITE);
        }
    }

    private final com.badlogic.gdx.utils.Array<String> cues =
            new com.badlogic.gdx.utils.Array<>();

    private void cue(String name) {
        if (cues.size < 24 && !cues.contains(name, false)) {
            cues.add(name);
        }
    }

    public com.badlogic.gdx.utils.Array<String> drainCues() {
        com.badlogic.gdx.utils.Array<String> copy = new com.badlogic.gdx.utils.Array<>(cues);
        cues.clear();
        return copy;
    }

    public boolean consumeShake() {
        boolean value = shakePending;
        shakePending = false;
        return value;
    }

    private void trackBlasts() {
        for (models.game.PlacedPlant plant : session.getPlants()) {
            int[] spot = plantSpots.get(plant);
            if (spot != null && (spot[0] != plant.getX() || spot[1] != plant.getY())) {
                slides.put(plant, new float[] {spot[0], spot[1], time});
            }
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
            cue(views.assets.Audio.PLANT_DIES);
            int[] spot = plantSpots.get(plant);
            if (matching() && spot != null) {
                addFx(POOF, "animation", spot[0], spot[1], 1.25f, 0.3f, 0.9f);
            }
            plantSpots.remove(plant);
        }
        drainDetonations();
        for (int i = blasts.size - 1; i >= 0; i--) {
            if (time >= blasts.get(i).end) {
                blasts.removeIndex(i);
            }
        }
    }

    private void noteTorchwoodBurn(float column, int row) {
        addFx(views.assets.AnimationCatalog.torchwoodHit(), "hit_normal",
                column, row, 1f, 0.45f, 0.8f);
    }

    private void trackGraves() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int column = 1; column <= GameSession.COLS; column++) {
                Tile tile = session.getGrid().getTile(column - 1, row - 1);
                if (tile == null) {
                    continue;
                }
                long key = row * 100L + column;
                boolean grave = tile.getTerrain() == TerrainType.GRAVE;
                Boolean before = graveSeen.get(key);
                graveSeen.put(key, grave);
                if (before != null && before && !grave) {
                    cue(views.assets.Audio.GRAVE_BREAKS);
                    addFx(views.assets.AnimationCatalog.graveDirt(), "gravebuster_dirt_anim",
                            column, row, 1.1f, 0.1f, 1.2f);
                }
            }
        }
    }

    private void drainDetonations() {
        java.util.List<models.entities.plant.PlantType> types = new java.util.ArrayList<>();
        java.util.List<int[]> spots = session.drainDetonations(types);
        for (int i = 0; i < spots.size() && i < types.size(); i++) {
            int[] spot = spots.get(i);
            float duration = animator.namedClipDuration(BOOM, "explosion");
            if (duration > 0f) {
                blasts.add(new Corpse(null, spot[0], spot[1], time, time + duration));
            }
            shakePending = true;
            noteDetonation(types.get(i), spot[0], spot[1]);
        }
    }

    private void noteDetonation(models.entities.plant.PlantType type, int column, int row) {
        cue(views.assets.Audio.EXPLODE);
        if (type == models.entities.plant.PlantType.JALAPENO
                && row >= 1 && row <= GameSession.ROWS) {
            burning[row] = time + BLAST_LIFE;
        }
        addFx(views.assets.AnimationCatalog.blastRear(), "explosion", column, row,
                2.2f, 0.5f, BLAST_LIFE);
        String[] own = views.assets.AnimationCatalog.detonation(type);
        if (own != null) {
            addFx(own[0], own[1], column, row, 1.6f, 0.35f, BLAST_LIFE);
        }
        if (type == models.entities.plant.PlantType.JALAPENO) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                addFx(views.assets.AnimationCatalog.laneFire(), "idle", col, row,
                        1.1f, 0.15f, BLAST_LIFE);
            }
        }
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

    private String abilityClipName(models.entities.zombie.ZombieType type, String fallback) {
        String[] wanted = views.assets.AnimationCatalog.abilityClips(type);
        if (wanted == null) {
            return fallback;
        }
        String resolved = animator.zombieClipName(type, wanted);
        return resolved == null ? fallback : resolved;
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
                float duration = animator.zombieClipDuration(zombie.getType(),
                        abilityClipName(zombie.getType(), clip));
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
            if (!current.containsKey(entry.key)) {
                noteDeath(entry.key, entry.value[0], (int) entry.value[1]);
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
        expireCorpses();
    }

    private void noteDeath(models.entities.zombie.Zombie zombie, float x, int row) {
        cue(zombie instanceof models.entities.zombie.Zomboss
                ? views.assets.Audio.BOSS_HURT : views.assets.Audio.ZOMBIE_DIES);
        String clip = animator.zombieClipName(zombie.getType(), "die");
        if (clip == null || !"die".equals(clip)) {
            return;
        }
        float duration = animator.zombieClipDuration(zombie.getType(), clip);
        if (duration <= 0f) {
            return;
        }
        if (blastNear(x, row)) {
            addFx(ashFor(zombie.getType()), "animation", x, row, 1.1f, 0.2f, ASH_LIFE);
            return;
        }
        corpses.add(new Corpse(zombie.getType(), x, row, time, time + duration));
        if (animator.hasZombieClip(zombie.getType(), "particles")) {
            float parts = animator.zombieClipDuration(zombie.getType(), "particles");
            if (parts > 0f) {
                corpses.add(new Corpse(zombie.getType(), x, row, time, time + parts, true));
            }
        }
    }

    private float flightProgress(float[] flight) {
        float span = Math.max(0.0001f, flight[2] - flight[1]);
        return Math.max(0f, Math.min(1f, (time - flight[1]) / span));
    }

    private void expireRides() {
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> done =
                new com.badlogic.gdx.utils.Array<>();
        for (com.badlogic.gdx.utils.ObjectMap.Entry<models.entities.zombie.Zombie, float[]> entry
                : rides.entries()) {
            if (time - entry.value[2] >= STORM_RIDE || !session.getZombies().contains(entry.key)) {
                done.add(entry.key);
            }
        }
        for (models.entities.zombie.Zombie zombie : done) {
            rides.remove(zombie);
        }
    }

    private void expireCorpses() {
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
            (float) thrower.getPosition().getX(), time, time + IMP_FLIGHT, IMP_ARC});
    }

    private com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> zombieFlightKeys() {
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> keys =
                new com.badlogic.gdx.utils.Array<>();
        for (models.entities.zombie.Zombie zombie : flights.keys()) {
            keys.add(zombie);
        }
        return keys;
    }

    private void trackBites() {
        for (models.entities.zombie.Zombie zombie : session.getZombies()) {
            boolean eating = isEating(zombie);
            Boolean before = biting.get(zombie);
            biting.put(zombie, eating);
            if (eating && (before == null || !before)) {
                cue(views.assets.Audio.BITE);
            }
        }
        com.badlogic.gdx.utils.Array<models.entities.zombie.Zombie> stale =
                new com.badlogic.gdx.utils.Array<>();
        for (models.entities.zombie.Zombie zombie : biting.keys()) {
            if (!session.getZombies().contains(zombie)) {
                stale.add(zombie);
            }
        }
        for (models.entities.zombie.Zombie zombie : stale) {
            biting.remove(zombie);
        }
    }

    private void trackFeeding() {
        trackBites();
        for (models.game.PlacedPlant plant : session.getPlants()) {
            int ticks = plant.getPlantFoodTicks();
            Integer previous = lastFood.get(plant);
            lastFood.put(plant, ticks);
            if ((previous == null || previous == 0) && ticks > 0) {
                foodStart.put(plant, time);
                noteFeast(plant);
            }
        }
        com.badlogic.gdx.utils.Array<models.game.PlacedPlant> stale =
                new com.badlogic.gdx.utils.Array<>();
        for (models.game.PlacedPlant plant : lastFood.keys()) {
            if (!session.getPlants().contains(plant)) {
                stale.add(plant);
            }
        }
        for (models.game.PlacedPlant plant : stale) {
            lastFood.remove(plant);
            foodStart.remove(plant);
        }
    }

    private void noteFeast(models.game.PlacedPlant plant) {
        cue(views.assets.Audio.GULP);
        addFx(PLANT_FOOD_GLOW, "plantfood", plant.getX(), plant.getY(), 1.6f, 0.85f, 1.4f);
        shakePending = true;
        if (!feedsWholeLane(plant.getType())) {
            return;
        }
        for (int column = plant.getX(); column <= GameSession.COLS; column++) {
            addFx(views.assets.AnimationCatalog.blastRear(), "explosion", column,
                    plant.getY(), 1.2f, 0.3f, 0.85f);
        }
    }

    private boolean feedsWholeLane(models.entities.plant.PlantType type) {
        models.entities.plant.PlantCategory category = type.getCategory();
        return category == models.entities.plant.PlantCategory.SHOOTER
                || category == models.entities.plant.PlantCategory.LOBBER
                || category == models.entities.plant.PlantCategory.STRIKE_THROUGH
                || category == models.entities.plant.PlantCategory.HOMING;
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
            String clip = chooseAttackClip(plant);
            if (clip == null || "idle".equals(clip)) {
                continue;
            }
            float duration = animator.plantClipDuration(plant.getType(), clip);
            if (duration > 0f) {
                firing.put(plant, time + duration * (1f - ATTACK_RELEASE));
                firingClip.put(plant, clip);
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
            firingClip.remove(plant);
            attacks.remove(plant);
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
        drawDrivingMowers(batch);
        drawGhost(batch);
        drawStorms(batch);
        batch.setColor(previous);
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

    private void drawTideWave(Batch batch, int boundary) {
        if (tideWave <= 0f || time - tideWave > TIDE_WAVE_LIFE) {
            return;
        }
        String clipName = animator.clipName(BIG_WAVE, "wave_crash", "wave");
        ClipRef clip = clipName == null ? null : animator.namedClip(BIG_WAVE, clipName);
        if (clip == null) {
            return;
        }
        float age = time - tideWave;
        float scale = animator.fitScale(BIG_WAVE, clipName, Lawn.height() * 1.15f);
        float lift = animator.centreOffset(BIG_WAVE, clipName, scale);
        float fade = Math.min(1f, 2.4f * Math.min(age, TIDE_WAVE_LIFE - age));
        batch.setColor(1f, 1f, 1f, Math.max(0f, fade));
        animator.draw(batch, clip, age, Lawn.columnLeft(boundary) + Lawn.cellWidth() * 0.2f,
                Lawn.bottom() + Lawn.height() / 2f + lift, scale, false, null);
        batch.setColor(Color.WHITE);
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
            tideColumn = -1;
            return;
        }
        if (tideColumn != firstWater) {
            if (tideColumn > 0) {
                tideWave = time;
                cue(views.assets.Audio.TIDE);
            }
            tideColumn = firstWater;
        }
        drawTideWave(batch, firstWater);
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
                if (tile.getTerrain() != TerrainType.WATER || tile.isLowTide()
                        || !drawWaterTile(batch, column, row)) {
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
        float spin = 0f;
        if (tile.getTerrain() == TerrainType.SLIDER_UP) {
            icon = "image_ui_almanac_stats_screen_nav_arrow_next";
            spin = 90f;
        } else if (tile.getTerrain() == TerrainType.SLIDER_DOWN) {
            icon = "image_ui_almanac_stats_screen_nav_arrow_previous";
            spin = 90f;
        } else if (tile.getTerrain() == TerrainType.FIRE) {
            drawFireTile(batch, column, row);
            return;
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
        float x = Lawn.columnCenter(column) - size / 2f;
        float y = Lawn.rowCenter(row) - size / 2f;
        if (spin == 0f) {
            batch.draw(region, x, y, size, size);
            return;
        }
        batch.draw(region, x, y, size / 2f, size / 2f, size, size, 1f, 1f, spin);
    }

    private void drawFireTile(Batch batch, int column, int row) {
        if (!animator.isReady()) {
            paint(batch, DANGER, column, row);
            return;
        }
        String clipName = animator.clipName(FIRE_TILE, "firetile_up", "idle");
        ClipRef clip = clipName == null ? null : animator.namedClip(FIRE_TILE, clipName);
        if (clip == null) {
            paint(batch, DANGER, column, row);
            return;
        }
        float scale = animator.fitScale(FIRE_TILE, clipName, Lawn.cellHeight());
        float lift = animator.centreOffset(FIRE_TILE, clipName, scale);
        batch.setColor(Color.WHITE);
        animator.draw(batch, clip, time + column * 0.17f, Lawn.columnCenter(column),
                Lawn.rowCenter(row) + lift, scale, true, null);
    }

    private void drawGrave(Batch batch, Tile tile, int column, int row) {
        float fraction = Math.max(0f, Math.min(1f, tile.getGraveHealth() / (float) Tile.getGraveMaxHealth()));
        TextureRegion region = tile.getGraveSunContent() > 0 ? art.graveWithSun(fraction)
                : tile.isGravePlantFood() ? art.graveWithFood(fraction)
                : art.grave(chapterName, fraction);
        if (region == null) {
            region = art.grave(chapterName, fraction);
        }
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
        if (tile.getGraveHealth() < Tile.getGraveMaxHealth()) {
            drawHealthBar(batch, tile.getGraveHealth(), Tile.getGraveMaxHealth(),
                    Lawn.columnCenter(column), gy + height + 3f, Lawn.cellWidth() * 0.6f);
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
            animator.draw(batch, clip, time + row * 0.21f,
                    Lawn.left() - Lawn.cellWidth() * 0.45f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 0.46f + lift, scale, true, null);
        }
        return true;
    }

    private void drawDrivingMowers(Batch batch) {
        if (!animator.isReady()) {
            return;
        }
        String animation = views.assets.AnimationCatalog.mower(chapterName);
        String idleName = animator.clipName(animation, "idle");
        if (idleName == null) {
            return;
        }
        float scale = animator.fitScale(animation, idleName, Lawn.cellHeight() * 0.7f);
        float lift = animator.centreOffset(animation, idleName, scale);
        for (models.game.RunningMower mower : session.getRunningMowers()) {
            int row = mower.getRow();
            float age = mowerRun[row] <= 0f ? MOWER_START : time - mowerRun[row];
            String wanted = age < MOWER_START ? "transition" : "attack";
            String clipName = animator.clipName(animation, wanted, "attack", "idle");
            ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
            if (clip == null) {
                continue;
            }
            batch.setColor(Color.WHITE);
            animator.draw(batch, clip, age, Lawn.columnCenter(mower.getX()),
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 0.46f + lift, scale, true, null);
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
    }

    private void drawGhost(Batch batch) {
        if (zombieGhost != null && hoverColumn >= 1 && hoverRow >= 1) {
            float span = Lawn.cellWidth() * 0.72f;
            float tall = Lawn.cellHeight() * 0.92f;
            batch.setColor(1f, 1f, 1f, hoverValid ? 0.62f : 0.3f);
            batch.draw(art.zombie(zombieGhost), Lawn.columnCenter(hoverColumn) - span / 2f,
                    Lawn.rowBottom(hoverRow) + Lawn.cellHeight() * 0.1f, span, tall);
            batch.setColor(Color.WHITE);
        }
        if (ghost == null || hoverColumn < 1 || hoverRow < 1) {
            return;
        }
        float width = Lawn.cellWidth() * 0.78f;
        float height = Lawn.cellHeight() * 0.8f;
        batch.setColor(1f, 1f, 1f, hoverValid ? 0.62f : 0.3f);
        batch.draw(art.plant(ghost), Lawn.columnCenter(hoverColumn) - width / 2f,
                Lawn.rowBottom(hoverRow) + Lawn.cellHeight() * 0.1f, width, height);
        batch.setColor(Color.WHITE);
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
                markProtected(batch, plant.getX(), plant.getY());
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
            drawEffects(batch, row);
        }
        drawSuns(batch);
        drawPlantFoodDrops(batch);
    }

    private void drawPlantFoodDrops(Batch batch) {
        TextureRegion region = art.uiOptional("IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_FILLED_SLOT");
        if (region == null) {
            region = art.uiOptional("IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_PLANTFOOD_LARGE");
        }
        if (region == null) {
            return;
        }
        for (models.game.PlantFoodDrop drop : session.getPlantFoodDrops()) {
            float size = Lawn.cellHeight() * 0.62f;
            float targetY = Lawn.rowCenter(drop.getY()) - size / 2f;
            float y = targetY;
            if (drop.isFalling()) {
                float startY = targetY + Lawn.cellHeight() * 1.4f;
                y = startY + (targetY - startY) * drop.getFallProgress();
            } else {
                y += (float) Math.sin(time * 4f + drop.getX()) * size * 0.08f;
            }
            float alpha = drop.isExpiring()
                    ? 0.45f + 0.55f * Math.abs((float) Math.sin(time * 6f)) : 1f;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(region, Lawn.columnCenter(drop.getX()) - size / 2f, y, size, size);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawProjectiles(Batch batch, int row) {
        TextureRegion region = art.pea();
        for (models.game.Projectile shot : session.getProjectileManager().getProjectiles()) {
            if (shot.getRow() != row) {
                continue;
            }
            float size = Lawn.cellWidth() * flightScale(shot);
            float centreX = Lawn.columnCenter(shot.getX());
            float y = Lawn.rowBottom(shot.getLane()) + Lawn.cellHeight() * 0.45f
                    + (float) shot.getHeight() * Lawn.cellHeight() * 0.5f;
            if (drawShotAnimation(batch, shot, centreX, y + size / 2f, size)) {
                continue;
            }
            if (region == null) {
                continue;
            }
            batch.setColor(projectileTint(shot.isLit()
                    ? models.entities.plant.PlantType.FIRE_PEASHOOTER : shot.getSource()));
            batch.draw(region, centreX - size / 2f, y, size, size);
        }
        batch.setColor(Color.WHITE);
    }

    private String grapeClip(models.game.Projectile shot) {
        if (shot.getLaneStep() < 0) {
            return "animation_verticle_up";
        }
        if (shot.getLaneStep() > 0) {
            return "animation_verticle_down";
        }
        return shot.getDirection() < 0 ? "animation_backward" : "animation_forward";
    }

    private boolean drawShotAnimation(Batch batch, models.game.Projectile shot,
                                      float centreX, float centreY, float size) {
        if (!animator.isReady()) {
            return false;
        }
        boolean scattered = shot.getMotion() == models.game.Projectile.Motion.SCATTER
                && shot.getSource() == models.entities.plant.PlantType.GRAPESHOT;
        String[] art = scattered
                ? new String[] {GRAPE_SHARD, grapeClip(shot)}
                : views.assets.AnimationCatalog.projectile(shot.isLit()
                        ? models.entities.plant.PlantType.FIRE_PEASHOOTER : shot.getSource());
        if (art == null) {
            return false;
        }
        ClipRef clip = animator.namedClip(art[0], art[1]);
        if (clip == null) {
            return false;
        }
        float scale = animator.fitScale(art[0], art[1], size * 1.3f);
        if (scale <= 0f) {
            return false;
        }
        float lift = animator.centreOffset(art[0], art[1], scale);
        float side = animator.sideOffset(art[0], art[1], scale);
        batch.setColor(Color.WHITE);
        float clock = time + (float) shot.getOriginX() * 0.17f;
        if (shot.getDirection() < 0 && !scattered) {
            animator.drawMirrored(batch, clip, clock, centreX - side, centreY + lift,
                    scale, true, null);
        } else {
            animator.draw(batch, clip, clock, centreX + side, centreY + lift,
                    scale, true, null);
        }
        return true;
    }

    private float flightScale(models.game.Projectile shot) {
        if (shot.getMotion() == models.game.Projectile.Motion.SCATTER) {
            return shot.getSource() == models.entities.plant.PlantType.GRAPESHOT ? 0.5f : 0.34f;
        }
        if (shot.getMotion() == models.game.Projectile.Motion.LOB) {
            return 0.66f;
        }
        return projectileScale(shot.getSource());
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
            float column = plant.getX();
            float lane = row;
            float[] slide = slides.get(plant);
            if (slide != null) {
                float span = slide[1] < 0.5f ? DROP_TIME : SLIDE_TIME;
                float progress = (time - slide[2]) / span;
                if (progress >= 1f) {
                    slides.remove(plant);
                } else {
                    float eased = progress * progress * (3f - 2f * progress);
                    column = slide[0] + (column - slide[0]) * eased;
                    lane = slide[1] + (lane - slide[1]) * eased;
                }
            }
            float centerX = Lawn.columnCenter(column);
            float feet = Lawn.rowBottom(lane) + Lawn.cellHeight() * 0.16f;
            float width = Lawn.cellWidth() * 0.78f;
            float height = Lawn.cellHeight() * 0.8f;
            batch.setColor(plantTint(plant));
            final models.game.PlacedPlant target = plant;
            final float px = centerX;
            final float py = feet;
            if (!drawPlantAnimation(batch, plant, centerX, feet)) {
                batch.draw(art.plant(plant.getType()), centerX - width / 2f,
                        Lawn.rowBottom(lane) + Lawn.cellHeight() * 0.1f, width, height);
            }
            if (isHurt(target)) {
                flash(batch, () -> drawPlantAnimation(batch, target, px, py));
            }
            drawPlantOverlays(batch, plant, centerX - width / 2f,
                    Lawn.rowBottom(lane) + Lawn.cellHeight() * 0.1f, width, height);
            drawHealthBar(batch, plant.getHealth(), plant.getMaxHealth(),
                    centerX - width / 2f, Lawn.rowBottom(lane) + Lawn.cellHeight() * 0.94f, width);
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
        drawFreeze(batch, plant, x, y, width, height);
        if (plant.getOctopusHealth() > 0) {
            batch.setColor(Color.WHITE);
            TextureRegion octopus = art.zombie(models.entities.zombie.ZombieType.OCTOPUS);
            if (octopus != null) {
                batch.draw(octopus, x + width * 0.15f, y + height * 0.3f, width * 0.7f, height * 0.7f);
            }
        }
        if (plant.getPumpkinHealth() > 0 && !drawPumpkin(batch, plant)) {
            batch.setColor(1f, 0.65f, 0.2f, 0.45f);
            fill.draw(batch, x, y, width, height);
        }
    }

    private void drawFreeze(Batch batch, models.game.PlacedPlant plant,
                            float x, float y, float width, float height) {
        int stage = plant.getIceHealth() > 0 ? 3 : Math.min(3, plant.getFreezeLevel());
        if (stage <= 0) {
            return;
        }
        batch.setColor(FROST[stage - 1]);
        fill.draw(batch, x, y, width, height);
        if (stage == 1) {
            drawFrostEdge(batch, x, y, width, height, 0.28f);
            batch.setColor(Color.WHITE);
            return;
        }
        if (!drawIceBlock(batch, ICE_PLANT, x + width / 2f, y, stage == 2 ? 0.6f : 1f)) {
            drawFrostEdge(batch, x, y, width, height, stage == 2 ? 0.5f : 0.8f);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawFrostEdge(Batch batch, float x, float y, float width, float height,
                               float alpha) {
        float rim = height * 0.16f;
        batch.setColor(0.82f, 0.95f, 1f, alpha);
        fill.draw(batch, x, y, width, rim);
        fill.draw(batch, x, y + height - rim, width, rim);
        fill.draw(batch, x, y, rim * 0.6f, height);
        fill.draw(batch, x + width - rim * 0.6f, y, rim * 0.6f, height);
    }

    private void drawPushed(Batch batch, int row) {
        for (models.game.PushedObject pushed : session.getPushedObjects()) {
            if (pushed.getRow() != row || pushed.isDestroyed()) {
                continue;
            }
            batch.setColor(Color.WHITE);
            if (drawPushedArt(batch, pushed, row)) {
                continue;
            }
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
            TextureRegion skin = art.plant(nut.getType());
            if (skin == null) {
                continue;
            }
            batch.draw(skin, Lawn.columnCenter(nut.getX()) - size / 2f,
                    Lawn.rowCenter(nut.getLane()) - size / 2f, size / 2f, size / 2f,
                    size, size, 1f, 1f, nut.getSpin());
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
                float progress = flightProgress(flight);
                column = flight[0] + (column - flight[0]) * progress;
                arc = flight[3] * 4f * progress * (1f - progress) * Lawn.cellHeight();
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
            if (isDiving(zombie) && drawRipple(batch, zombie, centerX, row)) {
                continue;
            }
            if (!drawZombieAnimation(batch, zombie, seed, centerX, feet)) {
                batch.draw(art.zombie(zombie.getType()), centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.06f, width, height);
                drawArmor(batch, zombie, centerX - width / 2f,
                        Lawn.rowBottom(row) + Lawn.cellHeight() * 0.06f, width, height);
            }
            if (isHurt(target)) {
                flash(batch, () -> drawZombieAnimation(batch, target, zseed, zx, zy));
            }
            drawZombotanyHead(batch, zombie, centerX, feet);
            if (zombie.getFrozenTicks() > 0 || zombie.getBattle().getIceHealth() > 0) {
                drawIceBlock(batch, ICE_ZOMBIE, centerX, Lawn.rowBottom(row), 0.9f);
            }
            int max = Math.max(1, (int) Math.round(zombie.getType().getHitpoints()
                    * session.getHealthFactor()));
            drawHealthBar(batch, zombie.getHealth(), max, centerX - width / 2f,
                    Lawn.rowBottom(row) + Lawn.cellHeight() * 1.02f, width);
        }
    }

    private boolean drawPushedArt(Batch batch, models.game.PushedObject pushed, int row) {
        if (!animator.isReady()) {
            return false;
        }
        String animation = views.assets.AnimationCatalog.pushed(pushed.getKind());
        String clipName = animator.clipName(animation,
                views.assets.AnimationCatalog.pushedClip(pushed.getKind()), "idle");
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            return false;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 0.95f);
        float anchor = animator.bottomOffset(animation, clipName, scale);
        animator.draw(batch, clip, time, Lawn.columnCenter(pushed.getX()),
                Lawn.rowBottom(row) + Lawn.cellHeight() * 0.1f - anchor, scale, true, null);
        return true;
    }

    private void drawZombotanyHead(Batch batch, models.entities.zombie.Zombie zombie,
                                   float centerX, float feet) {
        models.entities.plant.PlantType head =
                views.assets.AnimationCatalog.zombotanyHead(zombie.getType());
        if (head == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float size = Lawn.cellHeight() * 0.5f;
        float x = centerX - size * 0.04f;
        float y = feet + Lawn.cellHeight() * 0.72f;
        if (animator.isReady()) {
            ClipRef clip = animator.plantClip(head, "idle");
            if (clip != null) {
                float scale = animator.fitScale(
                        views.assets.AnimationCatalog.plant(head),
                        animator.plantClipName(head, "idle"), size);
                animator.draw(batch, clip, time + (float) zombie.getPosition().getX() * 0.3f,
                        x, y, scale, true, null);
                return;
            }
        }
        TextureRegion region = art.plant(head);
        if (region != null) {
            batch.draw(region, x - size / 2f, y, size, size);
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
            animator.draw(batch, clip, time - blast.start,
                    Lawn.columnCenter(blast.x) + EFFECT_SHIFT_X,
                    Lawn.rowCenter(row) + Lawn.cellHeight(), scale, false, null);
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

    public models.game.Sun sunAt(float stageX, float stageY) {
        models.game.Sun best = null;
        float bestDistance = Float.MAX_VALUE;
        for (models.game.Sun sun : session.getSunManager().getSuns()) {
            float size = sunSize(sun);
            float x = Lawn.columnCenter(sun.getX());
            float y = sunDrawY(sun);
            float dx = Math.abs(stageX - x);
            float dy = Math.abs(stageY - y);
            if (dx > size * 0.6f || dy > size * 0.6f) {
                continue;
            }
            float distance = dx + dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = sun;
            }
        }
        return best;
    }

    private float sunDrawY(models.game.Sun sun) {
        float targetY = Lawn.rowCenter(sun.getY());
        if (!sun.isFalling()) {
            return targetY;
        }
        float startY = Lawn.bottom() + Lawn.height() + 60f;
        return startY + (targetY - startY) * sun.getFallProgress();
    }

    private void drawSuns(Batch batch) {
        String animation = views.assets.AnimationCatalog.sun();
        String clipName = animator.isReady() ? animator.clipName(animation, "animation", "idle") : null;
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        TextureRegion region = art.uiOptional("image_ui_hud_ingame_sun");
        for (models.game.Sun sun : session.getSunManager().getSuns()) {
            float size = sunSize(sun);
            float y = sunDrawY(sun);
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
        Feast feast = plant.getPlantFoodTicks() > 0 ? feastFor(plant) : null;
        boolean fed = feast != null && feast.clip != null;
        boolean attacking = until != null && time < until;
        float clock;
        if (fed) {
            clock = feast.clock;
        } else if (attacking) {
            clock = time - (until - animator.plantClipDuration(plant.getType(), wanted[0]));
        } else {
            clock = time + plant.getX() * 0.37f + plant.getY() * 0.19f;
        }
        if (plant.getPlantFoodTicks() > 0) {
            drawPlantFoodGlow(batch, centerX, feet);
        }
        boolean loop = fed ? feast.loop : !attacking;
        animator.draw(batch, clip, clock, centerX, feet + animator.plantLift(),
                animator.plantScale() * popScale(plant), loop, null);
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
        animator.draw(batch, glow, time, centerX + FOOD_GLOW_SHIFT,
                feet + Lawn.cellHeight() * 2.35f + lift, scale, true, null);
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
                    firingClip.put(plant, clip);
                }
            }
        }
    }

    private static final class Feast {
        private String clip;
        private float clock;
        private boolean loop;
    }

    private String pickClip(models.entities.plant.PlantType type, String... names) {
        String found = animator.plantClipName(type, names);
        if (found == null) {
            return null;
        }
        for (String name : names) {
            if (name.equals(found)) {
                return found;
            }
        }
        return null;
    }

    private Feast feastFor(models.game.PlacedPlant plant) {
        models.entities.plant.PlantType type = plant.getType();
        Float began = foodStart.get(plant);
        float elapsed = began == null ? 0f : Math.max(0f, time - began);
        int stage = growthStage(plant);
        Feast feast = new Feast();
        String staged = stage > 0 ? pickClip(type, "plantfood_stage" + stage) : null;
        String intro = pickClip(type, "plantfood_on", "plantfood_start", "pf_start");
        String main = staged != null ? staged
                : pickClip(type, "plantfood", "plantfood_loop", "plantfood_idle", "pf");
        String outro = pickClip(type, "plantfood_off", "plantfood_end", "pf_end");
        float introTime = intro == null ? 0f : animator.plantClipDuration(type, intro);
        float outroTime = outro == null ? 0f : animator.plantClipDuration(type, outro);
        float window = models.game.GameSession.TICKS_PER_SECOND > 0
                ? plant.getPlantFoodTicks() / (float) models.game.GameSession.TICKS_PER_SECOND
                        + elapsed : elapsed;
        if (intro != null && elapsed < introTime) {
            feast.clip = intro;
            feast.clock = elapsed;
            return feast;
        }
        if (outro != null && elapsed >= window - outroTime) {
            feast.clip = outro;
            feast.clock = elapsed - (window - outroTime);
            return feast;
        }
        if (main != null) {
            float mainTime = animator.plantClipDuration(type, main);
            feast.clip = main;
            feast.clock = elapsed - introTime;
            feast.loop = outro != null || mainTime <= 0f || elapsed - introTime < mainTime;
            if (outro == null && mainTime > 0f && elapsed - introTime >= mainTime) {
                feast.clip = null;
            }
            return feast;
        }
        feast.clip = intro != null ? intro : outro;
        feast.clock = elapsed;
        return feast;
    }

    private String[] plantClipNames(models.game.PlacedPlant plant) {
        if (plant.getType().getName().toLowerCase().contains("mint")) {
            return new String[] {"loop", "idle"};
        }
        int stage = growthStage(plant);
        if (plant.getPlantFoodTicks() > 0) {
            String feasting = feastFor(plant).clip;
            if (feasting != null) {
                return new String[] {feasting, "idle"};
            }
        }
        Float until = firing.get(plant);
        if (until != null && time < until) {
            if (stage > 0) {
                return new String[] {"attack_stage" + stage, "attack " + stage,
                    "attack" + stage, "special_stage" + stage, "special " + stage,
                    "attack", "special", "idle_stage" + stage, "idle" + stage, "idle"};
            }
            String locked = firingClip.get(plant);
            return locked == null
                    ? new String[] {"attack", "special", "idle"}
                    : new String[] {locked, "attack", "special", "idle"};
        }
        if (until != null && time < until + recoveryWindow(plant)) {
            return new String[] {
                "recovery", "reload", "bite_end", "attack_end", "recover", "idle"};
        }
        if (plant.getActionCooldownTicks() > 0
                && plant.getActionCooldownTicks() <= CHARGE_TICKS) {
            return new String[] {
                "charge", "busy", "attack_start", "special_idle", "attack_emerge", "idle"};
        }
        float fraction = plant.getHealth() / (float) Math.max(1, plant.getMaxHealth());
        if (fraction <= 0.34f) {
            return new String[] {"damage3", "damage2", "damage", "idle3", "idle2", "idle"};
        }
        if (fraction <= 0.67f) {
            return new String[] {"damage2", "damage", "idle2", "idle"};
        }
        if (plant.getArmTicks() > 0) {
            return new String[] {"plant_idle", "plant", "idle2", "idle"};
        }
        if (stage > 0) {
            return new String[] {"idle_stage" + stage, "idle_stage" + stage + "_",
                "idle " + stage, "idle" + stage, "idle"};
        }
        return new String[] {"idle"};
    }

    private float recoveryWindow(models.game.PlacedPlant plant) {
        String clip = animator.plantClipName(plant.getType(),
                "recovery", "reload", "bite_end", "attack_end", "recover");
        if (clip == null || "idle".equals(clip)) {
            return 0f;
        }
        float duration = animator.plantClipDuration(plant.getType(), clip);
        return duration > 0f ? duration : 0f;
    }

    private int growthStage(models.game.PlacedPlant plant) {
        models.entities.plant.PlantType type = plant.getType();
        if (type.getTags().contains(models.entities.plant.PlantTag.WRAMP_UP)) {
            return Math.max(1, Math.min(3, plant.getGrowthStage()));
        }
        if (type.getTags().contains(models.entities.plant.PlantTag.STACK)
                && type != models.entities.plant.PlantType.PUMPKIN
                && type != models.entities.plant.PlantType.LILY_PAD) {
            return Math.max(1, Math.min(5, plant.getStackCount()));
        }
        return 0;
    }

    private String chooseAttackClip(models.game.PlacedPlant plant) {
        String[] variants = views.assets.AnimationCatalog.attackVariants(plant.getType());
        if (variants != null && variants.length > 0) {
            Integer seen = attacks.get(plant);
            int next = seen == null ? 0 : seen + 1;
            attacks.put(plant, next);
            int index = Math.abs(System.identityHashCode(plant) / 7 + next) % variants.length;
            String picked = animator.plantClipName(plant.getType(), variants[index]);
            if (picked != null && !"idle".equals(picked)) {
                return picked;
            }
        }
        return animator.plantClipName(plant.getType(), "attack", "special");
    }

    private boolean drawZombieAnimation(Batch batch, models.entities.zombie.Zombie zombie,
                                        int seed, float centerX, float feet) {
        if (!animator.isReady()) {
            return false;
        }
        if (zombie instanceof models.entities.zombie.Zomboss) {
            return drawZomboss(batch, (models.entities.zombie.Zomboss) zombie, centerX, feet);
        }
        if (views.assets.AnimationCatalog.mount(zombie.getType()) != null) {
            drawMount(batch, zombie, centerX);
        }
        ClipRef clip = zombieClip(zombie, isEating(zombie));
        if (clip == null) {
            return false;
        }
        float clock = zombie.getFrozenTicks() > 0 ? seed * 0.11f : time + seed * 0.11f;
        float scale = animator.zombieScale() * popScale(zombie);
        java.util.Map<String, Boolean> armour =
                animator.armourFor(zombie.getType(), armourFraction(zombie));
        if (zombie.getBattle().isHypnotized()) {
            animator.drawMirrored(batch, clip, clock, centerX,
                    feet + animator.zombieLift(), scale, true, armour);
        } else {
            animator.draw(batch, clip, clock, centerX,
                    feet + animator.zombieLift(), scale, true, armour);
        }
        return true;
    }

    private ClipRef zombieClip(models.entities.zombie.Zombie zombie, boolean eating) {
        models.entities.zombie.ZombieType type = zombie.getType();
        String[] stated = views.assets.AnimationCatalog.stateClips(type, eating,
                zombie.totalArmor() > 0, zombie.getBattle().isCharging(),
                zombie.getBattle().isSpinning(), hopping.containsKey(zombie));
        Float shattering = breaking.get(zombie);
        if (shattering != null && time < shattering) {
            return animator.zombieClip(type,
                    views.assets.AnimationCatalog.breakClip(type), "walk", "idle");
        }
        Float busy = ability.get(zombie);
        if (busy != null && time < busy) {
            String[] wanted = views.assets.AnimationCatalog.abilityClips(type);
            return wanted == null
                    ? animator.zombieClip(type, "walk", "idle")
                    : animator.zombieClip(type, wanted);
        }
        if (flights.containsKey(zombie) && !rides.containsKey(zombie)) {
            float[] flight = flights.get(zombie);
            return time > flight[2] - 0.25f
                    ? animator.zombieClip(type,
                            views.assets.AnimationCatalog.landClip(type), "fly", "walk", "idle")
                    : animator.zombieClip(type, "fly", "walk", "idle");
        }
        if (eating && stated != null) {
            return animator.zombieClip(type, stated);
        }
        if (eating) {
            String[] bites = views.assets.AnimationCatalog.biteClips(type,
                    (System.identityHashCode(zombie) & 1) == 0);
            return bites == null
                    ? animator.zombieClip(type, "eat", "attack", "walk", "idle")
                    : animator.zombieClip(type, bites);
        }
        String ride = views.assets.AnimationCatalog.rideClip(type);
        if (ride != null && animator.hasZombieClip(type, ride)) {
            return animator.zombieClip(type, ride, "walk", "idle");
        }
        if (isPushing(zombie)) {
            return animator.zombieClip(type, "push", "walk", "idle");
        }
        return stated == null
                ? animator.zombieClip(type, "walk", "walk1", "idle")
                : animator.zombieClip(type, stated);
    }

    private void drawMount(Batch batch, models.entities.zombie.Zombie zombie, float centerX) {
        String animation = views.assets.AnimationCatalog.mount(zombie.getType());
        if (animation == null) {
            return;
        }
        String clipName = animator.clipName(animation,
                views.assets.AnimationCatalog.mountClips(zombie.getType()));
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            return;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 0.95f);
        if (scale <= 0f) {
            return;
        }
        float anchor = animator.bottomOffset(animation, clipName, scale);
        Color previous = batch.getColor().cpy();
        batch.setColor(Color.WHITE);
        animator.draw(batch, clip, time, centerX,
                Lawn.rowBottom((int) zombie.getPosition().getY())
                        + Lawn.cellHeight() * 0.12f - anchor, scale, true, null);
        batch.setColor(previous);
    }

    private boolean isPushing(models.entities.zombie.Zombie zombie) {
        models.entities.zombie.ZombieType type = zombie.getType();
        if (type != models.entities.zombie.ZombieType.TROGLOBITE
                && type != models.entities.zombie.ZombieType.ARCAD
                && type != models.entities.zombie.ZombieType.BARREL_ROLLER) {
            return false;
        }
        for (models.game.PushedObject pushed : session.getPushedObjects()) {
            if (pushed.getRow() == (int) zombie.getPosition().getY() && !pushed.isDestroyed()
                    && Math.abs(pushed.getX() - zombie.getPosition().getX()) <= 1.5) {
                return true;
            }
        }
        return false;
    }

    private float armourFraction(models.entities.zombie.Zombie zombie) {
        int max = zombie.getType().getArmorType().getArmorHitpoints();
        if (max <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, zombie.totalArmor() / (float) max));
    }

    private boolean drawZomboss(Batch batch, models.entities.zombie.Zomboss boss,
                                float centerX, float feet) {
        String animation = views.assets.AnimationCatalog.zomboss(boss.getKind());
        String clipName = animator.clipName(animation,
                views.assets.AnimationCatalog.zombossClips(
                        boss.getKind(), boss.getMove(), boss.isStunned()));
        ClipRef clip = clipName == null ? null : animator.namedClip(animation, clipName);
        if (clip == null) {
            return false;
        }
        Float started = bossMoveStart.get(clipName);
        if (started == null) {
            bossMoveStart.clear();
            cue(views.assets.Audio.BOSS_MOVE);
            bossMoveStart.put(clipName, time);
            started = time;
        }
        float scale = animator.fitScale(animation, clipName, Lawn.cellHeight() * 2.25f);
        float anchor = animator.bottomOffset(animation, clipName, scale);
        float shift = 0f;
        if (bossFrom >= 0f && time - bossAt < BOSS_SLIDE) {
            float progress = (time - bossAt) / BOSS_SLIDE;
            float eased = progress * progress * (3f - 2f * progress);
            shift = (Lawn.rowBottom(bossFrom) - Lawn.rowBottom(boss.getPosition().getY()))
                    * (1f - eased);
        }
        animator.draw(batch, clip, time - started, centerX, feet - anchor + shift,
                scale, true, null);
        return true;
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

    private boolean drawPumpkin(Batch batch, models.game.PlacedPlant plant) {
        if (!animator.isReady()) {
            return false;
        }
        int max = models.entities.plant.PlantType.PUMPKIN.getBaseHp();
        float fraction = Math.max(0f, Math.min(1f, plant.getPumpkinHealth() / (float) max));
        String clipName = fraction > 0.66f ? "idle" : fraction > 0.33f ? "idle2" : "idle3";
        ClipRef clip = animator.plantClip(models.entities.plant.PlantType.PUMPKIN,
                clipName, "idle");
        if (clip == null) {
            return false;
        }
        batch.setColor(Color.WHITE);
        animator.draw(batch, clip, time + plant.getX() * 0.21f,
                Lawn.columnCenter(plant.getX()),
                Lawn.rowBottom(plant.getY()) + Lawn.cellHeight() * 0.16f + animator.plantLift(),
                animator.plantScale(), true, null);
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

    private void markProtected(Batch batch, int column, int row) {
        float pulse = 0.2f + 0.18f * (float) Math.abs(Math.sin(time * 2.4f));
        batch.setColor(GUARD.r, GUARD.g, GUARD.b, pulse);
        fill.draw(batch, Lawn.columnLeft(column), Lawn.rowBottom(row),
                Lawn.cellWidth(), Lawn.cellHeight());
        batch.setColor(GUARD.r, GUARD.g, GUARD.b, 0.9f);
        float left = Lawn.columnLeft(column);
        float bottom = Lawn.rowBottom(row);
        float width = Lawn.cellWidth();
        float height = Lawn.cellHeight();
        fill.draw(batch, left, bottom, width, GUARD_EDGE);
        fill.draw(batch, left, bottom + height - GUARD_EDGE, width, GUARD_EDGE);
        fill.draw(batch, left, bottom, GUARD_EDGE, height);
        fill.draw(batch, left + width - GUARD_EDGE, bottom, GUARD_EDGE, height);
    }

    protected void paint(Batch batch, Color color, double column, int row) {
        batch.setColor(color);
        fill.draw(batch, Lawn.columnLeft(column), Lawn.rowBottom(row),
                Lawn.cellWidth(), Lawn.cellHeight());
    }
}
