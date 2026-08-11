package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.MainController;
import utils.NewsStore;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Palette;
import views.ui.Ui;

public class MainMenuScreen extends BaseScreen {

    private final MainController controller;

    public MainMenuScreen(PvzGame game) {
        super(game);
        this.controller = new MainController(game.getApp());
    }

    @Override
    protected String title() {
        return "Plants vs. Zombies";
    }

    @Override
    protected boolean showsHeader() {
        return false;
    }

    @Override
    protected ScreenId backTarget() {
        return null;
    }

    @Override
    protected void buildContent(Table body) {
        Table grid = new Table();
        grid.defaults().width(300f).height(74f).pad(9f);

        grid.add(tile("Adventure", ScreenId.ADVENTURE));
        grid.add(tile("Quests & Minigames", ScreenId.QUESTS));
        grid.add(tile("Collection", ScreenId.COLLECTION)).row();

        grid.add(tile("Greenhouse", ScreenId.GREENHOUSE));
        grid.add(tile("Shop", ScreenId.SHOP));
        grid.add(newsTile()).row();

        grid.add(tile("Leaderboard", ScreenId.LEADERBOARD));
        grid.add(tile("Profile", ScreenId.PROFILE));
        grid.add(tile("Settings", ScreenId.SETTINGS)).row();

        Table lower = new Table();
        lower.add(Ui.button(skin, "Scoring Game", "purple",
                () -> toasts.error("The battle screen is not part of this build yet.")))
                .width(300f).height(66f).pad(9f);
        lower.add(Ui.button(skin, "Unlock everything", "blue", () -> {
            toasts.show(controller.handleCheatUnlockAll());
            topBar().refresh();
        })).width(300f).height(66f).pad(9f);
        lower.add(Ui.button(skin, "Log out", "brown", () -> {
            toasts.show(controller.handleLogout());
            router.go(ScreenId.SIGNUP);
        })).width(300f).height(66f).pad(9f);

        body.add(grid).expand().center().row();
        body.add(lower).padTop(6f);
    }

    private Table tile(String text, final ScreenId target) {
        Table wrapper = new Table();
        wrapper.add(Ui.button(skin, text, () -> router.go(target))).grow();
        return wrapper;
    }

    private Table newsTile() {
        int unread = app.getCurrentUser() == null ? 0
                : NewsStore.countUnread(app.getCurrentUser().getUsername());
        Table wrapper = new Table();
        wrapper.add(Ui.button(skin, unread > 0 ? "News  !" : "News",
                unread > 0 ? "purple" : "default", () -> router.go(ScreenId.NEWS))).grow().row();
        if (unread > 0) {
            Label badge = new Label(unread + " unread", skin, "small");
            badge.setColor(Palette.GOLD);
            wrapper.add(badge).padTop(2f);
        }
        return wrapper;
    }
}
