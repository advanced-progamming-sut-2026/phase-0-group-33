package views.menus;

public interface AppMenu {
    public abstract void display();
    public abstract String processCommand(String cmd);
}
