package com.comp2042.ui;

import javafx.beans.property.IntegerProperty;
import javafx.scene.text.Text;
import com.comp2042.models.GameMode;

/**
 * Manages HUD (Heads-Up Display) updates including score, level, lines, combo, timer, and progress.
 */
public class HudManager {
    
    private final Text scoreText;
    private final Text levelText;
    private final Text linesText;
    private final Text comboText;
    private final Text timerText;
    private final Text progressText;
    private final Text bestText;
    private final Text modeText;
    private final Text modeHintText;
    
    // Best records (static so they survive across runs)
    private static final int[] bestScores = new int[GameMode.values().length];
    private static final double[] bestRushTimes = new double[GameMode.values().length];
    
    static {
        for (int i = 0; i < bestRushTimes.length; i++) {
            bestRushTimes[i] = -1.0; // no record yet
        }
    }
    
    public HudManager(Text scoreText, Text levelText, Text linesText, Text comboText,
                     Text timerText, Text progressText, Text bestText, Text modeText, Text modeHintText) {
        this.scoreText = scoreText;
        this.levelText = levelText;
        this.linesText = linesText;
        this.comboText = comboText;
        this.timerText = timerText;
        this.progressText = progressText;
        this.bestText = bestText;
        this.modeText = modeText;
        this.modeHintText = modeHintText;
    }
    
    public void bindScore(IntegerProperty scoreProperty) {
        if (scoreText != null) {
            scoreText.textProperty().bind(scoreProperty.asString("Score %d"));
        }
    }
    
    public void bindLevel(IntegerProperty levelProperty) {
        if (levelText != null) {
            levelText.textProperty().bind(levelProperty.asString("Level %d"));
        }
    }
    
    public void bindLines(IntegerProperty linesProperty) {
        if (linesText != null) {
            linesText.textProperty().bind(linesProperty.asString("Lines %d"));
        }
    }
    
    public void bindCombo(IntegerProperty comboProperty) {
        if (comboText != null) {
            comboText.textProperty().bind(comboProperty.asString("Combo x%d"));
        }
    }
    
    public void setMode(GameMode mode) {
        if (modeText != null && mode != null) {
            modeText.setText("Mode: " + mode.getDisplayName());
        }
        
        if (modeHintText != null && mode != null) {
            modeHintText.setText(buildModeHint(mode));
        }
    }
    
    private String buildModeHint(GameMode mode) {
        if (mode == null) {
            return "";
        }
        switch (mode) {
            case CLASSIC:
                return "Classic: standard Tetris rules.";
            case SURVIVAL:
                return "Survival: clear 4 lines at once to gain a shield.";
            case HYPER:
                return "Hyper: faster with dimmed background.";
            case RUSH_40:
                return "Rush 40: clear 40 lines as fast as possible.";
            default:
                return "";
        }
    }
    
    public void clearProgressText() {
        if (progressText != null) {
            progressText.setText("");
        }
    }
    
    public void updateRushProgress(int linesCleared, int targetLines) {
        if (progressText == null) {
            return;
        }
        if (targetLines <= 0) {
            clearProgressText();
            return;
        }
        progressText.setText(String.format("Lines %d / %d", linesCleared, targetLines));
    }
    
    public void updateSurvivalStatus(int shields, int landingsUntilGarbage) {
        if (progressText == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Shields ").append(shields);
        
        if (landingsUntilGarbage >= 0) {
            sb.append(", Garbage in ").append(landingsUntilGarbage);
        }
        
        progressText.setText(sb.toString());
    }
    
    public void setTimerText(String text) {
        if (timerText != null) {
            timerText.setText(text);
        }
    }
    
    public void clearTimerText() {
        setTimerText("");
    }
    
    /**
     * Updates best score and best Rush-40 time for the given mode.
     */
    public void updateBestInfo(GameMode mode, int finalScore, double rushCompletionSeconds) {
        if (mode == null) {
            return;
        }
        int index = mode.ordinal();
        
        boolean newBestScore = false;
        boolean newBestTime = false;
        
        if (finalScore > bestScores[index]) {
            bestScores[index] = finalScore;
            newBestScore = true;
        }
        
        if (mode == GameMode.RUSH_40 && rushCompletionSeconds > 0.0) {
            double currentBest = bestRushTimes[index];
            if (currentBest < 0.0 || rushCompletionSeconds < currentBest) {
                bestRushTimes[index] = rushCompletionSeconds;
                newBestTime = true;
            }
        }
        
        refreshBestInfoForMode(mode, newBestScore, newBestTime);
    }
    
    /**
     * Refreshes best info for the given mode without highlighting.
     */
    public void refreshBestInfoForMode(GameMode mode) {
        refreshBestInfoForMode(mode, false, false);
    }
    
    private void refreshBestInfoForMode(GameMode mode,
                                       boolean highlightNewScore,
                                       boolean highlightNewTime) {
        if (bestText == null || mode == null) {
            return;
        }
        
        int index = mode.ordinal();
        int bestScore = bestScores[index];
        
        if (mode == GameMode.RUSH_40) {
            double bestTime = bestRushTimes[index];
            
            StringBuilder text = new StringBuilder();
            if (bestTime > 0.0) {
                long totalSeconds = (long) bestTime;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                text.append("Best Time ")
                    .append(String.format("%02d:%02d", minutes, seconds));
            } else {
                text.append("Best Time --:--");
            }
            
            if (highlightNewTime) {
                text.append(" (New!)");
            }
            
            bestText.setText(text.toString());
        } else {
            StringBuilder text = new StringBuilder();
            text.append("Best Score ").append(bestScore);
            if (highlightNewScore) {
                text.append(" (New!)");
            }
            bestText.setText(text.toString());
        }
    }
    
    public void setBestTextDefault() {
        if (bestText != null) {
            bestText.setText("Best Score 0");
        }
    }
}

