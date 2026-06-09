package models.progress.chapter;

import models.progress.level.Level;

import java.util.List;

public abstract class Chapter {

    protected String name;
    protected Level currentUnlockedLevel;
    protected List<Level> levels;

    public void unlockNextLevel() {
        // TODO
    }

    public Level getCurrentUnlockedLevel() {
        return currentUnlockedLevel;
    }

    public void setCurrentUnlockedLevel(Level currentUnlockedLevel) {
        this.currentUnlockedLevel = currentUnlockedLevel;
    }
}
