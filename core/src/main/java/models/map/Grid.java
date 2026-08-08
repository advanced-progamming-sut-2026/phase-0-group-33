package models.map;

public class Grid {
    private Tile[][] tiles;
    private int rows;
    private int cols;

    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.tiles = new Tile[rows][cols];
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) {
            return null;
        }
        return tiles[y][x];
    }

    public void setTile(int x, int y, Tile tile) {
        if (x >= 0 && x < cols && y >= 0 && y < rows) {
            tiles[y][x] = tile;
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Tile[][] getTiles() {
        return tiles;
    }
}
