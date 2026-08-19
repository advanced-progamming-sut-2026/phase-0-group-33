package views.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import models.App;
import views.PvzGame;
import views.Router;
import views.ScreenId;
import views.assets.Art;

public abstract class BaseScreen extends ScreenAdapter {

    public static final float WIDTH = 1280f;
    public static final float HEIGHT = 720f;

    protected final PvzGame game;
    protected final App app;
    protected final Skin skin;
    protected final Art art;
    protected final Router router;
    protected final Stage stage;
    protected final Toasts toasts;

    private TopBar topBar;

    protected BaseScreen(PvzGame game) {
        this.game = game;
        this.app = game.getApp();
        this.skin = game.getSkin();
        this.art = game.getArt();
        this.router = game.getRouter();
        this.stage = new Stage(new FitViewport(WIDTH, HEIGHT));
        this.toasts = new Toasts(skin, stage);
    }

    protected abstract String title();

    protected abstract void buildContent(Table body);

    protected ScreenId backTarget() {
        return ScreenId.MAIN;
    }

    protected boolean showsTopBar() {
        return app.getCurrentUser() != null;
    }

    protected boolean showsHeader() {
        return true;
    }

    protected TextureRegion background() {
        return art.menuBackground();
    }

    protected TopBar topBar() {
        return topBar;
    }

    @Override
    public void show() {
        TextureRegion backgroundRegion = background();
        if (backgroundRegion != null) {
            Image backdrop = new Image(backgroundRegion);
            backdrop.setScaling(Scaling.fill);
            backdrop.setFillParent(true);
            stage.addActor(backdrop);
            backdrop.getColor().a = 0f;
            backdrop.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.45f));
            addVignette();
        }

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        if (showsTopBar()) {
            topBar = new TopBar(app, skin, art, toasts);
            root.add(topBar).growX().height(58f).row();
        }

        Table body = new Table();
        root.add(body).grow().pad(18f, 34f, 24f, 34f).row();

        addHeader(body);
        buildContent(body);
        body.getColor().a = 0f;
        body.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.06f),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.3f,
                        com.badlogic.gdx.math.Interpolation.pow2Out)));

        InputMultiplexer multiplexer = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (Display.handleKey(keycode, app)) {
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    goBack();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
        stage.getRoot().getColor().a = 0f;
        stage.getRoot().addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.22f));
    }

    private void addHeader(Table body) {
        if (!showsHeader()) {
            body.add(Ui.label(skin, title(), "title")).padBottom(14f).row();
            return;
        }
        Table header = new Table();
        header.add(Ui.button(skin, "Back", "brown", this::goBack)).height(52f).width(130f).left();
        Table heading = new Table();
        heading.add(Ui.label(skin, title(), "title")).row();
        heading.add(Ui.divider(skin, 260f)).padTop(2f);
        header.add(heading).expandX().center();
        header.add().width(130f);
        body.add(header).growX().padBottom(14f).row();
    }

    private void addVignette() {
        TextureRegion fade = new TextureRegion(game.getAssets().verticalFade());
        Image top = new Image(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(fade));
        top.setScaling(Scaling.stretch);
        top.setBounds(0f, HEIGHT - 150f, WIDTH, 150f);
        stage.addActor(top);

        TextureRegion flipped = new TextureRegion(fade);
        flipped.flip(false, true);
        Image bottom = new Image(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(flipped));
        bottom.setScaling(Scaling.stretch);
        bottom.setBounds(0f, 0f, WIDTH, 110f);
        stage.addActor(bottom);
    }

    protected void goBack() {
        ScreenId target = backTarget();
        if (target != null) {
            router.go(target);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1f);
        game.getAnimations().update();
        stage.act(delta);
        stage.draw();
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
