package model.asset;

public abstract class Asset {

    protected int amount;

    public void add(int amountToAdd) {
        this.amount += amountToAdd;
    }

    public boolean spend(int amountToSpend) {
        if (this.amount >= amountToSpend) {
            this.amount -= amountToSpend;
            return true;
        }
        return false;
    }

    public int getAmount() {
        return amount;
    }
}
