package models;

import controllers.managers.UserManager;
import models.enums.Menus;
import models.game.GameSession;
import models.user.User;
import utils.SessionStore;
import views.MenuHub;

public class App {

    private static volatile App instance;

    private boolean stayLoggedIn;
    private GameSession currentGameSession;
    private Menus currentMenu;
    private boolean exitRequested;

    private App() {
        this.currentMenu = Menus.SIGNUP;
    }

    public static App getInstance() {
        if (instance == null) {
            synchronized (App.class) {
                if (instance == null) {
                    instance = new App();
                }
            }
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        UserManager.getInstance().setCurrentUser(user);
    }

    public void clearCurrentUser() {
        UserManager.getInstance().setCurrentUser(null);
        this.stayLoggedIn = false;
    }

    public void navigateTo(Menus menu) {
        this.currentMenu = menu;
    }

    public User getCurrentUser() {
        return UserManager.getInstance().getCurrentUser();
    }

    public boolean isStayLoggedIn() {
        return stayLoggedIn;
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

    public Menus getCurrentMenu() {
        return currentMenu;
    }

    public void requestExit() {
        this.exitRequested = true;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void run() {
        restoreSession();
        MenuHub menuHub = MenuHub.getInstance(this);
        menuHub.run();
    }

    private void restoreSession() {
        String username = SessionStore.loadSession();
        if (username == null) {
            return;
        }
        User user = UserManager.getInstance().loadUser(username);
        if (user != null) {
            setCurrentUser(user);
            setStayLoggedIn(true);
            navigateTo(Menus.MAIN);
            System.out.println("Welcome back, " + user.getNickname() + "! You are still logged in.");
        } else {
            SessionStore.clearSession();
        }
    }
}
