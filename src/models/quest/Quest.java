package models.quest;

import models.asset.Asset;

public class Quest {

    private String id;
    private String description;
    private QuestType type;
    private QuestPriority priority;
    private int goal;
    private int progress;
    private Asset reward;

    public void updateProgress(Object event) {

    }

    public boolean isCompleted() {
        return progress >= goal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuestType getType() {
        return type;
    }

    public void setType(QuestType type) {
        this.type = type;
    }

    public QuestPriority getPriority() {
        return priority;
    }

    public void setPriority(QuestPriority priority) {
        this.priority = priority;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Asset getReward() {
        return reward;
    }

    public void setReward(Asset reward) {
        this.reward = reward;
    }
}
