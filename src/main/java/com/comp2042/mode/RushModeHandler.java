package com.comp2042.mode;

import com.comp2042.models.ClearRow;

/**
 * Handles Rush-40 mode-specific logic including line count tracking and milestones.
 * Rush-40 mode challenges players to clear a target number of lines as fast as possible.
 */
public class RushModeHandler {
    
    private final int targetLines;
    
    // Rush-40 state
    private int linesCleared = 0;
    private int lastMilestone = 0;
    private boolean completed = false;
    private long startNanos = 0L;
    private long endNanos = 0L;
    
    /**
     * Creates a Rush-40 mode handler.
     * 
     * @param targetLines the target number of lines to clear (e.g., 40)
     * @param config the game configuration (reserved for future use)
     */
    public RushModeHandler(int targetLines, com.comp2042.models.GameConfig config) {
        this.targetLines = targetLines;
        // Config reserved for future use
    }
    
    /**
     * Starts the Rush-40 timer.
     */
    public void start() {
        startNanos = System.nanoTime();
        endNanos = 0L;
        linesCleared = 0;
        lastMilestone = 0;
        completed = false;
    }
    
    /**
     * Handles line clearing in Rush-40 mode.
     * Updates progress and checks for completion and milestones.
     * 
     * @param clearRow the result of line clearing (null if no lines cleared)
     * @return true if a new milestone was reached, false otherwise
     */
    public boolean handleLinesCleared(ClearRow clearRow) {
        if (completed || clearRow == null || clearRow.getLinesRemoved() <= 0) {
            return false;
        }
        
        int previousLines = linesCleared;
        linesCleared += clearRow.getLinesRemoved();
        
        // Check for completion
        if (linesCleared >= targetLines) {
            completed = true;
            endNanos = System.nanoTime();
            return checkMilestone(previousLines, linesCleared);
        }
        
        // Check for milestones (10, 20, 30, 40)
        return checkMilestone(previousLines, linesCleared);
    }
    
    /**
     * Checks if a milestone was reached and updates lastMilestone.
     * 
     * @param previousLines lines cleared before this clear
     * @param currentLines lines cleared after this clear
     * @return true if a new milestone was reached
     */
    private boolean checkMilestone(int previousLines, int currentLines) {
        int[] milestones = {10, 20, 30, 40};
        for (int milestone : milestones) {
            if (previousLines < milestone && currentLines >= milestone) {
                lastMilestone = milestone;
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the milestone message for the last reached milestone.
     * 
     * @return the milestone message (e.g., "10 Lines Cleared!"), or null if no new milestone
     */
    public String getMilestoneMessage() {
        if (lastMilestone > 0) {
            return lastMilestone + " Lines Cleared!";
        }
        return null;
    }
    
    /**
     * Gets the current number of lines cleared.
     * 
     * @return the lines cleared so far
     */
    public int getLinesCleared() {
        return linesCleared;
    }
    
    /**
     * Gets the target number of lines to clear.
     * 
     * @return the target lines
     */
    public int getTargetLines() {
        return targetLines;
    }
    
    /**
     * Checks if the Rush-40 goal has been completed.
     * 
     * @return true if target lines have been reached
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * Gets the completion time in seconds.
     * 
     * @return completion time in seconds, or -1.0 if not completed
     */
    public double getCompletionTimeSeconds() {
        if (!completed || startNanos == 0L || endNanos == 0L) {
            return -1.0;
        }
        long durationNanos = endNanos - startNanos;
        return durationNanos / 1_000_000_000.0;
    }
    
    /**
     * Resets Rush-40 state for a new game.
     */
    public void reset() {
        linesCleared = 0;
        lastMilestone = 0;
        completed = false;
        endNanos = 0L;
        startNanos = 0L;
    }
}

