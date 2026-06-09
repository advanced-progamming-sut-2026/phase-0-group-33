package views.menus;

import controllers.menuControllers.TravelLogController;

public class TravellogMenu implements AppMenu {
    private final TravelLogController controller;

    public TravellogMenu(TravelLogController controller) {
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