package views.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.ZenGardenController;
import models.Result;
import models.entities.plant.PlantType;
import models.game.Names;
import views.PvzGame;
import views.ScreenId;
import views.ui.AnimatedActor;
import views.ui.BaseScreen;
import views.ui.Ui;

public class ZenGardenScreen extends BaseScreen {

    private static final int COLUMNS = 3;

    private final ZenGardenController controller;
    private final Table beds = new Table();
    private views.battle.Overlay picker;

    public ZenGardenScreen(PvzGame game) {
        super(game);
        this.controller = new ZenGardenController(game.getApp());
    }

    @Override
    protected String title() {
        return "Zen Garden";
    }

    @Override
    protected TextureRegion background() {
        return art.gardenBackground();
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.MAIN;
    }

    @Override
    protected void buildContent(Table body) {
        Table header = new Table();
        header.add(Ui.wrapped(skin, "Move a plant into a bed and water it whenever it gets"
                + " thirsty. Each watering pays " + ZenGardenController.WATER_REWARD
                + " coins, and every " + ZenGardenController.HAPPY_AT
                + "th watering pays a bonus.", "muted")).width(680f).left().expandX();
        header.add(Ui.button(skin, "Greenhouse", "blue", () -> router.go(ScreenId.GREENHOUSE)))
                .width(200f).height(50f);
        body.add(header).growX().padBottom(12f).row();
        body.add(Ui.scroll(skin, beds)).grow();
        refresh();
    }

    private void refresh() {
        beds.clear();
        for (int slot = 1; slot <= ZenGardenController.SLOTS; slot++) {
            Table cell = bed(slot);
            Ui.appear(cell, slot);
            beds.add(cell).size(300f, 210f).pad(8f);
            if (slot % COLUMNS == 0) {
                beds.row();
            }
        }
    }

    private Table bed(final int slot) {
        Table card = Ui.card(skin, "card");
        card.pad(10f);
        String plant = controller.plantAt(slot);
        if (plant == null) {
            return emptyBed(card, slot);
        }
        PlantType type = Names.plant(plant);
        AnimatedActor animated = type == null ? null
                : AnimatedActor.plant(game.getAnimations(), type, 96f);
        if (animated != null) {
            Table window = new Table();
            window.setClip(true);
            window.add(animated).size(96f);
            card.add(window).size(96f).padBottom(2f).row();
        } else {
            card.add(Ui.iconCell(art.plant(type), 88f)).padBottom(2f).row();
        }
        card.add(Ui.label(skin, plant, "h2")).row();
        boolean thirsty = controller.isThirsty(slot);
        card.add(Ui.label(skin, thirsty ? "Thirsty!" : waitText(slot),
                thirsty ? "good" : "muted")).padBottom(4f).row();
        Table buttons = new Table();
        buttons.add(Ui.button(skin, thirsty ? "Water" : "Not yet",
                thirsty ? "green" : "small", () -> {
                    toasts.show(controller.handleWater(slot));
                    topBar().refresh();
                    refresh();
                })).width(130f).height(44f).padRight(6f);
        buttons.add(Ui.button(skin, "Take back", "small-brown", () -> {
            toasts.show(controller.handleReturnPlant(slot));
            refresh();
        })).width(130f).height(44f);
        card.add(buttons);
        return card;
    }

    private String waitText(int slot) {
        long minutes = controller.millisUntilThirsty(slot) / 60000;
        return "Watered " + controller.wateringsOf(slot) + " times  -  thirsty in "
                + Math.max(1, minutes) + "m";
    }

    private Table emptyBed(Table card, final int slot) {
        card.add(Ui.label(skin, "Empty bed", "h2")).padBottom(6f).row();
        card.add(Ui.wrapped(skin, "Pick one of your plants to live here.", "muted"))
                .width(250f).padBottom(8f).row();
        card.add(Ui.button(skin, "Choose a plant", "green", () -> choose(slot)))
                .width(220f).height(48f);
        return card;
    }

    private void choose(final int slot) {
        Table list = new Table();
        for (final String name : controller.availablePlants()) {
            PlantType type = Names.plant(name);
            if (type == null) {
                continue;
            }
            Table row = Ui.card(skin, "row");
            row.pad(6f, 12f, 6f, 12f);
            row.add(Ui.iconCell(art.plant(type), 40f)).padRight(10f);
            row.add(Ui.label(skin, name, "small")).growX().left();
            Ui.hoverLift(row, 1.02f);
            Ui.onClick(row, () -> {
                Result result = controller.handlePlaceInGarden(name, slot);
                toasts.show(result);
                shut();
                refresh();
            });
            list.add(row).growX().padBottom(4f).row();
        }
        Table content = new Table();
        content.add(Ui.scroll(skin, list)).size(520f, 360f).row();
        content.add(Ui.button(skin, "Cancel", "brown", this::shut))
                .width(200f).height(48f).padTop(8f);
        shut();
        picker = views.battle.Overlay.open(stage, skin, "Choose a plant", content);
    }

    private void shut() {
        if (picker != null) {
            picker.close();
            picker = null;
        }
    }
}
