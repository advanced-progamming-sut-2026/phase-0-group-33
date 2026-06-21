package views.menus;

import controllers.menuControllers.MainController;

public class MainMenu implements AppMenu{
    private final MainController controller;

    public MainMenu(MainController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd){
        // TODO
        return false;
    }
}
