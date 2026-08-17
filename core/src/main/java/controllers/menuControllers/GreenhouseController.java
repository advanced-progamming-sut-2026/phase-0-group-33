package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.enums.Menus;
import utils.UserDataStore;

import java.util.List;
import java.util.Random;

public class GreenhouseController extends BaseController {
    public static final int COLS = 4;
    public static final int ROWS = 3;
    public static final int FREE_POTS = COLS;
    public static final int MAX_POTS = COLS * ROWS;
    private static final long HOUR_MILLIS = 60L * 60 * 1000;
    private static final String MARIGOLD = "Marigold";

    public GreenhouseController(App app) {
        super(app);
    }

    private UserDataStore store() {
        return UserDataStore.forUser(app.getCurrentUser().getUsername());
    }

    public int unlockedSlots() {
        return Math.min(MAX_POTS, FREE_POTS + app.getCurrentUser().getPots().getAmount());
    }

    public static int slotIndex(int x, int y) {
        return (y - 1) * COLS + x;
    }

    public Result handleShowGreenhouse() {
        UserDataStore store = store();
        Result result = Result.ok("Greenhouse (" + unlockedSlots() + "/" + MAX_POTS
                + " pots unlocked):");
        for (int y = 1; y <= ROWS; y++) {
            for (int x = 1; x <= COLS; x++) {
                result.addMessage("(" + x + ", " + y + "): " + describePot(store, x, y));
            }
        }
        return result;
    }

    private String describePot(UserDataStore store, int x, int y) {
        if (slotIndex(x, y) > unlockedSlots()) {
            return "locked";
        }
        String plant = store.get(potKey(x, y) + ".plant", null);
        if (plant == null) {
            return "empty";
        }
        long readyAt = store.getLong(potKey(x, y) + ".ready", 0);
        long remaining = readyAt - System.currentTimeMillis();
        if (remaining <= 0) {
            return plant + " [ready]";
        }
        long minutes = remaining / 60000 + 1;
        return plant + " [growing, ready in " + minutes / 60 + "h " + minutes % 60 + "m]";
    }

    private static String potKey(int x, int y) {
        return "gh." + x + "." + y;
    }

    public boolean isPotUnlocked(int x, int y) {
        return slotIndex(x, y) <= unlockedSlots();
    }

    public String potPlant(int x, int y) {
        return store().get(potKey(x, y) + ".plant", null);
    }

    public long potRemainingMillis(int x, int y) {
        return Math.max(0L, store().getLong(potKey(x, y) + ".ready", 0) - System.currentTimeMillis());
    }

    public int potSpeedUpCost(int x, int y) {
        long remaining = potRemainingMillis(x, y);
        return (int) ((remaining + HOUR_MILLIS - 1) / HOUR_MILLIS);
    }

    private static List<String> boostablePlants(UserDataStore store) {
        List<String> result = new java.util.ArrayList<>();
        for (String name : MainController.unlockedPlants(store)) {
            models.entities.plant.PlantType type = models.game.Names.plant(name);
            if (type != null && type.getBaseHp() > 0) {
                result.add(name);
            }
        }
        return result;
    }

    public Result handlePlantPot(int x, int y) {
        if (loggedOut()) {
            return notLoggedIn();
        }
        if (x < 1 || x > COLS || y < 1 || y > ROWS) {
            return Result.fail("Pot coordinates must be x in 1.." + COLS + " and y in 1.." + ROWS + ".");
        }
        if (slotIndex(x, y) > unlockedSlots()) {
            return Result.fail("This pot is locked. Buy a pot in the shop to unlock it.");
        }
        UserDataStore store = store();
        if (store.get(potKey(x, y) + ".plant", null) != null) {
            return Result.fail("This pot is already occupied.");
        }
        List<String> unlocked = boostablePlants(store);
        Random random = new Random();
        boolean marigold = unlocked.isEmpty() || random.nextBoolean();
        String plant = marigold ? MARIGOLD : unlocked.get(random.nextInt(unlocked.size()));
        long hours = marigold ? 2 : 8;
        store.set(potKey(x, y) + ".plant", plant);
        store.setLong(potKey(x, y) + ".ready", System.currentTimeMillis() + hours * HOUR_MILLIS);
        store.save();
        return Result.ok(plant + " planted at (" + x + ", " + y + "); it needs "
                + hours + " hours to grow.");
    }

    public Result handleCollect(int x, int y) {
        if (loggedOut()) {
            return notLoggedIn();
        }
        UserDataStore store = store();
        String plant = store.get(potKey(x, y) + ".plant", null);
        if (plant == null) {
            return Result.fail("There is nothing growing in this pot.");
        }
        if (store.getLong(potKey(x, y) + ".ready", 0) > System.currentTimeMillis()) {
            return Result.fail("This plant has not finished growing yet.");
        }
        store.remove(potKey(x, y) + ".plant");
        store.remove(potKey(x, y) + ".ready");
        Result result;
        if (plant.equals(MARIGOLD)) {
            UserManager.getInstance().addCoins(500);
            result = Result.ok("Marigold collected: +500 coins.");
        } else if (store.getInt("boost." + plant, 0) > 0) {
            result = Result.ok(plant + " collected, but you already stored a boost for it;"
                    + " the pot is now empty.");
        } else {
            store.setInt("boost." + plant, 1);
            result = Result.ok(plant + " collected: one boost stored for your next game.");
        }
        store.save();
        return result;
    }

    public Result handleGrow(int x, int y) {
        if (loggedOut()) {
            return notLoggedIn();
        }
        UserDataStore store = store();
        String plant = store.get(potKey(x, y) + ".plant", null);
        if (plant == null) {
            return Result.fail("There is nothing growing in this pot.");
        }
        long remaining = store.getLong(potKey(x, y) + ".ready", 0) - System.currentTimeMillis();
        if (remaining <= 0) {
            return Result.fail("This plant is already fully grown.");
        }
        int cost = (int) ((remaining + HOUR_MILLIS - 1) / HOUR_MILLIS);
        Result payment = UserManager.getInstance().spendDiamonds(cost);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        store.setLong(potKey(x, y) + ".ready", System.currentTimeMillis());
        store.save();
        return Result.ok(plant + " is now fully grown (paid " + cost + " diamonds).");
    }

    public Result handleEnterShop() {
        app.navigateTo(Menus.SHOP);
        return Result.ok("Redirected to Shop menu");
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == Menus.MAIN || menu == Menus.SHOP) {
            app.navigateTo(menu);
            return Result.ok("Redirected to " + menuName + " menu");
        }
        return Result.fail(menu == null ? "No menu with the given name"
                : "You can't move to " + menuName + " from the greenhouse");
    }

    public Result handleExit() {
        app.navigateTo(Menus.MAIN);
        return Result.ok("Redirected to Main menu");
    }
}
