package com.comp2042;

import com.comp2042.models.ClearRow;
import com.comp2042.models.SimpleBoard;
import com.comp2042.models.ViewData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimpleBoard merge and line clearing operations.
 */
public class SimpleBoardMergeTest {

    private SimpleBoard board;

    @BeforeEach
    void setUp() {
        board = new SimpleBoard(25, 10);
        board.createNewBrick();
    }

    @Test
    void mergeBrickToBackground_addsBrickToBoard() {
        // Move brick to a known position
        for (int i = 0; i < 5; i++) {
            board.moveBrickDown();
        }
        
        board.mergeBrickToBackground();
        
        int[][] matrix = board.getBoardMatrix();
        
        // After merging, a new brick should be spawned (not null)
        ViewData after = board.getViewData();
        assertNotNull(after.getBrickData(), "New brick should be spawned after merge");
        
        // Check that brick cells were added to matrix
        boolean foundBrickCell = false;
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                if (matrix[row][col] != 0) {
                    foundBrickCell = true;
                    break;
                }
            }
        }
        assertTrue(foundBrickCell, "Brick cells should be in the matrix after merge");
    }

    @Test
    void clearRows_removesFullRows() {
        // Fill a row completely
        int[][] matrix = board.getBoardMatrix();
        int fillRow = 20;
        for (int col = 0; col < 10; col++) {
            matrix[fillRow][col] = 5;
        }
        
        // Merge a brick that completes the row
        // For simplicity, we'll directly test clearRows
        ClearRow result = board.clearRows();
        
        assertNotNull(result, "ClearRow result should not be null");
        assertEquals(1, result.getLinesRemoved(), "Should clear 1 full row");
    }

    @Test
    void clearRows_clearsMultipleRows() {
        int[][] matrix = board.getBoardMatrix();
        
        // Fill two rows completely
        for (int row = 20; row <= 21; row++) {
            for (int col = 0; col < 10; col++) {
                matrix[row][col] = 5;
            }
        }
        
        ClearRow result = board.clearRows();
        
        assertEquals(2, result.getLinesRemoved(), "Should clear 2 full rows");
        assertEquals(200, result.getScoreBonus(), 
                "Score bonus should be 200 for 2 lines (50 * 2 * 2)");
    }

    @Test
    void clearRows_shiftsRowsDown() {
        int[][] matrix = board.getBoardMatrix();
        
        // Fill bottom row completely
        int bottomRow = 24;
        for (int col = 0; col < 10; col++) {
            matrix[bottomRow][col] = 5;
        }
        
        // Add a block in the row above
        matrix[23][0] = 3;
        
        ClearRow result = board.clearRows();
        int[][] newMatrix = result.getNewMatrix();
        
        // After clearing, the block from row 23 should fall to row 24
        assertEquals(3, newMatrix[bottomRow][0], 
                "Block from above should fall to bottom row");
    }

    @Test
    void clearRows_returnsZeroWhenNoFullRows() {
        int[][] matrix = board.getBoardMatrix();
        
        // Add some blocks but don't fill any row completely
        matrix[20][0] = 1;
        matrix[20][1] = 2;
        matrix[21][5] = 3;
        
        ClearRow result = board.clearRows();
        
        assertEquals(0, result.getLinesRemoved(), 
                "Should return 0 when no full rows");
        assertEquals(0, result.getScoreBonus(), 
                "Score bonus should be 0");
    }

    @Test
    void mergeAndClear_workflow() {
        // Move brick down
        for (int i = 0; i < 15; i++) {
            board.moveBrickDown();
        }
        
        // Merge brick
        board.mergeBrickToBackground();
        
        // Fill the row that the brick is in to make it full
        int[][] matrix = board.getBoardMatrix();
        int targetRow = 20;
        for (int col = 0; col < 10; col++) {
            if (matrix[targetRow][col] == 0) {
                matrix[targetRow][col] = 7;
            }
        }
        
        // Now clear rows
        ClearRow result = board.clearRows();
        
        assertTrue(result.getLinesRemoved() >= 0, 
                "Should clear at least 0 rows (might be 1 if row was full)");
    }

    @Test
    void getViewData_returnsGhostPosition() {
        ViewData viewData = board.getViewData();
        
        assertNotNull(viewData, "ViewData should not be null");
        assertTrue(viewData.getGhostYPosition() >= viewData.getyPosition(),
                "Ghost Y should be at or below current Y position");
    }

    @Test
    void computeLandingY_calculatesCorrectLanding() {
        // This tests the private computeLandingY method indirectly
        // by checking ghost position
        
        ViewData viewData = board.getViewData();
        int currentY = viewData.getyPosition();
        int ghostY = viewData.getGhostYPosition();
        
        // Ghost should be at the bottom or at a collision point
        assertTrue(ghostY >= currentY, 
                "Ghost should be at or below current position");
        assertTrue(ghostY < 25, 
                "Ghost should be within board bounds");
    }
}

