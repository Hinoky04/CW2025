package com.comp2042.helpers;

import com.comp2042.models.DownData;
import com.comp2042.models.EventSource;
import com.comp2042.models.EventType;
import com.comp2042.models.GameSettings;
import com.comp2042.models.GameState;
import com.comp2042.models.MoveEvent;
import com.comp2042.models.ViewData;
import com.comp2042.interfaces.InputEventListener;
import com.comp2042.audio.SoundManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Helper class for input handling logic extracted from GuiController.
 * Handles keyboard input and delegates to appropriate game actions.
 */
public class GuiInputHandler {
    
    private final GameSettings gameSettings;
    private GameState gameState;
    private final InputEventListener eventListener;
    private final java.util.function.Consumer<ViewData> refreshBrickCallback;
    private final java.util.function.Consumer<MoveEvent> moveDownCallback;
    private final java.util.function.Consumer<DownData> hardDropCallback;
    private final Runnable togglePauseCallback;
    private final Runnable restartCallback;
    private final Runnable backToMainMenuCallback;
    private final java.util.function.Supplier<GameState> gameStateSupplier;
    
    /**
     * Creates a new input handler with the specified dependencies.
     *
     * @param gameSettings the game settings containing key bindings
     * @param gameState the initial game state
     * @param eventListener the event listener for game actions
     * @param refreshBrickCallback callback to refresh the brick display
     * @param moveDownCallback callback for soft drop movement
     * @param hardDropCallback callback for hard drop with notifications
     * @param togglePauseCallback callback to toggle pause state
     * @param restartCallback callback to restart the game
     * @param backToMainMenuCallback callback to return to main menu
     * @param gameStateSupplier supplier to get current game state dynamically
     */
    public GuiInputHandler(
            GameSettings gameSettings,
            GameState gameState,
            InputEventListener eventListener,
            java.util.function.Consumer<ViewData> refreshBrickCallback,
            java.util.function.Consumer<MoveEvent> moveDownCallback,
            java.util.function.Consumer<DownData> hardDropCallback,
            Runnable togglePauseCallback,
            Runnable restartCallback,
            Runnable backToMainMenuCallback,
            java.util.function.Supplier<GameState> gameStateSupplier) {
        this.gameSettings = gameSettings;
        this.gameState = gameState;
        this.eventListener = eventListener;
        this.refreshBrickCallback = refreshBrickCallback;
        this.moveDownCallback = moveDownCallback;
        this.hardDropCallback = hardDropCallback;
        this.togglePauseCallback = togglePauseCallback;
        this.restartCallback = restartCallback;
        this.backToMainMenuCallback = backToMainMenuCallback;
        this.gameStateSupplier = gameStateSupplier;
    }
    
    /**
     * Centralised key handler that processes keyboard input and delegates to appropriate game actions.
     * Handles movement, rotation, hold, soft/hard drop, pause, and restart based on configured key bindings.
     *
     * @param event the key event to process
     */
    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        
        // Get current game state
        GameState currentState = gameStateSupplier != null ? gameStateSupplier.get() : gameState;

        if (currentState == GameState.GAME_OVER) {
            handleGameOverKey(event);
            return;
        }

        // Pause toggle (uses settings - both P and ESC work)
        if (code == gameSettings.getPause() || code == gameSettings.getPauseAlt()) {
            togglePauseCallback.run();
            event.consume();
            return;
        }

        // Restart (uses settings)
        if (code == gameSettings.getRestart()) {
            restartCallback.run();
            event.consume();
            return;
        }

        if (!canHandleInput()) {
            return;
        }

        // Move left (uses settings)
        if (code == gameSettings.getMoveLeft()) {
            refreshBrickCallback.accept(eventListener.onLeftEvent(
                    new MoveEvent(EventType.LEFT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }

        // Move right (uses settings)
        if (code == gameSettings.getMoveRight()) {
            refreshBrickCallback.accept(eventListener.onRightEvent(
                    new MoveEvent(EventType.RIGHT, EventSource.USER)));
            SoundManager.playMove();
            event.consume();
        }

        // Rotate (uses settings)
        if (code == gameSettings.getRotate()) {
            refreshBrickCallback.accept(eventListener.onRotateEvent(
                    new MoveEvent(EventType.ROTATE, EventSource.USER)));
            SoundManager.playRotate();
            event.consume();
        }

        // Hold (uses settings)
        if (code == gameSettings.getHold()) {
            refreshBrickCallback.accept(eventListener.onHoldEvent(
                    new MoveEvent(EventType.DOWN, EventSource.USER)));
            SoundManager.playHold();
            event.consume();
        }

        // Soft drop (uses settings)
        if (code == gameSettings.getSoftDrop()) {
            moveDownCallback.accept(new MoveEvent(EventType.DOWN, EventSource.USER));
            SoundManager.playMove();
            event.consume();
        }

        // Hard drop (uses settings)
        if (code == gameSettings.getHardDrop()) {
            DownData downData = eventListener.onHardDropEvent(
                    new MoveEvent(EventType.HARD_DROP, EventSource.USER));
            hardDropCallback.accept(downData);
            refreshBrickCallback.accept(downData.getViewData());
            event.consume();
        }
    }

    /**
     * Key handling when the game has finished.
     * R key restarts, M or ESC returns to main menu.
     *
     * @param event the key event to process
     */
    private void handleGameOverKey(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.R) {
            restartCallback.run();
            event.consume();
        } else if (code == KeyCode.M || code == KeyCode.ESCAPE) {
            backToMainMenuCallback.run();
            event.consume();
        }
    }

    /**
     * Checks if input can be handled in the current game state.
     * Only allows input when the game is in PLAYING state.
     *
     * @return true if input can be handled, false otherwise
     */
    private boolean canHandleInput() {
        GameState currentState = gameStateSupplier != null ? gameStateSupplier.get() : gameState;
        return currentState == GameState.PLAYING;
    }
}
