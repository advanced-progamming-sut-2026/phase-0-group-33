package views.menus;

import controllers.menuControllers.GameController;
import models.Result;
import models.enums.regexes.commandHandlers.GameCommands;
import models.enums.regexes.commandHandlers.GlobalCommands;

import java.util.regex.Matcher;

public class GameMenu implements AppMenu {
    private final GameController controller;

    public GameMenu(GameController controller) {
        this.controller = controller;
    }

    @Override
    public boolean processCommand(String cmd) {
        String input = cmd.trim();

        Matcher showAllPlantsMatcher = GameCommands.SHOW_ALL_PLANTS.pattern.matcher(input);
        if (showAllPlantsMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showAvailablePlantsMatcher = GameCommands.SHOW_AVAILABLE_PLANTS.pattern.matcher(input);
        if (showAvailablePlantsMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher addPlantMatcher = GameCommands.ADD_PLANT.pattern.matcher(input);
        if (addPlantMatcher.matches()) {
            String type = addPlantMatcher.group("type");
            // TODO
            return true;
        }

        Matcher removePlantMatcher = GameCommands.REMOVE_PLANT.pattern.matcher(input);
        if (removePlantMatcher.matches()) {
            String type = removePlantMatcher.group("type");
            // TODO
            return true;
        }

        Matcher boostPlantMatcher = GameCommands.BOOST_PLANT.pattern.matcher(input);
        if (boostPlantMatcher.matches()) {
            String type = boostPlantMatcher.group("type");
            // TODO
            return true;
        }

        Matcher startGameMatcher = GameCommands.START_GAME.pattern.matcher(input);
        if (startGameMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher startZombieWavesMatcher = GameCommands.START_ZOMBIE_WAVES.pattern.matcher(input);
        if (startZombieWavesMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher advanceTimeMatcher = GameCommands.ADVANCE_TIME.pattern.matcher(input);
        if (advanceTimeMatcher.matches()) {
            String count = advanceTimeMatcher.group("count");
            // TODO
            return true;
        }

        Matcher collectSunMatcher = GameCommands.COLLECT_SUN.pattern.matcher(input);
        if (collectSunMatcher.matches()) {
            String x = collectSunMatcher.group("x");
            String y = collectSunMatcher.group("y");
            // TODO
            return true;
        }

        Matcher showSunAmountMatcher = GameCommands.SHOW_SUN_AMOUNT.pattern.matcher(input);
        if (showSunAmountMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher cheatAddSunMatcher = GameCommands.CHEAT_ADD_SUN.pattern.matcher(input);
        if (cheatAddSunMatcher.matches()) {
            String count = cheatAddSunMatcher.group("count");
            // TODO
            return true;
        }

        Matcher releaseNukeMatcher = GameCommands.RELEASE_NUKE.pattern.matcher(input);
        if (releaseNukeMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher plantPlantMatcher = GameCommands.PLANT_PLANT.pattern.matcher(input);
        if (plantPlantMatcher.matches()) {
            String type = plantPlantMatcher.group("type");
            String x = plantPlantMatcher.group("x");
            String y = plantPlantMatcher.group("y");
            // TODO
            return true;
        }

        Matcher cheatRemoveCooldownMatcher = GameCommands.CHEAT_REMOVE_COOLDOWN.pattern.matcher(input);
        if (cheatRemoveCooldownMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher pluckPlantMatcher = GameCommands.PLUCK_PLANT.pattern.matcher(input);
        if (pluckPlantMatcher.matches()) {
            String x = pluckPlantMatcher.group("x");
            String y = pluckPlantMatcher.group("y");
            // TODO
            return true;
        }

        Matcher feedPlantMatcher = GameCommands.FEED_PLANT.pattern.matcher(input);
        if (feedPlantMatcher.matches()) {
            String x = feedPlantMatcher.group("x");
            String y = feedPlantMatcher.group("y");
            // TODO
            return true;
        }

        Matcher cheatAddPlantFoodMatcher = GameCommands.CHEAT_ADD_PLANT_FOOD.pattern.matcher(input);
        if (cheatAddPlantFoodMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showMapMatcher = GameCommands.SHOW_MAP.pattern.matcher(input);
        if (showMapMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showPlantsStatusMatcher = GameCommands.SHOW_PLANTS_STATUS.pattern.matcher(input);
        if (showPlantsStatusMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher showTileStatusMatcher = GameCommands.SHOW_TILE_STATUS.pattern.matcher(input);
        if (showTileStatusMatcher.matches()) {
            String x = showTileStatusMatcher.group("x");
            String y = showTileStatusMatcher.group("y");
            // TODO
            return true;
        }

        Matcher zombiesInfoMatcher = GameCommands.ZOMBIES_INFO.pattern.matcher(input);
        if (zombiesInfoMatcher.matches()) {
            // TODO
            return true;
        }

        Matcher cheatSpawnZombieMatcher = GameCommands.CHEAT_SPAWN_ZOMBIE.pattern.matcher(input);
        if (cheatSpawnZombieMatcher.matches()) {
            String type = cheatSpawnZombieMatcher.group("type");
            String x = cheatSpawnZombieMatcher.group("x");
            String y = cheatSpawnZombieMatcher.group("y");
            // TODO
            return true;
        }

        Matcher changeMenuMatcher = GlobalCommands.CHANGE_MENU.pattern.matcher(input);
        if (changeMenuMatcher.matches()) {
            String menu = changeMenuMatcher.group("menu");
            // TODO
            return true;
        }

        Matcher showMenuMatcher = GlobalCommands.SHOW_MENU.pattern.matcher(input);
        if (showMenuMatcher.matches()) {
            Result result = new Result();
            result.setSuccess(true);
            result.addMessage("Game menu");
            printResultMsg(result);
            return true;
        }

        Matcher exitMatcher = GlobalCommands.EXIT.pattern.matcher(input);
        if (exitMatcher.matches()) {
            // TODO
            return true;
        }

        return false;
    }
}