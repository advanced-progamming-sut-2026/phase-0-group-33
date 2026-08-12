package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.TravelLogController;
import models.Result;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Ui;

import java.util.List;

public class QuestScreen extends BaseScreen {

    private static final String[] PAGES = {"critical", "high", "daily", "minigame"};

    private final TravelLogController controller;
    private final Table content = new Table();
    private String page = PAGES[0];

    public QuestScreen(PvzGame game) {
        super(game);
        this.controller = new TravelLogController(game.getApp());
    }

    @Override
    protected String title() {
        return "Travel Log";
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.ADVENTURE;
    }

    @Override
    protected void buildContent(Table body) {
        String[] labels = new String[PAGES.length];
        for (int i = 0; i < PAGES.length; i++) {
            labels[i] = label(PAGES[i]);
        }
        body.add(Ui.tabs(skin, labels, 0, index -> {
            page = PAGES[index];
            refresh();
        })).left().padBottom(12f).row();
        body.add(Ui.scroll(skin, content)).grow();
        refresh();
    }

    private String label(String name) {
        if ("minigame".equals(name)) {
            return "Minigames";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void refresh() {
        content.clear();
        Result result = controller.handleShowPage(page);
        if (!result.isSuccessfull()) {
            content.add(Ui.label(skin, "Could not load this page.", "bad")).pad(40f);
            return;
        }
        List<String> lines = result.getMessages();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("- ")) {
                line = line.substring(2);
            }
            Table card = "minigame".equals(page) ? minigameCard(line) : questCard(line);
            Ui.appear(card, i);
            content.add(card).growX().padBottom(8f).row();
        }
    }

    private Table questCard(String line) {
        String progress = "";
        String text = line;
        int bracket = line.lastIndexOf('[');
        if (bracket > 0 && line.endsWith("]")) {
            progress = line.substring(bracket + 1, line.length() - 1);
            text = line.substring(0, bracket).trim();
        }
        String reward = "";
        int rewardIndex = text.indexOf("| reward:");
        if (rewardIndex > 0) {
            reward = text.substring(rewardIndex + 9).trim();
            text = text.substring(0, rewardIndex).trim();
        }

        boolean done = "DONE".equals(progress);
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable(done ? "row-done" : "row"));
        card.pad(10f, 18f, 10f, 18f);
        card.add(Ui.iconCell(art.ui(done
                ? "image_ui_generic_check_mark_anim_check_mark_anim_102x102"
                : "image_ui_generic_star_icon"), 40f)).padRight(16f).top();

        Table details = new Table();
        details.add(Ui.wrapped(skin, text, "h2")).width(680f).left().row();
        if (!reward.isEmpty()) {
            String icon = reward.contains("gem") ? "image_ui_generic_gem_icon_small"
                    : reward.contains("packet") ? "image_ui_almanac_plant_select_pkt"
                    : reward.contains("plant") ? "image_ui_generic_leaf_backdrop"
                    : "image_ui_generic_coin_icon_small";
            details.add(Ui.pill(skin, art.ui(icon), reward, "gold")).left().padTop(4f).row();
        }
        details.add(progressWidget(progress, done)).left().padTop(6f);
        card.add(details).growX();

        card.add(Ui.label(skin, done ? "Completed" : progress, done ? "good" : "muted")).right().padLeft(16f);
        return card;
    }

    private Table progressWidget(String progress, boolean done) {
        Table box = new Table();
        int slash = progress.indexOf('/');
        if (done || slash <= 0) {
            return box;
        }
        try {
            float current = Float.parseFloat(progress.substring(0, slash).trim());
            float goal = Math.max(1f, Float.parseFloat(progress.substring(slash + 1).trim()));
            ProgressBar bar = new ProgressBar(0f, goal, 1f, false, skin, "gold-horizontal");
            bar.setValue(current);
            box.add(bar).width(420f).height(16f);
        } catch (NumberFormatException ignored) {
            return box;
        }
        return box;
    }

    private void startMinigame(String name) {
        Result result = new controllers.menuControllers.TravelLogController(app)
                .handlePlayMinigame(name, 1);
        if (!result.isSuccessfull()) {
            toasts.show(result);
            return;
        }
        router.go(views.ScreenId.BATTLE);
    }

    private Table minigameCard(String line) {
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable("row"));
        card.pad(12f, 18f, 12f, 18f);
        String name = line;
        String description = "";
        int sep = line.indexOf('|');
        if (sep > 0) {
            name = line.substring(0, sep).trim();
            description = line.substring(sep + 1).trim();
        }
        card.add(Ui.iconCell(art.ui("image_ui_generic_button_hud_minigames_normal"), 48f)).padRight(16f);
        Table text = new Table();
        text.add(Ui.label(skin, name, "h2")).left().row();
        text.add(Ui.wrapped(skin, description, "muted")).width(620f).left().padTop(4f);
        card.add(text).growX();
        final String key = name;
        card.add(Ui.button(skin, "Play", "small", () -> startMinigame(key)))
                .width(140f).height(50f).right();
        return card;
    }
}
