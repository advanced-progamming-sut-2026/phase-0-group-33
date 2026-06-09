package model;

import model.enums.Menus;
import model.game.GameSession;
import model.user.User;
import views.MenuHub;

public class App {

    private static volatile App instance;

    private User currentUser;
    private boolean stayLoggedIn;
    private GameSession currentGameSession;

    // TODO: private MiniGame activeMiniGame;

    private Menus currentMenu;

    private App() {
        //TODO: reflect exception(log)
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
        MenuHub menuHub = new MenuHub(this);
        menuHub.run();
    }
}
