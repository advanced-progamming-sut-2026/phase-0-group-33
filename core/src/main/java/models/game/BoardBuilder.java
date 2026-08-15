package models.game;

import models.map.Grid;
import models.map.TerrainType;
import models.map.Tile;
import models.progress.chapter.Chapter;
import models.progress.chapter.DarkAges;
import models.progress.chapter.FrostBite;
import models.progress.chapter.WaveyBeach;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class BoardBuilder {

    private BoardBuilder() {
    }

    public static void build(Grid grid, Chapter chapter, Random random) {
        int waterColumns = chapter == null ? 0 : chapter.getWaterColumns();
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                TerrainType terrain = col > GameSession.COLS - waterColumns
                        ? TerrainType.WATER
                        : TerrainType.NORMAL;
                grid.setTile(col - 1, row - 1, new Tile(new Point(col, row), terrain, col == 1));
            }
        }
        if (chapter == null) {
            return;
        }
        placeGraves(grid, chapter.getGraveCount(), random);
        if (chapter instanceof FrostBite) {
            placeSliders(grid, random);
        }
        if (chapter instanceof DarkAges) {
            markSpecialTiles(grid, random, 4, true);
        }
        if (chapter instanceof WaveyBeach) {
            markLowTide(grid, random, 3);
        }
    }

    private static void markSpecialTiles(Grid grid, Random random, int count, boolean necromancy) {
        for (int i = 0; i < count; i++) {
            int col = 4 + random.nextInt(GameSession.COLS - 3);
            int row = 1 + random.nextInt(GameSession.ROWS);
            Tile tile = grid.getTile(col - 1, row - 1);
            if (necromancy) {
                tile.setNecromancy(true);
            }
        }
    }

    public static void markLowTide(Grid grid, Random random, int count) {
        List<Tile> sea = new ArrayList<>();
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                Tile tile = grid.getTile(col - 1, row - 1);
                if (tile == null) {
                    continue;
                }
                tile.setLowTide(false);
                if (tile.getTerrain() == TerrainType.WATER) {
                    sea.add(tile);
                }
            }
        }
        Collections.shuffle(sea, random);
        for (int i = 0; i < Math.min(count, sea.size()); i++) {
            sea.get(i).setLowTide(true);
        }
    }

    private static void placeGraves(Grid grid, int count, Random random) {
        for (int i = 0; i < count; i++) {
            int col = 3 + random.nextInt(GameSession.COLS - 3);
            int row = 1 + random.nextInt(GameSession.ROWS);
            Tile tile = grid.getTile(col - 1, row - 1);
            if (tile.getTerrain() == TerrainType.NORMAL) {
                grid.setTile(col - 1, row - 1, new Tile(new Point(col, row), TerrainType.GRAVE, false));
            }
        }
    }

    private static void placeSliders(Grid grid, Random random) {
        for (TerrainType slider : new TerrainType[] { TerrainType.SLIDER_UP, TerrainType.SLIDER_DOWN }) {
            int col = 3 + random.nextInt(GameSession.COLS - 3);
            int row = slider == TerrainType.SLIDER_UP
                    ? 2 + random.nextInt(GameSession.ROWS - 1)
                    : 1 + random.nextInt(GameSession.ROWS - 1);
            Tile tile = grid.getTile(col - 1, row - 1);
            if (tile.getTerrain() == TerrainType.NORMAL) {
                grid.setTile(col - 1, row - 1, new Tile(new Point(col, row), slider, false));
            }
        }
    }
}
