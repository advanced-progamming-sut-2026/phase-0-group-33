package models;

import models.enums.Menus;
import models.game.GameSession;
import models.user.User;
import views.MenuHub;

import java.awt.*;

public class App {

    private static volatile App instance;

    private User currentUser;
    private boolean stayLoggedIn;
    private GameSession currentGameSession;
    private boolean exitRequested;

    // TODO: private MiniGame activeMiniGame;

    private Menus currentMenu;

    private App() {
        //TODO
    }

    public static App getInstance() {
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null) instance = new App();
            }
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void clearCurrentUser() {
        this.currentUser = null;
        this.stayLoggedIn = false;

    }

    public void navigateTo(Menus menu) {
        this.currentMenu = menu;

    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isStayLoggedIn() {
        return stayLoggedIn;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void setStayLoggedIn(boolean stayLoggedIn) {
        this.stayLoggedIn = stayLoggedIn;
    }

    public GameSession getCurrentGameSession() {
        return currentGameSession;
    }

    public void setCurrentGameSession(GameSession currentGameSession) {
        this.currentGameSession = currentGameSession;
    }

    // TODO
    //    public MiniGame getActiveMiniGame() {
    //        return activeMiniGame;
    //    }
    //    public void setActiveMiniGame(MiniGame activeMiniGame) {
    //        this.activeMiniGame = activeMiniGame;
    //    }

    public Menus getCurrentMenu() {
        return currentMenu;
    }

    public void run() {
        MenuHub menuHub = MenuHub.getInstance(this);
        menuHub.run();
    }
}
