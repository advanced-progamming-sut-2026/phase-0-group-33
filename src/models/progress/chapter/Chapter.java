package models.progress.chapter;

import models.entities.zombie.ZombieType;
import models.progress.level.Level;

import java.util.List;

public abstract class Chapter {

    protected String name;
    protected Level currentUnlockedLevel;
    protected List<Level> levels;

    public abstract List<ZombieType> getZombiePool();

    protected static List<ZombieType> commonZombies() {
        return List.of(ZombieType.NORMAL, ZombieType.CONE_HEAD, ZombieType.BUCKET_HEAD,
                ZombieType.BRICK_HEAD, ZombieType.KNIGHT, ZombieType.GARGANTUAR,
                ZombieType.IMP, ZombieType.ALLSTAR, ZombieType.ARCAD, ZombieType.UMBRELLA,
                ZombieType.TURQUOISE, ZombieType.PROSPECTOR, ZombieType.PIANO,
                ZombieType.NEWSPAPER, ZombieType.BARREL_ROLLER);
    }

    protected static List<ZombieType> withCommons(ZombieType... chapterSpecific) {
        List<ZombieType> pool = new java.util.ArrayList<>(commonZombies());
        pool.addAll(List.of(chapterSpecific));
        return pool;
    }

    public boolean isNight() {
        return false;
    }

    public int getGraveCount() {
        return 0;
    }

    public int getWaterColumns() {
        return 0;
    }

    public static Chapter getByName(String chapterName) {
        if (chapterName == null) {
            return null;
        }
        String normalized = chapterName.replaceAll("[\\s_-]", "").toLowerCase();
        Chapter[] all = { new Egypt(), new FrostBite(), new WaveyBeach(), new DarkAges() };
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
