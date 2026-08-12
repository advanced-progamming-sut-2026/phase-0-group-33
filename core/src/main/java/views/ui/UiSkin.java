package views.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import views.assets.GameAssets;

public final class UiSkin {

    private UiSkin() {
    }

    public static Skin build(GameAssets assets) {
        Skin skin = new Skin();
        TextureAtlas atlas = assets.ui();
        skin.addRegions(atlas);

        skin.add("white", new TextureRegionDrawable(new TextureRegion(assets.blank())), Drawable.class);

        skin.add("title", assets.font("title"));
        skin.add("h1", assets.font("h1"));
        skin.add("h2", assets.font("h2"));
        skin.add("body", assets.font("body"));
        skin.add("small", assets.font("small"));
        skin.add("tiny", assets.font("tiny"));

        labels(skin, assets);
        buttons(skin, atlas, assets);
        inputs(skin, atlas, assets);
        bars(skin, atlas);
        windows(skin, atlas, assets);
        return skin;
    }

    private static void labels(Skin skin, GameAssets assets) {
        skin.add("default", new Label.LabelStyle(assets.font("body"), Palette.TEXT));
        skin.add("title", new Label.LabelStyle(assets.font("title"), Palette.GOLD));
        skin.add("h1", new Label.LabelStyle(assets.font("h1"), Palette.TEXT));
        skin.add("h2", new Label.LabelStyle(assets.font("h2"), Palette.TEXT));
        skin.add("small", new Label.LabelStyle(assets.font("small"), Palette.TEXT));
        skin.add("muted", new Label.LabelStyle(assets.font("small"), Palette.MUTED));
        skin.add("gold", new Label.LabelStyle(assets.font("body"), Palette.GOLD));
        skin.add("good", new Label.LabelStyle(assets.font("small"), Palette.GOOD));
        skin.add("bad", new Label.LabelStyle(assets.font("small"), Palette.BAD));
        skin.add("tiny", new Label.LabelStyle(assets.font("tiny"), Palette.TEXT));
    }

    private static void buttons(Skin skin, TextureAtlas atlas, GameAssets assets) {
        TextButton.TextButtonStyle green = textButton(atlas, assets,
                "image_ui_generic_greenbutton", "image_ui_generic_greenbutton_down");
        skin.add("default", green);
        skin.add("green", green);
        skin.add("brown", textButton(atlas, assets,
                "image_ui_generic_brownbutton", "image_ui_generic_brownbutton_down"));
        skin.add("blue", textButton(atlas, assets,
                "image_ui_generic_bluebutton", "image_ui_generic_bluebutton_down"));
        skin.add("purple", textButton(atlas, assets,
                "image_ui_generic_purplebutton", "image_ui_generic_purplebutton_down"));

        TextButton.TextButtonStyle small = textButton(atlas, assets,
                "image_ui_generic_greenbutton", "image_ui_generic_greenbutton_down");
        small.font = assets.font("small");
        skin.add("small", small);

        TextButton.TextButtonStyle smallBrown = textButton(atlas, assets,
                "image_ui_generic_brownbutton", "image_ui_generic_brownbutton_down");
        smallBrown.font = assets.font("small");
        skin.add("small-brown", smallBrown);

        TextButton.TextButtonStyle smallPurple = textButton(atlas, assets,
                "image_ui_generic_purplebutton", "image_ui_generic_purplebutton_down");
        smallPurple.font = assets.font("small");
        skin.add("small-purple", smallPurple);

        TextButton.TextButtonStyle tab = new TextButton.TextButtonStyle();
        tab.font = assets.font("h2");
        tab.fontColor = Palette.TEXT;
        tab.up = patch(atlas, "image_ui_generic_tantab_down", 40, 40, 24, 24);
        tab.checked = patch(atlas, "image_ui_generic_greentab_active", 40, 40, 24, 24);
        tab.down = patch(atlas, "image_ui_generic_greentab_down", 40, 40, 24, 24);
        skin.add("tab", tab);

        ImageButton.ImageButtonStyle close = new ImageButton.ImageButtonStyle();
        close.imageUp = new TextureRegionDrawable(atlas.findRegion("image_ui_generic_close_btn"));
        close.imageDown = new TextureRegionDrawable(atlas.findRegion("image_ui_generic_close_down"));
        skin.add("close", close);

        ImageButton.ImageButtonStyle back = new ImageButton.ImageButtonStyle();
        back.imageUp = new TextureRegionDrawable(atlas.findRegion("image_ui_almanac_buttons_hud_back_normal"));
        back.imageDown = new TextureRegionDrawable(atlas.findRegion("image_ui_almanac_buttons_hud_back_selected"));
        skin.add("back", back);

        CheckBox.CheckBoxStyle checkBox = new CheckBox.CheckBoxStyle();
        checkBox.font = assets.font("body");
        checkBox.fontColor = Palette.TEXT;
        checkBox.checkboxOff = sized(atlas, "image_ui_almanac_checkbox_disabled", 34f);
        checkBox.checkboxOn = sized(atlas, "image_ui_almanac_checkbox_enabled", 34f);
        skin.add("default", checkBox);
    }

    private static TextButton.TextButtonStyle textButton(TextureAtlas atlas, GameAssets assets,
                                                         String up, String down) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = assets.font("h2");
        style.fontColor = Palette.TEXT;
        style.downFontColor = Palette.GOLD;
        style.disabledFontColor = Palette.LOCKED;
        style.up = patch(atlas, up, 34, 34, 22, 26);
        style.down = patch(atlas, down, 34, 34, 22, 26);
        style.disabled = patch(atlas, "image_ui_generic_disabledbutton", 34, 34, 22, 26);
        return style;
    }

    private static void inputs(Skin skin, TextureAtlas atlas, GameAssets assets) {
        TextField.TextFieldStyle field = new TextField.TextFieldStyle();
        field.font = assets.font("body");
        field.fontColor = Palette.TEXT;
        field.messageFontColor = Palette.MUTED;
        field.background = patch(atlas, "image_ui_mainmenu_text_entry_field", 12, 12, 10, 10);
        field.cursor = skin.newDrawable("white", Palette.GOLD);
        field.selection = skin.newDrawable("white", Palette.SELECTED);
        skin.add("default", field);

        ScrollPane.ScrollPaneStyle scroll = new ScrollPane.ScrollPaneStyle();
        scroll.vScroll = patch(atlas, "image_ui_almanac_general_scrollbar_bkgd", 4, 4, 6, 6);
        scroll.vScrollKnob = patch(atlas, "image_ui_almanac_general_scrollbar", 4, 4, 8, 8);
        skin.add("default", scroll);

        List.ListStyle list = new List.ListStyle();
        list.font = assets.font("body");
        list.fontColorSelected = Palette.GOLD;
        list.fontColorUnselected = Palette.TEXT;
        list.selection = skin.newDrawable("white", Palette.SELECTED);
        list.background = skin.newDrawable("white", Palette.SCRIM);
        skin.add("default", list);

        SelectBox.SelectBoxStyle select = new SelectBox.SelectBoxStyle();
        select.font = assets.font("body");
        select.fontColor = Palette.TEXT;
        select.background = patch(atlas, "image_ui_mainmenu_text_entry_field", 12, 12, 10, 10);
        select.scrollStyle = scroll;
        select.listStyle = list;
        skin.add("default", select);

        Slider.SliderStyle slider = new Slider.SliderStyle();
        slider.background = patch(atlas, "image_ui_generic_xp_progress_bar", 8, 8, 7, 7);
        slider.knobBefore = patch(atlas, "image_ui_generic_xp_progress_bar_fill_green", 8, 8, 7, 7);
        slider.knob = sized(atlas, "image_ui_almanac_scroll_slider", 26f);
        skin.add("default-horizontal", slider);
    }

    private static void bars(Skin skin, TextureAtlas atlas) {
        ProgressBar.ProgressBarStyle bar = new ProgressBar.ProgressBarStyle();
        bar.background = patch(atlas, "image_ui_generic_xp_progress_bar", 8, 8, 7, 7);
        bar.knobBefore = patch(atlas, "image_ui_generic_xp_progress_bar_fill_green", 8, 8, 7, 7);
        bar.background.setMinHeight(20f);
        bar.knobBefore.setMinHeight(20f);
        skin.add("default-horizontal", bar);

        ProgressBar.ProgressBarStyle gold = new ProgressBar.ProgressBarStyle();
        gold.background = patch(atlas, "image_ui_generic_xp_progress_bar", 8, 8, 7, 7);
        gold.knobBefore = patch(atlas, "image_ui_generic_xp_progress_bar_fill_yellow", 8, 8, 7, 7);
        gold.background.setMinHeight(18f);
        gold.knobBefore.setMinHeight(18f);
        skin.add("gold-horizontal", gold);
    }

    private static void windows(Skin skin, TextureAtlas atlas, GameAssets assets) {
        skin.add("panel", patch(atlas, "image_ui_dialog_asset_dialog_center", 8, 8, 8, 8), Drawable.class);
        skin.add("inset", patch(atlas, "image_ui_mainmenu_inset_bkgd", 16, 16, 16, 16), Drawable.class);
        skin.add("card", patch(atlas, "image_ui_quests_quest_panel_default", 44, 44, 44, 44), Drawable.class);
        skin.add("card-epic", patch(atlas, "image_ui_quests_quest_panel_epic", 44, 44, 44, 44), Drawable.class);
        skin.add("card-done", patch(atlas, "image_ui_quests_quest_panel_complete", 44, 44, 44, 44), Drawable.class);
        skin.add("row", patch(atlas, "image_ui_mainmenu_inset_bkgd", 16, 16, 16, 16), Drawable.class);
        skin.add("row-done", patch(atlas, "image_ui_generic_greentab_active", 40, 40, 24, 24), Drawable.class);
        skin.add("slot", skin.newDrawable("white", Palette.SLOT), Drawable.class);
        skin.add("scrim", skin.newDrawable("white", Palette.SCRIM), Drawable.class);
        skin.add("shade", skin.newDrawable("white", Palette.PANEL), Drawable.class);
        skin.add("highlight", skin.newDrawable("white", Palette.SELECTED), Drawable.class);

        Window.WindowStyle window = new Window.WindowStyle();
        window.titleFont = assets.font("h1");
        window.titleFontColor = Palette.GOLD;
        window.background = patch(atlas, "image_ui_dialog_asset_dialogborder", 52, 52, 54, 52);
        window.stageBackground = skin.newDrawable("white", Palette.SCRIM);
        skin.add("default", window);
    }

    private static NinePatchDrawable patch(TextureAtlas atlas, String name,
                                           int left, int right, int top, int bottom) {
        TextureRegion region = atlas.findRegion(name);
        if (region == null) {
            NinePatch flat = new NinePatch(atlas.findRegion("white-pixel"), Color.GRAY);
            return new NinePatchDrawable(flat);
        }
        int l = Math.min(left, Math.max(0, region.getRegionWidth() / 2 - 1));
        int r = Math.min(right, Math.max(0, region.getRegionWidth() / 2 - 1));
        int t = Math.min(top, Math.max(0, region.getRegionHeight() / 2 - 1));
        int b = Math.min(bottom, Math.max(0, region.getRegionHeight() / 2 - 1));
        return new NinePatchDrawable(new NinePatch(region, l, r, t, b));
    }

    private static TextureRegionDrawable sized(TextureAtlas atlas, String name, float size) {
        TextureRegionDrawable drawable = new TextureRegionDrawable(atlas.findRegion(name));
        float ratio = drawable.getMinHeight() == 0 ? 1f : drawable.getMinWidth() / drawable.getMinHeight();
        drawable.setMinHeight(size);
        drawable.setMinWidth(size * ratio);
        return drawable;
    }
}
