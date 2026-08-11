package views.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import models.progress.chapter.Chapter;
import models.progress.level.BossLevel;
import models.progress.level.Level;
import utils.UserDataStore;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Palette;
import views.ui.Ui;

public class AdventureScreen extends BaseScreen {

    private static final String[] CHAPTERS = {"Egypt", "Frost Bite", "Wavey Beach", "Dark Ages"};

    private final Table levelPane = new Table();
    private String selectedChapter = CHAPTERS[0];

    public AdventureScreen(PvzGame game) {
        super(game);
    }

    @Override
    protected String title() {
        return "Adventure";
    }

    @Override
    protected TextureRegion background() {
        return art.chapterBackground(selectedChapter);
    }

    @Override
    protected void buildContent(Table body) {
        Table columns = new Table();
        columns.add(buildChapterList()).width(420f).top().padRight(24f);
        columns.add(levelPane).width(700f).top();
        body.add(columns).grow().row();
        body.add(Ui.button(skin, "Quests & Minigames", "blue", () -> router.go(ScreenId.QUESTS)))
                .width(320f).height(58f).padTop(10f);
        showLevels(selectedChapter);
    }

    private Table buildChapterList() {
        Table panel = Ui.panel(skin);
        panel.add(Ui.label(skin, "Chapters", "h1")).left().padBottom(12f).row();
        for (int i = 0; i < CHAPTERS.length; i++) {
            final String name = CHAPTERS[i];
            Chapter chapter = Chapter.getByName(name);
            int total = chapter.getLevels().size();
            int done = Math.max(0, progress(name) - 1);
            boolean locked = isLocked(i);

            Table card = Ui.card(skin, locked ? "card" : "card-done");
            card.add(Ui.label(skin, name, "h2")).left().expandX();
            card.add(Ui.label(skin, done + " / " + total, locked ? "muted" : "gold")).right().row();
            if (locked) {
                card.add(Ui.label(skin, "Locked - finish " + CHAPTERS[i - 1] + " first", "bad"))
                        .colspan(2).left().padTop(4f);
            }
            if (!locked) {
                Ui.onClick(card, () -> {
                    selectedChapter = name;
                    showLevels(name);
                });
            }
            panel.add(card).growX().padBottom(10f).row();
        }
        return panel;
    }

    private void showLevels(String chapterName) {
        levelPane.clear();
        Table panel = Ui.panel(skin);
        panel.add(Ui.label(skin, chapterName, "h1")).left().padBottom(12f).row();

        Chapter chapter = Chapter.getByName(chapterName);
        int unlocked = progress(chapterName);
        Table grid = new Table();
        for (Level level : chapter.getLevels()) {
            boolean open = level.getLevelNumber() <= unlocked;
            Table card = Ui.card(skin, open ? "card" : "card");
            card.add(Ui.label(skin, "Level " + level.getLevelNumber(), "h2")).left().row();
            card.add(Ui.label(skin, describe(level), "muted")).left().padTop(4f).row();
            card.add(Ui.label(skin, open ? "Unlocked" : "Locked",
                    open ? "good" : "bad")).left().padTop(4f).row();
            if (open) {
                Ui.onClick(card, () -> toasts.error("The battle screen is not part of this build yet."));
            } else {
                card.setColor(Palette.LOCKED);
            }
            grid.add(card).width(300f).height(150f).pad(8f);
            if (level.getLevelNumber() % 2 == 0) {
                grid.row();
            }
        }
        panel.add(grid).growX().row();
        panel.add(Ui.label(skin, "Zombies in this chapter: " + chapter.getZombiePool().size(), "muted"))
                .left().padTop(10f);
        levelPane.add(panel).growX();
    }

    private String describe(Level level) {
        if (level instanceof BossLevel) {
            return "Boss battle";
        }
        return level.getSpecialType() == null ? "Ordinary level"
                : level.getSpecialType().name().replace('_', ' ').toLowerCase();
    }

    private boolean isLocked(int index) {
        if (index == 0) {
            return false;
        }
        String previous = CHAPTERS[index - 1];
        Chapter chapter = Chapter.getByName(previous);
        return progress(previous) < chapter.getLevels().size();
    }

    private int progress(String chapterName) {
        if (app.getCurrentUser() == null) {
            return 1;
        }
        return UserDataStore.forUser(app.getCurrentUser().getUsername())
                .getInt("progress." + chapterName, 1);
    }
}
