package controllers.menuControllers;

import controllers.managers.UserManager;
import models.App;
import models.Result;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.enums.Menus;
import models.game.GameSession;
import utils.NewsStore;
import utils.UserDataStore;

import java.util.List;

public class CollectionController extends BaseController {
    private static final int PURCHASE_COST = 2000;
    private static final int UPGRADE_COIN_COST = 1000;
    private static final int UPGRADE_PACKET_COST = 5;
    private static final int MAX_PLANT_LEVEL = 5;

    public CollectionController(App app) {
        super(app);
    }

    private UserDataStore store() {
        return UserDataStore.forUser(app.getCurrentUser().getUsername());
    }

    public Result handleShowPlants() {
        UserDataStore store = store();
        Result result = Result.ok("Your plants:");
        for (String name : MainController.unlockedPlants(store)) {
            int level = store.getInt("level." + name, 1);
            int packets = store.getInt("packets." + name, 0);
            result.addMessage("- " + name + " | level " + level + " | seed packets: " + packets);
        }
        return result;
    }

    public Result handleShowAllPlants() {
        Result result = Result.ok("All plants defined in the game:");
        for (PlantType type : PlantType.values()) {
            result.addMessage("- " + type.getName());
        }
        return result;
    }

    private java.util.Set<String> seenZombies() {
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        String stored = store().get("zombies", "");
        if (!stored.isEmpty()) {
            seen.addAll(java.util.Arrays.asList(stored.split(",")));
        }
        GameSession session = app.getCurrentGameSession();
        if (session != null) {
            for (ZombieType type : session.getEncounteredZombies()) {
                seen.add(type.getName());
            }
        }
        return seen;
    }

    public Result handleShowZombies() {
        java.util.Set<String> seen = seenZombies();
        if (seen.isEmpty()) {
            return Result.ok("You have not seen any zombies yet.");
        }
        Result result = Result.ok("Zombies you have seen:");
        for (String name : seen) {
            result.addMessage("- " + name);
        }
        return result;
    }

    public Result handleShowAllZombies() {
        Result result = Result.ok("All zombies defined in the game:");
        for (ZombieType type : ZombieType.values()) {
            result.addMessage("- " + type.getName());
        }
        return result;
    }

    public Result handleShowPlant(String plantName) {
        PlantType type = GameSession.resolvePlantType(plantName);
        if (type == null) {
            return Result.fail("No plant with this name exists.");
        }
        Result result = Result.ok(type.getName() + ":");
        result.addMessage("    category: " + type.getCategory());
        result.addMessage("    tags: " + type.getTags());
        result.addMessage("    sun cost: " + type.getCost());
        result.addMessage("    hp: " + type.getBaseHp());
        result.addMessage("    damage: " + (type.isInstantKill() ? "instant kill" : type.getDamage()));
        result.addMessage("    action interval: " + type.getActionInterval() + "s");
        result.addMessage("    recharge: " + type.getRecharge() + "s");
        return result;
    }

    public Result handleShowZombie(String zombieName) {
        ZombieType type = GameSession.resolveZombieType(zombieName);
        if (type == null) {
            return Result.fail("No zombie with this name exists.");
        }
        if (!seenZombies().contains(type.getName())) {
            return Result.fail("You have not encountered this zombie yet; its frame is empty.");
        }
        Result result = Result.ok(type.getName() + ":");
        result.addMessage("    hp: " + type.getHitpoints());
        result.addMessage("    speed: " + type.getSpeed() + " tiles/s");
        result.addMessage("    eat damage: " + type.getEatDps() + "/s");
        result.addMessage("    armor: " + type.getArmorType()
                + (type.getArmorType() == ZombieType.ArmorType.NONE
                ? "" : " (" + type.getArmorType().getArmorHitpoints() + " HP)"));
        result.addMessage("    wave cost: " + type.getWaveCost());
        return result;
    }

    public Result handleUpgradePlant(String plantName) {
        PlantType type = GameSession.resolvePlantType(plantName);
        if (type == null) {
            return Result.fail("No plant with this name exists.");
        }
        UserDataStore store = store();
        if (!MainController.unlockedPlants(store).contains(type.getName())) {
            return Result.fail("You have not unlocked this plant yet.");
        }
        int level = store.getInt("level." + type.getName(), 1);
        if (level >= MAX_PLANT_LEVEL) {
            return Result.fail(type.getName() + " is already at max level (" + MAX_PLANT_LEVEL + ").");
        }
        int packetCost = UPGRADE_PACKET_COST * level;
        int coinCost = UPGRADE_COIN_COST * level;
        int packets = store.getInt("packets." + type.getName(), 0);
        if (packets < packetCost) {
            return Result.fail("Not enough seed packets: you need " + packetCost
                    + " and have " + packets + ".");
        }
        Result payment = UserManager.getInstance().spendCoins(coinCost);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        store.setInt("packets." + type.getName(), packets - packetCost);
        store.setInt("level." + type.getName(), level + 1);
        store.save();
        return Result.ok(type.getName() + " upgraded to level " + (level + 1) + ".");
    }

    public Result handlePurchasePlant(String plantName) {
        PlantType type = GameSession.resolvePlantType(plantName);
        if (type == null) {
            return Result.fail("No plant with this name exists.");
        }
        UserDataStore store = store();
        List<String> unlocked = MainController.unlockedPlants(store);
        if (unlocked.contains(type.getName())) {
            return Result.fail("You already own this plant.");
        }
        Result payment = UserManager.getInstance().spendCoins(PURCHASE_COST);
        if (!payment.isSuccessfull()) {
            return payment;
        }
        store.set("plants", String.join(",", unlocked) + "," + type.getName());
        store.save();
        NewsStore.add(app.getCurrentUser().getUsername(), "New plant unlocked: " + type.getName());
        return Result.ok(type.getName() + " purchased for " + PURCHASE_COST + " coins.");
    }

    public Result handleMenuChange(String menuName) {
        Menus menu = Menus.getMenuByName(menuName);
        if (menu == Menus.GAME || menu == Menus.MAIN) {
            app.navigateTo(menu);
            return Result.ok("Redirected to " + menuName + " menu");
        }
        return Result.fail(menu == null ? "No menu with the given name"
                : "You can't move to " + menuName + " from the collection menu");
    }

    public Result handleExit() {
        if (app.getCurrentGameSession() == null) {
            app.navigateTo(Menus.MAIN);
            return Result.ok("Redirected to Main menu");
        }
        app.navigateTo(Menus.GAME);
        return Result.ok("Redirected to Game menu");
    }
}
