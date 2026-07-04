package views;

import models.Result;
import models.entities.zombie.Zombie;
import models.game.GamePhase;
import models.game.GameSession;
import models.game.PlacedPlant;
import models.game.PlantSlot;
import models.map.TerrainType;
import models.map.Tile;

import java.util.Map;

public final class GameBoardPrinter {

    private GameBoardPrinter() {
    }

    /** Prints the map: header info first (doc: show map section), then the grid. */
    public static Result showMap(GameSession session) {
        Result result = Result.ok(String.format(
                "Wave: %d/%d | Sun: %d | Plant foods: %d",
                session.getWaveManager().getCurrentWave(), session.getWaveManager().getTotalWaves(),
                session.getSunManager().getSunBalance(), session.getPlantFoods()));
        for (int row = 1; row <= GameSession.ROWS; row++) {
            StringBuilder line = new StringBuilder();
            line.append(session.hasLawnMower(row) ? "[M]" : "[ ]").append(' ');
            for (int col = 1; col <= GameSession.COLS; col++) {
                line.append(renderTile(session, col, row)).append(' ');
            }
            result.addMessage(line.toString());
        }
        result.addMessage("Legend: [M] mower | letter = plant initial | digit = zombies | * sun | "
                + "~ water | # grave");
        return result;
    }

    private static String renderTile(GameSession session, int col, int row) {
        Tile tile = session.getGrid().getTile(col - 1, row - 1);
        char terrain = '.';
        if (tile.getTerrain() == TerrainType.WATER) {
            terrain = '~';
        } else if (tile.getTerrain() == TerrainType.GRAVE) {
            terrain = '#';
        }
        PlacedPlant plant = session.plantAt(col, row);
        char plantChar = plant == null ? terrain : plant.getType().getName().charAt(0);
        int zombieCount = 0;
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() == row
                    && (int) Math.round(zombie.getPosition().getX()) == col) {
                zombieCount++;
            }
        }
        char zombieChar = zombieCount == 0 ? ' ' : Character.forDigit(Math.min(9, zombieCount), 10);
        char sunChar = session.getSunManager().hasSunAt(col, row) ? '*' : ' ';
        return "" + plantChar + zombieChar + sunChar;
    }

    /** Doc: for each chosen plant, its sun cost and when it can be planted. */
    public static Result showPlantsStatus(GameSession session) {
        Result result = Result.ok("Your plants:");
        for (PlantSlot slot : session.getSlots()) {
            String status;
            if (!slot.isReady()) {
                int seconds = slot.getCooldownTicks() / GameSession.TICKS_PER_SECOND + 1;
                status = "recharging, ready in " + seconds + "s";
            } else if (slot.getType().getCost() > session.getSunManager().getSunBalance()) {
                status = "not enough sun";
            } else {
                status = "ready to plant";
            }
            result.addMessage(String.format("- %s | cost: %d | %s%s", slot.getType().getName(),
                    slot.getType().getCost(), status, slot.isBoosted() ? " | boosted" : ""));
        }
        return result;
    }

    /** Doc: plants and zombies on the given tile with their intrinsic stats. */
    public static Result showTileStatus(GameSession session, int x, int y) {
        if (x < 1 || x > GameSession.COLS || y < 1 || y > GameSession.ROWS) {
            return Result.fail("Coordinates are outside the lawn.");
        }
        Tile tile = session.getGrid().getTile(x - 1, y - 1);
        Result result = Result.ok("Tile (" + x + ", " + y + ") | terrain: " + tile.getTerrain()
                + (tile.isHasLilyPad() ? " (lily pad)" : ""));
        PlacedPlant plant = session.plantAt(x, y);
        if (plant != null) {
            result.addMessage(String.format("Plant: %s | HP: %d/%d",
                    plant.getType().getName(), plant.getHealth(), plant.getMaxHealth()));
        }
        for (Zombie zombie : session.getZombies()) {
            if ((int) zombie.getPosition().getY() == y
                    && (int) Math.round(zombie.getPosition().getX()) == x) {
                result.addMessage(String.format("Zombie: %s | HP: %d | armor: %d",
                        zombie.getType().getName(), zombie.getHealth(), zombie.totalArmor()));
            }
        }
        return result;
    }

    /** Prints every zombie in the doc's format. */
    public static Result zombiesInfo(GameSession session) {
        if (session.getPhase() != GamePhase.BATTLE && session.getZombies().isEmpty()) {
            return Result.ok("There are no zombies on the map.");
        }
        Result result = Result.ok();
        for (Zombie zombie : session.getZombies()) {
            result.addMessage(zombie.getType().getName() + ":");
            result.addMessage("    position: " + formatCoordinate(zombie.getPosition().getX())
                    + ", " + (int) zombie.getPosition().getY());
            result.addMessage("    health: " + zombie.getHealth());
            result.addMessage("    armor:");
            for (Map.Entry<String, Integer> armor : zombie.getArmorInfo().entrySet()) {
                result.addMessage("        " + armor.getKey() + ": " + armor.getValue());
            }
            result.addMessage("    effects:");
            if (zombie.getFrozenTicks() > 0) {
                result.addMessage("        frozen: " + formatSeconds(zombie.getFrozenTicks()));
            }
            if (zombie.getChilledTicks() > 0) {
                result.addMessage("        chilled: " + formatSeconds(zombie.getChilledTicks()));
            }
            result.addMessage("");
        }
        return result;
    }

    private static String formatCoordinate(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }
        return String.format("%.1f", value);
    }

    private static String formatSeconds(int ticks) {
        double seconds = ticks / (double) GameSession.TICKS_PER_SECOND;
        if (seconds == Math.floor(seconds)) {
            return (int) seconds + "s";
        }
        return String.format("%.1fs", seconds);
    }
}
