package views.multiplayer;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import net.Reactions;
import views.ui.Ui;

import java.util.function.BiConsumer;

public final class ReactionBar extends Table {

    private static final float SLOT = 46f;

    public ReactionBar(Skin skin, BiConsumer<String, Integer> send) {
        super(skin);
        setBackground(skin.getDrawable("panel"));
        pad(5f, 8f, 5f, 8f);
        add(Ui.label(skin, "Say", "muted")).padRight(6f);
        for (int i = 0; i < Reactions.count(); i++) {
            final int index = i;
            add(Ui.button(skin, String.valueOf(i + 1), "small",
                    () -> send.accept(Reactions.TEXT, index)))
                    .size(SLOT, 34f).padRight(3f);
        }
        add(Ui.label(skin, "React", "muted")).padLeft(10f).padRight(6f);
        String[] faces = Reactions.faces();
        for (int i = 0; i < faces.length; i++) {
            final int index = i;
            add(Ui.button(skin, faces[i], "small",
                    () -> send.accept(Reactions.EMOJI, index)))
                    .size(SLOT, 34f).padRight(3f);
        }
        add(Ui.label(skin, "Sticker", "muted")).padLeft(10f).padRight(6f);
        for (int i = 0; i < Reactions.stickers().length; i++) {
            final int index = i;
            add(Ui.button(skin, String.valueOf(i + 1), "small-purple",
                    () -> send.accept(Reactions.STICKER, index)))
                    .size(SLOT, 34f).padRight(3f);
        }
    }

    public static String hint(String kind, int index) {
        return Reactions.describe(kind, index);
    }
}
