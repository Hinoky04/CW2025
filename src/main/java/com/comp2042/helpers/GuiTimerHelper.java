package com.comp2042.helpers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.comp2042.models.MoveEvent;
import com.comp2042.models.EventSource;

/**
 * Helper class for timer logic extracted from GuiController.
 * Contains exact same code, just moved to separate file.
 */
public class GuiTimerHelper {
    
    private int fallIntervalMs = 400;
    private double levelSpeedFactor = 0.15;
    private boolean timerEnabled;
    private long timerStartNanos;
    private long timerPauseStartNanos;
    private long timerPausedAccumNanos;
    private boolean timerRunning;
    
    private Timeline timeLine;
    private Timeline hudTimer;
    private final Text timerText;
    private final java.util.function.Consumer<MoveEvent> moveDownCallback;
    
    /**
     * Creates a new timer helper.
     *
     * @param timerText the text component to display timer (may be null)
     * @param moveDownCallback callback to execute when auto-drop timer fires
     */
    public GuiTimerHelper(Text timerText, java.util.function.Consumer<MoveEvent> moveDownCallback) {
        this.timerText = timerText;
        this.moveDownCallback = moveDownCallback;
    }
    
    /**
     * Sets the base fall interval in milliseconds.
     *
     * @param fallIntervalMs the fall interval in milliseconds
     */
    public void setFallIntervalMs(int fallIntervalMs) {
        this.fallIntervalMs = fallIntervalMs;
    }
    
    /**
     * Sets the level speed factor (how much faster each level becomes).
     *
     * @param levelSpeedFactor the speed factor (e.g., 0.15 = 15% faster per level)
     */
    public void setLevelSpeedFactor(double levelSpeedFactor) {
        this.levelSpeedFactor = levelSpeedFactor;
    }
    
    /**
     * Sets whether the HUD timer is enabled.
     *
     * @param timerEnabled true to enable timer display, false to disable
     */
    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }
    
    /**
     * Gets the auto-drop timeline.
     *
     * @return the timeline for automatic brick dropping
     */
    public Timeline getTimeLine() {
        return timeLine;
    }
    
    /**
     * Starts the automatic drop timer that moves bricks down periodically.
     */
    public void startAutoDropTimer() {
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(fallIntervalMs),
                ae -> moveDownCallback.accept(new MoveEvent(com.comp2042.models.EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    /**
     * Starts the HUD timer if enabled.
     * Displays elapsed time in the timer text component.
     */
    public void startHudTimerIfNeeded() {
        if (!timerEnabled || timerText == null) {
            return;
        }

        timerStartNanos = System.nanoTime();
        timerPauseStartNanos = 0L;
        timerPausedAccumNanos = 0L;
        timerRunning = true;

        if (hudTimer != null) {
            hudTimer.stop();
        }

        hudTimer = new Timeline(new KeyFrame(
                Duration.millis(200),
                ae -> updateTimerText()
        ));
        hudTimer.setCycleCount(Timeline.INDEFINITE);
        hudTimer.play();

        timerText.setText("Time 00:00");
    }

    private void updateTimerText() {
        if (!timerEnabled || timerText == null || timerStartNanos == 0L) {
            return;
        }

        long now = System.nanoTime();
        long effectiveNanos;

        if (timerRunning) {
            effectiveNanos = now - timerStartNanos - timerPausedAccumNanos;
        } else if (timerPauseStartNanos != 0L) {
            effectiveNanos = timerPauseStartNanos - timerStartNanos - timerPausedAccumNanos;
        } else {
            effectiveNanos = now - timerStartNanos - timerPausedAccumNanos;
        }

        if (effectiveNanos < 0L) {
            effectiveNanos = 0L;
        }

        long totalSeconds = effectiveNanos / 1_000_000_000L;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        timerText.setText(String.format("Time %02d:%02d", minutes, seconds));
    }
    
    /**
     * Updates the auto-drop timer speed based on the new level.
     *
     * @param newLevel the new level (affects drop speed)
     */
    public void onLevelChanged(int newLevel) {
        if (timeLine == null) {
            return;
        }

        double rate = 1.0 + (newLevel - 1) * levelSpeedFactor;
        timeLine.setRate(rate);
    }
    
    /**
     * Pauses both the auto-drop timer and HUD timer.
     */
    public void pause() {
        if (timeLine != null) {
            timeLine.pause();
        }

        if (timerEnabled && timerRunning) {
            timerPauseStartNanos = System.nanoTime();
            timerRunning = false;
            if (hudTimer != null) {
                hudTimer.pause();
            }
        }
    }
    
    /**
     * Resumes both the auto-drop timer and HUD timer.
     */
    public void resume() {
        if (timeLine != null) {
            timeLine.play();
        }

        if (timerEnabled && !timerRunning) {
            if (timerPauseStartNanos != 0L) {
                long paused = System.nanoTime() - timerPauseStartNanos;
                timerPausedAccumNanos += paused;
                timerPauseStartNanos = 0L;
            }
            timerRunning = true;
            if (hudTimer != null) {
                hudTimer.play();
            }
        }
    }
    
    /**
     * Stops both the auto-drop timer and HUD timer.
     */
    public void stop() {
        if (timeLine != null) {
            timeLine.stop();
        }

        if (hudTimer != null) {
            hudTimer.stop();
        }
        timerRunning = false;
    }
}

