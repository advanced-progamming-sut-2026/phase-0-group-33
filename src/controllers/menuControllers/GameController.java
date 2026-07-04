package controllers.menuControllers;

import models.App;
import models.Result;

public class GameController extends BaseController {
    public GameController(App app) {
        super(app);
    }

    // Preparation commands
    public Result handleShowAllPlants() {
        return null;
    }

    public Result handleShowAvailablePlants() {
        return null;
    }

    public Result handleAddPlant(String type) {
        return null;
    }

    public Result handleRemovePlant(String type) {
        return null;
    }

    public Result handleBoostPlant(String type) {
        return null;
    }

    public Result handleStartGame() {
        return null;
    }

    public Result handleStartZombieWaves() {
        return null;
    }

    // Battle commands
    public Result handleAdvanceTime(int count) {
        return null;
    }

    public Result handleCollectSun(int x, int y) {
        return null;
    }

    public Result handleCheatAddSun(int count) {
        return null;
    }

    public Result handleReleaseNuke() {
        return null;
    }

    public Result handlePlant(String type, int x, int y) {
        return null;
    }

    public Result handleCheatRemoveCooldown() {
        return null;
    }

    public Result handlePluck(int x, int y) {
        return null;
    }

    public Result handleFeedPlant(int x, int y) {
        return null;
    }

    public Result handleCheatAddPlantFood() {
        return null;
    }

    public Result handleCheatSpawnZombie(String type, int x, int y) {
        return null;
    }

    // Info & global
    public Result handleShowSunAmount() {
        return null;
    }

    public Result handleShowMap() {
        return null;
    }

    public Result handleShowPlantsStatus() {
        return null;
    }

    public Result handleShowTileStatus(int x, int y) {
        return null;
    }

    public Result handleZombiesInfo() {
        return null;
    }

    public Result handleMenuChange(String menuName) {
        return null;
    }

    public Result handleExit() {
        return null;
    }
}