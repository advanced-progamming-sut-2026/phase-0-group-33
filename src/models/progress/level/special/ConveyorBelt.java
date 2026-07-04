package models.progress.level.special;

import models.progress.chapter.Chapter;

public class ConveyorBelt extends SpecialLevel {
    public ConveyorBelt(Chapter chapter, int levelNumber) {
        super(chapter, levelNumber, SpecialLevelType.CONVEYOR_BELT);
    }
}
