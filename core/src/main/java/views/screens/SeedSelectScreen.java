package views.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.CollectionController;
import controllers.menuControllers.GameController;
import models.Result;
import models.entities.plant.PlantCategory;
import models.entities.plant.PlantType;
import models.game.GameSession;
import models.game.Names;
import models.game.PlantSlot;
import utils.UserDataStore;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Palette;
import views.ui.PlantCard;
import views.ui.SeedPacket;
import views.ui.Ui;

import java.util.ArrayList;
import java.util.List;

public class SeedSelectScreen extends BaseScreen {

    private static final int MAX_SLOTS = 8;
    private static final int COLUMNS = 5;
    private static final int BOOST_COST = 2;

    private final GameController controller;
    private final CollectionController collection;
    private final Table slotRow = new Table();
    private final Table pickerGrid = new Table();
    private final Table detailPane = new Table();

    private SelectBox<String> familyFilter;
    private PlantType focused;

    public SeedSelectScreen(PvzGame game) {
        super(game);
        this.controller = new GameController(game.getApp());
        this.collection = new CollectionController(game.getApp());
    }

    @Override
    protected String title() {
        return "Choose Your Plants";
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.ADVENTURE;
    }

    @Override
    protected TextureRegion background() {
        GameSession session = app.getCurrentGameSession();
        String chapter = session == null || session.getLevel() == null ? null
                : session.getLevel().getChapter().getName();
        return art.chapterBackground(chapter);
    }

    @Override
    protected void buildContent(Table body) {
        if (app.getCurrentGameSession() == null) {
            body.add(Ui.label(skin, "No level is loaded.", "bad")).expand();
            return;
        }

        familyFilter = new SelectBox<>(skin);
        List<String> families = new ArrayList<>();
        families.add("All families");
        for (PlantCategory category : PlantCategory.values()) {
            families.add(category.name().replace('_', ' '));
        }
        familyFilter.setItems(families.toArray(new String[0]));
        familyFilter.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                buildPicker();
            }
        });

        Table topStrip = new Table();
        topStrip.add(Ui.label(skin, "Your line-up", "h2")).left().padRight(14f);
        topStrip.add(slotRow).left().expandX();
        topStrip.add(Ui.label(skin, "Family", "muted")).padRight(8f);
        topStrip.add(familyFilter).width(220f).height(44f);
        body.add(topStrip).growX().padBottom(10f).row();

        Table columns = new Table();
        columns.add(Ui.scroll(skin, pickerGrid)).width(820f).grow().padRight(16f);
        columns.add(Ui.scroll(skin, detailPane)).width(350f).grow();
        body.add(columns).grow().row();

        Table footer = new Table();
        footer.add(Ui.label(skin, hint(), "muted")).left().expandX();
        footer.add(Ui.button(skin, "Let's Rock!", "green", this::startBattle)).width(260f).height(62f);
        body.add(footer).growX().height(74f).padTop(8f);

        refresh();
    }

    private String hint() {
        return "Pick up to " + MAX_SLOTS + " plants. Click a card to add or remove it.";
    }

    private GameSession session() {
        return app.getCurrentGameSession();
    }

    private UserDataStore store() {
        return UserDataStore.forUser(app.getCurrentUser().getUsername());
    }

    private void refresh() {
        buildSlots();
        buildPicker();
        showDetail(focused);
    }

    private void buildSlots() {
        slotRow.clear();
        List<PlantSlot> slots = session().getSlots();
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (i < slots.size()) {
                final PlantSlot slot = slots.get(i);
                SeedPacket packet = new SeedPacket(skin, art, slot.getType())
                        .cost(session().effectiveCost(slot.getType()))
                        .boosted(slot.isBoosted())
                        .onClick(() -> {
                            toasts.show(controller.handleRemovePlant(slot.getType().getName()));
                            refresh();
                        });
                Ui.hoverLift(packet, 1.05f);
                slotRow.add(packet).size(SeedPacket.PACKET_WIDTH, SeedPacket.PACKET_HEIGHT).padRight(6f);
            } else {
                Table empty = new Table(skin);
                empty.setBackground(skin.getDrawable("slot"));
                empty.add(Ui.label(skin, "empty", "muted"));
                slotRow.add(empty).size(SeedPacket.PACKET_WIDTH, SeedPacket.PACKET_HEIGHT).padRight(6f);
            }
        }
    }

    private void buildPicker() {
        pickerGrid.clear();
        UserDataStore store = store();
        int column = 0;
        for (String name : session().getSelection().getUnlockedPlantNames()) {
            final PlantType type = Names.plant(name);
            if (type == null || !matchesFamily(type)) {
                continue;
            }
            boolean selected = session().findSlot(type) != null;
            PlantCard card = new PlantCard(skin, art, type)
                    .level(store.getInt("level." + type.getName(), 1))
                    .cost(session().effectiveCost(type))
                    .selected(selected)
                    .boosted(selected && session().findSlot(type).isBoosted());
            if (selected) {
                card.note("in your line-up", Palette.GOOD);
            }
            card.onClick(() -> {
                focused = type;
                toggle(type);
            });
            Ui.hoverLift(card, 1.05f);
            Ui.appear(card, column);
            pickerGrid.add(card).size(PlantCard.CARD_WIDTH, PlantCard.CARD_HEIGHT).pad(6f);
            if (++column % COLUMNS == 0) {
                pickerGrid.row();
            }
        }
        if (column == 0) {
            pickerGrid.add(Ui.label(skin, "No plant matches this family.", "muted")).pad(30f);
        }
    }

    private boolean matchesFamily(PlantType type) {
        String family = familyFilter.getSelected();
        return family == null || family.startsWith("All")
                || type.getCategory().name().replace('_', ' ').equals(family);
    }

    private void toggle(PlantType type) {
        Result result = session().findSlot(type) == null
                ? controller.handleAddPlant(type.getName())
                : controller.handleRemovePlant(type.getName());
        toasts.show(result);
        refresh();
    }

    private void showDetail(PlantType type) {
        detailPane.clear();
        if (type == null) {
            detailPane.add(Ui.wrapped(skin, "Click a plant to see its details, boost it "
                    + "or spend seed packets on an upgrade.", "muted")).width(320f).pad(20f);
            return;
        }
        UserDataStore store = store();
        int level = store.getInt("level." + type.getName(), 1);
        PlantSlot slot = session().findSlot(type);

        Table panel = Ui.panel(skin);
        panel.add(Ui.iconCell(art.plant(type), 110f)).padBottom(6f).row();
        panel.add(Ui.label(skin, type.getName(), "h1")).padBottom(4f).row();
        panel.add(Ui.divider(skin, 290f)).padBottom(10f).row();

        stat(panel, art.statIcon("FAMILY"), "Family", type.getCategory().name().replace('_', ' '));
        stat(panel, art.statIcon("SUNCOST"), "Sun cost", String.valueOf(session().effectiveCost(type)));
        stat(panel, art.ui("image_ui_almanac_zombies_zombietoughness_icon"),
                "Health", String.valueOf(session().effectiveHp(type)));
        stat(panel, art.statIcon("ARMINGTIME"), "Recharge",
                session().effectiveRecharge(type) + "s");
        stat(panel, art.ui("image_ui_generic_star_icon"), "Level", level + " / 5");
        stat(panel, art.ui("image_ui_almanac_plant_select_pkt"), "Seed packets",
                String.valueOf(store.getInt("packets." + type.getName(), 0)));

        if (slot == null) {
            panel.add(Ui.label(skin, "Not in your line-up yet.", "muted")).padTop(6f).row();
        } else if (slot.isBoosted()) {
            panel.add(Ui.label(skin, "Boosted for this level.", "good")).padTop(6f).row();
        } else {
            panel.add(Ui.button(skin, "Boost (" + BOOST_COST + " gems)", "purple", () -> {
                toasts.show(controller.handleBoostPlant(type.getName()));
                topBar().refresh();
                refresh();
            })).width(280f).height(52f).padTop(10f).row();
        }

        panel.add(Ui.button(skin, "Upgrade plant", "small", () -> {
            toasts.show(collection.handleUpgradePlant(type.getName()));
            topBar().refresh();
            refresh();
        })).width(280f).height(48f).padTop(8f);

        detailPane.add(panel).growX();
    }

    private void stat(Table panel, TextureRegion icon, String key, String value) {
        Table row = new Table();
        row.add(Ui.iconCell(icon, 22f)).padRight(8f);
        row.add(Ui.label(skin, key, "muted")).left().expandX();
        row.add(Ui.label(skin, value, "small")).right();
        panel.add(row).growX().padBottom(6f).row();
    }

    private void startBattle() {
        Result result = controller.handleStartGame();
        if (!result.isSuccessfull()) {
            toasts.show(result);
            return;
        }
        router.go(ScreenId.BATTLE);
    }
}
