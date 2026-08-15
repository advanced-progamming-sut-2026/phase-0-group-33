package views;

import com.badlogic.gdx.Screen;
import models.enums.Menus;
import views.screens.AdventureScreen;
import views.screens.CollectionScreen;
import views.screens.GreenhouseScreen;
import views.screens.LeaderboardScreen;
import views.screens.LoginScreen;
import views.screens.MainMenuScreen;
import views.screens.NewsScreen;
import views.screens.ProfileScreen;
import views.screens.QuestScreen;
import views.screens.BattleScreen;
import views.screens.SeedSelectScreen;
import views.screens.SettingsScreen;
import views.screens.ShopScreen;
import views.screens.SignupScreen;

public final class Router {

    private final PvzGame game;

    public Router(PvzGame game) {
        this.game = game;
    }

    public void go(ScreenId id) {
        Screen previous = game.getScreen();
        if (game.getAudio() != null) {
            game.getAudio().setUser(game.getApp().getCurrentUser() == null ? null
                    : game.getApp().getCurrentUser().getUsername());
            game.getAudio().playMusic(id == ScreenId.BATTLE
                    ? views.assets.Audio.BATTLE : views.assets.Audio.MENU);
        }
        game.setScreen(create(id));
        if (previous != null) {
            previous.dispose();
        }
    }

    public void go(Menus menu) {
        go(ScreenId.from(menu));
    }

    public void syncWithApp() {
        go(game.getApp().getCurrentMenu());
    }

    private Screen create(ScreenId id) {
        switch (id) {
            case SIGNUP:
                return new SignupScreen(game);
            case LOGIN:
                return new LoginScreen(game);
            case PROFILE:
                return new ProfileScreen(game);
            case ADVENTURE:
                return new AdventureScreen(game);
            case COLLECTION:
                return new CollectionScreen(game);
            case GREENHOUSE:
                return new GreenhouseScreen(game);
            case SHOP:
                return new ShopScreen(game);
            case NEWS:
                return new NewsScreen(game);
            case LEADERBOARD:
                return new LeaderboardScreen(game);
            case SETTINGS:
                return new SettingsScreen(game);
            case QUESTS:
                return new QuestScreen(game);
            case SEED_SELECT:
                return new SeedSelectScreen(game);
            case BATTLE:
                return new BattleScreen(game);
            default:
                return new MainMenuScreen(game);
        }
    }
}
