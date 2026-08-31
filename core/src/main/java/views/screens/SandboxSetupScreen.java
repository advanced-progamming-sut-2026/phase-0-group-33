package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.SandboxController;
import models.Result;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Ui;

public class SandboxSetupScreen extends BaseScreen {

    private final SandboxController controller;
    private final Table levels = new Table();

    private String chapter;

    public SandboxSetupScreen(PvzGame game) {
        super(game);
        this.controller = new SandboxController(game.getApp());
    }

    @Override
    protected String title() {
        return "Sandbox";
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.MAIN;
    }

    @Override
    protected void buildContent(Table body) {
        if (!models.settings.GamePreferences.isDebugMode(app.getCurrentUser() == null
                ? null : app.getCurrentUser().getUsername())) {
            body.add(Ui.wrapped(skin, "The sandbox is a testing tool. Turn on debug mode"
                    + " in Settings to reach it.", "bad")).width(700f).pad(40f);
            return;
        }
        body.add(Ui.wrapped(skin, "Pick a lawn to play with. Everything is free in the"
                + " sandbox: no sun cost, no recharge, plant anywhere, drop any zombie,"
                + " paint the ground and fire the chapter's own events whenever you like.",
                "muted")).width(880f).padBottom(14f).row();

        Table picker = new Table();
        for (final String name : controller.chapters()) {
            picker.add(Ui.button(skin, name, "green", () -> {
                chapter = name;
                refreshLevels();
            })).width(210f).height(56f).pad(5f);
        }
        body.add(picker).padBottom(10f).row();
        body.add(Ui.scroll(skin, levels)).grow();
        chapter = controller.chapters().isEmpty() ? null : controller.chapters().get(0);
        refreshLevels();
    }

    private void refreshLevels() {
        levels.clear();
        if (chapter == null) {
            levels.add(Ui.label(skin, "No chapters are available.", "muted")).pad(30f);
            return;
        }
        levels.add(Ui.label(skin, chapter, "h1")).left().padBottom(8f).row();
        int number = 1;
        for (String name : controller.levelsOf(chapter)) {
            final int chosen = number;
            Table card = Ui.card(skin, "row");
            card.pad(10f, 16f, 10f, 16f);
            card.add(Ui.label(skin, name, "h2")).growX().left();
            card.add(Ui.button(skin, "Open", "green", () -> open(chosen)))
                    .width(150f).height(46f).right();
            Ui.appear(card, number);
            levels.add(card).growX().padBottom(6f).row();
            number++;
        }
    }

    private void open(int levelNumber) {
        Result result = controller.handleOpen(chapter, levelNumber);
        if (!result.isSuccessfull()) {
            toasts.show(result);
            return;
        }
        router.go(ScreenId.SANDBOX);
    }
}
