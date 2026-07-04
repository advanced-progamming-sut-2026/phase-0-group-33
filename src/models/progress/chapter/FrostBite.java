package models.progress.chapter;

import models.entities.zombie.ZombieType;
import models.progress.level.BossLevel;
import models.progress.level.OrdinaryLevel;
import models.progress.level.special.SaveOurSeeds;
import models.progress.level.special.TimedWar;

import java.util.ArrayList;
import java.util.List;

public class FrostBite extends Chapter {

    public FrostBite() {
        this.name = "Frost Bite";
        this.levels = new ArrayList<>();
        levels.add(new OrdinaryLevel(this, 1));
        levels.add(new SaveOurSeeds(this, 2));
        levels.add(new TimedWar(this, 3));
        levels.add(new BossLevel(this, 4));
        this.currentUnlockedLevel = levels.get(0);
    }

    @Override
    public List<ZombieType> getZombiePool() {
        return List.of(ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD,
                ZombieType.DODO, ZombieType.HUNTER, ZombieType.TROGLOBITE,
                ZombieType.IMP, ZombieType.GARGANTUAR);
    }
}
