package models.shop;

import java.util.List;

public class Shop {

    private List<ShopItem> permanentItems;
    private ShopItem dailyOffer;

    public boolean buyItem(String itemId, int count) {
        // TODO
        return false;
    }

    public void refreshDaily() {
        // TODO
    }

    public List<ShopItem> getPermanentItems() {
        return permanentItems;
    }

    public void setPermanentItems(List<ShopItem> permanentItems) {
        this.permanentItems = permanentItems;
    }

    public ShopItem getDailyOffer() {
        return dailyOffer;
    }

    public void setDailyOffer(ShopItem dailyOffer) {
        this.dailyOffer = dailyOffer;
    }
}
