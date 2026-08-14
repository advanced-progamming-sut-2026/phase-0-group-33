package views.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import models.entities.plant.PlantType;
import pvz.libpvz.pam.ClipRef;
import views.assets.Art;

public final class CursorOverlay extends Actor {

    private final Stage stage;
    private final Art art;
    private final EntityAnimator animator;
    private final Vector2 point = new Vector2();

    private PlantType plant;
    private String icon;
    private float time;

    public CursorOverlay(Stage stage, Art art, EntityAnimator animator) {
        this.stage = stage;
        this.art = art;
        this.animator = animator;
        setTouchable(Touchable.disabled);
        setBounds(0f, 0f, Lawn.STAGE_WIDTH, Lawn.STAGE_HEIGHT);
    }

    public void carryPlant(PlantType type) {
        this.plant = type;
        this.icon = null;
    }

    public void carryIcon(String region) {
        this.icon = region;
        this.plant = null;
    }

    public void carryNothing() {
        this.plant = null;
        this.icon = null;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        time += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (plant == null && icon == null) {
            return;
        }
        point.set(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(point);
        batch.setColor(1f, 1f, 1f, 0.85f);
        if (plant != null) {
            drawPlant(batch);
        } else {
            drawIcon(batch);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawPlant(Batch batch) {
        ClipRef clip = animator.plantClip(plant, "idle");
        if (clip == null) {
            TextureRegion region = art.plant(plant);
            float size = Lawn.cellHeight() * 0.7f;
            batch.draw(region, point.x - size / 2f, point.y - size / 2f, size, size);
            return;
        }
        animator.draw(batch, clip, time, point.x, point.y - Lawn.cellHeight() * 0.1f,
                animator.plantScale(), true, null);
    }

    private void drawIcon(Batch batch) {
        TextureRegion region = art.uiOptional(icon);
        if (region == null) {
            return;
        }
        float size = Lawn.cellHeight() * 0.62f;
        batch.draw(region, point.x - size / 2f, point.y - size / 2f, size, size);
    }
}
