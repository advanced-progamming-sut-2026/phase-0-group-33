package views.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import views.assets.Art;

public final class ScoreMeter extends Actor {

    private static final String RING = "image_ui_hud_ingame_score_meter_bg";
    private static final String FILL = "image_ui_hud_ingame_score_meter_fill";
    private static final String GLOW = "image_ui_hud_ingame_score_meter_highlight";
    private static final int MILESTONE = 500;

    private final Art art;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private int score;
    private float shown;
    private float pop;
    private float clock;

    public ScoreMeter(Art art, BitmapFont font) {
        this.art = art;
        this.font = font;
    }

    public void setScore(int score) {
        if (score > this.score) {
            pop = 1f;
        }
        this.score = score;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        clock += delta;
        pop = Math.max(0f, pop - delta * 2.2f);
        float wanted = (score % MILESTONE) / (float) MILESTONE;
        if (wanted < shown - 0.5f) {
            shown = 0f;
        }
        shown += (wanted - shown) * Math.min(1f, delta * 6f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion ring = art.uiOptional(RING);
        if (ring == null) {
            return;
        }
        float size = Math.min(getWidth(), getHeight());
        float grow = 1f + pop * 0.14f;
        float drawn = size * grow;
        float x = getX() + (getWidth() - drawn) / 2f;
        float y = getY() + (getHeight() - drawn) / 2f;
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(ring, x, y, drawn, drawn);
        TextureRegion fill = art.uiOptional(FILL);
        if (fill != null && shown > 0.02f) {
            float inner = drawn * 0.82f * shown;
            batch.setColor(1f, 1f, 1f, parentAlpha * 0.92f);
            batch.draw(fill, getX() + (getWidth() - inner) / 2f,
                    getY() + (getHeight() - inner) / 2f, inner, inner);
        }
        TextureRegion glow = art.uiOptional(GLOW);
        if (glow != null) {
            float pulse = 0.4f + 0.35f * (float) Math.abs(Math.sin(clock * 2.2f)) + pop * 0.4f;
            batch.setColor(1f, 1f, 1f, Math.min(1f, pulse) * parentAlpha);
            batch.draw(glow, x, y, drawn, drawn);
        }
        batch.setColor(Color.WHITE);
        drawNumber(batch, parentAlpha);
    }

    private void drawNumber(Batch batch, float parentAlpha) {
        if (font == null) {
            return;
        }
        String text = String.valueOf(score);
        layout.setText(font, text);
        float scale = Math.min(1f, (getWidth() * 0.78f) / Math.max(1f, layout.width));
        font.getData().setScale(scale);
        layout.setText(font, text);
        Color before = font.getColor().cpy();
        font.setColor(1f, 1f, 1f, parentAlpha);
        font.draw(batch, layout, getX() + (getWidth() - layout.width) / 2f,
                getY() + (getHeight() + layout.height) / 2f);
        font.setColor(before);
        font.getData().setScale(1f);
    }
}
