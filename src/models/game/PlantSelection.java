package models.game;

import models.Result;
import models.entities.plant.PlantCategory;
import models.entities.plant.PlantType;
import models.progress.level.special.SpecialLevelType;

import java.util.ArrayList;
import java.util.List;

/**
 * The pre-game plant picking state: which seeds the player chose for the
 * level (doc: 8 slots by default) and the level-specific selection rules
 * (conveyor belt, locked plants, plant-what-you-get).
 */
public class PlantSelection {
    private static final int DEFAULT_SLOTS = 8;

    private final List<PlantSlot> slots = new ArrayList<>();
    private final List<String> unlockedPlantNames;
    private final SpecialLevelType specialType;

    public PlantSelection(List<String> unlockedPlantNames, SpecialLevelType specialType) {
        this.unlockedPlantNames = unlockedPlantNames;
        this.specialType = specialType;
    }

    public Result listAllPlants() {
        Result result = Result.ok("All plants defined in the game:");
        for (PlantType type : PlantType.values()) {
            result.addMessage("- " + type.getName() + " | cost: " + type.getCost()
                    + " | category: " + type.getCategory());
        }
        return result;
    }

    public Result listAvailablePlants() {
        Result result = Result.ok("Plants you can pick for this level:");
        for (String name : unlockedPlantNames) {
            result.addMessage("- " + name);
        }
        return result;
    }

    public Result add(String typeName, boolean preparationPhase) {
        if (!preparationPhase) {
            return Result.fail("You can only pick plants before starting the game.");
        }
        if (specialType == SpecialLevelType.CONVEYOR_BELT) {
            return Result.fail("This is a conveyor belt level; plants arrive on the belt.");
        }
        PlantType type = Names.plant(typeName);
        if (type == null) {
            return Result.fail("No plant with this name exists.");
        }
        if (!isUnlocked(type)) {
            return Result.fail("You have not unlocked this plant yet.");
        }
        if (isLockedInThisLevel(type)) {
            return Result.fail("This plant is locked in this level.");
        }
        if (findSlot(type) != null) {
            return Result.fail("This plant is already selected.");
        }
        if (slots.size() >= DEFAULT_SLOTS) {
            return Result.fail("All " + DEFAULT_SLOTS + " plant slots are full.");
        }
        slots.add(new PlantSlot(type));
        return Result.ok(type.getName() + " added to your selection.");
    }

    private boolean isLockedInThisLevel(PlantType type) {
        if (specialType == SpecialLevelType.PLANT_WHAT_YOU_GET
                && type.getCategory() == PlantCategory.SUN_PRODUCER) {
            return true;
        }
        if (specialType == SpecialLevelType.LOCKED_PLANTS) {
            int index = unlockedPlantNames.indexOf(type.getName());
            return index >= 0 && index % 2 == 1;
        }
        return false;
    }

    public Result remove(String typeName, boolean preparationPhase) {
        if (!preparationPhase) {
            return Result.fail("You can only change your selection before starting the game.");
        }
        PlantType type = Names.plant(typeName);
        if (type == null) {
            return Result.fail("No plant with this name exists.");
        }
        PlantSlot slot = findSlot(type);
        if (slot == null) {
            return Result.fail("This plant is not in your selection.");
        }
        slots.remove(slot);
        return Result.ok(type.getName() + " removed from your selection.");
    }

    /** Marks a selected plant as boosted (payment is handled by the controller). */
    public Result markBoosted(String typeName) {
        PlantType type = Names.plant(typeName);
        PlantSlot slot = type == null ? null : findSlot(type);
        if (slot == null) {
            return Result.fail("This plant is not in your selection.");
        }
        if (slot.isBoosted()) {
            return Result.fail("This plant is already boosted.");
        }
        slot.setBoosted(true);
        return Result.ok(type.getName() + " is boosted for this level.");
    }

    public PlantSlot findSlot(PlantType type) {
        for (PlantSlot slot : slots) {
            if (slot.getType() == type) {
                return slot;
            }
        }
        return null;
    }

    private boolean isUnlocked(PlantType type) {
        for (String name : unlockedPlantNames) {
            if (Names.plant(name) == type) {
                return true;
            }
        }
        return false;
    }

    public List<PlantSlot> getSlots() {
        return slots;
    }

    public boolean isEmpty() {
        return slots.isEmpty();
    }

    public boolean isFull() {
        return slots.size() >= DEFAULT_SLOTS;
    }

    public List<String> getUnlockedPlantNames() {
        return unlockedPlantNames;
    }
}
