package views.multiplayer;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import models.Result;
import net.Online;
import net.Packet;
import net.Protocol;
import views.Router;
import views.ScreenId;
import views.battle.Overlay;
import views.ui.Toasts;
import views.ui.Ui;

public final class LobbyWatch {

    private final Stage stage;
    private final Skin skin;
    private final Router router;
    private final Toasts toasts;

    private Overlay prompt;

    public LobbyWatch(Stage stage, Skin skin, Router router, Toasts toasts) {
        this.stage = stage;
        this.skin = skin;
        this.router = router;
        this.toasts = toasts;
    }

    public void pump() {
        Packet event = Online.get().nextLobbyEvent();
        while (event != null) {
            handle(event);
            event = Online.get().nextLobbyEvent();
        }
    }

    private void handle(Packet event) {
        switch (event.type()) {
            case Protocol.INVITE_OFFER:
                askAbout(event.str("from"));
                break;
            case Protocol.MATCH_START:
                shut();
                router.go(ScreenId.DUEL);
                break;
            case Protocol.INVITE_CANCELLED:
                toasts.show(Result.fail(event.str(Protocol.MESSAGE, "The invite was declined.")));
                break;
            case Protocol.LOGOUT:
                dropped();
                break;
            default:
                break;
        }
    }

    private void dropped() {
        toasts.show(Result.fail("The connection to the server was lost."));
        router.go(ScreenId.CONNECT);
    }

    private void askAbout(final String from) {
        shut();
        Table content = new Table();
        content.add(Ui.wrapped(skin, from + " wants to play I, Zombie against you."
                + " Accept and you both drop straight into the lawn.", "muted"))
                .width(430f).padBottom(16f).row();
        Table actions = new Table();
        actions.add(Ui.button(skin, "Accept", "green", () -> answer(true)))
                .width(200f).height(52f).padRight(10f);
        actions.add(Ui.button(skin, "Decline", "brown", () -> answer(false)))
                .width(200f).height(52f);
        content.add(actions);
        prompt = Overlay.open(stage, skin, "A challenger!", content);
    }

    private void answer(boolean accept) {
        shut();
        Result result = Online.get().answerInvite(accept);
        if (!result.isSuccessfull()) {
            toasts.show(result);
        }
    }

    private void shut() {
        if (prompt != null) {
            prompt.close();
            prompt = null;
        }
    }
}
