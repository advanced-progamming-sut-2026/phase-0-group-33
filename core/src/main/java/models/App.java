package models;

import controllers.managers.UserManager;
import models.enums.Menus;
import models.game.GameSession;
import models.user.User;
import utils.NewsStore;
import utils.SessionStore;

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
    }

    public void resumeOnline() {
        String username = utils.DeviceSettings.resumeUser();
        String token = utils.DeviceSettings.resumeToken();
        if (username.isEmpty() || token.isEmpty()) {
            return;
        }
        if (!net.Online.get().resume(username, token).isSuccessfull()) {
            utils.DeviceSettings.clearResume();
            return;
        }
        User user = UserManager.getInstance().loadUser(username);
        if (user == null) {
            utils.DeviceSettings.clearResume();
            return;
        }
        setCurrentUser(user);
        setStayLoggedIn(true);
        navigateTo(Menus.MAIN);
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
            int unread = NewsStore.countUnread(username);
            if (unread > 0) {
                System.out.println("[!] You have " + unread
                        + " unread news; check the news menu.");
            }
        } else {
            SessionStore.clearSession();
        }
    }
}
