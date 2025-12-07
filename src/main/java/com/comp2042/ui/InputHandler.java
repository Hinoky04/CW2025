package com.comp2042.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import com.comp2042.models.DownData;
import com.comp2042.models.EventSource;
import com.comp2042.models.EventType;
import com.comp2042.models.GameSettings;
import com.comp2042.models.GameState;
import com.comp2042.models.MoveEvent;
import com.comp2042.audio.SoundManager;
import com.comp2042.interfaces.InputEventListener;

/**
 * Handles keyboard input for the game.
 * Processes key presses and delegates to appropriate game actions.
 */
public class InputHandler {
    
    private final GameSettings gameSettings;
    private final GameState gameState;
    private final InputEventListener eventListener;
    private final BoardRenderer boardRenderer;
    private final Runnable togglePause;
    private final Runnable restartSameMode;
    private final Runnable backToMainMenu;
    
    public InputHandler(GameSettings gameSettings,
                       GameState gameState,
                       InputEventListener eventListener,
                       BoardRenderer boardRenderer,
                       Runnable togglePause,
                       Runnable restartSameMode,
                       Runnable backToMainMenu) {
        this.gameSettings = gameSettings;
        this.gameState = gameState;
        this.eventListener = eventListener;
        this.boardRenderer = boardRenderer;
        this.togglePause = togglePause;
        this.restartSameMode = restartSameMode;
        this.backToMainMenu = backToMainMenu;
    }
    
    /**
     * Centralised key handler.
     */
    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (gameState == GameState.GAME_OVER) {
            handleGameOverKey(event);
            return;
        }
        
        // Pause toggle (uses settings - both P and ESC work)
        if (code == gameSettings.getPause() || code == gameSettings.getPauseAlt()) {
            togglePause.run();
            event.consume();
            return;
        }
        
        // Restart (uses settings)
        if (code == gameSettings.getRestart()) {
            restartSameMode.run();
            event.consume();
            return;
        }
        
        if (!canHandleInput()) {
            return;
        }
        
        // Move left (uses settings)
        if (code == gameSettings.getMoveLeft()) {
            boardRenderer.refreshBrick(eventListener.onLeftEvent(
                    new MoveEvent(EventType.LEFT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }
        
        // Move right (uses settings)
        if (code == gameSettings.getMoveRight()) {
            boardRenderer.refreshBrick(eventListener.onRightEvent(
                    new MoveEvent(EventType.RIGHT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }
        
        // Rotate (uses settings)
        if (code == gameSettings.getRotate()) {
            boardRenderer.refreshBrick(eventListener.onRotateEvent(
                    new MoveEvent(EventType.ROTATE, EventSource.USER)));
            SoundManager.playRotate();
            event.consume();
        }
        
        // Hold (uses settings)
        if (code == gameSettings.getHold()) {
            boardRenderer.refreshBrick(eventListener.onHoldEvent(
                    new MoveEvent(EventType.DOWN, EventSource.USER)));
            SoundManager.playHold();
            event.consume();
        }
        
        // Soft drop (uses settings)
        if (code == gameSettings.getSoftDrop()) {
            // This will be handled by the caller
            event.consume();
        }
        
        // Hard drop (uses settings)
        if (code == gameSettings.getHardDrop()) {
            DownData downData = eventListener.onHardDropEvent(
                    new MoveEvent(EventType.HARD_DROP, EventSource.USER));
            if (downData.getClearRow() != null &&
                    downData.getClearRow().getLinesRemoved() > 0) {
                // Notification will be handled by caller
                SoundManager.playLineClear();
            }
            boardRenderer.refreshBrick(downData.getViewData());
            event.consume();
        }
    }
    
    /**
     * Key handling when the game has finished.
     */
    private void handleGameOverKey(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if (code == KeyCode.R) {
            restartSameMode.run();
            event.consume();
        } else if (code == KeyCode.M || code == KeyCode.ESCAPE) {
            backToMainMenu.run();
            event.consume();
        }
    }
    
    private boolean canHandleInput() {
        return gameState == GameState.PLAYING;
    }
}

