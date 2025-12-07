package com.comp2042.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * Handles looping background music for the game screen.
 * All methods are static so any controller can start/stop music easily.
 */
public final class MusicPlayer {

    private static MediaPlayer backgroundPlayer;

    // Lower than 1.0 so SFX stand out over the music.
    private static final double MUSIC_VOLUME = 0.3;

    private MusicPlayer() {
        // Utility class, no instances.
    }

    /**
     * Lazily creates the MediaPlayer for background.mp3 and configures it.
     */
    private static MediaPlayer ensurePlayer() {
        if (backgroundPlayer != null) {
            return backgroundPlayer;
        }

        try {
            URL url = MusicPlayer.class.getResource("/sound/background.mp3");
            if (url == null) {
                System.err.println("MusicPlayer: Background music file not found: /sound/background.mp3");
                return null;
            }

            System.out.println("MusicPlayer: Loading background music from: " + url);
            Media media = new Media(url.toExternalForm());
            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundPlayer.setVolume(MUSIC_VOLUME);
            System.out.println("MusicPlayer: Background music player created successfully");
            return backgroundPlayer;
        } catch (Exception e) {
            System.err.println("MusicPlayer: Failed to create background music player: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Starts playing background music from the beginning in a loop.
     * Safe to call multiple times; it will reuse the same MediaPlayer.
     */
    public static void startBackgroundMusic() {
        try {
        MediaPlayer player = ensurePlayer();
        if (player == null) {
                System.err.println("MusicPlayer: Cannot start music - player is null");
            return;
        }

        // Always restart from the beginning for a new game.
        player.stop();
        player.seek(Duration.ZERO);
        player.setVolume(MUSIC_VOLUME);
        player.play();
            System.out.println("MusicPlayer: Background music started");
        } catch (Exception e) {
            System.err.println("MusicPlayer: Error starting background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pauses background music (used when the game is paused).
     */
    public static void pauseBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.pause();
        }
    }

    /**
     * Resumes background music after a pause.
     */
    public static void resumeBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.setVolume(MUSIC_VOLUME);
            backgroundPlayer.play();
        }
    }

    /**
     * Stops background music completely.
     * Used on game over, restart, or when leaving the game screen.
     */
    public static void stopBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.seek(Duration.ZERO);
        }
    }
}
