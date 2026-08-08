package models.shop;

public class ShopItem {

    private String id;
    private String name;
    private int priceCoin;
    private int priceDiamond;
    private boolean isDaily;
    private int maxPurchase;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriceCoin() {
        return priceCoin;
    }

    public void setPriceCoin(int priceCoin) {
        this.priceCoin = priceCoin;
    }

    public int getPriceDiamond() {
        return priceDiamond;
    }

    public void setPriceDiamond(int priceDiamond) {
        this.priceDiamond = priceDiamond;
    }

    public boolean isDaily() {
        return isDaily;
    }

    public void setDaily(boolean daily) {
        isDaily = daily;
    }

    public int getMaxPurchase() {
        return maxPurchase;
    }

    public void setMaxPurchase(int maxPurchase) {
        this.maxPurchase = maxPurchase;
    }
}
