package views.menus;

import controllers.menuControllers.LeaderboardController;

public class LeaderboardMenu implements AppMenu {
    private final LeaderboardController controller;

    public LeaderboardMenu(LeaderboardController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        // TODO
    }

    @Override
    public String processCommand(String cmd) {
        // TODO
        return null;
    }
}