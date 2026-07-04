package models.game;

import controllers.managers.SunManager;
import controllers.managers.WaveManager;
import models.entities.plant.PlantType;
import models.map.Grid;
import models.user.UserPlant;

import java.util.List;

public class GameSession {
    private Grid grid;
    private WaveManager waveManager;
    private SunManager sunManager;
    private List<UserPlant> selectedPlants;
    private int tickCount;
    private boolean isEnded;

    public static PlantType resolvePlantType(String name) {
        if (name == null) {
            return null;
        }
        for (PlantType type : PlantType.values()) {
            if (normalize(type.getName()).equals(normalize(name))) {
                return type;
            }
        }
        return null;
    }

    private static String normalize(String text) {
        return text.replaceAll("[\\s_-]", "").toLowerCase();
    }

    public void advanceTime(int ticks) {
    }

    public void placePlant(String type, String pos) {
    }

    public void pluckPlant(String pos) {
    }

    public void showMap() {
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public void setWaveManager(WaveManager waveManager) {
        this.waveManager = waveManager;
    }

    public SunManager getSunManager() {
        return sunManager;
    }

    public void setSunManager(SunManager sunManager) {
        this.sunManager = sunManager;
    }

    public List<UserPlant> getSelectedPlants() {
        return selectedPlants;
    }

    public void setSelectedPlants(List<UserPlant> selectedPlants) {
        this.selectedPlants = selectedPlants;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }

    public boolean isEnded() {
        return isEnded;
    }

    public void setEnded(boolean ended) {
        isEnded = ended;
    }
}