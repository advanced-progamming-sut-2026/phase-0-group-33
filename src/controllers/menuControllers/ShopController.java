package controllers.menuControllers;

import models.App;
import models.Result;

public class ShopController extends BaseController {
    public ShopController(App app) {
        super(app);
    }

    public Result handleShopList() {
        return null;
    }

    public Result handleShopDaily() {
        return null;
    }

    public Result handleShopBuy(String itemId, int count, String plantType) {
        return null;
    }

    public Result handleMenuChange(String menuName) {
        return null;
    }

    public Result handleExit() {
        return null;
    }
}