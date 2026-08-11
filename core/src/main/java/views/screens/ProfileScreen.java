package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import controllers.menuControllers.ProfileController;
import models.Result;
import views.PvzGame;
import views.ui.BaseScreen;
import views.ui.Ui;

public class ProfileScreen extends BaseScreen {

    private final ProfileController controller;
    private final Table summary = new Table();

    public ProfileScreen(PvzGame game) {
        super(game);
        this.controller = new ProfileController(game.getApp());
    }

    @Override
    protected String title() {
        return "Profile";
    }

    @Override
    protected void buildContent(Table body) {
        Table columns = new Table();
        columns.add(buildSummary()).width(430f).top().padRight(24f);
        columns.add(buildEditor()).width(640f).top();
        body.add(Ui.scroll(skin, columns)).grow();
    }

    private Table buildSummary() {
        Table panel = Ui.panel(skin);
        panel.add(Ui.label(skin, "Overview", "h1")).left().padBottom(4f).row();
        panel.add(Ui.divider(skin, 380f)).left().padBottom(12f).row();
        panel.add(summary).growX();
        refreshSummary();
        return panel;
    }

    private void refreshSummary() {
        summary.clear();
        Result info = controller.handleShowProfile();
        if (!info.isSuccessfull()) {
            summary.add(Ui.label(skin, "Not logged in.", "bad")).left();
            return;
        }
        for (String line : info.getMessages()) {
            int sep = line.indexOf(':');
            if (sep < 0) {
                summary.add(Ui.label(skin, line, "small")).left().colspan(3).row();
                continue;
            }
            String key = line.substring(0, sep);
            summary.add(Ui.iconCell(profileIcon(key), 24f)).padRight(10f).padBottom(8f);
            summary.add(Ui.label(skin, key, "muted")).left().padRight(16f).padBottom(8f);
            summary.add(Ui.label(skin, line.substring(sep + 1).trim(), "h2")).left().padBottom(8f).row();
        }
    }

    private com.badlogic.gdx.graphics.g2d.TextureRegion profileIcon(String key) {
        if (key.contains("Coins")) {
            return art.ui("image_ui_generic_coin_icon_small");
        }
        if (key.contains("Diamonds")) {
            return art.ui("image_ui_generic_gem_icon_small");
        }
        if (key.contains("miopoint")) {
            return art.ui("image_ui_generic_star_icon");
        }
        if (key.contains("Levels")) {
            return art.trophy("Egypt");
        }
        return art.statIcon("FAMILY");
    }

    private Table buildEditor() {
        Table panel = Ui.panel(skin);
        panel.add(Ui.label(skin, "Edit account", "h1")).colspan(3).left().padBottom(14f).row();

        final TextField username = Ui.field(skin, "new username");
        editorRow(panel, "Username", username, () -> {
            Result result = controller.handleChangeUsername(username.getText());
            toasts.show(result);
            afterChange(result, username);
        });

        final TextField nickname = Ui.field(skin, "new nickname");
        editorRow(panel, "Nickname", nickname, () -> {
            Result result = controller.handleChangeNickname(nickname.getText());
            toasts.show(result);
            afterChange(result, nickname);
        });

        final TextField email = Ui.field(skin, "new email");
        editorRow(panel, "Email", email, () -> {
            Result result = controller.handleChangeEmail(email.getText());
            toasts.show(result);
            afterChange(result, email);
        });

        final TextField oldPassword = Ui.password(skin, "current password");
        final TextField newPassword = Ui.password(skin, "new password");
        panel.add(Ui.label(skin, "Password", "h2")).right().padRight(14f).padBottom(10f);
        panel.add(oldPassword).width(340f).height(46f).left().padBottom(10f);
        panel.add().row();
        panel.add();
        panel.add(newPassword).width(340f).height(46f).left().padBottom(10f);
        panel.add(Ui.button(skin, "Save", "small", () -> {
            Result result = controller.handleChangePassword(oldPassword.getText(), newPassword.getText());
            toasts.show(result);
            if (result.isSuccessfull()) {
                oldPassword.setText("");
                newPassword.setText("");
            }
        })).width(140f).height(46f).padLeft(12f).row();

        return panel;
    }

    private void editorRow(Table panel, String label, TextField field, Runnable action) {
        panel.add(Ui.label(skin, label, "h2")).right().padRight(14f).padBottom(10f);
        panel.add(field).width(340f).height(46f).left().padBottom(10f);
        panel.add(Ui.button(skin, "Save", "small", action)).width(140f).height(46f).padLeft(12f).padBottom(10f).row();
    }

    private void afterChange(Result result, TextField field) {
        if (result.isSuccessfull()) {
            field.setText("");
            refreshSummary();
            topBar().refresh();
        }
    }
}
