package controllers.managers;

import database.StaticPlantDAO;
import database.UserPlantDAO;
import models.Result;
import models.entities.plant.EffectivePlantStats;
import models.entities.plant.StaticPlant;
import models.user.UserPlantDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PlantManager {
    // Singleton Instance
    private static PlantManager instance;

    // DAOs
    private final UserPlantDAO userPlantDAO;
    private final StaticPlantDAO staticPlantDAO;

    // Thread-safe lightweight cache for static plant definitions to prevent redundant DB hits
    private final Map<Integer, StaticPlant> staticPlantCache;

    // Constants
    private static final int MAX_PLANT_LEVEL = 10;

    private PlantManager() {
        this.userPlantDAO = new UserPlantDAO();
        this.staticPlantDAO = new StaticPlantDAO();
        this.staticPlantCache = new ConcurrentHashMap<>();
    }

    public static synchronized PlantManager getInstance() {
        if (instance == null) {
            instance = new PlantManager();
        }
        return instance;
    }

    public Result unlockPlant(String username, int plantId) {
        Result result = new Result();

        StaticPlant basePlant = getStaticPlantDefinition(plantId);
        if (basePlant == null) {
            result.setSuccess(false);
            result.addMessage("Plant ID " + plantId + " does not exist in the game definitions.");
            return result;
        }

        if (isPlantUnlocked(username, plantId)) {
            result.setSuccess(false);
            result.addMessage("User " + username + " already has plant ID " + plantId);
            return result;
        }

        boolean success = userPlantDAO.unlockPlant(username, plantId);
        if (!success) {
            result.setSuccess(false);
            result.addMessage("Database error occurred while trying to unlock the plant.");
            return result;
        }

        result.setSuccess(true);
        result.addMessage("Successfully unlocked plant: " + basePlant.getName());
        result.setData(new UserPlantDTO(username, basePlant, 1));
        return result;
    }

    public Result upgradePlant(String username, int plantId) {
        Result result = new Result();

        Result detailsResult = getPlantDetails(username, plantId);
        if (!detailsResult.isSuccessfull()) {
            return detailsResult; // Pass through the failure (e.g., plant not owned)
        }

        UserPlantDTO currentPlant = (UserPlantDTO) detailsResult.getData();

        if (currentPlant.getUpgradeLevel() >= MAX_PLANT_LEVEL) {
            result.setSuccess(false);
            result.addMessage(currentPlant.getBasePlant().getName()
                    + " is already at max level (" + MAX_PLANT_LEVEL + ").");
            return result;
        }

        // TODO: integrate resource system.
        // Example: Result paymentResult = UserManager.getInstance().spendCoins(upgradeCost);
        // if (!paymentResult.isSuccess()) return paymentResult;

        boolean success = userPlantDAO.upgradePlant(username, plantId);
        if (!success) {
            result.setSuccess(false);
            result.addMessage("Database error occurred while trying to upgrade the plant.");
            return result;
        }

        currentPlant.setUpgradeLevel(currentPlant.getUpgradeLevel() + 1);

        result.setSuccess(true);
        result.addMessage("Successfully upgraded plant to level " + currentPlant.getUpgradeLevel());
        result.setData(currentPlant);
        return result;
    }

    public Result getUserPlants(String username) {
        Result result = new Result();
        List<UserPlantDTO> plants = userPlantDAO.getUserPlantsWithStats(username);

        result.setSuccess(true);
        result.setData(plants);
        return result;
    }

    public Result getPlantDetails(String username, int plantId) {
        Result result = new Result();
        List<UserPlantDTO> allPlants = userPlantDAO.getUserPlantsWithStats(username);

        for (UserPlantDTO plant : allPlants) {
            if (plant.getBasePlant().getId() == plantId) {
                result.setSuccess(true);
                result.setData(plant);
                return result;
            }
        }

        result.setSuccess(false);
        result.addMessage("User " + username + " does not own plant ID " + plantId);
        return result;
    }

    public boolean isPlantUnlocked(String username, int plantId) {
        return getPlantLevel(username, plantId) > 0;
    }


    // return The level of the plant, or 0 if it is not unlocked.
    public int getPlantLevel(String username, int plantId) {
        Result res = getPlantDetails(username, plantId);
        if (res.isSuccessfull() && res.getData() != null) {
            return ((UserPlantDTO) res.getData()).getUpgradeLevel();
        }
        return 0;
    }

    /**
     * Calculates the effective runtime stats of a plant, factoring in static base stats and
     * the dynamic modifiers granted by the user's current upgrade level.
     *
     * @param username The username of the player.
     * @param plantId  The static ID of the plant.
     * @return A Result containing an EffectivePlantStats object.
     */
    public Result getPlantEffectiveStats(String username, int plantId) {
        Result result = new Result();

        Result detailsResult = getPlantDetails(username, plantId);
        if (!detailsResult.isSuccessfull()) {
            return detailsResult; // Pass through failure
        }

        UserPlantDTO userPlant = (UserPlantDTO) detailsResult.getData();
        StaticPlant base = userPlant.getBasePlant();
        int level = userPlant.getUpgradeLevel();

        int effectiveHp = base.getBaseHp();
        int effectiveCost = base.getCost();

        // Dynamic stat modifier parsing
        if (level >= 3 && base.getLvl3Upgrade() != null) {
            String upg = base.getLvl3Upgrade();
            if (upg.contains("HP +")) {
                try {
                    effectiveHp += Integer.parseInt(upg.replace("HP +", "").trim());
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse HP upgrade string: " + upg);
                }
            }
        }

        if (level >= 4 && base.getLvl4Upgrade() != null) {
            String upg = base.getLvl4Upgrade();
            if (upg.contains("Cost -")) {
                try {
                    effectiveCost -= Integer.parseInt(upg.replace("Cost -", "").trim());
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse Cost upgrade string: " + upg);
                }
            }
        }

        EffectivePlantStats stats = new EffectivePlantStats(
                effectiveHp, effectiveCost, base.getBaseDamage(), base.getRechargeTime());

        result.setSuccess(true);
        result.setData(stats);
        return result;
    }

    private StaticPlant getStaticPlantDefinition(int plantId) {
        if (staticPlantCache.containsKey(plantId)) {
            return staticPlantCache.get(plantId);
        }

        StaticPlant plant = staticPlantDAO.getBasePlant(plantId);
        if (plant != null) {
            staticPlantCache.put(plantId, plant);
        }

        return plant;
    }
}