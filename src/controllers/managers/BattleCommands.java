package controllers.managers;

import models.Result;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameMode;
import models.game.GamePhase;
import models.game.GameSession;
import models.game.Names;
import models.game.PlacedPlant;
import models.game.PlantSlot;
import models.game.Sun;
import models.progress.level.special.SpecialLevelType;

import java.util.ArrayList;

public class BattleCommands {
    private static final int TIMED_WAR_KILL_GOAL = 12;

    private final GameSession session;

    public BattleCommands(GameSession session) {
        this.session = session;
    }

    public Result advanceTime(int ticks) {
        if (session.getPhase() == GamePhase.PREPARATION) {
            return Result.fail("Start the game first.");
        }
        if (session.isOver()) {
            return Result.fail("The game is already over.");
        }
        for (int i = 0; i < ticks && !session.isOver(); i++) {
            tickOnce();
        }
        return Result.ok("Time advanced by " + ticks + " ticks (game time: "
                + session.getTickCount() / GameSession.TICKS_PER_SECOND + "s).");
    }

    private void tickOnce() {
        session.incrementTick();
        for (PlantSlot slot : new ArrayList<>(session.getSlots())) {
            slot.tick();
        }
        session.getSunManager().tick();
        if (usesWaves()) {
            session.getWaveManager().tick();
        }
        session.getCombatManager().plantsAct();
        session.getCombatManager().zombiesAct();
        session.getBehaviorManager().tickEnvironment();
        session.getPlantActionManager().tick();
        session.getMinigameManager().tick();
        tickTimer();
        checkVictory();
    }

    private boolean usesWaves() {
        GameMode mode = session.getMode();
        return mode == GameMode.ADVENTURE || mode == GameMode.SCORING
                || mode == GameMode.ZOMBOTANY || mode == GameMode.WALLNUT_BOWLING;
    }

    private void tickTimer() {
        if (!session.isSpecial(SpecialLevelType.TIMED_WAR) || session.getTimerTicksLeft() < 0) {
            return;
        }
        if (session.getZombiesKilled() >= TIMED_WAR_KILL_GOAL) {
            session.setTimerTicksLeft(-1);
            session.winGame();
            return;
        }
        session.setTimerTicksLeft(session.getTimerTicksLeft() - 1);
        if (session.getTimerTicksLeft() == 0) {
            session.loseGame("Time is up! You only killed " + session.getZombiesKilled()
                    + " of " + TIMED_WAR_KILL_GOAL + " zombies.");
        }
    }

    private void checkVictory() {
        if (session.isOver() || !usesWaves() || session.isSpecial(SpecialLevelType.TIMED_WAR)) {
            return;
        }
        if (session.getWaveManager().allWavesCleared()) {
            session.winGame();
        }
    }

    public Result startZombieWaves() {
        if (session.getPhase() != GamePhase.BATTLE) {
            return Result.fail("Start the game first.");
        }
        if (!usesWaves()) {
            return Result.fail("This mode has no zombie waves.");
        }
        if (session.getWaveManager().isStarted()) {
            return Result.fail("The zombie waves have already started.");
        }
        session.getWaveManager().startWaves();
        return Result.ok("The horde is on its way!");
    }

    public Result collectSun(int x, int y) {
        if (session.getPhase() != GamePhase.BATTLE) {
            return Result.fail("There is no running battle.");
        }
        if (x < 1 || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            return Result.fail("Coordinates are outside the lawn.");
        }
        Sun sun = session.getSunManager().collectAt(x, y);
        if (sun == null) {
            return Result.fail("There is no sun to collect at (" + x + ", " + y + ").");
        }
        if (sun.isFalling()) {
            session.getCombatManager().applyRadioactiveExplosion(x, y);
            return Result.ok("The radioactive sun exploded mid-air at (" + x + ", " + y + ")!");
        }
        if (sun.isProducedByPlant()) {
            PlacedPlant producer = session.plantAt(x, y);
            if (producer != null) {
                producer.setSunPending(false);
            }
        }
        return Result.ok("Collected " + sun.getValue() + " sun. Balance: "
                + session.getSunManager().getSunBalance());
    }

    public Result feedPlant(int x, int y) {
        if (session.getPhase() != GamePhase.BATTLE) {
            return Result.fail("There is no running battle.");
        }
        if (session.getPlantFoods() < 1) {
            return Result.fail("You have no plant food.");
        }
        PlacedPlant plant = session.plantAt(x, y);
        if (plant == null) {
            return Result.fail("There is no plant at (" + x + ", " + y + ").");
        }
        if (plant.isDisabled()) {
            return Result.fail("This plant is disabled and cannot use plant food.");
        }
        session.setPlantFoods(session.getPlantFoods() - 1);
        session.getCombatManager().applyPlantFood(plant);
        return Result.ok(plant.getType().getName() + " unleashed its plant food power. "
                + "Plant foods left: " + session.getPlantFoods());
    }

    public Result cheatSpawnZombie(String typeName, int x, int y) {
        if (session.getPhase() != GamePhase.BATTLE) {
            return Result.fail("There is no running battle.");
        }
        ZombieType type = Names.zombie(typeName);
        if (type == null) {
            return Result.fail("No zombie with this name exists.");
        }
        if (x < 1 || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            return Result.fail("Coordinates are outside the lawn.");
        }
        session.spawnZombie(type, x, y, Math.max(1, session.getWaveManager().getCurrentWave()));
        return Result.ok(type.getName() + " spawned at (" + x + ", " + y + ").");
    }

    public Result releaseNuke() {
        if (session.getPhase() != GamePhase.BATTLE) {
            return Result.fail("There is no running battle.");
        }
        int count = session.getZombies().size();
        for (Zombie zombie : new ArrayList<>(session.getZombies())) {
            session.getCombatManager().damageZombie(zombie, 1_000_000);
        }
        checkVictory();
        return Result.ok("The nuke wiped out " + count + " zombies.");
    }
}
