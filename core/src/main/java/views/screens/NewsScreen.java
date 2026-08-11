package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.NewsController;
import utils.NewsStore;
import views.PvzGame;
import views.ui.BaseScreen;
import views.ui.Ui;

import java.util.List;

public class NewsScreen extends BaseScreen {

    private final NewsController controller;
    private final Table feed = new Table();

    public NewsScreen(PvzGame game) {
        super(game);
        this.controller = new NewsController(game.getApp());
    }

    @Override
    protected String title() {
        return "News";
    }

    @Override
    protected void buildContent(Table body) {
        Table header = new Table();
        header.add(Ui.label(skin, unreadText(), "h2")).left().expandX();
        header.add(Ui.button(skin, "Mark all as read", "blue", () -> {
            controller.handleShowUnread();
            refresh();
        })).width(260f).height(50f);
        body.add(header).growX().padBottom(12f).row();
        body.add(Ui.scroll(skin, feed)).grow();
        refresh();
    }

    private String unreadText() {
        int unread = NewsStore.countUnread(app.getCurrentUser().getUsername());
        return unread == 0 ? "Everything is read." : unread + " unread item(s)";
    }

    private void refresh() {
        feed.clear();
        List<String> entries = NewsStore.readAll(app.getCurrentUser().getUsername());
        if (entries.isEmpty()) {
            feed.add(Ui.label(skin, "No news yet. Play a level to make history.", "muted")).pad(40f);
            return;
        }
        for (int i = entries.size() - 1; i >= 0; i--) {
            Table card = new Table(skin);
            card.setBackground(skin.getDrawable("row"));
            card.pad(10f, 16f, 10f, 16f);
            card.add(Ui.iconCell(art.ui("image_ui_generic_star_icon"), 30f)).padRight(14f);
            card.add(Ui.wrapped(skin, entries.get(i), "h2")).growX().left();
            feed.add(card).growX().padBottom(8f).row();
        }
    }
}
