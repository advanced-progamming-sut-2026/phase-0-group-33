package views.menus;

import controllers.menuControllers.GeneralController;

public class GeneralMenu implements AppMenu {
    private final GeneralController controller;

    public GeneralMenu(GeneralController controller) {
        this.controller = controller;
    }

    @Override
    public void display() {
        // TODO
    }

    @Override
    public boolean processCommand(String cmd) {
        // TODO
        return false;
    }
}