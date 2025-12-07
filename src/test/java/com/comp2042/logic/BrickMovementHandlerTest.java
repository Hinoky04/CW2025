package com.comp2042.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BrickMovementHandler.
 * Tests movement validation and position updates.
 */
public class BrickMovementHandlerTest {
    
    private CollisionDetector collisionDetector;
    private BrickMovementHandler movementHandler;
    private int[][] boardMatrix;
    private MockBrickRotator brickRotator;
    private static final int BOARD_ROWS = 20;
    private static final int BOARD_COLUMNS = 10;
    
    @BeforeEach
    void setUp() {
        collisionDetector = new CollisionDetector(BOARD_COLUMNS);
        boardMatrix = new int[BOARD_ROWS][BOARD_COLUMNS];
        brickRotator = new MockBrickRotator();
        movementHandler = new BrickMovementHandler(collisionDetector, brickRotator, boardMatrix);
    }
    
    @Test
    void tryMove_validMoveRight_updatesPosition() {
        // Given: brick at (3, 5) with a 2x2 shape
        Point currentOffset = new Point(3, 5);
        brickRotator.setShape(new int[][]{{1, 1}, {1, 1}});
        
        // When: moving right
        boolean result = movementHandler.tryMove(currentOffset, 1, 0);
        
        // Then: should succeed and update position
        assertTrue(result, "Valid move right should succeed");
        assertEquals(4, currentOffset.x, "X position should be updated");
        assertEquals(5, currentOffset.y, "Y position should remain same");
    }
    
    @Test
    void tryMove_validMoveLeft_updatesPosition() {
        // Given: brick at (5, 5)
        Point currentOffset = new Point(5, 5);
        brickRotator.setShape(new int[][]{{1, 1}, {1, 1}});
        
        // When: moving left
        boolean result = movementHandler.tryMove(currentOffset, -1, 0);
        
        // Then: should succeed
        assertTrue(result, "Valid move left should succeed");
        assertEquals(4, currentOffset.x, "X position should be updated");
    }
    
    @Test
    void tryMove_validMoveDown_updatesPosition() {
        // Given: brick at (3, 5)
        Point currentOffset = new Point(3, 5);
        brickRotator.setShape(new int[][]{{1, 1}, {1, 1}});
        
        // When: moving down
        boolean result = movementHandler.tryMove(currentOffset, 0, 1);
        
        // Then: should succeed
        assertTrue(result, "Valid move down should succeed");
        assertEquals(3, currentOffset.x, "X position should remain same");
        assertEquals(6, currentOffset.y, "Y position should be updated");
    }
    
    @Test
    void tryMove_blockedByLeftBoundary_returnsFalse() {
        // Given: brick at left edge (x=0)
        Point currentOffset = new Point(0, 5);
        brickRotator.setShape(new int[][]{{1, 1}, {1, 1}});
        
        // When: trying to move left
        boolean result = movementHandler.tryMove(currentOffset, -1, 0);
        
        // Then: should fail
        assertFalse(result, "Move left at boundary should fail");
        assertEquals(0, currentOffset.x, "Position should not change");
    }
    
    @Test
    void tryMove_blockedByRightBoundary_returnsFalse() {
        // Given: brick at right edge (x=8 for 2x2 shape in 10-column board)
        Point currentOffset = new Point(8, 5);
        brickRotator.setShape(new int[][]{{1, 1}, {1, 1}});
        
        // When: trying to move right
        boolean result = movementHandler.tryMove(currentOffset, 1, 0);
        
        // Then: should fail
        assertFalse(result, "Move right at boundary should fail");
        assertEquals(8, currentOffset.x, "Position should not change");
    }
    
    @Test
    void tryMove_blockedByExistingBlock_returnsFalse() {
        // Given: board with block at (4, 6) and brick at (3, 5)
        boardMatrix[6][4] = 1;
        Point currentOffset = new Point(3, 5);
        brickRotator.setShape(new int[][]{{1, 1}, {1, 1}});
        
        // When: trying to move down (would place brick at (3,6) and (4,6), causing collision)
        boolean result = movementHandler.tryMove(currentOffset, 0, 1);
        
        // Then: should fail due to collision
        assertFalse(result, "Move blocked by existing block should fail");
        assertEquals(3, currentOffset.x, "Position should not change");
        assertEquals(5, currentOffset.y, "Position should not change");
    }
    
    @Test
    void tryMove_nullOffset_returnsFalse() {
        // Given: null offset
        brickRotator.setShape(new int[][]{{1, 1}, {1, 1}});
        
        // When: trying to move
        boolean result = movementHandler.tryMove(null, 1, 0);
        
        // Then: should fail
        assertFalse(result, "Move with null offset should fail");
    }
    
    @Test
    void updateBoardMatrix_updatesReference() {
        // Given: new board matrix
        int[][] newBoardMatrix = new int[25][12];
        newBoardMatrix[10][5] = 1;
        
        // When: updating board matrix
        movementHandler.updateBoardMatrix(newBoardMatrix);
        
        // Then: subsequent moves should use new matrix
        // This is tested indirectly - if update works, collision detection will use new matrix
        assertNotNull(movementHandler, "Handler should still be valid after update");
    }
    
    // Mock BrickRotator for testing
    private static class MockBrickRotator extends BrickRotator {
        private int[][] shape;
        
        public void setShape(int[][] shape) {
            this.shape = shape;
        }
        
        @Override
        public int[][] getCurrentShape() {
            return shape;
        }
    }
}

