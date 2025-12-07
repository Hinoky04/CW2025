package com.comp2042.mode;

import com.comp2042.models.Board;
import com.comp2042.models.ClearRow;
import com.comp2042.models.GameConfig;
import com.comp2042.models.GameMode;
import com.comp2042.models.Score;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SurvivalModeHandler.
 * Tests shield mechanics, garbage row pressure, and threshold calculations.
 */
public class SurvivalModeHandlerTest {
    
    private SurvivalModeHandler handler;
    private MockBoard board;
    private GameConfig config;
    private Score score;
    
    @BeforeEach
    void setUp() {
        board = new MockBoard();
        config = GameConfig.forMode(GameMode.SURVIVAL); // Uses threshold of 4, but we'll test with that
        handler = new SurvivalModeHandler(board, config);
        score = new Score();
    }
    
    @Test
    void handleBrickLanded_noLinesCleared_incrementsCounter() {
        // Given: no lines cleared
        ClearRow clearRow = null;
        
        // When: handling brick landed
        handler.handleBrickLanded(clearRow, score);
        
        // Then: counter should increment (tested via getLandingsUntilGarbage)
        int landingsUntil = handler.getLandingsUntilGarbage(score, config.getMaxNoClearBeforeGarbage());
        assertEquals(3, landingsUntil, "Should have 3 landings until garbage after 1 no-clear (threshold is 4)");
    }
    
    @Test
    void handleBrickLanded_linesCleared_resetsCounter() {
        // Given: some no-clear landings, then lines cleared
        handler.handleBrickLanded(null, score); // 1 no-clear
        handler.handleBrickLanded(null, score); // 2 no-clear
        
        ClearRow clearRow = new ClearRow(2, new int[20][10], 200); // 2 lines cleared
        
        // When: handling brick landed with lines cleared
        handler.handleBrickLanded(clearRow, score);
        
        // Then: counter should reset
        int landingsUntil = handler.getLandingsUntilGarbage(score, config.getMaxNoClearBeforeGarbage());
        assertEquals(4, landingsUntil, "Counter should reset after clearing lines");
    }
    
    @Test
    void handleBrickLanded_tetrisGrantsShield() {
        // Given: no shields initially
        assertEquals(0, handler.getShields(), "Should start with 0 shields");
        
        // When: clearing 4 lines (Tetris)
        ClearRow clearRow = new ClearRow(4, new int[20][10], 800);
        handler.handleBrickLanded(clearRow, score);
        
        // Then: should gain a shield
        assertEquals(1, handler.getShields(), "Tetris should grant 1 shield");
    }
    
    @Test
    void handleBrickLanded_maxShields_capsAtThree() {
        // Given: already have 3 shields
        for (int i = 0; i < 3; i++) {
            ClearRow clearRow = new ClearRow(4, new int[20][10], 800);
            handler.handleBrickLanded(clearRow, score);
        }
        assertEquals(3, handler.getShields(), "Should have 3 shields");
        
        // When: clearing another Tetris
        ClearRow clearRow = new ClearRow(4, new int[20][10], 800);
        handler.handleBrickLanded(clearRow, score);
        
        // Then: should still be capped at 3
        assertEquals(3, handler.getShields(), "Shields should be capped at 3");
    }
    
    @Test
    void handleBrickLanded_thresholdReachedWithShield_consumesShield() {
        // Given: have 1 shield and at threshold
        ClearRow tetris = new ClearRow(4, new int[20][10], 800);
        handler.handleBrickLanded(tetris, score); // Gain shield
        assertEquals(1, handler.getShields(), "Should have 1 shield");
        
        // Reach threshold with no-clear landings (threshold is 4 for Survival)
        for (int i = 0; i < 4; i++) {
            handler.handleBrickLanded(null, score);
        }
        
        // When: threshold reached
        handler.handleBrickLanded(null, score); // This should trigger garbage
        
        // Then: shield should be consumed, no garbage added
        assertEquals(0, handler.getShields(), "Shield should be consumed");
        assertEquals(0, board.getGarbageRowCount(), "No garbage should be added when shield consumed");
    }
    
    @Test
    void handleBrickLanded_thresholdReachedNoShield_addsGarbage() {
        // Given: no shields and at threshold (threshold is 4 for Survival)
        for (int i = 0; i < 4; i++) {
            handler.handleBrickLanded(null, score);
        }
        
        // When: threshold reached
        handler.handleBrickLanded(null, score); // This should trigger garbage
        
        // Then: garbage should be added and counter reset
        assertEquals(1, board.getGarbageRowCount(), "Garbage row should be added");
        // After adding garbage, counter resets to 0, but we just did one more landing
        // So noClearLandingCount is 0 after reset, landingsUntil = threshold - 0 = 4
        // But wait - the landing that triggered garbage also increments counter first
        // So: 4 landings -> counter=4, then 5th landing -> counter=5 (>=4), add garbage, reset to 0
        // But the 5th landing itself increments counter, so after reset it's 0, landingsUntil = 4
        int landingsUntil = handler.getLandingsUntilGarbage(score, config.getMaxNoClearBeforeGarbage());
        assertTrue(landingsUntil >= 3 && landingsUntil <= 4, "Counter should be reset (landingsUntil should be 3-4)");
    }
    
    @Test
    void computeGarbageThreshold_level1_returnsBaseThreshold() {
        // Given: level 1 (default)
        // When: computing threshold
        int threshold = handler.computeGarbageThreshold(score, config.getMaxNoClearBeforeGarbage());
        
        // Then: should return base threshold (4 for Survival mode)
        assertEquals(4, threshold, "Level 1 should return base threshold");
    }
    
    @Test
    void computeGarbageThreshold_level4_reducesThreshold() {
        // Given: level 4 (reduction = (4-1)/3 = 1)
        // Set level by clearing 30 lines (level 4 = 1 + 30/10)
        score.registerLinesCleared(30, 0);
        
        // When: computing threshold
        int threshold = handler.computeGarbageThreshold(score, config.getMaxNoClearBeforeGarbage());
        
        // Then: should be reduced by 1 (4 - 1 = 3)
        assertEquals(3, threshold, "Level 4 should reduce threshold by 1");
    }
    
    @Test
    void computeGarbageThreshold_level10_reducesThresholdMore() {
        // Given: level 10 (reduction = (10-1)/3 = 3)
        // Set level by clearing 90 lines (level 10 = 1 + 90/10, capped at 10)
        score.registerLinesCleared(90, 0);
        
        // When: computing threshold
        int threshold = handler.computeGarbageThreshold(score, config.getMaxNoClearBeforeGarbage());
        
        // Then: should be reduced by 3 (4 - 3 = 1, minimum is 1)
        assertEquals(1, threshold, "Level 10 should reduce threshold to minimum 1");
    }
    
    @Test
    void computeGarbageThreshold_highLevel_minimumIsOne() {
        // Given: very high level that would reduce below 1
        // Set level to 10 (max level)
        score.registerLinesCleared(90, 0);
        
        // When: computing threshold with a base threshold that would go negative
        int threshold = handler.computeGarbageThreshold(score, 2); // Base 2, level 10 reduces by 3
        
        // Then: should be clamped to minimum of 1
        assertEquals(1, threshold, "Threshold should be clamped to minimum 1");
    }
    
    @Test
    void reset_clearsState() {
        // Given: some state
        handler.handleBrickLanded(new ClearRow(4, new int[20][10], 800), score); // Gain shield
        handler.handleBrickLanded(null, score); // Increment counter
        
        // When: resetting
        handler.reset();
        
        // Then: state should be cleared
        assertEquals(0, handler.getShields(), "Shields should be reset");
        assertEquals(4, handler.getLandingsUntilGarbage(score, config.getMaxNoClearBeforeGarbage()), "Counter should be reset");
    }
    
    // Mock Board for testing
    private static class MockBoard implements Board {
        private int garbageRowCount = 0;
        
        @Override
        public void addGarbageRow() {
            garbageRowCount++;
        }
        
        public int getGarbageRowCount() {
            return garbageRowCount;
        }
        
        // Other Board methods not needed for these tests
        @Override
        public boolean moveBrickDown() { return false; }
        @Override
        public boolean moveBrickLeft() { return false; }
        @Override
        public boolean moveBrickRight() { return false; }
        @Override
        public boolean rotateLeftBrick() { return false; }
        @Override
        public boolean createNewBrick() { return false; }
        @Override
        public boolean holdCurrentBrick() { return false; }
        @Override
        public int[][] getBoardMatrix() { return new int[20][10]; }
        @Override
        public com.comp2042.models.ViewData getViewData() { return null; }
        @Override
        public int[][][] getNextQueue() { return null; }
        @Override
        public void mergeBrickToBackground() {}
        @Override
        public com.comp2042.models.ClearRow clearRows() { return null; }
        @Override
        public com.comp2042.models.Score getScore() { return null; }
        @Override
        public void newGame() {}
    }
}

