package com.comp2042.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ClearRow class.
 * Verifies data integrity and defensive copying.
 */
class ClearRowTest {

    @Test
    void testConstructorAndGetters() {
        int[][] matrix = new int[][]{{0, 0}, {0, 0}};
        ClearRow clearRow = new ClearRow(2, matrix, 200);
        
        assertEquals(2, clearRow.getLinesRemoved());
        assertEquals(200, clearRow.getScoreBonus());
        assertNotNull(clearRow.getNewMatrix());
    }

    @Test
    void testGetNewMatrixReturnsDefensiveCopy() {
        int[][] originalMatrix = new int[][]{{1, 2}, {3, 4}};
        ClearRow clearRow = new ClearRow(1, originalMatrix, 50);
        
        int[][] returnedMatrix = clearRow.getNewMatrix();
        
        // Modify the returned matrix
        returnedMatrix[0][0] = 999;
        
        // Original should not be affected
        assertEquals(1, originalMatrix[0][0], "Original matrix should not be modified");
        assertEquals(999, returnedMatrix[0][0], "Returned matrix should be modifiable");
    }

    @Test
    void testZeroLinesRemoved() {
        int[][] matrix = new int[][]{{0, 0}, {0, 0}};
        ClearRow clearRow = new ClearRow(0, matrix, 0);
        
        assertEquals(0, clearRow.getLinesRemoved());
        assertEquals(0, clearRow.getScoreBonus());
    }

    @Test
    void testMultipleLinesRemoved() {
        int[][] matrix = new int[][]{{0, 0}, {0, 0}};
        ClearRow clearRow = new ClearRow(4, matrix, 800);
        
        assertEquals(4, clearRow.getLinesRemoved());
        assertEquals(800, clearRow.getScoreBonus());
    }

    @Test
    void testLargeScoreBonus() {
        int[][] matrix = new int[][]{{0, 0}, {0, 0}};
        ClearRow clearRow = new ClearRow(4, matrix, 10000);
        
        assertEquals(10000, clearRow.getScoreBonus());
    }

    @Test
    void testMatrixDimensionsPreserved() {
        int[][] originalMatrix = new int[][]{
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        ClearRow clearRow = new ClearRow(1, originalMatrix, 50);
        
        int[][] returnedMatrix = clearRow.getNewMatrix();
        
        assertEquals(originalMatrix.length, returnedMatrix.length);
        assertEquals(originalMatrix[0].length, returnedMatrix[0].length);
    }
}
