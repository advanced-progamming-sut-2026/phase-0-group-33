package controllers.managers;

import models.Result;
import models.entities.plant.PlantCategory;
import models.entities.plant.PlantTag;
import models.entities.plant.PlantType;
import models.entities.zombie.Zombie;
import models.game.GameMode;
import models.game.GamePhase;
import models.game.GameSession;
import models.game.Names;
import models.game.PlacedPlant;
import models.game.PlantSlot;
import models.map.TerrainType;
import models.map.Tile;

import java.util.ArrayList;

public class PlantingManager {
    private final GameSession session;

    public PlantingManager(GameSession session) {
        this.session = session;
    }

    public Result plant(String typeName, int x, int y) {
        if (session.getPhase() != GamePhase.BATTLE) {
            return Result.fail("Start the game before planting.");
        }
        if (session.getMode() == GameMode.I_ZOMBIE) {
            return Result.fail("You command the zombies here; use 'place zombie' instead.");
        }
        PlantType type = Names.plant(typeName);
        if (type == null) {
            return Result.fail("No plant with this name exists.");
        }
        if (x < 1 || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            return Result.fail("Coordinates are outside the lawn.");
        }
        if (session.getMode() == GameMode.WALLNUT_BOWLING) {
            return session.getMinigameManager().placeBowlingNut(type, x, y);
        }
        PlantSlot slot = session.findSlot(type);
        if (slot == null) {
            return Result.fail("This plant is not in your selection.");
        }
        if (!slot.isReady()) {
            int seconds = slot.getCooldownTicks() / GameSession.TICKS_PER_SECOND + 1;
            return Result.fail(type.getName() + " is recharging; ready in " + seconds + "s.");
        }
        int cost = session.effectiveCost(type);
        if (session.getSunManager().getSunBalance() < cost) {
            return Result.fail("Not enough sun: " + type.getName() + " costs " + cost
                    + " and you have " + session.getSunManager().getSunBalance() + ".");
        }
        return plantOnTile(type, slot, cost, x, y);
    }

    private Result plantOnTile(PlantType type, PlantSlot slot, int cost, int x, int y) {
        Tile tile = session.getGrid().getTile(x - 1, y - 1);
        Result terrainError = checkTerrain(type, tile, x, y);
        if (terrainError != null) {
            return terrainError;
        }
        if (type == PlantType.GRAVE_BUSTER) {
            tile.setTerrain(TerrainType.NORMAL);
            consume(slot, cost);
            return Result.ok("The grave at (" + x + ", " + y + ") was busted.");
        }
        if (type == PlantType.HOT_POTATO) {
            tile.setTerrain(TerrainType.NORMAL);
            consume(slot, cost);
            return Result.ok("The ice at (" + x + ", " + y + ") melted away.");
        }
        if (type == PlantType.LILY_PAD) {
            tile.setHasLilyPad(true);
            consume(slot, cost);
            return Result.ok("Lily Pad placed on the water at (" + x + ", " + y + ").");
        }
        if (type == PlantType.GOLD_BLOOM) {
            session.getSunManager().addSun(375);
            consume(slot, cost);
            return Result.ok("Gold Bloom burst into 375 sun!");
        }
        if (type.getName().toLowerCase().contains("mint")) {
            return releaseMint(type, slot, cost);
        }
        Result occupied = handleStacking(type, slot, cost, x, y);
        if (occupied != null) {
            return occupied;
        }
        if (type.getBaseHp() == 0 && type.getCategory() == PlantCategory.EXPLOSIVE) {
            return detonateInstantly(type, slot, cost, x, y);
        }
        PlacedPlant plant = new PlacedPlant(type, x, y, session.effectiveHp(type));
        if (type == PlantType.POTATO_MINE) {
            plant.setArmTicks(15 * GameSession.TICKS_PER_SECOND);
        }
        if (type == PlantType.PRIMAL_POTATO_MINE) {
            plant.setArmTicks(5 * GameSession.TICKS_PER_SECOND);
        }
        session.getPlants().add(plant);
        consume(slot, cost);
        if (slot.isBoosted()) {
            slot.setBoosted(false);
            session.getCombatManager().applyPlantFood(plant);
        }
        return Result.ok(type.getName() + " planted at (" + x + ", " + y + ").");
    }

    private Result checkTerrain(PlantType type, Tile tile, int x, int y) {
        TerrainType terrain = tile.getTerrain();
        if (terrain == TerrainType.GRAVE && type != PlantType.GRAVE_BUSTER) {
            return Result.fail("You cannot plant on a gravestone.");
        }
        if (terrain != TerrainType.GRAVE && type == PlantType.GRAVE_BUSTER) {
            return Result.fail("Grave Buster can only be used on gravestones.");
        }
        if (terrain == TerrainType.SLIDER_UP || terrain == TerrainType.SLIDER_DOWN) {
            return Result.fail("Slider tiles cannot hold plants.");
        }
        if ((terrain == TerrainType.FROZEN || terrain == TerrainType.ICE)
                && type != PlantType.HOT_POTATO) {
            return Result.fail("This tile is frozen; melt it with a Hot Potato first.");
        }
        if (terrain != TerrainType.FROZEN && terrain != TerrainType.ICE
                && type == PlantType.HOT_POTATO) {
            return Result.fail("Hot Potato can only be used on frozen tiles.");
        }
        if (terrain == TerrainType.WATER && type != PlantType.LILY_PAD
                && !type.getTags().contains(PlantTag.WATER) && !tile.isHasLilyPad()) {
            return Result.fail("Place a Lily Pad on the water first.");
        }
        if (terrain != TerrainType.WATER && type == PlantType.LILY_PAD) {
            return Result.fail("Lily Pads can only be placed on water.");
        }
        if (terrain != TerrainType.WATER && type == PlantType.TANGLE_KELP) {
            return Result.fail("Tangle Kelp can only be planted in water.");
        }
        return null;
    }

    private Result handleStacking(PlantType type, PlantSlot slot, int cost, int x, int y) {
        PlacedPlant existing = session.plantAt(x, y);
        if (existing == null) {
            return null;
        }
        if (type == PlantType.PEA_POD && existing.getType() == PlantType.PEA_POD) {
            if (existing.getStackCount() >= 5) {
                return Result.fail("This Pea Pod already has 5 heads.");
            }
            existing.setStackCount(existing.getStackCount() + 1);
            consume(slot, cost);
            return Result.ok("Pea Pod at (" + x + ", " + y + ") now has "
                    + existing.getStackCount() + " heads.");
        }
        if (type == PlantType.PUMPKIN && existing.getType() != PlantType.PUMPKIN) {
            if (existing.getPumpkinHealth() > 0) {
                return Result.fail("This plant is already protected by a Pumpkin.");
            }
            existing.setPumpkinHealth(session.effectiveHp(PlantType.PUMPKIN));
            consume(slot, cost);
            return Result.ok(existing.getType().getName() + " at (" + x + ", " + y
                    + ") is now protected by a Pumpkin.");
        }
        return Result.fail("There is already a plant at (" + x + ", " + y + ").");
    }

    private Result releaseMint(PlantType type, PlantSlot slot, int cost) {
        int affected = 0;
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            if (plant.getType().getCategory() == type.getCategory()) {
                session.getCombatManager().applyPlantFood(plant);
                affected++;
            }
        }
        consume(slot, cost);
        return Result.ok(type.getName() + " empowered " + affected
                + " plants of its family and vanished.");
    }

    private Result detonateInstantly(PlantType type, PlantSlot slot, int cost, int x, int y) {
        consume(slot, cost);
        if (type == PlantType.ICE_SHROOM) {
            for (Zombie zombie : session.getZombies()) {
                zombie.setFrozenTicks(5 * GameSession.TICKS_PER_SECOND);
            }
            return Result.ok("Ice-shroom froze every zombie on the map.");
        }
        if (type == PlantType.DOOM_SHROOM) {
            for (Zombie zombie : new ArrayList<>(session.getZombies())) {
                session.getCombatManager().damageZombie(zombie, session.effectiveDamage(type) + 800);
            }
            return Result.ok("Doom-shroom devastated the whole garden!");
        }
        PlacedPlant bomb = new PlacedPlant(type, x, y, 1);
        session.getPlants().add(bomb);
        session.getCombatManager().explode(bomb);
        return Result.ok(type.getName() + " exploded at (" + x + ", " + y + ").");
    }

    private void consume(PlantSlot slot, int cost) {
        session.getSunManager().spendSun(cost);
        if (!session.isCooldownsDisabled()) {
            slot.setCooldownTicks(slot.getType().getRecharge() * GameSession.TICKS_PER_SECOND);
        }
        if (slot.isSingleUse()) {
            session.getSlots().remove(slot);
        }
    }

    public Result pluck(int x, int y) {
        if (session.getPhase() != GamePhase.BATTLE) {
            return Result.fail("There is no running battle.");
        }
        PlacedPlant plant = session.plantAt(x, y);
        if (plant == null) {
            Tile tile = session.getGrid().getTile(x - 1, y - 1);
            if (tile != null && tile.isHasLilyPad()) {
                tile.setHasLilyPad(false);
                return Result.ok("Lily Pad removed from (" + x + ", " + y + ").");
            }
            return Result.fail("There is no plant at (" + x + ", " + y + ").");
        }
        session.removePlant(plant, false);
        return Result.ok(plant.getType().getName() + " plucked from (" + x + ", " + y + ").");
    }
}
