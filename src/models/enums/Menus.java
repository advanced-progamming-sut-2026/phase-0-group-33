package models.enums;

public enum Menus {
    GREENHOUSE,
    COLLECTION,
    SETTINGS,
    PROFILE,
    NEWS,
    TRAVELLOG,
    SIGNUP,
    LOGIN,
    MAIN,
    SHOP,
    GAME,
    LEADERBOARD;

    public static Menus getMenuByName(String menuName) {
        for (Menus m : Menus.values()) {
            if (m.toString().equalsIgnoreCase(menuName))
                return m;
        }
        return null;
    }
}
