package views.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public final class GameAssets implements Disposable {

    public static final String UI = "skin/pvz2_skin.atlas";
    public static final String PLANTS = "ATLASES/UI_SEEDPACKETS_768_00.atlas";
    public static final String ZOMBIES = "ATLASES/UI_ZOMBIEPACKETS_768_00.atlas";
    public static final String MENU_BACKGROUND = "ATLASES/MAINMENU_BACKGROUND_768_00.atlas";
    public static final String GARDEN_BACKGROUND = "ATLASES/DELAYLOAD_BACKGROUND_ZEN_768_00.atlas";
    public static final String LOGO = "ATLASES/UI_MAINMENULOGO_768_00.atlas";
    public static final String TROPHIES = "ATLASES/GAMETROPHIES_768_00.atlas";
    public static final String STAT_ICONS = "ATLASES/UI_ALMANAC_STATICONS_768_00.atlas";
    public static final String MOWERS = "ATLASES/BEACHMOWERGROUP_768_00.atlas";
    public static final String GRAVES_DARK = "ATLASES/TOMBSTONE_DARK_BASE_768_00.atlas";
    public static final String GRAVES_EGYPT = "ATLASES/EGYPT_GRAVESTONE_768_00.atlas";

    private static final String FONT_BODY = "skin/AVENIRNEXTLTPRO-DEMICN.TTF";
    private static final String FONT_DISPLAY = "skin/HOUSE OF TERROR.TTF";

    private final ObjectMap<String, TextureAtlas> atlases = new ObjectMap<>();
    private final ObjectMap<String, BitmapFont> fonts = new ObjectMap<>();
    private Texture blank;
    private Texture fade;

    public TextureAtlas atlas(String path) {
        TextureAtlas cached = atlases.get(path);
        if (cached == null) {
            cached = new TextureAtlas(Gdx.files.internal(path));
            atlases.put(path, cached);
        }
        return cached;
    }

    public TextureRegion region(String atlasPath, String name) {
        return atlas(atlasPath).findRegion(name);
    }

    public TextureAtlas ui() {
        return atlas(UI);
    }

    public BitmapFont font(String key) {
        return fonts.get(key);
    }

    public void loadFonts() {
        generate("title", FONT_DISPLAY, 46, 3f);
        generate("h1", FONT_DISPLAY, 34, 2.5f);
        generate("h2", FONT_BODY, 25, 2f);
        generate("body", FONT_BODY, 19, 1.6f);
        generate("small", FONT_BODY, 15, 1.4f);
        generate("tiny", FONT_BODY, 12, 1.2f);
    }

    private void generate(String key, String path, int size, float border) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.borderWidth = border;
        parameter.borderColor = com.badlogic.gdx.graphics.Color.valueOf("1d2b13ff");
        parameter.shadowOffsetY = 1;
        parameter.shadowColor = com.badlogic.gdx.graphics.Color.valueOf("00000055");
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        fonts.put(key, font);
    }

    public Texture verticalFade() {
        if (fade == null) {
            int height = 64;
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
                    1, height, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            for (int y = 0; y < height; y++) {
                float t = 1f - y / (float) (height - 1);
                pixmap.setColor(0f, 0f, 0f, t * t * 0.85f);
                pixmap.drawPixel(0, y);
            }
            fade = new Texture(pixmap);
            fade.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
        }
        return fade;
    }

    public Texture blank() {
        if (blank == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap =
                    new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            pixmap.fill();
            blank = new Texture(pixmap);
            pixmap.dispose();
        }
        return blank;
    }

    @Override
    public void dispose() {
        for (TextureAtlas atlas : atlases.values()) {
            atlas.dispose();
        }
        atlases.clear();
        for (BitmapFont font : fonts.values()) {
            font.dispose();
        }
        fonts.clear();
        if (blank != null) {
            blank.dispose();
            blank = null;
        }
        if (fade != null) {
            fade.dispose();
            fade = null;
        }
    }
}
