package views.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import models.entities.plant.PlantType;
import models.game.Names;
import views.assets.Art;

import java.util.List;
import java.util.function.Consumer;

public final class Dialogs {

    private Dialogs() {
    }

    public static void confirm(Stage stage, Skin skin, String heading, String message,
                               final Runnable onConfirm) {
        final Table scrim = scrim(skin, stage);
        Table panel = Ui.panel(skin);
        panel.pad(26f);
        panel.add(Ui.label(skin, heading, "h1")).padBottom(12f).row();
        panel.add(Ui.wrapped(skin, message, "h2")).width(520f).padBottom(20f).row();

        Table actions = new Table();
        actions.add(Ui.button(skin, "Confirm", () -> {
            scrim.remove();
            onConfirm.run();
        })).width(200f).height(56f).padRight(14f);
        actions.add(Ui.button(skin, "Cancel", "brown", scrim::remove)).width(180f).height(56f);
        panel.add(actions);

        scrim.add(panel).width(620f);
    }

    public static void choosePlant(Stage stage, Skin skin, Art art, List<String> plantNames,
                                   final Consumer<String> onPick) {
        final Table scrim = scrim(skin, stage);
        Table panel = Ui.panel(skin);
        panel.pad(24f);
        panel.add(Ui.label(skin, "Choose a plant", "h1")).padBottom(14f).row();

        Table grid = new Table();
        int column = 0;
        for (final String name : plantNames) {
            PlantType type = Names.plant(name);
            if (type == null) {
                continue;
            }
            PlantCard card = new PlantCard(skin, art, type);
            card.onClick(() -> {
                scrim.remove();
                onPick.accept(name);
            });
            grid.add(card).size(PlantCard.CARD_WIDTH, PlantCard.CARD_HEIGHT).pad(6f);
            if (++column % 5 == 0) {
                grid.row();
            }
        }
        panel.add(Ui.scroll(skin, grid)).width(820f).height(420f).row();
        panel.add(Ui.button(skin, "Cancel", "brown", scrim::remove))
                .width(180f).height(52f).padTop(14f);

        scrim.add(panel);
    }

    private static Table scrim(Skin skin, Stage stage) {
        Table scrim = new Table();
        scrim.setFillParent(true);
        scrim.setBackground(skin.getDrawable("scrim"));
        scrim.setTouchable(Touchable.enabled);
        stage.addActor(scrim);
        scrim.toFront();
        return scrim;
    }
}
