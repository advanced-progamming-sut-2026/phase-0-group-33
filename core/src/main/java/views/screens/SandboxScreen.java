package views.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.SandboxController;
import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.map.TerrainType;
import views.PvzGame;
import views.ScreenId;
import views.ui.Ui;

public class SandboxScreen extends BattleScreen {

    private static final String[] TABS = {"Plants", "Zombies", "Events", "World"};
    private static final float TAB_WIDTH = 71f;
    private static final float FILTER_WIDTH = 92f;
    private static final float PANEL_WIDTH = 300f;
    private static final float LIST_HEIGHT = 388f;

    private final SandboxController sandbox;
    private final Table panel = new Table();
    private final Table column = new Table();
    private final Table body = new Table();
    private final Table list = new Table();
    private final Table tabRow = new Table();
    private final Table armedCell = new Table();
    private final Table filterRow = new Table();

    private boolean open = true;
    private String tab = TABS[0];
    private String filter = "All";
    private int filters;
    private TerrainType brush;

    public SandboxScreen(PvzGame game) {
        super(game);
        this.sandbox = new SandboxController(game.getApp());
        buildPanel();
    }

    @Override
    protected void showObjectives() {
        setPaused(false);
        panel.toFront();
    }

    @Override
    protected void leave() {
        sandbox.handleLeave();
        router.go(ScreenId.SANDBOX_SETUP);
    }

    private GameSession session() {
        return app.getCurrentGameSession();
    }

    private void buildPanel() {
        panel.setFillParent(true);
        panel.right().top();
        body.setBackground(skin.getDrawable("panel"));
        body.pad(6f);
        body.add(armedCell).width(PANEL_WIDTH - 18f).height(50f).padBottom(4f).row();
        body.add(Ui.label(skin, "space freeze   tab hide   c clear", "muted")).padBottom(3f).row();
        body.add(tabRow).padBottom(3f).row();
        body.add(filterRow).width(PANEL_WIDTH - 18f).padBottom(3f).row();
        body.add(Ui.scroll(skin, list)).size(PANEL_WIDTH - 18f, LIST_HEIGHT).row();
        panel.add(column).width(PANEL_WIDTH).padTop(80f).padRight(4f);
        stage.addActor(panel);
        panel.toFront();
        rebuildTop();
        refreshTabs();
        refreshArmed();
        refreshList();
    }

    private void rebuildTop() {
        column.clearChildren();
        column.add(Ui.button(skin, isFrozen() ? "Resume time" : "Freeze time",
                isFrozen() ? "green" : "blue", () -> {
                    setFrozen(!isFrozen());
                    rebuildTop();
                })).width(PANEL_WIDTH).height(44f).padBottom(3f).row();
        column.add(Ui.button(skin, open ? "Hide tools" : "Show tools", "small-purple", () -> {
            open = !open;
            rebuildTop();
        })).width(PANEL_WIDTH).height(32f).padBottom(3f).row();
        column.add(body).width(PANEL_WIDTH);
        body.setVisible(open);
        panel.toFront();
    }

    private void refreshTabs() {
        tabRow.clear();
        for (final String name : TABS) {
            tabRow.add(Ui.button(skin, name, name.equals(tab) ? "small-purple" : "small", () -> {
                tab = name;
                filter = "All";
                refreshTabs();
                refreshList();
            })).width(TAB_WIDTH).height(34f).padRight(1f);
        }
    }

    private void refreshArmed() {
        armedCell.clear();
        String armed = armed();
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable(armed == null ? "row" : "highlight"));
        card.pad(3f, 8f, 3f, 8f);
        TextureRegion icon = armedIcon();
        if (icon != null) {
            card.add(Ui.iconCell(icon, 32f)).padRight(8f);
        }
        card.add(Ui.label(skin, armed == null ? "Pick a tool below" : armed,
                armed == null ? "muted" : "gold")).growX().left();
        if (armed != null) {
            card.add(Ui.button(skin, "X", "small-brown", this::clearTool)).width(36f).height(30f);
        }
        armedCell.add(card).grow();
    }

    private String armed() {
        if (brush != null) {
            return "Paint " + pretty(brush.name());
        }
        if (pendingZombie != null) {
            return "Drop " + pendingZombie.getName();
        }
        if (pending != null) {
            return "Plant " + pending.getName();
        }
        return null;
    }

    private TextureRegion armedIcon() {
        if (pendingZombie != null) {
            return art.zombie(pendingZombie);
        }
        if (pending != null) {
            return art.plant(pending);
        }
        return null;
    }

    private void clearTool() {
        brush = null;
        armSandbox(Tool.NONE, null, null);
        refreshArmed();
        refreshList();
    }

    private void arm(PlantType plant, ZombieType zombie, TerrainType terrain) {
        brush = terrain;
        armSandbox(plant == null ? Tool.NONE : Tool.PLANT, plant, zombie);
        refreshArmed();
        refreshList();
    }

    private void refreshList() {
        list.clear();
        filterRow.clear();
        switch (tab) {
            case "Zombies":
                zombieFilters();
                zombieList();
                break;
            case "Events":
                eventList();
                break;
            case "World":
                worldList();
                break;
            default:
                plantFilters();
                plantList();
                break;
        }
    }

    private void addFilter(final String name) {
        filterRow.add(Ui.button(skin, name,
                name.equals(filter) ? "small-purple" : "small", () -> {
                    filter = name;
                    refreshList();
                })).width(FILTER_WIDTH).height(26f).pad(1f);
        filters++;
        if (filters % 3 == 0) {
            filterRow.row();
        }
    }

    private void plantFilters() {
        filters = 0;
        addFilter("All");
        addFilter("Shooter");
        addFilter("Lobber");
        addFilter("Explosive");
        addFilter("Wall nut");
    }

    private void zombieFilters() {
        filters = 0;
        addFilter("All");
        addFilter("Armoured");
        addFilter("Plain");
    }

    private static String pretty(String raw) {
        String text = raw.replace('_', ' ').toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private Table pick(TextureRegion icon, String name, String note, boolean chosen,
                       Runnable action) {
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable(chosen ? "highlight" : "row"));
        card.pad(3f, 8f, 3f, 8f);
        if (icon != null) {
            card.add(Ui.iconCell(icon, 30f)).padRight(8f);
        }
        Table text = new Table();
        text.add(Ui.label(skin, name, chosen ? "gold" : "small")).left().growX().row();
        if (note != null) {
            text.add(Ui.label(skin, note, "muted")).left().growX();
        }
        card.add(text).growX();
        Ui.hoverLift(card, 1.02f);
        Ui.onClick(card, action);
        return card;
    }

    private void plantList() {
        for (final PlantType type : PlantType.values()) {
            String family = pretty(type.getCategory().name());
            if (!"All".equals(filter) && !family.equals(filter)) {
                continue;
            }
            String note = type.getDamage() == 0 ? family
                    : family + "  -  " + type.getDamage() + " dmg";
            list.add(pick(art.plant(type), type.getName(), note, pending == type,
                    () -> arm(type, null, null))).growX().padBottom(2f).row();
        }
    }

    private void zombieList() {
        for (final ZombieType type : ZombieType.values()) {
            boolean armour = type.getArmorType() != ZombieType.ArmorType.NONE;
            if ("Armoured".equals(filter) && !armour) {
                continue;
            }
            if ("Plain".equals(filter) && armour) {
                continue;
            }
            String note = type.getHitpoints() + " hp"
                    + (armour ? "  -  " + pretty(type.getArmorType().name()) : "");
            list.add(pick(art.zombie(type), type.getName(), note, pendingZombie == type,
                    () -> arm(null, type, null))).growX().padBottom(2f).row();
        }
    }

    private void eventList() {
        list.add(Ui.label(skin, "Chapter events", "muted")).left().padBottom(2f).row();
        for (final String event : sandbox.events()) {
            list.add(pick(null, event, null, false,
                    () -> toasts.show(sandbox.handleEvent(event)))).growX().padBottom(2f).row();
        }
        list.add(Ui.label(skin, "Waves", "muted")).left().padTop(6f).padBottom(2f).row();
        list.add(pick(null, "Send the next wave", null, false,
                () -> toasts.show(sandbox.handleNextWave()))).growX().padBottom(2f).row();
        list.add(pick(null, "Send in the Zomboss", null, false,
                () -> toasts.show(sandbox.handleSpawnBoss()))).growX().padBottom(2f).row();
    }

    private void worldList() {
        GameSession session = session();
        boolean endless = session != null && session.isEndlessMowers();
        list.add(Ui.label(skin, "Lawn", "muted")).left().padBottom(2f).row();
        list.add(pick(null, "Clear every zombie", null, false,
                () -> act(sandbox.handleClearZombies()))).growX().padBottom(2f).row();
        list.add(pick(null, "Dig up every plant", null, false,
                () -> act(sandbox.handleClearPlants()))).growX().padBottom(2f).row();
        list.add(pick(null, "Feed every plant", null, false,
                () -> act(sandbox.handleFeedAll()))).growX().padBottom(2f).row();
        list.add(pick(null, "Top up sun and food", null, false,
                () -> act(sandbox.handleRefill()))).growX().padBottom(2f).row();
        list.add(Ui.label(skin, "Lawn mowers", "muted")).left().padTop(6f).padBottom(2f).row();
        list.add(pick(null, endless ? "Endless mowers" : "Normal mowers",
                endless ? "tap for normal" : "tap for endless", endless, () -> {
                    session().setEndlessMowers(!session().isEndlessMowers());
                    refreshList();
                })).growX().padBottom(2f).row();
        list.add(pick(null, "Take the mowers away", null, false,
                () -> act(sandbox.handleMowers(false)))).growX().padBottom(2f).row();
        list.add(Ui.label(skin, "Paint a tile, then click the lawn", "muted"))
                .left().padTop(6f).padBottom(2f).row();
        for (final TerrainType terrain : TerrainType.values()) {
            list.add(pick(null, pretty(terrain.name()), null, brush == terrain,
                    () -> arm(null, null, terrain))).growX().padBottom(2f).row();
        }
    }

    private void act(Result result) {
        toasts.show(result);
        refreshList();
    }

    @Override
    protected boolean extraKey(int keycode) {
        if (keycode == com.badlogic.gdx.Input.Keys.SPACE) {
            setFrozen(!isFrozen());
            rebuildTop();
            return true;
        }
        if (keycode == com.badlogic.gdx.Input.Keys.TAB) {
            open = !open;
            rebuildTop();
            return true;
        }
        if (keycode == com.badlogic.gdx.Input.Keys.C) {
            clearTool();
            return true;
        }
        return false;
    }

    @Override
    protected boolean sandboxClick(int column, int row) {
        if (brush != null) {
            toasts.show(sandbox.handleTerrain(brush.name(), column, row));
            return true;
        }
        if (pendingZombie != null) {
            toasts.show(sandbox.handlePlaceZombie(pendingZombie.getName(), column, row));
            return true;
        }
        return false;
    }
}
