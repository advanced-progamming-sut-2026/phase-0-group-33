package views.menus;

import controllers.menuControllers.ShopController;

public class ShopMenu implements AppMenu {
    private final ShopController controller;

    public ShopMenu(ShopController controller) {
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