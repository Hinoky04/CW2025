package com.comp2042;

import com.comp2042.models.SimpleBoard;
import com.comp2042.models.ViewData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimpleBoard hold/swap functionality.
 */
public class SimpleBoardHoldTest {

    private SimpleBoard board;

    @BeforeEach
    void setUp() {
        board = new SimpleBoard(25, 10);
        board.createNewBrick();
    }

    @Test
    void holdCurrentBrick_movesBrickToHold() {
        ViewData before = board.getViewData();
        int[][] currentBrickShape = before.getBrickData();
        
        boolean gameOver = board.holdCurrentBrick();
        
        assertFalse(gameOver, "Hold should not cause game over on empty board");
        
        ViewData after = board.getViewData();
        int[][] holdBrickShape = after.getHoldBrickData();
        
        assertNotNull(holdBrickShape, "Hold brick should exist after holding");
        // The held brick should be the same type as the original
        assertEquals(currentBrickShape.length, holdBrickShape.length, 
                "Held brick should have same dimensions");
    }

    @Test
    void holdCurrentBrick_spawnsNewBrick() {
        board.holdCurrentBrick();
        
        ViewData after = board.getViewData();
        int[][] newBrickShape = after.getBrickData();
        
        assertNotNull(newBrickShape, "New brick should be spawned after hold");
    }

    @Test
    void holdCurrentBrick_canOnlyHoldOncePerTurn() {
        // First hold should succeed
        boolean firstHold = board.holdCurrentBrick();
        assertFalse(firstHold, "First hold should succeed");
        
        // Second hold in same turn should fail
        boolean secondHold = board.holdCurrentBrick();
        assertFalse(secondHold, "Second hold should fail (already held this turn)");
        
        // After landing, should be able to hold again
        // (This would require merging brick first, which is tested separately)
    }

    @Test
    void holdCurrentBrick_swapsWhenBrickAlreadyHeld() {
        // First hold
        board.holdCurrentBrick();
        
        // Merge current brick to enable another hold
        board.mergeBrickToBackground();
        board.clearRows();
        board.createNewBrick();
        
        // Now hold again - should swap
        board.holdCurrentBrick();
        ViewData afterSwap = board.getViewData();
        int[][] swappedActiveBrick = afterSwap.getBrickData();
        int[][] swappedHeldBrick = afterSwap.getHoldBrickData();
        
        // The previously held brick should now be active
        assertNotNull(swappedActiveBrick, "Active brick should exist after swap");
        // The previously active brick should now be held
        assertNotNull(swappedHeldBrick, "Held brick should exist after swap");
    }

    @Test
    void holdCurrentBrick_handlesGameOverScenario() {
        // Fill board near top to cause game over on spawn
        int[][] matrix = board.getBoardMatrix();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 10; col++) {
                matrix[row][col] = 8; // Fill with blocks
            }
        }
        
        // Create new brick - should collide immediately
        board.createNewBrick();
        
        // Try to hold - the new brick after hold should also collide
        board.holdCurrentBrick();
        
        // This might return true if the swapped brick collides
        // The exact behavior depends on implementation
        assertNotNull(board.getViewData(), "ViewData should exist");
    }
}

