package views.multiplayer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import net.Reactions;
import views.assets.Art;
import views.ui.Ui;

import java.util.function.BiConsumer;

public final class ReactionBar extends Table {

    private static final float WIDE = 116f;
    private static final float NARROW = 56f;
    private static final float TALL = 36f;

    private final Skin skin;
    private final Art art;
    private final BiConsumer<String, Integer> send;

    public ReactionBar(Skin skin, Art art, BiConsumer<String, Integer> send) {
        super(skin);
        this.skin = skin;
        this.art = art;
        this.send = send;
        setBackground(skin.getDrawable("panel"));
        pad(4f, 10f, 4f, 10f);
        add(Ui.label(skin, "Say", "muted")).padRight(6f);
        addMessages();
        add(Ui.label(skin, "React", "muted")).padLeft(12f).padRight(6f);
        addFaces();
        add(Ui.label(skin, "Sticker", "muted")).padLeft(12f).padRight(6f);
        addStickers();
    }

    private void addMessages() {
        String[] lines = Reactions.messages();
        for (int i = 0; i < lines.length; i++) {
            final int index = i;
            add(Ui.button(skin, lines[i], "small",
                    () -> send.accept(Reactions.TEXT, index)))
                    .size(WIDE, TALL).padRight(3f);
        }
    }

    private void addFaces() {
        String[] faces = Reactions.faces();
        for (int i = 0; i < faces.length; i++) {
            final int index = i;
            add(chip(ReactionArt.face(art, i), faces[i], "row",
                    () -> send.accept(Reactions.EMOJI, index))).size(WIDE, TALL).padRight(3f);
        }
    }

    private void addStickers() {
        for (int i = 0; i < Reactions.stickers().length; i++) {
            final int index = i;
            add(chip(ReactionArt.sticker(art, i), "", "highlight",
                    () -> send.accept(Reactions.STICKER, index))).size(NARROW, TALL)
                    .padRight(3f);
        }
    }

    private Table chip(TextureRegion icon, String text, String style, Runnable action) {
        Table card = new Table(skin);
        card.setBackground(skin.getDrawable(style));
        card.pad(2f, 6f, 2f, 6f);
        if (icon != null) {
            card.add(Ui.iconCell(icon, 26f)).padRight(text.isEmpty() ? 0f : 5f);
        }
        if (!text.isEmpty()) {
            card.add(Ui.label(skin, text, "muted")).growX().left();
        }
        Ui.hoverLift(card, 1.05f);
        Ui.onClick(card, action);
        return card;
    }

    public static String hint(String kind, int index) {
        return Reactions.describe(kind, index);
    }
}
