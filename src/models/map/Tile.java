package models.map;

import java.awt.Point;
import models.entities.plant.Plant;
import models.entities.zombie.Zombie;
import java.util.ArrayList;
import java.util.List;

public class Tile {
    private Point position;
    private TerrainType terrain;
    private Plant plant;
    private List<Zombie> zombies;
    private boolean hasLawnMower;

    public Tile(Point position, TerrainType terrain, boolean hasLawnMower) {
        this.position = position;
        this.terrain = terrain;
        this.plant = null;
        this.zombies = new ArrayList<>();
        this.hasLawnMower = hasLawnMower;
    }

    public void addPlant(Plant plant) {
        this.plant = plant;
    }

    public void removePlant() {
        this.plant = null;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public TerrainType getTerrain() {
        return terrain;
    }

    public void setTerrain(TerrainType terrain) {
        this.terrain = terrain;
    }

    public Plant getPlant() {
        return plant;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public boolean isHasLawnMower() {
        return hasLawnMower;
    }

    public void setHasLawnMower(boolean hasLawnMower) {
        this.hasLawnMower = hasLawnMower;
    }
}