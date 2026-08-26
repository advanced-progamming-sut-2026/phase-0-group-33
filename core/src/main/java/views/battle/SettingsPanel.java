package views.battle;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import models.settings.GamePreferences;
import views.assets.Audio;
import views.ui.Ui;

public final class SettingsPanel {

    private SettingsPanel() {
    }

    public static Table build(Skin skin, String username, Audio audio, LawnView lawn) {
        Table panel = new Table();
        panel.add(volume(skin, username, audio, true)).growX().padBottom(8f).row();
        panel.add(volume(skin, username, audio, false)).growX().padBottom(8f).row();
        panel.add(speed(skin, username)).growX().padBottom(8f).row();
        panel.add(grid(skin, username, lawn)).growX();
        return panel;
    }

    private static Table row(Skin skin, String title, Actor control) {
        Table line = new Table();
        line.add(Ui.label(skin, title, "h2")).width(190f).left().padRight(12f);
        line.add(control).growX().left();
        return line;
    }

    private static Table volume(Skin skin, final String username, final Audio audio,
                                final boolean music) {
        int value = music ? GamePreferences.getMusicVolume(username)
                : GamePreferences.getSfxVolume(username);
        final Label readout = Ui.label(skin, value + "%", "gold");
        final Slider slider = new Slider(0f, 100f, 5f, false, skin);
        slider.setValue(value);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int level = (int) slider.getValue();
                readout.setText(level + "%");
                if (music) {
                    GamePreferences.setMusicVolume(username, level);
                    audio.applyVolume();
                } else {
                    GamePreferences.setSfxVolume(username, level);
                }
            }
        });
        Table box = new Table();
        box.add(slider).width(260f).padRight(12f);
        box.add(readout).width(70f).left();
        return row(skin, music ? "Music" : "Sound effects", box);
    }

    private static Table speed(Skin skin, final String username) {
        final Label readout = Ui.label(skin,
                String.valueOf(GamePreferences.getGameSpeed(username)), "gold");
        final Slider slider = new Slider(GamePreferences.MIN_SPEED,
                GamePreferences.MAX_SPEED, 1f, false, skin);
        slider.setValue(GamePreferences.getGameSpeed(username));
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int level = (int) slider.getValue();
                readout.setText(String.valueOf(level));
                GamePreferences.setGameSpeed(username, level);
            }
        });
        Table box = new Table();
        box.add(slider).width(260f).padRight(12f);
        box.add(readout).width(70f).left();
        return row(skin, "Game speed", box);
    }

    private static Table grid(Skin skin, final String username, final LawnView lawn) {
        final CheckBox box = new CheckBox("  Show the lawn grid", skin);
        box.setChecked(GamePreferences.isGridVisible(username));
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GamePreferences.setGridVisible(username, box.isChecked());
                if (lawn != null) {
                    lawn.setShowGrid(box.isChecked());
                }
            }
        });
        Table line = new Table();
        line.add(box).left();
        line.add().growX();
        return line;
    }
}
