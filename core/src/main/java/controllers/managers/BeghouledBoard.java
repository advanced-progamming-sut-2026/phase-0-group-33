package controllers.managers;

import models.entities.plant.PlantType;
import models.game.GameSession;
import models.game.PlacedPlant;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

final class BeghouledBoard {

    private final GameSession session;
    private final List<PlantType> kinds;
    private final BooleanSupplier hasMatch;

    BeghouledBoard(GameSession session, List<PlantType> kinds, BooleanSupplier hasMatch) {
        this.session = session;
        this.kinds = kinds;
        this.hasMatch = hasMatch;
    }

    static void swapPositions(PlacedPlant a, PlacedPlant b) {
        int x = a.getX();
        int y = a.getY();
        a.setX(b.getX());
        a.setY(b.getY());
        b.setX(x);
        b.setY(y);
    }

    boolean anyMoveLeft() {
        for (int row = 1; row <= GameSession.ROWS; row++) {
            for (int col = 1; col <= GameSession.COLS; col++) {
                if (wouldMatch(col, row, col + 1, row) || wouldMatch(col, row, col, row + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean wouldMatch(int x1, int y1, int x2, int y2) {
        PlacedPlant first = session.plantAt(x1, y1);
        PlacedPlant second = session.plantAt(x2, y2);
        if (first == null || second == null) {
            return false;
        }
        swapPositions(first, second);
        boolean match = hasMatch.getAsBoolean();
        swapPositions(first, second);
        return match;
    }

    void reshuffle() {
        for (PlacedPlant plant : new ArrayList<>(session.getPlants())) {
            int x = plant.getX();
            int y = plant.getY();
            session.removePlant(plant, false);
            PlantType type = kinds.get(session.getRandom().nextInt(kinds.size()));
            session.getPlants().add(new PlacedPlant(type, x, y, type.getBaseHp()));
        }
    }
}
