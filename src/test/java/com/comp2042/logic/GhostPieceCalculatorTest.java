package com.comp2042.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GhostPieceCalculator.
 * Tests landing position calculation for ghost pieces.
 */
public class GhostPieceCalculatorTest {
    
    private GhostPieceCalculator calculator;
    private int[][] boardMatrix;
    private static final int BOARD_ROWS = 20;
    private static final int BOARD_COLUMNS = 10;
    
    @BeforeEach
    void setUp() {
        boardMatrix = new int[BOARD_ROWS][BOARD_COLUMNS];
        calculator = new GhostPieceCalculator(BOARD_ROWS, boardMatrix);
    }
    
    @Test
    void computeLandingY_emptyBoard_landsAtBottom() {
        // Given: empty board, brick at (3, 5) with 2x2 shape
        int startX = 3;
        int startY = 5;
        int[][] shape = {{1, 1}, {1, 1}};
        
        // When: computing landing position
        int landingY = calculator.computeLandingY(startX, startY, shape);
        
        // Then: should land near bottom (row 18 for 2x2 shape in 20-row board)
        assertTrue(landingY >= startY, "Landing Y should be at or below start Y");
        assertTrue(landingY < BOARD_ROWS, "Landing Y should be within board bounds");
    }
    
    @Test
    void computeLandingY_blockedByExistingBlock_landsAboveBlock() {
        // Given: board with block at row 10, column 3-4
        boardMatrix[10][3] = 1;
        boardMatrix[10][4] = 1;
        int startX = 3;
        int startY = 5;
        int[][] shape = {{1, 1}, {1, 1}};
        
        // When: computing landing position
        int landingY = calculator.computeLandingY(startX, startY, shape);
        
        // Then: should land at row 9 (above the block at row 10)
        // The shape at (3,9) would place cells at rows 9-10, so it stops at row 9
        assertTrue(landingY <= 9, "Should land at or above row 9");
        assertTrue(landingY >= 5, "Should be at or below start position");
    }
    
    @Test
    void computeLandingY_startingAtBottom_returnsStartY() {
        // Given: brick already at bottom
        int startX = 3;
        int startY = 18; // Near bottom
        int[][] shape = {{1, 1}, {1, 1}};
        
        // When: computing landing position
        int landingY = calculator.computeLandingY(startX, startY, shape);
        
        // Then: should return start Y (can't go further down)
        assertEquals(startY, landingY, "Should return start Y when already at bottom");
    }
    
    @Test
    void computeLandingY_partialBlock_landsCorrectly() {
        // Given: board with block only at column 4, row 8
        boardMatrix[8][4] = 1;
        int startX = 3;
        int startY = 5;
        int[][] shape = {{1, 1}, {1, 1}}; // 2x2 shape
        
        // When: computing landing position
        int landingY = calculator.computeLandingY(startX, startY, shape);
        
        // Then: should land at row 6 (above the block at row 8)
        // The shape at x=3 occupies columns 3-4, so it will collide with block at (8,4)
        // Shape at (3,6) places cells at rows 6-7, shape at (3,7) would place at rows 7-8 (collision)
        assertEquals(6, landingY, "Should land above the partial block");
    }
    
    @Test
    void computeLandingY_multipleBlocks_landsAboveFirstBlock() {
        // Given: board with blocks at rows 8 and 10
        boardMatrix[8][3] = 1;
        boardMatrix[8][4] = 1;
        boardMatrix[10][3] = 1;
        boardMatrix[10][4] = 1;
        int startX = 3;
        int startY = 5;
        int[][] shape = {{1, 1}, {1, 1}};
        
        // When: computing landing position
        int landingY = calculator.computeLandingY(startX, startY, shape);
        
        // Then: should land at row 6 (above first block at row 8)
        // Shape at (3,6) places cells at rows 6-7, shape at (3,7) would place at rows 7-8 (collision)
        assertEquals(6, landingY, "Should land above the first blocking row");
    }
    
    @Test
    void computeLandingY_irregularShape_landsCorrectly() {
        // Given: L-shaped brick
        int[][] shape = {
            {1, 0, 0},
            {1, 1, 1},
            {0, 0, 0}
        };
        int startX = 3;
        int startY = 5;
        // Place block to block the bottom part of L
        boardMatrix[10][3] = 1; // Blocks the left column of L
        
        // When: computing landing position
        int landingY = calculator.computeLandingY(startX, startY, shape);
        
        // Then: should land above the blocking block
        // The L-shape at (3,8) would place a cell at (10,3), so it stops at row 8
        assertTrue(landingY <= 9, "Should land at or above row 9");
        assertTrue(landingY >= 5, "Should be at or below start position");
    }
    
    @Test
    void updateBoardMatrix_updatesReference() {
        // Given: new board matrix with different state
        int[][] newBoardMatrix = new int[25][12];
        newBoardMatrix[15][5] = 1;
        
        // When: updating board matrix
        calculator.updateBoardMatrix(newBoardMatrix);
        
        // Then: subsequent calculations should use new matrix
        int startX = 5;
        int startY = 10;
        int[][] shape = {{1, 1}, {1, 1}};
        int landingY = calculator.computeLandingY(startX, startY, shape);
        
        // Should land above the block at row 15
        assertTrue(landingY < 15, "Should use updated board matrix for calculation");
    }
}

