package com.comp2042.helpers;

import com.comp2042.models.GameMode;
import com.comp2042.models.GameState;
import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.Pane;

/**
 * Helper class for game state management logic extracted from GuiController.
 * Handles game state transitions and overlay visibility.
 */
public class GuiStateManager {
    
    private GameState gameState = GameState.PLAYING;
    private final BooleanProperty isPause;
    private final BooleanProperty isGameOver;
    private final Pane pauseOverlay;
    private final com.comp2042.ui.GameOverPanel gameOverPanel;
    private final GuiDangerHelper dangerHelper;
    private final GuiColorHelper colorHelper;
    private final GameMode currentMode;
    
    /**
     * Creates a new state manager with the specified UI components and helpers.
     *
     * @param isPause boolean property for pause state
     * @param isGameOver boolean property for game over state
     * @param pauseOverlay the pause overlay pane
     * @param gameOverPanel the game over panel
     * @param dangerHelper helper for danger zone management
     * @param colorHelper helper for color management
     * @param currentMode the current game mode
     */
    public GuiStateManager(
            BooleanProperty isPause,
            BooleanProperty isGameOver,
            Pane pauseOverlay,
            com.comp2042.ui.GameOverPanel gameOverPanel,
            GuiDangerHelper dangerHelper,
            GuiColorHelper colorHelper,
            GameMode currentMode) {
        this.isPause = isPause;
        this.isGameOver = isGameOver;
        this.pauseOverlay = pauseOverlay;
        this.gameOverPanel = gameOverPanel;
        this.dangerHelper = dangerHelper;
        this.colorHelper = colorHelper;
        this.currentMode = currentMode;
    }
    
    /**
     * Central helper for changing game state.
     * Keeps internal flags and overlays in sync.
     *
     * @param newState the new game state to set
     */
    public void setGameState(GameState newState) {
        gameState = newState;
        isPause.set(newState == GameState.PAUSED);
        isGameOver.set(newState == GameState.GAME_OVER);

        if (pauseOverlay != null) {
            pauseOverlay.setVisible(newState == GameState.PAUSED);
        }
        if (gameOverPanel != null) {
            gameOverPanel.setVisible(newState == GameState.GAME_OVER);
        }

        if (newState == GameState.GAME_OVER) {
            if (dangerHelper != null) {
                dangerHelper.setDanger(false);
            }
        }
    }
    
    /**
     * Gets the current game state.
     *
     * @return the current game state
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Sets game over state and makes blocks visible in Invisible mode.
     */
    public void setGameOver() {
        setGameState(GameState.GAME_OVER);
        
        // In Invisible mode, make all blocks visible after game over
        if (colorHelper != null && currentMode == GameMode.HYPER) {
            colorHelper.setGameOver(true);
        }
    }
    
    /**
     * Resets game over state in color helper.
     */
    public void resetGameOverState() {
        if (colorHelper != null) {
            colorHelper.setGameOver(false);
        }
    }
}
