package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimpleBoard core logic.
 * Focus on line clearing, board reset, and scoring.
 */
public class SimpleBoardTest {

    /**
     * newGame() should clear the board and reset the score to zero.
     */
    @Test
    void newGame_clearsBoardAndResetsScore() {
        // Use a small board for easier reasoning
        SimpleBoard board = new SimpleBoard(4, 4);

        // Put some blocks on the board
        int[][] matrix = board.getBoardMatrix();
        matrix[0][0] = 1;
        matrix[1][1] = 2;

        // Give the player some score
        board.getScore().add(100);

        // When: starting a new game
        board.newGame();

        // Then: every cell on the board should be zero
        int[][] after = board.getBoardMatrix();
        for (int row = 0; row < after.length; row++) {
            for (int col = 0; col < after[row].length; col++) {
                assertEquals(0, after[row][col],
                        "Board should be cleared by newGame()");
            }
        }

        // And: score should be reset to zero
        int score = board.getScore().scoreProperty().get();
        assertEquals(0, score, "Score should be reset by newGame()");
    }

    /**
     * clearRows() should remove a full row and update the board matrix.
     */
    @Test
    void clearRows_removesSingleFullRow() {
        SimpleBoard board = new SimpleBoard(4, 4);
        int[][] matrix = board.getBoardMatrix();

        // Fill the bottom row completely (full line)
        int lastRow = matrix.length - 1;
        for (int col = 0; col < matrix[lastRow].length; col++) {
            matrix[lastRow][col] = 1;
        }

        // Add one block above so we can see that rows shift down
        matrix[lastRow - 1][0] = 2;

        // When: clearing rows
        ClearRow clearRow = board.clearRows();

        // Then: one line should be reported as removed
        assertNotNull(clearRow, "clearRows() should not return null");
        assertEquals(1, clearRow.getLinesRemoved(),
                "Exactly one full row should be removed");

        int[][] after = board.getBoardMatrix();

        // OLD assumption (kept as explanation): we first thought
        // the bottom row would become completely empty after clearing.
        // This expectation was wrong because rows above fall down.
        //
        // for (int col = 0; col < after[lastRow].length; col++) {
        //     assertEquals(0, after[lastRow][col],
        //             "Bottom row should be empty after clearing");
        // }

        // Correct behaviour: the block that was above the full row
        // falls down into the bottom row, other cells become empty.
        assertEquals(2, after[lastRow][0],
                "Block above cleared row should fall down to bottom row");

        for (int col = 1; col < after[lastRow].length; col++) {
            assertEquals(0, after[lastRow][col],
                    "Other cells in bottom row should be empty after clearing");
        }
    }

    /**
     * If there is no full row, clearRows() should report 0 lines removed.
     */
    /**
    * I first assumed that after clearing a full row the bottom row would be all zeros.
    * The initial assertion failed and showed that one block from the row above
    * correctly falls down into the bottom row. The commented-out assertion below
    * documents this wrong assumption; the active assertions check the correct behaviour.
    */
    @Test
    void clearRows_returnsZeroWhenNoFullRow() {
        SimpleBoard board = new SimpleBoard(4, 4);
        int[][] matrix = board.getBoardMatrix();

        // Put some blocks in the bottom row but do not fill it completely
        int lastRow = matrix.length - 1;
        matrix[lastRow][0] = 1;
        matrix[lastRow][1] = 1;
        // Row is not full because other cells are still zero

        // When: clearing rows
        ClearRow clearRow = board.clearRows();

        // Then: no full lines should be removed
        assertNotNull(clearRow, "clearRows() should not return null");
        assertEquals(0, clearRow.getLinesRemoved(),
                "No full rows should be removed");
    }
}
