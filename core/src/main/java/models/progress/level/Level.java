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
        return 3 + 2 * levelNumber;
    }

    public int getBaseWaveBudget() {
        double chapterFactor = getChapter() == null ? 1.0 : getChapter().getDifficultyFactor();
        return (int) Math.round((215 + 135 * levelNumber) * chapterFactor);
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
