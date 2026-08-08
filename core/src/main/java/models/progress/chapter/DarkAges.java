package models.progress.chapter;

import models.entities.zombie.ZombieType;
import models.progress.level.BossLevel;
import models.progress.level.OrdinaryLevel;
import models.progress.level.special.NightOps;
import models.progress.level.special.PlantWhatYouGet;

import java.util.ArrayList;
import java.util.List;

public class DarkAges extends Chapter {

    public DarkAges() {
        this.name = "Dark Ages";
        this.levels = new ArrayList<>();
        levels.add(new OrdinaryLevel(this, 1));
        levels.add(new NightOps(this, 2));
        levels.add(new PlantWhatYouGet(this, 3));
        levels.add(new BossLevel(this, 4));
        this.currentUnlockedLevel = levels.get(0);
    }

    @Override
    public List<ZombieType> getZombiePool() {
        return List.of(ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.KNIGHT,
                ZombieType.JUGGLER, ZombieType.WIZARD, ZombieType.KING,
                ZombieType.IMP_DRAGON, ZombieType.GARGANTUAR);
    }

    @Override
    public boolean isNight() {
        return true;
    }

    @Override
    public int getGraveCount() {
        return 2;
    }
}
