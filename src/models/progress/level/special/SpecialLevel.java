package models.progress.level.special;

import models.progress.chapter.Chapter;
import models.progress.level.Level;

/** A level with one of the eight special rule sets from the doc. */
public class SpecialLevel extends Level {
    private final SpecialLevelType specialType;

    public SpecialLevel(Chapter chapter, int levelNumber, SpecialLevelType specialType) {
        super(chapter, levelNumber);
        this.specialType = specialType;
    }

    @Override
    public SpecialLevelType getSpecialType() {
        return specialType;
    }
}
