package views.menus;

import models.Result;

public interface AppMenu {
    public abstract void display();
    public abstract boolean processCommand(String cmd);

    default void printResultMsg(Result result) {
        for (String s : result.getMessages()) System.out.println(s);
    }
}
