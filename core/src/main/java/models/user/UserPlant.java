package models.user;

import models.entities.plant.PlantType;

public class UserPlant {

    private PlantType plantType;
    private int upgradeLevel = 1;

    public UserPlant(PlantType plantType) {
        this.plantType = plantType;
    }

    public void upgrade() {
        this.upgradeLevel++;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public void setPlantType(PlantType plantType) {
        this.plantType = plantType;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }
}
