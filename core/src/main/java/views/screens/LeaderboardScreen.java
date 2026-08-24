package views.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import net.BoardRow;
import net.Online;
import views.PvzGame;
import views.ui.BaseScreen;
import views.ui.Palette;
import views.ui.Ui;

import java.util.Comparator;
import java.util.List;

public class LeaderboardScreen extends BaseScreen {

    private static final String[] MEDALS = {"Egypt", "Frost Bite", "Wavey Beach"};
    private static final String[] COLUMNS = {"My Point", "Best score", "Levels",
        "Minigames", "Quests", "Daily quests"};
    private static final String[] HEADINGS = {"Player", "Levels", "Minigames",
        "Daily Q", "Other Q", "Best", "My Point"};
    private static final float[] WIDTHS = {230f, 110f, 120f, 110f, 110f, 120f, 130f};

    private final Table table = new Table();

    private SelectBox<String> sortColumn;
    private SelectBox<String> sortOrder;

    public LeaderboardScreen(PvzGame game) {
        super(game);
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
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
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
        controls.add(Ui.button(skin, "Refresh", "blue", this::refresh)).width(160f).height(46f);

        body.add(controls).growX().padBottom(12f).row();
        body.add(Ui.scroll(skin, table)).grow();
        refresh();
    }

    private void refresh() {
        table.clear();
        if (!Online.get().isSignedIn()) {
            table.add(Ui.label(skin, "The leaderboard lives on the server; you are offline.",
                    "bad")).pad(40f);
            return;
        }
        List<BoardRow> rows = Online.get().leaderboard();
        if (rows.isEmpty()) {
            table.add(Ui.label(skin, "The server has no players yet.", "muted")).pad(40f);
            return;
        }
        rows.sort(order());
        addHeader();
        String me = app.getCurrentUser() == null ? "" : app.getCurrentUser().getUsername();
        for (int i = 0; i < rows.size(); i++) {
            addRow(rows.get(i), i + 1, me);
        }
    }

    private Comparator<BoardRow> order() {
        Comparator<BoardRow> comparator;
        switch (sortColumn.getSelected()) {
            case "Best score":
                comparator = Comparator.comparingInt(BoardRow::getBest);
                break;
            case "Levels":
                comparator = Comparator.comparingInt(BoardRow::getLevels);
                break;
            case "Minigames":
                comparator = Comparator.comparingInt(BoardRow::getMinigames);
                break;
            case "Quests":
                comparator = Comparator.comparingInt(BoardRow::getQuests);
                break;
            case "Daily quests":
                comparator = Comparator.comparingInt(BoardRow::getDailyQuests);
                break;
            default:
                comparator = Comparator.comparingInt(row -> Math.max(-1, row.getPoint()));
                break;
        }
        return "asc".equals(sortOrder.getSelected()) ? comparator : comparator.reversed();
    }

    private void addHeader() {
        Table row = new Table(skin);
        row.setBackground(skin.getDrawable("shade"));
        row.pad(6f, 18f, 6f, 18f);
        row.add(Ui.label(skin, "Rank", "gold")).width(72f).left();
        for (int i = 0; i < HEADINGS.length; i++) {
            row.add(Ui.label(skin, HEADINGS[i], "gold")).width(WIDTHS[i]).left();
        }
        table.add(row).growX().height(46f).padBottom(5f).row();
    }

    private void addRow(BoardRow entry, int rank, String me) {
        Table row = new Table(skin);
        row.setBackground(skin.getDrawable("row"));
        row.pad(6f, 18f, 6f, 18f);
        if (rank <= MEDALS.length) {
            row.add(Ui.iconCell(art.trophy(MEDALS[rank - 1]), 30f)).width(72f).left();
        } else {
            row.add(Ui.label(skin, "#" + rank, "gold")).width(72f).left();
        }
        Label name = Ui.label(skin, entry.getUsername(), "h2");
        if (entry.getUsername().equals(me)) {
            name.setColor(Palette.GOLD);
        }
        row.add(name).width(WIDTHS[0]).left();
        String[] cells = {
            String.valueOf(entry.getLevels()), String.valueOf(entry.getMinigames()),
            String.valueOf(entry.getDailyQuests()), String.valueOf(entry.getQuests()),
            String.valueOf(entry.getBest()),
        };
        for (int i = 0; i < cells.length; i++) {
            row.add(Ui.label(skin, cells[i], "small")).width(WIDTHS[i + 1]).left();
        }
        row.add(Ui.label(skin, entry.hasPoint() ? String.valueOf(entry.getPoint()) : "-",
                entry.hasPoint() ? "good" : "muted")).width(WIDTHS[6]).left();
        Ui.appear(row, rank);
        table.add(row).growX().height(46f).padBottom(5f).row();
    }
}
