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
    private int graveHealth;
    private boolean hasLilyPad;

    public Tile(Point position, TerrainType terrain, boolean hasLawnMower) {
        this.position = position;
        this.terrain = terrain;
        this.plant = null;
        this.zombies = new ArrayList<>();
        this.hasLawnMower = hasLawnMower;
        if (terrain == TerrainType.GRAVE) {
            this.graveHealth = 700;
        }
    }

    /** Gravestone HP (doc: 700; blocked shots damage it, then it becomes normal ground). */
    public int getGraveHealth() {
        return graveHealth;
    }

    /** Damages the gravestone; the tile turns to normal ground once it breaks. */
    public void damageGrave(int damage) {
        if (terrain != TerrainType.GRAVE) {
            return;
        }
        graveHealth -= damage;
        if (graveHealth <= 0) {
            graveHealth = 0;
            terrain = TerrainType.NORMAL;
        }
    }

    public boolean isHasLilyPad() {
        return hasLilyPad;
    }

    public void setHasLilyPad(boolean hasLilyPad) {
        this.hasLilyPad = hasLilyPad;
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