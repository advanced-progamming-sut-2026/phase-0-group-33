package views.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import models.entities.plant.PlantType;
import views.assets.Art;

public final class SeedPacket extends Table {

    public static final float PACKET_WIDTH = 96f;
    public static final float PACKET_HEIGHT = 116f;

    private static final Color DIM = new Color(0f, 0f, 0f, 0.6f);

    private final Skin skin;
    private final PlantType type;
    private final Image portrait;
    private final Label costLabel;
    private final Drawable shade;

    private float cooldown;
    private boolean affordable = true;
    private boolean armed;

    public SeedPacket(Skin skin, Art art, PlantType type) {
        this.skin = skin;
        this.type = type;
        this.shade = skin.getDrawable("white");

        setBackground(skin.getDrawable("card"));
        pad(5f);

        portrait = new Image(new TextureRegionDrawable(art.plant(type)));
        portrait.setScaling(Scaling.fit);
        Container<Image> cell = new Container<>(portrait);
        cell.size(PACKET_WIDTH - 22f, 56f);

        Label name = new Label(type.getName(), skin, "tiny");
        name.setAlignment(Align.center);
        name.setWrap(true);

        Table price = new Table();
        price.add(Ui.iconCell(art.ui("image_ui_hud_ingame_sun"), 20f)).padRight(3f);
        costLabel = new Label(String.valueOf(type.getCost()), skin, "small");
        price.add(costLabel);

        add(cell).row();
        add(name).width(PACKET_WIDTH - 16f).height(26f).row();
        add(price).padTop(1f);
    }

    public PlantType getType() {
        return type;
    }

    public SeedPacket cost(int value) {
        costLabel.setText(String.valueOf(value));
        return this;
    }

    public SeedPacket boosted(boolean value) {
        setBackground(skin.getDrawable(value ? "card-epic" : "card"));
        return this;
    }

    public SeedPacket cooldown(float fraction) {
        this.cooldown = Math.max(0f, Math.min(1f, fraction));
        return this;
    }

    public SeedPacket affordable(boolean value) {
        this.affordable = value;
        portrait.setColor(value ? Color.WHITE : new Color(0.45f, 0.45f, 0.5f, 1f));
        return this;
    }

    public SeedPacket armed(boolean value) {
        if (armed != value) {
            armed = value;
            setBackground(skin.getDrawable(value ? "card-done" : "card"));
        }
        return this;
    }

    public boolean isReady() {
        return cooldown <= 0f && affordable;
    }

    public SeedPacket onClick(Runnable action) {
        Ui.onClick(this, action);
        return this;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (cooldown <= 0f) {
            return;
        }
        Color previous = batch.getColor().cpy();
        batch.setColor(DIM.r, DIM.g, DIM.b, DIM.a * parentAlpha * getColor().a);
        shade.draw(batch, getX(), getY() + getHeight() * (1f - cooldown),
                getWidth(), getHeight() * cooldown);
        batch.setColor(previous);
    }
}
