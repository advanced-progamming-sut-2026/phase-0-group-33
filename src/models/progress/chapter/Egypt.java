package models.progress.chapter;

import models.entities.zombie.ZombieType;
import models.progress.level.BossLevel;
import models.progress.level.OrdinaryLevel;
import models.progress.level.special.ConveyorBelt;
import models.progress.level.special.LockedPlants;

import java.util.ArrayList;
import java.util.List;

public class Egypt extends Chapter {

    public Egypt() {
        this.name = "Egypt";
        this.levels = new ArrayList<>();
        levels.add(new OrdinaryLevel(this, 1));
        levels.add(new ConveyorBelt(this, 2));
        levels.add(new LockedPlants(this, 3));
        levels.add(new BossLevel(this, 4));
        this.currentUnlockedLevel = levels.get(0);
    }

    @Override
    public List<ZombieType> getZombiePool() {
        return withCommons(ZombieType.RA, ZombieType.EXPLORER, ZombieType.TOMB_RAISER);
    }

    @Override
    public int getGraveCount() {
        return 2;
    }
}
