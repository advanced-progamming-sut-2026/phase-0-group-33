package views.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import models.entities.plant.PlantType;
import views.assets.Art;

public final class PlantCard extends Table {

    public static final float CARD_WIDTH = 142f;
    public static final float CARD_HEIGHT = 198f;

    private final Skin skin;
    private final Art art;
    private final PlantType type;
    private final Table body = new Table();
    private final Image portrait;
    private final Label caption;
    private final Table badges = new Table();
    private final Table footer = new Table();
    private final Image lockIcon;
    private final Table highlight = new Table();

    public PlantCard(Skin skin, Art art, PlantType type) {
        this.skin = skin;
        this.art = art;
        this.type = type;

        portrait = new Image(new TextureRegionDrawable(art.plant(type)));
        portrait.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        caption = new Label(type.getName(), skin, "small");
        caption.setWrap(true);
        caption.setAlignment(com.badlogic.gdx.utils.Align.center);

        lockIcon = new Image(new TextureRegionDrawable(art.ui("image_ui_lock_small")));
        lockIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        lockIcon.setVisible(false);

        highlight.setBackground(skin.getDrawable("highlight"));
        highlight.setVisible(false);

        Container<Image> portraitCell = new Container<>(portrait);
        portraitCell.size(92f, 76f);

        Stack stack = new Stack();
        stack.add(highlight);
        stack.add(body);

        body.pad(8f);
        body.add(badges).growX().height(22f).row();
        body.add(portraitCell).padTop(2f).row();
        body.add(caption).width(CARD_WIDTH - 40f).height(38f).padTop(2f).row();
        body.add(footer).growX().padTop(4f).row();

        setBackground(skin.getDrawable("card"));
        add(stack).size(CARD_WIDTH - 16f, CARD_HEIGHT - 16f);
    }

    public PlantType getType() {
        return type;
    }

    public PlantCard level(int level) {
        Label label = new Label("Lv " + level, skin, "gold");
        badges.add(label).left();
        badges.add().expandX();
        return this;
    }

    public PlantCard cost(int cost) {
        Table box = new Table();
        box.add(Ui.iconCell(art.ui("image_ui_hud_ingame_sun"), 22f)).padRight(4f);
        box.add(new Label(String.valueOf(cost), skin, "small"));
        badges.add(box).right();
        return this;
    }

    public PlantCard packets(int have, int need) {
        ProgressBar bar = new ProgressBar(0f, Math.max(1, need), 1f, false, skin, "gold-horizontal");
        bar.setValue(Math.min(have, need));
        footer.add(bar).growX().height(14f).row();
        footer.add(new Label(have + " / " + need, skin, "muted")).padTop(2f);
        return this;
    }

    public PlantCard note(String text, com.badlogic.gdx.graphics.Color color) {
        Label label = new Label(text, skin, "small");
        label.setColor(color);
        footer.add(label).row();
        return this;
    }

    public PlantCard locked(boolean locked) {
        lockIcon.setVisible(locked);
        if (locked) {
            portrait.setColor(0.35f, 0.35f, 0.35f, 1f);
            badges.clearChildren();
            badges.add(new Image(new TextureRegionDrawable(art.ui("image_ui_lock_small")))).size(20f).left();
            badges.add().expandX();
        }
        return this;
    }

    public PlantCard boosted(boolean boosted) {
        if (boosted) {
            setBackground(skin.getDrawable("card-epic"));
        }
        return this;
    }

    public PlantCard selected(boolean selected) {
        highlight.setVisible(selected);
        return this;
    }

    public PlantCard onClick(Runnable action) {
        Ui.onClick(this, action);
        return this;
    }
}
