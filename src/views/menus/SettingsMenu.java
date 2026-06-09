package views.menus;

import controllers.menuControllers.SettingsController;

public class SettingsMenu implements AppMenu {
    private final SettingsController controller;

    public SettingsMenu(SettingsController controller) {
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