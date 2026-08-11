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

        if (showsHeader()) {
            Table header = new Table();
            header.add(Ui.button(skin, "Back", "brown", this::goBack)).height(52f).left();
            header.add(Ui.label(skin, title(), "title")).expandX().center();
            header.add().width(120f);
            body.add(header).growX().padBottom(14f).row();
        } else {
            body.add(Ui.label(skin, title(), "title")).padBottom(14f).row();
        }

        buildContent(body);

        InputMultiplexer multiplexer = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
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

    protected void goBack() {
        ScreenId target = backTarget();
        if (target != null) {
            router.go(target);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1f);
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
