package views;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import models.App;
import views.assets.Animations;
import views.assets.Audio;
import views.assets.Art;
import views.assets.GameAssets;
import views.ui.UiSkin;

public class PvzGame extends Game {

    private App app;
    private GameAssets assets;
    private Art art;
    private Animations animations;
    private Audio audio;
    private Skin skin;
    private Router router;

    public App getApp() {
        return app;
    }

    public GameAssets getAssets() {
        return assets;
    }

    public Art getArt() {
        return art;
    }

    public Animations getAnimations() {
        return animations;
    }

    public Audio getAudio() {
        return audio;
    }

    public Skin getSkin() {
        return skin;
    }

    public Router getRouter() {
        return router;
    }

    @Override
    public void create() {
        app = App.getInstance();
        app.run();

        assets = new GameAssets();
        assets.loadFonts();
        art = new Art(assets);
        animations = new Animations();
        audio = new Audio();
        views.ui.Ui.setClickSound(() -> audio.play(Audio.CLICK));
        audio.setUser(app.getCurrentUser() == null ? null : app.getCurrentUser().getUsername());
        skin = UiSkin.build(assets);
        router = new Router(this);

        router.go(app.getCurrentUser() == null ? ScreenId.SIGNUP : ScreenId.MAIN);
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (animations != null) {
            animations.dispose();
        }
        if (audio != null) {
            audio.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
        super.dispose();
    }
}
