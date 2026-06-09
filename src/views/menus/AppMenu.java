package views.menus;

public interface AppMenu {
    public abstract void display();
    public abstract boolean processCommand(String cmd);
}
