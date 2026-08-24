package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import models.Result;
import utils.DeviceSettings;
import net.Online;
import net.Protocol;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Ui;

public class ConnectScreen extends BaseScreen {

    private final Table content = new Table();

    public ConnectScreen(PvzGame game) {
        super(game);
    }

    @Override
    protected String title() {
        return "Connect";
    }

    @Override
    protected boolean showsTopBar() {
        return false;
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.CONNECT;
    }

    @Override
    protected void buildContent(Table body) {
        body.add(content).grow();
        showForm("");
    }

    private void showForm(String problem) {
        content.clear();
        Table form = Ui.panel(skin);
        form.add(Ui.label(skin, "Where is the server?", "h1")).colspan(2).padBottom(10f).row();
        form.add(Ui.wrapped(skin, "Your account, your progress and every duel live on the"
                + " server. Start one with  ./gradlew :server:run  and point this at it.",
                "muted")).colspan(2).width(620f).padBottom(18f).row();

        final TextField host = Ui.field(skin, "host");
        host.setText(DeviceSettings.serverHost());
        final TextField port = Ui.field(skin, "port");
        port.setText(String.valueOf(DeviceSettings.serverPort()));

        form.add(Ui.label(skin, "Host", "h2")).right().padRight(18f).padBottom(12f);
        form.add(host).width(380f).height(46f).left().padBottom(12f).row();
        form.add(Ui.label(skin, "Port", "h2")).right().padRight(18f).padBottom(12f);
        form.add(port).width(380f).height(46f).left().padBottom(12f).row();

        if (!problem.isEmpty()) {
            form.add(Ui.wrapped(skin, problem, "bad")).colspan(2).width(620f).padBottom(12f).row();
        }

        Table actions = new Table();
        actions.add(Ui.button(skin, "Connect", () -> attempt(host.getText(), port.getText())))
                .height(58f).width(220f).padRight(12f);
        actions.add(Ui.button(skin, "Use 127.0.0.1", "blue",
                () -> attempt(Protocol.DEFAULT_HOST, String.valueOf(Protocol.DEFAULT_PORT))))
                .height(58f).width(240f);
        form.add(actions).colspan(2).padTop(6f);
        content.add(form).width(760f).center();
    }

    private void attempt(String host, String portText) {
        int port;
        try {
            port = Integer.parseInt(portText.trim());
        } catch (NumberFormatException e) {
            showForm("The port must be a number.");
            return;
        }
        Result result = Online.get().connect(host, port);
        if (!result.isSuccessfull()) {
            showForm(result.getMessages().isEmpty() ? "Could not connect."
                    : result.getMessages().get(0));
            return;
        }
        DeviceSettings.setServer(host.trim(), port);
        toasts.show(result);
        router.go(ScreenId.SIGNUP);
    }
}
