package com.comp2042.helpers;

import com.comp2042.Main;
import com.comp2042.models.GameMode;
import com.comp2042.audio.MusicPlayer;

/**
 * Helper class for navigation logic extracted from GuiController.
 * Handles navigation actions like restart, settings, and main menu.
 */
public class GuiNavigationHandler {
    
    private final Main mainApp;
    private final GameMode currentMode;
    private final GuiTimerHelper timerHelper;
    private final GuiStateManager stateManager;
    
    /**
     * Creates a new navigation handler with the specified dependencies.
     *
     * @param mainApp the main application for scene navigation
     * @param currentMode the current game mode
     * @param timerHelper helper for timer management
     * @param stateManager helper for state management
     */
    public GuiNavigationHandler(
            Main mainApp,
            GameMode currentMode,
            GuiTimerHelper timerHelper,
            GuiStateManager stateManager) {
        this.mainApp = mainApp;
        this.currentMode = currentMode;
        this.timerHelper = timerHelper;
        this.stateManager = stateManager;
    }
    
    /**
     * Restarts the game in the same mode.
     */
    public void restartSameMode() {
        if (mainApp == null || currentMode == null) {
            return;
        }

        if (timerHelper != null) {
            timerHelper.stop();
        }

        // Reset game over state in color helper when restarting
        stateManager.resetGameOverState();

        try {
            MusicPlayer.stopBackgroundMusic();
        } catch (Exception | Error e) {
            System.err.println("Warning: Could not stop background music: " + e.getMessage());
            e.printStackTrace();
        }

        mainApp.showGameScene(currentMode);
    }
    
    /**
     * Navigates to settings screen.
     */
    public void goToSettings() {
        // Pause the game while in settings
        if (timerHelper != null) {
            timerHelper.pause();
        }
        try {
            MusicPlayer.pauseBackgroundMusic();
        } catch (Exception | Error e) {
            System.err.println("Warning: Could not pause background music: " + e.getMessage());
            e.printStackTrace();
        }

        if (mainApp != null && currentMode != null) {
            // Pass the current game mode so we can return to it after settings
            mainApp.showSettingsScene(currentMode);
        }
    }
    
    /**
     * Navigates back to main menu.
     */
    public void backToMainMenu() {
        if (timerHelper != null) {
            timerHelper.stop();
        }

        try {
            MusicPlayer.stopBackgroundMusic();
        } catch (Exception | Error e) {
            System.err.println("Warning: Could not stop background music: " + e.getMessage());
            e.printStackTrace();
        }

        if (mainApp != null) {
            mainApp.showMainMenu();
        }
    }
}
