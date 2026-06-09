package views.menus;

import controllers.menuControllers.NewsController;

public class NewsMenu implements AppMenu {
    private final NewsController controller;

    public NewsMenu(NewsController controller) {
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