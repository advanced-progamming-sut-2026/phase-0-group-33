package views.menus;

import controllers.menuControllers.LoginController;

public class LoginMenu implements AppMenu {
    private final LoginController controller;

    public LoginMenu(LoginController controller) {
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