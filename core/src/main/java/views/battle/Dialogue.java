package views.battle;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import views.assets.Animations;
import views.ui.AnimatedActor;
import views.ui.Ui;

import java.util.List;

public final class Dialogue extends Table {

    private static final String SPEAKER = "CRAZYDAVE";

    private final Skin skin;
    private final List<String> lines;
    private final Runnable onFinished;
    private final com.badlogic.gdx.scenes.scene2d.ui.Label text;

    private int index;

    private Dialogue(Skin skin, Animations animations, List<String> lines, Runnable onFinished) {
        super(skin);
        this.skin = skin;
        this.lines = lines;
        this.onFinished = onFinished;

        setFillParent(true);
        bottom();
        setTouchable(Touchable.enabled);

        Table bubble = new Table(skin);
        bubble.setBackground(skin.getDrawable("panel"));
        bubble.pad(14f, 18f, 14f, 18f);

        AnimatedActor portrait = AnimatedActor.whole(animations, SPEAKER, 128f,
                "anim_mediumtalk", "anim_smalltalk", "anim_idle");
        if (portrait != null) {
            Table window = new Table();
            window.setClip(true);
            window.add(portrait).size(128f);
            bubble.add(window).size(128f).padRight(14f);
        }

        text = Ui.wrapped(skin, lines.get(0), "h2");
        bubble.add(text).width(700f).growX();
        bubble.add(Ui.button(skin, "Next", "small", this::next)).width(120f).height(48f).padLeft(12f);

        add(bubble).growX().pad(0f, 60f, 24f, 60f);
        getColor().a = 0f;
        addAction(Actions.fadeIn(0.25f));
    }

    public static Dialogue open(Stage stage, Skin skin, Animations animations,
                                List<String> lines, Runnable onFinished) {
        if (lines == null || lines.isEmpty()) {
            onFinished.run();
            return null;
        }
        Dialogue dialogue = new Dialogue(skin, animations, lines, onFinished);
        stage.addActor(dialogue);
        return dialogue;
    }

    private void next() {
        index++;
        if (index >= lines.size()) {
            addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(onFinished),
                    Actions.removeActor()));
            return;
        }
        text.setText(lines.get(index));
    }
}
