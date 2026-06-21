package models.entities.plant;

/**
 * A Data Transfer Object representing the final, calculated runtime stats
 * of a plant, factoring in static base stats and dynamic modifiers
 * granted by the user's current upgrade level.
 */
public class EffectivePlantStats {
    private final int maxHp;
    private final int cost;
    private final String damageAmount;
    private final int rechargeTime;

    /**
     * Constructs the effective stats for a plant.
     *
     * @param maxHp        The modified maximum health points.
     * @param cost         The modified sun cost to plant.
     * @param damageAmount The base or modified damage string.
     * @param rechargeTime The base or modified recharge/cooldown time.
     */
    public EffectivePlantStats(int maxHp, int cost, String damageAmount, int rechargeTime) {
        this.maxHp = maxHp;
        this.cost = cost;
        this.damageAmount = damageAmount;
        this.rechargeTime = rechargeTime;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCost() {
        return cost;
    }

    public String getDamageAmount() {
        return damageAmount;
    }

    public int getRechargeTime() {
        return rechargeTime;
    }

    @Override
    public String toString() {
        return "EffectivePlantStats{" +
                "maxHp=" + maxHp +
                ", cost=" + cost +
                ", damageAmount='" + damageAmount + '\'' +
                ", rechargeTime=" + rechargeTime +
                '}';
    }
}