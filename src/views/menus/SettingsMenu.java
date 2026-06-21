package views.menus;

import controllers.menuControllers.SettingsController;

public class SettingsMenu implements AppMenu {
    private final SettingsController controller;

    public SettingsMenu(SettingsController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        // TODO
        return false;
    }
}