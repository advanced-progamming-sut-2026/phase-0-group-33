package views.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import models.Result;

import java.util.List;

public final class Toasts {

    private static final int MAX_LINES = 4;
    private static final int MAX_ON_SCREEN = 3;
    private static final float LIFETIME = 2.6f;

    private final Skin skin;
    private final Table container = new Table();
    private final java.util.Map<String, Table> showing = new java.util.LinkedHashMap<>();

    public Toasts(Skin skin, Stage stage) {
        this.skin = skin;
        container.setFillParent(true);
        container.top().padTop(96f);
        container.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        stage.addActor(container);
    }

    public void show(Result result) {
        if (result == null) {
            return;
        }
        List<String> messages = result.getMessages();
        if (messages.isEmpty()) {
            push(result.isSuccessfull() ? "Done." : "Something went wrong.", result.isSuccessfull());
            return;
        }
        int shown = 0;
        for (String message : messages) {
            if (shown++ >= MAX_LINES) {
                break;
            }
            push(message, result.isSuccessfull());
        }
    }

    public void success(String message) {
        push(message, true);
    }

    public void error(String message) {
        push(message, false);
    }

    private void push(String message, boolean success) {
        Table repeat = showing.get(message);
        if (repeat != null && repeat.hasParent()) {
            repeat.clearActions();
            repeat.getColor().a = 1f;
            repeat.addAction(fade(message));
            return;
        }
        while (showing.size() >= MAX_ON_SCREEN) {
            String oldest = showing.keySet().iterator().next();
            Table stale = showing.remove(oldest);
            if (stale != null) {
                stale.clearActions();
                stale.remove();
            }
        }
        Table bubble = new Table(skin);
        bubble.setBackground(skin.getDrawable("panel"));
        bubble.pad(10f, 22f, 10f, 22f);
        Label label = new Label(message, skin, "small");
        label.setColor(success ? Palette.GOOD : Palette.BAD);
        label.setWrap(true);
        bubble.add(label).width(560f);
        bubble.getColor().a = 0f;
        bubble.addAction(Actions.sequence(Actions.fadeIn(0.18f), fade(message)));
        container.add(bubble).padBottom(6f).row();
        container.toFront();
        showing.put(message, bubble);
    }

    private com.badlogic.gdx.scenes.scene2d.Action fade(final String message) {
        return Actions.sequence(
                Actions.delay(LIFETIME),
                Actions.fadeOut(0.4f),
                Actions.run(() -> showing.remove(message)),
                Actions.removeActor());
    }
}
