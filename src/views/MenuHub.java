package views;

import controllers.menuControllers.CollectionController;
import controllers.menuControllers.GameController;
import controllers.menuControllers.GreenhouseController;
import controllers.menuControllers.LeaderboardController;
import controllers.menuControllers.LoginController;
import controllers.menuControllers.MainController;
import controllers.menuControllers.NewsController;
import controllers.menuControllers.ProfileController;
import controllers.menuControllers.SettingsController;
import controllers.menuControllers.ShopController;
import controllers.menuControllers.SignupController;
import controllers.menuControllers.TravelLogController;
import models.App;
import models.enums.Menus;
import views.menus.AppMenu;
import views.menus.CollectionMenu;
import views.menus.GameMenu;
import views.menus.GreenhouseMenu;
import views.menus.LeaderboardMenu;
import views.menus.LoginMenu;
import views.menus.MainMenu;
import views.menus.NewsMenu;
import views.menus.ProfileMenu;
import views.menus.SettingsMenu;
import views.menus.ShopMenu;
import views.menus.SignupMenu;
import views.menus.TravelLogMenu;

import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Owns the REPL loop: reads a line, routes it to the menu the {@link App}
 * currently points at, and prints an error for unrecognized commands.
 */
public class MenuHub {
    private static MenuHub instance;

    private final App app;
    private final Scanner scanner;
    private final Map<Menus, AppMenu> menus = new EnumMap<>(Menus.class);

    private MenuHub(App app) {
        this.app = app;
        this.scanner = new Scanner(System.in);

        menus.put(Menus.COLLECTION, new CollectionMenu(new CollectionController(app)));
        menus.put(Menus.GAME, new GameMenu(new GameController(app)));
        menus.put(Menus.GREENHOUSE, new GreenhouseMenu(new GreenhouseController(app)));
        menus.put(Menus.LEADERBOARD, new LeaderboardMenu(new LeaderboardController(app)));
        menus.put(Menus.LOGIN, new LoginMenu(new LoginController(app)));
        menus.put(Menus.MAIN, new MainMenu(new MainController(app)));
        menus.put(Menus.NEWS, new NewsMenu(new NewsController(app)));
        menus.put(Menus.PROFILE, new ProfileMenu(new ProfileController(app)));
        menus.put(Menus.SETTINGS, new SettingsMenu(new SettingsController(app)));
        menus.put(Menus.SHOP, new ShopMenu(new ShopController(app)));
        menus.put(Menus.SIGNUP, new SignupMenu(new SignupController(app)));
        menus.put(Menus.TRAVELLOG, new TravelLogMenu(new TravelLogController(app)));
    }

    public static MenuHub getInstance(App app) {
        if (instance == null) {
            instance = new MenuHub(app);
        }
        return instance;
    }

    public AppMenu getCurrentMenu() {
        return menus.get(app.getCurrentMenu());
    }

    public void run() {
        while (!app.isExitRequested() && scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (command.isEmpty()) {
                continue;
            }
            AppMenu menu = getCurrentMenu();
            boolean validCommand = menu.processCommand(command);
            if (!validCommand) {
                System.out.println("Invalid command!");
            }
        }
    }
}
