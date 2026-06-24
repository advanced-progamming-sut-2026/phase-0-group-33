package models.entities.plant;


import models.map.Position;

public abstract class Plant {
    protected PlantType type;
    protected Position position;
    protected int health;
    protected int currentCooldown;
    protected PlantState state;

    public abstract void onTick();
    public abstract void applyPlantFood();

    public PlantType getType() {
        return type;
    }

    public void setType(PlantType type) {
        this.type = type;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getCurrentCooldown() {
        return currentCooldown;
    }

    public void setCurrentCooldown(int currentCooldown) {
        this.currentCooldown = currentCooldown;
    }

    public PlantState getState() {
        return state;
    }

    public void setState(PlantState state) {
        this.state = state;
    }
}