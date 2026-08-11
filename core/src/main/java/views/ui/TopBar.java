package views.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.managers.UserManager;
import models.App;
import models.settings.GamePreferences;
import models.user.User;
import views.assets.Art;

public final class TopBar extends Table {

    private static final int COIN_STEP = 1000;
    private static final int GEM_STEP = 25;

    private final App app;
    private final Label nickname;
    private final Label coins;
    private final Label diamonds;
    private final Table cheats = new Table();

    public TopBar(App app, Skin skin, Art art, Toasts toasts) {
        this.app = app;
        setBackground(skin.getDrawable("shade"));
        pad(8f, 20f, 8f, 20f);

        nickname = Ui.label(skin, "", "h2");
        coins = Ui.label(skin, "0", "gold");
        diamonds = Ui.label(skin, "0", "gold");

        add(nickname).left();
        add().expandX();
        add(cheats).right().padRight(18f);
        add(Ui.iconCell(art.ui("image_ui_generic_coin_icon_small"), 28f)).padRight(6f);
        add(coins).padRight(22f);
        add(Ui.iconCell(art.ui("image_ui_generic_gem_icon_small"), 28f)).padRight(6f);
        add(diamonds);

        cheats.add(Ui.button(skin, "+coins", "small-brown", () -> {
            toasts.show(UserManager.getInstance().addCoins(COIN_STEP));
            refresh();
        })).padRight(8f).height(40f);
        cheats.add(Ui.button(skin, "+gems", "small-brown", () -> {
            toasts.show(UserManager.getInstance().addDiamonds(GEM_STEP));
            refresh();
        })).height(40f);

        refresh();
    }

    public void refresh() {
        User user = app.getCurrentUser();
        if (user == null) {
            nickname.setText("");
            coins.setText("0");
            diamonds.setText("0");
            cheats.setVisible(false);
            return;
        }
        nickname.setText(user.getNickname() == null ? user.getUsername() : user.getNickname());
        coins.setText(String.valueOf(user.getCoins().getAmount()));
        diamonds.setText(String.valueOf(user.getDiamonds().getAmount()));
        cheats.setVisible(GamePreferences.isDebugMode(user.getUsername()));
    }
}
