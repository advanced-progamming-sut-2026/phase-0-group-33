package views.menus;

import controllers.menuControllers.SignupController;

public class SignupMenu implements AppMenu {
    private final SignupController controller;

    public SignupMenu(SignupController controller) {
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