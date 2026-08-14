package views.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import pvz.libpvz.pam.ClipRef;
import views.assets.AnimationCatalog;
import views.assets.Animations;

public final class AnimatedActor extends Actor {

    private final Animations animations;
    private final ClipRef clip;
    private final float scale;
    private final float offsetY;

    private float time;

    private AnimatedActor(Animations animations, ClipRef clip, float scale, float offsetY) {
        this.animations = animations;
        this.clip = clip;
        this.scale = scale;
        this.offsetY = offsetY;
    }

    public static AnimatedActor plant(Animations animations, PlantType type, float box) {
        return build(animations, AnimationCatalog.plant(type), box, false, "idle", "idle2");
    }

    public static AnimatedActor zombie(Animations animations, ZombieType type, float box) {
        return build(animations, AnimationCatalog.zombie(type), box, false, "idle", "walk");
    }

    public static AnimatedActor named(Animations animations, String animation, float box,
                                      String... preferred) {
        return build(animations, animation, box, false, preferred);
    }

    public static AnimatedActor whole(Animations animations, String animation, float box,
                                      String... preferred) {
        return build(animations, animation, box, true, preferred);
    }

    private static AnimatedActor build(Animations animations, String animation, float box,
                                       String... preferred) {
        return build(animations, animation, box, false, preferred);
    }

    private static AnimatedActor build(Animations animations, String animation, float box,
                                       boolean wholeAnimation, String... preferred) {
        if (animations == null || !animations.isAvailable()) {
            return null;
        }
        String clipName = animations.firstClip(animation, preferred);
        ClipRef ref = animations.clip(animation, clipName);
        Rectangle bounds = animations.bounds(animation, wholeAnimation ? null : clipName);
        if (ref == null || bounds == null || bounds.height <= 0f || bounds.width <= 0f) {
            return null;
        }
        float scale = Math.min(box / bounds.width, box / bounds.height);
        float centreOffset = -(bounds.y + bounds.height / 2f) * scale;
        AnimatedActor actor = new AnimatedActor(animations, ref, scale, centreOffset);
        actor.setSize(box, box);
        return actor;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float centreX = getX() + getWidth() / 2f;
        float centreY = getY() + getHeight() / 2f + offsetY;
        batch.setColor(1f, 1f, 1f, getColor().a * parentAlpha);
        animations.draw(batch, clip, time, centreX, centreY, scale, scale, true, null);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
