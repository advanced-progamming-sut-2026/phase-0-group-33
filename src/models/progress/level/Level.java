package models.progress.level;

import models.progress.chapter.Chapter;

public abstract class Level {

    protected Chapter chapter;
    protected int levelNumber;

    public Level(Chapter chapter, int levelNumber) {
        this.chapter = chapter;
        this.levelNumber = levelNumber;
    }

    public void start() {
        // TODO
    }

    public boolean checkVictoryCondition() {
        // TODO
        return false;
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
