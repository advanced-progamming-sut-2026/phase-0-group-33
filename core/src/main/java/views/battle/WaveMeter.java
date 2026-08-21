package views.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import views.assets.Art;

public final class WaveMeter extends Actor {

    public static final float NATURAL_WIDTH = 273f;
    public static final float NATURAL_HEIGHT = 33f;

    private static final String TROUGH = "image_ui_hud_ingame_progress_meter";
    private static final String FILL = "image_ui_hud_ingame_progress_meter_fill";
    private static final String BOSS_FILL = "image_ui_hud_ingame_progress_meter_zomboss_fill";
    private static final String POLE = "image_ui_hud_ingame_progress_meter_flag_pole";
    private static final String FLAG = "image_ui_hud_ingame_progress_meter_flag_default";
    private static final String HEAD = "image_ui_hud_ingame_progress_meter_zombiehead";
    private static final String BOSS_HEAD = "image_ui_hud_ingame_progress_meter_zomboss_head";

    private static final float INSET = 7f;
    private static final float MAX_FLAGS = 12f;

    private final Art art;

    private float progress;
    private float shown;
    private int waves = 1;
    private boolean boss;
    private float clock;

    public WaveMeter(Art art) {
        this.art = art;
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
    }

    public void setWaves(int waves) {
        this.waves = Math.max(1, waves);
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        clock += delta;
        float step = Math.max(0.12f, Math.abs(progress - shown)) * delta * 4.5f;
        shown = shown < progress ? Math.min(progress, shown + step)
                : Math.max(progress, shown - step);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion trough = art.uiOptional(TROUGH);
        if (trough == null) {
            return;
        }
        float scale = getHeight() / NATURAL_HEIGHT;
        float track = getWidth() - INSET * 2f * scale;
        float left = getX() + INSET * scale;
        drawTrack(batch, left, track, scale);
        drawFill(batch, left, track, scale);
        batch.setColor(Color.WHITE);
        batch.draw(trough, getX(), getY(), getWidth(), getHeight());
        drawFlags(batch, left, track, scale);
        drawHead(batch, left, track, scale);
        batch.setColor(Color.WHITE);
    }

    private void drawTrack(Batch batch, float left, float track, float scale) {
        TextureRegion flat = art.uiOptional("white-pixel");
        if (flat == null) {
            return;
        }
        batch.setColor(0.16f, 0.13f, 0.1f, 0.82f);
        batch.draw(flat, left, getY() + INSET * scale, track,
                getHeight() - INSET * 2f * scale);
    }

    private void drawFill(Batch batch, float left, float track, float scale) {
        TextureRegion fill = art.uiOptional(boss ? BOSS_FILL : FILL);
        if (fill == null) {
            return;
        }
        float width = track * shown;
        if (width <= 0f) {
            return;
        }
        float height = getHeight() - INSET * 2f * scale;
        batch.setColor(Color.WHITE);
        batch.draw(fill, left + track - width, getY() + INSET * scale, width, height);
    }

    private void drawFlags(Batch batch, float left, float track, float scale) {
        TextureRegion pole = art.uiOptional(POLE);
        TextureRegion flag = art.uiOptional(FLAG);
        if (pole == null || flag == null || boss) {
            return;
        }
        int shownWaves = (int) Math.min(waves, MAX_FLAGS);
        for (int wave = 1; wave <= shownWaves; wave++) {
            float at = wave / (float) shownWaves;
            float x = left + track * (1f - at);
            boolean last = wave == shownWaves;
            float poleHeight = getHeight() * (last ? 1.35f : 1.05f);
            float poleWidth = pole.getRegionWidth() * scale * 0.5f;
            batch.setColor(Color.WHITE);
            batch.draw(pole, x - poleWidth / 2f, getY() + getHeight() * 0.35f,
                    poleWidth, poleHeight);
            float flagWidth = flag.getRegionWidth() * scale * (last ? 1.2f : 0.85f);
            float flagHeight = flag.getRegionHeight() * scale * (last ? 1.2f : 0.85f);
            float wave1 = (float) Math.sin(clock * 4f + wave) * flagWidth * 0.06f;
            batch.draw(flag, x - poleWidth / 2f + wave1,
                    getY() + getHeight() * 0.35f + poleHeight - flagHeight,
                    flagWidth, flagHeight);
        }
    }

    private void drawHead(Batch batch, float left, float track, float scale) {
        TextureRegion head = art.uiOptional(boss ? BOSS_HEAD : HEAD);
        if (head == null) {
            return;
        }
        float size = head.getRegionHeight() * scale * 1.15f;
        float width = head.getRegionWidth() * scale * 1.15f;
        float bob = (float) Math.sin(clock * 6f) * size * 0.05f;
        batch.setColor(Color.WHITE);
        batch.draw(head, left + track * (1f - shown) - width / 2f,
                getY() + (getHeight() - size) / 2f + bob, width, size);
    }
}
