package models.map;

import java.awt.Point;
import models.entities.zombie.Zombie;
import java.util.ArrayList;
import java.util.List;

public class Tile {
    private Point position;
    private TerrainType terrain;
    private List<Zombie> zombies;
    private boolean hasLawnMower;
    private int graveHealth;
    private boolean hasLilyPad;
    private int graveSunContent;
    private boolean gravePlantFood;
    private boolean necromancy;
    private boolean lowTide;

    public Tile(Point position, TerrainType terrain, boolean hasLawnMower) {
        this.position = position;
        this.terrain = terrain;
        this.zombies = new ArrayList<>();
        this.hasLawnMower = hasLawnMower;
        if (terrain == TerrainType.GRAVE) {
            this.graveHealth = 700;
        }
    }

    public int getGraveHealth() {
        return graveHealth;
    }

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

    public int getGraveSunContent() {
        return graveSunContent;
    }

    public void setGraveSunContent(int graveSunContent) {
        this.graveSunContent = graveSunContent;
    }

    public boolean isGravePlantFood() {
        return gravePlantFood;
    }

    public void setGravePlantFood(boolean gravePlantFood) {
        this.gravePlantFood = gravePlantFood;
    }

    public void clearGraveContent() {
        this.graveSunContent = 0;
        this.gravePlantFood = false;
    }

    public boolean isNecromancy() {
        return necromancy;
    }

    public void setNecromancy(boolean necromancy) {
        this.necromancy = necromancy;
    }

    public boolean isLowTide() {
        return lowTide;
    }

    public void setLowTide(boolean lowTide) {
        this.lowTide = lowTide;
    }

    public boolean isHasLilyPad() {
        return hasLilyPad;
    }

    public void setHasLilyPad(boolean hasLilyPad) {
        this.hasLilyPad = hasLilyPad;
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
        if (terrain == TerrainType.GRAVE && this.terrain != TerrainType.GRAVE) {
            this.graveHealth = 700;
        } else if (terrain != TerrainType.GRAVE) {
            this.graveHealth = 0;
            clearGraveContent();
        }
        this.terrain = terrain;
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
