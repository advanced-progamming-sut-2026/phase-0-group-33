package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.entities.plant.PlantType;
import models.game.Names;
import utils.UserDataStore;

import java.util.ArrayList;
import java.util.List;

public class ZenGardenController extends BaseController {

    public static final int SLOTS = 6;
    public static final int WATER_REWARD = 150;
    public static final int HAPPY_BONUS = 250;
    public static final int HAPPY_AT = 5;

    private static final long THIRST_MILLIS = 4L * 60 * 60 * 1000;

    public ZenGardenController(App app) {
        super(app);
    }

    private UserDataStore store() {
        return UserDataStore.forUser(app.getCurrentUser().getUsername());
    }

    private static String slotKey(int slot) {
        return "zen.slot" + slot;
    }

    public String plantAt(int slot) {
        return store().get(slotKey(slot) + ".plant", null);
    }

    public int wateringsOf(int slot) {
        return store().getInt(slotKey(slot) + ".water", 0);
    }

    public boolean isThirsty(int slot) {
        String plant = plantAt(slot);
        if (plant == null) {
            return false;
        }
        return store().getLong(slotKey(slot) + ".next", 0) <= System.currentTimeMillis();
    }

    public long millisUntilThirsty(int slot) {
        return Math.max(0, store().getLong(slotKey(slot) + ".next", 0)
                - System.currentTimeMillis());
    }

    public List<String> availablePlants() {
        List<String> names = new ArrayList<>();
        for (String name : MainController.unlockedPlants(store())) {
            names.add(name);
        }
        return names;
    }

    public Result handleShowGarden() {
        if (loggedOut()) {
            return notLoggedIn();
        }
        Result result = Result.ok("Zen Garden (" + SLOTS + " beds):");
        for (int slot = 1; slot <= SLOTS; slot++) {
            String plant = plantAt(slot);
            if (plant == null) {
                result.addMessage(slot + ". empty bed");
                continue;
            }
            String thirst = isThirsty(slot) ? "thirsty"
                    : (millisUntilThirsty(slot) / 60000) + "m until thirsty";
            result.addMessage(slot + ". " + plant + " | watered "
                    + wateringsOf(slot) + " times | " + thirst);
        }
        result.addMessage("Water a plant with 'water plant -s <slot>'"
                + " to earn " + WATER_REWARD + " coins.");
        return result;
    }

    public Result handlePlaceInGarden(String typeName, int slot) {
        if (loggedOut()) {
            return notLoggedIn();
        }
        if (slot < 1 || slot > SLOTS) {
            return Result.fail("The Zen Garden only has beds 1 to " + SLOTS + ".");
        }
        PlantType type = Names.plant(typeName);
        if (type == null) {
            return Result.fail("No plant with this name exists.");
        }
        if (!MainController.unlockedPlants(store()).contains(type.getName())) {
            return Result.fail("You have not unlocked " + type.getName() + " yet.");
        }
        if (plantAt(slot) != null) {
            return Result.fail("Bed " + slot + " already holds " + plantAt(slot) + ".");
        }
        UserDataStore store = store();
        store.set(slotKey(slot) + ".plant", type.getName());
        store.setInt(slotKey(slot) + ".water", 0);
        store.setLong(slotKey(slot) + ".next", System.currentTimeMillis());
        store.save();
        return Result.ok(type.getName() + " moved into bed " + slot
                + ". Water it to keep it happy.");
    }

    public Result handleWater(int slot) {
        if (loggedOut()) {
            return notLoggedIn();
        }
        String plant = plantAt(slot);
        if (plant == null) {
            return Result.fail("Bed " + slot + " is empty.");
        }
        if (!isThirsty(slot)) {
            long minutes = millisUntilThirsty(slot) / 60000;
            return Result.fail(plant + " is not thirsty yet; come back in "
                    + Math.max(1, minutes) + " minutes.");
        }
        UserDataStore store = store();
        int waterings = wateringsOf(slot) + 1;
        store.setInt(slotKey(slot) + ".water", waterings);
        store.setLong(slotKey(slot) + ".next", System.currentTimeMillis() + THIRST_MILLIS);
        store.save();
        int reward = WATER_REWARD;
        String extra = "";
        if (waterings % HAPPY_AT == 0) {
            reward += HAPPY_BONUS;
            extra = " " + plant + " is thrilled and paid a bonus!";
        }
        UserManager.getInstance().addCoins(reward);
        return Result.ok("You watered " + plant + " and earned " + reward + " coins." + extra);
    }

    public Result handleReturnPlant(int slot) {
        if (loggedOut()) {
            return notLoggedIn();
        }
        String plant = plantAt(slot);
        if (plant == null) {
            return Result.fail("Bed " + slot + " is empty.");
        }
        UserDataStore store = store();
        store.remove(slotKey(slot) + ".plant");
        store.remove(slotKey(slot) + ".water");
        store.remove(slotKey(slot) + ".next");
        store.save();
        return Result.ok(plant + " left bed " + slot + ".");
    }

    public Result handleEnterGarden() {
        if (loggedOut()) {
            return notLoggedIn();
        }
        app.navigateTo(models.enums.Menus.ZEN_GARDEN);
        return Result.ok("Redirected to Zen Garden menu");
    }
}
