package views.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public final class MenuTile extends Table {

    public static final float TILE_WIDTH = 300f;
    public static final float TILE_HEIGHT = 94f;

    private final Table badge = new Table();

    public MenuTile(Skin skin, TextureRegion icon, String text, String style, Runnable action) {
        setBackground(skin.getDrawable(style));
        pad(8f, 14f, 8f, 14f);

        add(Ui.iconCell(icon, 54f)).padRight(12f);

        Label label = new Label(text, skin, "h2");
        label.setAlignment(Align.left);
        add(label).growX();

        add(badge).right();

        Ui.hoverLift(this, 1.04f);
        Ui.onClick(this, action);
    }

    public MenuTile mark(Skin skin, String text) {
        Table bubble = new Table(skin);
        bubble.setBackground(skin.getDrawable("highlight"));
        bubble.pad(2f, 8f, 2f, 8f);
        Label label = new Label(text, skin, "gold");
        label.setAlignment(Align.center);
        bubble.add(label);
        badge.add(bubble).padLeft(8f);
        return this;
    }
}
