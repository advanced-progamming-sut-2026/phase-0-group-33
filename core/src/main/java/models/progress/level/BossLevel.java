package models.progress.level;

import models.entities.zombie.Zomboss;
import models.progress.chapter.Chapter;
import models.progress.level.special.SpecialLevelType;

public class BossLevel extends Level {

    public BossLevel(Chapter chapter, int levelNumber) {
        super(chapter, levelNumber);
    }

    @Override
    public SpecialLevelType getSpecialType() {
        return SpecialLevelType.CONVEYOR_BELT;
    }

    @Override
    public int getWaveCount() {
        return 1;
    }

    public Zomboss.BossKind getBossKind() {
        String name = chapter == null ? "" : chapter.getName().replaceAll("[^A-Za-z]", "");
        if (name.equalsIgnoreCase("DarkAges")) {
            return Zomboss.BossKind.DRAGON;
        }
        if (name.equalsIgnoreCase("FrostBite")) {
            return Zomboss.BossKind.MAMMOTH;
        }
        if (name.equalsIgnoreCase("WaveyBeach")) {
            return Zomboss.BossKind.SHARK;
        }
        return Zomboss.BossKind.ROBOT;
    }
}
