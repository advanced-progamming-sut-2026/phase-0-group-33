package views.multiplayer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import models.entities.plant.PlantType;
import models.entities.zombie.ZombieType;
import models.game.Names;
import net.Reactions;
import views.assets.Art;

public final class ReactionArt {

    private ReactionArt() {
    }

    public static TextureRegion face(Art art, int index) {
        switch (Math.max(0, Math.min(2, index))) {
            case 0:
                return art.ui("image_ui_hud_ingame_sun");
            case 1:
                return art.brain();
            default:
                return art.pea();
        }
    }

    public static TextureRegion sticker(Art art, int index) {
        String name = Reactions.stickers()[Math.max(0, Math.min(2, index))];
        PlantType plant = Names.plant(name);
        if (plant != null) {
            return art.plant(plant);
        }
        ZombieType zombie = Names.zombie(name);
        return zombie == null ? art.placeholder() : art.zombie(zombie);
    }
}
