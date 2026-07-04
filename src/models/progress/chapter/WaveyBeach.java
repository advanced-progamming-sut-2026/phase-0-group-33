package models.progress.chapter;

import models.entities.zombie.ZombieType;
import models.progress.level.BossLevel;
import models.progress.level.OrdinaryLevel;
import models.progress.level.special.DeadLine;
import models.progress.level.special.LoveYourPlants;

import java.util.ArrayList;
import java.util.List;

public class WaveyBeach extends Chapter {

    public WaveyBeach() {
        this.name = "Wavey Beach";
        this.levels = new ArrayList<>();
        levels.add(new OrdinaryLevel(this, 1));
        levels.add(new DeadLine(this, 2));
        levels.add(new LoveYourPlants(this, 3));
        levels.add(new BossLevel(this, 4));
        this.currentUnlockedLevel = levels.get(0);
    }

    @Override
    public List<ZombieType> getZombiePool() {
        return List.of(ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD,
                ZombieType.FISHERMAN, ZombieType.OCTOPUS, ZombieType.SNORKEL,
                ZombieType.IMP, ZombieType.GARGANTUAR);
    }

    @Override
    public int getWaterColumns() {
        return 2;
    }
}
