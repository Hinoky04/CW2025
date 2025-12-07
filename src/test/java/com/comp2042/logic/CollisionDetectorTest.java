package com.comp2042.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CollisionDetector.
 * Tests boundary checking and collision detection logic.
 */
public class CollisionDetectorTest {
    
    private CollisionDetector detector;
    private static final int BOARD_COLUMNS = 10;
    
    @BeforeEach
    void setUp() {
        detector = new CollisionDetector(BOARD_COLUMNS);
    }
    
    @Test
    void isWithinHorizontalBounds_shapeFullyInsideBounds_returnsTrue() {
        // Given: a 2x2 shape at position x=3 (columns 3-4)
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = 3;
        
        // When: checking bounds
        boolean result = detector.isWithinHorizontalBounds(shape, offsetX);
        
        // Then: should be within bounds
        assertTrue(result, "Shape at x=3 should be within bounds for 10-column board");
    }
    
    @Test
    void isWithinHorizontalBounds_shapeAtLeftEdge_returnsTrue() {
        // Given: a 2x2 shape at position x=0 (columns 0-1)
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = 0;
        
        // When: checking bounds
        boolean result = detector.isWithinHorizontalBounds(shape, offsetX);
        
        // Then: should be within bounds
        assertTrue(result, "Shape at x=0 should be within bounds");
    }
    
    @Test
    void isWithinHorizontalBounds_shapeAtRightEdge_returnsTrue() {
        // Given: a 2x2 shape at position x=8 (columns 8-9, last two columns)
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = 8;
        
        // When: checking bounds
        boolean result = detector.isWithinHorizontalBounds(shape, offsetX);
        
        // Then: should be within bounds
        assertTrue(result, "Shape at x=8 should be within bounds for 10-column board");
    }
    
    @Test
    void isWithinHorizontalBounds_shapeExtendsLeft_returnsFalse() {
        // Given: a 2x2 shape at position x=-1 (columns -1 to 0)
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = -1;
        
        // When: checking bounds
        boolean result = detector.isWithinHorizontalBounds(shape, offsetX);
        
        // Then: should be out of bounds
        assertFalse(result, "Shape extending left should be out of bounds");
    }
    
    @Test
    void isWithinHorizontalBounds_shapeExtendsRight_returnsFalse() {
        // Given: a 2x2 shape at position x=9 (columns 9-10, extends beyond)
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = 9;
        
        // When: checking bounds
        boolean result = detector.isWithinHorizontalBounds(shape, offsetX);
        
        // Then: should be out of bounds
        assertFalse(result, "Shape extending right should be out of bounds");
    }
    
    @Test
    void isWithinHorizontalBounds_emptyShape_returnsTrue() {
        // Given: an empty shape
        int[][] shape = {{0, 0}, {0, 0}};
        int offsetX = 5;
        
        // When: checking bounds
        boolean result = detector.isWithinHorizontalBounds(shape, offsetX);
        
        // Then: should be treated as in bounds
        assertTrue(result, "Empty shape should be treated as in bounds");
    }
    
    @Test
    void isWithinHorizontalBounds_irregularShape_returnsCorrect() {
        // Given: an L-shaped brick (3x3 with only some cells filled)
        int[][] shape = {
            {1, 0, 0},
            {1, 1, 1},
            {0, 0, 0}
        };
        int offsetX = 5;
        
        // When: checking bounds
        boolean result = detector.isWithinHorizontalBounds(shape, offsetX);
        
        // Then: should be within bounds (columns 5-7)
        assertTrue(result, "L-shaped brick at x=5 should be within bounds");
    }
    
    @Test
    void hasCollision_noCollision_returnsFalse() {
        // Given: empty board and a shape
        int[][] boardMatrix = new int[20][BOARD_COLUMNS];
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = 3;
        int offsetY = 5;
        
        // When: checking collision
        boolean result = detector.hasCollision(boardMatrix, shape, offsetX, offsetY);
        
        // Then: should be no collision
        assertFalse(result, "Empty board should have no collision");
    }
    
    @Test
    void hasCollision_collisionWithExistingBlock_returnsTrue() {
        // Given: board with a block at (5, 5) and shape that would overlap
        int[][] boardMatrix = new int[20][BOARD_COLUMNS];
        boardMatrix[5][5] = 1;
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = 4;
        int offsetY = 4;
        
        // When: checking collision (shape at (4,4) would place cells at (4,4), (4,5), (5,4), (5,5))
        boolean result = detector.hasCollision(boardMatrix, shape, offsetX, offsetY);
        
        // Then: should detect collision
        assertTrue(result, "Should detect collision with existing block");
    }
    
    @Test
    void hasCollision_shapeAtBottom_returnsFalse() {
        // Given: shape at bottom of board (no blocks below)
        int[][] boardMatrix = new int[20][BOARD_COLUMNS];
        int[][] shape = {{1, 1}, {1, 1}};
        int offsetX = 3;
        int offsetY = 18; // Near bottom
        
        // When: checking collision
        boolean result = detector.hasCollision(boardMatrix, shape, offsetX, offsetY);
        
        // Then: should be no collision if within bounds
        assertFalse(result, "Shape at bottom with no blocks should have no collision");
    }
}

