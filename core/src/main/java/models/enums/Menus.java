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
        if (menuName == null) {
            return null;
        }
        String normalized = menuName.replaceAll("[\\s_-]", "");
        for (Menus m : Menus.values()) {
            if (m.name().equalsIgnoreCase(normalized)) {
                return m;
            }
        }
        return null;
    }
}
