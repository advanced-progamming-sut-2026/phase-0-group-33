package models.progress.level.special;

import models.progress.chapter.Chapter;

public class DeadLine extends SpecialLevel {
    public DeadLine(Chapter chapter, int levelNumber) {
        super(chapter, levelNumber, SpecialLevelType.DEAD_LINE);
    }
}
