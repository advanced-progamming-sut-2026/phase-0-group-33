package views.menus;

import controllers.menuControllers.ProfileController;

public class ProfileMenu implements AppMenu {
    private final ProfileController controller;

    public ProfileMenu(ProfileController controller) {
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