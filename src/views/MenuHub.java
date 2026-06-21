package views;

import controllers.menuControllers.*;
import models.App;
import models.enums.Menus;
import views.menus.AppMenu;
import views.menus.*;


import java.util.Scanner;

public class MenuHub {
    private static MenuHub instance;

    private final App app;
    private final Scanner scanner;
    private Menus currentMenu;

    private final AppMenu collectionMenu;
    private final AppMenu gameMenu;
    private final AppMenu greenhouseMenu;
    private final AppMenu leaderboardMenu;
    private final AppMenu loginMenu;
    private final AppMenu mainMenu;
    private final AppMenu newsMenu;
    private final AppMenu profileMenu;
    private final AppMenu settingsMenu;
    private final AppMenu shopMenu;
    private final AppMenu signupMenu;
    private final AppMenu travellogMenu;

    private MenuHub(App app) {
        this.app = app;
        this.scanner = new Scanner(System.in);
        this.currentMenu = Menus.SIGNUP;

        CollectionController collectionController = new CollectionController(app);
        GameController gameController = new GameController(app);
        GreenhouseController greenhouseController = new GreenhouseController(app);
        LeaderboardController leaderboardController = new LeaderboardController(app);
        LoginController loginController = new LoginController(app);
        MainController mainController = new MainController(app);
        NewsController newsController = new NewsController(app);
        ProfileController profileController = new ProfileController(app);
        SettingsController settingsController = new SettingsController(app);
        ShopController shopController = new ShopController(app);
        SignupController signupController = new SignupController(app);
        TravelLogController travellogController = new TravelLogController(app);

        this.collectionMenu = new CollectionMenu(collectionController);
        this.gameMenu = new GameMenu(gameController);
        this.greenhouseMenu = new GreenhouseMenu(greenhouseController);
        this.leaderboardMenu = new LeaderboardMenu(leaderboardController);
        this.loginMenu = new LoginMenu(loginController);
        this.mainMenu = new MainMenu(mainController);
        this.newsMenu = new NewsMenu(newsController);
        this.profileMenu = new ProfileMenu(profileController);
        this.settingsMenu = new SettingsMenu(settingsController);
        this.shopMenu = new ShopMenu(shopController);
        this.signupMenu = new SignupMenu(signupController);
        this.travellogMenu = new TravelLogMenu(travellogController);
    }

    public static MenuHub getInstance(App app) {
        if (instance == null) instance = new MenuHub(app);
        return instance;
    }

    public AppMenu getCurrentMenu() {
        switch (currentMenu) {
            case COLLECTION:
                return collectionMenu;
            case GAME:
                return gameMenu;
            case GREENHOUSE:
                return greenhouseMenu;
            case LEADERBOARD:
                return leaderboardMenu;
            case LOGIN:
                return loginMenu;
            case MAIN:
                return mainMenu;
            case NEWS:
                return newsMenu;
            case PROFILE:
                return profileMenu;
            case SETTINGS:
                return settingsMenu;
            case SHOP:
                return shopMenu;
            case SIGNUP:
                return signupMenu;
            case TRAVELLOG:
                return travellogMenu;
            default:
                return null;
        }
    }
    
    public void run() {
        while(true) {
            AppMenu menu = getCurrentMenu();

            String command = scanner.nextLine().trim();

            boolean validCommand = menu.processCommand(command);

            if(!validCommand) {
                // TODO: what should we do if it wasn't a valid message?
            }
        }
    }
}
