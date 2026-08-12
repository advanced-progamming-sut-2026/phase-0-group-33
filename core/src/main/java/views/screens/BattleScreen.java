package views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import controllers.menuControllers.GameController;
import models.App;
import models.Result;
import models.entities.plant.PlantType;
import models.game.GameSession;
import models.game.PlantSlot;
import models.game.Sun;
import models.progress.level.special.SpecialLevelType;
import models.settings.GamePreferences;
import views.PvzGame;
import views.Router;
import views.ScreenId;
import views.assets.Art;
import views.battle.Lawn;
import views.battle.LawnView;
import views.ui.BaseScreen;
import views.ui.SeedPacket;
import views.ui.Toasts;
import views.ui.Ui;

public class BattleScreen extends ScreenAdapter {

    private static final float SECONDS_PER_TICK = 1f / GameSession.TICKS_PER_SECOND;
    private static final int MAX_TICKS_PER_FRAME = 6;

    protected final PvzGame game;
    protected final App app;
    protected final Skin skin;
    protected final Art art;
    protected final Router router;
    protected final Stage stage;
    protected final Toasts toasts;
    protected final GameController controller;
    protected final GameSession session;

    private final Table seedBar = new Table();
    private final java.util.List<SeedPacket> packets = new java.util.ArrayList<>();
    private Tool tool = Tool.NONE;
    private PlantType pending;
    private String seedSignature = "";
    private float waveDelay;
    private LawnView lawnView;
    private Label sunLabel;
    private Label plantFoodLabel;
    private Label waveLabel;
    private ProgressBar waveBar;
    private float accumulator;
    private boolean paused;

    public BattleScreen(PvzGame game) {
        this.game = game;
        this.app = game.getApp();
        this.skin = game.getSkin();
        this.art = game.getArt();
        this.router = game.getRouter();
        this.stage = new Stage(new FitViewport(BaseScreen.WIDTH, BaseScreen.HEIGHT));
        this.toasts = new Toasts(skin, stage);
        this.controller = new GameController(game.getApp());
        this.session = game.getApp().getCurrentGameSession();
    }

    protected boolean isPaused() {
        return paused;
    }

    protected void setPaused(boolean value) {
        this.paused = value;
    }

    protected LawnView lawnView() {
        return lawnView;
    }

    protected enum Tool {
        NONE,
        PLANT,
        SHOVEL,
        FOOD
    }

    @Override
    public void show() {
        if (session == null) {
            router.go(ScreenId.ADVENTURE);
            return;
        }

        Image underlay = new Image(art.chapterBackground(chapterName()));
        underlay.setScaling(Scaling.fill);
        underlay.setFillParent(true);
        underlay.setColor(0.28f, 0.3f, 0.32f, 1f);
        stage.addActor(underlay);

        Image backdrop = new Image(art.chapterBackground(chapterName()));
        backdrop.setScaling(Scaling.fit);
        backdrop.setFillParent(true);
        stage.addActor(backdrop);

        lawnView = createLawnView();
        lawnView.setShowGrid(GamePreferences.isGridVisible(username()));
        stage.addActor(lawnView);

        stage.addActor(buildHud());
        installLawnInput();
        installInput();
        refreshHud();
        rebuildSeedBar();
    }

    protected LawnView createLawnView() {
        return new LawnView(session, art, skin);
    }

    private void installInput() {
        InputMultiplexer multiplexer = new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    onEscape();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    protected void onEscape() {
        leave();
    }

    protected void leave() {
        app.setCurrentGameSession(null);
        router.go(ScreenId.ADVENTURE);
    }

    private String username() {
        return app.getCurrentUser() == null ? "guest" : app.getCurrentUser().getUsername();
    }

    private String chapterName() {
        return session.getLevel() == null ? null : session.getLevel().getChapter().getName();
    }

    private Table buildHud() {
        Table root = new Table();
        root.setFillParent(true);
        root.top();

        Table bar = new Table(skin);
        bar.setBackground(skin.getDrawable("panel"));
        bar.pad(6f, 16f, 6f, 16f);

        bar.add(Ui.iconCell(art.ui("image_ui_hud_ingame_sun"), 34f)).padRight(6f);
        sunLabel = new Label("0", skin, "h2");
        bar.add(sunLabel).width(90f).left().padRight(20f);

        bar.add(Ui.iconCell(art.ui("image_ui_hud_ingame_plantfood_button"), 32f)).padRight(6f);
        plantFoodLabel = new Label("0", skin, "h2");
        bar.add(plantFoodLabel).width(50f).left().padRight(24f);

        Table waveBox = new Table();
        waveLabel = new Label("", skin, "small");
        waveBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "gold-horizontal");
        waveBox.add(waveLabel).left().row();
        waveBox.add(waveBar).width(320f).height(16f).padTop(2f);
        bar.add(waveBox).padRight(20f);

        bar.add().expandX();
        bar.add(Ui.button(skin, "Menu", "small-brown", this::onEscape)).width(120f).height(46f);

        root.add(bar).growX().height(58f).row();
        root.add(buildSeedTray()).growX().height(123f).row();
        root.add().expand().row();
        root.add(buildToolBar()).growX().height(66f);
        return root;
    }

    private Table buildSeedTray() {
        Table tray = new Table(skin);
        tray.setBackground(skin.getDrawable("panel"));
        tray.pad(3f, 12f, 3f, 12f);
        tray.add(seedBar).left().expandX();
        return tray;
    }

    private Table buildToolBar() {
        Table tray = new Table(skin);
        tray.setBackground(skin.getDrawable("panel"));
        tray.pad(6f, 12f, 6f, 12f);
        tray.add().expandX();

        tray.add(Ui.button(skin, "Shovel", "small-brown",
                () -> selectTool(Tool.SHOVEL, null))).width(120f).height(46f).padLeft(8f);
        tray.add(Ui.button(skin, "Plant Food", "small-purple",
                () -> selectTool(Tool.FOOD, null))).width(150f).height(46f).padLeft(8f);

        if (GamePreferences.isDebugMode(username())) {
            tray.add(Ui.button(skin, "+Sun", "small", () -> {
                toasts.show(controller.handleCheatAddSun(500));
            })).width(96f).height(46f).padLeft(8f);
            tray.add(Ui.button(skin, "+Food", "small", () -> {
                toasts.show(controller.handleCheatAddPlantFood());
            })).width(104f).height(46f).padLeft(8f);
        }
        return tray;
    }

    private void selectTool(Tool next, PlantType type) {
        if (tool == next && pending == type) {
            tool = Tool.NONE;
            pending = null;
            return;
        }
        tool = next;
        pending = type;
    }

    private void rebuildSeedBar() {
        seedBar.clear();
        packets.clear();
        for (final PlantSlot slot : session.getSlots()) {
            SeedPacket packet = new SeedPacket(skin, art, slot.getType())
                    .cost(session.effectiveCost(slot.getType()))
                    .boosted(slot.isBoosted())
                    .onClick(() -> selectTool(Tool.PLANT, slot.getType()));
            packets.add(packet);
            seedBar.add(packet).size(SeedPacket.PACKET_WIDTH, SeedPacket.PACKET_HEIGHT).padRight(5f);
        }
        seedSignature = signature();
    }

    private String signature() {
        StringBuilder builder = new StringBuilder();
        for (PlantSlot slot : session.getSlots()) {
            builder.append(slot.getType().name()).append(slot.isBoosted()).append(',');
        }
        return builder.toString();
    }

    private void refreshSeedBar() {
        if (!signature().equals(seedSignature)) {
            rebuildSeedBar();
        }
        java.util.List<PlantSlot> slots = session.getSlots();
        int sun = session.getSunManager().getSunBalance();
        for (int i = 0; i < packets.size() && i < slots.size(); i++) {
            PlantSlot slot = slots.get(i);
            int recharge = Math.max(1, session.effectiveRecharge(slot.getType())
                    * GameSession.TICKS_PER_SECOND);
            packets.get(i).cooldown(slot.getCooldownTicks() / (float) recharge)
                    .affordable(slot.isSingleUse() || sun >= session.effectiveCost(slot.getType()));
        }
    }

    private void installLawnInput() {
        lawnView.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                int column = Lawn.columnAt(Lawn.LEFT + x);
                int row = Lawn.rowAt(Lawn.BOTTOM + y);
                if (column >= 1 && row >= 1) {
                    useTool(column, row);
                }
            }
        });
    }

    private void useTool(int column, int row) {
        Result result;
        switch (tool) {
            case PLANT:
                result = controller.handlePlant(pending.getName(), column, row);
                break;
            case SHOVEL:
                result = controller.handlePluck(column, row);
                break;
            case FOOD:
                result = controller.handleFeedPlant(column, row);
                break;
            default:
                result = session.collectSun(column, row);
                if (!result.isSuccessfull()) {
                    return;
                }
                break;
        }
        toasts.show(result);
        if (result.isSuccessfull() && tool != Tool.PLANT) {
            tool = Tool.NONE;
            pending = null;
        }
    }

    private void updateHover() {
        com.badlogic.gdx.math.Vector2 point = stage.screenToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
        int column = Lawn.columnAt(point.x);
        int row = Lawn.rowAt(point.y);
        if (column < 1 || row < 1) {
            lawnView.clearHover();
            return;
        }
        lawnView.setHover(tool == Tool.NONE ? -1 : column, tool == Tool.NONE ? -1 : row);
        collectSunUnder(column, row);
    }

    private void collectSunUnder(int column, int row) {
        for (Sun sun : new java.util.ArrayList<>(session.getSunManager().getSuns())) {
            if (!sun.isFalling() && sun.getX() == column && sun.getY() == row) {
                session.collectSun(column, row);
                return;
            }
        }
    }

    private void autoStartWaves() {
        if (session.getWaveManager().isStarted()
                || session.isSpecial(SpecialLevelType.PLANT_WHAT_YOU_GET)) {
            return;
        }
        waveDelay += SECONDS_PER_TICK;
        if (waveDelay >= 3f) {
            toasts.show(session.startZombieWaves());
        }
    }

    protected void refreshHud() {
        sunLabel.setText(String.valueOf(session.getSunManager().getSunBalance()));
        plantFoodLabel.setText(String.valueOf(session.getPlantFoods()));
        int wave = session.getWaveManager().getCurrentWave();
        int total = session.getWaveManager().getTotalWaves();
        waveLabel.setText(wave == 0 ? "The horde is coming" : "Wave " + wave + " of " + total);
        waveBar.setValue(total == 0 ? 0f : Math.min(1f, wave / (float) total));
    }

    protected void onTick() {
        autoStartWaves();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1f);
        if (session != null && !paused && !session.isOver()) {
            advance(delta);
        }
        if (session != null) {
            refreshHud();
            refreshSeedBar();
            updateHover();
        }
        stage.act(delta);
        stage.draw();
    }

    private void advance(float delta) {
        int speed = GamePreferences.getGameSpeed(username());
        accumulator += delta * speed;
        int budget = MAX_TICKS_PER_FRAME;
        while (accumulator >= SECONDS_PER_TICK && budget-- > 0) {
            accumulator -= SECONDS_PER_TICK;
            session.advanceTime(1);
            onTick();
            if (session.isOver()) {
                accumulator = 0f;
                onGameOver();
                return;
            }
        }
        if (accumulator > SECONDS_PER_TICK) {
            accumulator = 0f;
        }
    }

    protected void onGameOver() {
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
