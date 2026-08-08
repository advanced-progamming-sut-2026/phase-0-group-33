package models.asset;

public abstract class Asset {
    protected int amount = 0;

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
        return this.amount;
    }

    public void setAmount(int value) {
        this.amount = value;
    }
}
