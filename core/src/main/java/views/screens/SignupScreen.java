package views.screens;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import controllers.menuControllers.SignupController;
import models.Result;
import models.enums.SecurityQuestionData;
import views.PvzGame;
import views.ScreenId;
import views.ui.BaseScreen;
import views.ui.Ui;

public class SignupScreen extends BaseScreen {

    private final SignupController controller;
    private final Table content = new Table();

    private TextField username;
    private TextField password;
    private TextField confirm;
    private TextField nickname;
    private TextField email;
    private SelectBox<String> gender;

    public SignupScreen(PvzGame game) {
        super(game);
        this.controller = new SignupController(game.getApp());
    }

    @Override
    protected String title() {
        return "Create Account";
    }

    @Override
    protected ScreenId backTarget() {
        return ScreenId.LOGIN;
    }

    @Override
    protected void buildContent(Table body) {
        body.add(content).grow();
        showAccountForm();
    }

    private void showAccountForm() {
        content.clear();
        Table form = Ui.panel(skin);

        username = Ui.field(skin, "letters, digits and hyphens");
        password = Ui.password(skin, "8+ chars, upper, lower, digit, symbol");
        confirm = Ui.password(skin, "repeat password");
        nickname = Ui.field(skin, "3 to 30 characters");
        email = Ui.field(skin, "you@example.com");
        gender = new SelectBox<>(skin);
        gender.setItems("male", "female");

        formRow(form, "Username", username);
        formRow(form, "Password", password);
        formRow(form, "Confirm", confirm);
        formRow(form, "Nickname", nickname);
        formRow(form, "Email", email);
        formRow(form, "Gender", gender);

        Table actions = new Table();
        actions.add(Ui.button(skin, "Continue", this::submitAccount)).height(58f).width(220f).padRight(14f);
        actions.add(Ui.button(skin, "I have an account", "blue", () -> router.go(ScreenId.LOGIN)))
                .height(58f).width(280f);
        form.add(actions).colspan(2).padTop(18f);

        content.add(form).width(720f).center();
    }

    private void submitAccount() {
        Result result = controller.handleRegistry(username.getText(), password.getText(),
                confirm.getText(), nickname.getText(), email.getText(), gender.getSelected());
        if (!result.isSuccessfull()) {
            toasts.show(result);
            return;
        }
        toasts.success("Details accepted. Pick a security question.");
        showQuestionForm();
    }

    private void showQuestionForm() {
        content.clear();
        Table form = Ui.panel(skin);

        final SelectBox<String> question = new SelectBox<>(skin);
        String[] questions = new String[SecurityQuestionData.values().length];
        for (int i = 0; i < questions.length; i++) {
            questions[i] = SecurityQuestionData.values()[i].getQuestion();
        }
        question.setItems(questions);

        final TextField answer = Ui.field(skin, "your answer");
        final TextField answerConfirm = Ui.field(skin, "repeat your answer");

        formRow(form, "Question", question);
        formRow(form, "Answer", answer);
        formRow(form, "Confirm", answerConfirm);

        Table actions = new Table();
        actions.add(Ui.button(skin, "Create account", () -> {
            int index = question.getSelectedIndex() + 1;
            Result result = controller.handleQuestionSelection(String.valueOf(index),
                    answer.getText(), answerConfirm.getText());
            toasts.show(result);
            if (result.isSuccessfull()) {
                router.go(ScreenId.LOGIN);
            }
        })).height(58f).width(240f).padRight(14f);
        actions.add(Ui.button(skin, "Back", "brown", this::showAccountForm)).height(58f).width(160f);
        form.add(actions).colspan(2).padTop(18f);

        content.add(form).width(720f).center();
    }

    private void formRow(Table form, String label, com.badlogic.gdx.scenes.scene2d.Actor input) {
        form.add(Ui.label(skin, label, "h2")).right().padRight(18f).padBottom(10f);
        form.add(input).width(420f).height(46f).left().padBottom(10f).row();
    }
}
