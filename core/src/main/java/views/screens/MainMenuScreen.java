package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import controllers.menuControllers.MainController;
import utils.NewsStore;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.MenuButton;
import views.ui.Ui;

public class MainMenuScreen extends BaseScreen {

    private final MainController controller;
    private views.ui.MenuCast cast;
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
        body.add(logo).height(116f).padTop(2f).padBottom(0f).row();

        Table top = new Table();
        top.add(button("image_ui_generic_button_hud_minigames", "Adventure", ScreenId.ADVENTURE));
        top.add(button("image_ui_generic_buttons_hud_quests", "Quests", ScreenId.QUESTS));
        top.add(button("image_ui_hud_almanacbutton_buttons_hud_almanac",
                "Almanac", ScreenId.COLLECTION));
        top.add(button("image_ui_generic_buttons_hud_zg", "Greenhouse", ScreenId.GREENHOUSE));
        top.add(button("image_ui_hud_eventshop_buttons_hud_event_shop", "Shop", ScreenId.SHOP));
        top.add(button("image_ui_generic_content_well", "Versus", ScreenId.MULTIPLAYER)
                .withIcon(art.zombie(models.entities.zombie.ZombieType.NORMAL)));
        body.add(top).padTop(2f).row();

        Table lower = new Table();
        lower.add(newsButton());
        lower.add(button("image_ui_generic_content_well", "Leaderboard", ScreenId.LEADERBOARD)
                .withIcon(art.ui("image_ui_generic_star_icon")));
        lower.add(button("image_ui_hud_plantboost_buttons_hud_plant_boost",
                "Profile", ScreenId.PROFILE));
        lower.add(button("image_ui_hud_settingsbutton_buttons_hud_settings",
                "Settings", ScreenId.SETTINGS));
        if (models.settings.GamePreferences.isDebugMode(app.getCurrentUser() == null
                ? null : app.getCurrentUser().getUsername())) {
            lower.add(button("image_ui_generic_content_well", "Sandbox",
                    ScreenId.SANDBOX_SETUP)
                    .withIcon(art.ui("image_ui_hud_ingame_shovel_icon")));
        }
        body.add(lower).padTop(2f).row();

        Table strip = new Table();
        strip.add(Ui.button(skin, "Unlock everything", "blue", () -> {
            toasts.show(controller.handleCheatUnlockAll());
            topBar().refresh();
        })).width(250f).height(52f).pad(4f);
        strip.add(Ui.button(skin, "Log out", "brown", () -> {
            toasts.show(controller.handleLogout());
            router.go(ScreenId.SIGNUP);
        })).width(190f).height(52f).pad(4f);
        strip.add(Ui.button(skin, "Quit", "brown", views.ui.Display::quit))
                .width(150f).height(52f).pad(4f);
        body.add(strip).padTop(6f).row();
        body.add().expand().row();
        showCast();
    }

    private void showCast() {
        if (cast != null) {
            cast.remove();
        }
        cast = new views.ui.MenuCast(game.getAnimations());
        cast.setBounds(0f, 6f, stage.getViewport().getWorldWidth(), 124f);
        stage.addActor(cast);
    }

    private MenuButton button(String region, String text, final ScreenId target) {
        String face = art.uiOptional(region + "_normal") == null ? region : region + "_normal";
        MenuButton tile = new MenuButton(skin, art.ui(face),
                art.uiOptional(region + "_selected"), text, () -> router.go(target));
        Ui.appear(tile, order++);
        return tile;
    }

    private MenuButton newsButton() {
        int unread = app.getCurrentUser() == null ? 0
                : NewsStore.countUnread(app.getCurrentUser().getUsername());
        MenuButton tile = new MenuButton(skin,
                art.ui("image_ui_hud_tasklist_buttons_hud_task_list_normal"),
                art.uiOptional("image_ui_hud_tasklist_buttons_hud_task_list_selected"),
                "News", () -> router.go(ScreenId.NEWS));
        if (unread > 0) {
            tile.mark(skin, String.valueOf(unread));
        }
        Ui.appear(tile, order++);
        return tile;
    }
}
