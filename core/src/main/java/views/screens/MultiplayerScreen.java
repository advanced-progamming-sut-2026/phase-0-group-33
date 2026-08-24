package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import models.Result;
import net.Online;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Ui;

import java.util.List;

public class MultiplayerScreen extends BaseScreen {

    private final Table people = new Table();

    private TextField target;
    private boolean queued;

    public MultiplayerScreen(PvzGame game) {
        super(game);
    }

    @Override
    protected String title() {
        return "I, Zombie - Two Players";
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.QUESTS;
    }

    @Override
    protected void buildContent(Table body) {
        body.add(Ui.wrapped(skin, "One of you grows the garden, the other raises the horde."
                + " The zombie side wins by eating all five brains; the plant side wins by"
                + " holding out for two minutes.", "muted")).width(900f).padBottom(14f).row();
        Table columns = new Table();
        columns.add(challengeCard()).width(440f).top().padRight(14f);
        columns.add(randomCard()).width(440f).top();
        body.add(columns).padBottom(12f).row();
        body.add(Ui.button(skin, "Couch play on this device", "blue",
                () -> router.go(ScreenId.COUCH))).width(420f).height(56f);
        refreshPeople();
    }

    private Table challengeCard() {
        Table card = Ui.card(skin, "card");
        card.pad(16f);
        card.add(Ui.label(skin, "Challenge a player", "h1")).left().padBottom(8f).row();
        target = Ui.field(skin, "their username");
        card.add(target).growX().height(46f).padBottom(8f).row();
        card.add(Ui.button(skin, "Send the invite", "green", this::invite))
                .growX().height(52f).padBottom(10f).row();
        card.add(Ui.label(skin, "Online right now", "muted")).left().padBottom(4f).row();
        card.add(Ui.scroll(skin, people)).growX().height(190f).row();
        card.add(Ui.button(skin, "Refresh the list", "small", this::refreshPeople))
                .growX().height(38f).padTop(6f);
        return card;
    }

    private Table randomCard() {
        Table card = Ui.card(skin, "card");
        card.pad(16f);
        card.add(Ui.label(skin, "Random opponent", "h1")).left().padBottom(8f).row();
        card.add(Ui.wrapped(skin, "Join the queue and the server pairs you with the next"
                + " player looking for a game. If somebody is already waiting you start"
                + " immediately.", "muted")).width(400f).padBottom(12f).row();
        card.add(Ui.button(skin, "Find me an opponent", "green", this::queue))
                .growX().height(52f).padBottom(8f).row();
        card.add(Ui.button(skin, "Leave the queue", "brown", this::unqueue))
                .growX().height(46f);
        return card;
    }

    private void invite() {
        if (!signedIn()) {
            return;
        }
        toasts.show(Online.get().invite(target.getText().trim()));
    }

    private void queue() {
        if (!signedIn()) {
            return;
        }
        Result result = Online.get().joinQueue();
        queued = result.isSuccessfull();
        toasts.show(result);
    }

    private void unqueue() {
        if (!queued) {
            toasts.show(Result.fail("You are not in the queue."));
            return;
        }
        queued = false;
        toasts.show(Online.get().leaveQueue());
    }

    private boolean signedIn() {
        if (Online.get().isSignedIn()) {
            return true;
        }
        toasts.show(Result.fail("You need to be signed in to the server for this."));
        return false;
    }

    private void refreshPeople() {
        people.clear();
        if (!Online.get().isSignedIn()) {
            people.add(Ui.label(skin, "You are offline.", "muted")).pad(16f);
            return;
        }
        List<String> names = Online.get().whoIsOnline();
        if (names.isEmpty()) {
            people.add(Ui.label(skin, "Nobody else is online.", "muted")).pad(16f);
            return;
        }
        for (final String name : names) {
            Table row = Ui.card(skin, "row");
            row.pad(5f, 12f, 5f, 12f);
            row.add(Ui.label(skin, name, "small")).growX().left();
            row.add(Ui.button(skin, "Invite", "small-brown", () -> {
                target.setText(name);
                invite();
            })).width(110f).height(34f);
            people.add(row).growX().padBottom(3f).row();
        }
    }

    @Override
    public void hide() {
        if (queued) {
            queued = false;
            Online.get().leaveQueue();
        }
    }
}
