package com.comp2042.helpers;

import com.comp2042.models.DownData;
import com.comp2042.ui.NotificationPanel;
import com.comp2042.audio.SoundManager;
import javafx.scene.Group;

/**
 * Helper class for notification logic extracted from GuiController.
 * Handles displaying notifications like score bonuses, milestones, and congratulations.
 */
public class GuiNotificationHandler {
    
    private final Group groupNotification;
    
    /**
     * Creates a new notification handler.
     *
     * @param groupNotification the JavaFX Group container for displaying notifications
     */
    public GuiNotificationHandler(Group groupNotification) {
        this.groupNotification = groupNotification;
    }
    
    /**
     * Shows a score bonus notification when lines are cleared.
     *
     * @param downData the down data containing line clear information
     */
    public void showScoreBonus(DownData downData) {
        if (groupNotification == null || downData.getClearRow() == null ||
                downData.getClearRow().getLinesRemoved() <= 0) {
            return;
        }

        String bonusText = "+" + downData.getClearRow().getScoreBonus();
        NotificationPanel notificationPanel = new NotificationPanel(bonusText);
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());

        SoundManager.playLineClear();
    }
    
    /**
     * Shows a milestone notification for Rush 40 mode.
     * Called when the player reaches milestones (10, 20, 30, 40 lines).
     * Positioned higher than score bonuses to avoid overlap.
     *
     * @param message the milestone message to display (e.g., "10 Lines Cleared!")
     */
    public void showRushMilestone(String message) {
        if (groupNotification == null) {
            return;
        }
        NotificationPanel notificationPanel = new NotificationPanel(message);
        // Position milestone notifications higher (about 60 pixels above score bonuses)
        notificationPanel.setLayoutY(143.0); // Original is 203.0, so 203 - 60 = 143
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
        
        // Play a sound for milestone achievement (reuse line clear sound or could add a new one)
        SoundManager.playLineClear();
    }
    
    /**
     * Shows a congratulations message when Rush 40 mode is completed.
     * Displays a special celebration message for successfully clearing 40 lines.
     */
    public void showRush40Congratulations() {
        if (groupNotification == null) {
            return;
        }
        NotificationPanel notificationPanel = new NotificationPanel("🎉 Congratulations! You cleared 40 lines! 🎉");
        // Position congratulations message prominently in the center
        notificationPanel.setLayoutY(100.0);
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
        
        // Play a special sound for completion (using game over sound or could add a new one)
        SoundManager.playGameOver();
    }
}
