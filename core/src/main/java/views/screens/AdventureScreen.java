package views.screens;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import controllers.menuControllers.MainController;
import models.Result;
import models.entities.zombie.ZombieType;
import models.game.GamePhase;
import models.game.GameSession;
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

    private final Table chapterPane = new Table();
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
        columns.add(chapterPane).width(430f).top().padRight(22f);
        columns.add(levelPane).width(680f).top();
        body.add(columns).grow().row();
        body.add(Ui.button(skin, "Quests & Minigames", "blue", () -> router.go(ScreenId.QUESTS)))
                .width(320f).height(56f).padTop(8f);
        buildChapterList();
        showLevels(selectedChapter);
    }

    private void buildChapterList() {
        chapterPane.clear();
        Table panel = Ui.panel(skin);
        panel.add(Ui.label(skin, "Chapters", "h1")).left().padBottom(4f).row();
        panel.add(Ui.divider(skin, 380f)).left().padBottom(10f).row();

        for (int i = 0; i < CHAPTERS.length; i++) {
            final String name = CHAPTERS[i];
            Chapter chapter = Chapter.getByName(name);
            int total = chapter.getLevels().size();
            int done = Math.max(0, Math.min(total, progress(name) - 1));
            boolean locked = isLocked(i);
            boolean active = name.equals(selectedChapter);

            Table card = new Table(skin);
            card.setBackground(skin.getDrawable(active ? "card-done" : "card"));
            card.pad(8f, 12f, 8f, 12f);
            card.add(Ui.iconCell(art.trophy(name), 52f)).padRight(10f);

            Table info = new Table();
            Table head = new Table();
            head.add(Ui.label(skin, name, "h2")).left().expandX();
            head.add(Ui.label(skin, done + " / " + total, locked ? "muted" : "gold")).right();
            info.add(head).growX().row();

            ProgressBar bar = new ProgressBar(0f, total, 1f, false, skin, "gold-horizontal");
            bar.setValue(done);
            info.add(bar).growX().height(14f).padTop(4f).row();

            if (locked) {
                info.add(Ui.label(skin, "Finish " + CHAPTERS[i - 1] + " to unlock", "bad"))
                        .left().padTop(3f);
            }
            card.add(info).growX();

            if (locked) {
                card.setColor(Palette.LOCKED);
            } else {
                Ui.hoverLift(card, 1.02f);
                Ui.onClick(card, () -> {
                    selectedChapter = name;
                    buildChapterList();
                    showLevels(name);
                });
            }
            Ui.appear(card, i);
            panel.add(card).growX().padBottom(9f).row();
        }
        chapterPane.add(panel).growX();
    }

    private void showLevels(String chapterName) {
        levelPane.clear();
        Chapter chapter = Chapter.getByName(chapterName);
        int unlocked = progress(chapterName);

        Table panel = Ui.panel(skin);
        Table head = new Table();
        head.add(Ui.iconCell(art.trophy(chapterName), 46f)).padRight(12f);
        head.add(Ui.label(skin, chapterName, "h1")).left().expandX();
        head.add(Ui.pill(skin, art.zombie(ZombieType.NORMAL),
                chapter.getZombiePool().size() + " zombies", "small")).right();
        panel.add(head).growX().padBottom(4f).row();
        panel.add(Ui.divider(skin, 620f)).padBottom(12f).row();

        Table grid = new Table();
        int index = 0;
        for (Level level : chapter.getLevels()) {
            boolean open = level.getLevelNumber() <= unlocked;
            Table card = new Table(skin);
            card.setBackground(skin.getDrawable(open ? "card" : "card"));
            card.pad(10f);

            card.add(Ui.iconCell(levelIcon(level), 54f)).padRight(10f).top();
            Table info = new Table();
            info.add(Ui.label(skin, "Level " + level.getLevelNumber(), "h2")).left().row();
            info.add(Ui.label(skin, describe(level), "muted")).left().padTop(2f).row();
            info.add(Ui.label(skin, open ? "Ready to play" : "Locked", open ? "good" : "bad"))
                    .left().padTop(4f);
            card.add(info).growX().top();

            if (open) {
                Ui.hoverLift(card, 1.03f);
                Ui.onClick(card, () -> enterLevel(level.getLevelNumber()));
            } else {
                card.setColor(Palette.LOCKED);
            }
            Ui.appear(card, index);
            grid.add(card).width(300f).height(126f).pad(7f);
            if (++index % 2 == 0) {
                grid.row();
            }
        }
        panel.add(grid).growX();
        levelPane.add(panel).growX();
    }

    private void enterLevel(int levelNumber) {
        Result result = new MainController(app).handleEnterChapter(selectedChapter, levelNumber);
        if (!result.isSuccessfull()) {
            toasts.show(result);
            return;
        }
        GameSession session = app.getCurrentGameSession();
        if (session != null && session.getPhase() == GamePhase.BATTLE) {
            router.go(ScreenId.BATTLE);
        } else {
            router.go(ScreenId.SEED_SELECT);
        }
    }

    private TextureRegion levelIcon(Level level) {
        if (level instanceof BossLevel) {
            return art.zombie(ZombieType.GARGANTUAR);
        }
        if (level.getSpecialType() == null) {
            return art.zombie(ZombieType.NORMAL);
        }
        return art.ui("image_ui_generic_jalapeno_difficulty_icon");
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
