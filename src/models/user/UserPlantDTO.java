package models.user;

import models.entities.plant.StaticPlant;
import java.util.Objects;


public class UserPlantDTO {
    private String username;
    private StaticPlant basePlant;
    private int upgradeLevel;

    public UserPlantDTO() {
    }

    public UserPlantDTO(String username, StaticPlant basePlant, int upgradeLevel) {
        this.username = username;
        this.basePlant = basePlant;
        this.upgradeLevel = upgradeLevel;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public StaticPlant getBasePlant() { return basePlant; }
    public void setBasePlant(StaticPlant basePlant) { this.basePlant = basePlant; }

    public int getUpgradeLevel() { return upgradeLevel; }
    public void setUpgradeLevel(int upgradeLevel) { this.upgradeLevel = upgradeLevel; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPlantDTO that = (UserPlantDTO) o;
        return username.equals(that.username) && basePlant.equals(that.basePlant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, basePlant);
    }

    @Override
    public String toString() {
        return "UserPlantDTO{" +
                "plantName=" + (basePlant != null ? basePlant.getName() : "null") +
                ", upgradeLevel=" + upgradeLevel +
                '}';
    }
}