package com.comp2042.mode;

import com.comp2042.models.Board;
import com.comp2042.models.ClearRow;
import com.comp2042.models.GameConfig;
import com.comp2042.models.Score;

/**
 * Handles Survival mode-specific logic including garbage row pressure and shields.
 * Survival mode adds garbage rows when the player fails to clear lines,
 * and grants shields when clearing 4 lines at once (Tetris).
 */
public class SurvivalModeHandler {
    
    private static final int MAX_SHIELDS = 3;
    
    private final Board board;
    private final GameConfig config;
    
    // Survival-mode state
    private int noClearLandingCount = 0;
    private int shields = 0;
    
    /**
     * Creates a Survival mode handler.
     * 
     * @param board the game board
     * @param config the game configuration
     */
    public SurvivalModeHandler(Board board, GameConfig config) {
        this.board = board;
        this.config = config;
    }
    
    /**
     * Handles Survival mode effects after a brick lands.
     * Updates shield count, garbage pressure, and adds garbage rows if needed.
     * 
     * @param clearRow the result of line clearing (null if no lines cleared)
     * @param score the score object for level-based threshold calculation
     */
    public void handleBrickLanded(ClearRow clearRow, Score score) {
        int baseThreshold = config.getMaxNoClearBeforeGarbage();
        if (baseThreshold <= 0) {
            return;
        }

        int linesRemoved = (clearRow != null) ? clearRow.getLinesRemoved() : 0;

        // Gain a shield on Tetris (4 lines cleared at once).
        if (linesRemoved >= 4) {
            shields++;
            if (shields > MAX_SHIELDS) {
                shields = MAX_SHIELDS;
            }
        }

        int threshold = computeGarbageThreshold(score, baseThreshold);

        if (linesRemoved > 0) {
            noClearLandingCount = 0;
        } else {
            noClearLandingCount++;
        }

        if (noClearLandingCount >= threshold) {
            if (shields > 0) {
                shields--;
            } else {
                board.addGarbageRow();
            }
            noClearLandingCount = 0;
        }
    }
    
    /**
     * Computes the current garbage threshold based on the base config and player's level.
     * Higher levels reduce the threshold, making garbage appear faster.
     * 
     * @param score the score object to get the current level
     * @param baseThreshold the base threshold from config
     * @return the adjusted threshold (minimum 1)
     */
    public int computeGarbageThreshold(Score score, int baseThreshold) {
        int level = score.getLevel();
        int reduction = (level - 1) / 3;
        int threshold = baseThreshold - reduction;

        if (threshold < 1) {
            threshold = 1;
        }
        if (threshold > baseThreshold) {
            threshold = baseThreshold;
        }
        return threshold;
    }
    
    /**
     * Calculates how many landings remain until the next garbage row.
     * 
     * @param score the score object for threshold calculation
     * @param baseThreshold the base threshold from config
     * @return the number of landings until garbage (0 if already at threshold)
     */
    public int getLandingsUntilGarbage(Score score, int baseThreshold) {
        int threshold = computeGarbageThreshold(score, baseThreshold);
        int landingsUntilGarbage = threshold - noClearLandingCount;
        if (landingsUntilGarbage < 0) {
            landingsUntilGarbage = 0;
        }
        return landingsUntilGarbage;
    }
    
    /**
     * Gets the current number of shields.
     * 
     * @return the shield count (0 to MAX_SHIELDS)
     */
    public int getShields() {
        return shields;
    }
    
    /**
     * Resets Survival mode state for a new game.
     */
    public void reset() {
        noClearLandingCount = 0;
        shields = 0;
    }
}

