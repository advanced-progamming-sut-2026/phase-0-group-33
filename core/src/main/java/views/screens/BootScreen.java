package views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import views.PvzGame;

public class BootScreen extends ScreenAdapter {

    private final PvzGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    public BootScreen(PvzGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.15f, 0.07f, 1f, true);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Plants vs. Zombies -- Group 33", 40f, Gdx.graphics.getHeight() - 40f);
        font.draw(batch, "libGDX is running. Core game logic is on the classpath.",
                40f, Gdx.graphics.getHeight() - 70f);
        font.draw(batch, "Signed in: " + describeUser(), 40f, Gdx.graphics.getHeight() - 100f);
        font.draw(batch, String.format("%d fps", Gdx.graphics.getFramesPerSecond()),
                40f, 40f);
        batch.end();
    }

    private String describeUser() {
        if (game.getApp() == null || game.getApp().getCurrentUser() == null) {
            return "nobody yet";
        }
        return game.getApp().getCurrentUser().getNickname();
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
