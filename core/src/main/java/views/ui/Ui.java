package views.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

public final class Ui {

    private static final Color RESTING = new Color(0.92f, 0.92f, 0.92f, 1f);

    private Ui() {
    }

    public static Label label(Skin skin, String text) {
        return new Label(text, skin);
    }

    public static Label label(Skin skin, String text, String style) {
        return new Label(text, skin, style);
    }

    public static Label wrapped(Skin skin, String text, String style) {
        Label label = new Label(text, skin, style);
        label.setWrap(true);
        return label;
    }

    private static Runnable clickSound;

    public static void setClickSound(Runnable action) {
        clickSound = action;
    }

    private static void click() {
        if (clickSound != null) {
            clickSound.run();
        }
    }

    public static TextButton button(Skin skin, String text, Runnable action) {
        return button(skin, text, "default", action);
    }

    public static TextButton button(Skin skin, String text, String style, final Runnable action) {
        TextButton button = new TextButton(text, skin, style);
        button.getLabelCell().pad(0f, 14f, 0f, 14f);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!((TextButton) actor).isDisabled()) {
                    click();
                    action.run();
                }
            }
        });
        hoverLift(button, 1.035f);
        return button;
    }

    public static void hoverLift(final Actor actor, final float scale) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) {
                    return;
                }
                actor.setOrigin(com.badlogic.gdx.utils.Align.center);
                actor.addAction(Actions.parallel(
                        Actions.scaleTo(scale, scale, 0.12f, Interpolation.pow2Out),
                        Actions.color(Color.WHITE, 0.12f)));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) {
                    return;
                }
                actor.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.14f, Interpolation.pow2Out),
                        Actions.color(RESTING, 0.14f)));
            }
        });
        actor.setColor(RESTING);
    }

    public static void appear(Actor actor, int index) {
        actor.getColor().a = 0f;
        actor.addAction(Actions.sequence(
                Actions.delay(Math.min(index, 14) * 0.035f),
                Actions.fadeIn(0.24f, Interpolation.pow2Out)));
    }

    public static Table pill(Skin skin, TextureRegion icon, String text, String style) {
        Table box = new Table(skin);
        box.setBackground(skin.getDrawable("slot"));
        box.pad(4f, 10f, 4f, 12f);
        if (icon != null) {
            box.add(iconCell(icon, 24f)).padRight(6f);
        }
        box.add(new Label(text, skin, style));
        return box;
    }

    public static Table divider(Skin skin, float width) {
        Table line = new Table(skin);
        line.setBackground(skin.getDrawable("slot"));
        Table holder = new Table();
        holder.add(line).width(width).height(2f);
        return holder;
    }

    public static ImageButton iconButton(Skin skin, String style, final Runnable action) {
        ImageButton button = new ImageButton(skin, style);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }

    public static TextField field(Skin skin, String placeholder) {
        TextField field = new TextField("", skin);
        field.setMessageText(placeholder);
        return field;
    }

    public static TextField password(Skin skin, String placeholder) {
        TextField field = field(skin, placeholder);
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    public static Image icon(TextureRegion region, float size) {
        Image image = new Image(new TextureRegionDrawable(region));
        image.setScaling(Scaling.fit);
        image.setSize(size, size);
        return image;
    }

    public static Container<Image> iconCell(TextureRegion region, float size) {
        Image image = new Image(region == null ? null : new TextureRegionDrawable(region));
        image.setScaling(Scaling.fit);
        Container<Image> container = new Container<>(image);
        container.size(size);
        return container;
    }

    public static Table panel(Skin skin) {
        Table table = new Table(skin);
        table.setBackground(skin.getDrawable("shade"));
        table.pad(18f);
        return table;
    }

    public static Table card(Skin skin, String style) {
        Table table = new Table(skin);
        table.setBackground(skin.getDrawable(style));
        table.pad(14f);
        return table;
    }

    public static ScrollPane scroll(Skin skin, Actor content) {
        if (content instanceof Table) {
            ((Table) content).top();
        }
        ScrollPane pane = new ScrollPane(content, skin);
        pane.setFadeScrollBars(false);
        pane.setScrollingDisabled(true, false);
        pane.setOverscroll(false, false);
        return pane;
    }

    public static Table tabs(Skin skin, String[] labels, int initial,
                             final java.util.function.IntConsumer onSelect) {
        Table bar = new Table();
        com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup<TextButton> group =
                new com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup<>();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            final TextButton button = new TextButton(labels[i], skin, "tab");
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (button.isChecked()) {
                        onSelect.accept(index);
                    }
                }
            });
            group.add(button);
            bar.add(button).width(210f).height(54f).padRight(8f);
        }
        group.setMaxCheckCount(1);
        group.setMinCheckCount(1);
        group.getButtons().get(initial).setChecked(true);
        return bar;
    }

    public static Table row(Actor... actors) {
        Table table = new Table();
        for (Actor actor : actors) {
            table.add(actor).padRight(8f);
        }
        return table;
    }

    public static void onClick(Actor actor, final Runnable action) {
        actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                click();
                action.run();
            }
        });
    }

    public static Label chip(Skin skin, String text, Color color) {
        Label label = new Label(text, skin, "small");
        label.setColor(color);
        return label;
    }
}
