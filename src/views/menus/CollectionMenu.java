package views.menus;

import controllers.menuControllers.CollectionController;

public class CollectionMenu implements AppMenu {
    private final CollectionController controller;

    public CollectionMenu(CollectionController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        // TODO
        return false;
    }
}