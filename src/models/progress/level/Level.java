package models.progress.level;

import models.progress.chapter.Chapter;
import models.progress.level.special.SpecialLevelType;

public abstract class Level {

    protected Chapter chapter;
    protected int levelNumber;

    public Level(Chapter chapter, int levelNumber) {
        this.chapter = chapter;
        this.levelNumber = levelNumber;
    }

    public int getWaveCount() {
        return 2 + levelNumber;
    }

    public int getBaseWaveBudget() {
        return 100 + 100 * levelNumber;
    }

    public SpecialLevelType getSpecialType() {
        return null;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }
}
