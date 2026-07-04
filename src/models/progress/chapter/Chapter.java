package models.progress.chapter;

import models.entities.zombie.ZombieType;
import models.progress.level.Level;

import java.util.List;

/**
 * One of the four adventure chapters. Each chapter defines its environment
 * (which zombies appear, whether sun falls from the sky, special terrain)
 * and holds its four levels: ordinary, two specials, and a boss level.
 */
public abstract class Chapter {

    protected String name;
    protected Level currentUnlockedLevel;
    protected List<Level> levels;

    /** Zombies that may appear in this chapter's waves. */
    public abstract List<ZombieType> getZombiePool();

    /** Dark Ages is night: no sun falls from the sky. */
    public boolean isNight() {
        return false;
    }

    /** Number of gravestone tiles placed at level start (Egypt / Dark Ages). */
    public int getGraveCount() {
        return 0;
    }

    /** Number of rightmost columns covered by water (Big Wave Beach). */
    public int getWaterColumns() {
        return 0;
    }

    /** Finds a chapter instance by (case/space-insensitive) name. */
    public static Chapter getByName(String chapterName) {
        if (chapterName == null) {
            return null;
        }
        String normalized = chapterName.replaceAll("[\\s_-]", "").toLowerCase();
        Chapter[] all = {new Egypt(), new FrostBite(), new WaveyBeach(), new DarkAges()};
        for (Chapter chapter : all) {
            if (chapter.getName().replaceAll("[\\s_-]", "").toLowerCase().equals(normalized)) {
                return chapter;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

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
