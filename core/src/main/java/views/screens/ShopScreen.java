package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.MainController;
import controllers.menuControllers.ShopController;
import models.Result;
import models.entities.plant.PlantType;
import utils.UserDataStore;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Dialogs;
import views.ui.Ui;

import java.util.List;

public class ShopScreen extends BaseScreen {

    private final ShopController controller;
    private final Table list = new Table();
    private int rows;

    public ShopScreen(PvzGame game) {
        super(game);
        this.controller = new ShopController(game.getApp());
    }

    @Override
    protected String title() {
        return "Shop";
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.GREENHOUSE;
    }

    @Override
    protected void buildContent(Table body) {
        body.add(Ui.scroll(skin, list)).grow();
        refresh();
    }

    private void refresh() {
        list.clear();
        rows = 0;
        item("1", "Greenhouse Pot", "Unlocks one greenhouse pot.",
                "2000 coins", "image_ui_generic_leaf_backdrop", null);
        item("2", "Plant Food", "Adds one plant food to your next level (max 3).",
                "3 diamonds", "image_ui_hud_ingame_gem", null);
        item("3", "Random Seed Bundle", "5 seed packets of a random unlocked plant.",
                "1000 coins", "image_ui_generic_coin_icon_small", null);
        item("4", "Choice Seed Bundle", "10 seed packets of a plant you pick.",
                "5 diamonds", "image_ui_generic_gem_icon_small", null);
        item("5", "Currency Exchange", "Trades 5 diamonds for 500 coins.",
                "5 diamonds", "image_ui_generic_gem_icon_small", null);

        Result daily = controller.handleShopDaily();
        String dailyText = daily.getMessages().size() > 1 ? daily.getMessages().get(1) : "Daily offer";
        item("6", "Daily Offer", dailyText, "1600 coins", "image_ui_generic_star_icon", null);
    }

    private void item(final String id, String name, String description, String price,
                      String iconRegion, PlantType preview) {
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable("row"));
        card.pad(12f, 20f, 12f, 20f);
        card.add(Ui.iconCell(preview == null ? art.ui(iconRegion) : art.plant(preview), 56f))
                .padRight(18f);

        Table text = new Table();
        text.add(Ui.label(skin, name, "h2")).left().row();
        text.add(Ui.wrapped(skin, description, "muted")).width(640f).left().padTop(4f);
        card.add(text).growX();

        boolean gems = price.contains("diamond") || price.contains("gem");
        String amount = price.split(" ")[0];
        card.add(Ui.pill(skin, art.ui(gems ? "image_ui_generic_gem_icon_small"
                : "image_ui_generic_coin_icon_small"), amount, "gold")).padRight(18f).right();
        card.add(Ui.button(skin, "Buy", gems ? "small-purple" : "small",
                () -> startPurchase(id, name, price))).width(140f).height(50f).right();

        Ui.appear(card, rows);
        list.add(card).growX().height(92f).padBottom(9f).row();
        rows++;
    }

    private void startPurchase(final String id, String name, String price) {
        if ("4".equals(id)) {
            List<String> unlocked = MainController.unlockedPlants(
                    UserDataStore.forUser(app.getCurrentUser().getUsername()));
            Dialogs.choosePlant(stage, skin, art, unlocked, plant ->
                    Dialogs.confirm(stage, skin, "Confirm purchase",
                            "Buy 10 seed packets of " + plant + " for " + price + "?",
                            () -> complete(id, plant)));
            return;
        }
        Dialogs.confirm(stage, skin, "Confirm purchase",
                "Buy " + name + " for " + price + "?", () -> complete(id, null));
    }

    private void complete(String id, String plantType) {
        Result result = controller.handleShopBuy(id, 1, plantType);
        toasts.show(result);
        topBar().refresh();
        refresh();
    }
}
