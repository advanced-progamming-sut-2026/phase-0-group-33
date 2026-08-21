package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.SandboxController;
import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.map.TerrainType;
import views.PvzGame;
import views.ScreenId;
import views.ui.Ui;

public class SandboxScreen extends BattleScreen {

    private static final String[] TABS = {"Plants", "Zombies", "Events", "World"};
    private static final float PANEL_WIDTH = 300f;

    private final SandboxController sandbox;
    private final Table panel = new Table();
    private final Table list = new Table();

    private String tab = TABS[0];
    private boolean mowers = true;
    private TerrainType brush;

    public SandboxScreen(PvzGame game) {
        super(game);
        this.sandbox = new SandboxController(game.getApp());
        buildPanel();
        showObjectives();
    }

    @Override
    protected void showObjectives() {
        announce("Sandbox: everything is free. Pick a tool on the right.");
    }

    @Override
    protected void leave() {
        sandbox.handleLeave();
        router.go(ScreenId.SANDBOX_SETUP);
    }

    private void buildPanel() {
        panel.setFillParent(true);
        panel.right().top();
        Table box = new Table(skin);
        box.setBackground(skin.getDrawable("panel"));
        box.pad(8f);

        Table tabs = new Table();
        for (final String name : TABS) {
            tabs.add(Ui.button(skin, name, "small", () -> {
                tab = name;
                refreshList();
            })).width(68f).height(38f).padRight(2f);
        }
        box.add(tabs).padBottom(6f).row();
        box.add(Ui.scroll(skin, list)).size(PANEL_WIDTH - 20f, 470f).row();
        box.add(Ui.button(skin, "Clear tool", "small-brown",
                () -> armSandbox(Tool.NONE, null, null))).width(PANEL_WIDTH - 24f)
                .height(40f).padTop(6f);
        panel.add(box).width(PANEL_WIDTH).padTop(84f).padRight(4f);
        stage.addActor(panel);
        refreshList();
    }

    private void refreshList() {
        list.clear();
        switch (tab) {
            case "Zombies":
                zombieList();
                break;
            case "Events":
                eventList();
                break;
            case "World":
                worldList();
                break;
            default:
                plantList();
                break;
        }
    }

    private Table row(String text, Runnable action, String style) {
        Table card = Ui.card(skin, "row");
        card.pad(4f, 8f, 4f, 8f);
        card.add(Ui.label(skin, text, style)).growX().left();
        Ui.hoverLift(card, 1.02f);
        Ui.onClick(card, action);
        return card;
    }

    private void plantList() {
        for (final PlantType type : PlantType.values()) {
            Table card = Ui.card(skin, "row");
            card.pad(3f, 8f, 3f, 8f);
            card.add(Ui.iconCell(art.plant(type), 32f)).padRight(8f);
            card.add(Ui.label(skin, type.getName(), "small")).growX().left();
            Ui.hoverLift(card, 1.02f);
            Ui.onClick(card, () -> {
                armSandbox(Tool.PLANT, type, null);
                brush = null;
                announce("Planting " + type.getName());
            });
            list.add(card).growX().padBottom(2f).row();
        }
    }

    private void zombieList() {
        for (final ZombieType type : ZombieType.values()) {
            Table card = Ui.card(skin, "row");
            card.pad(3f, 8f, 3f, 8f);
            card.add(Ui.iconCell(art.zombie(type), 32f)).padRight(8f);
            card.add(Ui.label(skin, type.getName(), "small")).growX().left();
            Ui.hoverLift(card, 1.02f);
            Ui.onClick(card, () -> {
                armSandbox(Tool.NONE, null, type);
                brush = null;
                announce("Dropping " + type.getName());
            });
            list.add(card).growX().padBottom(2f).row();
        }
    }

    private void eventList() {
        for (final String event : sandbox.events()) {
            list.add(row(event, () -> {
                Result result = sandbox.handleEvent(event);
                toasts.show(result);
                announce(result.getMessages().isEmpty() ? event
                        : result.getMessages().get(0));
            }, "small")).growX().padBottom(2f).row();
        }
        list.add(row("Next wave", () -> toasts.show(sandbox.handleNextWave()), "small"))
                .growX().padBottom(2f).row();
        list.add(row("Send in the Zomboss", () -> toasts.show(sandbox.handleSpawnBoss()),
                "small")).growX().padBottom(2f).row();
    }

    private void worldList() {
        list.add(row("Clear every zombie", () -> toasts.show(sandbox.handleClearZombies()),
                "small")).growX().padBottom(2f).row();
        list.add(row("Dig up every plant", () -> toasts.show(sandbox.handleClearPlants()),
                "small")).growX().padBottom(2f).row();
        list.add(row("Feed every plant", () -> toasts.show(sandbox.handleFeedAll()), "small"))
                .growX().padBottom(2f).row();
        list.add(row("Top up sun and food", () -> toasts.show(sandbox.handleRefill()), "small"))
                .growX().padBottom(2f).row();
        list.add(row(mowers ? "Remove lawn mowers" : "Restore lawn mowers", () -> {
            mowers = !mowers;
            toasts.show(sandbox.handleMowers(mowers));
            refreshList();
        }, "small")).growX().padBottom(2f).row();
        list.add(Ui.label(skin, "Paint a tile", "muted")).left().padTop(6f).padBottom(2f).row();
        for (final TerrainType terrain : TerrainType.values()) {
            list.add(row(terrain.name().replace('_', ' '), () -> {
                brush = terrain;
                armSandbox(Tool.NONE, null, null);
                announce("Painting " + terrain);
            }, "small")).growX().padBottom(2f).row();
        }
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
