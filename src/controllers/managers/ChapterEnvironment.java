package controllers.managers;

import models.entities.plant.PlantTag;
import models.entities.zombie.Zombie;
import models.entities.zombie.ZombieType;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.game.PushedObject;
import models.map.TerrainType;
import models.map.Tile;
import models.progress.chapter.Chapter;
import models.progress.chapter.DarkAges;
import models.progress.chapter.Egypt;
import models.progress.chapter.FrostBite;
import models.progress.chapter.WaveyBeach;

import java.util.ArrayList;

public class ChapterEnvironment {
    private final GameSession session;

    public ChapterEnvironment(GameSession session) {
        this.session = session;
    }

    private Chapter chapter() {
        return session.getLevel() == null ? null : session.getLevel().getChapter();
    }

    public void tickEnvironment() {
        movePushedObjects();
        applySliders();
    }

    private void movePushedObjects() {
        for (PushedObject pushed : new ArrayList<>(session.getPushedObjects())) {
            if (pushed.isDestroyed()) {
                session.getPushedObjects().remove(pushed);
                if (pushed.getKind() == PushedObject.Kind.BARREL) {
                    session.spawnZombie(ZombieType.IMP, pushed.getX(), pushed.getRow(),
                            Math.max(1, session.getWaveManager().getCurrentWave()));
                    session.spawnZombie(ZombieType.IMP, pushed.getX(), pushed.getRow(),
                            Math.max(1, session.getWaveManager().getCurrentWave()));
                    System.out.printf("The barrel broke and two Imps jumped out in lane %d!%n",
                            pushed.getRow());
                }
                continue;
            }
            if (!pushed.isMoving()) {
                continue;
            }
            pushed.setX(pushed.getX() - 0.0185);
            PlacedPlant crushed = session.plantAt((int) Math.round(pushed.getX()), pushed.getRow());
            if (crushed != null) {
                System.out.printf("The rolling %s crushed %s at (%d, %d)!%n",
                        pushed.getKind().name().toLowerCase().replace('_', ' '),
                        crushed.getType().getName(), crushed.getX(), crushed.getY());
                session.removePlant(crushed, true);
            }
            if (pushed.getX() < 0.5) {
                session.getPushedObjects().remove(pushed);
            }
        }
    }

    private void applySliders() {
        for (Zombie zombie : session.getZombies()) {
            if (zombie.getType() == ZombieType.DODO) {
                continue;
            }
            int col = (int) Math.round(zombie.getPosition().getX());
            int row = (int) zombie.getPosition().getY();
            Tile tile = session.getGrid().getTile(col - 1, row - 1);
            if (tile == null || zombie.getBattle().getLastSliderColumn() == col) {
                continue;
            }
            if (tile.getTerrain() == TerrainType.SLIDER_UP && row > 1) {
                zombie.getPosition().setY(row - 1);
                zombie.getBattle().setLastSliderColumn(col);
                System.out.printf("A slider pushed the %s up to lane %d.%n",
                        zombie.getType().getName(), row - 1);
            } else if (tile.getTerrain() == TerrainType.SLIDER_DOWN && row < GameSession.ROWS) {
                zombie.getPosition().setY(row + 1);
                zombie.getBattle().setLastSliderColumn(col);
                System.out.printf("A slider pushed the %s down to lane %d.%n",
                        zombie.getType().getName(), row + 1);
            }
        }
    }

    public void onWaveStart(int waveNumber) {
        Chapter chapter = chapter();
        if (chapter instanceof FrostBite) {
            iceWind();
        } else if (chapter instanceof WaveyBeach) {
            shiftTide();
        } else if (chapter instanceof DarkAges) {
            darkAgesGraves();
            necromancy();
        }
    }

    public void afterWaveSpawn(int waveNumber) {
        Chapter chapter = chapter();
        if (chapter instanceof Egypt && waveNumber == session.getWaveManager().getTotalWaves()) {
            for (Zombie zombie : session.getZombies()) {
                if (zombie.getSpawnWave() == waveNumber && session.getRandom().nextInt(100) < 40) {
                    int jump = 1 + session.getRandom().nextInt(4);
                    zombie.getPosition().setX(Math.max(2, zombie.getPosition().getX() - jump));
                    System.out.printf("A whirlwind carried the %s %d columns ahead!%n",
                            zombie.getType().getName(), jump);
                }
            }
        }
    }

    private void iceWind() {
        if (session.getRandom().nextInt(100) >= 40) {
            return;
        }
        int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
        System.out.printf("An icy wind sweeps through lane %d!%n", row);
        for (PlacedPlant plant : session.getPlants()) {
            if (plant.getY() != row || plant.getType().getTags().contains(PlantTag.FIRE)) {
                continue;
            }
            plant.setFreezeLevel(plant.getFreezeLevel() + 1);
            if (plant.getFreezeLevel() >= 3 && plant.getIceHealth() == 0) {
                plant.setIceHealth(600);
                System.out.printf("%s at (%d, %d) froze solid!%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
            }
        }
    }

    private void shiftTide() {
        int water = 2 + session.getRandom().nextInt(3);
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                Tile tile = session.getGrid().getTile(col - 1, row - 1);
                if (col > GameSession.COLS - water) {
                    if (tile.getTerrain() == TerrainType.NORMAL) {
                        tile.setTerrain(TerrainType.WATER);
                    }
                } else if (tile.getTerrain() == TerrainType.WATER) {
                    tile.setTerrain(TerrainType.NORMAL);
                    tile.setHasLilyPad(false);
                }
            }
        }
        System.out.printf("The tide shifted; the last %d columns are underwater.%n", water);
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            Tile tile = session.getGrid().getTile(plant.getX() - 1, plant.getY() - 1);
            if (tile.getTerrain() == TerrainType.WATER && !tile.isHasLilyPad()
                    && !plant.getType().getTags().contains(PlantTag.WATER)) {
                System.out.printf("%s at (%d, %d) was swept away by the tide!%n",
                        plant.getType().getName(), plant.getX(), plant.getY());
                session.removePlant(plant, false);
            }
        }
        lowTideAmbush();
    }

    private void lowTideAmbush() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                Tile tile = session.getGrid().getTile(col - 1, row - 1);
                if (tile.isLowTide() && tile.getTerrain() == TerrainType.WATER
                        && session.getRandom().nextInt(100) < 30) {
                    session.spawnZombie(ZombieType.NORMAL, col, row,
                            Math.max(1, session.getWaveManager().getCurrentWave()));
                    System.out.printf("A zombie emerged from beneath the low tide at (%d, %d)!%n",
                            col, row);
                }
            }
        }
    }

    private void darkAgesGraves() {
        int count = 1 + session.getRandom().nextInt(2);
        for (int i = 0; i < count; i++) {
            int col = 2 + session.getRandom().nextInt(GameSession.COLS - 2);
            int row = 1 + session.getRandom().nextInt(GameSession.ROWS);
            Tile tile = session.getGrid().getTile(col - 1, row - 1);
            if (tile.getTerrain() != TerrainType.NORMAL || session.plantAt(col, row) != null) {
                continue;
            }
            tile.setTerrain(TerrainType.GRAVE);
            if (session.getRandom().nextInt(100) < 30) {
                if (session.getRandom().nextBoolean()) {
                    tile.setGraveSunContent(50);
                    System.out.printf("A grave rose at (%d, %d) carrying 50 sun;"
                            + " break it to collect!%n", col, row);
                } else {
                    tile.setGravePlantFood(true);
                    System.out.printf("A grave rose at (%d, %d) carrying a plant food;"
                            + " break it to collect!%n", col, row);
                }
            } else {
                System.out.printf("A grave rose at (%d, %d).%n", col, row);
            }
        }
    }

    private void necromancy() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                Tile tile = session.getGrid().getTile(col - 1, row - 1);
                if (tile.getTerrain() == TerrainType.GRAVE && tile.isNecromancy()) {
                    session.spawnZombie(ZombieType.NORMAL, col, row,
                            Math.max(1, session.getWaveManager().getCurrentWave()));
                    System.out.printf("Necromancy! A zombie crawled out from the grave at (%d, %d).%n",
                            col, row);
                }
            }
        }
    }
}
