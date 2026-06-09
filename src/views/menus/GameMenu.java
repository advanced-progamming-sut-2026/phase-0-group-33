package views.menus;

import controllers.menuControllers.GameController;

public class GameMenu implements AppMenu {
    private final GameController controller;

    public GameMenu(GameController controller) {
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