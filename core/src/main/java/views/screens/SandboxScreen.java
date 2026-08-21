package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.SandboxController;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.map.TerrainType;
import views.PvzGame;
import views.ScreenId;
import views.ui.Ui;

public class SandboxScreen extends BattleScreen {

    private static final String[] TABS = {"Plants", "Zombies", "Events", "World"};
    private static final float PANEL_WIDTH = 264f;

    private final SandboxController sandbox;
    private final Table panel = new Table();
    private final Table list = new Table();

    private final Table body = new Table();
    private final Table freezeCell = new Table();
    private boolean open = true;
    private String tab = TABS[0];
    private boolean mowers = true;
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

    private void buildPanel() {
        panel.setFillParent(true);
        panel.right().top();
        body.setBackground(skin.getDrawable("panel"));
        body.pad(6f);
        Table tabs = new Table();
        for (final String name : TABS) {
            tabs.add(Ui.button(skin, name, "small", () -> {
                tab = name;
                refreshList();
            })).width(58f).height(34f).padRight(2f);
        }
        body.add(tabs).padBottom(4f).row();
        body.add(Ui.scroll(skin, list)).size(PANEL_WIDTH - 18f, 452f).row();
        body.add(Ui.button(skin, "Clear tool", "small-brown",
                () -> armSandbox(Tool.NONE, null, null))).width(PANEL_WIDTH - 22f)
                .height(36f).padTop(4f);

        Table column = new Table();
        column.add(freezeCell).width(PANEL_WIDTH).height(42f).padBottom(2f).row();
        column.add(Ui.button(skin, "Sandbox tools", "small-purple", this::toggle))
                .width(PANEL_WIDTH).height(36f).padBottom(2f).row();
        column.add(body).width(PANEL_WIDTH);
        refreshFreeze();
        panel.add(column).width(PANEL_WIDTH).padTop(82f).padRight(4f);
        stage.addActor(panel);
        panel.toFront();
        refreshList();
    }

    private void refreshFreeze() {
        freezeCell.clear();
        freezeCell.add(Ui.button(skin, isFrozen() ? "Resume time" : "Freeze time",
                isFrozen() ? "green" : "blue", () -> {
                    setFrozen(!isFrozen());
                    refreshFreeze();
                    panel.toFront();
                })).width(PANEL_WIDTH).height(42f);
    }

    private void toggle() {
        open = !open;
        body.setVisible(open);
        panel.toFront();
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
                toasts.success("Planting " + type.getName());
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
                toasts.success("Dropping " + type.getName());
            });
            list.add(card).growX().padBottom(2f).row();
        }
    }

    private void eventList() {
        for (final String event : sandbox.events()) {
            list.add(row(event, () -> {
                toasts.show(sandbox.handleEvent(event));
            }, "small")).growX().padBottom(2f).row();
        }
        list.add(row("Next wave", () -> toasts.show(sandbox.handleNextWave()), "small"))
                .growX().padBottom(2f).row();
        list.add(row("Send in the Zomboss", () -> toasts.show(sandbox.handleSpawnBoss()),
                "small")).growX().padBottom(2f).row();
    }

    private void worldList() {
        list.add(row(session().isEndlessMowers() ? "Mowers: endless (tap for normal)"
                : "Mowers: normal (tap for endless)", () -> {
                    session().setEndlessMowers(!session().isEndlessMowers());
                    refreshList();
                }, "small")).growX().padBottom(2f).row();
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
                toasts.success("Painting " + terrain);
            }, "small")).growX().padBottom(2f).row();
        }
    }

    private models.game.GameSession session() {
        return app.getCurrentGameSession();
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
