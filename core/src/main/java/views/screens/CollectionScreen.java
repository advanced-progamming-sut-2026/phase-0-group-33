package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.CollectionController;
import controllers.menuControllers.MainController;
import models.Result;
import models.entities.plant.PlantCategory;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import utils.UserDataStore;
import views.PvzGame;
import views.ui.BaseScreen;
import views.ui.PlantCard;
import views.ui.Ui;
import views.ui.ZombieCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CollectionScreen extends BaseScreen {

    private static final int MAX_LEVEL = 5;
    private static final int PACKETS_PER_LEVEL = 5;
    private static final int COINS_PER_LEVEL = 1000;
    private static final int PURCHASE_COST = 2000;
    private static final int COLUMNS = 5;

    private final CollectionController controller;
    private final Table gridPane = new Table();
    private final Table detailPane = new Table();

    private boolean plantsTab = true;
    private SelectBox<String> familyFilter;
    private SelectBox<String> stateFilter;

    public CollectionScreen(PvzGame game) {
        super(game);
        this.controller = new CollectionController(game.getApp());
    }

    @Override
    protected String title() {
        return "Collection";
    }

    @Override
    protected void buildContent(Table body) {
        familyFilter = new SelectBox<>(skin);
        List<String> families = new ArrayList<>();
        families.add("All families");
        for (PlantCategory category : PlantCategory.values()) {
            families.add(category.name().replace('_', ' '));
        }
        familyFilter.setItems(families.toArray(new String[0]));

        stateFilter = new SelectBox<>(skin);
        stateFilter.setItems("All", "Unlocked", "Locked", "Upgradable");

        com.badlogic.gdx.scenes.scene2d.utils.ChangeListener listener =
                new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                        refresh();
                    }
                };
        familyFilter.addListener(listener);
        stateFilter.addListener(listener);

        Table tabs = new Table();
        tabs.add(Ui.tabs(skin, new String[]{"Plants", "Zombies"}, 0, index -> {
            plantsTab = index == 0;
            refresh();
        })).left();
        tabs.add().expandX();
        tabs.add(Ui.label(skin, "Family", "muted")).padRight(8f);
        tabs.add(familyFilter).width(230f).height(46f).padRight(16f);
        tabs.add(Ui.label(skin, "Show", "muted")).padRight(8f);
        tabs.add(stateFilter).width(190f).height(46f);

        body.add(tabs).growX().padBottom(12f).row();

        Table columns = new Table();
        columns.add(Ui.scroll(skin, gridPane)).width(800f).grow().padRight(16f);
        columns.add(Ui.scroll(skin, detailPane)).width(370f).grow();
        body.add(columns).grow();

        refresh();
    }

    private void refresh() {
        familyFilter.setDisabled(!plantsTab);
        stateFilter.setDisabled(!plantsTab);
        gridPane.clear();
        detailPane.clear();
        if (plantsTab) {
            buildPlantGrid();
            showPlantDetail(PlantType.values()[0]);
        } else {
            buildZombieGrid();
            showZombieDetail(ZombieType.values()[0]);
        }
    }

    private UserDataStore store() {
        return UserDataStore.forUser(app.getCurrentUser().getUsername());
    }

    private void buildPlantGrid() {
        UserDataStore store = store();
        List<String> unlocked = MainController.unlockedPlants(store);
        int column = 0;
        for (PlantType type : PlantType.values()) {
            boolean owned = unlocked.contains(type.getName());
            int level = store.getInt("level." + type.getName(), 1);
            int packets = store.getInt("packets." + type.getName(), 0);
            int needed = PACKETS_PER_LEVEL * level;
            boolean upgradable = owned && level < MAX_LEVEL && packets >= needed;

            if (!matchesFilters(type, owned, upgradable)) {
                continue;
            }

            PlantCard card = new PlantCard(skin, art, type);
            if (owned) {
                card.level(level).packets(packets, needed);
            } else {
                card.locked(true).note(PURCHASE_COST + " coins", views.ui.Palette.MUTED);
            }
            card.onClick(() -> showPlantDetail(type));
            Ui.hoverLift(card, 1.05f);
            Ui.appear(card, column);
            gridPane.add(card).size(PlantCard.CARD_WIDTH, PlantCard.CARD_HEIGHT).pad(7f);
            if (++column % COLUMNS == 0) {
                gridPane.row();
            }
        }
        if (column == 0) {
            gridPane.add(Ui.label(skin, "No plant matches these filters.", "muted")).pad(30f);
        }
    }

    private boolean matchesFilters(PlantType type, boolean owned, boolean upgradable) {
        String family = familyFilter.getSelected();
        if (family != null && !family.startsWith("All")
                && !type.getCategory().name().replace('_', ' ').equals(family)) {
            return false;
        }
        String state = stateFilter.getSelected();
        if ("Unlocked".equals(state)) {
            return owned;
        }
        if ("Locked".equals(state)) {
            return !owned;
        }
        if ("Upgradable".equals(state)) {
            return upgradable;
        }
        return true;
    }

    private void showPlantDetail(PlantType type) {
        detailPane.clear();
        UserDataStore store = store();
        List<String> unlocked = MainController.unlockedPlants(store);
        boolean owned = unlocked.contains(type.getName());
        int level = store.getInt("level." + type.getName(), 1);
        int packets = store.getInt("packets." + type.getName(), 0);

        Table panel = Ui.panel(skin);
        panel.add(Ui.iconCell(art.plant(type), 120f)).padBottom(6f).row();
        panel.add(Ui.label(skin, type.getName(), "h1")).padBottom(4f).row();
        panel.add(Ui.divider(skin, 300f)).padBottom(10f).row();

        stat(panel, art.statIcon("FAMILY"), "Family", type.getCategory().name().replace('_', ' '));
        stat(panel, art.ui("image_ui_almanac_zombies_zombietoughness_icon"),
                "Health", String.valueOf(type.getBaseHp()));
        stat(panel, art.statIcon("SUNCOST"), "Sun cost", String.valueOf(type.getCost()));
        stat(panel, art.statIcon("SPECIAL"), "Damage",
                type.isInstantKill() ? "instant kill" : String.valueOf(type.getDamage()));
        stat(panel, art.statIcon("ARMINGTIME"), "Action every", type.getActionInterval() + "s");
        stat(panel, art.statIcon("ARMINGTIME"), "Recharge", type.getRecharge() + "s");
        stat(panel, art.statIcon("PLANTFOOD"), "Tags", tagText(type));
        stat(panel, art.ui("image_ui_generic_star_icon"), "Level",
                owned ? level + " / " + MAX_LEVEL : "locked");
        stat(panel, art.ui("image_ui_almanac_plant_select_pkt"), "Seed packets",
                owned ? String.valueOf(packets) : "-");

        if (owned) {
            panel.add(Ui.button(skin, "Upgrade (" + PACKETS_PER_LEVEL * level + " packets, "
                    + COINS_PER_LEVEL * level + " coins)", "small", () -> {
                Result result = controller.handleUpgradePlant(type.getName());
                toasts.show(result);
                topBar().refresh();
                refresh();
                showPlantDetail(type);
            })).growX().height(52f).padTop(14f).row();
        } else {
            panel.add(Ui.button(skin, "Buy for " + PURCHASE_COST + " coins", "small", () -> {
                Result result = controller.handlePurchasePlant(type.getName());
                toasts.show(result);
                topBar().refresh();
                refresh();
                showPlantDetail(type);
            })).growX().height(52f).padTop(14f).row();
        }
        detailPane.add(panel).growX();
    }

    private String tagText(PlantType type) {
        if (type.getTags().isEmpty()) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        for (Object tag : type.getTags()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(tag.toString().toLowerCase());
        }
        return builder.toString();
    }

    private void buildZombieGrid() {
        Set<String> seen = seenZombies();
        int discoveredCount = 0;
        for (ZombieType type : ZombieType.values()) {
            if (seen.contains(type.getName())) {
                discoveredCount++;
            }
        }
        gridPane.add(Ui.pill(skin, art.ui("image_ui_almanac_zombie_seed_pkt"),
                "Discovered " + discoveredCount + " of " + ZombieType.values().length, "small"))
                .colspan(COLUMNS).left().padBottom(8f).row();
        int column = 0;
        for (ZombieType type : ZombieType.values()) {
            boolean discovered = seen.contains(type.getName());
            ZombieCard card = new ZombieCard(skin, art, type, discovered);
            if (discovered) {
                card.onClick(() -> showZombieDetail(type));
                Ui.hoverLift(card, 1.05f);
            }
            Ui.appear(card, column);
            gridPane.add(card).size(ZombieCard.CARD_WIDTH, ZombieCard.CARD_HEIGHT).pad(7f);
            if (++column % COLUMNS == 0) {
                gridPane.row();
            }
        }
    }

    private Set<String> seenZombies() {
        Set<String> seen = new LinkedHashSet<>();
        String stored = store().get("zombies", "");
        if (!stored.isEmpty()) {
            seen.addAll(Arrays.asList(stored.split(",")));
        }
        return seen;
    }

    private void showZombieDetail(ZombieType type) {
        detailPane.clear();
        Table panel = Ui.panel(skin);
        panel.add(Ui.iconCell(art.zombie(type), 130f)).padBottom(6f).row();
        panel.add(Ui.label(skin, type.getName(), "h1")).padBottom(4f).row();
        panel.add(Ui.divider(skin, 300f)).padBottom(10f).row();
        stat(panel, art.ui("image_ui_almanac_zombies_zombietoughness_icon"),
                "Health", String.valueOf(type.getHitpoints()));
        stat(panel, art.ui("image_ui_almanac_zombies_zombiespeed_icon"),
                "Speed", type.getSpeed() + " tiles/s");
        stat(panel, art.statIcon("SPECIAL"), "Eat damage", type.getEatDps() + "/s");
        stat(panel, art.ui("image_ui_lock_small"), "Armor",
                type.getArmorType() == ZombieType.ArmorType.NONE ? "none"
                : type.getArmorType().name().replace('_', ' ').toLowerCase()
                + " (" + type.getArmorType().getArmorHitpoints() + " hp)");
        stat(panel, art.ui("image_ui_lock_small_gold"), "Metallic armor",
                type.getArmorType().isMetallic() ? "yes" : "no");
        stat(panel, art.statIcon("FAMILY"), "Wave cost", String.valueOf(type.getWaveCost()));
        detailPane.add(panel).growX();
    }

    private void stat(Table panel, com.badlogic.gdx.graphics.g2d.TextureRegion icon,
                      String key, String value) {
        Table row = new Table();
        row.add(Ui.iconCell(icon, 22f)).padRight(8f);
        row.add(Ui.label(skin, key, "muted")).left().expandX();
        row.add(Ui.wrapped(skin, value, "small")).width(160f).right();
        panel.add(row).growX().padBottom(6f).row();
    }
}
