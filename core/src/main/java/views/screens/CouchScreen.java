package views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import models.App;
import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.game.DuelRules;
import models.game.GameSession;
import views.PvzGame;
import views.Router;
import views.ScreenId;
import views.assets.Art;
import views.battle.Lawn;
import views.battle.LawnView;
import views.battle.Overlay;
import views.ui.BaseScreen;
import views.ui.Toasts;
import views.ui.Ui;

import java.util.ArrayList;
import java.util.List;

public class CouchScreen extends ScreenAdapter {

    private static final float SECONDS_PER_TICK = 1f / GameSession.TICKS_PER_SECOND;
    private static final int MAX_TICKS_PER_FRAME = 6;

    private final PvzGame game;
    private final App app;
    private final Skin skin;
    private final Art art;
    private final Router router;
    private final Stage stage;
    private final Toasts toasts;

    private final views.multiplayer.ReactionPop pop;
    private final Table plantTray = new Table();
    private final Table zombieTray = new Table();
    private final List<Table> plantSlots = new ArrayList<>();
    private final List<Table> zombieSlots = new ArrayList<>();

    private GameSession session;
    private DuelRules rules;
    private List<ZombieType> roster = new ArrayList<>();
    private LawnView lawnView;
    private Label clock;
    private Label plantSun;
    private Label zombieSun;
    private Label cursorLabel;
    private Overlay overlay;

    private PlantType chosenPlant;
    private int chosenZombie;
    private int cursorColumn = DuelRules.FIRST_ZOMBIE_COLUMN + 3;
    private int cursorRow = 3;
    private float accumulator;

    public CouchScreen(PvzGame game) {
        this.game = game;
        this.app = game.getApp();
        this.skin = game.getSkin();
        this.art = game.getArt();
        this.router = game.getRouter();
        this.stage = new Stage(new FitViewport(BaseScreen.WIDTH, BaseScreen.HEIGHT));
        this.toasts = new Toasts(skin, stage);
        this.pop = new views.multiplayer.ReactionPop(stage, skin, art, game.getAnimations());
    }

    @Override
    public void show() {
        session = DuelRules.newSession(app.getCurrentUser(), System.nanoTime());
        rules = new DuelRules(session);
        roster = new ArrayList<>(rules.roster());
        app.setCurrentGameSession(session);
        Lawn.configure(art.chapterBackground(null));
        buildStage();
        installInput();
        refreshTrays();
    }

    private void buildStage() {
        Image underlay = new Image(art.chapterBackground(null));
        underlay.setScaling(Scaling.fill);
        underlay.setFillParent(true);
        underlay.setColor(0.28f, 0.3f, 0.32f, 1f);
        stage.addActor(underlay);
        Image backdrop = new Image(art.chapterBackground(null));
        backdrop.setScaling(Scaling.fit);
        backdrop.setFillParent(true);
        stage.addActor(backdrop);
        lawnView = new LawnView(session, art, skin, game.getAnimations());
        stage.addActor(lawnView);
        stage.addActor(buildHud());
        stage.addActor(buildTrays());
        stage.addActor(buildStickers());
        lawnView.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onMouse(Lawn.left() + x, Lawn.bottom() + y);
            }
        });
    }

    private Table buildHud() {
        Table bar = new Table(skin);
        bar.setFillParent(true);
        bar.top();
        Table strip = new Table(skin);
        strip.setBackground(skin.getDrawable("shade"));
        strip.pad(6f, 16f, 6f, 16f);
        plantSun = Ui.label(skin, "0", "title");
        strip.add(Ui.iconCell(art.ui("image_ui_hud_ingame_sun"), 30f)).padRight(5f);
        strip.add(plantSun).width(90f).left();
        strip.add(Ui.label(skin, "mouse", "muted")).padRight(18f);
        zombieSun = Ui.label(skin, "0", "title");
        strip.add(Ui.iconCell(art.zombie(ZombieType.NORMAL), 30f)).padRight(5f);
        strip.add(zombieSun).width(90f).left();
        cursorLabel = Ui.label(skin, "", "muted");
        strip.add(cursorLabel).growX().left();
        clock = Ui.label(skin, "2:00", "title");
        strip.add(clock).right().padRight(12f);
        strip.add(Ui.button(skin, "Quit", "small-brown", this::leave)).width(110f).height(34f);
        bar.add(strip).growX();
        return bar;
    }

    private Table buildTrays() {
        Table holder = new Table();
        holder.setFillParent(true);
        holder.padTop(78f).padBottom(6f).padLeft(4f).padRight(4f);
        holder.add(plantTray).left().top().expand();
        holder.add(zombieTray).right().top().expand();
        return holder;
    }

    private Table buildStickers() {
        Table holder = new Table();
        holder.setFillParent(true);
        holder.bottom().padBottom(6f);
        Table bar = new Table(skin);
        bar.setBackground(skin.getDrawable("panel"));
        bar.pad(4f, 10f, 4f, 10f);
        bar.add(Ui.label(skin, "P1 stickers", "muted")).padRight(8f);
        for (int i = 0; i < net.Reactions.stickers().length; i++) {
            final int index = i;
            Table chip = new Table(skin);
            chip.setBackground(skin.getDrawable("highlight"));
            chip.pad(2f, 6f, 2f, 6f);
            chip.add(Ui.iconCell(
                    views.multiplayer.ReactionArt.sticker(art, i), 26f));
            Ui.hoverLift(chip, 1.06f);
            Ui.onClick(chip, () -> sendSticker(index, true));
            bar.add(chip).size(52f, 34f).padRight(3f);
        }
        bar.add(Ui.label(skin, "P2 presses 1 2 3", "muted")).padLeft(12f);
        holder.add(bar);
        return holder;
    }

    private void installInput() {
        InputMultiplexer multiplexer = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (views.ui.Display.handleKey(keycode, app)) {
                    return true;
                }
                return onKey(keycode);
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    private boolean onKey(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            leave();
            return true;
        }
        if (keycode >= Input.Keys.NUM_1
                && keycode < Input.Keys.NUM_1 + net.Reactions.stickers().length) {
            sendSticker(keycode - Input.Keys.NUM_1, false);
            return true;
        }
        return cycleZombie(keycode) || moveCursor(keycode) || dropZombie(keycode);
    }

    private boolean cycleZombie(int keycode) {
        int step;
        if (keycode == Input.Keys.TAB || keycode == Input.Keys.DOWN) {
            step = 1;
        } else if (keycode == Input.Keys.UP) {
            step = -1;
        } else {
            return false;
        }
        if (!roster.isEmpty()) {
            chosenZombie = (chosenZombie + step + roster.size()) % roster.size();
            refreshTrays();
        }
        return true;
    }

    private void sendSticker(int index, boolean fromPlants) {
        pop.show(fromPlants ? "Player 1" : "Player 2", net.Reactions.STICKER, index,
                fromPlants);
        game.getAudio().play(views.assets.Audio.CHIME);
    }

    private boolean moveCursor(int keycode) {
        if (keycode == Input.Keys.W) {
            cursorRow = Math.max(1, cursorRow - 1);
        } else if (keycode == Input.Keys.S) {
            cursorRow = Math.min(GameSession.ROWS, cursorRow + 1);
        } else if (keycode == Input.Keys.A) {
            cursorColumn = Math.max(DuelRules.FIRST_ZOMBIE_COLUMN, cursorColumn - 1);
        } else if (keycode == Input.Keys.D) {
            cursorColumn = Math.min(GameSession.COLS, cursorColumn + 1);
        } else {
            return false;
        }
        return true;
    }

    private boolean dropZombie(int keycode) {
        if (keycode != Input.Keys.SPACE && keycode != Input.Keys.ENTER) {
            return false;
        }
        if (chosenZombie < 0 || chosenZombie >= roster.size()) {
            return true;
        }
        Result placed = rules.placeZombie(roster.get(chosenZombie).getName(),
                cursorColumn, cursorRow);
        if (!placed.isSuccessfull()) {
            toasts.show(placed);
        }
        refreshTrays();
        return true;
    }

    private void onMouse(float stageX, float stageY) {
        if (catchSun(stageX, stageY)) {
            return;
        }
        int column = Lawn.columnAt(stageX);
        int row = Lawn.rowAt(stageY);
        if (column < 1 || row < 1 || chosenPlant == null) {
            return;
        }
        Result planted = session.plantAt(chosenPlant.getName(), column, row);
        if (!planted.isSuccessfull()) {
            toasts.show(planted);
        }
        refreshTrays();
    }

    private boolean catchSun(float stageX, float stageY) {
        models.game.Sun sun = lawnView.sunAt(stageX, stageY);
        if (sun == null) {
            return false;
        }
        if (session.collectSun(sun.getX(), sun.getY()).isSuccessfull()) {
            game.getAudio().play(views.assets.Audio.SUN);
            return true;
        }
        return false;
    }

    private void refreshTrays() {
        plantTray.clear();
        plantSlots.clear();
        for (PlantType type : DuelRules.PLANTS) {
            Table card = card(art.plant(type), type.getName(), type.getCost(),
                    type == chosenPlant, () -> pickPlant(type));
            plantSlots.add(card);
            plantTray.add(card).size(132f, 70f).padBottom(2f).row();
        }
        zombieTray.clear();
        zombieSlots.clear();
        for (int i = 0; i < roster.size(); i++) {
            ZombieType type = roster.get(i);
            final int index = i;
            Table card = card(art.zombie(type), type.getName(),
                    type.getWaveCost(), i == chosenZombie, () -> pickZombie(index));
            zombieSlots.add(card);
            zombieTray.add(card).size(158f, 70f).padBottom(2f).row();
        }
    }

    private void pickPlant(PlantType type) {
        chosenPlant = type == chosenPlant ? null : type;
        lawnView.setGhost(chosenPlant);
        refreshTrays();
    }

    private void pickZombie(int index) {
        chosenZombie = index;
        refreshTrays();
    }

    private Table card(TextureRegion icon, String name, int cost, boolean chosen,
                       Runnable action) {
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable(chosen ? "highlight" : "card"));
        card.pad(2f, 6f, 2f, 6f);
        if (icon != null) {
            card.add(Ui.iconCell(icon, 34f)).padRight(5f);
        }
        Table text = new Table();
        text.add(Ui.label(skin, name, "muted")).left().row();
        text.add(Ui.label(skin, String.valueOf(cost), "gold")).left();
        card.add(text).growX().left();
        Ui.hoverLift(card, 1.04f);
        Ui.onClick(card, action);
        return card;
    }

    private void leave() {
        app.setCurrentGameSession(null);
        router.go(ScreenId.MULTIPLAYER);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1f);
        game.getAnimations().update();
        advance(delta);
        stage.act(delta);
        stage.draw();
    }

    private void advance(float delta) {
        if (overlay != null) {
            return;
        }
        accumulator += delta;
        int ticks = 0;
        while (accumulator >= SECONDS_PER_TICK && ticks < MAX_TICKS_PER_FRAME) {
            accumulator -= SECONDS_PER_TICK;
            ticks++;
            rules.tick();
            if (finished()) {
                return;
            }
        }
        refreshHud();
    }

    private boolean finished() {
        if (session.isOver() || rules.brainsGone()) {
            showResult("Player 2 wins", "Every brain was eaten.");
            return true;
        }
        if (rules.timeUp()) {
            showResult("Player 1 wins", "The garden held out for the whole round.");
            return true;
        }
        return false;
    }

    private void refreshHud() {
        plantSun.setText(String.valueOf(session.getSunManager().getSunBalance()));
        zombieSun.setText(String.valueOf(rules.getSun()));
        int left = rules.secondsLeft();
        clock.setText(left / 60 + ":" + String.format("%02d", left % 60));
        cursorLabel.setText("P2  tab picks  -  W A S D aims  -  space drops  -  column "
                + cursorColumn + ", lane " + cursorRow);
        lawnView.setSelection(cursorColumn, cursorRow);
        for (int i = 0; i < zombieSlots.size(); i++) {
            zombieSlots.get(i).getColor().a = rules.isReady(roster.get(i)) ? 1f : 0.5f;
        }
        for (int i = 0; i < plantSlots.size(); i++) {
            plantSlots.get(i).getColor().a = session.getSunManager().getSunBalance()
                    >= DuelRules.PLANTS[i].getCost() ? 1f : 0.5f;
        }
        for (String cue : lawnView.drainCues()) {
            game.getAudio().play(cue);
        }
    }

    private void showResult(String title, String reason) {
        if (overlay != null) {
            return;
        }
        game.getAudio().stopMusic();
        game.getAudio().play(views.assets.Audio.WIN);
        Table content = new Table();
        content.add(Ui.wrapped(skin, reason, "muted")).width(380f).padBottom(16f).row();
        content.add(Ui.button(skin, "Back to the lobby", "green", this::leave))
                .width(300f).height(56f);
        overlay = Overlay.open(stage, skin, title, content);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
