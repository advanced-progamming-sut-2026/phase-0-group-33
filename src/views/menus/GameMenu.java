package views.menus;

import controllers.menuControllers.GameController;
import models.Result;
import models.enums.regexes.commandHandlers.GameCommands;
import models.enums.regexes.commandHandlers.GlobalCommands;
import views.CommandRouter;

public class GameMenu implements AppMenu {
    private final CommandRouter router = new CommandRouter();

    public GameMenu(GameController controller) {
        registerPreparationCommands(controller);
        registerBattleCommands(controller);
        registerInfoAndGlobalCommands(controller);
    }

    private void registerPreparationCommands(GameController controller) {
        router.add(GameCommands.SHOW_ALL_PLANTS.pattern, matcher -> controller.handleShowAllPlants())
                .add(GameCommands.SHOW_AVAILABLE_PLANTS.pattern,
                        matcher -> controller.handleShowAvailablePlants())
                .add(GameCommands.ADD_PLANT.pattern,
                        matcher -> controller.handleAddPlant(matcher.group("type")))
                .add(GameCommands.REMOVE_PLANT.pattern,
                        matcher -> controller.handleRemovePlant(matcher.group("type")))
                .add(GameCommands.BOOST_PLANT.pattern,
                        matcher -> controller.handleBoostPlant(matcher.group("type")))
                .add(GameCommands.START_GAME.pattern, matcher -> controller.handleStartGame())
                .add(GameCommands.START_ZOMBIE_WAVES.pattern,
                        matcher -> controller.handleStartZombieWaves());
    }

    private void registerBattleCommands(GameController controller) {
        router.add(GameCommands.ADVANCE_TIME.pattern, matcher ->
                        controller.handleAdvanceTime(Integer.parseInt(matcher.group("count"))))
                .add(GameCommands.COLLECT_SUN.pattern, matcher -> controller.handleCollectSun(
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GameCommands.CHEAT_ADD_SUN.pattern, matcher ->
                        controller.handleCheatAddSun(Integer.parseInt(matcher.group("count"))))
                .add(GameCommands.RELEASE_NUKE.pattern, matcher -> controller.handleReleaseNuke())
                .add(GameCommands.PLANT_PLANT.pattern, matcher -> controller.handlePlant(
                        matcher.group("type"),
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GameCommands.CHEAT_REMOVE_COOLDOWN.pattern,
                        matcher -> controller.handleCheatRemoveCooldown())
                .add(GameCommands.PLUCK_PLANT.pattern, matcher -> controller.handlePluck(
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GameCommands.FEED_PLANT.pattern, matcher -> controller.handleFeedPlant(
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GameCommands.CHEAT_ADD_PLANT_FOOD.pattern,
                        matcher -> controller.handleCheatAddPlantFood())
                .add(GameCommands.CHEAT_SPAWN_ZOMBIE.pattern, matcher -> controller.handleCheatSpawnZombie(
                        matcher.group("type"),
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))));
    }

    private void registerInfoAndGlobalCommands(GameController controller) {
        router.add(GameCommands.SHOW_SUN_AMOUNT.pattern, matcher -> controller.handleShowSunAmount())
                .add(GameCommands.SHOW_MAP.pattern, matcher -> controller.handleShowMap())
                .add(GameCommands.SHOW_PLANTS_STATUS.pattern,
                        matcher -> controller.handleShowPlantsStatus())
                .add(GameCommands.SHOW_TILE_STATUS.pattern, matcher -> controller.handleShowTileStatus(
                        Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y"))))
                .add(GameCommands.ZOMBIES_INFO.pattern, matcher -> controller.handleZombiesInfo())
                .add(GlobalCommands.SHOW_MENU.pattern, matcher -> Result.ok("Game menu"))
                .add(GlobalCommands.CHANGE_MENU.pattern,
                        matcher -> controller.handleMenuChange(matcher.group("menu")))
                .add(GlobalCommands.EXIT.pattern, matcher -> controller.handleExit());
    }

    @Override
    public boolean processCommand(String cmd) {
        return router.dispatch(cmd.trim());
    }
}
