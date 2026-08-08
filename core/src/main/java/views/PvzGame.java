package views;

import com.badlogic.gdx.Game;
import models.App;
import views.screens.BootScreen;

public class PvzGame extends Game {

    private App app;

    public App getApp() {
        return app;
    }

    @Override
    public void create() {
        app = App.getInstance();
        setScreen(new BootScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        super.dispose();
    }
}
