package models.enums;

public enum DifficultyLevel {
    NEWBIE(1), EASY(2), MEDIUM(3), HARD(4), EXTREME(5);

    private int level;

    DifficultyLevel(int level) {
        this.level = level;
    }

    public int getLevelNumber() { return this.level; }

    public static DifficultyLevel getDifficultyLevel(int level) {
        for (DifficultyLevel difficultyLevel : DifficultyLevel.values()) {
            if (difficultyLevel.getLevelNumber() == level) {
                return difficultyLevel;
            }
        }
        return DifficultyLevel.MEDIUM;
    }
}
