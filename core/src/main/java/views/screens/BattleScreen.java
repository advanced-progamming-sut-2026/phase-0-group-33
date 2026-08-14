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
import models.game.GameMode;
import models.game.Sun;
import models.entities.zombie.ZombieType;
import models.progress.level.special.SpecialLevelType;
import models.settings.GamePreferences;
import views.PvzGame;
import views.Router;
import views.ScreenId;
import views.assets.Art;
import views.battle.ConveyorBar;
import views.battle.CursorOverlay;
import views.battle.Dialogue;
import views.battle.Lawn;
import views.battle.Overlay;
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
    private ZombieType pendingZombie;
    private int swapColumn = -1;
    private int swapRow = -1;
    private String seedSignature = "";
    private float waveDelay;
    private int lastWave;
    private int lastPlantFood = -1;
    private int lastCoins = -1;
    private int lastGems = -1;
    private int lastPots = -1;
    private Overlay overlay;
    private Label notice;
    private float noticeTimer;
    private final String levelChapter;
    private final int levelNumber;
    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Table> seedTrayCell;
    private com.badlogic.gdx.scenes.scene2d.ui.Cell<Table> objectiveCell;
    private CursorOverlay cursor;
    private LawnView lawnView;
    private Label sunLabel;
    private Label plantFoodLabel;
    private Label waveLabel;
    private Label objectiveLabel;
    private Table objectivePanel;
    private ProgressBar waveBar;
    private float accumulator;
    private float shake;
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
        this.levelChapter = session == null || session.getLevel() == null ? null
                : session.getLevel().getChapter().getName();
        this.levelNumber = session == null || session.getLevel() == null ? 1
                : session.getLevel().getLevelNumber();
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

        com.badlogic.gdx.graphics.g2d.TextureRegion backgroundRegion =
                art.chapterBackground(chapterName());
        Lawn.configure(backgroundRegion);

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
        cursor = new CursorOverlay(stage, art, lawnView.animator());
        stage.addActor(cursor);
        stage.addActor(buildNotice());
        installLawnInput();
        installInput();
        refreshHud();
        rebuildSeedBar();
        setPaused(true);
        Dialogue.open(stage, skin, game.getAnimations(), openingLines(), this::showObjectives);
    }

    protected LawnView createLawnView() {
        return new LawnView(session, art, skin, game.getAnimations());
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
        if (overlay != null) {
            return;
        }
        openPauseMenu();
    }

    private Table buildNotice() {
        Table holder = new Table();
        holder.setFillParent(true);
        holder.center();
        holder.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        notice = new Label("", skin, "title");
        notice.setColor(views.ui.Palette.BAD);
        notice.getColor().a = 0f;
        holder.add(notice);
        return holder;
    }

    protected void announce(String text) {
        notice.setText(text);
        notice.getColor().a = 1f;
        noticeTimer = 2.4f;
    }

    private void fadeNotice(float delta) {
        if (noticeTimer <= 0f) {
            return;
        }
        noticeTimer -= delta;
        if (noticeTimer < 0.6f) {
            notice.getColor().a = Math.max(0f, noticeTimer / 0.6f);
        }
    }

    private void showObjectives() {
        Table content = new Table();
        for (String line : objectives()) {
            content.add(Ui.label(skin, line, "h2")).left().padBottom(6f).row();
        }
        content.add(Ui.button(skin, "Let's go!", "green", this::closeOverlay))
                .width(240f).height(56f).padTop(14f);
        setPaused(true);
        overlay = Overlay.open(stage, skin, "Level Briefing", content);
    }

    private java.util.List<String> openingLines() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (session.getMode() != GameMode.ADVENTURE) {
            return lines;
        }
        String chapter = levelChapter == null ? "" : levelChapter;
        if (levelNumber == 1) {
            lines.add("Welcome to " + chapter + "! I hope you brought your gardening gloves.");
            lines.add("These zombies are hungry, and your brain is on the menu.");
            lines.add("Plant well and they will never reach the house. Good luck!");
        } else if (session.getLevel() != null && session.getLevel().getSpecialType() != null) {
            lines.add("Careful now, this one plays by its own rules.");
            lines.add("Read the briefing before you plant a single seed.");
        }
        return lines;
    }

    private java.util.List<String> objectives() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (session.getMode() == GameMode.VASEBREAKER) {
            lines.add("Break every vase and survive whatever crawls out.");
            return lines;
        }
        if (session.getMode() == GameMode.WALLNUT_BOWLING) {
            lines.add("Bowl the nuts from behind the red line.");
            return lines;
        }
        if (session.getMode() == GameMode.I_ZOMBIE) {
            lines.add("You command the zombies. Eat every brain to win.");
            return lines;
        }
        if (session.getMode() == GameMode.BEGHOULED) {
            lines.add("Swap neighbouring plants to line up three of a kind.");
            return lines;
        }
        lines.add("Do not let the zombies reach your house.");
        SpecialLevelType special = session.getLevel() == null ? null
                : session.getLevel().getSpecialType();
        if (special == SpecialLevelType.DEAD_LINE) {
            lines.add("No zombie may cross the red line on the lawn.");
        } else if (special == SpecialLevelType.SAVE_OUR_SEEDS) {
            lines.add("Keep the marked Wall-nuts alive.");
        } else if (special == SpecialLevelType.TIMED_WAR) {
            lines.add("Destroy 12 zombies before the timer runs out.");
        } else if (special == SpecialLevelType.LOVE_YOUR_PLANTS) {
            lines.add("Losing 5 plants loses the level.");
        } else if (special == SpecialLevelType.NIGHT_OPS) {
            lines.add("No sun falls at night; grow your own.");
        } else if (special == SpecialLevelType.CONVEYOR_BELT) {
            lines.add("Plants arrive on the belt; you cannot choose them.");
        } else if (special == SpecialLevelType.PLANT_WHAT_YOU_GET) {
            lines.add("Plant freely, then start the wave yourself.");
        } else if (special == SpecialLevelType.LOCKED_PLANTS) {
            lines.add("Some of your plants are locked for this level.");
        }
        lines.add("Waves in this level: " + session.getWaveManager().getTotalWaves());
        return lines;
    }

    private void closeOverlay() {
        if (overlay != null) {
            overlay.close();
            overlay = null;
        }
        setPaused(false);
    }

    private void openPauseMenu() {
        setPaused(true);
        Table content = new Table();
        content.add(Ui.button(skin, "Resume", "green", this::closeOverlay))
                .width(300f).height(56f).padBottom(10f).row();
        content.add(Ui.button(skin, "Restart level", "blue", this::restart))
                .width(300f).height(56f).padBottom(10f).row();
        content.add(Ui.button(skin, "Save and quit", "brown", this::leave))
                .width(300f).height(56f);
        overlay = Overlay.open(stage, skin, "Paused", content);
    }

    private void restart() {
        if (levelChapter == null) {
            leave();
            return;
        }
        app.setCurrentGameSession(null);
        Result result = new controllers.menuControllers.MainController(app)
                .handleEnterChapter(levelChapter, levelNumber);
        if (!result.isSuccessfull()) {
            leave();
            return;
        }
        router.go(app.getCurrentGameSession().getPhase() == models.game.GamePhase.BATTLE
                ? ScreenId.BATTLE : ScreenId.SEED_SELECT);
    }

    protected void leave() {
        boolean minigame = session != null && session.getLevel() == null;
        app.setCurrentGameSession(null);
        router.go(minigame ? ScreenId.QUESTS : ScreenId.ADVENTURE);
    }

    private void startWaveManually() {
        toasts.show(session.startZombieWaves());
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
        objectiveCell = root.add(buildObjectivePanel()).left().padLeft(14f);
        root.row();
        seedTrayCell = root.add(buildSeedTray()).growX().height(showsSeedTray() ? 123f : 0f);
        root.row();
        root.add().expand().row();
        root.add(buildToolBar()).growX().height(66f);
        return root;
    }

    private Table buildObjectivePanel() {
        objectivePanel = new Table(skin);
        objectivePanel.setBackground(skin.getDrawable("panel"));
        objectivePanel.pad(4f, 12f, 4f, 12f);
        objectiveLabel = new Label("", skin, "small");
        objectivePanel.add(objectiveLabel);
        return objectivePanel;
    }

    private String objectiveStatus() {
        if (session.getMode() == GameMode.VASEBREAKER) {
            return "Vases left: " + session.getMinigameManager().getVases().size();
        }
        if (session.getMode() == GameMode.I_ZOMBIE) {
            int brains = 0;
            for (int row = 1; row <= GameSession.ROWS; row++) {
                if (session.hasBrain(row)) {
                    brains++;
                }
            }
            return "Brains left: " + brains;
        }
        if (session.getMode() == GameMode.BEGHOULED) {
            return "Combos: " + session.getMinigameManager().getCombosMade()
                    + " / " + session.getMinigameManager().getCombosNeeded();
        }
        if (session.isSpecial(SpecialLevelType.TIMED_WAR)) {
            int seconds = Math.max(0, session.getTimerTicksLeft()) / GameSession.TICKS_PER_SECOND;
            return "Kills " + session.getZombiesKilled() + " / 12    Time left: " + seconds + "s";
        }
        if (session.isSpecial(SpecialLevelType.LOVE_YOUR_PLANTS)) {
            return "Plants lost: " + session.getPlantsLost() + " / 5";
        }
        if (session.isSpecial(SpecialLevelType.SAVE_OUR_SEEDS)) {
            int guarded = 0;
            for (models.game.PlacedPlant plant : session.getPlants()) {
                if (plant.isProtectedSeed()) {
                    guarded++;
                }
            }
            return "Protected plants: " + guarded;
        }
        if (session.isSpecial(SpecialLevelType.DEAD_LINE)) {
            return "Hold the red line";
        }
        return null;
    }

    private Table buildSeedTray() {
        Table tray = new Table(skin);
        tray.setBackground(skin.getDrawable("panel"));
        tray.pad(3f, 12f, 3f, 12f);
        if (session.getMode() == GameMode.I_ZOMBIE) {
            tray.add(buildZombieTray()).left().expandX();
        } else if (usesConveyor()) {
            ConveyorBar belt = new ConveyorBar(session, art, lawnView.animator(),
                    type -> selectTool(Tool.PLANT, type));
            tray.add(belt).growX().height(112f);
        } else {
            tray.add(seedBar).left().expandX();
        }
        return tray;
    }

    private boolean usesConveyor() {
        return session.isSpecial(SpecialLevelType.CONVEYOR_BELT)
                || session.getMode() == GameMode.WALLNUT_BOWLING;
    }

    private boolean showsSeedTray() {
        return session.getMode() == GameMode.I_ZOMBIE || usesConveyor()
                || !session.getSlots().isEmpty();
    }

    private Table buildToolBar() {
        Table tray = new Table(skin);
        tray.setBackground(skin.getDrawable("panel"));
        tray.pad(6f, 12f, 6f, 12f);
        tray.add().expandX();

        if (session.isSpecial(SpecialLevelType.PLANT_WHAT_YOU_GET)
                && !session.getWaveManager().isStarted()) {
            tray.add(Ui.button(skin, "Start the wave", "green", this::startWaveManually))
                    .width(200f).height(46f).padRight(8f);
        }
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
                    .cost(slot.isSingleUse() ? 0 : session.effectiveCost(slot.getType()))
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
        if (session.getMode() == GameMode.I_ZOMBIE || usesConveyor()) {
            return;
        }
        if (seedTrayCell != null) {
            float wanted = showsSeedTray() ? 123f : 0f;
            if (seedTrayCell.getMinHeight() != wanted) {
                seedTrayCell.height(wanted);
                seedTrayCell.getTable().invalidateHierarchy();
            }
        }
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
                    .affordable(slot.isSingleUse() || sun >= session.effectiveCost(slot.getType()))
                    .armed(tool == Tool.PLANT && pending == slot.getType());
        }
    }

    private void installLawnInput() {
        lawnView.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                int column = Lawn.columnAt(Lawn.left() + x);
                int row = Lawn.rowAt(Lawn.bottom() + y);
                if (column >= 1 && row >= 1) {
                    useTool(column, row);
                }
            }
        });
    }

    private void useTool(int column, int row) {
        if (session.getMode() == GameMode.VASEBREAKER && tool == Tool.NONE) {
            Result broken = controller.handleBreakVase(column, row);
            if (broken.isSuccessfull() || !hasVases()) {
                toasts.show(broken);
                return;
            }
        }
        if (session.getMode() == GameMode.I_ZOMBIE) {
            placeZombie(column, row);
            return;
        }
        if (session.getMode() == GameMode.BEGHOULED) {
            swapAt(column, row);
            return;
        }
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

    private boolean hasVases() {
        return !session.getMinigameManager().getVases().isEmpty();
    }

    private void placeZombie(int column, int row) {
        if (pendingZombie == null) {
            toasts.error("Pick a zombie from the tray first.");
            return;
        }
        toasts.show(controller.handlePlaceZombie(pendingZombie.getName(), column, row));
    }

    private void swapAt(int column, int row) {
        if (swapColumn < 0) {
            swapColumn = column;
            swapRow = row;
            lawnView().setSelection(column, row);
            return;
        }
        Result result = controller.handleSwap(swapColumn, swapRow, column, row);
        toasts.show(result);
        swapColumn = -1;
        swapRow = -1;
        lawnView().setSelection(-1, -1);
    }

    private Table buildZombieTray() {
        Table tray = new Table();
        for (final ZombieType type : session.getMinigameManager().getIzombieTypes()) {
            Table card = new Table(skin);
            card.setBackground(skin.getDrawable("card"));
            card.pad(4f);
            card.add(Ui.iconCell(art.zombie(type), 46f)).row();
            card.add(Ui.label(skin, type.getName(), "tiny")).width(88f).row();
            Table price = new Table();
            price.add(Ui.iconCell(art.ui("image_ui_hud_ingame_sun"), 18f)).padRight(3f);
            price.add(Ui.label(skin, String.valueOf(type.getWaveCost()), "small"));
            card.add(price);
            Ui.hoverLift(card, 1.05f);
            Ui.onClick(card, () -> {
                pendingZombie = type;
                toasts.success(type.getName() + " ready; click a tile right of the line.");
            });
            tray.add(card).size(96f, 116f).padRight(5f);
        }
        return tray;
    }

    private void updateHover() {
        com.badlogic.gdx.math.Vector2 point = stage.screenToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
        int column = Lawn.columnAt(point.x);
        int row = Lawn.rowAt(point.y);
        if (column < 1 || row < 1) {
            lawnView.clearHover();
            updateCursor();
            return;
        }
        lawnView.setHover(tool == Tool.NONE ? -1 : column, tool == Tool.NONE ? -1 : row);
        lawnView.setGhost(tool == Tool.PLANT ? pending : null);
        lawnView.setHoverValid(tool != Tool.PLANT || canPlantAt(column, row));
        updateCursor();
        collectSunUnder(column, row);
    }

    private void updateCursor() {
        if (cursor == null) {
            return;
        }
        switch (tool) {
            case PLANT:
                cursor.carryPlant(pending);
                break;
            case SHOVEL:
                cursor.carryIcon("image_ui_hud_ingame_shovel_icon");
                break;
            case FOOD:
                cursor.carryIcon("image_ui_hud_ingame_plantfood_button");
                break;
            default:
                cursor.carryNothing();
                break;
        }
    }

    private boolean canPlantAt(int column, int row) {
        if (pending == null) {
            return false;
        }
        if (session.plantAt(column, row) != null) {
            return false;
        }
        models.game.PlantSlot slot = session.findSlot(pending);
        if (slot == null || !slot.isReady()) {
            return false;
        }
        return slot.isSingleUse()
                || session.getSunManager().getSunBalance() >= session.effectiveCost(pending);
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
                || session.getMode() == GameMode.VASEBREAKER
                || session.getMode() == GameMode.I_ZOMBIE
                || session.getMode() == GameMode.BEGHOULED
                || session.isSpecial(SpecialLevelType.PLANT_WHAT_YOU_GET)) {
            return;
        }
        waveDelay += SECONDS_PER_TICK;
        if (waveDelay >= 3f) {
            toasts.show(session.startZombieWaves());
        }
    }

    protected void refreshHud() {
        String status = objectiveStatus();
        objectivePanel.setVisible(status != null);
        objectiveLabel.setText(status == null ? "" : status);
        if (objectiveCell != null) {
            float wanted = status == null ? 0f : 34f;
            if (objectiveCell.getMinHeight() != wanted) {
                objectiveCell.height(wanted);
                objectiveCell.getTable().invalidateHierarchy();
            }
        }
        sunLabel.setText(String.valueOf(session.getSunManager().getSunBalance()));
        plantFoodLabel.setText(String.valueOf(session.getPlantFoods()));
        int wave = session.getWaveManager().getCurrentWave();
        int total = session.getWaveManager().getTotalWaves();
        waveLabel.setText(wave == 0 ? "The horde is coming" : "Wave " + wave + " of " + total);
        waveBar.setValue(total == 0 ? 0f : Math.min(1f, wave / (float) total));
    }

    protected void onTick() {
        autoStartWaves();
        trackRewards();
        int wave = session.getWaveManager().getCurrentWave();
        if (wave != lastWave) {
            lastWave = wave;
            announce(waveNotice(wave));
        }
    }

    private String waveNotice(int wave) {
        String chapter = levelChapter == null ? ""
                : levelChapter.replaceAll("[^A-Za-z]", "").toLowerCase();
        if ("darkages".equals(chapter)) {
            return "Necromancy! The graves are waking up!";
        }
        if ("waveybeach".equals(chapter)) {
            return "The tide is out; zombies are surfacing!";
        }
        return wave >= session.getWaveManager().getTotalWaves()
                ? "The final wave is here!" : "A huge wave of zombies is approaching!";
    }

    private void trackRewards() {
        int food = session.getPlantFoods();
        if (lastPlantFood >= 0 && food > lastPlantFood) {
            toasts.success("You picked up plant food.");
        }
        lastPlantFood = food;
        if (app.getCurrentUser() == null) {
            return;
        }
        int coins = app.getCurrentUser().getCoins().getAmount();
        int gems = app.getCurrentUser().getDiamonds().getAmount();
        int pots = app.getCurrentUser().getPots().getAmount();
        if (lastCoins >= 0 && coins > lastCoins) {
            toasts.success("A zombie dropped " + (coins - lastCoins) + " coins.");
        }
        if (lastGems >= 0 && gems > lastGems) {
            toasts.success("A zombie dropped a diamond.");
        }
        if (lastPots >= 0 && pots > lastPots) {
            toasts.success("A zombie dropped a pot.");
        }
        lastCoins = coins;
        lastGems = gems;
        lastPots = pots;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1f);
        if (session != null && !paused && !session.isOver()) {
            advance(delta);
        }
        if (session != null && session.isOver() && overlay == null) {
            onGameOver();
        }
        game.getAnimations().update();
        if (session != null) {
            lawnView.setFrozen(paused || session.isOver());
            if (lawnView.consumeShake()) {
                shake = 0.32f;
            }
            applyShake(delta);
            refreshHud();
            refreshSeedBar();
            updateHover();
            fadeNotice(delta);
        }
        stage.act(delta);
        stage.draw();
    }

    private void applyShake(float delta) {
        com.badlogic.gdx.graphics.Camera camera = stage.getViewport().getCamera();
        if (shake <= 0f) {
            camera.position.set(BaseScreen.WIDTH / 2f, BaseScreen.HEIGHT / 2f, 0f);
            camera.update();
            return;
        }
        shake = Math.max(0f, shake - delta);
        float power = shake * 26f;
        camera.position.set(BaseScreen.WIDTH / 2f + com.badlogic.gdx.math.MathUtils.random(-power, power),
                BaseScreen.HEIGHT / 2f + com.badlogic.gdx.math.MathUtils.random(-power, power), 0f);
        camera.update();
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
        if (overlay != null) {
            return;
        }
        boolean won = session.getPhase() == models.game.GamePhase.WON;
        setPaused(true);
        controller.handleAdvanceTime(0);

        Table content = new Table();
        content.add(Ui.label(skin, won ? "The lawn is safe." : "The zombies ate your brains.",
                "h2")).padBottom(14f).row();
        if (!won) {
            content.add(Ui.button(skin, "Try again", "green", this::restart))
                    .width(300f).height(56f).padBottom(10f).row();
        }
        content.add(Ui.button(skin, "Back to the map", "brown", this::leave))
                .width(300f).height(56f);
        overlay = Overlay.open(stage, skin, won ? "Victory" : "Defeat", content);
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
