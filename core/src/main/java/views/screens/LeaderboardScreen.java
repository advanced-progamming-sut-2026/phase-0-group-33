package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.LeaderboardController;
import models.Result;
import views.PvzGame;
import views.ui.BaseScreen;
import views.ui.Palette;
import views.ui.Ui;

import java.util.List;

public class LeaderboardScreen extends BaseScreen {

    private static final String[] COLUMNS = {"miopoint", "levels", "minigames", "quests", "dailyquests"};

    private final LeaderboardController controller;
    private final Table table = new Table();

    private SelectBox<String> sortColumn;
    private SelectBox<String> sortOrder;

    public LeaderboardScreen(PvzGame game) {
        super(game);
        this.controller = new LeaderboardController(game.getApp());
    }

    @Override
    protected String title() {
        return "Leaderboard";
    }

    @Override
    protected void buildContent(Table body) {
        sortColumn = new SelectBox<>(skin);
        sortColumn.setItems(COLUMNS);
        sortOrder = new SelectBox<>(skin);
        sortOrder.setItems("desc", "asc");

        com.badlogic.gdx.scenes.scene2d.utils.ChangeListener listener =
                new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                        refresh();
                    }
                };
        sortColumn.addListener(listener);
        sortOrder.addListener(listener);

        Table controls = new Table();
        controls.add(Ui.label(skin, "Sort by", "muted")).padRight(10f);
        controls.add(sortColumn).width(240f).height(46f).padRight(18f);
        controls.add(Ui.label(skin, "Order", "muted")).padRight(10f);
        controls.add(sortOrder).width(160f).height(46f);
        controls.add().expandX();

        body.add(controls).growX().padBottom(12f).row();
        body.add(Ui.scroll(skin, table)).grow();
        refresh();
    }

    private void refresh() {
        table.clear();
        Result result = controller.handleShowLeaderboard(sortColumn.getSelected(), sortOrder.getSelected());
        if (!result.isSuccessfull()) {
            table.add(Ui.label(skin, result.getMessages().isEmpty()
                    ? "Could not load players." : result.getMessages().get(0), "bad")).pad(40f);
            return;
        }
        List<String> lines = result.getMessages();
        String currentUsername = app.getCurrentUser() == null ? "" : app.getCurrentUser().getUsername();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("Sort with:")) {
                continue;
            }
            String[] cells = line.split("\\|");
            boolean header = i == 0;
            Table row = new Table(skin);
            row.setBackground(skin.getDrawable(header ? "shade" : "row"));
            row.pad(6f, 18f, 6f, 18f);
            if (!header) {
                row.add(Ui.label(skin, "#" + i, "gold")).width(60f).left();
            } else {
                row.add(Ui.label(skin, "Rank", "gold")).width(60f).left();
            }
            for (int c = 0; c < cells.length; c++) {
                String value = cells[c].trim();
                String style = header ? "gold" : (c == 0 ? "h2" : "small");
                com.badlogic.gdx.scenes.scene2d.ui.Label label = Ui.label(skin, value, style);
                if (!header && c == 0 && value.equals(currentUsername)) {
                    label.setColor(Palette.GOLD);
                }
                row.add(label).width(c == 0 ? 240f : 170f).left();
            }
            table.add(row).growX().height(46f).padBottom(5f).row();
        }
    }
}
