package views.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.Map;

public final class Animations implements Disposable {

    private static final String INDEX = "animations.json";
    private static final String RESOLUTION = "768";

    private final TextureBank textures;
    private final PamPlayer player;
    private final ObjectMap<String, String> paths = new ObjectMap<>();
    private final ObjectMap<String, ObjectSet<String>> clipNames = new ObjectMap<>();
    private final ObjectMap<String, ClipRef> clipCache = new ObjectMap<>();
    private final ObjectMap<String, Rectangle> boundsCache = new ObjectMap<>();

    private boolean broken;

    public Animations() {
        TextureBank bank = null;
        PamPlayer pam = null;
        try {
            bank = new TextureBank(RESOLUTION, Gdx.files.internal(""));
            pam = new PamPlayer(bank, Gdx.files.internal(""));
            readIndex();
        } catch (RuntimeException e) {
            broken = true;
        }
        this.textures = bank;
        this.player = pam;
    }

    private void readIndex() {
        JsonValue root = new JsonReader().parse(Gdx.files.internal(INDEX));
        for (JsonValue entry = root.get("animations").child; entry != null; entry = entry.next) {
            String name = entry.getString("name", null);
            String path = entry.getString("path", null);
            if (name == null || path == null) {
                continue;
            }
            paths.put(name, path);
            ObjectSet<String> clips = new ObjectSet<>();
            JsonValue clipList = entry.get("clips");
            if (clipList != null) {
                for (JsonValue clip = clipList.child; clip != null; clip = clip.next) {
                    clips.add(clip.name());
                }
            }
            clipNames.put(name, clips);
        }
    }

    public boolean isAvailable() {
        return !broken && player != null;
    }

    public void update() {
        if (textures != null) {
            textures.update();
        }
    }

    public boolean has(String animation) {
        return animation != null && paths.containsKey(animation);
    }

    public boolean hasClip(String animation, String clip) {
        ObjectSet<String> clips = clipNames.get(animation);
        return clips != null && clips.contains(clip);
    }

    public String firstClip(String animation, String... preferred) {
        for (String candidate : preferred) {
            if (hasClip(animation, candidate)) {
                return candidate;
            }
        }
        ObjectSet<String> clips = clipNames.get(animation);
        return clips == null || clips.isEmpty() ? null : clips.first();
    }

    public ClipRef clip(String animation, String clip) {
        if (!isAvailable() || clip == null || !has(animation)) {
            return null;
        }
        String key = animation + '#' + clip;
        if (clipCache.containsKey(key)) {
            return clipCache.get(key);
        }
        ClipRef ref = null;
        try {
            ref = player.getClip(paths.get(animation), clip);
        } catch (RuntimeException e) {
            ref = null;
        }
        clipCache.put(key, ref);
        return ref;
    }

    public Rectangle bounds(String animation) {
        if (!isAvailable() || !has(animation)) {
            return null;
        }
        if (boundsCache.containsKey(animation)) {
            return boundsCache.get(animation);
        }
        Rectangle rect = null;
        try {
            rect = player.bounds(paths.get(animation));
        } catch (RuntimeException e) {
            rect = null;
        }
        boundsCache.put(animation, rect);
        return rect;
    }

    public void draw(Batch batch, ClipRef clip, float time, float x, float y,
                     float scaleX, float scaleY, boolean loop, Map<String, Boolean> visibility) {
        if (!isAvailable() || clip == null) {
            return;
        }
        try {
            player.draw(batch, clip, time, x, y, scaleX, scaleY, loop, visibility);
        } catch (RuntimeException e) {
            broken = true;
        }
    }

    @Override
    public void dispose() {
        if (textures != null) {
            textures.dispose();
        }
    }
}
