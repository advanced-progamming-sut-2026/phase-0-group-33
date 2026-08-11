package views.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import controllers.menuControllers.SettingsController;
import models.Result;
import models.settings.GamePreferences;
import views.PvzGame;
import views.ui.BaseScreen;
import views.ui.Ui;

public class SettingsScreen extends BaseScreen {

    private final SettingsController controller;

    public SettingsScreen(PvzGame game) {
        super(game);
        this.controller = new SettingsController(game.getApp());
    }

    @Override
    protected String title() {
        return "Settings";
    }

    @Override
    protected void buildContent(Table body) {
        final String username = app.getCurrentUser().getUsername();
        Table panel = Ui.panel(skin);
        panel.pad(28f);
        addDifficulty(panel);
        addGameSpeed(panel, username);
        addToggles(panel, username);
        body.add(panel).width(940f).top();
    }

    private void addDifficulty(Table panel) {
        final SelectBox<String> difficulty = new SelectBox<>(skin);
        difficulty.setItems("1 - Newbie", "2 - Easy", "3 - Medium", "4 - Hard", "5 - Extreme");
        difficulty.setSelectedIndex(app.getCurrentUser().getDifficultyLevel().getLevelNumber() - 1);
        difficulty.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Result result = controller.handleChangeDifficulty(
                        String.valueOf(difficulty.getSelectedIndex() + 1));
                toasts.show(result);
            }
        });
        settingRow(panel, "Difficulty", "Scales zombie health, damage and speed.", difficulty, 320f);
    }

    private void addGameSpeed(Table panel, final String username) {
        final Label speedValue = Ui.label(skin, String.valueOf(GamePreferences.getGameSpeed(username)), "gold");
        final Slider speed = new Slider(GamePreferences.MIN_SPEED, GamePreferences.MAX_SPEED, 1f, false, skin);
        speed.setValue(GamePreferences.getGameSpeed(username));
        speed.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int value = (int) speed.getValue();
                speedValue.setText(String.valueOf(value));
                GamePreferences.setGameSpeed(username, value);
            }
        });
        Table speedBox = new Table();
        speedBox.add(speed).width(260f).padRight(14f);
        speedBox.add(speedValue);
        settingRow(panel, "Game speed", "How fast the battle advances (1 to 3).", speedBox, 320f);
    }

    private void addToggles(Table panel, final String username) {
        final CheckBox grid = new CheckBox("  Draw red grid lines on the lawn", skin);
        grid.setChecked(GamePreferences.isGridVisible(username));
        grid.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GamePreferences.setGridVisible(username, grid.isChecked());
            }
        });
        settingRow(panel, "Lawn grid", "Shows the tile boundaries during a battle.", grid, 420f);

        final CheckBox debug = new CheckBox("  Enable debug mode", skin);
        debug.setChecked(GamePreferences.isDebugMode(username));
        debug.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GamePreferences.setDebugMode(username, debug.isChecked());
                topBar().refresh();
                toasts.success(debug.isChecked()
                        ? "Debug mode on: cheat buttons are visible in every menu."
                        : "Debug mode off.");
            }
        });
        settingRow(panel, "Debug mode", "Adds coin, gem, sun and plant food cheats.", debug, 420f);
    }

    private void settingRow(Table panel, String title, String description, Actor control, float width) {
        Table text = new Table();
        text.add(Ui.label(skin, title, "h2")).left().row();
        text.add(Ui.wrapped(skin, description, "muted")).width(420f).left().padTop(4f);
        panel.add(text).left().padBottom(22f).padRight(30f);
        panel.add(control).width(width).left().padBottom(22f).row();
    }
}
