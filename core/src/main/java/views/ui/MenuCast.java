package views.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import pvz.libpvz.pam.ClipRef;
import views.assets.AnimationCatalog;
import views.assets.Animations;

public final class MenuCast extends Actor {

    private static final PlantType[] CROWD = {
        PlantType.SUNFLOWER, PlantType.PEASHOOTER, PlantType.WALL_NUT,
        PlantType.SNOW_PEA, PlantType.CHERRY_BOMB};
    private static final ZombieType[] SHAMBLERS = {
        ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD};
    private static final float WALK_SECONDS = 26f;
    private static final float PLANT_UNIT = 0.56f;
    private static final float ZOMBIE_UNIT = 0.46f;
    private static final float PLANT_LIFT = 0.36f;
    private static final float ZOMBIE_LIFT = 0.5f;
    private static final float REFERENCE_CELL = 93f;

    private final Animations animations;

    private float clock;

    public MenuCast(Animations animations) {
        this.animations = animations;
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        clock += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (animations == null || !animations.isAvailable()) {
            return;
        }
        batch.setColor(1f, 1f, 1f, parentAlpha);
        drawPlants(batch);
        drawZombie(batch);
        batch.setColor(Color.WHITE);
    }

    private void drawPlants(Batch batch) {
        float step = getWidth() / (CROWD.length + 1f);
        for (int i = 0; i < CROWD.length; i++) {
            String animation = AnimationCatalog.plant(CROWD[i]);
            String clipName = animations.firstClip(animation, "idle", "idle2");
            if (clipName == null) {
                continue;
            }
            ClipRef clip = animations.clip(animation, clipName);
            if (clip == null) {
                continue;
            }
            float scale = PLANT_UNIT * getHeight() / REFERENCE_CELL;
            float bob = (float) Math.sin(clock * 1.7f + i) * 3f;
            animations.draw(batch, clip, clock + i * 0.6f,
                    getX() + step * (i + 1), getY() + getHeight() * PLANT_LIFT + bob,
                    scale, scale, true, null);
        }
    }

    private void drawZombie(Batch batch) {
        int index = (int) (clock / WALK_SECONDS) % SHAMBLERS.length;
        ZombieType type = SHAMBLERS[index];
        String animation = AnimationCatalog.zombie(type);
        String clipName = animations.firstClip(animation, "walk", "idle");
        if (clipName == null) {
            return;
        }
        ClipRef clip = animations.clip(animation, clipName);
        if (clip == null) {
            return;
        }
        float progress = (clock % WALK_SECONDS) / WALK_SECONDS;
        float scale = ZOMBIE_UNIT * getHeight() / REFERENCE_CELL;
        float x = getX() + getWidth() + 90f - progress * (getWidth() + 190f);
        animations.draw(batch, clip, clock, x, getY() + getHeight() * ZOMBIE_LIFT,
                -scale, scale, true, null);
    }
}
