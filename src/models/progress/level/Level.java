package models.progress.level;

import models.progress.chapter.Chapter;
import models.progress.level.special.SpecialLevelType;

/**
 * One playable stage of a chapter. Wave counts and budgets are design
 * decisions (the doc leaves the exact difficulty function to the team):
 * each level has {@code 2 + levelNumber} waves and a first-wave budget of
 * {@code 100 + 100 * levelNumber}; later waves grow per the doc
 * (25% harder each wave, final wave twice the previous one).
 */
public abstract class Level {

    protected Chapter chapter;
    protected int levelNumber;

    public Level(Chapter chapter, int levelNumber) {
        this.chapter = chapter;
        this.levelNumber = levelNumber;
    }

    /** Number of zombie waves in this level. */
    public int getWaveCount() {
        return 2 + levelNumber;
    }

    /** Total zombie cost budget of the first wave (before difficulty scaling). */
    public int getBaseWaveBudget() {
        return 100 + 100 * levelNumber;
    }

    /** Non-null only for special levels. */
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
