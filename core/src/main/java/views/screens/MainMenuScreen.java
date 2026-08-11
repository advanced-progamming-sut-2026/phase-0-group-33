package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import controllers.menuControllers.MainController;
import utils.NewsStore;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.MenuTile;
import views.ui.Ui;

public class MainMenuScreen extends BaseScreen {

    private final MainController controller;
    private int order;

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
        body.clearChildren();

        Image logo = new Image(art.logo());
        logo.setScaling(Scaling.fit);
        logo.setOrigin(com.badlogic.gdx.utils.Align.center);
        logo.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1.03f, 1.03f, 1.9f,
                                com.badlogic.gdx.math.Interpolation.sine),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 1.9f,
                                com.badlogic.gdx.math.Interpolation.sine))));
        body.add(logo).height(126f).padTop(4f).padBottom(10f).row();

        Table grid = new Table();
        grid.defaults().size(MenuTile.TILE_WIDTH, MenuTile.TILE_HEIGHT).pad(7f);

        grid.add(tile("image_ui_generic_button_hud_minigames_normal", "Adventure", ScreenId.ADVENTURE));
        grid.add(tile("image_ui_generic_buttons_hud_quests_normal", "Quests", ScreenId.QUESTS));
        grid.add(tile("image_ui_hud_almanacbutton_buttons_hud_almanac_normal",
                "Collection", ScreenId.COLLECTION)).row();

        grid.add(tile("image_ui_generic_buttons_hud_zg_normal", "Greenhouse", ScreenId.GREENHOUSE));
        grid.add(tile("image_ui_hud_eventshop_buttons_hud_event_shop_normal", "Shop", ScreenId.SHOP));
        grid.add(newsTile()).row();

        grid.add(tile("image_ui_generic_star_icon", "Leaderboard", ScreenId.LEADERBOARD));
        grid.add(tile("image_ui_hud_plantboost_buttons_hud_plant_boost_normal",
                "Profile", ScreenId.PROFILE));
        grid.add(tile("image_ui_hud_settingsbutton_buttons_hud_settings_normal",
                "Settings", ScreenId.SETTINGS)).row();

        body.add(grid).expand().center().row();

        Table lower = new Table();
        lower.add(Ui.button(skin, "Scoring Game", "purple",
                        () -> toasts.error("The battle screen is not part of this build yet.")))
                .width(268f).height(60f).pad(7f);
        lower.add(Ui.button(skin, "Unlock everything", "blue", () -> {
            toasts.show(controller.handleCheatUnlockAll());
            topBar().refresh();
        })).width(268f).height(60f).pad(7f);
        lower.add(Ui.button(skin, "Log out", "brown", () -> {
            toasts.show(controller.handleLogout());
            router.go(ScreenId.SIGNUP);
        })).width(268f).height(60f).pad(7f);
        body.add(lower).padBottom(4f);
    }

    private MenuTile tile(String iconRegion, String text, final ScreenId target) {
        MenuTile tile = new MenuTile(skin, art.ui(iconRegion), text, "card",
                () -> router.go(target));
        Ui.appear(tile, order++);
        return tile;
    }

    private MenuTile newsTile() {
        int unread = app.getCurrentUser() == null ? 0
                : NewsStore.countUnread(app.getCurrentUser().getUsername());
        MenuTile tile = new MenuTile(skin, art.ui("image_ui_hud_tasklist_buttons_hud_task_list_normal"),
                "News", unread > 0 ? "card-epic" : "card", () -> router.go(ScreenId.NEWS));
        if (unread > 0) {
            tile.mark(skin, String.valueOf(unread));
        }
        Ui.appear(tile, order++);
        return tile;
    }
}
