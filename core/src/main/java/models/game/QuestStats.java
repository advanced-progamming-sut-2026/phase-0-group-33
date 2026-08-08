package models.game;

import models.entities.plant.PlantCategory;
import models.entities.plant.PlantTag;
import models.entities.plant.PlantType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuestStats {
    private final Map<String, Integer> killsByPlant = new HashMap<>();
    private final Set<PlantCategory> killCategories = new HashSet<>();
    private final Set<PlantCategory> plantedCategories = new HashSet<>();
    private final Set<Integer> plantedColumns = new HashSet<>();
    private final Set<Integer> plantedRows = new HashSet<>();
    private int explosivesPlanted;
    private int sunProducersPlanted;
    private boolean nonShroomPlanted;
    private boolean anythingPlanted;
    private boolean unattributedKill;
    private int firstWaveTick = -1;
    private int earlyKills;
    private int totalKills;
    private int firstColumnKills;

    public void onPlanted(PlantType type, int x, int y) {
        anythingPlanted = true;
        plantedColumns.add(x);
        plantedRows.add(y);
        plantedCategories.add(type.getCategory());
        if (type.getCategory() == PlantCategory.EXPLOSIVE) {
            explosivesPlanted++;
        }
        if (type.getCategory() == PlantCategory.SUN_PRODUCER) {
            sunProducersPlanted++;
        }
        if (!type.getTags().contains(PlantTag.SHROOM)) {
            nonShroomPlanted = true;
        }
    }

    public void onWaveOneStarted(int tick) {
        if (firstWaveTick < 0) {
            firstWaveTick = tick;
        }
    }

    public void onKill(PlantType source, int tick, double x, boolean mowerUsedInRow) {
        totalKills++;
        if (source == null) {
            unattributedKill = true;
        } else {
            killsByPlant.merge(source.getName(), 1, Integer::sum);
            killCategories.add(source.getCategory());
        }
        if (firstWaveTick >= 0 && tick - firstWaveTick <= 30 * GameSession.TICKS_PER_SECOND) {
            earlyKills++;
        }
        if (x <= 1.5 && mowerUsedInRow) {
            firstColumnKills++;
        }
    }

    public int killsOf(String plantName) {
        return killsByPlant.getOrDefault(plantName, 0);
    }

    public boolean onlyOneFamilyKilled() {
        return totalKills > 0 && !unattributedKill && killCategories.size() == 1;
    }

    public Set<PlantCategory> getPlantedCategories() {
        return plantedCategories;
    }

    public Set<Integer> getPlantedColumns() {
        return plantedColumns;
    }

    public Set<Integer> getPlantedRows() {
        return plantedRows;
    }

    public int getExplosivesPlanted() {
        return explosivesPlanted;
    }

    public int getSunProducersPlanted() {
        return sunProducersPlanted;
    }

    public boolean isAllPlantedShroom() {
        return anythingPlanted && !nonShroomPlanted;
    }

    public boolean isAnythingPlanted() {
        return anythingPlanted;
    }

    public int getEarlyKills() {
        return earlyKills;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getFirstColumnKills() {
        return firstColumnKills;
    }
}
