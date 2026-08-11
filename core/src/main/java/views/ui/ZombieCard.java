package views.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import models.entities.zombie.ZombieType;
import views.assets.Art;

public final class ZombieCard extends Table {

    public static final float CARD_WIDTH = 138f;
    public static final float CARD_HEIGHT = 168f;

    private final ZombieType type;

    public ZombieCard(Skin skin, Art art, ZombieType type, boolean discovered) {
        this.type = type;
        setBackground(skin.getDrawable("card"));
        pad(10f);

        if (discovered) {
            Image portrait = new Image(new TextureRegionDrawable(art.zombie(type)));
            portrait.setScaling(Scaling.fit);
            Container<Image> cell = new Container<>(portrait);
            cell.size(96f, 92f);
            add(cell).row();
            Label name = new Label(type.getName(), skin, "small");
            name.setWrap(true);
            name.setAlignment(Align.center);
            add(name).width(CARD_WIDTH - 30f).padTop(6f);
        } else {
            Table empty = new Table(skin);
            empty.setBackground(skin.getDrawable("slot"));
            empty.add(new Label("?", skin, "title"));
            add(empty).size(96f, 92f).row();
            Label unknown = new Label("Not discovered", skin, "muted");
            unknown.setAlignment(Align.center);
            add(unknown).width(CARD_WIDTH - 30f).padTop(6f);
        }
    }

    public ZombieType getType() {
        return type;
    }

    public ZombieCard onClick(Runnable action) {
        Ui.onClick(this, action);
        return this;
    }
}
