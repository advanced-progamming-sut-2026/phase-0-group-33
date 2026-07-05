package models.game;

import models.map.Grid;
import models.map.TerrainType;
import models.map.Tile;
import models.progress.chapter.Chapter;
import models.progress.chapter.FrostBite;

import java.awt.Point;
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
