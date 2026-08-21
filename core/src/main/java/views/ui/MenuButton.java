package views.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public final class MenuButton extends Table {

    public static final float SIZE = 104f;

    private final TextureRegion face;
    private final TextureRegion pressed;
    private TextureRegion inner;
    private final Label caption;
    private final Table badge = new Table();

    private float clock;
    private boolean hovered;
    private float lift;

    public MenuButton(Skin skin, TextureRegion face, TextureRegion pressed,
                      String text, Runnable action) {
        this.face = face;
        this.pressed = pressed;
        this.caption = new Label(text, skin, "h2");
        caption.setAlignment(Align.center);
        setTouchable(Touchable.enabled);
        add().size(SIZE, SIZE).row();
        add(caption).width(SIZE + 40f).padTop(2f).row();
        add(badge).padTop(1f);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (action != null) {
                    action.run();
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                              com.badlogic.gdx.scenes.scene2d.Actor from) {
                hovered = true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer,
                             com.badlogic.gdx.scenes.scene2d.Actor to) {
                hovered = false;
            }
        });
    }

    public MenuButton withIcon(TextureRegion icon) {
        this.inner = icon;
        return this;
    }

    public MenuButton mark(Skin skin, String text) {
        Table bubble = new Table(skin);
        bubble.setBackground(skin.getDrawable("highlight"));
        bubble.pad(1f, 8f, 1f, 8f);
        Label label = new Label(text, skin, "gold");
        bubble.add(label);
        badge.add(bubble);
        return this;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        clock += delta;
        float wanted = hovered ? 1f : 0f;
        lift += (wanted - lift) * Math.min(1f, delta * 12f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion region = isPressed() && pressed != null ? pressed : face;
        if (region != null) {
            float bob = (float) Math.sin(clock * 2.1f + getX() * 0.03f) * 2.4f;
            float grow = 1f + lift * 0.12f;
            float size = SIZE * grow;
            float x = getX() + (getWidth() - size) / 2f;
            float y = getY() + getHeight() - size - (SIZE - size) / 2f + bob + lift * 5f;
            batch.setColor(1f, 1f, 1f, parentAlpha);
            batch.draw(region, x, y, size, size);
            if (inner != null) {
                float small = size * 0.56f;
                batch.draw(inner, x + (size - small) / 2f, y + (size - small) / 2f, small, small);
            }
            batch.setColor(Color.WHITE);
        }
        caption.setColor(1f, 1f, 1f - lift * 0.25f, parentAlpha);
        super.draw(batch, parentAlpha);
    }

    private boolean isPressed() {
        for (com.badlogic.gdx.scenes.scene2d.EventListener listener : getListeners()) {
            if (listener instanceof ClickListener && ((ClickListener) listener).isPressed()) {
                return true;
            }
        }
        return false;
    }
}
