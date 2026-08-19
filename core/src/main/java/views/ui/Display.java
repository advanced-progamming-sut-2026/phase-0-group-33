package views.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public final class Display {

    private static final int MIN_WIDTH = 960;
    private static final int MIN_HEIGHT = 540;

    private static int windowedWidth = 1280;
    private static int windowedHeight = 720;

    private Display() {
    }

    public static boolean isFullscreen() {
        return Gdx.graphics != null && Gdx.graphics.isFullscreen();
    }

    public static void setFullscreen(boolean fullscreen) {
        if (Gdx.graphics == null || fullscreen == Gdx.graphics.isFullscreen()) {
            return;
        }
        if (fullscreen) {
            windowedWidth = Math.max(MIN_WIDTH, Gdx.graphics.getWidth());
            windowedHeight = Math.max(MIN_HEIGHT, Gdx.graphics.getHeight());
            com.badlogic.gdx.Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            if (mode != null) {
                Gdx.graphics.setFullscreenMode(mode);
            }
        } else {
            Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
        }
    }

    public static void toggle() {
        setFullscreen(!isFullscreen());
    }

    public static boolean isToggleKey(int keycode) {
        if (keycode == Input.Keys.F11) {
            return true;
        }
        boolean alt = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
        return alt && (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER);
    }

    public static boolean handleKey(int keycode, models.App app) {
        if (!isToggleKey(keycode)) {
            return false;
        }
        toggle();
        if (app != null && app.getCurrentUser() != null) {
            models.settings.GamePreferences.setFullscreen(
                    app.getCurrentUser().getUsername(), isFullscreen());
        }
        return true;
    }
}
