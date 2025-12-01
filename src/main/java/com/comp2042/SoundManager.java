package com.comp2042;

import javafx.scene.media.AudioClip;

import java.net.URL;

/**
 * Central place for short sound effects (SFX).
 * All methods are static so callers can simply call playMove(), playRotate(), etc.
 */
public final class SoundManager {

    // Global SFX volume (0.0 - 1.0, JavaFX allows a bit above 1.0 but 1.0 is usually enough).
    private static final double SFX_VOLUME = 1.0;

    // Filenames inside src/main/resources/sound/
    private static final String MOVE_FILE        = "move.mp3";
    private static final String ROTATE_FILE     = "rotate.mp3";
    private static final String HOLD_FILE       = "hold.mp3";
    private static final String LINE_CLEAR_FILE = "line_clear.mp3";
    private static final String GAME_OVER_FILE  = "game_over.mp3";
    private static final String UI_CLICK_FILE   = "ui_click.mp3";
    private static final String START_GAME_FILE = "start_game.mp3";
    private static final String PAUSE_FILE      = "pause.mp3";

    // Static clips loaded once.
    private static final AudioClip MOVE_CLIP        = load(MOVE_FILE);
    private static final AudioClip ROTATE_CLIP     = load(ROTATE_FILE);
    private static final AudioClip HOLD_CLIP       = load(HOLD_FILE);
    private static final AudioClip LINE_CLEAR_CLIP = load(LINE_CLEAR_FILE);
    private static final AudioClip GAME_OVER_CLIP  = load(GAME_OVER_FILE);
    private static final AudioClip UI_CLICK_CLIP   = load(UI_CLICK_FILE);
    private static final AudioClip START_GAME_CLIP = load(START_GAME_FILE);
    private static final AudioClip PAUSE_CLIP      = load(PAUSE_FILE);

    private SoundManager() {
        // Utility class, no instances.
    }

    /**
     * Loads an AudioClip from the /sound/ resource folder.
     */
    private static AudioClip load(String fileName) {
        try {
            URL resource = SoundManager.class.getResource("/sound/" + fileName);
            if (resource == null) {
                System.err.println("Sound file not found: " + fileName);
                return null;
            }
            AudioClip clip = new AudioClip(resource.toExternalForm());
            clip.setVolume(SFX_VOLUME);
            return clip;
        } catch (Exception e) {
            System.err.println("Failed to load sound " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Plays the given clip if available using the global SFX volume.
     */
    private static void play(AudioClip clip) {
        if (clip == null) {
            return;
        }
        // Re-apply volume in case we later decide to make it configurable.
        clip.setVolume(SFX_VOLUME);
        clip.play();
    }

    // === Public helpers used by the rest of the game ===

    public static void playMove() {
        play(MOVE_CLIP);
    }

    public static void playRotate() {
        play(ROTATE_CLIP);
    }

    public static void playHold() {
        play(HOLD_CLIP);
    }

    public static void playLineClear() {
        play(LINE_CLEAR_CLIP);
    }

    public static void playGameOver() {
        play(GAME_OVER_CLIP);
    }

    public static void playUiClick() {
        play(UI_CLICK_CLIP);
    }

    public static void playStartGame() {
        play(START_GAME_CLIP);
    }

    public static void playPause() {
        play(PAUSE_CLIP);
    }
}
