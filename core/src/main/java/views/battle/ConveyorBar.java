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
                pick(y);
            }
        });
    }

    private void pick(float localY) {
        PlantSlot best = null;
        float bestDistance = SLOT_WIDTH;
        float fromTop = getHeight() - localY;
        for (PlantSlot slot : session.getSlots()) {
            Float offset = positions.get(slot);
            if (offset == null) {
                continue;
            }
            float distance = Math.abs(fromTop - (offset + SLOT_WIDTH / 2f));
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
                current = Math.max(target, getHeight());
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
            Float offset = positions.get(slot);
            if (offset == null) {
                continue;
            }
            drawPlant(batch, slot.getType(),
                    getY() + getHeight() - offset - SLOT_WIDTH / 2f);
        }
    }

    private void drawBelt(Batch batch) {
        TextureRegion belt = art.conveyorBelt();
        TextureRegion side = art.conveyorTop();
        if (belt == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        float tile = SLOT_WIDTH;
        float beltWidth = getWidth() * 0.86f;
        for (float y = -tile; y < getHeight() + tile; y += tile) {
            batch.draw(belt, getX(), getY() + y + scroll, beltWidth, tile);
            if (side != null) {
                batch.draw(side, getX() + beltWidth * 0.86f, getY() + y + scroll,
                        beltWidth * 0.34f, tile);
            }
        }
    }

    private void drawPlant(Batch batch, PlantType type, float centreY) {
        float centreX = getX() + getWidth() * 0.43f;
        float feet = centreY - SLOT_WIDTH * 0.4f;
        ClipRef clip = animator.plantClip(type, "idle");
        if (clip == null) {
            TextureRegion region = art.plant(type);
            float size = SLOT_WIDTH * 0.72f;
            batch.draw(region, centreX - size / 2f, feet, size, size);
            return;
        }
        float scale = animator.plantScale() * 0.68f;
        animator.draw(batch, clip, time, centreX, feet + animator.plantLift() * 0.72f,
                scale, true, null);
    }
}
