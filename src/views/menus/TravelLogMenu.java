package views.menus;

import controllers.menuControllers.TravelLogController;

public class TravelLogMenu implements AppMenu {
    private final TravelLogController controller;

    public TravelLogMenu(TravelLogController controller) {
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