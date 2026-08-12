package views.battle;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.math.Interpolation;
import views.ui.Ui;

public final class Overlay extends Table {

    private Overlay(Skin skin) {
        super(skin);
        setFillParent(true);
        setBackground(skin.getDrawable("scrim"));
        setTouchable(Touchable.enabled);
    }

    public static Overlay open(Stage stage, Skin skin, String title, Table content) {
        Overlay overlay = new Overlay(skin);
        Table panel = Ui.panel(skin);
        panel.pad(26f, 34f, 26f, 34f);
        panel.add(Ui.label(skin, title, "title")).padBottom(4f).row();
        panel.add(Ui.divider(skin, 340f)).padBottom(16f).row();
        panel.add(content).growX();
        overlay.add(panel).center();

        overlay.getColor().a = 0f;
        overlay.addAction(Actions.fadeIn(0.2f, Interpolation.pow2Out));
        panel.setOrigin(com.badlogic.gdx.utils.Align.center);
        panel.setTransform(true);
        panel.setScale(0.92f);
        panel.addAction(Actions.scaleTo(1f, 1f, 0.22f, Interpolation.swingOut));

        stage.addActor(overlay);
        return overlay;
    }

    public void close() {
        addAction(Actions.sequence(Actions.fadeOut(0.16f), Actions.removeActor()));
    }
}
