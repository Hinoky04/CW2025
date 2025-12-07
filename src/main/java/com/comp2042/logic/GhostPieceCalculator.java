package com.comp2042.logic;

/**
 * Calculates the landing position (ghost piece) for a brick if it were hard-dropped.
 * Used to display a shadow/preview of where the brick will land.
 */
public class GhostPieceCalculator {
    
    private final int rows;
    private int[][] boardMatrix;  // Not final - can be updated when board changes
    
    /**
     * Creates a ghost piece calculator for a board with the given dimensions.
     * 
     * @param rows the number of rows in the board
     * @param boardMatrix the current board state (reference, not copied)
     */
    public GhostPieceCalculator(int rows, int[][] boardMatrix) {
        this.rows = rows;
        this.boardMatrix = boardMatrix;
    }
    
    /**
     * Computes the final Y coordinate where the given shape would land
     * if it were hard-dropped from (startX, startY) straight down.
     * 
     * @param startX the starting X coordinate
     * @param startY the starting Y coordinate
     * @param shape the brick shape matrix
     * @return the Y coordinate where the brick would land
     */
    public int computeLandingY(int startX, int startY, int[][] shape) {
        // Start from the current position and move down until we can't go further
        int landingY = startY;
        
        // Keep moving down one row at a time while the next position is valid
        while (true) {
            // Check if we can move down one more row
            int nextY = landingY + 1;
            
            // If next position would be out of bounds, stop here
            if (nextY >= rows) {
                break;
            }
            
            // Check if there's a collision at the next position
            if (MatrixOperations.intersect(boardMatrix, shape, startX, nextY)) {
                break;
            }
            
            // No collision, can move down
            landingY = nextY;
        }
        
        return landingY;
    }
    
    /**
     * Updates the board matrix reference when the board state changes.
     * This is necessary because mergeBrickToBackground() creates a new array.
     * 
     * @param newBoardMatrix the new board matrix
     */
    public void updateBoardMatrix(int[][] newBoardMatrix) {
        this.boardMatrix = newBoardMatrix;
    }
}

