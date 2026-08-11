package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import controllers.menuControllers.LoginController;
import models.Result;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Ui;

public class LoginScreen extends BaseScreen {

    private final LoginController controller;
    private final Table content = new Table();

    public LoginScreen(PvzGame game) {
        super(game);
        this.controller = new LoginController(game.getApp());
    }

    @Override
    protected String title() {
        return "Welcome Back";
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.SIGNUP;
    }

    @Override
    protected void buildContent(Table body) {
        body.add(content).grow();
        showLoginForm();
    }

    private void showLoginForm() {
        content.clear();
        Table form = Ui.panel(skin);

        final TextField username = Ui.field(skin, "username");
        final TextField password = Ui.password(skin, "password");
        final CheckBox stayLoggedIn = new CheckBox("  Stay logged in", skin);

        form.add(Ui.label(skin, "Username", "h2")).right().padRight(18f).padBottom(12f);
        form.add(username).width(420f).height(46f).left().padBottom(12f).row();
        form.add(Ui.label(skin, "Password", "h2")).right().padRight(18f).padBottom(12f);
        form.add(password).width(420f).height(46f).left().padBottom(12f).row();
        form.add();
        form.add(stayLoggedIn).left().padBottom(14f).row();

        Table actions = new Table();
        actions.add(Ui.button(skin, "Log in", () -> {
            Result result = controller.handleLogin(username.getText(), password.getText(),
                    stayLoggedIn.isChecked() ? "-stay-logged-in" : null);
            toasts.show(result);
            if (result.isSuccessfull()) {
                router.go(ScreenId.MAIN);
            }
        })).height(58f).width(200f).padRight(12f);
        actions.add(Ui.button(skin, "Forgot password", "blue", this::showRecoveryStart))
                .height(58f).width(250f).padRight(12f);
        actions.add(Ui.button(skin, "Sign up", "brown", () -> router.go(ScreenId.SIGNUP)))
                .height(58f).width(170f);
        form.add(actions).colspan(2).padTop(10f);

        content.add(form).width(760f).center();
    }

    private void showRecoveryStart() {
        content.clear();
        Table form = Ui.panel(skin);
        form.add(Ui.label(skin, "Password recovery", "h1")).colspan(2).padBottom(16f).row();

        final TextField username = Ui.field(skin, "username");
        final TextField email = Ui.field(skin, "registered email");

        form.add(Ui.label(skin, "Username", "h2")).right().padRight(18f).padBottom(12f);
        form.add(username).width(420f).height(46f).left().padBottom(12f).row();
        form.add(Ui.label(skin, "Email", "h2")).right().padRight(18f).padBottom(12f);
        form.add(email).width(420f).height(46f).left().padBottom(12f).row();

        Table actions = new Table();
        actions.add(Ui.button(skin, "Continue", () -> {
            Result result = controller.handleForgotPassword(username.getText(), email.getText());
            if (!result.isSuccessfull()) {
                toasts.show(result);
                return;
            }
            showRecoveryAnswer(result.getMessages().isEmpty() ? "" : result.getMessages().get(0));
        })).height(58f).width(200f).padRight(12f);
        actions.add(Ui.button(skin, "Cancel", "brown", () -> {
            controller.handleResetPasswordQuit();
            showLoginForm();
        })).height(58f).width(170f);
        form.add(actions).colspan(2).padTop(10f);

        content.add(form).width(760f).center();
    }

    private void showRecoveryAnswer(String question) {
        content.clear();
        Table form = Ui.panel(skin);
        form.add(Ui.label(skin, "Security question", "h1")).colspan(2).padBottom(10f).row();
        form.add(Ui.wrapped(skin, question, "h2")).colspan(2).width(600f).padBottom(18f).row();

        final TextField answer = Ui.field(skin, "your answer");
        form.add(Ui.label(skin, "Answer", "h2")).right().padRight(18f).padBottom(12f);
        form.add(answer).width(420f).height(46f).left().padBottom(12f).row();

        Table actions = new Table();
        actions.add(Ui.button(skin, "Verify", () -> {
            Result result = controller.handleSecurityAnswer(answer.getText());
            toasts.show(result);
            if (result.isSuccessfull()) {
                showNewPassword();
            } else {
                showLoginForm();
            }
        })).height(58f).width(200f).padRight(12f);
        actions.add(Ui.button(skin, "Cancel", "brown", () -> {
            controller.handleResetPasswordQuit();
            showLoginForm();
        })).height(58f).width(170f);
        form.add(actions).colspan(2).padTop(10f);

        content.add(form).width(760f).center();
    }

    private void showNewPassword() {
        content.clear();
        Table form = Ui.panel(skin);
        form.add(Ui.label(skin, "Set a new password", "h1")).colspan(2).padBottom(16f).row();

        final TextField password = Ui.password(skin, "new password");
        final TextField confirm = Ui.password(skin, "repeat new password");

        form.add(Ui.label(skin, "Password", "h2")).right().padRight(18f).padBottom(12f);
        form.add(password).width(420f).height(46f).left().padBottom(12f).row();
        form.add(Ui.label(skin, "Confirm", "h2")).right().padRight(18f).padBottom(12f);
        form.add(confirm).width(420f).height(46f).left().padBottom(12f).row();

        Table actions = new Table();
        actions.add(Ui.button(skin, "Save", () -> {
            Result result = controller.handleNewPassword(password.getText(), confirm.getText());
            toasts.show(result);
            if (result.isSuccessfull()) {
                showLoginForm();
            }
        })).height(58f).width(200f).padRight(12f);
        actions.add(Ui.button(skin, "Cancel", "brown", () -> {
            controller.handleResetPasswordQuit();
            showLoginForm();
        })).height(58f).width(170f);
        form.add(actions).colspan(2).padTop(10f);

        content.add(form).width(760f).center();
    }
}
