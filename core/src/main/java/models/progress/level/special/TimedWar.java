package models.progress.level.special;

import models.progress.chapter.Chapter;

public class TimedWar extends SpecialLevel {
    public TimedWar(Chapter chapter, int levelNumber) {
        super(chapter, levelNumber, SpecialLevelType.TIMED_WAR);
    }
}
