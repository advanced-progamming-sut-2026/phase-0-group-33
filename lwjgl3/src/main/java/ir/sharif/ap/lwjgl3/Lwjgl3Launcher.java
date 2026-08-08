package ir.sharif.ap.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import views.PvzGame;

public final class Lwjgl3Launcher {

    private Lwjgl3Launcher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Plants vs. Zombies - Group 33");
        config.setWindowedMode(1280, 720);
        config.setWindowSizeLimits(960, 540, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        new Lwjgl3Application(new PvzGame(), config);
    }
}
