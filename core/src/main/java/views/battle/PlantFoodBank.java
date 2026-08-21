package views.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import views.assets.Art;

public final class PlantFoodBank extends Actor {

    public static final float NATURAL_WIDTH = 206f;
    public static final float NATURAL_HEIGHT = 88f;

    private static final String BANK = "image_ui_hud_ingame_plantfood_bank";
    private static final String FILLED = "image_ui_hud_ingame_plantfood_bank_filled_slot";
    private static final String FILLING = "image_ui_hud_ingame_plantfood_bank_filling_slot";

    private static final float[] SLOT_X = {0.4199f, 0.5364f, 0.6529f, 0.7694f, 0.8859f};
    private static final float SLOT_Y = 0.5114f;
    private static final float SLOT_SIZE = 0.075f;

    private final Art art;

    private int stored;
    private float clock;

    public PlantFoodBank(Art art) {
        this.art = art;
    }

    public void setStored(int stored) {
        this.stored = Math.max(0, stored);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        clock += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion bank = art.uiOptional(BANK);
        if (bank == null) {
            return;
        }
        batch.setColor(Color.WHITE);
        batch.draw(bank, getX(), getY(), getWidth(), getHeight());
        TextureRegion filled = art.uiOptional(FILLED);
        TextureRegion filling = art.uiOptional(FILLING);
        if (filled == null) {
            return;
        }
        float size = getWidth() * SLOT_SIZE;
        for (int slot = 0; slot < SLOT_X.length && slot < stored; slot++) {
            boolean newest = slot == stored - 1 && filling != null;
            TextureRegion dot = newest ? filling : filled;
            float pulse = newest ? 1f + (float) Math.sin(clock * 5f) * 0.18f : 1f;
            float wide = size * pulse * (newest ? 1.2f : 1f);
            batch.draw(dot, getX() + getWidth() * SLOT_X[slot] - wide / 2f,
                    getY() + getHeight() * SLOT_Y - wide / 2f, wide, wide);
        }
    }
}
