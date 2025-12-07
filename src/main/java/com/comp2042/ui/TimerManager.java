package com.comp2042.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Manages game timers including auto-drop timer and HUD timer.
 */
public class TimerManager {
    
    private Timeline timeLine;
    private Timeline hudTimer;
    
    private int fallIntervalMs = 400;
    private double levelSpeedFactor = 0.15;
    
    private boolean timerEnabled;
    private long timerStartNanos;
    private long timerPauseStartNanos;
    private long timerPausedAccumNanos;
    private boolean timerRunning;
    
    private final HudManager hudManager;
    private final Runnable onAutoDrop;
    
    public TimerManager(HudManager hudManager, Runnable onAutoDrop) {
        this.hudManager = hudManager;
        this.onAutoDrop = onAutoDrop;
    }
    
    public void setFallIntervalMs(int fallIntervalMs) {
        this.fallIntervalMs = fallIntervalMs;
    }
    
    public void setLevelSpeedFactor(double levelSpeedFactor) {
        this.levelSpeedFactor = levelSpeedFactor;
    }
    
    public void setTimerEnabled(boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
        if (!timerEnabled) {
            hudManager.clearTimerText();
        }
    }
    
    public void startAutoDropTimer() {
        timeLine = new Timeline(new KeyFrame(
                Duration.millis(fallIntervalMs),
                ae -> onAutoDrop.run()
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }
    
    public void startHudTimerIfNeeded() {
        if (!timerEnabled) {
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
        
        hudManager.setTimerText("Time 00:00");
    }
    
    private void updateTimerText() {
        if (!timerEnabled || timerStartNanos == 0L) {
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
        
        hudManager.setTimerText(String.format("Time %02d:%02d", minutes, seconds));
    }
    
    public void onLevelChanged(int newLevel) {
        if (timeLine == null) {
            return;
        }
        
        double rate = 1.0 + (newLevel - 1) * levelSpeedFactor;
        timeLine.setRate(rate);
    }
    
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
    
    public void stop() {
        if (timeLine != null) {
            timeLine.stop();
        }
        if (hudTimer != null) {
            hudTimer.stop();
        }
        timerRunning = false;
    }
    
    public Timeline getTimeLine() {
        return timeLine;
    }
}

