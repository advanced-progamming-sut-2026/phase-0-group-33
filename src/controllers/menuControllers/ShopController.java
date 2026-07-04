package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.enums.Menus;
import models.game.GameSession;
import utils.UserDataStore;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * Shop (reached from the greenhouse). Items and prices follow the shop table
 * in the doc; the daily offer is a 10-packet bundle for a random unlocked
 * plant at 20% off (1600 coins), resets at 00:00 system time, once per day.
 */
public class ShopController extends BaseController {
    private static final int POT_PRICE = 2000;
    private static final int MAX_POTS = 20;
    private static final int PLANT_FOOD_DIAMONDS = 3;
    private static final int RANDOM_BUNDLE_COINS = 1000;
    private static final int CHOICE_BUNDLE_DIAMONDS = 5;
    private static final int EXCHANGE_DIAMONDS = 5;
    private static final int EXCHANGE_COINS = 500;
    private static final int DAILY_PRICE = 1600;

    public ShopController(App app) {
        super(app);
    }

    private UserDataStore store() {
        return new UserDataStore(app.getCurrentUser().getUsername());
    }

    public Result handleShopList() {
        return Result.ok("Shop items:",
                "1. Pot | 2000 coins | unlocks one greenhouse pot (max 20)",
                "2. Plant Food | 3 diamonds | +1 plant food for the start of your next level (max 3)",
                "3. Random Seed Packet Bundle | 1000 coins | 5 seed packets of a random unlocked plant",
                "4. Choice Seed Packet Bundle | 5 diamonds | 10 seed packets of a chosen plant (-t)",
                "5. Currency Exchange | 5 diamonds | 500 coins",
                "6. Daily Offer | see 'shop daily'");
    }

    public Result handleShopDaily() {
        String plant = dailyPlant();
        String bought = store().get("daily.lastBuy", "");
        String status = bought.equals(todayKey()) ? " (already bought today)" : "";
        return Result.ok("Daily offer (resets at 00:00):",
                "6. Daily Bundle | " + DAILY_PRICE + " coins (20% off 2000) | 10 seed packets of "
                        + plant + status);
    }

    /** The daily plant is derived from the date, so it is stable for the whole day. */
    private String dailyPlant() {
        List<String> unlocked = MainController.unlockedPlants(store());
        Random dayRandom = new Random(LocalDate.now().toEpochDay());
        return unlocked.get(dayRandom.nextInt(unlocked.size()));
    }

    private String todayKey() {
        return LocalDate.now().toString();
    }

    public Result handleShopBuy(String itemId, int count, String plantType) {
        if (count < 1) {
            return Result.fail("Count must be at least 1.");
        }
        switch (itemId) {
            case "1":
                return buyPots(count);
            case "2":
                return buyPlantFood(count);
            case "3":
                return buyRandomBundle(count);
            case "4":
                return buyChoiceBundle(count, plantType);
            case "5":
                return buyExchange(count);
            case "6":
                return buyDaily(count);
            default:
                return Result.fail("No shop item with id " + itemId + ".");
        }
    }

    private Result buyPots(int count) {
        int owned = app.getCurrentUser().getPots().getAmount();
        if (owned + count > MAX_POTS - 5) {
            return Result.fail("The greenhouse only has room for " + MAX_POTS + " pots.");
        }
        Result payment = UserManager.getInstance().spendCoins(POT_PRICE * count);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        return UserManager.getInstance().addPots(count);
    }

    private Result buyPlantFood(int count) {
        UserDataStore store = store();
        int current = store.getInt("plantFoods", 0);
        if (current + count > 3) {
            return Result.fail("You can store at most 3 plant foods (you have " + current + ").");
        }
        Result payment = UserManager.getInstance().spendDiamonds(PLANT_FOOD_DIAMONDS * count);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        store.setInt("plantFoods", current + count);
        store.save();
        return Result.ok("Plant food purchased. Stored for your next level: " + (current + count));
    }

    private Result buyRandomBundle(int count) {
        Result payment = UserManager.getInstance().spendCoins(RANDOM_BUNDLE_COINS * count);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        UserDataStore store = store();
        List<String> unlocked = MainController.unlockedPlants(store);
        Random random = new Random();
        Result result = Result.ok();
        for (int i = 0; i < count; i++) {
            String plant = unlocked.get(random.nextInt(unlocked.size()));
            int packets = store.addInt("packets." + plant, 5);
            result.addMessage("You got 5 seed packets of " + plant + " (total " + packets + ").");
        }
        store.save();
        return result;
    }

    private Result buyChoiceBundle(int count, String plantType) {
        if (plantType == null) {
            return Result.fail("Choose a plant with -t <plant_type> for this bundle.");
        }
        UserDataStore store = store();
        String name = resolveUnlockedPlant(store, plantType);
        if (name == null) {
            return Result.fail("You can only buy packets for plants you have unlocked.");
        }
        Result payment = UserManager.getInstance().spendDiamonds(CHOICE_BUNDLE_DIAMONDS * count);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        int packets = store.addInt("packets." + name, 10 * count);
        store.save();
        return Result.ok("You got " + 10 * count + " seed packets of " + name
                + " (total " + packets + ").");
    }

    private Result buyExchange(int count) {
        Result payment = UserManager.getInstance().spendDiamonds(EXCHANGE_DIAMONDS * count);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        return UserManager.getInstance().addCoins(EXCHANGE_COINS * count);
    }

    private Result buyDaily(int count) {
        if (count != 1) {
            return Result.fail("The daily offer can only be bought once per day.");
        }
        UserDataStore store = store();
        if (todayKey().equals(store.get("daily.lastBuy", ""))) {
            return Result.fail("You already bought today's offer. It resets at 00:00.");
        }
        Result payment = UserManager.getInstance().spendCoins(DAILY_PRICE);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        String plant = dailyPlant();
        int packets = store.addInt("packets." + plant, 10);
        store.set("daily.lastBuy", todayKey());
        store.save();
        return Result.ok("Daily bundle: 10 seed packets of " + plant + " (total " + packets + ").");
    }

    private String resolveUnlockedPlant(UserDataStore store, String typeName) {
        models.entities.plant.PlantType type = GameSession.resolvePlantType(typeName);
        if (type == null) {
            return null;
        }
        return MainController.unlockedPlants(store).contains(type.getName()) ? type.getName() : null;
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == Menus.GREENHOUSE || menu == Menus.MAIN) {
            app.navigateTo(menu);
            return Result.ok("Redirected to " + menuName + " menu");
        }
        return Result.fail(menu == null ? "No menu with the given name"
                : "You can't move to " + menuName + " from the shop");
    }

    /** The shop is reached from the greenhouse, so exiting returns there. */
    public Result handleExit() {
        app.navigateTo(Menus.GREENHOUSE);
        return Result.ok("Redirected to Greenhouse menu");
    }
}
