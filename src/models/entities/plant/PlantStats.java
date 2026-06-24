package models.entities.plant;

public class PlantStats {
    private int maxHp;
    private int cost;
    private String damage;
    private int recharge;

    public PlantStats() {
    }

    public PlantStats(int maxHp, int cost, String damage, int recharge) {
        this.maxHp = maxHp;
        this.cost = cost;
        this.damage = damage;
        this.recharge = recharge;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public int getRecharge() {
        return recharge;
    }

    public void setRecharge(int recharge) {
        this.recharge = recharge;
    }
}