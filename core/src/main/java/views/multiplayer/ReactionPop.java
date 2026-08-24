package views.multiplayer;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.game.Names;
import net.Reactions;
import views.assets.Animations;
import views.assets.Art;
import views.ui.AnimatedActor;
import views.ui.Ui;

public final class ReactionPop {

    private static final float LIFETIME = 3.4f;
    private static final float STICKER_SIZE = 118f;

    private final Stage stage;
    private final Skin skin;
    private final Art art;
    private final Animations animations;

    private Table current;

    public ReactionPop(Stage stage, Skin skin, Art art, Animations animations) {
        this.stage = stage;
        this.skin = skin;
        this.art = art;
        this.animations = animations;
    }

    public void show(String from, String kind, int index) {
        if (current != null) {
            current.remove();
        }
        Table bubble = new Table(skin);
        bubble.setBackground(skin.getDrawable("panel"));
        bubble.pad(10f, 14f, 10f, 14f);
        bubble.add(Ui.label(skin, from, "gold")).left().row();
        addBody(bubble, kind, index);
        bubble.pack();
        bubble.setPosition(stage.getWidth() - bubble.getWidth() - 18f,
                stage.getHeight() - bubble.getHeight() - 96f);
        bubble.getColor().a = 0f;
        bubble.addAction(Actions.sequence(
                Actions.fadeIn(0.18f, Interpolation.pow2Out),
                Actions.delay(LIFETIME),
                Actions.fadeOut(0.3f),
                Actions.removeActor()));
        stage.addActor(bubble);
        current = bubble;
    }

    private void addBody(Table bubble, String kind, int index) {
        if (Reactions.STICKER.equals(kind)) {
            addSticker(bubble, index);
            return;
        }
        if (Reactions.EMOJI.equals(kind)) {
            Table line = new Table();
            line.add(Ui.iconCell(ReactionArt.face(art, index), 46f)).padRight(8f);
            line.add(Ui.label(skin, Reactions.describe(kind, index), "h2"));
            bubble.add(line).left();
            return;
        }
        bubble.add(Ui.wrapped(skin, Reactions.describe(kind, index), "h2")).width(260f).left();
    }

    private void addSticker(Table bubble, int index) {
        String name = Reactions.stickers()[Math.max(0, Math.min(2, index))];
        AnimatedActor actor = animatedFor(name);
        if (actor == null) {
            bubble.add(Ui.iconCell(ReactionArt.sticker(art, index), STICKER_SIZE)).row();
        } else {
            Table window = new Table();
            window.setClip(true);
            window.add(actor).size(STICKER_SIZE);
            bubble.add(window).size(STICKER_SIZE).row();
        }
        bubble.add(Ui.label(skin, Reactions.describe(Reactions.STICKER, index), "small")).left();
    }

    private AnimatedActor animatedFor(String name) {
        PlantType plant = Names.plant(name);
        if (plant != null) {
            return AnimatedActor.plant(animations, plant, STICKER_SIZE);
        }
        ZombieType zombie = Names.zombie(name);
        return zombie == null ? null : AnimatedActor.zombie(animations, zombie, STICKER_SIZE);
    }

}
