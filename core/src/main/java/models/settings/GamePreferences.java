package models.settings;

import utils.UserDataStore;

public final class GamePreferences {

    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 3;

    private static final String KEY_SPEED = "pref.gameSpeed";
    private static final String KEY_GRID = "pref.showGrid";
    private static final String KEY_DEBUG = "pref.debugMode";
    private static final String KEY_MUSIC = "pref.musicVolume";
    private static final String KEY_SFX = "pref.sfxVolume";
    private static final String KEY_FULLSCREEN = "pref.fullscreen";

    private GamePreferences() {
    }

    private static UserDataStore store(String username) {
        return UserDataStore.forUser(username);
    }

    public static int getGameSpeed(String username) {
        int value = store(username).getInt(KEY_SPEED, MIN_SPEED);
        return Math.max(MIN_SPEED, Math.min(MAX_SPEED, value));
    }

    public static void setGameSpeed(String username, int speed) {
        UserDataStore store = store(username);
        store.setInt(KEY_SPEED, Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed)));
        store.save();
    }

    public static boolean isGridVisible(String username) {
        return store(username).getInt(KEY_GRID, 0) == 1;
    }

    public static void setGridVisible(String username, boolean visible) {
        UserDataStore store = store(username);
        store.setInt(KEY_GRID, visible ? 1 : 0);
        store.save();
    }

    public static int getMusicVolume(String username) {
        return clampVolume(store(username).getInt(KEY_MUSIC, 70));
    }

    public static void setMusicVolume(String username, int volume) {
        UserDataStore store = store(username);
        store.setInt(KEY_MUSIC, clampVolume(volume));
        store.save();
    }

    public static int getSfxVolume(String username) {
        return clampVolume(store(username).getInt(KEY_SFX, 80));
    }

    public static void setSfxVolume(String username, int volume) {
        UserDataStore store = store(username);
        store.setInt(KEY_SFX, clampVolume(volume));
        store.save();
    }

    public static boolean isFullscreen(String username) {
        return store(username).getInt(KEY_FULLSCREEN, 0) == 1;
    }

    public static void setFullscreen(String username, boolean fullscreen) {
        UserDataStore store = store(username);
        store.setInt(KEY_FULLSCREEN, fullscreen ? 1 : 0);
        store.save();
    }

    private static int clampVolume(int volume) {
        return Math.max(0, Math.min(100, volume));
    }

    public static boolean isDebugMode(String username) {
        return store(username).getInt(KEY_DEBUG, 0) == 1;
    }

    public static void setDebugMode(String username, boolean enabled) {
        UserDataStore store = store(username);
        store.setInt(KEY_DEBUG, enabled ? 1 : 0);
        store.save();
    }
}
