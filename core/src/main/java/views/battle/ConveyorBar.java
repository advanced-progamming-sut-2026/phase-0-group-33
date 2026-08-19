package views.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import models.entities.plant.PlantType;
import models.game.GameSession;
import models.game.PlantSlot;
import pvz.libpvz.pam.ClipRef;
import views.assets.Art;

import java.util.function.Consumer;

public final class ConveyorBar extends Actor {

    private static final float SLOT_WIDTH = 74f;
    private static final float BELT_SPEED = 34f;
    private static final float SLIDE_SPEED = 260f;

    private final GameSession session;
    private final Art art;
    private final EntityAnimator animator;
    private final Consumer<PlantType> onPick;
    private final ObjectMap<PlantSlot, Float> positions = new ObjectMap<>();

    private float scroll;
    private float time;

    public ConveyorBar(GameSession session, Art art, EntityAnimator animator,
                       Consumer<PlantType> onPick) {
        this.session = session;
        this.art = art;
        this.animator = animator;
        this.onPick = onPick;
        setTouchable(Touchable.enabled);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                pick(x);
            }
        });
    }

    private void pick(float localX) {
        PlantSlot best = null;
        float bestDistance = SLOT_WIDTH;
        for (PlantSlot slot : session.getSlots()) {
            Float x = positions.get(slot);
            if (x == null) {
                continue;
            }
            float distance = Math.abs(localX - (x + SLOT_WIDTH / 2f));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = slot;
            }
        }
        if (best != null) {
            onPick.accept(best.getType());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
        scroll = (scroll + BELT_SPEED * delta) % SLOT_WIDTH;
        java.util.List<PlantSlot> slots = session.getSlots();
        for (int i = 0; i < slots.size(); i++) {
            PlantSlot slot = slots.get(i);
            float target = i * SLOT_WIDTH;
            Float current = positions.get(slot);
            if (current == null) {
                current = Math.max(target, getWidth());
                positions.put(slot, current);
            }
            float step = SLIDE_SPEED * delta;
            current = current > target ? Math.max(target, current - step)
                    : Math.min(target, current + step);
            positions.put(slot, current);
        }
        com.badlogic.gdx.utils.Array<PlantSlot> stale = new com.badlogic.gdx.utils.Array<>();
        for (PlantSlot slot : positions.keys()) {
            if (!slots.contains(slot)) {
                stale.add(slot);
            }
        }
        for (PlantSlot slot : stale) {
            positions.remove(slot);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawBelt(batch);
        batch.setColor(Color.WHITE);
        for (PlantSlot slot : session.getSlots()) {
            Float x = positions.get(slot);
            if (x == null) {
                continue;
            }
            drawPlant(batch, slot.getType(), getX() + x + SLOT_WIDTH / 2f);
        }
    }

    private void drawBelt(Batch batch) {
        TextureRegion belt = art.conveyorBelt();
        TextureRegion top = art.conveyorTop();
        if (belt == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float tile = SLOT_WIDTH;
        float beltHeight = getHeight() * 0.3f;
        for (float x = -tile; x < getWidth() + tile; x += tile) {
            batch.draw(belt, getX() + x - scroll, getY(), tile, beltHeight);
            if (top != null) {
                batch.draw(top, getX() + x - scroll, getY() + beltHeight * 0.86f,
                        tile, beltHeight * 0.34f);
            }
        }
    }

    private void drawPlant(Batch batch, PlantType type, float centreX) {
        float feet = getY() + getHeight() * 0.02f;
        ClipRef clip = animator.plantClip(type, "idle");
        if (clip == null) {
            TextureRegion region = art.plant(type);
            float size = getHeight() * 0.58f;
            batch.draw(region, centreX - size / 2f, feet, size, size);
            return;
        }
        float scale = animator.plantScale() * 0.78f;
        animator.draw(batch, clip, time, centreX, feet + animator.plantLift() * 0.72f,
                scale, true, null);
    }
}
