package views.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.ScreenAdapter;
import models.App;
import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.GameSetup;
import models.game.Names;
import net.MatchSnapshot;
import net.Online;
import net.Packet;
import net.Protocol;
import views.PvzGame;
import views.Router;
import views.ScreenId;
import views.assets.Art;
import views.battle.Lawn;
import views.battle.LawnView;
import views.battle.Overlay;
import views.multiplayer.ReactionBar;
import views.multiplayer.ReactionPop;
import views.ui.BaseScreen;
import views.ui.Toasts;
import views.ui.Ui;

import java.util.ArrayList;
import java.util.List;

public class DuelScreen extends ScreenAdapter {

    private final PvzGame game;
    private final App app;
    private final Skin skin;
    private final Art art;
    private final Router router;
    private final Stage stage;
    private final Toasts toasts;
    private final ReactionPop pop;

    private final Table tray = new Table();
    private final List<Table> slots = new ArrayList<>();
    private final List<String> roster = new ArrayList<>();

    private GameSession shell;
    private LawnView lawnView;
    private Label clock;
    private Label sunLabel;
    private Label roleLabel;
    private Overlay overlay;
    private String role = Protocol.ROLE_PLANTS;
    private String opponent = "";
    private String chosen;
    private int mySun;
    private int secondsLeft;
    private List<Integer> costs = new ArrayList<>();
    private List<Integer> cooling = new ArrayList<>();

    public DuelScreen(PvzGame game) {
        this.game = game;
        this.app = game.getApp();
        this.skin = game.getSkin();
        this.art = game.getArt();
        this.router = game.getRouter();
        this.stage = new Stage(new FitViewport(BaseScreen.WIDTH, BaseScreen.HEIGHT));
        this.toasts = new Toasts(skin, stage);
        this.pop = new ReactionPop(stage, skin, art, game.getAnimations());
    }

    @Override
    public void show() {
        readStart();
        buildShell();
        buildStage();
        installInput();
        refreshTray();
    }

    private void readStart() {
        Packet start = Online.get().matchStart();
        if (start == null) {
            return;
        }
        role = start.str("role", Protocol.ROLE_PLANTS);
        opponent = start.str("opponent", "your rival");
        secondsLeft = start.num("seconds", 120);
        roster.clear();
        roster.addAll(isZombieSide() ? start.list("roster") : start.list("seeds"));
        costs = numbers(start.list("costs"));
        cooling = new ArrayList<>();
        for (int i = 0; i < roster.size(); i++) {
            cooling.add(0);
        }
    }

    private static List<Integer> numbers(List<String> raw) {
        List<Integer> values = new ArrayList<>();
        for (String item : raw) {
            try {
                values.add(Integer.parseInt(item));
            } catch (NumberFormatException e) {
                values.add(0);
            }
        }
        return values;
    }

    private boolean isZombieSide() {
        return Protocol.ROLE_ZOMBIES.equals(role);
    }

    private void buildShell() {
        List<String> plants = new ArrayList<>();
        for (PlantType type : PlantType.values()) {
            plants.add(type.getName());
        }
        shell = new GameSession(GameSetup.duel(app.getCurrentUser(), plants, 1));
        for (String name : roster) {
            if (!isZombieSide()) {
                shell.addPlantToSelection(name);
            }
        }
        if (isZombieSide()) {
            shell.addPlantToSelection(PlantType.SUNFLOWER.getName());
        }
        shell.startGame();
        shell.getPlants().clear();
        shell.getZombies().clear();
        app.setCurrentGameSession(shell);
    }

    private void buildStage() {
        Lawn.configure(art.chapterBackground(null));
        Image underlay = new Image(art.chapterBackground(null));
        underlay.setScaling(Scaling.fill);
        underlay.setFillParent(true);
        underlay.setColor(0.28f, 0.3f, 0.32f, 1f);
        stage.addActor(underlay);
        Image backdrop = new Image(art.chapterBackground(null));
        backdrop.setScaling(Scaling.fit);
        backdrop.setFillParent(true);
        stage.addActor(backdrop);

        lawnView = new LawnView(shell, art, skin, game.getAnimations());
        lawnView.setFrozen(false);
        stage.addActor(lawnView);
        stage.addActor(buildHud());
        stage.addActor(buildTray());
        stage.addActor(buildFooter());
        lawnView.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onLawnClick(Lawn.left() + x, Lawn.bottom() + y);
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
        sunLabel = Ui.label(skin, "0", "title");
        strip.add(Ui.iconCell(art.ui("image_ui_hud_ingame_sun"), 34f)).padRight(6f);
        strip.add(sunLabel).width(110f).left();
        roleLabel = Ui.label(skin, "", "h2");
        strip.add(roleLabel).growX().left().padLeft(14f);
        clock = Ui.label(skin, "2:00", "title");
        strip.add(clock).right().padRight(12f);
        strip.add(Ui.button(skin, "Give up", "small-brown", this::giveUp)).width(130f).height(36f);
        bar.add(strip).growX();
        refreshRole();
        return bar;
    }

    private Table buildTray() {
        Table holder = new Table();
        holder.setFillParent(true);
        holder.left().top().padTop(66f);
        holder.add(tray).left();
        return holder;
    }

    private Table buildFooter() {
        Table holder = new Table();
        holder.setFillParent(true);
        holder.bottom().padBottom(8f);
        holder.add(new ReactionBar(skin, art, this::react));
        return holder;
    }

    private void refreshRole() {
        roleLabel.setText(isZombieSide()
                ? "You raise the horde against " + opponent + " - eat all five brains"
                : "You grow the garden against " + opponent + " - hold out to the buzzer");
    }

    private void installInput() {
        InputMultiplexer multiplexer = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (views.ui.Display.handleKey(keycode, app)) {
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    giveUp();
                    return true;
                }
                return pickWithNumber(keycode);
            }
        });
        com.badlogic.gdx.Gdx.input.setInputProcessor(multiplexer);
    }

    private boolean pickWithNumber(int keycode) {
        int index = keycode - Input.Keys.NUM_1;
        if (index < 0 || index >= roster.size()) {
            return false;
        }
        choose(roster.get(index));
        return true;
    }

    private void refreshTray() {
        tray.clear();
        slots.clear();
        for (int i = 0; i < roster.size(); i++) {
            Table card = slotCard(i);
            slots.add(card);
            tray.add(card).size(104f, 92f).padBottom(3f).row();
        }
    }

    private Table slotCard(final int index) {
        final String name = roster.get(index);
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable(name.equals(chosen) ? "highlight" : "card"));
        card.pad(3f);
        TextureRegion icon = iconFor(name);
        if (icon != null) {
            card.add(Ui.iconCell(icon, 42f)).row();
        }
        card.add(Ui.label(skin, name, "muted")).row();
        card.add(Ui.label(skin, String.valueOf(costOf(name)), "gold"));
        Ui.hoverLift(card, 1.04f);
        Ui.onClick(card, () -> choose(name));
        return card;
    }

    private TextureRegion iconFor(String name) {
        if (isZombieSide()) {
            ZombieType type = Names.zombie(name);
            return type == null ? null : art.zombie(type);
        }
        PlantType type = Names.plant(name);
        return type == null ? null : art.plant(type);
    }

    private int costOf(String name) {
        if (!isZombieSide()) {
            PlantType type = Names.plant(name);
            return type == null ? 0 : type.getCost();
        }
        int index = roster.indexOf(name);
        return index >= 0 && index < costs.size() ? costs.get(index) : 0;
    }

    private void choose(String name) {
        chosen = name.equals(chosen) ? null : name;
        refreshTray();
        if (!isZombieSide()) {
            lawnView.setGhost(chosen == null ? null : Names.plant(chosen));
        } else {
            lawnView.setZombieGhost(chosen == null ? null : Names.zombie(chosen));
        }
    }

    private void onLawnClick(float stageX, float stageY) {
        if (!isZombieSide() && chosen == null && catchSun(stageX, stageY)) {
            return;
        }
        int column = Lawn.columnAt(stageX);
        int row = Lawn.rowAt(stageY);
        if (column < 1 || row < 1 || chosen == null) {
            return;
        }
        Online.get().intent(Packet.of(Protocol.MATCH_INTENT)
                .put("what", isZombieSide() ? Protocol.INTENT_ZOMBIE : Protocol.INTENT_PLANT)
                .put("type", chosen).put("x", column).put("y", row));
    }

    private boolean catchSun(float stageX, float stageY) {
        models.game.Sun sun = lawnView.sunAt(stageX, stageY);
        if (sun == null) {
            return false;
        }
        Online.get().intent(Packet.of(Protocol.MATCH_INTENT)
                .put("what", Protocol.INTENT_SUN).put("x", sun.getX()).put("y", sun.getY()));
        game.getAudio().play(views.assets.Audio.SUN);
        return true;
    }

    private void react(String kind, int index) {
        Online.get().react(kind, index);
        toasts.show(Result.ok("Sent: " + ReactionBar.hint(kind, index)));
    }

    private void giveUp() {
        if (overlay != null) {
            return;
        }
        Online.get().leaveMatch();
        leave();
    }

    private void leave() {
        Online.get().clearMatch();
        app.setCurrentGameSession(null);
        router.go(ScreenId.MULTIPLAYER);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1f);
        game.getAnimations().update();
        drain();
        applyState();
        stage.act(delta);
        stage.draw();
    }

    private void applyState() {
        Packet state = Online.get().takeState();
        if (state == null) {
            return;
        }
        MatchSnapshot.apply(shell, state);
        mySun = state.num("mine", mySun);
        secondsLeft = state.num("left", secondsLeft);
        cooling = numbers(state.list("cool"));
        sunLabel.setText(String.valueOf(mySun));
        clock.setText(secondsLeft / 60 + ":" + String.format("%02d", secondsLeft % 60));
        for (String cue : lawnView.drainCues()) {
            game.getAudio().play(cue);
        }
        refreshCooling();
    }

    private void refreshCooling() {
        for (int i = 0; i < slots.size() && i < cooling.size(); i++) {
            boolean ready = cooling.get(i) <= 0 && costOf(roster.get(i)) <= mySun;
            slots.get(i).getColor().a = ready ? 1f : 0.5f;
        }
    }

    private void drain() {
        Packet event = Online.get().nextMatchEvent();
        while (event != null) {
            if (Protocol.MATCH_OVER.equals(event.type())) {
                showResult(event);
            } else if (Protocol.REACTION_IN.equals(event.type())) {
                pop.show(event.str("from", opponent), event.str("kind"), event.num("index", 0));
            } else if (Protocol.MESSAGE.equals(event.type())) {
                toasts.show(Result.fail(event.str(Protocol.MESSAGE)));
            }
            event = Online.get().nextMatchEvent();
        }
    }

    private void showResult(Packet event) {
        if (overlay != null) {
            return;
        }
        boolean won = role.equals(event.str("winner"));
        game.getAudio().play(won ? views.assets.Audio.WIN : views.assets.Audio.LOSE);
        Table content = new Table();
        content.add(Ui.label(skin, won ? "You take the round." : opponent + " takes the round.",
                "h2")).padBottom(8f).row();
        content.add(Ui.wrapped(skin, event.str("reason"), "muted")).width(380f).padBottom(16f).row();
        content.add(Ui.button(skin, "Back to the lobby", "green", this::leave))
                .width(300f).height(56f);
        overlay = Overlay.open(stage, skin, won ? "Victory" : "Defeat", content);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        Lawn.configure(art.chapterBackground(null));
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
