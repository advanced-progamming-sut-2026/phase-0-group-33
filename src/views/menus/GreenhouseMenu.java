package views.menus;

import controllers.menuControllers.GreenhouseController;

public class GreenhouseMenu implements AppMenu {
    private final GreenhouseController controller;

    public GreenhouseMenu(GreenhouseController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        // TODO
        return false;
    }
}