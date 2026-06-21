package models.entities.plant;

import java.util.Objects;

public class StaticPlant {
    private int id;
    private String name;
    private String category;
    private String tags;
    private int cost;
    private int baseHp;
    private String baseDamage;
    private String baseAbility;
    private String plantFoodEffect;
    private String lvl2Upgrade;
    private String lvl3Upgrade;
    private String lvl4Upgrade;
    private String actionInterval;
    private int rechargeTime;

    public StaticPlant() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public int getBaseHp() { return baseHp; }
    public void setBaseHp(int baseHp) { this.baseHp = baseHp; }

    public String getBaseDamage() { return baseDamage; }
    public void setBaseDamage(String baseDamage) { this.baseDamage = baseDamage; }

    public String getBaseAbility() { return baseAbility; }
    public void setBaseAbility(String baseAbility) { this.baseAbility = baseAbility; }

    public String getPlantFoodEffect() { return plantFoodEffect; }
    public void setPlantFoodEffect(String plantFoodEffect) { this.plantFoodEffect = plantFoodEffect; }

    public String getLvl2Upgrade() { return lvl2Upgrade; }
    public void setLvl2Upgrade(String lvl2Upgrade) { this.lvl2Upgrade = lvl2Upgrade; }

    public String getLvl3Upgrade() { return lvl3Upgrade; }
    public void setLvl3Upgrade(String lvl3Upgrade) { this.lvl3Upgrade = lvl3Upgrade; }

    public String getLvl4Upgrade() { return lvl4Upgrade; }
    public void setLvl4Upgrade(String lvl4Upgrade) { this.lvl4Upgrade = lvl4Upgrade; }

    public String getActionInterval() { return actionInterval; }
    public void setActionInterval(String actionInterval) { this.actionInterval = actionInterval; }

    public int getRechargeTime() { return rechargeTime; }
    public void setRechargeTime(int rechargeTime) { this.rechargeTime = rechargeTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticPlant that = (StaticPlant) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StaticPlant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cost=" + cost +
                ", rechargeTime=" + rechargeTime +
                '}';
    }
}