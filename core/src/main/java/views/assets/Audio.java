package views.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import models.settings.GamePreferences;

public final class Audio implements Disposable {

    public static final String MENU = "menu";
    public static final String BATTLE = "battle";
    public static final String BOSS = "boss";
    public static final String MINIGAME = "minigame";

    public static final String PLANT = "plant";
    public static final String SHOOT = "shoot";
    public static final String LOB = "lob";
    public static final String SPLAT = "splat";
    public static final String EXPLODE = "explode";
    public static final String GULP = "gulp";
    public static final String SUN = "sun";
    public static final String MOWER = "mower";
    public static final String CLICK = "click";
    public static final String LOSE = "lose";
    public static final String WIN = "win";
    public static final String BITE = "bite";
    public static final String ZOMBIE_DIES = "zombie-dies";
    public static final String ARMOUR_BREAKS = "armour-breaks";
    public static final String PLANT_DIES = "plant-dies";
    public static final String SHOVEL = "shovel";
    public static final String WAVE = "wave";
    public static final String HUGE_WAVE = "huge-wave";
    public static final String GRAVE_BREAKS = "grave-breaks";
    public static final String RISE = "rise";
    public static final String STORM = "storm";
    public static final String TIDE = "tide";
    public static final String MINT = "mint";
    public static final String BOSS_MOVE = "boss-move";
    public static final String BOSS_HURT = "boss-hurt";
    public static final String CHIME = "chime";

    private static final String[] MUSIC_CUES = {MENU, BATTLE, BOSS, MINIGAME};
    private static final String[] SOUND_CUES = {
        PLANT, SHOOT, LOB, SPLAT, EXPLODE, GULP, SUN, MOWER, CLICK, LOSE, WIN,
        BITE, ZOMBIE_DIES, ARMOUR_BREAKS, PLANT_DIES, SHOVEL, WAVE, HUGE_WAVE,
        GRAVE_BREAKS, RISE, STORM, TIDE, MINT, BOSS_MOVE, BOSS_HURT, CHIME};

    public static String[] musicCues() {
        return MUSIC_CUES.clone();
    }

    public static String[] soundCues() {
        return SOUND_CUES.clone();
    }

    public static String[] extensions() {
        return EXTENSIONS.clone();
    }

    public static String folder() {
        return ROOT;
    }

    public boolean isPresent(String name) {
        return locate(name) != null;
    }

    private static final String ROOT = "AUDIO/";
    private static final String[] EXTENSIONS = {".ogg", ".mp3", ".wav"};

    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();
    private final ObjectMap<String, Music> tracks = new ObjectMap<>();

    private String username;
    private String playing;
    private Music current;

    public void setUser(String username) {
        this.username = username;
        applyVolume();
    }

    public void playMusic(String name) {
        if (name == null || name.equals(playing)) {
            return;
        }
        stopMusic();
        Music track = track(name);
        if (track == null) {
            playing = name;
            return;
        }
        playing = name;
        current = track;
        track.setLooping(true);
        track.setVolume(musicVolume());
        track.play();
    }

    public void stopMusic() {
        if (current != null) {
            current.stop();
            current = null;
        }
        playing = null;
    }

    public void play(String name) {
        Sound sound = sound(name);
        float volume = sfxVolume();
        if (sound != null && volume > 0f) {
            sound.play(volume);
        }
    }

    public void applyVolume() {
        if (current != null) {
            current.setVolume(musicVolume());
        }
    }

    private float musicVolume() {
        return GamePreferences.getMusicVolume(username) / 100f;
    }

    private float sfxVolume() {
        return GamePreferences.getSfxVolume(username) / 100f;
    }

    private Music track(String name) {
        if (tracks.containsKey(name)) {
            return tracks.get(name);
        }
        FileHandle file = locate(name);
        Music music = null;
        if (file != null) {
            try {
                music = Gdx.audio.newMusic(file);
            } catch (RuntimeException ignored) {
                music = null;
            }
        }
        tracks.put(name, music);
        return music;
    }

    private Sound sound(String name) {
        if (sounds.containsKey(name)) {
            return sounds.get(name);
        }
        FileHandle file = locate(name);
        Sound sound = null;
        if (file != null) {
            try {
                sound = Gdx.audio.newSound(file);
            } catch (RuntimeException ignored) {
                sound = null;
            }
        }
        sounds.put(name, sound);
        return sound;
    }

    private FileHandle locate(String name) {
        for (String extension : EXTENSIONS) {
            FileHandle file = Gdx.files.internal(ROOT + name + extension);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        stopMusic();
        for (Music track : tracks.values()) {
            if (track != null) {
                track.dispose();
            }
        }
        for (Sound sound : sounds.values()) {
            if (sound != null) {
                sound.dispose();
            }
        }
        tracks.clear();
        sounds.clear();
    }
}
