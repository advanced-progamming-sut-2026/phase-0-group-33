package controllers.managers;

import models.entities.plant.PlantCategory;
import models.entities.plant.PlantType;
import models.game.GameSession;
import models.entities.zombie.ZombieType;
import models.game.Names;
import models.game.Vase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class VaseDeck {

    private static final int AFFORDABLE = 250;

    private static final PlantCategory[] ESSENTIAL = {
        PlantCategory.SHOOTER, PlantCategory.LOBBER,
        PlantCategory.EXPLOSIVE, PlantCategory.WALL_NUT,
    };

    private static final PlantCategory[] EXTRA = {
        PlantCategory.SHOOTER, PlantCategory.SHOOTER, PlantCategory.LOBBER,
        PlantCategory.EXPLOSIVE, PlantCategory.WALL_NUT,
        PlantCategory.MELEE, PlantCategory.SUN_PRODUCER,
    };

    private final GameSession session;
    private final List<PlantType> cards = new ArrayList<>();

    VaseDeck(GameSession session) {
        this.session = session;
    }

    void shuffle() {
        cards.clear();
        cards.addAll(draw(ESSENTIAL));
        cards.addAll(draw(EXTRA));
    }

    private List<PlantType> draw(PlantCategory[] shape) {
        List<PlantType> picked = new ArrayList<>();
        for (PlantCategory family : shape) {
            PlantType pick = randomOf(family);
            if (pick != null) {
                picked.add(pick);
            }
        }
        Collections.shuffle(picked, session.getRandom());
        return picked;
    }

    PlantType deal() {
        if (cards.isEmpty()) {
            shuffle();
        }
        return cards.isEmpty() ? PlantType.PEASHOOTER : cards.remove(0);
    }

    List<Vase> board() {
        shuffle();
        List<int[]> spots = new ArrayList<>();
        for (int col = 3; col <= GameSession.COLS; col++) {
            for (int row = 1; row <= GameSession.ROWS; row++) {
                spots.add(new int[] {col, row});
            }
        }
        Collections.shuffle(spots, session.getRandom());
        List<Vase> board = new ArrayList<>();
        for (int i = 0; i < spots.size(); i++) {
            board.add(create(i, spots.get(i)[0], spots.get(i)[1]));
        }
        return board;
    }

    private Vase create(int index, int x, int y) {
        if (index == 0) {
            return new Vase(x, y, Vase.VaseKind.GHOUL, ZombieType.GARGANTUAR, null);
        }
        if (index == 1) {
            return new Vase(x, y, Vase.VaseKind.PLANT, null, deal());
        }
        int roll = session.getRandom().nextInt(100);
        if (roll < 55) {
            ZombieType[] pool = {ZombieType.NORMAL, ZombieType.CONE_HEAD,
                ZombieType.BUCKET_HEAD};
            return new Vase(x, y, Vase.VaseKind.ORDINARY,
                    pool[session.getRandom().nextInt(pool.length)], null);
        }
        if (roll < 80) {
            return new Vase(x, y, Vase.VaseKind.ORDINARY, null, deal());
        }
        return new Vase(x, y, Vase.VaseKind.ORDINARY, null, null);
    }

    private PlantType randomOf(PlantCategory family) {
        List<PlantType> pool = new ArrayList<>();
        for (String name : session.getSelection().getUnlockedPlantNames()) {
            PlantType type = Names.plant(name);
            if (type != null && type.getCategory() == family && type.getCost() <= AFFORDABLE) {
                pool.add(type);
            }
        }
        return pool.isEmpty() ? null : pool.get(session.getRandom().nextInt(pool.size()));
    }
}
