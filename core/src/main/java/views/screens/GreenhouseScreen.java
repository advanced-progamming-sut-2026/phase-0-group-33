package views.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.GreenhouseController;
import models.Result;
import models.entities.plant.PlantType;
import models.game.Names;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Palette;
import views.ui.Ui;

public class GreenhouseScreen extends BaseScreen {

    private final GreenhouseController controller;
    private final Table grid = new Table();

    public GreenhouseScreen(PvzGame game) {
        super(game);
        this.controller = new GreenhouseController(game.getApp());
    }

    @Override
    protected String title() {
        return "Greenhouse";
    }

    @Override
    protected TextureRegion background() {
        return art.gardenBackground();
    }

    @Override
    protected void buildContent(Table body) {
        Table header = new Table();
        header.add(Ui.label(skin, "Marigold grows in 2h and pays 500 coins. "
                + "Any other plant grows in 8h and stores a boost.", "muted")).left().expandX();
        header.add(Ui.button(skin, "Open shop", "blue", () -> router.go(ScreenId.SHOP)))
                .width(200f).height(50f);
        body.add(header).growX().padBottom(12f).row();
        body.add(Ui.scroll(skin, grid)).grow();
        refresh();
    }

    private void refresh() {
        grid.clear();
        for (int y = 1; y <= GreenhouseController.ROWS; y++) {
            for (int x = 1; x <= GreenhouseController.COLS; x++) {
                Table cell = pot(x, y);
                Ui.appear(cell, (y - 1) * GreenhouseController.COLS + x);
                grid.add(cell).size(238f, 150f).pad(6f);
            }
            grid.row();
        }
    }

    private Table pot(final int x, final int y) {
        Table card = Ui.card(skin, "card");
        card.pad(8f);
        String plant = controller.potPlant(x, y);
        if (plant != null && controller.isPotUnlocked(x, y)) {
            return growingPot(card, x, y, plant);
        }
        if (!controller.isPotUnlocked(x, y)) {
            card.add(Ui.iconCell(art.ui("image_ui_lock_small"), 34f)).padBottom(2f).row();
            card.add(Ui.label(skin, "Locked", "muted")).row();
            card.add(Ui.button(skin, "Buy a pot", "small-brown", () -> router.go(ScreenId.SHOP)))
                    .width(158f).height(40f).padTop(6f);
            card.setColor(Palette.LOCKED);
            return card;
        }

        card.add(Ui.iconCell(art.ui("image_ui_generic_leaf_backdrop"), 40f)).padBottom(2f).row();
        card.add(Ui.label(skin, "Empty pot", "muted")).row();
        card.add(Ui.button(skin, "Plant", "small", () -> {
            Result result = controller.handlePlantPot(x, y);
            toasts.show(result);
            refresh();
        })).width(158f).height(40f).padTop(6f);
        return card;
    }

    private Table growingPot(Table card, final int x, final int y, String plant) {
        PlantType type = Names.plant(plant);
        views.ui.AnimatedActor animated = type == null ? null
                : views.ui.AnimatedActor.plant(game.getAnimations(), type, 54f);
        if (animated == null) {
            card.add(Ui.iconCell(type == null
                    ? art.ui("image_ui_generic_leaf_backdrop") : art.plant(type), 46f))
                    .padBottom(2f).row();
        } else {
            com.badlogic.gdx.scenes.scene2d.ui.Container<views.ui.AnimatedActor> holder =
                    new com.badlogic.gdx.scenes.scene2d.ui.Container<>(animated);
            holder.size(54f);
            card.add(holder).padBottom(2f).row();
        }
        card.add(Ui.label(skin, plant, "small")).row();

        long remaining = controller.potRemainingMillis(x, y);
        if (remaining <= 0) {
            card.add(Ui.label(skin, "Ready", "good")).padTop(2f).row();
            card.add(Ui.button(skin, "Collect", "small", () -> {
                Result result = controller.handleCollect(x, y);
                toasts.show(result);
                topBar().refresh();
                refresh();
            })).width(168f).height(40f).padTop(4f);
            return card;
        }

        long minutes = remaining / 60000 + 1;
        long totalMinutes = "Marigold".equals(plant) ? 120L : 480L;
        com.badlogic.gdx.scenes.scene2d.ui.ProgressBar growth =
                new com.badlogic.gdx.scenes.scene2d.ui.ProgressBar(
                        0f, totalMinutes, 1f, false, skin, "gold-horizontal");
        growth.setValue(Math.max(0L, totalMinutes - minutes));
        card.add(growth).width(190f).height(12f).padTop(2f).row();
        card.add(Ui.label(skin, minutes / 60 + "h " + minutes % 60 + "m left", "muted")).padTop(1f).row();
        int cost = controller.potSpeedUpCost(x, y);
        card.add(Ui.button(skin, "Grow (" + cost + " gems)", "small-brown", () -> {
            Result result = controller.handleGrow(x, y);
            toasts.show(result);
            topBar().refresh();
            refresh();
        })).width(190f).height(40f).padTop(4f);
        card.setColor(Palette.TEXT);
        return card;
    }
}
