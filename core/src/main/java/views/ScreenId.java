package views;

import models.enums.Menus;

public enum ScreenId {
    CONNECT,
    SIGNUP,
    LOGIN,
    MAIN,
    PROFILE,
    ADVENTURE,
    COLLECTION,
    GREENHOUSE,
    ZEN_GARDEN,
    SANDBOX_SETUP,
    SANDBOX,
    SHOP,
    NEWS,
    LEADERBOARD,
    SETTINGS,
    QUESTS,
    SEED_SELECT,
    BATTLE;

    public static ScreenId from(Menus menu) {
        if (menu == null) {
            return MAIN;
        }
        switch (menu) {
            case SIGNUP:
                return SIGNUP;
            case LOGIN:
                return LOGIN;
            case PROFILE:
                return PROFILE;
            case COLLECTION:
                return COLLECTION;
            case GREENHOUSE:
                return GREENHOUSE;
            case ZEN_GARDEN:
                return ZEN_GARDEN;
            case SHOP:
                return SHOP;
            case NEWS:
                return NEWS;
            case LEADERBOARD:
                return LEADERBOARD;
            case SETTINGS:
                return SETTINGS;
            case TRAVELLOG:
                return QUESTS;
            case GAME:
                return BATTLE;
            default:
                return MAIN;
        }
    }
}
