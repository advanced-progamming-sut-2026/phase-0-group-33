package views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import controllers.menuControllers.GameController;
import models.App;
import models.game.GameSession;
import models.settings.GamePreferences;
import views.PvzGame;
import views.Router;
import views.ScreenId;
import views.assets.Art;
import views.battle.LawnView;
import views.ui.BaseScreen;
import views.ui.Toasts;
import views.ui.Ui;

public class BattleScreen extends ScreenAdapter {

    private static final float SECONDS_PER_TICK = 1f / GameSession.TICKS_PER_SECOND;
    private static final int MAX_TICKS_PER_FRAME = 6;

    protected final PvzGame game;
    protected final App app;
    protected final Skin skin;
    protected final Art art;
    protected final Router router;
    protected final Stage stage;
    protected final Toasts toasts;
    protected final GameController controller;
    protected final GameSession session;

    private LawnView lawnView;
    private Label sunLabel;
    private Label plantFoodLabel;
    private Label waveLabel;
    private ProgressBar waveBar;
    private float accumulator;
    private boolean paused;

    public BattleScreen(PvzGame game) {
        this.game = game;
        this.app = game.getApp();
        this.skin = game.getSkin();
        this.art = game.getArt();
        this.router = game.getRouter();
        this.stage = new Stage(new FitViewport(BaseScreen.WIDTH, BaseScreen.HEIGHT));
        this.toasts = new Toasts(skin, stage);
        this.controller = new GameController(game.getApp());
        this.session = game.getApp().getCurrentGameSession();
    }

    protected boolean isPaused() {
        return paused;
    }

    protected void setPaused(boolean value) {
        this.paused = value;
    }

    protected LawnView lawnView() {
        return lawnView;
    }

    @Override
    public void show() {
        if (session == null) {
            router.go(ScreenId.ADVENTURE);
            return;
        }

        Image backdrop = new Image(art.chapterBackground(chapterName()));
        backdrop.setScaling(Scaling.fill);
        backdrop.setFillParent(true);
        stage.addActor(backdrop);

        lawnView = createLawnView();
        lawnView.setShowGrid(GamePreferences.isGridVisible(username()));
        stage.addActor(lawnView);

        stage.addActor(buildHud());
        installInput();
        refreshHud();
    }

    protected LawnView createLawnView() {
        return new LawnView(session, art, skin);
    }

    private void installInput() {
        InputMultiplexer multiplexer = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    onEscape();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    protected void onEscape() {
        leave();
    }

    protected void leave() {
        app.setCurrentGameSession(null);
        router.go(ScreenId.ADVENTURE);
    }

    private String username() {
        return app.getCurrentUser() == null ? "guest" : app.getCurrentUser().getUsername();
    }

    private String chapterName() {
        return session.getLevel() == null ? null : session.getLevel().getChapter().getName();
    }

    private Table buildHud() {
        Table root = new Table();
        root.setFillParent(true);
        root.top();

        Table bar = new Table(skin);
        bar.setBackground(skin.getDrawable("panel"));
        bar.pad(6f, 16f, 6f, 16f);

        bar.add(Ui.iconCell(art.ui("image_ui_hud_ingame_sun"), 34f)).padRight(6f);
        sunLabel = new Label("0", skin, "h2");
        bar.add(sunLabel).width(90f).left().padRight(20f);

        bar.add(Ui.iconCell(art.ui("image_ui_hud_ingame_plantfood_button"), 32f)).padRight(6f);
        plantFoodLabel = new Label("0", skin, "h2");
        bar.add(plantFoodLabel).width(50f).left().padRight(24f);

        Table waveBox = new Table();
        waveLabel = new Label("", skin, "small");
        waveBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "gold-horizontal");
        waveBox.add(waveLabel).left().row();
        waveBox.add(waveBar).width(320f).height(16f).padTop(2f);
        bar.add(waveBox).padRight(20f);

        bar.add().expandX();
        bar.add(Ui.button(skin, "Menu", "small-brown", this::onEscape)).width(120f).height(46f);

        root.add(bar).growX().height(58f).row();
        root.add().expand();
        return root;
    }

    protected void refreshHud() {
        sunLabel.setText(String.valueOf(session.getSunManager().getSunBalance()));
        plantFoodLabel.setText(String.valueOf(session.getPlantFoods()));
        int wave = session.getWaveManager().getCurrentWave();
        int total = session.getWaveManager().getTotalWaves();
        waveLabel.setText(wave == 0 ? "The horde is coming" : "Wave " + wave + " of " + total);
        waveBar.setValue(total == 0 ? 0f : Math.min(1f, wave / (float) total));
    }

    protected void onTick() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1f);
        if (session != null && !paused && !session.isOver()) {
            advance(delta);
        }
        if (session != null) {
            refreshHud();
        }
        stage.act(delta);
        stage.draw();
    }

    private void advance(float delta) {
        int speed = GamePreferences.getGameSpeed(username());
        accumulator += delta * speed;
        int budget = MAX_TICKS_PER_FRAME;
        while (accumulator >= SECONDS_PER_TICK && budget-- > 0) {
            accumulator -= SECONDS_PER_TICK;
            session.advanceTime(1);
            onTick();
            if (session.isOver()) {
                accumulator = 0f;
                onGameOver();
                return;
            }
        }
        if (accumulator > SECONDS_PER_TICK) {
            accumulator = 0f;
        }
    }

    protected void onGameOver() {
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
