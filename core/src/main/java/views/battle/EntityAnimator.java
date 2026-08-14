package views.battle;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.ObjectMap;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import pvz.libpvz.pam.ClipRef;
import views.assets.AnimationCatalog;
import views.assets.Animations;

import java.util.HashMap;
import java.util.Map;

public final class EntityAnimator {

    private static final float PLANT_UNIT = 0.56f;
    private static final float ZOMBIE_UNIT = 0.46f;
    private static final float PLANT_LIFT = 0.36f;
    private static final float ZOMBIE_LIFT = 0.5f;

    private final Animations animations;
    private final ObjectMap<String, ClipRef> refs = new ObjectMap<>();
    private static final float REFERENCE_CELL = 93f;
    private final ObjectMap<ZombieType, Map<String, Boolean>> armour = new ObjectMap<>();

    public EntityAnimator(Animations animations) {
        this.animations = animations;
    }

    public boolean isReady() {
        return animations.isAvailable();
    }

    public void update() {
        animations.update();
    }

    public ClipRef plantClip(PlantType type, String... preferred) {
        return lookup(AnimationCatalog.plant(type), preferred);
    }

    public ClipRef zombieClip(ZombieType type, String... preferred) {
        return lookup(AnimationCatalog.zombie(type), preferred);
    }

    private ClipRef lookup(String animation, String... preferred) {
        String clip = animations.firstClip(animation, preferred);
        if (clip == null) {
            return null;
        }
        String key = animation + '#' + clip;
        if (refs.containsKey(key)) {
            return refs.get(key);
        }
        ClipRef ref = animations.clip(animation, clip);
        refs.put(key, ref);
        return ref;
    }

    public float plantScale() {
        return PLANT_UNIT * Lawn.cellHeight() / REFERENCE_CELL;
    }

    public float zombieScale() {
        return ZOMBIE_UNIT * Lawn.cellHeight() / REFERENCE_CELL;
    }

    public float plantLift() {
        return Lawn.cellHeight() * PLANT_LIFT;
    }

    public float zombieLift() {
        return Lawn.cellHeight() * ZOMBIE_LIFT;
    }

    public Map<String, Boolean> armourFor(ZombieType type) {
        if (armour.containsKey(type)) {
            return armour.get(type);
        }
        String part = AnimationCatalog.armorPart(type);
        Map<String, Boolean> map = null;
        if (part != null) {
            map = new HashMap<>();
            map.put(part, Boolean.TRUE);
        }
        armour.put(type, map);
        return map;
    }

    public void draw(Batch batch, ClipRef clip, float time, float x, float y,
                     float scale, boolean loop, Map<String, Boolean> visibility) {
        animations.draw(batch, clip, time, x, y, scale, scale, loop, visibility);
    }
}
