package models.progress.chapter;

import models.progress.level.Level;
import java.util.List;

public abstract class Chapter {

    protected String name;
    protected Level currentUnlockedLevel;
    protected List<Level> levels;

    public void unlockNextLevel() {
        if (levels == null || levels.isEmpty()) {
            return;
        }
        if (currentUnlockedLevel == null) {
            currentUnlockedLevel = levels.get(0);
            return;
        }
        int currentIndex = levels.indexOf(currentUnlockedLevel);
        if (currentIndex >= 0 && currentIndex < levels.size() - 1) {
            currentUnlockedLevel = levels.get(currentIndex + 1);
        }
    }

    public Level getCurrentUnlockedLevel() {
        return currentUnlockedLevel;
    }

    public void setCurrentUnlockedLevel(Level currentUnlockedLevel) {
        this.currentUnlockedLevel = currentUnlockedLevel;
    }

    public List<Level> getLevels() {
        return levels;
    }
}